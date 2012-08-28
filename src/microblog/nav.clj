(ns microblog.nav
  (:use
        net.cgrand.enlive-html
  ))

(def main-navmenu [{:title "Slashdot" :url "http://slashdot.org"}
              {:title "Ars" :url "http://arstechnica.com"}
              {:title "Daily WTF" :url "http://thedailywtf.com"}])

;Outputting the main menu:
; (apply str (emit * (unwrap
;         ((content (map topnav-item main-navmenu)) {:tag :div}))))
(defsnippet topnav-item "templates/base.html" 
  ;just use the very first li as a template
  [:div#top-nav :ul [:li (nth-of-type 1)]] ;selector
  [navitem] ;args
  [:a] (do->
          (set-attr :href (:url navitem))
          (content (:title navitem))))

