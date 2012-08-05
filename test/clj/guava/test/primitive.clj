(ns clj.guava.test.primitive
  (:require [clj.guava.primitive :refer :all ])
  (:require [clojure.test :refer :all ])
  (:import [com.google.common.primitives
            Bytes SignedBytes UnsignedBytes
            Ints UnsignedInteger UnsignedInts
            Longs UnsignedLong UnsignedLongs
            Floats Doubles Chars Booleans Shorts]))

(deftest test-primitive-bytes
  (testing "length in bytes of primitve types"
    (is (= byte-bytes 1) "one byte has one byte")
    (is (= char-bytes 2) "one char has two bytes")
    (is (= int-bytes 4) "one int has four bytes")
    (is (= long-bytes 8) "one long has eight bytes")
    (is (= float-bytes 4) "one float has four bytes")
    (is (= double-bytes 8) "one double has eight bytes")
    ))

(deftest test-finite-double
  (testing "test is-finite of double"
    (is (false? (finite? Double/NaN)) "Nan is NOT finite")
    (is (false? (finite? (/ 1.0 0.0))) "Infinit is NOT finite")
    (is (true? (finite? 1.0)) "1.0 is finite")
    (is (true? (finite? Double/MAX_VALUE)) "1.0 is finite")
    (is (true? (finite? Double/MIN_VALUE)) "1.0 is finite")
    ))

(deftest test-finite-float
  (testing "test is-finite of float"
    (is (false? (finite? Float/NaN)) "Nan is NOT finite")
    (is (false? (finite? (/ (float 1.0) (float 0.0)))) "Infinit is NOT finite")
    (is (true? (finite? (float 1.0))) "1.0 is finite")
    (is (true? (finite? Float/MAX_VALUE)) "1.0 is finite")
    (is (true? (finite? Float/MIN_VALUE)) "1.0 is finite")
    ))

(deftest char-bytes-transform
  (testing "transforming between char/int/long and byte array"
    (doseq [k (range 0 256)]
      (is (= (char k) (bytes->char (char->bytes (char k))))))
    (doseq [k (range -2560 2560)]
      (is (= (int k) (bytes->int (int->bytes (int k))))))
    (doseq [k (range -2560 2560)]
      (is (= k (bytes->long (long->bytes k)))))
    (doseq [k (range -2560 2560)]
      (is (= (short k) (bytes->short (short->bytes (short k))))))
    ;    (doseq [k (range 0 2)]
    ;      (is (true? true)))

    ))

(deftest uint-constructors
  (testing "generate uint by diffrent constructors"
    (doseq [n [1238 (int 1238) "1238" (bigint 1238) (biginteger 1238)]]
      (is (instance? UnsignedInteger (uint n)))
      (is (= "1238" (str n))))
    ))

(deftest uint-constructors
  (testing "generate ulong by diffrent constructors"
    (doseq [n [1238 (int 1238) "1238" (bigint 1238) (biginteger 1238)]]
      (is (instance? UnsignedLong (ulong n)))
      (is (= "1238" (str n))))
    ))
