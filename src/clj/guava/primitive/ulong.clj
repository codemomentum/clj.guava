(ns clj.guava.primitive.ulong
  (:import [com.google.common.primitives
            Longs UnsignedLong UnsignedLongs]))

; ====================================
; UnSignedLong constructors
; ====================================

(defprotocol ulong-constructor
  "Use function 'ulong' to construct an unsigned long"
  ^{:added "0.1"}
  (ulong [a] [a b]))

(extend-protocol ulong-constructor Integer
  (ulong [arg]
    ^{:tag UnsignedLong :added "0.1"}
    (UnsignedLong/asUnsigned (long arg))))

(extend-protocol ulong-constructor Long
  (ulong [arg]
    ^{:tag UnsignedLong :added "0.1"}
    (UnsignedLong/asUnsigned arg)))

(extend-protocol ulong-constructor String
  (ulong
    ([arg]
      ^{:tag UnsignedLong :added "0.1"}
      (UnsignedLong/valueOf arg))
    ([arg base]
      ^{:tag UnsignedLong :added "0.1"}
      (UnsignedLong/valueOf arg base))))

(extend-protocol ulong-constructor BigInteger
  (ulong [arg]
    ^{:tag UnsignedLong :added "0.1"}
    (UnsignedLong/valueOf arg)))

(extend-protocol ulong-constructor clojure.lang.BigInt
  (ulong [arg]
    ^{:tag UnsignedLong :added "0.1"}
    (UnsignedLong/valueOf (biginteger arg))))


(defn +
  "Override the + operaton"
  ^{:tag UnsignedLong :added "0.1"}
  ([a] a)
  ([^UnsignedLong a b] (.add a b))
  ([a b & more] (reduce + (+ a b) more)))

(defn -
  "Override the - operaton"
  ^{:tag UnsignedLong :added "0.1"}
  ([a] (- (ulong 0) a))
  ([^UnsignedLong a b] (.subtract a b))
  ([a b & more] (reduce - (- a b) more)))

(defn *
  "Override the * operation"
  ^{:tag UnsignedLong :added "0.1"}
  ([a] a)
  ([^UnsignedLong a b] (.multiply a b))
  ([a b & more] (reduce * (* a b) more)))

(defn /
  "Override the / operation"
  ^{:tag UnsignedLong :added "0.1"}
  ([a] (/ (ulong 1) a))
  ([^UnsignedLong a b] (.divide a b))
  ([a b & more] (reduce / (/ a b) more)))

(defn mod
  "Override the mod operation"
  ^{:tag UnsignedLong :added "0.1"}
  [^UnsignedLong a b] (.remainder a b))

(defn rem
  "Override the rem operation"
  ^{:tag UnsignedLong :added "0.1"}
  [a b]
  (mod a b))


; There's no need to override <, >, <=, >=, =, ==, 
; because clojure support comparing between java.lang.Number
; and both UnsignedInteger and UnsignedLong extends java.lang.Number
