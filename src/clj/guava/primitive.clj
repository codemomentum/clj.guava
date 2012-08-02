(ns ^{:doc "Wrapper of guava primitives"
      :author "qiuxiafei <qiuxiafei@gmail.com>"}
  clj.guava.primitive
  (:import [com.google.common.primitives
            Bytes SignedBytes UnsignedBytes
            Ints UnsignedInteger UnsignedInts
            Longs UnsignedLong UnsignedLongs
            Floats Doubles Chars Booleans Shorts]))

;(set! *warn-on-reflection* true)

; ====================================
; length of primitive types in bytes
; ====================================

(def ^:const ^{:tag Integer :added "0.1"}
  byte-bytes (int 1))

(def ^:const ^{:tag Integer :added "0.1"}
  char-bytes Chars/BYTES)

(def ^:const ^{:tag Integer :added "0.1"}
  int-bytes Ints/BYTES)

(def ^:const ^{:tag Integer :added "0.1"}
  float-bytes Floats/BYTES)

(def ^:const ^{:tag Integer :added "0.1"}
  double-bytes Doubles/BYTES)

(def ^:const ^{:tag Integer :added "0.1"}
  long-bytes Longs/BYTES)

; ====================================
; judge if a double/float is finite
; ====================================

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


; ====================================
; transform between primitives and byte array
; ====================================

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

; ====================================
; UnSingedInteger constructors
; ====================================

(defprotocol uint-constructor
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
; UnSingedLong constructors
; ====================================

(defprotocol ulong-constructor
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
; UnSingedInteger constructors
; ====================================

;(defprotocol unsined
;  (+ [a b & more])
;  (- [a b & more])
;  (* [a b & more])
;  (/ [a b & more])
;  (< [a b & more])
;  (> [a b & more])
;  (<= [a b & more])
;  (>= [a b & more]) 
;  (= [a b & more])
;  (== [a b & more])
;  (mod [a b])
;  )
;
;(defn the-fn [a]
;  (str a))
