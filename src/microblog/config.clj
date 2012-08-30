(ns microblog.config)

(def socket-io {:name "localhost" :port 1337})

(def mysql-db {:subprotocol "mysql"
               :subname "//localhost:3306/test"
               :user "test_user"
               :password "password"})

