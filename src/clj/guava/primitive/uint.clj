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

(defn +
  "Override the + operaton"
  ^{:tag UnsignedInteger :added "0.1"}
  ([a] a)
  ([^UnsignedInteger a b] (.add a b))
  ([a b & more] (reduce + (+ a b) more)))

(defn -
  "Override the - operaton"
  ^{:tag UnsignedInteger :added "0.1"}
  ([a] (- (uint 0) a))
  ([^UnsignedInteger a b] (.subtract a b))
  ([a b & more] (reduce - (- a b) more)))

(defn *
  "Override the * operation"
  ^{:tag UnsignedInteger :added "0.1"}
  ([a] a)
  ([^UnsignedInteger a b] (.multiply a b))
  ([a b & more] (reduce * (* a b) more)))

(defn /
  "Override the / operation"
  ^{:tag UnsignedInteger :added "0.1"}
  ([a] (/ (uint 1) a))
  ([^UnsignedInteger a b] (.divide a b))
  ([a b & more] (reduce / (/ a b) more)))

(defn mod
  "Override the mod operation"
  ^{:tag UnsignedInteger :added "0.1"}
  [^UnsignedInteger a b] (.remainder a b))

(defn rem
  "Override the rem operation"
  ^{:tag UnsignedInteger :added "0.1"}
  [a b]
  (mod a b))

; There's no need to override <, >, <=, >=, =, ==, 
; because clojure support comparing between java.lang.Number
; and both UnsignedInteger and UnsignedLong extends java.lang.Number
