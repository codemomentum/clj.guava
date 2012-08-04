(ns ^{:doc "Clojure wrapper for com.google.common.cache"
      :author "xumingming"}
  clj.guava.cache
  (:import [com.google.common.cache LoadingCache RemovalListener CacheLoader
            CacheBuilder Weigher RemovalNotification CacheStats RemovalCause
            Cache])
  (:import [java.util.concurrent TimeUnit]))

(def ^{:added "0.1" :doc "time unit"}
  TIME-UNTIS {:nanos (TimeUnit/NANOSECONDS)
              :micros (TimeUnit/MICROSECONDS)
              :millis (TimeUnit/MILLISECONDS)
              :seconds (TimeUnit/SECONDS)
              :minutes (TimeUnit/MINUTES)
              :hours (TimeUnit/HOURS)
              :days (TimeUnit/DAYS)})

(def ^{:added "0.1" :doc "remove cause for the removal listener"}
  REMOVE-CAUSES {RemovalCause/EXPLICIT :explicit 
                 RemovalCause/REPLACED :replaced 
                 RemovalCause/COLLECTED :collected 
                 RemovalCause/EXPIRED :expired 
                 RemovalCause/SIZE :size })
(defn cache
  "Creates a cache with the specified loader.

  There are many options can be set on the cache:
    :loader              the function used to auto-load the value when the
                         key requested is not in the cache.
                         If the loader is not specified, then when you get
                         the key, you must provide the loader, and also the
                         function refresh & get-all is not supported.
    :max-size            the max count of object can be put into the cache.
    :max-weight          max weight of the cache, this param must specified
                         together with :weigher which calculate the weight
                         for each value put into the cache
    :expire-after-access expires the item after last write to the
                         item is X (nano/milli/second...) before
    :expire-after-write  expires the item after last access(read/write)
                         to the item is X (nano/milli/second...) before
    :removal-listener    specifies a callback to execute when an item
                         is removed from the cache.
    :refresh-after-write specifies that active entries are eligible for
                         automatic refresh once a fixed duration has
                         elapsed after the entry's creation, or the most
                         recent replacement of its value.
    :ticker              A time source; returns a time value representing
                         the number of nanoseconds elapsed since some fixed
                         but arbitrary point in time. NOTE: this is mostly
                         used in testing
    :weak-keys           stores keys using weak references.
    :weak-values         stores values using weak references.
    :soft-values         wraps values in soft references.

    removal-listener is a 3-params function:
      (def removal-listener (fn [key value cause]
                               ;; your logic here
                              ))

      cause is one of the following:
        :explicit
        :replaced
        :collected
        :expired
        :size
"
  {:added "0.1" :tag Cache}
  [& {:keys [loader max-size max-weight weigher expire-after-access expire-after-write
             removal-listener refresh-after-write ticker weak-keys weak-values soft-values]}]
  (let [^CacheBuilder builder (CacheBuilder/newBuilder)]
    (when max-size
      (.maximumSize builder max-size))
    (when max-weight
      (.maximumWeight builder max-weight))
    (when weigher
      (.weigher builder (reify Weigher
                          (weigh [this key value]
                            (weigher key value)))))
    (when expire-after-access
      (.expireAfterAccess builder (first expire-after-access) (TIME-UNTIS (second expire-after-access))))
    (when expire-after-write
      (.expireAfterWrite builder (first expire-after-write) (TIME-UNTIS (second expire-after-write))))
    (when removal-listener
      (.removalListener builder (reify RemovalListener
                                  (^void onRemoval [this ^RemovalNotification notification]
                                    (removal-listener (.getKey notification) (.getValue notification)
                                                      (REMOVE-CAUSES (.getCause notification)))))))
    (when ticker
      (.ticker builder ticker))
    (when weak-keys
      (.weakKeys builder))
    (when weak-values
      (.weakValues builder))
    (when soft-values
      (.softValues builder))
    (if loader
      (.build builder (proxy [CacheLoader] []
                        (load [key]
                          (loader key))))
      (.build builder))))

(defn get
  "Gets a value for the specified key.

   You can also optionally pass in a loader function, then if
   the key is not cache, the loader-function will be used to
   load the key, this implemented the:
      'if cached, return; otherwise create, cache and return'
   pattern.

    e.g.
      (get cache :my-big-data (fn [key]
                                 ;; your logic to get the big data
                                 ))"
  {:added "0.1"}
  ([^LoadingCache cache key]
     (.get cache key))
  ([^Cache cache key loader]
     (.get cache key loader)))

(defn get-all
  "Gets all the value for the provided keys. This function is usually used to warm up the cache."
  {:added "0.1"}
  [^LoadingCache cache ^Iterable keys]
  (let [amap (.getAll cache keys)]
    (into {} (for [[key value] amap]
               [key value]))))

(defn refresh
  "Refreshes the value of the given key."
  ^{:added "0.1"}
  [^LoadingCache cache key]
  (.refresh cache key))

(defn put!
  "Puts a key value pair into the cache."
  {:added "0.1"}
  [^Cache cache key value]
  (.put cache key value))

(defn as-map
  "Returns all the content in the cache as a map."
  {:added "0.1"}
  [^Cache cache]
  (into {} (for [[key value] (.asMap cache)]
             [key value])))

(defn invalidate!
  "Invalidates the specified keys in the cache, if the keys is not
  specified, then invalidate the entire cache."
  {:added "0.1"}
  [^Cache cache & keys]
  (if keys
    (.invalidateAll cache ^Iterable keys)
    (.invalidateAll cache)))

(defn cleanup
  "Mannually cleanup the cache.

  Caches built with cache function do not perform cleanup and evict
  values 'automatically,' or instantly after a value expires, or
  anything of the sort. Instead, it performs small amounts of
  maintenance during write operations, or during occasional read
  operations if writes are rare. So if you want to manually cleanup
  the cache, you can call this function."
  ^{:added "0.1"}
  [^Cache cache]
  (.cleanUp cache))

(defn stats
  "Returns the stats for the cache"
  {:added "0.1"}
  [^Cache cache]
  (let [^CacheStats cs (.stats cache)]
    {:hit-cnt (.hitCount cs)
     :request-cnt (.requestCount cs)
     :hit-rate (.hitRate cs)
     :miss-cnt (.missCount cs)
     :miss-rate (.missRate cs)
     :load-cnt (.loadCount cs)
     :load-success-cnt (.loadSuccessCount cs)
     :load-exception-cnt (.loadExceptionCount cs)
     :load-exception-rate (.loadExceptionRate cs)
     :total-load-time (.totalLoadTime cs)
     :average-load-penalty (.averageLoadPenalty cs)
     :eviction-cnt (.evictionCount cs)}))