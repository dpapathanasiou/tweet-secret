(ns tweet-secret.core-test
  (:require [clojure.test :refer :all]
            [tweet-secret.core :as core :refer :all]
            [tweet-secret.config :as config]
            [tweet-secret.utils  :as utils]
            [tweet-secret.languages :as languages]))


; Test parameters for English

(def english-dict     '"http://www.cs.duke.edu/~ola/ap/linuxwords")
(def english-corpus   '"http://www.gutenberg.org/cache/epub/1661/pg1661.txt")
(def english-messages '["Hit me baby"])
(def english-tweets   '["If the lady loves her husband·, she does not love your Majesty."
                        "I have made a small study of tattoo marks and have even· contributed to the literature of the subject."
                        "It is in a German-speaking country--in Bohemia, not far from Ca·rlsbad."])

(deftest english-encode
  "This tests the encoding of an English-language message, using a static corpus from gutenberg.org,
   and a static, universally available dictionary text found online (rather than the linux words file
   as defined in config.properties, which can vary from distro to distro and computer to computer)."
  
  (testing "Encoding a message in English"
    (binding [core/*dictionary-text* (utils/slurp-url-or-file english-dict)]
      (let [eligible-tweets (core/get-eligible-tweets (core/parse-corpus core/*corpus-parse-fn* (core/load-corpus english-corpus)))]
        (is (not (nil? core/*dictionary-text*)))
        (is (= english-tweets (core/encode-plaintext (languages/en-tokenize-words english-messages) eligible-tweets)))))))

(deftest english-decode
  "This tests the decoding of a series of English-language tweets, using a static corpus from gutenberg.org,
   and a static, universally available dictionary text found online (rather than the linux words file
   as defined in config.properties, which can vary from distro to distro and computer to computer).
   Since the encoding/decoding process does not preserve case in English, the output of the decode function
   is forced into lower case, to make sure the test passes if the only difference with the input is case."

  (testing "Decoding tweets back to the original English message"
    (binding [core/*dictionary-text* (utils/slurp-url-or-file english-dict)]
      (let [eligible-tweets (core/get-eligible-tweets (core/parse-corpus core/*corpus-parse-fn* (core/load-corpus english-corpus)))]
        (is (not (nil? core/*dictionary-text*)))
        (is (= (map #(clojure.string/lower-case %) (core/decode-tweets eligible-tweets english-tweets))
               (map #(clojure.string/lower-case %) (languages/en-tokenize-words english-messages))))))))
  
