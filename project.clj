(defproject tweet-secret "1.0"
  :description "Steganography for Twitter"
  :url "http://github.com/dpapathanasiou/tweet-secret"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.cli "0.2.4"]]
  :main tweet-secret.core
  :aot [tweet-secret.core])
