(ns microblog.blog
  (:require [microblog.db :as db])
  (:use microblog.util
        ring.util.response
        [clojure.data.json :only [json-str read-json]]
        [net.cgrand.moustache :exclude [not-found]]
        net.cgrand.enlive-html))

(defn blog-post [req]
  (let [title ((:params req) "title")
        body ((:params req) "body")
        author (logged-in-user req)]
      (println "Author:" author "\nTitle:" title "\nBody:" body))
  (merge
    (response
      (json-str {:status "success" :data "Posted"}))
      {:headers {"Content-type" "application/json"}}))

(def routes
  (app
    ["post"] blog-post))
