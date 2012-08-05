(ns ^{:doc "Clojure version for guava io"
      :author "lysu <sulifx@gmail.com>"}
  clj.guava.io
  (:import [com.google.common.io Files ByteStreams
            CharStreams InputSupplier LineProcessor
            OutputSupplier ByteArrayDataOutput
            PatternFilenameFilter  Resources]
           [com.google.common.base Charsets]
           [java.nio.charset Charset]
           [java.nio.channels ReadableByteChannel
            WritableByteChannel FileChannel$MapMode]
           [java.nio MappedByteBuffer]
           [java.io File InputStream OutputStream
            Reader FilenameFilter]
           [java.net URL]
           [java.util.zip Checksum]))

(def ^{:added 0.1 :doc "Charsets Holder"}
  CHARSETS {:iso-8859-1 Charsets/ISO_8859_1 
            :us-ascii Charsets/US_ASCII
            :utf-16 Charsets/UTF_16
            :utf-16be Charsets/UTF_16BE
            :utf-16le Charsets/UTF_16LE
            :utf-8 Charsets/UTF_8})

(defmulti ->input-supplier
  "
  Returns a InputSupplier
  from Byte Array/String/URL/File/Bytable InputSupplier
  "
  {:tag InputSupplier :added 0.1}
  (fn [value & args]
    (type value)))

(defmethod ->input-supplier (Class/forName "[B")
  ([in]
     (ByteStreams/newInputStreamSupplier in))
  ([in off len]
     (ByteStreams/newInputStreamSupplier in off len)))

(defmethod ->input-supplier String
  ([in]
     (CharStreams/newReaderSupplier in)))

(defmethod ->input-supplier InputSupplier
  ([in charset]
     (CharStreams/newReaderSupplier in charset)))

(defmethod ->input-supplier URL
  ([in]
     (Resources/newInputStreamSupplier in))
  ([in charset]
     (Resources/newReaderSupplier in charset)))

(defmethod ->input-supplier File
  ([f]
     (Files/newInputStreamSupplier f))
  ([f charset]
     (Files/newReaderSupplier f charset)))

(defmulti ->output-supplier
  "
   Returns OutputSupplier object
   write to other OutputSupplier or File
   when writes to file  will
    use CharStreams replace ByteStreams when Charsets is supplied 
    and use last append parameter to control append or not
  "
  {:tag OutputSupplier :added 0.1}
  (fn [value & args]
    (type value)))

(defmethod ->output-supplier OutputSupplier
  ([out charset]
     (CharStreams/newWriterSupplier out charset)))

(declare ->output-supplier-file-type)

(defmethod ->output-supplier File
  ([f]
     (Files/newOutputStreamSupplier f))
  ([f sec-param]
     (->output-supplier-file-type f sec-param))
  ([f sec-param thd-param]
     (->output-supplier-file-type f sec-param thd-param)))

(defmulti ->output-supplier-file-type
  (fn [f value & args]
    (type value)))

(defmethod ->output-supplier-file-type java.lang.Boolean
  ([f value]
     (Files/newOutputStreamSupplier f value)))

(defmethod ->output-supplier-file-type Charset
  ([f value]
     (Files/newWriterSupplier f value))
  ([f value append]
     (Files/newWriterSupplier f value append)))

(defmulti ->string
  "
   Reads all characters from a file/Readable/URL into a String, 
   using the given character set if supplied.
  "
  {:tag String :added 0.1}
  (fn [value & opt]
    (type value)))

(defmethod ->string Readable
  ([^Readable r]
     (CharStreams/toString r)))

(defmethod ->string InputSupplier
  ([^InputSupplier in]
     (CharStreams/toString in)))

(defmethod ->string File
  ([^File f ^Charset charset]
     (Files/toString f charset)))

(defmethod ->string URL
  ([^URL url ^Charset charset]
     (Resources/toString url charset)))

(defmulti ->byte-array
  "
   Reads all bytes from a file/InputStream/InputSupplier/URL/String 
   into a byte array. 
  "
  {:tag (Class/forName "[B") :added 0.1}
  (fn [value & opt]
    (type value)))

