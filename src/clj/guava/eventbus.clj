;; The usage of the eventbus is:
;; 1. create a eventbus
;;   (def bus (eventbus))
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

(defn eventbus
  "Creates a new eventbus with the specified name, if name not provided :default will be used."
  {:added "0.1"}
  ([]
     (eventbus :default))
  ([name]
     (let [bus {:name name
                :handlers (atom {})
                :events (mk-thread-local (LinkedList.))
                :dispatching? (mk-thread-local false)
                :register-lock (Object.)}]
       bus)))

(defn register!
  "Register the event-handler to handle the specified event"
  {:added "0.1"}
  [eventbus event-name handler]
  (when-not (fn? handler)
    (throw (IllegalArgumentException. "event handler should be a function accepts a single param.")))
  (let [handlers (:handlers eventbus)
        this-handlers (@handlers event-name)]
    (locking (:register-lock eventbus)
      (when-not this-handlers
        (swap! handlers assoc-in [event-name] [])))
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
    (.offer events {:event-name event-name :event event})
    (dispatch eventbus)))

(defn- mk-thread-local [init-value]
  (proxy [ThreadLocal] []
    (initialValue [] init-value)))

(defn- dispatch [eventbus]
  "Dispatches the event to handlers."
  ;; here if dispatching is true, it means current thread is dispatching events
  ;; then we just do nothing and leave this function to garantee the events dispatching order
  (when-not (.get ^ThreadLocal (:dispatching? eventbus))
    (.set ^ThreadLocal (:dispatching? eventbus) true)
    (try
      (let [^LinkedList events (.get ^ThreadLocal (:events eventbus))]
        (while (not (empty? events))
          (let [head-event (.poll events)
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
                    (error e)
                    ;; if user registered a event handler for :exception, then we 
                    ;; post the exceptions to the :exception event, user can register
                    ;; to handler to do some handling of the exception.
                    (if (not-empty (@handlers :exception))
                      (post! eventbus :exception e)))))
              ;; there is no event handler for this event, wrap them as :dead event
              ;; user code can register a handler for :dead to see whether there is
              ;; any event without event-handler
              (if (not-empty (@handlers :dead))
                (post! eventbus :dead {:event-name event-name :event event-obj}))))))
      (finally
       (.set ^ThreadLocal (:dispatching? eventbus) false)))))