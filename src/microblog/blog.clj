(ns microblog.blog
  (:require [microblog.db :as db])
  (:require [microblog.nav :as nav])
  (:require [microblog.user :as user])
  (:import [java.util Date])
  (:use microblog.util
        ring.util.response
        [clojure.data.json :only [json-str read-json]]
        [net.cgrand.moustache :exclude [not-found]]
        net.cgrand.enlive-html))

(defn blog-post [req]
  (let [title ((:params req) "title")
        body ((:params req) "body")
        author (logged-in-user req)]
      (println "Author:" author "\nTitle:" title "\nBody:" body)
      (db/insert-record :microblog {:title title 
                                    :body body 
                                    :author (:uid author) 
                                    :timestamp (Date.)})
  (merge
    (response
      (json-str {:status "success" :data "Posted"}))
      {:headers {"Content-type" "application/json"}})))

(defn get-blog-posts []
  (db/select-result ["select * from microblog"]))

(defsnippet show-blogs "templates/base.html"
  [:#left-wrapper [:.post first-of-type]]
  [req]
  [:.post] (clone-for [entry (get-blog-posts)]
                                [:.post-title] (content (:title entry))
                                [:.post-date] (content (.toString (:timestamp entry)))
                                [:.post-content] (content (:body entry))))

(defsnippet add-blog "templates/base.html"
  [:#left-wrapper [:.add-post (nth-of-type 1)]]
  [req]
  [:*] identity)

(deftemplate add-post "templates/base.html"
  [topnav req]
  [:div#top-nav :ul] (content (map nav/topnav-item topnav))
  [:div#footer] (html-content "Copyright &copy; 2012")
  [:div#login-container] (content (user/login-box req))
  [:div#left-wrapper]
    (let [user (logged-in-user req)]
      (content (add-blog req))))


(def routes
  (app
    ["add"] #(response (add-post nav/main-navmenu %))
    ["post"] blog-post))
