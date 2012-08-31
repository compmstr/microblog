(ns microblog.util
  (:use net.cgrand.enlive-html
        ring.util.response))

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
  (if (:session req)
    ((:session req) :logged-in)
    nil))
  
(defn response-set-headers [res headers]
  (merge res
    {:headers headers}))

(defn wrap-session-print
  [handler & opts]
  (fn [req]
    (println (req :session))
    (handler req)))

(defn snippet-to-string [snip & args]
  (apply str
         (emit* (apply snip args))))

(defn snippet-to-response
  "Converts a snippet to a response, pass in the snippet, and the args to pass that snippet"
  [snip & args]
  (response
   (apply snippet-to-string snip args)))

(defn template-from-snippet
  "Turns a snippet into a template, basically just wraps it in emit*"
  [snippet]
  (comp #(apply str %) emit* snippet))

(defmacro defsnippet*
"Snippet definition using nodes instead of a source file
You can use the args in the nodes form (ex: passing the request to the snippet used as a base"
  [name nodes selector args & forms]
  `(def ~name
     (fn ~args
       ((net.cgrand.enlive-html/snippet*
         (net.cgrand.enlive-html/select ~nodes ~selector) ~args ~@forms)
        ~@args))))

(defn js-node
  [content]
  {:tag :script :attrs {:language "javascript"} :content content})

(defn remote-js-node
  [url]
  {:tag :script :attrs {:language "javascript" :src url} :content nil})
