(ns ^{:doc "Clojure version for guava io"
      :author "lysu <sulifx@gmail.com>"}
  clj.guava.io
  (:import [com.google.common.io Files ByteStreams
            CharStreams InputSupplier LineProcessor
            OutputSupplier ByteArrayDataOutput
            PatternFilenameFilter Resources 
            LittleEndianDataInputStream LittleEndianDataOutputStream NullOutputStream
            LimitInputStream FileBackedOutputStream CountingInputStream CountingOutputStream]
           [com.google.common.base Charsets]
           [java.nio.charset Charset]
           [java.nio.channels ReadableByteChannel
            WritableByteChannel FileChannel$MapMode]
           [java.nio MappedByteBuffer]
           [java.io File InputStream OutputStream
            Reader FilenameFilter]
           [java.net URL]
           [java.util.zip Checksum])
  (:use [clj.guava.base :only [check-arg]]))

(def ^{:doc "Type object for a Java primitive byte array." :private true} 
  byte-array-type (class (make-array Byte/TYPE 0)))

(def ^{:added "0.1" :doc "charset type const"}
  CHARSETS {:iso-8859-1 Charsets/ISO_8859_1 
            :us-ascii Charsets/US_ASCII
            :utf-16 Charsets/UTF_16
            :utf-16be Charsets/UTF_16BE
            :utf-16le Charsets/UTF_16LE
            :utf-8 Charsets/UTF_8})

(defprotocol ^{:added "0.1"} IOSupplierFactory
             "Factory functions that create guava IO supplier.

   Common options include
   
     :append    true to open stream in append mode
     :charset   string name of encoding to use, e.g. \"UTF-8\".
     :off        offset to read bytes
     :len        length read from offset
    
  use OutputSupplier, we could take free from open/close resource recycle, and easy to use other Guava IO functions.
  see this more detail on http://code.google.com/p/guava-libraries/wiki/IOExplained#InputSupplier_and_OutputSupplier
 "
             (^{:added "0.1"} make-input-reader-supplier [x opts] "Creates a input-reader-supplier. See also IOSupplierFactory docs.")
             (^{:added "0.1"} make-output-writer-supplier [x opts] "Creates a input-writer-supplier. See also IOSupplierFactory docs.")
             (^{:added "0.1"} make-input-stream-supplier [x opts] "Creates a input-stream-supplier. See also IOSupplierFactory docs.")
             (^{:added "0.1"} make-output-stream-supplier [x opts] "Creates a output-stream-supplier. See also IOSupplierFactory docs."))

(defn input-stream-supplier
  "Returns a InputSupplier of input-stream  --- a factory for underlying bytes resources.
  The underlying resources is base on the parameter your given
  for example:
    (byte-array [:off offset] [:len length])   -> ByteArrayInputStream factory
    (url)                                           -> input-stream factory
    (file)                                          -> input-stream factory"
  {:added "0.1" :tag InputSupplier}
  [x & opts]
  (make-input-stream-supplier x (when opts (apply hash-map opts))))

(defn input-reader-supplier
  "Returns a InputSupplier of reader  --- a factory for underlying characters resources.
  The underlying resources is base on the parameter your give
  for example:
    (string)                                  -> StringReader factory
    (input-supplier [:charset charset])   -> reader factory
    (url [:charset charset])                -> reader factory
    (file [:charset charset])               -> reader factor

  when charset is not supplied, utf-8 will be default option."
  {:added "0.1" :tag InputSupplier}
  [x & opts]
  (make-input-reader-supplier x (when opts (apply hash-map opts))))

(defn output-stream-supplier
  "Returns a OutputSupplier of output-stream  --- a factory for underlying bytes resources.
  The underlying resources is base on the parameter your give.
  for example:
    (file)                                           -> output-stream factory
    (file [:append append])                        -> append output-stream factory
   
  when append is not supplied will use true for default value"
  {:added "0.1" :tag OutputSupplier}
  [x & opts]
  (make-output-stream-supplier x (when opts (apply hash-map opts))))

