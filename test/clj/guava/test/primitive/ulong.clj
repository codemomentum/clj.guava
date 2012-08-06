(ns clj.guava.test.primitive.ulong
  (:require [clj.guava.primitive.ulong :refer :all ])
  (:require [clojure.test :refer :all ])
  (:import [com.google.common.primitives
            Bytes SignedBytes UnsignedBytes
            Ints UnsignedInteger UnsignedInts
            Longs UnsignedLong UnsignedLongs
            Floats Doubles Chars Booleans Shorts]))

(deftest uint-constructors
  (testing "generate ulong by diffrent constructors"
    (doseq [n [1238 (int 1238) "1238" (bigint 1238) (biginteger 1238)]]
      (is (instance? UnsignedLong (ulong n)))
      (is (= "1238" (str n))))
    ))



(deftest ulong-operations
  (testing "ulong +"
    (let [a (ulong 9834)
          b (ulong 823)
          c (ulong 9275038)
          res1 (+ a)
          res2 (+ a b)
          res3 (+ a b c)]
      (is (instance? UnsignedLong res1))
      (is (= 9834 res1))
      (is (instance? UnsignedLong res2))
      (is (= 10657 res2))
      (is (instance? UnsignedLong res3))
      (is (= 9285695 res3))))

  )
