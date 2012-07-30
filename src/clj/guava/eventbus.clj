;; The usage of the eventbus is:
;; 1. create a eventbus
;;   (def bus (mk-eventbus))
;; 2. register your event handler function to the eventbus
;;   (register! bus event-name handler)
;; 3. then the event generator can post the event:
;;   (post! bus event-name event)

(ns ^{:doc "Clojure version of guava eventbus, it is totally reimplemented"
      :author "xumingming"}
  clj.guava.eventbus
  (:import [java.util LinkedList])
  (:use [clojure.tools.logging :only [error]]))

(declare mk-thread-local dispatch)

(defn mk-eventbus
  "Creates a new eventbus with the specified name, if name not provided :default will be used."
  {:added "0.1"}
  ([]
     (mk-eventbus :default))
  ([name]
     (let [eventbus {:name name
                     :handlers (atom {})
                     :events (mk-thread-local (LinkedList.))
                     :dispatching? (mk-thread-local false)}]
       eventbus)))

(defn register!
  "Register the event-handler to handle the specified event"
  {:added "0.1"}
  [eventbus event-name handler]
  (when-not (fn? handler)
    (throw (IllegalArgumentException. "event handler should be a function accepts a single param.")))
  (let [handlers (:handlers eventbus)
        this-handlers (get-in @handlers [event-name])]
    (when-not this-handlers
      (swap! handlers assoc-in [event-name] []))
    (swap! handlers update-in [event-name] conj handler)))

(defn unregister!
  "Unregisers the event-handler from the event."
  {:added "0.1"}
  [eventbus event-name handler]
  (let [handlers (:handlers eventbus)
        this-handlers (@handlers event-name)]
    (if this-handlers
      (swap! handlers update-in [event-name] #(vec (remove #{handler} %)))
      (throw (RuntimeException. (str "No such event registered: " event-name))))))

(defn post!
  "Post a event to the specified eventbus"
  {:added "0.1"}
  [eventbus event-name event]
  (let [^LinkedList events (.get ^ThreadLocal (:events eventbus))]
    (.addLast events {:event-name event-name :event event})
    (dispatch eventbus)))

(defn- mk-thread-local [init-value]
  (proxy [ThreadLocal] []
    (initialValue [] init-value)))

(defn- dispatch [eventbus]
  "Dispatches the event to handlers."
  ;; here if dispatching is true, it means current thread is dispatching the thread
  ;; then we just do nothing and leave this function to garantee the events dispatching order
  (when-not (.get ^ThreadLocal (:dispatching? eventbus))
    (.set ^ThreadLocal (:dispatching? eventbus) true)
    (try
      (let [^LinkedList events (.get ^ThreadLocal (:events eventbus))]
        (while (not (empty? events))
          (let [head-event (.removeFirst events)
                handlers (:handlers eventbus)
                event-name (:event-name head-event)
                event-obj (:event head-event)
                this-handlers (@handlers event-name)]
            (if (and this-handlers (not (empty? this-handlers)))
              (doseq [handler this-handlers]
                ;; catch all the exceptions, to make sure one error in
                ;; a handler will not affect the whole event processing.
                (try
                  (handler event-obj)
                  (catch Throwable e
                    (error e))))
              ;; there is no event handler for this event, wrap them as :dead-event
              ;; user code can register a handler for :dead-event to see if there is
              ;; there is any event without event-handler
              (post! eventbus :dead-event {:event-name event-name :event event-obj})))))
      (finally
       (.set ^ThreadLocal (:dispatching? eventbus) false)))))