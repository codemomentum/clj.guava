(ns clj.guava.test.primitive.uint
  (:require [clj.guava.primitive.uint :refer :all ])
  (:require [clojure.test :refer :all ])
  (:import [com.google.common.primitives
            Bytes SignedBytes UnsignedBytes
            Ints UnsignedInteger UnsignedInts
            Longs UnsignedLong UnsignedLongs
            Floats Doubles Chars Booleans Shorts]))


(deftest uint-constructors
  (testing "generate uint by diffrent constructors"
    (doseq [n [1238 (int 1238) "1238" (bigint 1238) (biginteger 1238)]]
      (is (instance? UnsignedInteger (uint n)))
      (is (= "1238" (str n))))
    ))


(deftest uint-operations
  (testing "uint +"
    (let [a (uint 9834)
          b (uint 823)
          c (uint 9275038)
          res1 (+)
          res2 (+ a)
          res3 (+ a b)
          res4 (+ a b c)]
      (is (instance? UnsignedInteger res1))
      (is (= 0 res1))
      (is (instance? UnsignedInteger res2))
      (is (= 9834 res2))
      (is (instance? UnsignedInteger res3))
      (is (= 10657 res3))
      (is (instance? UnsignedInteger res4))
      (is (= 9285695 res4))))

  )
