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
          res1 (+ a)
          res2 (+ a b)
          res3 (+ a b c)]
      (is (instance? UnsignedInteger res1))
      (is (= 9834 res1))
      (is (instance? UnsignedInteger res2))
      (is (= 10657 res2))
      (is (instance? UnsignedInteger res3))
      (is (= 9285695 res3))))

  (testing "uint -"
    (let [a (uint 9834)
          b (uint 823)
          c (uint 9275038)
          res1 (- a)
          res2 (- a b)
          res3 (- c b a)]
      (is (instance? UnsignedInteger res1))
      (is (= 4294957462 res1))
      (is (instance? UnsignedInteger res2))
      (is (= 9011 res2))
      (is (instance? UnsignedInteger res3))
      (is (= 9264381 res3))))

  (testing "uint *"
    (let [a (uint 9834)
          b (uint 823)
          c (uint 23)
          res1 (* a)
          res2 (* a b)
          res3 (* a b c)]
      (is (instance? UnsignedInteger res1))
      (is (= 9834 res1))
      (is (instance? UnsignedInteger res2))
      (is (= 8093382 res2))
      (is (instance? UnsignedInteger res3))
      (is (= 186147786 res3))))

  (testing "uint /"
    (let [a (uint 94)
          b (uint 823)
          c (uint 9275038)
          res1 (/ a)
          res2 (/ b a)
          res3 (/ c a b)]
      (is (instance? UnsignedInteger res1))
      (is (= 0 res1))
      (is (instance? UnsignedInteger res2))
      (is (= 8 res2))
      (is (instance? UnsignedInteger res3))
      (is (= 119 res3))))

  (testing "uint mod/rem"
    (let [a (uint 10)
          b (uint 3)
          r (rem a b)
          m (mod a b)]
      (is (instance? UnsignedInteger r))
      (is (instance? UnsignedInteger m))
      (is (= 1 m))
      (is (= 1 r))
      ))

  (testing "uint compaire"
    (let [a (uint 10)
          b (uint 3)
          c (uint 1)]
      (is (< b a))
      (is (<= b a))
      (is (> a b))
      (is (>= a b))
      (is (= a a))
      (is (>= a a))
      (is (<= a a))
      (is (>= a a a b b c c))
      (is (<= c c b b a a))
      ))
  )

(deftest reader-literal
  (testing "uint reader literal"
    (is (instance? UnsignedInteger #ui 12))
    (is (instance? UnsignedInteger (+ #ui 12 #ui 12)))
    (is (= 24 (+ #ui 12 #ui 12))))
  )
