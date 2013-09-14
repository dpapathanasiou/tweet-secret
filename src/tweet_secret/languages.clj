
(ns tweet-secret.languages
  (:require [tweet-secret.utils :as utils]))

; This file makes tweet-secret extensible by defining functions for
; handling different natural languages, taking into account grammatical
; rules and quirks of how words are delimited, e.g., with or without
; spaces in between, etc.
; 
; For each supported language, two functions need to be implemented:
; 
; (1) A function to parse a string into a list of sentence strings
; 
; (2) A function to split a message string into word tokens, where
; each token be expected to be found in one or more of the dictionary-files
; defined in config.properties
; 
; where (1) corresponds to corpus-parse-fn in config.properties and
; (2) corresponds to tokenize-fn in config.properties


; English

(defn en-parse-sentences [text]
  "Convert the English language text string into a list of sentence strings using a basic English grammar regex"
  (map #(clojure.string/triml %)
       (map #(clojure.string/trimr %)
            (clojure.string/split
             (clojure.string/replace text #"([.?!;])\s{1}" "$1\u0090") ; U+0090 = device control string (i.e., this is not expected to be in the text string)
             #"\u0090"))))

(defn en-tokenize-words [text]
  "Split the text string into individual word tokens"
  (utils/split-string (utils/join-strings text " ") " "))