(defmethod ->byte-array File
  ([f]
     (Files/toByteArray f)))

(defmethod ->byte-array InputStream
  ([^InputStream in]
     (ByteStreams/toByteArray in)))

(defmethod ->byte-array InputSupplier
  ([^InputSupplier in]
     (ByteStreams/toByteArray in)))

(defmethod ->byte-array URL
  ([in]
     (Resources/toByteArray in)))

(defmethod ->byte-array String
  ([^String in ^Charset charset]
     (and in charset (.getBytes in charset))))

(defmulti copy!
  "copy from one source to other"
  {:tag Void :added 0.1}
  (fn [from to & args]
    [(type from) (type to)]))

;;!!!!!<? extends OutpuStream> vs <w extends Appendable & Closable>
(defmethod copy! [InputSupplier OutputSupplier]
  ([^InputSupplier in ^OutputSupplier out]
     (ByteStreams/copy in out)))

(defmethod copy! [InputSupplier OutputStream]
  ([^InputSupplier in ^OutputStream out]
     (ByteStreams/copy in out)))

(defmethod copy! [InputStream OutputSupplier]
  ([^InputStream in ^OutputSupplier out]
     (ByteStreams/copy in out)))

(defmethod copy! [InputStream OutputStream]
  ([^InputStream in ^OutputStream out]
     (ByteStreams/copy in out)))

(defmethod copy! [ReadableByteChannel WritableByteChannel]
  ([^ReadableByteChannel in ^WritableByteChannel out]
     (ByteStreams/copy in out)))

(defmethod copy! [Readable Appendable]
  ([^Readable in ^Appendable out]
     (CharStreams/copy in out)))

(defmethod copy! [InputSupplier Appendable]
  ([^InputSupplier in ^Appendable append]
     (CharStreams/copy in append)))

(defmethod copy! [InputSupplier File]
  ([^InputSupplier in ^File f]
     (Files/copy in f))
  ([^InputSupplier in ^File f ^Charset charset]
     (Files/copy in f charset)))

(defmethod copy! [File OutputSupplier]
  ([^File from ^OutputSupplier to]
     (Files/copy from to))
  ([^File from ^OutputSupplier to ^Charset charset]
     (Files/copy from charset to)))

(defmethod copy! [File OutputStream]
  ([^File from  ^OutputStream to]
     (Files/copy from to)))

(defmethod copy! [File Appendable]
  ([^File from ^Appendable to ^Charset charset]
     (Files/copy from charset to)))

(defmethod copy! [URL OutputStream]
  ([^URL from ^OutputStream to]
     (Resources/copy from to)))

(defmethod copy! [File File]
  ([^File from ^File to]
     (Files/copy from to)))

(defmulti read-first-line
  "Reads first line from file/reader
   The line doesn't contain line-termination characters"
  (fn [file & args]
    (type file)))

(defmethod read-first-line File
  ([file charset]
     (Files/readFirstLine file charset)))

(defmethod read-first-line InputSupplier
  ([in]
     (CharStreams/readFirstLine in)))

(defmulti skip-fully!
  "Discards n characters/bytes of data from reader/inputStream"
  {:added 0.1}
  (fn [src n]
    (type src)))

(defmethod skip-fully! InputStream
  ([in n]
     (ByteStreams/skipFully in n)))

(defmethod skip-fully! Reader
  ([r n]
     (CharStreams/skipFully r n)))

(defn append!
  "Appends a character sequence to a file using the given character set"
  ([^CharSequence from ^File to ^Charset charset]
     (Files/append from to charset)))

(defmulti equal
  "Returns true if inputs/files contain same bytes"
  {:added 0.1 :tag Boolean}
  (fn [v1 v2]
    [(type v1) (type v2)]))

(defmethod equal [File File]
  ([file1 file2]
     (Files/equal file1 file2)))

(defmethod equal [InputSupplier InputSupplier]
  ([in1 in2]
     (ByteStreams/equal in1 in2)))

