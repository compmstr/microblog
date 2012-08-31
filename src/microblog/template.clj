(ns microblog.template
  (:require
   [microblog.user :as user]
   [microblog.nav :as nav]
   [microblog.config :as config])
  (:use
   microblog.util
   net.cgrand.enlive-html))

(defsnippet base-snip "templates/base.html" [:html] [topnav req]
  [:div#admin-bar] (content (user/admin-bar req))
  [:h1#site-title] (content {:tag :a :attrs {:href config/home-url} :content "App Testbed"})
  [:div#top-nav :ul] (content (map nav/topnav-item topnav))
  [:div#footer] (html-content "Copyright &copy; 2012")
  [:div#login-container] (content (user/login-box req))
  )

(deftemplate no-auth "templates/noauth.html"
  [])

(defn noauth-response
  []
  {:status 403 :headers {"Content-type" "text/html"} :body (no-auth)})

(deftemplate page-404 "templates/404.html"
  [])
(defn page-404-response [& req]
  {:status 404 :headers {"Content-type" "text/html"} :body (page-404)})

;;Regular template
;(deftemplate index "templates/base.html"
  ;[topnav req]
  ;;To apply a snippet to a list of items, use map
  ;[:div#top-nav :ul] (content (map nav/topnav-item topnav))
  ;[:div#left-wrapper] (content (blog/show-blogs req))
  ;[:div#footer] (html-content "Copyright &copy; 2012")
  ;[:div#login-container] (content (user/login-box req)))

