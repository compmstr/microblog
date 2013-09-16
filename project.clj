(defproject microblog "0.0.1"
  :description "Microblog example using Mustache and Enlive"
  :source-path "src"
  :main microblog.core
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [enlive "1.0.0"]
                 [net.cgrand/moustache "1.1.0"]
                 [org.clojure/data.json "0.1.3"]
                 [org.clojure/java.jdbc "0.1.1"]
                 [mysql/mysql-connector-java "5.1.6"]
                 [org.mindrot/jbcrypt "0.3m"]
                 [ring/ring-core "1.1.0"]
                 [ring/ring-devel "1.1.0"]
                 [ring/ring-jetty-adapter "1.1.0"]])
