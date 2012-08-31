(ns microblog.socket-conn
  "Write-only socket connection used to send broadcast messages to socket.io"
  (:require [microblog.config :as config])
  (:require [clojure.string :as string])
  (:import (java.net Socket)
           (java.io PrintWriter BufferedReader InputStreamReader)))

(declare start-conn)
(defonce conn (ref nil))

(defn connect-in-5-seconds []
  (.start (Thread. (fn []
                     (Thread/sleep 5000)
                     (start-conn)))))

(defn connect
  "Connects to a server, returning a ref to the conection"
  [server]
  (let [socket (Socket. (:name server) (:port server))
        out (PrintWriter. (.getOutputStream socket))
        in (BufferedReader. (InputStreamReader. (.getInputStream socket)))
        last-ping (System/currentTimeMillis)
        conn {:socket socket :out out :in in :last-ping last-ping :last-pong last-ping}]
    conn))

(defn disconnect []
  (dosync
   (alter
    conn merge {:exit true})))

(defn write [msg]
  (if (not (nil? (:out @conn)))
    (doto (:out @conn)
      (.println (str msg "\r"))
      (.flush))))

(defn write-broadcast
  "Sends a message over the socket prepended with 'BROADCAST '"
  [msg]
  (write (str "BROADCAST " msg)))

(defn keep-connection []
  (while (nil? (:exit @conn))
    (if (or (nil? @conn)
            (not (.isConnected (:socket @conn))))
      (do ;if we're not connected
        (println "Connection closed, reconnecting")
        (dosync
         (ref-set conn (connect config/socket-io))))
      (do ;if we're connected
        ;Check for PONG <timestamp> messages
        (if (.ready (:in @conn))
          (let [msg (.readLine (:in @conn))]
            (if (.startsWith msg "PONG")
              (let [split-msg (string/split msg #"\s+")]
                ;(println "Pong")
                (dosync
                 (alter conn merge {:last-pong (Long/parseLong (second split-msg))}))))))
        (let [cur-time (System/currentTimeMillis)
              next-ping (+ (:last-ping @conn) 5000)
              pong-timeout (+ (:last-pong @conn) 15000)]
          ;Send out a PING <timestamp> message every 5 seconds
          ; unless the last ping is greater than the last pong (we sent out a ping last)
          (if (and
               (= (:last-ping @conn) (:last-pong @conn))
               (>= cur-time next-ping))
            (do
              ;(println "Ping")
              (dosync
               (alter conn merge {:last-ping cur-time}))
              (write (str "PING " cur-time))))
          ;;Timeout if there's been no pong in 15 seconds
          (if (>= cur-time pong-timeout)
            (do
              (println "PONG timeout!")
              (disconnect)
              (connect-in-5-seconds))))))
    (Thread/sleep 100))
  (println "Closing connection to socket")
  (.close (:out @conn))
  (.close (:socket @conn))
  (dosync
   (ref-set conn nil)))
  
(defn start-conn []
  (try
    (dosync
     (ref-set conn (connect config/socket-io)))
     (.start (Thread. keep-connection))
     (catch Exception e
       ;(println "Error connecting to socket.io:" (.getMessage e) "-- Retrying in 5 seconds")
       (connect-in-5-seconds))))
