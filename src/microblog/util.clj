(ns microblog.util)

;;Enlive helper functions
(defn remove-element 
  "Transformation -- removes element from tree"
  []
  (fn [elt] nil))

;;Ring helper functions
(defn merge-into-session 
  "Updates only the keys within the passed in session object that are
    set inside newdata"
  [req newdata]
  (merge req {:session (merge (req :session) newdata)}))

(defn set-logged-in 
  "Sets the logged in flag on the request/response passed in"
  [req curuser]
  (merge-into-session req {:logged-in curuser}))

(defn logged-in-user
  "Gets the currently logged in user for a request"
  [req]
  ((:session req) :logged-in))
  
(defn response-set-headers [res headers]
  (merge res
    {:headers headers}))

(defn wrap-session-print
  [handler & opts]
  (fn [req]
    (println (req :session))
    (handler req)))