(def ^{:added 0.1 :doc "Checksum Holder"}
  CHECKSUM {:crc32 java.util.zip.CRC32
            :adler32 java.util.zip.Adler32})

(defmulti checksum
  "Computes and returns the checksum for file or input
   The checksum object is reset when this method returns success"
  {:tag long :added 0.1}
  (fn [src checksum]
    (type src)))

(defmethod checksum File
  ([file checksum]
     (Files/getChecksum file checksum)))

(defmethod checksum InputSupplier
  ([in checksum]
     (ByteStreams/getChecksum in checksum)))

(def ^{:added 0.1 :doc "MapMode Holder"}
  MAP-MODE {:private (FileChannel$MapMode/PRIVATE)
            :read-only (FileChannel$MapMode/READ_ONLY)
            :read-write (FileChannel$MapMode/READ_WRITE)})

(defn file-map
  "Maps file into memory"
  {:tag MappedByteBuffer :added 0.1}
  ([^File file]
     (Files/map file))
  ([^File file ^FileChannel$MapMode  mode]
     (Files/map file mode))
  ([^File file ^FileChannel$MapMode mode size]
     (Files/map file mode size)))

(defn create-parent-dirs!
  "Creates any necessary but nonexistent parent directories of specified file.
   Note that if this operation fails it may have succeeded in some (but not all)
   of necessary parent directories"
  ([^File file]
     (Files/createParentDirs file)))

(defn create-temp-dir!
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
  {:added 0.1 :tag File}
  ([]
     (Files/createTempDir)))

(defn touch!
  "Create an empty file or update the last update timestamp
   on the same as the unix command of the same name"
  {:added 0.1}
  ([^File file]
     (Files/touch file)))

(defn move!
  "Moves file from one path to another"
  [^File from ^File to]
  (Files/move from to))

(defn file-extension
  "Returns the file extension for give file name
   or the empty string if the file has no extension.
   the result does not include the '.'"
  {:tag String :added 0.1}
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
  {:tag String :added 0.1}
  [path-name]
  (Files/simplifyPath path-name))

(defmulti read-lines
  "Reads line from file/InputSupplier/url"
  {:added 0.1}
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

(defmulti write!
  "write byte-array/CharSequence to File/OutputSupplier"
  {:added 0.1}
  (fn [from to & args]
    [(type from) (type to)]))

(defmethod write! [(Class/forName "[B") File]
  ([from to]
     (Files/write from to)))

(defmethod write! [CharSequence File]
  ([from to charset]
     (Files/write from to charset)))

;;!!!!!<? extends OutpuStream> vs <w extends Appendable & Closable>
(defmethod write! [CharSequence OutputSupplier]
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

(defmulti pattern-file-name-filter
  "Returns object filter file name only accepts files matching a regular expression.
   The returned object is thread-safe and immutable "
  {:added 0.1 :tag FilenameFilter}
  (fn [pattern]
    (type pattern)))

(defmethod pattern-file-name-filter String
  ([^String pattern]
     (PatternFilenameFilter. pattern)))

(defmethod pattern-file-name-filter java.util.regex.Pattern
  ([^java.util.regex.Pattern pattern]
     (PatternFilenameFilter. pattern)))

(defmulti resource
  "Returen URL pointing to resourceName in supplied class context or classpath"
  (fn [param-1 & opt]
    (type param-1)))

(defmethod resource Class
  ([contextClass resourceName]
     (Resources/getResource contextClass resourceName)))

(defmethod resource String
  ([resourceName]
     (Resources/getResource resourceName)))

;;TODO(chrisn): Not all streams support skipping by guava.
(defn length
  "Returns the length of supplied input stream, in bytes"
  {:added 0.1}
  ([^InputSupplier input-supplier]
     (ByteStreams/length input-supplier)))

(defn slice
  "Returns an InputSupplier that returns input streams
   from the an underlying supplier,
   where each stream starts at the given offset
   and is limited to the specified number of bytes."
  {:tag InputSupplier :added 0.1}
  ([^InputSupplier input-supplier offset length]
     (ByteStreams/slice input-supplier offset length)))

;;TODO...
                                        ; (defmulti join)


