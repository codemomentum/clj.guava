(ns ^{:doc "Operating system and jvm specific functions"
      :author "dennis zhuang<killme2008@gmail.com>"}
  clj.guava.os
  (:use [clojure.string :only [join]]))

(definline now
  "Returns the current time in milliseconds."
  {:added "0.1" :tag long}
  []
  `(System/currentTimeMillis))

(defn- props->map
  "Cast a java.util.Properties to a clojure map"
  {:tag clojure.lang.IPersistentMap}
  [props]
  (when props
    (reduce (fn [x [k v]] (assoc x k v)) {} props)))

(defn sys-property
  "Returns the system properties as a hash-map or specific system property value by name."
  {:added "0.1"}
  ([]
     (props->map  (System/getProperties)))
  ([^String name]
     (System/getProperty name)))

(defn os-info
  "Returns a hash-map contains os information including :name,:version and :arch"
  {:added "0.1" :tag clojure.lang.IPersistentMap}
  []
  {:name (sys-property "os.name") :version (sys-property "os.version") :arch (sys-property "os.arch")})

(defn java-version
  "Returns the java version in string or nil."
  {:added "0.1" :tag String}
  []
  (sys-property "java.version"))

(defmacro ^:private runtime-info
  [method]
  `(-> (Runtime/getRuntime) (.~method)))

(defn cpus
  "Returns the number of processors available to the Java virtual machine."
  {:added "0.1" :tag long}
  []
  (runtime-info availableProcessors))

(defn jvm-mem
  "Returns the Java Virtual Machine memory information in bytes including :free :total :max."
  {:added "0.1" :tag clojure.lang.IPersistentMap}
  []
  {:free (runtime-info freeMemory) :total (runtime-info totalMemory) :max (runtime-info maxMemory)})

(defn gc
  "Runs the garbage collector,the same with System.gc() in java."
  {:added "0.1" :static true}
  []
  (System/gc))

(defn getenv
  "Returns an string map of the current system environment or specific system environment value by name."
  {:added "0.1"}
  ([]
     (into {} (System/getenv)))
  ([^String name]
     (System/getenv name)))

(def ^{:added "0.1" :dynamic true :doc "Whether to warn on system exit,default is true."} *warn-on-exit* true)

(defn thread-dump
  "Dump current thread or specific thread stacks as an string sequence."
  {:added 0.1 :tag clojure.lang.ISeq}
  ([]
     (thread-dump (Thread/currentThread)))
  ([^Thread thread]
     (map str (seq (.getStackTrace thread)))))

(defn exit
  "Terminates the currently running Java Virtual Machine with status,default status is zero."
  ([]
     (exit 0))
  ([status]
     (when *warn-on-exit*
       (println "System exit warning,you can close this by binding *warn-on-exit* to be false.")
       (println (format "System exit with status %d, dump thead:\r\n%s" status (join "r\n" (thread-dump))))
       (println "---------------------------------------------------------"))
     (System/exit status)))

