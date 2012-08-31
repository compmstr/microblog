(ns microblog.blog
  (:require [microblog.db :as db])
  (:require [microblog.nav :as nav])
  (:require [microblog.user :as user])
  (:require [microblog.template :as template])
  (:require [microblog.socket-conn :as socket-conn])
  (:import [java.util Date])
  (:use microblog.util
        ring.util.response
        [clojure.data.json :only [json-str read-json]]
        [net.cgrand.moustache :exclude [not-found]]
        net.cgrand.enlive-html))


(defsnippet single-blog-post "templates/base.html"
  [:#left-wrapper [:.post first-of-type]]
  [title timestamp body]
  [:.post-title :h3] (content title)
  [:.post-date] (content timestamp)
  [:.post-content] (html-content body))

(defn blog-post [req]
  (let [title ((:params req) "title")
        body ((:params req) "body")
        author (logged-in-user req)
        date (Date.)]
      (println "Author:" author "\nTitle:" title "\nBody:" body)
      (socket-conn/write-broadcast
       (json-str {:message "blog-added" :new-entry (snippet-to-string
                                                    single-blog-post title (.toString date) body)}))
      (db/insert-record :microblog {:title title 
                                    :body body 
                                    :author (:uid author) 
                                    :timestamp date})
      (-> (response
           (json-str {:status "success" :data "Posted"}))
          (content-type "application/json"))))

(defn get-blog-posts []
  (db/select-result ["SELECT * FROM microblog ORDER BY timestamp DESC"]))

(defsnippet show-blogs "templates/base.html"
  [:#left-wrapper [:.post first-of-type]]
  [req]
  [:.post] (clone-for [entry (get-blog-posts)]
                      (substitute (single-blog-post (:title entry) (.toString (:timestamp entry)) (:body entry)))))

(defsnippet add-blog "templates/base.html"
  [:#left-wrapper [:.add-post (nth-of-type 1)]]
  [req]
  [:*] identity)

(defsnippet* add-post-snip
  (template/base-snip topnav req)
  [:html]
  [topnav req]
  [:div#left-wrapper] (let [user (logged-in-user req)]
                        (content (add-blog req))))

(defn add-post [req]
  (let [cur-user (logged-in-user req)]
    ;(if (and cur-user (.contains (:permissions cur-user) "add_posts"))
    (if (and
         cur-user
         (->
          cur-user
          :permissions
          (.contains "add_posts")))
      (snippet-to-response add-post-snip nav/main-navmenu req)
      (template/noauth-response))))
      

(def routes
  (app
   :get (app
         ["add"] add-post
         [&] pass)
   :post (app
          ["post"] blog-post)))
