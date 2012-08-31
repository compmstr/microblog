(ns microblog.core
  (:require [microblog.db :as db]
            [microblog.user :as user]
            [microblog.nav :as nav]
            [microblog.template :as template]
            [microblog.config :as config]
            [microblog.blog :as blog]
            [microblog.socket-conn :as socket-conn])
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

(defsnippet* index-snip
  (template/base-snip topnav req)
  [:html]
  [topnav req]
  ;To apply a snippet to a list of items, use map
  [:head] (append (js-node "var change_this = 5;")
                  (remote-js-node "http://localhost:1338/socket.io/socket.io.js")
                  (remote-js-node "/js/update-posts.js"))
  [:div#left-wrapper] (content (blog/show-blogs req))
  )
  
;Return the template with no transforms
(deftemplate raw "templates/base.html"
  [])

(defn json-response [req]
  (->
   (response
    (json-str {:status "success" :data "haha, you thought there would be data"}))
   (content-type "application/json")))

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
    [""] #(snippet-to-response index-snip nav/main-navmenu %)
    [&] template/page-404-response))

;Start up the socket.io connection
(if (nil? @socket-conn/conn)
  (socket-conn/start-conn))

(defonce server
  (run-jetty #'my-app-handler {:port 8888
                               :join? false}))

(defn reload []
  (.stop server)
  (map load ["template" "db" "user" "nav" "blog" "core"])
  (.start server))
