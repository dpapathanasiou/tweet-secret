
(ns tweet-secret.config
  (:use [clojure.java.io :only (reader)])
  (:import (java.util Properties)))

(defn load-properties [src]
  "Load the given properties file and make it available as a Properties hash"
  (with-open [rdr (reader src)]
    (doto (Properties.)
      (.load rdr))))

(defn get-property [k]
  "Lookup and return the value of the given key in this app's properties object"
  (let [app-config (load-properties "config.properties")]
    (when app-config
      (.get app-config k))))
