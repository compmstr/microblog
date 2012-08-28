(ns microblog.core
  (:require [microblog.db :as db]
            [microblog.user :as user]
            [microblog.nav :as nav]
            [microblog.blog :as blog])
  (:use ring.util.response
        ring.middleware.session
        ring.middleware.params
        ring.middleware.resource
        ring.middleware.file-info
        [clojure.data.json :only [json-str read-json]]
        net.cgrand.enlive-html
        [net.cgrand.moustache :exclude [not-found]]
        [ring.adapter.jetty :only [run-jetty]]
        microblog.util))

(deftemplate index "templates/base.html"
  [topnav req]
  ;To apply a snippet to a list of items, use map
  [:div#top-nav :ul] (content (map nav/topnav-item topnav))
  [:div#left-wrapper] (content (blog/show-blogs req))
  [:div#footer] (html-content "Copyright &copy; 2012")
  [:div#login-container] (content (user/login-box req)))

;Return the template with no transforms
(deftemplate raw "templates/base.html"
  [])

(defn json-response [req]
  (merge
    (response 
      (json-str {:status "success" :data "haha, you thought there would be data"}))
      {:headers {"Content-type" "application/json"}}))

(def my-app-handler
  (app
    wrap-params
    wrap-session
    wrap-params
    ;Using wrap-resource because it goes off the classpath
    (wrap-resource "resources")
    (wrap-file-info)
    wrap-session-print
    ["json"] json-response
    ["raw"] (-> (raw) response constantly)
    ["blog" &] blog/routes
    ["user" &] user/routes
    [""] #(response (index nav/main-navmenu %))))

(defonce server
  (run-jetty #'my-app-handler {:port 8888
                               :join? false}))

(defn reload [] (.stop server) (load "core") (.start server))
