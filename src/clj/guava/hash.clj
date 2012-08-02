(ns ^{:doc "Clojure wrapper of com.google.common.hash"
      :author "xumingming"}
  clj.guava.hash
  (:use [clj.guava.base :only [check-arg]])
  (:import [com.google.common.hash HashFunction Hashing Hasher BloomFilter
            Funnel PrimitiveSink]))

(def ^{:added "0.1" :doc "All the hash algorithm supported."}
  ALGORITHMS {:md5 (Hashing/md5)
              :murmur3-32 (Hashing/murmur3-32)
              :murmur3-128 (Hashing/murmur3-128)
              :sha1 (Hashing/sha1)
              :sha256 (Hashing/sha256)
              :sha512 (Hashing/sha512)
              :good-fast-hash (Hashing/goodFastHash 32)})

(def ^{:added "0.1" :dynamic true :doc "seed for :murmur3_128 and :murmur3_32"} *seed* 0)
(def ^{:added "0.1" :dynamic true :doc "minimumBits for :good-fast-hash"} *min-bits* 32)
(def ^{:added "0.1" :doc "Funnel used to handle clojure simple structures"}
  CLOJURE-FUNNEL
  (reify Funnel
    (^void funnel [this from ^PrimitiveSink into]
      (doseq [value (vals from)]
        (cond
         (string? value) (.putString into value)
         (char? value) (.putChar into value)
         (instance? Boolean value) (.putBoolean into value)
         (instance? Double value) (.putDouble into value)
         (instance? Float value) (.putFloat into value)
         (instance? Long value) (.putLong into value)
         (instance? Integer value) (.putInt into value)
         (instance? Short value) (.putShort into value)
         (instance? Byte value) (.putByte into value))))))

(defn hash
  "Generates a hash number according to the algorithm specified and the input data provided.

   Usage:
     (hash :md5  :james 25)    => -8648192032652653463
     (hash :sha256 :james 25)  => -6742704120543053434

   The following hash algorithm are supported:
     - :md5
     - :murmur3-32
     - :murmur3-128
     - :sha1
     - :sha256
     - :sha512
     - :good-fast-hash

  For :murmur3-32 and :murmur3-128, you can pass a dynamic var: *seed* to change the default seed(default 0).
  For :good-fast-hash you can pass in a *min-bits* to specify the minimum bits of hash(default 32)"
  {:added "0.1" :tag long}
  [algorithm & xs]
  (check-arg (contains? ALGORITHMS algorithm) (str "Algorithm " algorithm " not supported!"))
  (check-arg (not-empty xs) "No data supported to the hash.")
  (let [^HashFunction hf (cond
                          (and (= :good-fast-hash algorithm)
                               (thread-bound? #'*min-bits*)) (Hashing/goodFastHash *min-bits*)
                          (and (contains? #{:murmur3-32 :murmur3-128} algorithm)
                               (thread-bound? #'*seed*)) (if (= :murmur3-128 algorithm)
                                                          (Hashing/murmur3-128 *seed*)
                                                          (Hashing/murmur3-32 *seed*))
                          :else (ALGORITHMS algorithm))
        ^Hasher hasher (.newHasher hf)]
    (doseq [x xs]
      (cond
       (instance? Byte x) (.putByte hasher ^Byte x)
       (instance? Short x) (.putShort hasher ^Short x)
       (instance? Integer x) (.putInt hasher ^Integer x)
       (instance? Long x) (.putLong hasher ^Long x)
       (instance? Float x) (.putFloat hasher ^Float x)
       (instance? Double x) (.putDouble hasher ^Double x)
       (instance? Boolean x) (.putBoolean hasher ^Boolean x)
       (char? x) (.putChar hasher ^Character x)
       (string? x) (.putString hasher ^String x)
       (instance? clojure.lang.IPersistentMap x) (.putObject hasher ^Object x CLOJURE-FUNNEL)
       ;; For other types, just use its string representation
       :else (.putString hasher (str x))))
    (-> hasher .hash Hashing/padToLong)))

(defn bloom-filter 
  "Creates a bloom filter with the expected insertion number and false positive probability.

  The data which are expected to be put into includes usual map and record -- we support nested map/record."
  {:added "0.1" :tag BloomFilter}
  [expected-insertions false-positive-probability]
  (BloomFilter/create CLOJURE-FUNNEL expected-insertions false-positive-probability))

(defn bloom-put!
  "Puts data into bloom filter"
  {:added "0.1"}
  [^BloomFilter bloom-filter x]
  (.put bloom-filter x))

(defn bloom-contains?
  "Checks whether a specified value is in the bloom-filter, if
   the return is false, then the value is absolutely NOT in the filter.
   but if the return value is true, it means MAYBE in the filter. "
  {:added "0.1" :tag boolean}
  [^BloomFilter bloom-filter x]
  (.mightContain bloom-filter x))


(defn consistent-hash
  "Assigns to input a 'bucket' in the range [0, buckets), in a uniform
   manner that minimizes the need for remapping as buckets grows. That is,
   consistent-hash(h, n) equals:
     : n - 1, with approximate probability 1/n
     : consistent-hash(h, n - 1), otherwise (probability 1 - 1/n)

   See the <a href='http://en.wikipedia.org/wiki/Consistent_hashing'>wikipedia
   article on consistent hashing</a> for more information."
   {:added "0.1" :tag Integer}
   [^long hash ^Integer buckets]
  (Hashing/consistentHash hash buckets))