(defn output-writer-supplier
  "Returns a OutputSupplier of writer  --- a factory for underlying characters resources.
  The underlying resources is base on the parameter your give.
  for example:
    (output-supplier [:charset charset])          -> writer factory
    (file [:charset charset])                       -> writer factory
    (file [:charset charset] [:append append])    -> append writer factory

  when charset/append is not supplied will use :utf-8/true as default"
  {:added "0.1" :tag OutputSupplier}
  [x & opts]
  (make-output-writer-supplier x (when opts (apply hash-map opts))))

(defn- ^Boolean append? [opts]
  (boolean (:append opts)))

(defn- ^Charsets encoding [opts]
  (or (:charset opts) (:utf-8 CHARSETS)))

(def default-streams-impl
  {:make-input-reader-supplier
   (fn [x opts] 
     (throw (IllegalArgumentException. (str "Cannot open <" (pr-str x) "> as an InputSupplier of reader."))))
   :make-output-writer-supplier
   (fn [x opts] 
     (throw (IllegalArgumentException. (str "Cannot open <" (pr-str x) "> as an InputSupplier of writer."))))
   :make-input-stream-supplier 
   (fn [x opts] 
     (throw (IllegalArgumentException. (str "Cannot open <" (pr-str x) "> as an InputSupplier of input-stream."))))
   :make-output-stream-supplier
   (fn [x opts] 
     (throw (IllegalArgumentException. (str "Cannot open <" (pr-str x) "> as an OutputSupplier of output-stream."))))})

(extend byte-array-type
  IOSupplierFactory
  (assoc default-streams-impl
    :make-input-stream-supplier 
    (fn [x opts]
      (let [off (:off opts) len (:len opts)]
        (if (and off len)
          (ByteStreams/newInputStreamSupplier x off len)
          (ByteStreams/newInputStreamSupplier x))))))

(extend String
  IOSupplierFactory
  (assoc default-streams-impl
    :make-input-reader-supplier
    (fn [x opts]
      (CharStreams/newReaderSupplier x))))

(extend InputSupplier
  IOSupplierFactory
  (assoc default-streams-impl
    :make-input-reader-supplier
    (fn [x opts]
      (CharStreams/newReaderSupplier x (encoding opts)))))

(extend URL
  IOSupplierFactory
  (assoc default-streams-impl
    :make-input-stream-supplier
    (fn [x opts]
      (Resources/newInputStreamSupplier x))
    :make-input-reader-supplier
    (fn [x opts]
      (Resources/newReaderSupplier x (encoding opts)))))

(extend File
  IOSupplierFactory
  (assoc default-streams-impl
    :make-input-stream-supplier
    (fn [x opts]
      (Files/newInputStreamSupplier x))
    :make-input-reader-supplier
    (fn [x opts]
      (Files/newReaderSupplier x (encoding opts)))
    :make-output-stream-supplier
    (fn [x opts]
      (Files/newOutputStreamSupplier x (append? opts)))
    :make-output-writer-supplier
    (fn [x opts]
      (Files/newWriterSupplier x (encoding opts) (append? opts)))))

(extend OutputSupplier
  IOSupplierFactory
  (assoc default-streams-impl
    :make-output-writer-supplier
    (fn [x opts]
      (CharStreams/newWriterSupplier x (encoding opts)))))

(extend Object
  IOSupplierFactory
  default-streams-impl)

(defprotocol ^{:added "0.1"} GuavaCoercions
             "Coerce between resource, string and bytes."
             (^{:tag String, :added "0.1"} make-as-string [x opts] "Coerce argument to a string.")
             (^{:tag bytes, :added "0.1"} as-byte-array [x] "Coerce argument to a byte-array"))

(defn as-string
  "Coerce argument to a string. 
   Common option will be

    :charset   string name of encoding to use, e.g. \"UTF-8\".

   if not supply charset will use utf-8 as default"
  {:added "0.1" :tag String}
  [x & opts]
  (make-as-string x (when opts (apply hash-map opts))))

