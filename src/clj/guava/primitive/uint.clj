(ns clj.guava.primitive.uint
  (:import [com.google.common.primitives
            Ints UnsignedInteger UnsignedInts]))

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
