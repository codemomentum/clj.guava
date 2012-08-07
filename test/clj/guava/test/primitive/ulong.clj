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
  (testing "ulong -"
    (let [a (ulong 9834)
          b (ulong 823)
          c (ulong 9275038)
          res1 (- a)
          res2 (- a b)
          res3 (- c b a)]
      (is (instance? UnsignedLong res1))
      (is (= (ulong 18446744073709541782) res1))
      (is (instance? UnsignedLong res2))
      (is (= 9011 res2))
      (is (instance? UnsignedLong res3))
      (is (= 9264381 res3))))

  (testing "ulong *"
    (let [a (ulong 9834)
          b (ulong 823)
          c (ulong 23)
          res1 (* a)
          res2 (* a b)
          res3 (* a b c)]
      (is (instance? UnsignedLong res1))
      (is (= 9834 res1))
      (is (instance? UnsignedLong res2))
      (is (= 8093382 res2))
      (is (instance? UnsignedLong res3))
      (is (= 186147786 res3))))

  (testing "ulong /"
    (let [a (ulong 94)
          b (ulong 823)
          c (ulong 9275038)
          res1 (/ a)
          res2 (/ b a)
          res3 (/ c a b)]
      (is (instance? UnsignedLong res1))
      (is (= 0 res1))
      (is (instance? UnsignedLong res2))
      (is (= 8 res2))
      (is (instance? UnsignedLong res3))
      (is (= 119 res3))))

  (testing "ulong mod/rem"
    (let [a (ulong 10)
          b (ulong 3)
          r (rem a b)
          m (mod a b)]
      (is (instance? UnsignedLong r))
      (is (instance? UnsignedLong m))
      (is (= 1 m))
      (is (= 1 r))
      ))

  (testing "ulong compaire"
    (let [a (ulong 10)
          b (ulong 3)
          c (ulong 1)]
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