(extend-protocol GuavaCoercions
  nil
  (make-as-string [_ _] nil)
  (as-byte-array [_] nil)

  Readable
  (make-as-string [x opts] (CharStreams/toString x))
  (as-byte-array [x] (throw (IllegalArgumentException. (str "Cannot as byte-array for <" (pr-str x) "> ."))))

  InputSupplier
  (make-as-string [x opts] (CharStreams/toString x))
  (as-byte-array [x] (ByteStreams/toByteArray x))

  File
  (make-as-string [x opts] (Files/toString x (encoding opts)))
  (as-byte-array [x] (Files/toByteArray x))

  URL
  (make-as-string [x opts] (Resources/toString x (encoding opts)))
  (as-byte-array [x] (Resources/toByteArray x))

  InputStream
  (make-as-string [x _] (throw (IllegalArgumentException. (str "Cannot as string for <" (pr-str x) "> ."))))
  (as-byte-array [x] (ByteStreams/toByteArray x)))

(defmulti 
  #^{:doc "Internal helper for copy operation"
     :private true
     :arglists '([from to opts])
     :tag long}
  do-copy
  (fn [from to opts] [(type from) (type to)]))

(defmethod do-copy [InputSupplier OutputSupplier]
  ([^InputSupplier in ^OutputSupplier out opts]
     (check-arg (:type opts) 
                "Type option must be supplied for coping from InputSupplier to OutputSupplier.")
     (condp = (:type opts)
       :char (CharStreams/copy in out)
       :byte (ByteStreams/copy in out)
       (throw (IllegalArgumentException. 
               "Type must be supplied as :char or :byte")))))

(defmethod do-copy [InputSupplier OutputStream]
  ([^InputSupplier in ^OutputStream out opts]
     (ByteStreams/copy in out)))

(defmethod do-copy [InputStream OutputSupplier]
  ([^InputStream in ^OutputSupplier out opts]
     (ByteStreams/copy in out)))

(defmethod do-copy [InputStream OutputStream]
  ([^InputStream in ^OutputStream out opts]
     (ByteStreams/copy in out)))

(defmethod do-copy [ReadableByteChannel WritableByteChannel]
  ([^ReadableByteChannel in ^WritableByteChannel out opts]
     (ByteStreams/copy in out)))

(defmethod do-copy [Readable Appendable]
  ([^Readable in ^Appendable out opts]
     (CharStreams/copy in out)))

(defmethod do-copy [InputSupplier Appendable]
  ([^InputSupplier in ^Appendable append opts]
     (CharStreams/copy in append)))

(defmethod do-copy [InputSupplier File]
  ([^InputSupplier in ^File f opts]
     (check-arg (:type opts) "Type must be supplied for copy from InputSupplier to File")
     (condp = (:type opts)
       :char (Files/copy in f ^Charset (encoding opts))
       :byte (Files/copy in f)
       (throw (IllegalArgumentException. 
               "Type must be supplied as :char or :byte")))))

(defmethod do-copy [File OutputSupplier]
  ([^File from ^OutputSupplier to opts]
     (check-arg (:type opts) "Type must be supplied for copy from InputSupplier to File")
     (condp = (:type opts)
       :char (Files/copy from ^Charset (encoding opts) to)
       :byte (Files/copy from to)
       (throw (IllegalArgumentException. 
               "Type must be supplied as :char or :byte")))))

(defmethod do-copy [File OutputStream]
  ([^File from  ^OutputStream to opts]
     (Files/copy from to)))

(defmethod do-copy [File Appendable]
  ([^File from ^Appendable to opts]
     (Files/copy from ^Charset (encoding opts) to)))

(defmethod do-copy [URL OutputStream]
  ([^URL from ^OutputStream to opts]
     (Resources/copy from to)))

(defmethod do-copy [File File]
  ([^File from ^File to opts]
     (Files/copy from to)))

