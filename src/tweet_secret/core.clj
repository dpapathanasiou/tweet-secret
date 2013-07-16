(ns tweet-secret.core)

(def *excess-marker* "\u2758") ; U+2758 = Light Vertical Bar (an unobtrusive marker within the broadcast tweet text)

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
  (filter #(and (<= (count %) (- 140 (.length *excess-marker*))) (> (count %) 0)) sentences))

(defn get-matching-tweet [eligible-tweets target-length]
  "Find the element within the list of tweet strings where the collective string length meets or exceeds the target length, and return its index, along with the excess amount"
  (if (> target-length (apply + (map #(.length %) eligible-tweets)))
    (list -1 0)
    (loop [eligible-tweets eligible-tweets, counted-length 0, ind 0]
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
  (let [original-tweet (reduce #(str %1 %2) (seq (.split broadcast-tweet *excess-marker*)))]
    (loop [eligible-tweets eligible-tweets, counted-length 0]
      (if (= (first eligible-tweets) original-tweet)
        (- counted-length (.indexOf broadcast-tweet *excess-marker*))
        (recur (rest eligible-tweets) (+ counted-length (.length (first eligible-tweets))))))))
