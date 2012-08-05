(ns ^{:doc "Wrapper of guava primitives"
      :author "qiuxiafei <qiuxiafei@gmail.com>"}
  clj.guava.primitive
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

; ====================================
; UnSignedInteger constructors
; ====================================

(defprotocol uint-constructor
  "Use function 'uint' to construct an unsigned integer"
  (uint [a] [a b]))

(extend-protocol uint-constructor Integer
  (uint [arg]
    (UnsignedInteger/asUnsigned arg)))

(extend-protocol uint-constructor Long
  (uint [arg]
    (UnsignedInteger/valueOf (int arg))))

(extend-protocol uint-constructor String
  (uint
    ([arg] (UnsignedInteger/valueOf arg))
    ([arg base] (UnsignedInteger/valueOf arg base))))

(extend-protocol uint-constructor BigInteger
  (uint [arg]
    (UnsignedInteger/valueOf arg)))

(extend-protocol uint-constructor clojure.lang.BigInt
  (uint [arg]
    (UnsignedInteger/valueOf (biginteger arg))))

; ====================================
; UnSignedLong constructors
; ====================================

(defprotocol ulong-constructor
  "Use function 'ulong' to construct an unsigned long"
  (ulong [a] [a b]))

(extend-protocol ulong-constructor Integer
  (ulong [arg]
    (UnsignedLong/asUnsigned (long arg))))

(extend-protocol ulong-constructor Long
  (ulong [arg]
    (UnsignedLong/asUnsigned arg)))

(extend-protocol ulong-constructor String
  (ulong
    ([arg] (UnsignedLong/valueOf arg))
    ([arg base] (UnsignedLong/valueOf arg base))))

(extend-protocol ulong-constructor BigInteger
  (ulong [arg]
    (UnsignedLong/valueOf arg)))

(extend-protocol ulong-constructor clojure.lang.BigInt
  (ulong [arg]
    (UnsignedLong/valueOf (biginteger arg))))

; ====================================
; Operations on unsigned int/long
; ====================================
;
;(defprotocol unsigned
;  (+ [a] [a b] [a b & more])
;  (- [a] [a b] [a b & more]) ;; TOOD why there's reflaction warning here??
;  (* [a] [a b] [a b & more])
;  (/ [a] [a b] [a b & more])
;  (< [a] [a b] [a b & more])
;  (> [a] [a b] [a b & more])
;  (<= [a] [a b] [a b & more])
;  (>= [a] [a b] [a b & more])
;  (= [a] [a b] [a b & more])
;  (== [a] [a b] [a b & more])
;  (mod [a b])
;  )
;
;(extend-protocol unsigned UnsignedInteger
;  (+
;    ([a] a)
;    ([^UnsignedInteger a b] (.add a b))
;    ([a b & more] (reduce + (+ a b) more)))
;  (-
;    ([a] (- (uint 0) a))
;    ([^UnsignedInteger a b] (.subtract a b))
;    ([a b & more] (reduce - (- a b) more))
;    )
;
;  )