(defn copy
  "Copies one source to another.  Returns effect byte(when :byte) or char (when :char) or throws IOException.

  'Options' are key/value pairs and may be one of

    :type        operation type for handle data :char or :byte
    :charset     encoding to use if converting between
                  byte and char streams see CHARSETS.  
   
  if charset not supplied will use :utf-8 as default

  for example:
  (input-supplier output-supplier :type :byte)                                  -> copy and return bytes number effected
  (input-supplier output-supplier :type :char :charset (:utf-8  CHARSETS))   -> copy and return characters number effected
  (input-supplier output-stream)                                                  -> copy and return bytes number effected
  (input-stream output-supplier)                                                  -> copy and return bytes number effected
  (input-stream output-stream)                                                    -> copy and return bytes number effected
  (readable appendable)                                                            -> copy and return characters
  (file appendable :charset (:utf-8 CHARSETS))                                  -> copy and return characters copied
  (input-supplier appendable)                                                     -> copy and return characters copied
  (input-supplier file :type [] :charset [])                                    -> copy and return byte/characters number base on :type
  (file output-supplier :type [] :charset [])                                   -> copy and return byte/characters number base on :type
  (file output-stream)                                                             -> copy and return bytes copied
  (file file)                                                                       -> copy and return bytes copied
  (url output-stream)                                                              -> copy and return bytes copied

  it will close resource only when use any input-supplier or output-supplier"
  {:added "0.1" :tag long}
  [input output & opts]
  (do-copy input output (when opts (apply hash-map opts))))

(defmulti do-read-first-line
  #^{:doc "Internal helper for read first line"
     :private true
     :arglists '([file charset] [input-supplier])
     :tag String}
  (fn [file & args]
    (type file)))

(defmethod do-read-first-line File
  ([file charset]
     (check-arg charset
                "The charset must be supplied for read line from file")
     (Files/readFirstLine file charset)))

(defmethod do-read-first-line InputSupplier
  ([in] (CharStreams/readFirstLine in)))

(defn read-first-line
  "Reads first line from file/InputSupplier of reader
   The line doesn't contain line-termination characters"
  {:added "0.1" :tag String}
  [res & more]
  (apply do-read-first-line res more))

(defmulti do-skip-fully
  #^{:doc "Internal skip fully"
     :private true
     :arglists '([input-stream] [reader])
     :tag Void}
  {:added "0.1"}
  (fn [src n]
    (type src)))

(defmethod do-skip-fully InputStream
  ([in n]
     (ByteStreams/skipFully in n)))

(defmethod do-skip-fully Reader
  ([r n]
     (CharStreams/skipFully r n)))

(defn skip-fully
  "Discards n characters (when use reader) 
   or n bytes (when use input-stream)in resource"
  {:added "0.1" :tag Void}
  [res n]
  (do-skip-fully res n))

(defn append
  "Appends a character sequence to a file using the given character set"
  {:added "0.1" :tag Void}
  ([^CharSequence from ^File to ^Charset charset]
     (Files/append from to charset)))

(defmulti do-equal
  #^{:doc "Internal helper for equal"
     :private true
     :arglists '([file file] [input-supplier input-supplier])
     :tag Void}  
  (fn [v1 v2]
    [(type v1) (type v2)]))

(defmethod do-equal [File File]
  ([file1 file2]
     (Files/equal file1 file2)))

(defmethod do-equal [InputSupplier InputSupplier]
  ([in1 in2]
     (ByteStreams/equal in1 in2)))

(defn equal 
  "Returns true if inputs/files contain same bytes"
  {:added "0.1" :tag Boolean}
  [v1 v2] (do-equal v1 v2))

(def ^{:added "0.1" :doc "Checksum Holder"}
  CHECKSUM {:crc32 java.util.zip.CRC32
            :adler32 java.util.zip.Adler32})

(defmulti do-checksum
  #^{:doc "Internal helper for checksum"
     :private true
     :arglists '([file checksum] [input-supplier-of-stream checksum])
     :tag long}  
  (fn [src checksum]
    (type src)))

(defmethod do-checksum File
  ([file checksum]
     (Files/getChecksum file checksum)))

(defmethod do-checksum InputSupplier
  ([in checksum]
     (ByteStreams/getChecksum in checksum)))

(defn checksum 
  "Computes and returns the checksum for file or input-supplier of bytes
   The checksum object is reset when this method returns success"
  {:added "0.1" :tag long}
  [res checksum] (do-checksum res checksum))

(def ^{:added "0.1" :doc "MapMode Holder"}
  MAP-MODE {:private (FileChannel$MapMode/PRIVATE)
            :read-only (FileChannel$MapMode/READ_ONLY)
            :read-write (FileChannel$MapMode/READ_WRITE)})

