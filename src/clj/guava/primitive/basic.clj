(ns clj.guava.primitive.basic
  (:import [com.google.common.primitives
            Bytes SignedBytes UnsignedBytes
            Ints UnsignedInteger UnsignedInts
            Longs UnsignedLong UnsignedLongs
            Floats Doubles Chars Booleans Shorts]))

; ====================================
; length of primitive types in bytes
; ====================================

(def ^{:doc "Length of a Byte in byte"
       :tag Integer :added "0.1" :const true}
  byte-bytes (int 1))

(def ^{:doc "Length of a Char in byte"
       :tag Integer :added "0.1" :const true}
  char-bytes Chars/BYTES)

(def ^{:doc "Length of a Integer in byte"
       :tag Integer :added "0.1" :const true}
  int-bytes Ints/BYTES)

(def ^{:doc "Length of a Float in byte"
       :tag Integer :added "0.1" :const true}
  float-bytes Floats/BYTES)

(def ^{:doc "Length of a Double in byte"
       :tag Integer :added "0.1" :const true}
  double-bytes Doubles/BYTES)

(def ^{:doc "Length of a Long in byte"
       :tag Integer :added "0.1" :const true}
  long-bytes Longs/BYTES)

; ====================================
; judge if a double/float is finite
; ====================================

(defn finite?
  "Return true if a double/float is neither Infinity nor NaN"
  ^{:tag Boolean :added "0.1"}
  [arg]
  (Doubles/isFinite arg))


; ====================================
; transform between primitives and byte array
; ====================================

(defn bytes->char
  "Parse a char from byte array representation"
  ^{:tag Character :added "0.1"}
  [bytes]
  (Chars/fromByteArray bytes))

(defn char->bytes
  "Convert a char to bytes array"
  ^{:tag bytes :added "0.1"}
  [c]
  (Chars/toByteArray c))

(defn bytes->int
  "Parse a int from byte array representation"
  ^{:tag Integer :added "0.1"}
  [bytes]
  (Ints/fromByteArray bytes))

(defn int->bytes
  "Convert a int to bytes array"
  ^{:tag bytes :added "0.1"}
  [n]
  (Ints/toByteArray n))

(defn short->bytes
  "Convert a short to bytes array"
  ^{:tag Short :added "0.1"}
  [s]
  (Shorts/toByteArray s))

(defn bytes->short
  "Parse a short from byte array representation"
  ^{:tag bytes :added "0.1"}
  [bytes]
  (Shorts/fromByteArray bytes))

(defn long->bytes
  "Convert long int to bytes array"
  ^{:tag bytes :added "0.1"}
  [l]
  (Longs/toByteArray l))

(defn bytes->long
  "Parse a long from byte array representation"
  ^{:tag Long :added "0.1"}
  [bytes]
  (Longs/fromByteArray bytes))
