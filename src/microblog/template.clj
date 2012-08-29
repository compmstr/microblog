(ns microblog.template
  (:require
   [microblog.user :as user]
   [microblog.nav :as nav])
  (:use
   net.cgrand.enlive-html))

(defsnippet base-snip "templates/base.html" [:html] [topnav req]
  [:div#top-nav :ul] (content (map nav/topnav-item topnav))
  [:div#footer] (html-content "Copyright &copy; 2012")
  [:div#login-container] (content (user/login-box req))
  )


;;Regular template
;(deftemplate index "templates/base.html"
  ;[topnav req]
  ;;To apply a snippet to a list of items, use map
  ;[:div#top-nav :ul] (content (map nav/topnav-item topnav))
  ;[:div#left-wrapper] (content (blog/show-blogs req))
  ;[:div#footer] (html-content "Copyright &copy; 2012")
  ;[:div#login-container] (content (user/login-box req)))

