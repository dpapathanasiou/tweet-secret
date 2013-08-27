
(ns tweet-secret.utils)

(defn join-strings [strings-list & [sep]]
  "Join the list of strings using the optional separator string using clojure's (reduce #(str %1 %2) ...) logic.
   For improved readability and comprehension, using this util method over the native clojure combination."
  (if sep
    (reduce #(str %1 sep %2) strings-list)
    (reduce #(str %1 %2) strings-list)))

