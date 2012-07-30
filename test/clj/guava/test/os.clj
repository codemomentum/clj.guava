(ns clj.guava.test.os
  (:use [clj.guava.os])
  (:use [clojure.test]))

(deftest test-now
  (is (= (System/currentTimeMillis) (now))))

(deftest test-sys-property
  (is (= (System/getProperty "os.name") (sys-property "os.name"))))

(deftest test-os-info
  (is (map? (os-info)))
  (is (= 3 (count (os-info))))
  (is (not-any? #(nil? %) (vals (os-info))))
  (is (= (sys-property "os.name") (:name (os-info)))))

(deftest test-java-version
  (is (= (sys-property "java.version" ) (java-version))))

(deftest test-cpus
  (is (> (cpus) 0)))

(deftest test-jvm-meme
  (is (map? (jvm-mem)))
  (is (= 3 (count (jvm-mem))))
  (is (every? integer? (vals (jvm-mem))))
  (is (not-any? nil? (vals (jvm-mem)))))

(deftest test-gc
  (gc))

(deftest test-getenv
  (is (= (System/getenv "JAVA_HOME") (getenv "JAVA_HOME"))))

(deftest test-thread-dump
  (is (seq? (thread-dump)))
  (is (not-any? nil? (thread-dump)))
  (is (every? string? (thread-dump))))


