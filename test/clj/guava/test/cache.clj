(ns clj.guava.test.cache
  (:require [clj.guava.cache :as c])
  (:use [clojure.test])
  (:import [com.google.common.cache CacheLoader$InvalidCacheLoadException CacheStats])
  (:import [com.google.common.base Ticker])
  (:import [java.util.concurrent.atomic AtomicLong]))

(deftest test-as-map
  (let [acache (c/cache :loader (fn [key] (inc key)))]
    (c/get acache 1)
    (c/get acache 2)
    (is (= {1 2 2 3} (c/as-map acache)))))

(deftest test-get
  (let [acache (c/cache :loader (fn [key] (inc key)))]
    (is (= 2 (c/get acache 1)))
    (is (= 100 (c/get acache 2 (fn [] 100))))))

(deftest test-get-all
  (let [acache (c/cache :loader (fn [key] (inc key)))]
    (is (= {1 2 2 3} (c/get-all acache [1 2])))))

(deftest test-refresh
  (let [acache (c/cache :loader (fn [key] (rand)))
        init-value (c/get acache 1)
        value1 (c/get acache 1)]
    (is (= init-value value1))
    (c/refresh acache 1)
    (let [new-value (c/get acache 1)]
      (is (not= init-value new-value)))))

(deftest test-put!
  (let [data {1 2 3 4}
        acache (c/cache :loader (fn [key] (data key)))]
    (is (thrown? CacheLoader$InvalidCacheLoadException (c/get acache 100)))
    (c/put! acache 100 200)
    (is (= 200 (c/get acache 100)))))

(deftest test-invalidate!
  (let [acache (c/cache :loader (fn [key] (inc key)))]
    (c/get acache 1)
    (c/get acache 2)
    (c/get acache 3)
    (c/get acache 4)    
    (is (= {1 2 2 3 3 4 4 5} (c/as-map acache)))
    (c/invalidate! acache 1)
    (is (= {2 3 3 4 4 5} (c/as-map acache)))
    (c/invalidate! acache 2 3)
    (is (= {4 5} (c/as-map acache)))
    (c/invalidate! acache)
    (is (empty? (c/as-map acache)))))

(deftest test-stats
  (let [acache (c/cache :loader (fn [key] (inc key)))]
    (c/get acache 1)
    (c/get acache 2)
    (c/get acache 3)
    (let [^CacheStats orig-cs (.stats acache)
          ^CacheStats new-cs (c/stats acache)]
      (is (= (.hitCount orig-cs) (:hit-cnt new-cs)))
      (is (= (.requestCount orig-cs) (:request-cnt new-cs)))
      (is (= (.hitRate orig-cs) (:hit-rate new-cs)))
      (is (= (.missCount orig-cs) (:miss-cnt new-cs)))
      (is (= (.missRate orig-cs) (:miss-rate new-cs)))
      (is (= (.loadCount orig-cs) (:load-cnt new-cs)))
      (is (= (.loadSuccessCount orig-cs) (:load-success-cnt new-cs)))
      (is (= (.loadExceptionCount orig-cs) (:load-exception-cnt new-cs)))
      (is (= (.loadExceptionRate orig-cs) (:load-exception-rate new-cs)))
      (is (= (.totalLoadTime orig-cs) (:total-load-time new-cs)))
      (is (= (.averageLoadPenalty orig-cs) (:average-load-penalty new-cs)))
      (is (= (.evictionCount orig-cs) (:eviction-cnt new-cs))))))

(deftest test-cache-loader
  (let [acache (c/cache)]
    (is (thrown? ClassCastException (c/get acache 1)))
    (is (= 1 (c/get acache 1 (fn [] 1))))))

(deftest test-cache-max-size
  (let [acache (c/cache :loader (fn [key] (inc key)) :max-size 10)]
    (doseq [i (range 10)]
      (c/get acache i))
    (is (= 10 (count (c/as-map acache))))
    (c/get acache 11)
    (is (= 10 (count (c/as-map acache))))))

(deftest test-cache-max-weight
  (let [acache (c/cache :loader (fn [key] key) :max-weight 10 :weigher (fn [key value] value))]
    (c/get acache 1)
    (c/get acache 2)
    (c/get acache 3)
    (c/get acache 4)
    (is (= 4 (count (c/as-map acache))))
    (c/get acache 5)
    (is (> 4 (count (c/as-map acache))))))

(def nanos (AtomicLong.))
(def fake-ticker (proxy [Ticker] []
                   (^long read []
                     (.get ^AtomicLong nanos))))
(defn- advance-time! [x]
  (.addAndGet ^AtomicLong nanos x))

(deftest test-cache-expire-after-access
  (let [acache (c/cache :loader (fn [key] (inc key)) :ticker fake-ticker :expire-after-access [10 :nanos])]
    (c/get acache 1)
    (is (= {1 2} (c/as-map acache)))
    (advance-time! 9)
    (c/get acache 1)    
    (is (= {1 2} (c/as-map acache)))
    (advance-time! 2)
    (is (= {1 2} (c/as-map acache)))
    (advance-time! 11)    
    (is (empty? (c/as-map acache)))))

(deftest test-cache-expire-after-write
  (let [acache (c/cache :loader (fn [key] (inc key)) :ticker fake-ticker :expire-after-write [10 :nanos])]
    (c/get acache 1)
    (is (= {1 2} (c/as-map acache)))
    (advance-time! 9)
    (c/get acache 1)
    (advance-time! 2)
    (is (empty? (c/as-map acache)))
    (c/get acache 1)
    (advance-time! 9)
    (c/put! acache 1 2)
    (is (= {1 2} (c/as-map acache)))))

(deftest test-removal-listener
  (let [box (atom [])
        rl (fn [key value cause] (swap! box conj [key value cause]))
        acache (c/cache :loader (fn [key] key) :removal-listener rl)]
    (c/get acache 1)
    (c/invalidate! acache 1)
    (is (= [[1 1 :explicit]] @box))))

