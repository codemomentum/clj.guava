(ns clj.guava.collect
  (:refer-clojure :exclude [sort max min reverse empty?])
  (:import [com.google.common.base Function]
           [com.google.common.collect ImmutableMultiset Ranges Range DiscreteDomains DiscreteDomain BoundType]
           [com.google.common.collect Ordering])
  (:use [clj.guava.base :only [var-ns check-not-nil check-arg]]))

;;Ordering creation and manipulation
(defn order-by
  "Returns a ordering by type,valid type includes:
       :nat     the natural ordering on Comparable types.
       :str     compares Objects by the lexicographical ordering of
                their string representations, as returned by toString().
       :arb     returns an arbitrary ordering over all objects, for
                which compare(a, b) == 0 implies a == b (identity
                equality). There is no meaning whatsoever to the order
                imposed, but it is constant for the life of the VM.

       comparator   returns a ordering using a java.util.Comparator instance for comparing.

   default type is natural.
  "
  {:tag Ordering :added "0.1"}
  ([]
     (order-by :nat))
  ([type]
     (condp = type
       :nat (Ordering/natural)
       :str    (Ordering/usingToString)
       :arb (Ordering/arbitrary)
       (Ordering/from ^java.util.Comparator type))))

(defn ordering
  "Returns an ordering based on pred"
  {:tag Ordering :added "0.1"}
  [pred]
  (Ordering/from ^java.util.Comparator (comparator pred)))

(defn explicit
  "Returns an ordering that compares objects according to the order in which they are given in the given sequence or varadic arguments."
  {:tag Ordering :added "0.1"}
  ([col]
     (Ordering/explicit (vec col)))
  ([first & others]
     (Ordering/explicit first (into-array Object others))))

(defn reverse
  "Returns the reverse ordering."
  {:tag Ordering :added "0.1"}
  [^Ordering ord]
  (.reverse ord))

(defn nilsfirst
  "Returns an Ordering that orders nulls before non-null elements, and otherwise behaves the same as the original Ordering. "
  {:tag Ordering :added "0.1"}
  [^Ordering ord]
  (.nullsFirst ord))

(defn nilslast
  "Returns an Ordering that orders nulls after non-null elements, and otherwise behaves the same as the original Ordering. "
  {:tag Ordering :added "0.1"}
  [^Ordering ord]
  (.nullsLast ord))

(defn compound
  "Returns an ordering which first uses the ordering this, but which in the event of a \"tie\",
   then delegates to secondaryComparator. For example, to sort a bug list first by status and
   second by priority, you might use byStatus.compound(byPriority). For a compound ordering with
   three or more components, simply chain multiple calls to this method."
  {:tag Ordering :added "0.1"}
  [^Ordering ord ^java.util.Comparator cmp]
  (.compound ord cmp))


;;ordering application
(def ^{:tag Ordering :dynamic true} *ordering* nil)

(defn search
  "Searches sortedList for key using the binary search algorithm,returns the found index,otherwise returns a negative number."
  {:added "0.1"}
  ([col key]
     (check-not-nil *ordering* "*ordering* is not bound")
     (search *ordering* col key))
  ([^Ordering ord col key]
     (.binarySearch ord (list* col) key)))

(defn cmp
  "Compares its two arguments for the ordering. Returns a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second."
  {:added "0.1"}
  ([left right]
     (check-not-nil *ordering* "*ordering* is not bound")
     (cmp *ordering* left right))
  ([^Ordering ord left right]
     (.compare ord left right)))

(defn greatest
  " Returns the k greatest elements sequence of the given iterable according to the ordering, in order from greatest to least."
  {:tag clojure.lang.ISeq :added "0.1"}
  ([col k]
     (check-not-nil *ordering* "*ordering* is not bound")
     (greatest *ordering* col k))
  ([^Ordering ord col k]
     (seq (.greatestOf ord (seq col) k))))

(defn least
  "Returns the k least elements of the given iterable according to the ordering, in order from least to greatest."
  {:tag clojure.lang.ISeq :added "0.1"}
  ([col k]
     (check-not-nil *ordering* "*ordering* is not bound")
     (least *ordering* col k))
  ([^Ordering ord col k]
     (.leastOf ord (seq col) k)))

(defn max
  "Returns the greatest of the specified values according to the ordering."
  {:added "0.1"}
  ([^Ordering ord] nil)
  ([^Ordering ord x] x)
  ([^Ordering ord x y] (.max ord x y))
  ([^Ordering ord x y z & others]
     (.max ord x y z (into-array Object others))))

(defn max*
  "Returns the least of the specified values according to the ordering."
  {:added "0.1"}
  ([col]
     (check-not-nil *ordering* "*ordering* is not bound")
     (max* *ordering* col))
  ([^Ordering ord col]
     (.max ord ^Iterable (seq col))))

