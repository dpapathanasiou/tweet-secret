(ns tweet-secret.core
  (:require [tweet-secret.config :as config]
            [tweet-secret.utils  :as utils])
  (:use [clojure.tools.cli :only [cli]]))

(def ^:dynamic *excess-marker*   (config/get-property "excess-marker")) ; the unobtrusive marker within the broadcast tweet text
(def ^:dynamic *tweet-size*      (try
                                   (-
                                    (Integer/parseInt (config/get-property "tweet-size"))
                                    (.length *excess-marker*))
                                   (catch NumberFormatException _ 0)))
(def ^:dynamic *dictionary-text* (utils/join-strings
                                  (map #(slurp %) (clojure.string/split (config/get-property "dictionary-files") #" "))
                                  "\n"))

(defn load-corpus [url-or-filename]
  "Fetch the text content contained in either the url or filename and return a string, with all whitespace normalized as plain text space"
  (try
    (clojure.string/replace
     (clojure.string/replace (slurp url-or-filename) #"\s" " ")
     #"\s{2,}" " ")
    (catch Exception _ nil)))

(defn parse-corpus [corpus-text]
  "Convert the corpus-text string into a list of parsed sentences (using a basic English grammar regex)"
  (when corpus-text
    (map #(clojure.string/triml %)
         (map #(clojure.string/trimr %)
              (clojure.string/split
               (clojure.string/replace corpus-text #"([.?!;])\s{1}" "$1\u0090") ; U+0090 = device control string (i.e., this is not expected to be in the corpus-text)
               #"\u0090")))))

(defn get-eligible-tweets [sentences]
  "Filter the list of sentences and return those which would fit into a tweet, leaving room for the excess marker character"
  (filter #(and (<= (count %) *tweet-size*) (> (count %) 0)) sentences))

(defn get-matching-tweet [eligible-tweets target-length]
  "Find the element within the list of tweet strings where the collective string length meets or exceeds the target length, and return its index, along with the excess amount"
  (if (> target-length (apply + (map #(.length %) eligible-tweets)))
    (list -1 0)
    (loop [eligible-tweets eligible-tweets, counted-length 0, ind -1]
      (if (>= counted-length target-length)
        (list ind (- counted-length target-length))
        (recur (rest eligible-tweets) (+ counted-length (.length (first eligible-tweets))) (inc ind))))))

(defn generate-tweet [eligible-tweets target-number]
  "'Encode' the string to tweet for this target number, marking the excess point in the string (if any) with the excess marker"
  (let [match (get-matching-tweet eligible-tweets target-number)]
    (when (> (first match) -1)
      (let [matching-tweet (nth eligible-tweets (first match))]
        (if (= 0 (second match))
          matching-tweet
          (str (.substring matching-tweet 0 (second match)) *excess-marker* (.substring matching-tweet (second match))))))))

(defn find-target [eligible-tweets broadcast-tweet]
  "'Decode' the broadcast-tweet by removing the excess marker, look it up vs the collective length of the eligible tweets, account for the excess, returning the target number"
  (let [original-tweet (utils/join-strings (utils/split-string broadcast-tweet *excess-marker*))]
    (loop [eligible-tweets eligible-tweets, counted-length 0]
      (if (= (first eligible-tweets) original-tweet)
        (- (+ counted-length (.length (first eligible-tweets))) (.indexOf broadcast-tweet *excess-marker*))
        (recur (rest eligible-tweets) (+ counted-length (.length (first eligible-tweets))))))))

(defn grep-dictionary [w dict-text]
  "Do the equivalent of grep -in '^[word]$' versus the dict-text string (the contents of a dictionary file) and return the matching line number, if any"
  (try
    (let [m (re-seq (re-pattern (str "(?i)\n" w "\n")) dict-text)]
      (+ 1 (count (utils/split-string (.substring dict-text 0 (.indexOf dict-text (first m))) "\n"))))
    (catch Exception _ -1)))

(defn lookup-dictionary [line-number dict-text]
  "The opposite of grep-dictionary, this function takes a physical line number and returns the word found there in the dictionary file"
  (try
    (last (take line-number (utils/split-string dict-text "\n")))
    (catch Exception _ nil)))

(defn encode-plaintext [message eligible-tweets]
  "Convert a string of words into encoded tweets for broadcast"
  (map #(generate-tweet eligible-tweets (grep-dictionary % *dictionary-text*)) (utils/split-string message " ")))

(defn decode-tweets [eligible-tweets broadcast-tweets]
  "Convert the list of broadcast tweets back into their original, secret message"
  (map #(lookup-dictionary (find-target eligible-tweets %) *dictionary-text*) broadcast-tweets))

(defn -main [& args]
  "Command line entry point for both encoding plaintext and decoding tweets"

  ; make sure the properties data is defined correctly
  (when (< (.length *tweet-size*) 1)
    (println "\nSorry, the tweet-size in the config.properties file is not defined correctly. Please use a positive integer and try again.\n")
    (System/exit 0))
  
  (let [[options args banner]
        (cli args
             "tweet-secret: Text steganography optimized for Twitter"
             ["-c" "--corpus" "REQUIRED: at least one url or full path filename of the secret corpus text(s) known only by you and your friends"
              :assoc-fn (fn [previous key val]
                          (assoc previous key
                                 (if-let [oldval (get previous key)]
                                   (conj oldval val)
                                   (vector val))))]
             ["-d" "--decode" "Decode this tweet into plaintext (if none present, text after all the option switches will be encoded)"
              :assoc-fn (fn [previous key val]
                          (assoc previous key
                                 (if-let [oldval (get previous key)]
                                   (conj oldval val)
                                   (vector val))))]
             ["-h" "--help" "Show the command line usage help" :default false :flag true])]

    ; make sure the command-line arguments are correct
    (when (or (:help options)
              (< (count (:corpus options)) 1))
      (println banner)
      (System/exit 0))

    (let [eligible-tweets (get-eligible-tweets (parse-corpus (utils/join-strings (map #(load-corpus %) (:corpus options)) " ")))] ; make sure the corpus is large enough
      (when (> (count (utils/split-string *dictionary-text* "\n")) (apply + (map #(count %) eligible-tweets))) 
        (println "\nSorry, your corpus text is not large enough. Please use a larger text, or, include additional --corpus options and try again.\n")
        (System/exit 0))

      ; can now encode or decode as directed
      (if (= (count (:decode options)) 0)
        (encode-plaintext (utils/join-strings args " ") eligible-tweets)
        (decode-tweets eligible-tweets (:decode options))))))