(defn file-map
  "Maps file into memory
   see more detail for MappedByteBuffer in 
    http://docs.oracle.com/javase/6/docs/api/java/nio/MappedByteBuffer.html?is-external=true"
  {:tag MappedByteBuffer  :added "0.1"}
  ([^File file]
     (Files/map file))
  ([^File file ^FileChannel$MapMode  mode]
     (Files/map file mode))
  ([^File file ^FileChannel$MapMode mode size]
     (Files/map file mode size)))

(defn create-parent-dirs
  "Creates any necessary but nonexistent parent directories of specified file.
   Note that if this operation fails it may have succeeded in some (but not all)
   of necessary parent directories"
  ([^File file]
     (Files/createParentDirs file)))

(defn create-temp-dir
  "Atomically creates a new directory somewhere beneath the system's 
   temporary directory (as defined by the java.io.tmpdir system property), 
   and returns its name.
   Use this method instead of File.createTempFile(String, String) 
   when you wish to create a directory, not a regular file. 
   A common pitfall is to call createTempFile, 
   delete the file and create a directory in its place, 
   but this leads a race condition which can be exploited to create security vulnerabilities, 
   especially when executable files are to be written into the directory.

  This method assumes that the temporary volume is writable, has free inodes and free blocks, 
  and that it will not be called thousands of times per second."
  {:added "0.1" :tag File}
  ([]
     (Files/createTempDir)))

(defn touch
  "Create an empty file or update the last update timestamp
   on the same as the unix command of the same name"
  {:added "0.1"}
  ([^File file]
     (Files/touch file)))

(defn move
  "Moves file from one path to another"
  [^File from ^File to]
  (Files/move from to))

(defn file-extension
  "Returns the file extension for give file name
   or the empty string if the file has no extension.
   the result does not include the '.'"
  {:tag String :added "0.1"}
  ([^String file-name]
     (Files/getFileExtension file-name)))

(defn simplify-path
  "Returns the lexically cleaned form of the path name, 
   usually (but not always) equivalent to the original. 
   The following heuristics are used:
      empty string becomes .
      . stays as .
      fold out ./
      fold out ../ when possible
      collapse multiple slashes
      delete trailing slashes
   These heuristics do not always match the behavior of the filesystem. 
   In particular, consider the path a/../b, which simplifyPath will change to b. 
   If a is a symlink to x, a/../b may refer to a sibling of x,
   rather than the sibling of a referred to by b."
  {:tag String :added "0.1"}
  [path-name]
  (Files/simplifyPath path-name))

(defmulti read-lines
  "Reads line from file/InputSupplier/url"
  {:added "0.1"}
  (fn [supplier & args]
    (type supplier)))

(defmethod read-lines InputSupplier
  ([^InputSupplier supplier]
     (seq (CharStreams/readLines supplier)))
  ([^InputSupplier supplier ^LineProcessor callback]
     (seq (CharStreams/readLines supplier callback))))

(defmethod read-lines File
  ([file charset]
     (seq (Files/readLines file charset)))
  ([file charset ^LineProcessor callback]
     (seq (Files/readLines file charset callback))))

(defmethod read-lines URL
  ([url charset]
     (Resources/readLines url charset))
  ([url charset ^LineProcessor callback]
     (Resources/readLines url charset callback)))

(defmulti write
  "write byte-array/CharSequence to File/OutputSupplier"
  {:added "0.1"}
  (fn [from to & args]
    [(type from) (type to)]))

(defmethod write [byte-array-type File]
  ([from to]
     (Files/write from to)))

(defmethod write [CharSequence File]
  ([from to charset]
     (Files/write from to charset)))

(defmethod write [CharSequence OutputSupplier]
  ([from to]
     (CharStreams/write from to)))

(defmulti read-bytes
  "Process the bytes of file/supplied stream"
  (fn [src processor]
    (type src)))

(defmethod read-bytes InputSupplier
  ([supplier processor]
     (ByteStreams/readBytes supplier processor)))

(defmethod read-bytes File
  ([file processor]
     (Files/readBytes file processor)))

