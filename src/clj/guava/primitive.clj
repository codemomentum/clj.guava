(ns ^{:doc "Wrapper of guava primitives"
      :author "qiuxiafei <qiuxiafei@gmail.com>"}
  clj.guava.primitive
  (:import [com.google.common.primitives
            Bytes SignedBytes UnsignedBytes
            Ints UnsignedInteger UnsignedInts
            Longs UnsignedLong UnsignedLongs
            Floats Doubles Chars Booleans Shorts]))

(set! *warn-on-reflection* true)

; length in bytes
(def ^{:tag Integer :added "0.1"}
  byte-bytes (int 1))

(def ^{:tag Integer :added "0.1"}
  char-bytes Chars/BYTES)

(def ^{:tag Integer :added "0.1"}
  int-bytes Ints/BYTES)

(def ^{:tag Integer :added "0.1"}
  float-bytes Floats/BYTES)

(def ^{:tag Integer :added "0.1"}
  double-bytes Doubles/BYTES)

(def ^{:tag Integer :added "0.1"}
  long-bytes Longs/BYTES)

(defn finite-double?
  "Return true if a double is neither Infinity nor NaN"
  ^{:tag Boolean :added "0.1"}
  [arg]
  (Doubles/isFinite arg))

; use Doubles here! Because clojure's / operator convert float Infinity into double!
(defn finite-float?
  "Return true if a float is neither Infinity nor NaN"
  ^{:tag Boolean :added "0.1"}
  [arg]
  (Doubles/isFinite arg))

; transform between primitives and byte array
(defn bytes->char
  "Parse a char from byte array representation"
  ^{:tag Character :added "0.1"}
  [bytes]
  (Chars/fromByteArray bytes))

(defn char->bytes
  ^{:tag bytes :added "0.1"}
  [c]
  (Chars/toByteArray c))

(defn bytes->int
  "Parse a int from byte array representation"
  ^{:tag Integer :added "0.1"}
  [bytes]
  (Ints/fromByteArray bytes))

(defn int->bytes
  ^{:tag bytes :added "0.1"}
  [n]
  (Ints/toByteArray n))

(defn short->bytes
  ^{:tag Short :added "0.1"}
  [s]
  (Shorts/toByteArray s))

(defn bytes->short
  "Parse a short from byte array representation"
  ^{:tag bytes :added "0.1"}
  [bytes]
  (Shorts/fromByteArray bytes))

(defn long->bytes
  ^{:tag bytes :added "0.1"}
  [l]
  (Longs/toByteArray l))

(defn bytes->long
  "Parse a long from byte array representation"
  ^{:tag Long :added "0.1"}
  [bytes]
  (Longs/fromByteArray bytes))

