(ns ^{:doc "Wrapper of guava primitives"
      :author "qiuxiafei <qiuxiafei@gmail.com>"}
  clj.guava.primitive
  (:import [com.google.common.primitives
            Bytes SignedBytes UnsignedBytes
            Ints UnsignedInteger UnsignedInts
            Longs UnsignedLong UnsignedLongs
            Floats Doubles Chars Booleans Shorts]))

; length in bytes
(def byte-bytes 1)

(def char-bytes Chars/BYTES)

(def int-bytes Ints/BYTES)

(def float-bytes Floats/BYTES)

(def double-bytes Doubles/BYTES)

(def long-bytes Longs/BYTES)

; finit double 
(defn finite-double? [arg]
  (Doubles/isFinite arg))

; use Doubles here! Because clojure's / operator convert float Infinity into double!
(defn finite-float? [arg]
  (Doubles/isFinite arg))

; transform between primitives and byte array
(defn bytes->char [bytes]
  (Chars/fromByteArray bytes))

(defn char->bytes [c]
  (Chars/toByteArray c))

(defn bytes->int [bytes]
  (Ints/fromByteArray bytes))

(defn int->bytes [n]
  (Ints/toByteArray n))

(defn short->bytes [s]
  (Shorts/toByteArray s))

(defn bytes->short [bytes]
  (Shorts/fromByteArray bytes))

(defn long->bytes [l]
  (Longs/toByteArray l))

(defn bytes->long [bytes]
  (Longs/fromByteArray bytes))