(defmulti do-pattern-file-name-filter
  #^{:doc "Internal helper for pattern file name filte"
     :private true
     :arglists '([string-pattern] [regex-pattern])
     :tag Void} 
  (fn [pattern]
    (type pattern)))

(defmethod do-pattern-file-name-filter String
  ([^String pattern]
     (PatternFilenameFilter. pattern)))

(defmethod do-pattern-file-name-filter java.util.regex.Pattern
  ([^java.util.regex.Pattern pattern]
     (PatternFilenameFilter. pattern)))

(defn pattern-file-name-filter
  "Returns object filter file name only accepts files matching a regular expression.
   The returned object is thread-safe and immutable
   see more about FilenameFilter 
   on http://docs.oracle.com/javase/6/docs/api/java/io/FilenameFilter.html?is-external=true"
  {:added "0.1" :tag FilenameFilter}
  [pattern]
  (do-pattern-file-name-filter pattern))

(defn resource
  "Returen URL pointing to resourceName in supplied class context or classpath"
  {:added "0.1" :tag URL}
  ([contextClazz resourceName]
     (Resources/getResource contextClazz resourceName))
  ([resourceName]
     (Resources/getResource resourceName)))

(defn length
  "Returns the length of supplied input stream, in bytes"
  {:added "0.1"}
  ([^InputSupplier input-supplier]
     (ByteStreams/length input-supplier)))

(defn slice
  "Returns an InputSupplier that returns input streams
   from the an underlying supplier,
   where each stream starts at the given offset
   and is limited to the specified number of bytes."
  {:added "0.1" :tag InputSupplier}
  ([^InputSupplier input-supplier offset length]
     (ByteStreams/slice input-supplier offset length)))

(defn dev-null
  "Returns outputStream that simply discards written bytes, like unix /dev/null does"
  {:added "0.1" :tag OutputStream}
  []
  (NullOutputStream.))

(defn litte-endian-data-input-stream
  "Returns an implementation of DataInput that uses little-endian byte ordering 
   for reading short, int, float, double, and long values.
   Note: This class intentionally violates the specification of its supertype DataInput, 
          which explicitly requires big-endian byte order."
  {:added "0.1" :tag LittleEndianDataInputStream}          
  ([in] (LittleEndianDataInputStream. in)))

(defn litte-endian-data-output-stream
  "Returns an implementation of DataOutput that uses little-endian byte ordering 
   for writing char, short, int, float, double, and long values.
   Note: This class intentionally violates the specification of its supertype DataOutput, 
          which explicitly requires big-endian byte order."
  {:added "0.1" :tag LittleEndianDataOutputStream}
  ([out] (LittleEndianDataOutputStream. out)))

(defn limit-input-stream
  "Returns an InputStream that limits the number of bytes which can be read."
  {:added "0.1" :tag  LimitInputStream}
  [input-stream limit]
  (LimitInputStream. input-stream limit))

(defn file-backed-output-stream
  "Returns an OutputStream that starts buffering to a byte array, 
   but switches to file buffering once the data reaches a configurable size.
   This class is thread-safe."
  {:added "0.1" :tag FileBackedOutputStream}
  ([file-threshold]
     (FileBackedOutputStream. file-threshold))
  ([file-threshold rest-on-finalize]
     (FileBackedOutputStream. file-threshold rest-on-finalize)))

(defn counting-input-stream
  "Returns an InputStream that counts the number of bytes read"
  {:added "0.1" :tag CountingInputStream}
  ([in] (CountingInputStream. in)))

(defn counting-output-stream
  "Returns an OutpuStream taht counts the number of bytes writed"
  {:added "0.1" :tag CountingOutputStream}
  ([out] (CountingOutputStream. out)))

(defn join 
  "Joins multiple Reader/InputStream suppliers into a single supplier. 
   Reader/InputStream returned from the supplier will contain the concatenated data from the readers of the underlying suppliers.
   Reading from the joined reader/input-stream will throw a NullPointerException if any of the suppliers are null or return null."
  [type & suppliers]
  (condp = type
    :char (CharStreams/join suppliers)
    :byte (ByteStreams/join suppliers)
    (throw (IllegalArgumentException. 
            "Type must be supplied as :char or :byte"))))


