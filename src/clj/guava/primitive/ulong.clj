(ns clj.guava.primitive.ulong
  (:import [com.google.common.primitives
            Longs UnsignedLong UnsignedLongs]))

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

(defn +
  ""
  ([] (ulong 0))
  ([a] a)
  ([^UnsignedLong a ^UnsignedLong b] (.add a b))
  ([a b & more] (reduce + (+ a b) more)))
