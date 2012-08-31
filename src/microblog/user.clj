(ns microblog.user
  (:require [microblog.db :as db])
  (:import org.mindrot.jbcrypt.BCrypt)
  (:use microblog.util
        ring.util.response
        [net.cgrand.moustache :only [app]]
        [clojure.data.json :only [json-str read-json]]
        net.cgrand.enlive-html))

(defsnippet login-box "templates/base.html"
  [:div#login-container]
  [req & data]
  ;To select two independent elements, use a set
  #{[:.login-link] [:#login-overlay]} (if (:logged-in (:session req))
                  (remove-element)
                  identity)
  [:.login-message] (let [data (first data)]
                      (if (and data (data :message))
                        (content (data :message))
                        (remove-element)))
  [:#username-entry] (if (and (:params req) ((:params req) "username"))
                      (set-attr :value ((:params req) "username"))
                      identity)
  [:.loggedin-message] (if (:logged-in (:session req))
                  identity
                  (remove-element))
  [:.loggedin-message :#username-message] 
      (if (:logged-in (:session req))
        (content (:fname (:logged-in (:session req))))))

(defn get-user 
  "Returns a user from the DB that matches the username/password passed in
   Returns a customer object (map) if successful
   Returns a string if an error occurs"
  [username pass]
  (println "Logging in" username "with password:" pass)
  (let [users (db/select-result ["select * from users where username = ?" username])
        user (first users)]
    (if (= (count users) 0)
      "User not found"
      (if (BCrypt/checkpw pass (:password user))
        user
        "Password didn't match"))))

(defn logout-ajax [req]
  (println "Logging out...")
  (let [updated-req (set-logged-in req nil)]
    (set-logged-in
      (merge
        (response
          (json-str {:status "success" :data "Logged Out"
                      :updates {:login-container (apply str
                                                  (emit*
                                                   ;Redirect after logout
                                                    (js-node "window.location='http://localhost:8888'")))}}))
          {:headers {"Content-type" "application/json"}})
      nil)))

(defn login-ajax [req]
  (println "Logging in...")
  (let* [username ((:params req) "username")
         pass ((:params req) "password")
         user (get-user username pass)
         logged-in (map? user)]
      (if logged-in ;if we logged in successfully:
        (let* [updated-req (set-logged-in req user)
             res
              (merge
                (response
                  (json-str 
                    {:status "success" :data "Logged In" 
                    :updates {:login-container (apply str 
                                                (emit* 
                                                  (login-box updated-req)))}}))
                {:headers {"Content-type" "application/json"}})]
          (set-logged-in res user))
        (let* [updated-req (set-logged-in req nil) ;login failed
              res
              (merge
                (response
                  (json-str {:status "failed" :data user
                             :updates {:login-container (apply str
                                                        (emit*
                                                          (login-box updated-req {:message user})))}}))
                {:headers {"Content-type" "application/json"}})]
            (println "Login failed -" user)
            (set-logged-in res nil)))))

(def routes
  (app
    ["login"] login-ajax
    ["logout"] logout-ajax))