(defn min
  "Returns the greatest of the specified values according to the ordering."
  {:added "0.1"}
  ([^Ordering ord] nil)
  ([^Ordering ord x] x)
  ([^Ordering ord x y] (.min ord x y))
  ([^Ordering ord x y z & others]
     (.min ord x y z (into-array Object others))))

(defn min*
  "Returns the least of the specified values according to the ordering."
  {:added "0.1"}
  ([col]
     (check-not-nil *ordering* "*ordering* is not bound")
     (min* *ordering* col))
  ([^Ordering ord col]
     (.min ord ^Iterable (seq col))))

(defn sort
  "Returns a lazy sorted sequence of the items in coll according to the ordering."
  ([col]
     (check-not-nil *ordering* "*ordering* is not bound")
     (sort *ordering* col))
  ([^Ordering ord col]
     (lazy-seq (.sortedCopy ord (seq col)))))

(defn ordered?
  "Returns true if each element in iterable after the first is greater than or equal to the element that preceded it, according to the ordering."
  {:added "0.1"}
  ([col]
     (check-not-nil *ordering* "*ordering* is not bound")
     (ordered? *ordering* col))
  ([^Ordering ord col]
     (.isOrdered ord (seq col))))

(defn strictly-ordered?
  "Returns true if each element in iterable after the first is strictly greater than the element that preceded it, according to the ordering."
  {:added "0.1"}
  ([col]
     (check-not-nil *ordering* "*ordering* is not bound")
     (strictly-ordered? *ordering* col))
  ([^Ordering ord col]
     (.isStrictlyOrdered ord (seq col))))

(defmacro with-ordering
  "Bind *ordering* to the given ordering and evalute body with the binding."
  {:added "0.1"}
  [^Ordering ord & body]
  `(binding [*ordering* ~ord]
     ~@body))

;;other utilities
(def ^:private boolean-some
  (comp boolean some))

(defmulti include?
  "Returns true if key is present in the given collection, otherwise
  returns false or nil."
  {:added "0.1"}
  (fn [coll key]
    (type coll)))

(prefer-method include? clojure.lang.IPersistentVector java.util.Collection)
(prefer-method include? clojure.lang.IPersistentList java.util.Collection)
(prefer-method include? clojure.lang.IPersistentSet java.util.Collection)
(prefer-method include? clojure.lang.IPersistentMap java.util.Map)

(defmethod include? clojure.lang.IPersistentVector
  [coll key]
  (boolean-some #(= % key) coll))

(defmethod include? clojure.lang.IPersistentList
  [coll key]
  (boolean-some #(= % key) coll))

(defmethod include? clojure.lang.IPersistentSet
  [coll key]
  (boolean-some #(= % key) coll))

(defmethod include? clojure.lang.IPersistentMap
  [coll key]
  (contains? coll key))

(defmethod include? java.util.Collection
  [coll key]
  (boolean-some #(= % key) (seq coll)))

(defmethod include? java.util.Map
  [coll key]
  (.containsKey ^java.util.Map coll key))

(defmethod include? Range
  [coll key]
  (.contains ^Range coll key))

(defmethod include? :default
  [coll key]
  (when (seq coll)
    (or (= key (first coll)) (recur (next coll) key))))

(def ^:private character-discrete-domains
  (proxy [DiscreteDomain java.io.Serializable] []
    (next [^Character ch]
      (when-not (= ch Character/MAX_VALUE)
        (char (inc (int ch)))))
    (previous [^Character ch]
      (when-not (= ch Character/MIN_VALUE)
        (char (dec (int ch)))))
    (distance [^Character start ^Character end]
      (- (int start) (int end)))
    (minValue [] (Character/MIN_VALUE))
    (maxValue [] (Character/MAX_VALUE))))

(defn- ^{:tag DiscreteDomain } get-discrete-domain
  [clazz]
  (case clazz
    (#=java.lang.Long #=java.lang.Integer) (DiscreteDomains/longs)
    #=java.lang.Character character-discrete-domains
    nil))

(defn- check-range-elements-type
  [& es]
  (doseq [e es]
    (check-not-nil (get-discrete-domain (class e)) "Only supports integers and characters ranges.")))

(defmacro create-ranges
  [method & es]
  (apply check-range-elements-type es)
  `(. Ranges ~method ~@es))

