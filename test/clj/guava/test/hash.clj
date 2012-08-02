(ns clj.guava.test.hash
  (:require [clj.guava.hash :as h])
  (:use [clojure.test])
  (:import [com.google.common.hash Hashing Hasher HashCode HashFunction]))

(defn test-hash-helper [algo]
  (let [^HashFunction hf (h/ALGORITHMS algo)
        ^Hasher hasher (.newHasher hf)]
    (.putString hasher "james")
    (.putLong hasher 25)
    (.putDouble hasher 2.5)
    (.putChar hasher \a)
    (.putString hasher ":object")
    (.putObject hasher {:name "rose" :children {:jack :tom}} h/CLOJURE-FUNNEL)
    (is (= (-> hasher .hash Hashing/padToLong) (h/hash algo "james" 25 2.5 \a :object {:name "rose" :children {:jack :tom}})))))

(deftest test-hash
  (test-hash-helper :md5)
  (test-hash-helper :murmur3-32)
  (test-hash-helper :murmur3-128)
  (test-hash-helper :sha1)
  (test-hash-helper :sha256)
  (test-hash-helper :sha512)
  (test-hash-helper :good-fast-hash))

(deftest test-murmur3-32-with-seed
  (let [^HashFunction hf (Hashing/murmur3_32 100)
        ^Hasher hasher (.newHasher hf)]
    (.putString hasher "james")
    (.putLong hasher 25)
    (.putDouble hasher 2.5)
    (.putChar hasher \a)
    (.putString hasher ":object")
    (binding [h/*seed* 100]
      (is (= (-> hasher .hash Hashing/padToLong) (h/hash :murmur3-32 "james" 25 2.5 \a :object))))))

(deftest test-murmur3-128-with-sed
  (let [^HashFunction hf (Hashing/murmur3_128 100)
        ^Hasher hasher (.newHasher hf)]
    (.putString hasher "james")
    (.putLong hasher 25)
    (.putDouble hasher 2.5)
    (.putChar hasher \a)
    (.putString hasher ":object")
    (binding [h/*seed* 100]
      (is (= (-> hasher .hash Hashing/padToLong) (h/hash :murmur3-128 "james" 25 2.5 \a :object))))))

(deftest test-good-fast-hash-with-min-bits
  (let [^HashFunction hf (Hashing/goodFastHash 35)
        ^Hasher hasher (.newHasher hf)]
    (.putString hasher "james")
    (.putLong hasher 25)
    (.putDouble hasher 2.5)
    (.putChar hasher \a)
    (.putString hasher ":object")
    (binding [h/*min-bits* 35]
      (is (= (-> hasher .hash Hashing/padToLong) (h/hash :good-fast-hash "james" 25 2.5 \a :object))))))

(deftest test-hash-invalid-algorithm
  (is (thrown? IllegalArgumentException (h/hash :no-such-algorithm "james" 25))))

(deftest test-hash-empty-input
  (is (thrown? IllegalArgumentException (h/hash :md5))))

(defrecord Person [name age])

(deftest test-bloom-filter
  (let [bf (h/bloom-filter 100 0.01)
        rose (Person. "rose" 30)]
    (h/bloom-filter-put! bf {:name "james" :age 25})
    (h/bloom-filter-put! bf {:name "jack" :age 30})
    (h/bloom-filter-put! bf rose)
    (is (h/bloom-filter-contains? bf {:name "james" :age 25}))
    (is (h/bloom-filter-contains? bf {:name "jack" :age 30}))
    (is (h/bloom-filter-contains? bf rose))
    (is (= false (h/bloom-filter-contains? bf {:name "jamie" :age 25})))))

(deftest test-consistent-hash
  (is (= (Hashing/consistentHash 100 10) (h/consistent-hash 100 10))))