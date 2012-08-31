(ns microblog.config)

(def socket-io {:name "localhost" :port 1337})
(def client-socket-io "http://localhost:1338/socket.io/socket.io.js")
(def home-url "http://localhost:8888")

(def mysql-db {:subprotocol "mysql"
               :subname "//localhost:3306/test"
               :user "test_user"
               :password "password"})

