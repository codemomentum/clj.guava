(ns clj.guava.test.io
  (:use [clj.guava.io]
        [clj.guava.base]
        [clojure.test]
        [clojure.java.io :only [as-url input-stream output-stream reader]])
  (:require [clj.guava.os :as os])
  (:import [com.google.common.io ByteStreams Files InputSupplier OutputSupplier]
           [java.io ByteArrayInputStream ByteArrayOutputStream]
           [java.nio.channels Channels]))

(deftest should-create-an-temp-dir
  (let [temp-dir (create-temp-dir!)]
    (is (not-nil? temp-dir))
    (is (not-nil? (.getName temp-dir)))))

(deftest should-touch-an-new-file
  (let [temp-dir (create-temp-dir!)
        new-file (clojure.java.io/file temp-dir "touch.test")]
    (is (not (.exists new-file)))
    (touch! new-file)
    (is (.exists new-file))))

(deftest should-create-parent-dirs
  (let [temp-dir (create-temp-dir!)
        new-file (clojure.java.io/file temp-dir "test/test/test/aaa.txt")
        fold (clojure.java.io/file temp-dir "test/test/")]
    (is (not (.exists fold)))
    (create-parent-dirs! new-file)
    (is (.exists fold))))

(deftest should-move-file
  (let [temp-dir (create-temp-dir!)
        old-pos (clojure.java.io/file temp-dir "bbccaa.txt")
        new-pos (clojure.java.io/file temp-dir "aabbcc.txt")]
    (touch! old-pos)
    (is (.exists old-pos))
    (is (not (.exists new-pos)))
    (move! old-pos new-pos)
    (is (.exists new-pos))
    (is (not (.exists old-pos)))))

(deftest should-simplify-path
  (let [temp-path-1 "."
        temp-path-2 "./"
        temp-path-3 "../"]
    (is (= "." (simplify-path temp-path-1)))
    (is (= "." (simplify-path temp-path-2)))
    (is (= ".." (simplify-path temp-path-3)))))

(deftest should-get-file-extension
  (let [file-name-with-ext "abc.txt"
        file-name-without-ext "abc"]
    (is (= "txt" (file-extension file-name-with-ext)))
    (is (= "" (file-extension file-name-without-ext)))))

(deftest should-get-url-for-resource-name
  (let [url (resource "clj/guava/io.clj")]
    (is (not-nil? url))
    (is (= java.net.URL (type url)))))

(deftest should-byte-array->input-supplier
  (let [bytes (byte-array (map byte [7 1 1]))
        input-supplier (->input-supplier bytes 1 1)]
    (is (not-nil? input-supplier))
    (is (= (byte 1)
           (aget (ByteStreams/toByteArray input-supplier) 0)))
    (is (isa? (type input-supplier) InputSupplier))))

(deftest should-string->input-supplier
  (let [test-str "hello"
        input-supplier (->input-supplier test-str)]
    (is (not-nil? input-supplier))
    (is (isa? (type input-supplier) InputSupplier))))

(deftest should-byte-input-supplier-wrap-to-char-input-supplier
  (let [bytes (byte-array (map byte [7 1 1]))
        byte-input-supplier (->input-supplier bytes 1 1)
        char-input-supplier (->input-supplier byte-input-supplier (CHARSETS :utf-8 ))]
    (is (not-nil? char-input-supplier))
    (is (isa? (type char-input-supplier) InputSupplier))))

(deftest should-use-url-to-define-input-supplier
  (let [url (resource "clj/guava/io.clj")
        input-supplier (->input-supplier url)]
    (is (not-nil? input-supplier))
    (is (isa? (type input-supplier) InputSupplier))))

(def ^:File test-file nil)
(def ^:String test-file-name "")
(def ^:File test-file-sec nil)
(def ^:String test-file-name-sec "")

(defn file-system-fixture
  [f]
  (let [filename "yyyuuuuiiidsfadf.txt"
        filename-sec "yyyuuuuiiidsfadf2.txt"
        tmpdir (os/getenv "java.io.tmpdir")
        temp-file (clojure.java.io/file tmpdir filename)
        temp-file-name (.getAbsolutePath temp-file)
        temp-file-sec (clojure.java.io/file tmpdir filename-sec)
        temp-file-name-sec (.getAbsolutePath temp-file-sec)]
    (try
      (touch! temp-file)
      (with-redefs [^File test-file temp-file
                    ^String test-file-name  temp-file-name
                    ^File test-file-sec temp-file-sec
                    ^String test-file-name-sec  temp-file-name-sec]
        (f))
      (finally
        (.delete temp-file)
        (.delete temp-file-sec)))))

(use-fixtures :each file-system-fixture)

(deftest should-file->input-supplier
  (let [input-supplier (->input-supplier test-file)]
    (is (not-nil? input-supplier))
    (is (isa? (type input-supplier) InputSupplier))))

(deftest should-file->output-supplier-with-byte-and-none-append-mode
  (let [output-suppler (->output-supplier test-file)]
    (is (not-nil? output-suppler))
    (is (isa? (type output-suppler) OutputSupplier))))

