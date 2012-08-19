(ns clj.guava.test.collect
  (:refer-clojure :exclude [sort max min reverse empty?])
  (:use [clj.guava.collect])
  (:use [clojure.test])
  (:use [clj.guava.base :only [not-nil? bfalse? btrue?]]))

(deftest test-create-ordering
  (is (not-nil? (order-by)))
  (is (not-nil? (order-by :nat)))
  (is (not-nil? (order-by :str)))
  (is (not-nil? (order-by :arb)))
  (is (not-nil? (order-by (comparator #(> %1 %2)))))
  (is (not-nil? (order-by #(- %1 %2))))
  (is (not-nil? (ordering #(> %1 %2))))
  (is (thrown? java.lang.IllegalArgumentException (explicit 1 2 3 1))) 
  (is (not-nil? (explicit 1 2 3 4))))

(deftest test-ordering-manipulation
  (let [ord (order-by)]
    (is (not-nil? ord))
    (is (not-nil? (reverse ord)))
    (is (not-nil? (compound ord #(- %1 %2))))
    (is (not-nil? (nilslast ord)))
    (is (not-nil? (nilsfirst ord)))))

(deftest test-greatest-least
  (testing "testing greatest"
    (let [ord (order-by)]
      (is (= '(5) (greatest ord '(1 2 3 4 5) 1)))
      (is (= '(5 4) (greatest ord '(1 2 3 4 5) 2)))
      (is (= '(5 4 3) (greatest ord '(1 2 3 4 5) 3)))
      (let [ord (reverse ord)]
        (is (= '(1) (greatest ord '(1 2 3 4 5) 1)))
        (is (= '(1 2) (greatest ord '(1 2 3 4 5) 2)))
        (is (= '(1 2 3) (greatest ord '(1 2 3 4 5) 3))))))
  (testing "testing least"
    (let [ord (order-by)]
      (is (= '(1) (least ord '(1 2 3 4 5) 1)))
      (is (= '(1 2) (least ord '(1 2 3 4 5) 2)))
      (is (= '(1 2 3) (least ord '(1 2 3 4 5) 3)))
      (let [ord (reverse ord)]
        (is (= '(5) (least ord '(1 2 3 4 5) 1)))
        (is (= '(5 4) (least ord '(1 2 3 4 5) 2)))
        (is (= '(5 4 3) (least ord '(1 2 3 4 5) 3)))))))

(deftest test-cmp
  (let [ord (order-by)]
    (is (< (cmp ord 1 2) 0))
    (is (thrown? NullPointerException (cmp ord nil 1)))
    (is (= (cmp ord 1 1) 0))
    (is (> (cmp ord 2 1) 0))))

(deftest test-max
  (testing "testing max"
    (let [ord (order-by)]
      (is (= 6 (max ord 5 6)))
      (is (= 6 (max ord 6)))
      (is (nil? (max ord)))
      (is (= 6 (max ord 1 2 3 4 5 6)))
      ))
  (testing "testing max*"
    (let [ord (order-by :str)]
      (is (= 2 (max* ord '(1 2 11 12 13))))
      (is (thrown? NullPointerException (max* ord '()))))))


(deftest test-min
  (testing "testing min"
    (let [ord (order-by)]
      (is (= 5 (min ord 5 6)))
      (is (= 6 (min ord 6)))
      (is (nil? (min ord)))
      (is (= 1 (min ord 1 2 3 4 5 6)))
      ))
  (testing "testing min*"
    (let [ord (order-by :str)]
      (is (= 1 (min* ord '(1 2 11 12 13))))
      (is (= 0 (min* ord '(9 8 2 3 5 0 7))))
      (is (thrown? NullPointerException (min* ord '()))))))

(deftest test-sort-ordered?
  (let [ord1 (order-by)
        ord2 (-> (order-by) (reverse))
        ls (shuffle (take 10 (iterate inc 1)))]
    (is (false? (ordered? ord1 ls)))
    (is (= '(1 2 3 4 5 6 7 8 9 10) (sort ord1 ls)))
    (is (not= ls (sort ord1 ls)))
    (is (ordered? ord1 (sort ord1 ls)))
    (is (strictly-ordered? ord1 (sort ord1 ls)))
    (is (false? (ordered? ord2 (sort ord1 ls))))
    (is (false? (ordered? ord2 ls)))
    (is (false? (ordered? ord1 ls)))
    (is (= '(10 9 8 7 6 5 4 3 2 1)  (sort ord2 ls)))
    (is (false? (strictly-ordered? ord1 '(1 2 2 3 4 5 6 7 8 9))))
    (is (ordered? ord1 '(1 2 2 3 4 5 6 7 8 9)))
    (is (strictly-ordered? ord1 '(1 2 3 4 5 6 7 8 9)))
    (let [ls (sort ord1 ls)]
      (is (= 9 (search ord1 ls 10)))
      (is (= 2 (search ord1 ls 3)))
      (is (= -11 (search ord1 ls 11)))
      (is (= -11 (search ord1 ls 99)))
      )))

(deftest test-include?
  (is (include? [4 5 6 7] 5))
  (is (include? '(4 5 6 7) 5))
  (is (bfalse? (include? [4 5 6] 0)))
  (is (include? {:a 1 :b 2} :a))
  (is (include? #{5 6 3 4} 3))
  (let [m (doto (java.util.HashMap.) (.put "a" 1) (.put "b" 2))
        ls (doto (java.util.ArrayList.) (.add 1) (.add 2) (.add "hello"))]
    (is (include? m "a"))
    (is (include? m "b"))
    (is (bfalse? (include? m "c")))
    (is (bfalse? (include? m "d")))
    (is (include? ls 1))
    (is (include? ls 2))
    (is (include? ls "hello"))
    (is (bfalse? (include? ls "world")))
    ))

(deftest test-with-ordering
  (testing "with-ordering"
    (is (thrown-with-msg? NullPointerException #"is not bound" (cmp 1 2))) 
    (with-ordering (order-by)
      (is (ordered? '(1 2 3 4)))
      (is (< (cmp 1 2) 0))
      (is (= '(1 2 3 4) (sort '(3 4 1 2)))))))

(deftest test-ranges
  (testing "create ranges"
    (is (= '(1 2 3 4 5) (as-seq (ranges 1 .. 5))))
    (is (= '(1 2 3 4 5) (as-seq (ranges 1 5))))
    (is (= '(1 2 3 4) (as-seq (ranges 1 ... 5))))
    (is (thrown? NullPointerException (eval '(clj.guava.collect/ranges "a"))))
    (is (thrown? NullPointerException (eval '(clj.guava.collect/ranges "a" ..))))
    (is (thrown? NullPointerException (eval '(clj.guava.collect/ranges "a" ...))))
    (is (thrown? NullPointerException (eval '(clj.guava.collect/ranges  .. "a"))))
    (is (thrown? NullPointerException (eval '(clj.guava.collect/ranges  ... "a"))))
    (is (thrown? NullPointerException (eval '(clj.guava.collect/ranges   "a" .. "z"))))
    (is (thrown? NullPointerException (eval '(clj.guava.collect/ranges   "a" ... "z"))))
    (is (= '(\a \b \c \d) (as-seq (ranges \a \d))))
    (is (= '(0) (as-seq (ranges 0))))))

(deftest test-lower-upper-empty?
  (let [a (ranges 0 .. 5)
        b (ranges 0 ... 5)
        c (ranges 0)
        d (ranges 0 ..)
        e (ranges ... 5)]
    (is (upper-bound? a))
    (is (upper-bound? b))
    (is (upper-bound? c))
    (is (not (upper-bound? d)))
    (is (upper-bound? e))
    (is (lower-bound? a))
    (is (lower-bound? b))
    (is (lower-bound? c))
    (is (lower-bound? d))
    (is (not (lower-bound? e)))
    (is (bounded? a))
    (is (bounded? b))
    (is (bounded? c))
    (is (not (bounded? d)))
    (is (not (bounded? e)))
    (is (not (empty? a)))
    (is (not (empty? b)))
    (is (not (empty? c)))
    (is (not (empty? d)))
    (is (empty? (ranges 0 ... 0)))
    (is (not (empty? e)))

    (testing "lower and upper"
      (is (= 0 (lower a)))
      (is (= 0 (lower b)))
      (is (= 0 (lower c)))
      (is (= 0 (lower d)))
      (is (thrown? IllegalArgumentException (lower e)))

      (is (= 5 (upper a)))
      (is (= 4 (upper b)))
      (is (= 0 (upper c)))
      (is (= 0 (lower (ranges 0 ... 0))))
      (is (thrown? IllegalArgumentException (upper d)))
      (is (= 4 (upper e))))))

(deftest test-include-include-all?
  (let [r (ranges 0 .. 100)]
    (is (include? r 0))
    (is (include? r 100))
    (is (include? r 50))
    (is (not (include? r -1)))
    (is (not (include? r 101)))

    (is (include-all? r '(1 2 3)))
    (is (include-all? r '(90 91 92)))
    (is (include-all? r [0 1 2 3 4 5 6 7 8 9 0]))
    (is (not (include-all? r [-1 0 1 2 3])))
    (is (include? (ranges -1 ... 1) 0))
    (is (include? (ranges -1 ... 1) -1))
    (is (include-all? (ranges -1 ... 1) [0 -1]))
    (is (not (include? (ranges -1 ... 1) 1)))))

(deftest test-encloses?
  (let [a (ranges 3 .. 6)
        b (ranges 4 .. 5)
        c (ranges 4 ... 4)
        d (ranges 3 ... 6)
        e (ranges 4 .. 6)]

    (is (encloses? a b))
    (is (encloses? a a ))
    (is (encloses? a c))
    (is (not (encloses? e a)))))

(deftest test-connected?
  (is (connected? (ranges 0 5) (ranges 5 10)))
  (is (connected? (ranges 0 5) (ranges 3 9)))
  (is (not (connected? (ranges 1 5) (ranges 6 10))))
  (is (connected? (ranges 1 ... 5) (ranges 5 10)))
  (is (connected? (ranges 0 9) (ranges 3 4))))

(deftest test-intersection
  (is (thrown? IllegalArgumentException (intersection (ranges 3 4) (ranges 5 10))))
  (is (= '(3 4) (as-seq (intersection (ranges 0 9) (ranges 3 4)))))
  (is (= '(3 4 5) (as-seq (intersection (ranges 0 5) (ranges 3 9)))))
  (is (thrown? IllegalArgumentException (intersection (ranges 1 5) (ranges 6 0))))
  (is (= '(5) (as-seq (intersection (ranges 1 5) (ranges 5 10))))))

(deftest test-union
  (is (= (ranges 3 10) (union (ranges 3 5) (ranges 5 10))))
  (is (= (ranges 0 9) (union (ranges 0 9) (ranges 3 4))))
  (is (= (ranges 0 9) (union (ranges 0 5) (ranges 3 9))))
  (is (= (ranges 1 10) (union (ranges 1 5) (ranges 6 10))))
  (is (= (ranges 0 9) (union (ranges 0) (ranges  1 9))))
  (is (= (ranges 0 9) (union (ranges 0 ... 0) (ranges  1 9))))
  )