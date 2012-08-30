(ns microblog.db
  (:import java.util.Date)
  (:import org.mindrot.jbcrypt.BCrypt)
  (:require [microblog.config :as config])
  (:require [clojure.java.jdbc :as sql]))

(def db-schema {
          :microblog [
            [:id :integer "PRIMARY KEY" "AUTO_INCREMENT"]
            [:author :integer]
            [:title "varchar(255)"]
            [:body :text]
            [:timestamp :datetime "NOT NULL"]]
          :users [
            [:uid :integer "PRIMARY KEY" "AUTO_INCREMENT"]
            [:username "varchar(255)"]
            [:password "varchar(255)"]
            [:fname "varchar(255)"]
            [:lname "varchar(255)"]
            [:permissions :text]]
          })

(declare insert-record)
(declare create-tables)

(defn setup-initial-users []
  (insert-record :users {:username "guest" 
                         :fname "guest"
                         :lname "guest"
                         :password (BCrypt/hashpw "pass" (BCrypt/gensalt 12))
                         :permissions "view_posts"})
  (insert-record :users {:username "admin" 
                         :fname "admin"
                         :lname "admin"
                         :password (BCrypt/hashpw "admin_pass" (BCrypt/gensalt 12))
                         :permissions "view_posts,add_posts"}))

(defn install-db []
  (create-tables db-schema)
  (setup-initial-users))

(defn create-table [args]
  "Creates a DB table, taking in a vector of args to pass
  through to the main sql/create-table
  "
  (sql/with-connection config/mysql-db
    (sql/transaction
      (apply sql/create-table args))))
(defn create-tables [tables]
  (map (fn [[key val]]
          (create-table
            (concat (list key)
                    val)))
        tables))
(defn insert-record [table record]
  (sql/with-connection config/mysql-db
    (sql/insert-record table record )))
(defn select-result [query]
  (sql/with-connection config/mysql-db
    (sql/with-query-results results
      query
      (into [] results))))
;-> [{:data "Hello World"}]
(defn drop-table [table]
  (sql/with-connection config/mysql-db
    (sql/drop-table table)))

(defn test-db []
  (println (create-table (db-schema :microblog)))
  (println (insert-record :microblog {:title "Initial Post" :body "Hello World" :timestamp (Date.)}))
  (println (select-result ["Select * from microblog"]))
  (println (drop-table :microblog)))