(defmacro ranges
  "Create a integers or characters range,for example:
      (ranges 0)        Returns a range that contains only the given value 0;
      (ranges .. 0)     Returns a range that contains all values less than or equal to 0;
      (ranges ... 0)    Returns a range that contains all values strictly less than 0;
      (ranges 0 ..)     Returns a range that contains all values greater than or equal to 0;
      (ranges 0 ...)    The same with (ranges 0 ..) ;
      (ranges 0 .. 10)  Returns a range that contains all values greater than or equal to 0 and less than or equal to 10;
      (ranges 0 ... 10) Returns a range that contains all values greater than or equal to 0 and strictly less than 10 ;
      (ranges \\a \\z)   The same with (ranges \\a .. \\z).
  "
  {:added "0.1" :tag Range}
  ([x]
     `(create-ranges singleton ~x))
  ([x y]
     (condp = x
       '.. `(create-ranges atMost ~y)
       '... `(create-ranges lessThan ~y)
       (condp = y
         '.. `(create-ranges atLeast ~x)
         '... `(create-ranges atLeast ~x)
         `(create-ranges closed ~x ~y))))
  ([start op end]
     (check-range-elements-type start end)
     `(condp = (quote ~op)
        '.. (create-ranges closed ~start ~end)
        '... (create-ranges closedOpen ~start ~end)
        (throw (IllegalArgumentException. (format "Unknow symbol for range:%s" (quote ~op)))))))

(defn empty?
  "Returns true if the range is of the form [v..v) or (v..v]."
  {:added "0.1"}
  [^Range r]
  (.isEmpty r))

(def ^{:added "0.1" :doc "Bound type for ranges" :tag clojure.lang.IPersistentMap}
  BOUND-TYPES
  {BoundType/CLOSED :closed
   BoundType/OPEN  :open})

(defn lower-bound?
  "Returns true if the range has a lower endpoint."
  {:added "0.1"}
  [^Range r]
  (.hasLowerBound r))

(defn upper-bound?
  "Returns true if the range has an upper endpoint."
  {:added "0.1"}
  [^Range r]
  (.hasUpperBound r))

(defn bounded?
  "Returns true if the range is bounded."
  {:added "0.1"}
  [r]
  (and (upper-bound? r) (lower-bound? r)))

(defn lower-bound
  "Returns the lower endpoint and type of the range"
  {:added "0.1" :tag clojure.lang.IPersistentMap}
  [^Range r]
  {:type (BOUND-TYPES (.lowerBoundType r)) :value (.lowerEndpoint r)})

(defn upper-bound
  "Returns the upper endpoint and type of the range"
  {:added "0.1" :tag clojure.lang.IPersistentMap}
  [^Range r]
  {:type (BOUND-TYPES (.upperBoundType r)) :value (.upperEndpoint r)})

(defn lower
  "Returns the lowest value of the range,otherwise returns nil."
  {:added "0.1" :tag clojure.lang.IPersistentMap}
  [^Range r]
  (check-arg (lower-bound? r) "The range is unbounded below:%s" r)
  (let [v (.lowerEndpoint r)
        bt (BOUND-TYPES (.lowerBoundType r))]
    (when bt
      (case bt
        :closed v
        :open
        (when-let [dd (get-discrete-domain (class v))]
          (.next dd v))))))

(defn upper
  " Returns the highest value of the range,otherwise returns nil"
  {:added "0.1"}
  [^Range r]
  (check-arg (upper-bound? r) "The range is unbounded above:%s" r)
  (let [v (.upperEndpoint r)
        bt (BOUND-TYPES (.upperBoundType r))]
    (when bt
      (case bt
        :closed v
        :open
        (when-let [dd (get-discrete-domain (class v))]
          (.previous dd v))))))

(defn include-all?
  "Returns true if every element in values is contained in the range."
  {:added "0.1"}
  [^Range r ^Iterable values]
  (.containsAll r values))

(defn encloses?
  "Returns true if the bounds of right do not extend outside the bounds of left range."
  {:added "0.1"}
  [^Range left ^Range right]
  (.encloses left right))

(defn connected?
  "Returns true if there exists a (possibly empty) range which is enclosed by both left range and right."
  {:added "0.1"}
  [^Range left ^Range right]
  (.isConnected left right))

(defn intersection
  "Returns the maximal range enclosed by both left range and right, if such a range exists."
  {:added "0.1" :tag Range}
  [^Range left ^Range right]
  (.intersection left right))

(defn union
  "Returns the minimal range that encloses both left range and right."
  {:added "0.1" :tag Range}
  [^Range left ^Range right]
  (.span left right))

(defn as-seq
  "Returns a sequence for bounded range in order,now it just support integers and characters range."
  {:added "0.1" :tag clojure.lang.ISeq}
  [^Range r]
  (check-arg (or (upper-bound? r) (lower-bound? r)) "The range is unbounded:%s" r)
  (let [x (cond
           (upper-bound? r) (upper r)
           (lower-bound? r) (lower r))
        dd (get-discrete-domain (class x))]
    (check-not-nil dd "The range element type is not integers or characters:%s" (str (class x)))
    (seq (.asSet r dd))))