(deftest should-file->output-supplier-with-byte-and-append-mode
  (let [output-suppler (->output-supplier test-file)]
    (is (not-nil? output-suppler))
    (is (isa? (type output-suppler) OutputSupplier))))

(deftest should-file->output-supplier-with-byte-and-append-mode
  (let [output-suppler (->output-supplier test-file true)]
    (is (not-nil? output-suppler))
    (is (isa? (type output-suppler) OutputSupplier))))

(deftest should-file->output-supplier-with-file-and-none-append-mode
  (let [output-suppler (->output-supplier test-file (:utf-8 CHARSETS))]
    (is (not-nil? output-suppler))
    (is (isa? (type output-suppler) OutputSupplier))))

(deftest should-file->output-supplier-with-file-and-append-mode
  (let [output-suppler (->output-supplier test-file (:utf-8 CHARSETS) true)]
    (is (not-nil? output-suppler))
    (is (isa? (type output-suppler) OutputSupplier))))

(deftest should-append-to-an-file
  (append! "test1" test-file (:utf-8 CHARSETS))
  (append! "test2" test-file (:utf-8 CHARSETS))
  (is (= "test1test2" (read-first-line test-file (:utf-8 CHARSETS)))))

(deftest should-read-first-line-for-file
  (append! "test1" test-file (:utf-8 CHARSETS))
  (is (= "test1" (read-first-line test-file (:utf-8 CHARSETS)))))

(deftest should-read-first-line-for-input-supplier
  (let [input-supplier (->input-supplier test-file (:utf-8 CHARSETS))]
    (append! "121" test-file (:utf-8 CHARSETS))
    (is (= "121" (read-first-line input-supplier)))))

(deftest should-equal-if-two-file-has-same-content
  (append! "clj" test-file (:utf-8 CHARSETS))
  (append! "clj" test-file-sec (:utf-8 CHARSETS))
  (is (equal test-file test-file-sec))
  (append! "clj" test-file-sec (:utf-8 CHARSETS))
  (is (not (equal test-file test-file-sec))))

(deftest should-equal-if-two-input-supplier-has-same-content
  (append! "clj" test-file (:utf-8 CHARSETS))
  (append! "clj" test-file-sec (:utf-8 CHARSETS))
  (is (equal (->input-supplier test-file) (->input-supplier test-file-sec)))
  (append! "clj" test-file-sec (:utf-8 CHARSETS))
  (is (not (equal (->input-supplier test-file) (->input-supplier test-file-sec)))))

(deftest should-readable->string
  (append! "test" test-file (:utf-8 CHARSETS))
  (with-open [r (clojure.java.io/reader test-file)]
    (is (= "test" (->string r)))))

(deftest should-input-supplier->string
  (append! "test" test-file (:utf-8 CHARSETS))
  (let [input-supplier (->input-supplier test-file (:utf-8 CHARSETS))]
    (is (= "test" (->string input-supplier)))))

(deftest should-file->string
  (append! "test" test-file (:utf-8 CHARSETS))
  (is (= "test" (->string test-file (:utf-8 CHARSETS)))))

(deftest should-url->string
  (append! "test" test-file (:utf-8 CHARSETS))
  (is (= "test" (->string (as-url test-file) (:utf-8 CHARSETS)))))

(deftest should-string->byte-array
  (is (= "123" (apply str (map char (->byte-array "123" (:utf-8 CHARSETS)))))))

(deftest should-file->byte-array
  (append! "123" test-file (:utf-8 CHARSETS))
  (is (= "123" (apply str (map char (->byte-array test-file))))))

(deftest should-input-supplier->byte-array
  (append! "123" test-file (:utf-8 CHARSETS))
  (is (= "123" (apply str (map char (->byte-array (->input-supplier test-file)))))))

(deftest should-input-stream->byte-array
  (append! "1233" test-file (:utf-8 CHARSETS))
  (with-open [in (input-stream test-file)]
    (is (= "1233" (apply str (map char (->byte-array in)))))))

(deftest should-url->byte-array
  (append! "123" test-file (:utf-8 CHARSETS))
  (is (= "123" (apply str (map char (->byte-array (as-url test-file)))))))

(deftest should-map-file
  (append! "test-map" test-file (:utf-8 CHARSETS))
  (is (not-nil? (file-map test-file (:read-only MAP-MODE)))))

(deftest should-copy-char-from-readable-to-appendable
  (append! "12345678" test-file (:utf-8 CHARSETS))
  (let [builder (StringBuilder. )]
    (with-open [r (clojure.java.io/reader test-file)]
      (copy! r builder))
    (is (= "12345678" (.toString builder)))))

(deftest should-copy-char-from-input-supplier-to-appendable
  (append! "1234568" test-file (:utf-8 CHARSETS))
  (let [builder (StringBuilder. )
        input-supplier (->input-supplier test-file (:utf-8 CHARSETS))]
    (copy! input-supplier builder)
    (is (= "1234568" (.toString builder)))))

(deftest should-copy-bytes-from-input-stream-to-output-stream
  (append! "1234568" test-file (:utf-8 CHARSETS))
  (with-open [in (input-stream  test-file)
              out (output-stream test-file-sec)]
    (copy! in out))
  (is (equal test-file test-file-sec)))

(deftest should-copy-bytes-from-input-stream-to-output-supplier
  (append! "1234568" test-file (:utf-8 CHARSETS))
  (with-open [in (input-stream  test-file)]
    (copy! in (->output-supplier test-file-sec))
    (is (equal test-file test-file-sec))))

(deftest should-copy-bytes-from-input-supplier-to-output-stream
  (append! "1234568" test-file (:utf-8 CHARSETS))
  (with-open [out (output-stream test-file-sec)]
    (copy! (->input-supplier test-file) out))
  (is (equal test-file test-file-sec)))

(deftest should-copy-bytes-from-input-channel-to-output-channel
  (append! "1234568" test-file (:utf-8 CHARSETS))
  (with-open [in (input-stream  test-file)
              out (output-stream test-file-sec)]
    (copy! (Channels/newChannel in) (Channels/newChannel out)))
  (is (equal test-file test-file-sec)))

(deftest should-copy-bytes-from-file-to-output-supplier
  (append! "1234568" test-file (:utf-8 CHARSETS))
  (copy! test-file (->output-supplier test-file-sec))
  (is (equal test-file test-file-sec)))

(deftest should-copy-bytes-from-file-to-file
  (append! "1234568" test-file (:utf-8 CHARSETS))
  (copy! test-file test-file-sec)
  (is (equal test-file test-file-sec)))

(deftest should-copy-char-from-file-to-output-supplier
  (append! "1234568" test-file (:utf-8 CHARSETS))
  (copy! test-file (->output-supplier test-file-sec (:utf-8 CHARSETS)) (:utf-8 CHARSETS))
  (is (equal test-file test-file-sec)))

(deftest should-copy-char-from-input-supplier-to-file
  (append! "1234568" test-file (:utf-8 CHARSETS))
  (copy! (->input-supplier test-file (:utf-8 CHARSETS)) test-file-sec (:utf-8 CHARSETS)) 
  (is (equal test-file test-file-sec)))

(deftest should-copy-char-from-file-to-output-stream
  (append! "1234568" test-file (:utf-8 CHARSETS))
  (with-open [out (output-stream test-file-sec)]
    (copy! test-file out))
  (is (equal test-file test-file-sec)))

(deftest should-copy-char-from-file-to-appendable
  (append! "12345678" test-file (:utf-8 CHARSETS))
  (let [builder (StringBuilder. )]
    (copy! test-file builder (:utf-8 CHARSETS))
    (is (= "12345678" (.toString builder)))))

(deftest should-copy-bytes-from-url-to-output-stream
  (append! "1234568" test-file (:utf-8 CHARSETS))
  (with-open [out (output-stream test-file-sec)]
    (copy! (as-url test-file) out))
  (is (equal test-file test-file-sec)))

(deftest should-read-lines-from-file
  (append! "1\n2\n3" test-file (:utf-8 CHARSETS))
  (let [lines (read-lines test-file (:utf-8 CHARSETS))]
    (is (= 3 (count lines)))))

(deftest should-read-lines-from-input-supplier
  (append! "1\n2\n3" test-file (:utf-8 CHARSETS))
  (let [lines (read-lines (->input-supplier test-file (:utf-8 CHARSETS)))]
    (is (= 3 (count lines)))))

(deftest should-read-lines-from-url
  (append! "1\n2\n3" test-file (:utf-8 CHARSETS))
  (let [lines (read-lines (as-url test-file) (:utf-8 CHARSETS))]
    (is (= 3 (count lines)))))

(deftest should-write-char-seq-to-file
  (write! "test" test-file (:utf-8 CHARSETS))
  (is (= "test" (->string test-file (:utf-8 CHARSETS)))))

(deftest should-write-char-seq-to-file
  (write! (->byte-array "test" (:utf-8 CHARSETS)) test-file)
  (is (= "test" (->string test-file (:utf-8 CHARSETS)))))

(deftest should-skip-char
  (append! "test" test-file (:utf-8 CHARSETS))
  (with-open [r (reader test-file)]
    (skip-fully! r 2)
    (is (= "st" (->string r)))))

(deftest should-skip-bytes
  (write! (byte-array (map byte [7 1 2])) test-file)
  (with-open [in (input-stream test-file)]
    (skip-fully! in 2)
    (is (= 2 (aget (->byte-array in) 0)))))

(deftest should-get-input-supplier-length-in-bytes
  (is (= 3 (length (->input-supplier (byte-array (map byte [7 1 2])))))))

(deftest should-slice-a-input-supplier
  (let [input-supplier (->input-supplier (byte-array (map byte [1 2 3])))
        sliced-supplier (slice input-supplier 1 1)]
    (is (= (seq [2]) (seq (->byte-array sliced-supplier))))))

