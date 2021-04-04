;;;
;;;   Copyright 2012, 2013, 2014, Ruediger Gad
;;;
;;;   This software is released under the terms of the Eclipse Public License 
;;;   (EPL) 1.0. You can find a copy of the EPL at: 
;;;   http://opensource.org/licenses/eclipse-1.0.php
;;;

(ns
  ^{:author "Ruediger Gad",
    :doc "Utility and helper functions"}
  clj-assorted-utils.util
  (:require
    (clojure
      [string :as str]
      [walk :refer :all]
      [xml :refer :all])
    (clojure.java [io :refer :all]))
  (:import
    (java.io BufferedReader BufferedWriter ByteArrayOutputStream IOException ObjectOutputStream)
    (java.util ArrayList HashMap HashSet List Map)
    (java.util.concurrent CountDownLatch Executors ThreadFactory TimeUnit)
    (java.util.zip GZIPOutputStream ZipOutputStream)))


(defn sleep
  "Sleep for the given number of milliseconds."
  [ms]
  (Thread/sleep ms))

(defn exec
  "Execute the given command."
  [cmd]
  (-> (Runtime/getRuntime) (.exec cmd)))

(defn exec-blocking
  "Execute the given command and block until execution has finished."
  [cmd]
  (-> ^java.lang.Process (exec cmd) (.waitFor)))

(defn process-input-stream-line-by-line
  "Process the given input stream line-by-line.
   The data read from the stream is passed to the given function.
   The function is executed in an own thread."
  [input-stream function]
  (let [^BufferedReader rdr (reader input-stream)
        running (ref true)
        thread (Thread. (fn []
                          (try 
                            (while @running
                              (let [line (.readLine rdr)]
                                (if (not (nil? line))
                                  (function line)
                                  (dosync (ref-set running false)))))
                            (catch Exception e
                              (println "Cought exception:" e)))))]
    (.start thread)
    thread))

(defn exec-with-out
  "Execute the given command and process the output written to stdout with stdout-fn.
   stdout-fn is run in an own thread.
   The data written to stdout is passed to stdout-fn line-by-line.
   When a value of nil is read processing stops and the process in which cmd was executed is destroyed."
  ([cmd stdout-fn]
    (let [^Process proc (exec cmd)
          stdout-thread (process-input-stream-line-by-line (.getInputStream proc) stdout-fn)]
      proc))
  ([cmd stdout-fn stderr-fn]
    (let [^Process proc (exec cmd)
          stdout-thread (process-input-stream-line-by-line (.getInputStream proc) stdout-fn)
          stderr-thread (process-input-stream-line-by-line (.getErrorStream proc) stderr-fn)]
      proc)))



;;;
;;; Helper functions for handling system properties.
;;;
(defn get-system-property
  "Get system property p. p is a String naming the property go get."
  [p]
  (System/getProperty p))
(def get-arch (partial get-system-property "os.arch"))
(def get-os (partial get-system-property "os.name"))

(defn is-os? [^String os]
  (-> ^java.lang.String (get-os) (.toLowerCase) (.startsWith os)))



;;;
;;; Helper functions for handling files and directories accepting names as 
;;; Strings or URI (Essentially everything clojure.java.io/file can handle.).
;;; Well actually these had only been tested using Strings... o_X
;;;
(defn exists?
  "Returns true if f exists in the filesystem."
  [f]
  (.exists (file f)))

(defn is-file?
  "Returns true if f is a file."
  [f]
  (.isFile (file f)))

(defn file-exists?
  "Returns true if f exists and is a file."
  [f]
  (and 
    (exists? f)
    (is-file? f)))

(defn is-dir?
  "Returns true if f is a directory."
  [d]
  (.isDirectory (file d)))

(defn dir-exists?
  "Returns true if f exists and is a directory."
  [d]
  (and
    (exists? d)
    (is-dir? d)))

(defn mkdir
  "Create directory d and if required all parent directories.
   This is equivalent to 'mkdir -p d' on Linux systems."
  [d]
  (.mkdirs (file d)))

(defn rm
  "Delete file f."
  [f]
  (if (exists? f)
    (delete-file (file f))))

(defn rmdir
  "Delete d if d is an empty directory."
  [d]
  (if (is-dir? d) (rm d)))

(defn touch
  "If f does not exist, create it."
  [f]
  (.createNewFile (file f)))



;;;
;;; Delayed and repeated evaluation.
;;;
(defmacro delay-eval
  "Evaluates the supplied body with the given delay 'd' ([ms])."
  [d & body]
  `(doto (Thread. 
           #(do
              (Thread/sleep ~d)
              ~@body))
     (.start)))

(defn executor
  "Create an executor for executing fns in an own thread."
  []
  (Executors/newSingleThreadScheduledExecutor (proxy [ThreadFactory] []
                                                (newThread [^java.lang.Runnable r] (doto (Thread. r) (.setDaemon true))))))

(defn shutdown
  "Shut executor down."
  [^java.util.concurrent.ExecutorService exec]
  (.shutdown exec))

(defn shutdown-now
  "Force executor shut down."
  [^java.util.concurrent.ExecutorService exec]
  (.shutdownNow exec))

(defn run-once
  "Run f using executor exec once with delay d.
   Optionally a time unit tu can be given.
   tu defaults to TimeUnit/MILLISECONDS."
  ([exec f d]
   (run-once exec f d TimeUnit/MILLISECONDS))
  ([^java.util.concurrent.ScheduledExecutorService exec ^java.lang.Runnable f ^long d ^TimeUnit tu]
   (.schedule exec f d tu)))

(defn run-repeat
  "Run f repeatedly using executor exec with delay d.
   Optionally an initial delay, id, and a time unit tu can be given.
   Time unit is a static member of TimeUnit, e.g., TimeUnit/SECONDS 
   and defaults to TimeUnit/MILLISECONDS."
  ([exec f d]
   (run-repeat exec f 0 d))
  ([exec f id d]
   (run-repeat exec f id d TimeUnit/MILLISECONDS))
  ([^java.util.concurrent.ScheduledExecutorService exec ^java.lang.Runnable f ^java.lang.Long id ^java.lang.Long d ^TimeUnit tu]
   (.scheduleAtFixedRate exec f id d tu)))



;;;
;;; Flags and a simple counter.
;;; I use these primarily for unit testing to check if something happened or not.
;;;
(defn prepare-counter 
  "Prepare a simple counter. Use @ to access the value."
  ([] (prepare-counter 0))
  ([init] (ref init)))

(defn inc-counter
  "Increment the given simple counter c."
  [c]
  (dosync (alter c inc)))

(defn counter
  "Convenience function for creating a more powerful counter.
   The created counter allows to have arbitrary single-argument functions passed for manipulating the internal value.
   When the created counter is called with no argument the current value is returned.
   Example:
   (use 'clj-assorted-utils.util)
   (let [cntr (counter 123)]
     (println (cntr))
     (cntr inc)
     (cntr dec)
     (cntr #(- % 123))
     (cntr))"
  ([] (counter 0))
  ([init] 
   (let [cntr (ref init)]
     (fn 
       ([] @cntr)
       ([op]
         (cond
           (fn? op) (dosync (alter cntr op))
           :default (println "No function passed:" op)))))))

(defn delta-counter
  "Creates a counter for calculating deltas.
   The returned delta counter function expects two arguments, a keyword and a numerical value.
   When called for the first time the initial value will be associated to an internal counter and zero is returned.
   When called subsequently with the same keyword, the difference between the current value
   and the stored value is calculated and this delta is returned.
   The internal counter is then set to the new value.
   Example:
   (let [cntr (counter)
         delta-cntr (delta-counter)]
     (= 0 (delta-cntr :cntr (cntr)))
     (cntr inc)
     (cntr inc)
     (cntr inc)
     (= 3 (delta-cntr :cntr (cntr)))
     (= 0 (delta-cntr :cntr (cntr))))"
  []
  (let [counters (ref {})]
    (fn [k v]
      (let [cntr (@counters k)]
        (if cntr
          (let [delta (- v (cntr))]
            (cntr (fn [_] v))
            delta)
          (let [new-cntr (counter)]
            (dosync (alter counters assoc k new-cntr))
            (new-cntr)))))))

(defprotocol Flag
  (set-flag
    [this]
    "Set flag to true.")
  (flag-set?
    [this]
    "Test if flag had been set.
     Returns true if flag was set and false otherwise.")
  (await-flag
    [this]
    "Block the current thread until the flag was set."))

(defrecord CountDownFlag [cntr ^CountDownLatch cdl]
  Flag
    (set-flag [this]
      (if (not= (cntr) 0)
        (cntr dec))
      (if (.flag-set? this)
        (.countDown cdl)))
    (flag-set? [_] (= 0 (cntr)))
    (await-flag [_] (.await cdl)))

(defn prepare-flag
  "Prepare a flag with default value false."
  ([]
   (prepare-flag 1))
  ([n]
   (->CountDownFlag
     (counter n)
     (CountDownLatch. 1))))



;;;
;;; Convenience functions for getting class and fn names.
;;;
(defn classname
  "Get classname without leading package names of the given object o."
  [o]
  (-> (type o) (str) (str/split #"\.") (last)))


(defn fn-name
  "Get the name of the given fn f."
  [^clojure.lang.IFn f]
  (-> 
    (.getClass f) 
    (.getName) 
    (str/split #"\$") 
    (last) 
    (str/replace "_" "-")))



;;;
;;; Functions for getting information about function arguments.
;;;
(defn get-defn-arglists
  "Get the arglists of a function that was defined with defn.
   This function is intended to be used from within a macor.
   For a macro version of this functionality please use get-defn-arglists-m."
  [f]
  (-> f meta :arglists vec))

(defmacro get-defn-arglists-m
  "Macro version of get-defn-arglists."
  [f]
  `(get-defn-arglists (var ~f)))

(defn map-quote-vec
  [c]
  (into [] (map (fn [x] `(quote ~x)) c)))

(defn get-fn-arglists
  "Returns a map that contains the actual fn that was passed as argument and its arglists.
   The result will be a map of form: {:fn f :args <f-args>}.
   This function is intended to be used from within a macor.
   For a macro version of this functionality please use get-fn-arglists-m."
  [f]
  (let [m {:fn f
           :args (if (vector? (nth f 1))
                   [(map-quote-vec (nth f 1))]
                   (reduce (fn [v e] (conj v (map-quote-vec (first e)))) [] (rest f)))}]
    m))

(defmacro get-fn-arglists-m
  "Macro version of get-fn-arglists."
  [f]
  (get-fn-arglists f))



;;;
;;; Byte to Int
;;;
(defn byte-seq-to-int
  "Convert the byte sequence byte-seq to an integer."
  [byte-seq]
  (loop [s byte-seq acc 0 shift 0]
    (if (empty? s)
      acc
      (recur (rest s) 
             (+ acc 
                (bit-shift-left 
                  (first s) 
                  (* 8 shift))) 
             (inc shift)))))



;;;
;;; Functions for messing with byte vectors.
;;;
(defn get-int-from-byte-vector
  "From given vector of bytes v get the integer starting at offset n.
   Starting at offset n the next 4 bytes will be converted to an integer."
  [v n]
  (byte-seq-to-int 
    (subvec v n (+ n 4))))

(defn int-to-byte-vector
  "Convert the given integer val to a vector of 4 bytes."
  [val]
  (loop [acc [] shift 0] 
    (if (= 8 shift)
      acc
      (recur 
        (conj acc (bit-and (bit-shift-right val (* 8 shift)) 0xff))
        (inc shift)))))

(defn vec-replace
  "In given vector v replace the content of v starting at index n with delta."
  [v n delta]
  (loop [i 0 acc v]
    (if (= i (count delta))
      acc
      (recur (inc i) (assoc acc (+ n i) (delta i))))))

(defn change-int-in-byte-vector
  "In the given vector v, change the byte representation of an integer by applying function f."
  [v n f]
  (let [int-val (get-int-from-byte-vector v n)
        new-int (f int-val)
        byte-vec (subvec (int-to-byte-vector new-int) 0 4)]
    (vec-replace v n byte-vec)))



;;;
;;; Functions for messing with XML data.
;;;
(defn xml-string-to-map
  "Takes an XML definition in form of a string and outputs the corresponding map."
  [^String xml-str]
  (with-open [xml-in (clojure.java.io/input-stream 
                       (.getBytes xml-str "UTF-8"))] 
    (clojure.xml/parse xml-in)))

(defn stringify-keyword
  "If a keyword is passed returns the name of the keyword.
   Else the input is left unchanged."
  [k]
  (if (keyword? k)
    (name k)
    k))

(defn stringify-map
  "Convert _all_ keywords in a map to their respective names.
   This, essentially, is an extended version of clojure.walk/stringify-keys,
   which only converts the keys to strings."
  [m]
  (let [map-fn (fn [[k v]] [(stringify-keyword k) (stringify-keyword v)])
        walk-fn (fn [m] 
                  (if (map? m)
                    (into {} (map map-fn m))
                    m))]
  (clojure.walk/postwalk walk-fn m)))

(defn xml-string-to-map-stringified
  "Convert the XML string xml-str to a map with all keywords converted to strings."
  [xml-str]
  (let [xml-map (xml-string-to-map xml-str)]
    (stringify-map xml-map)))



;;;
;;; Print to stderr.
;;;
(defn print-err
  "print to stderr."
  [& s]
  (binding [*out* *err*]
    (apply print s)))

(defn println-err
  "println to stderr."
  [& s]
  (binding [*out* *err*]
    (apply println s)))

(defn print-err-ln
  "Deprecated! Use println-err instead."
  [& s]
  (apply println-err s))

(defmacro with-err-str
  "Analog to with-out-str, just for *err*: https://clojuredocs.org/clojure.core/with-out-str"
  [& body]
  `(let [s# (new java.io.StringWriter)]
     (binding [*err* s#]
       ~@body
       (str s#))))



;;;
;;; Customizable with-out-str that allows to execute a function on each added string.
;;;

(defmacro with-out-str-custom
  "Customizable version of with-out-str: https://clojuredocs.org/clojure.core/with-out-str
   This version executes the function write-fn for every element that is added to the writer.
   The element that is added to the writer is the string representation of the return value of write-fn."
  [write-fn & body]
  `(let [wrtr# (proxy [java.io.StringWriter] []
                 (write [^String s#]
                   (proxy-super write (str (~write-fn s#)))))]
     (binding [*out* wrtr#]
       ~@body
       (str wrtr#))))

(defmacro with-err-str-custom
  "Customizable version of with-err-str.
   This version executes the function write-fn for every element that is added to the writer.
   The element that is added to the writer is the string representation of the return value of write-fn."
  [write-fn & body]
  `(let [wrtr# (proxy [java.io.StringWriter] []
                 (write [^String s#]
                   (proxy-super write (str (~write-fn s#)))))]
     (binding [*err* wrtr#]
       ~@body
       (str wrtr#))))

(defmacro with-out-str-cb
  "Callback version of with-out-str: https://clojuredocs.org/clojure.core/with-out-str
   This version executes the function cb-fn for every element that was added to the writer.
   cb-fn is executed after the element was added to the writer."
  [cb-fn & body]
  `(let [wrtr# (proxy [java.io.StringWriter] []
                 (write [arg#]
                   (let [~'^java.io.StringWriter this ~'this]
                     (condp = (type arg#)
                       String (proxy-super write ^String arg#)
                       Integer (proxy-super write ^Integer arg#)))
                   (~cb-fn arg#)))]
     (binding [*out* wrtr#]
       ~@body
       (str wrtr#))))

(defmacro with-err-str-cb
  "Callback version of with-err-str.
   This version executes the function cb-fn for every element that was added to the writer.
   cb-fn is executed after the element was added to the writer."
  [cb-fn & body]
  `(let [wrtr# (proxy [java.io.StringWriter] []
                 (write
                   ([arg#]
                     (let [~'^java.io.StringWriter this ~'this]
                       (condp = (type arg#)
                         String (proxy-super write ^String arg#)
                         Integer (proxy-super write ^Integer arg#)))
                     (~cb-fn arg#))
                   ([^String s# ^Integer off# ^Integer len#]
                     (let [~'^java.io.StringWriter this ~'this]
                       (proxy-super write s# off# len#))
                     (~cb-fn s#))))]
     (binding [*err* wrtr#]
       ~@body
       (str wrtr#))))

(defmacro with-eo-str
  "Macro that executes body capturing its stdout and stderr output.
   This returns a map with the following structure:
   {:all both-stdout-and-std-err-output-in-proper-order
    :stderr stderr-output
    :stdout stdout-output}"
  [& body]
  `(let [all-str# (ref "")
         out-str# (atom "")
         ret# (atom nil)
         err-wrtr# (writer System/err)
         out-wrtr# (writer System/out)
         err-str# (with-err-str-cb
                    (fn [e-str#]
                      (binding [*out* err-wrtr#]
                        (println e-str#))
                      (dosync
                        (alter all-str# str e-str#)))
                    (let [out-str-tmp# (with-out-str-cb
                                         (fn [o-str#]
                                           (binding [*out* out-wrtr#]
                                             (println o-str#))
                                           (dosync
                                             (alter all-str# str o-str#)))
                                         (let [ret-tmp# (do ~@body)]
                                           (reset! ret# ret-tmp#)))]
                      (reset! out-str# out-str-tmp#)))]
     {:all @all-str#
      :stderr err-str#
      :stdout @out-str#
      :ret @ret#}))



;;;
;;; Functions for serializing objects.
;;;
(defn object-to-byte-array
  "Write the given object into a byte array.
   A newly allocated byte arry with the serialized object is returned."
  [obj]
  (let [byte-out (ByteArrayOutputStream.)
        obj-out (ObjectOutputStream. byte-out)]
    (doto obj-out (.writeObject obj) .flush .close)
    (.toByteArray byte-out)))

(defn compress-object-to-byte-array 
  "Write an object into a byte array and compress it.
   A newly allocated byte array with the serialized and compressed object is returned.
   The compression algorithm can be specified via the second parameter.
   Currently GZIP (:gzip) and ZIP (:zip) are supported.
   The default is GZIP."
  ([obj]
    (compress-object-to-byte-array obj :gzip))
  ([obj alg]
    (let [byte-out (ByteArrayOutputStream.)
          compress-out (cond
                         (= :zip alg) (ZipOutputStream. byte-out)
                         :default (GZIPOutputStream. byte-out))
          obj-out (ObjectOutputStream. compress-out)]
      (doto obj-out (.writeObject obj) .flush .close)
      (.toByteArray byte-out))))



;;;
;;; Functions for processing input.
;;;
(defn process-line-by-line
  "Process input from location line-by-line.
   Each line is passed to f.
   The processing is done with a clojure reader.
   So all locations supported by reader are automatically supported as well."
  [location f]
  (with-open [rdr (clojure.java.io/reader location)]
    (doseq [line (line-seq rdr)]
      (f line))))



;;;
;;; Shutdown hook convenience
;;;
(defn add-shutdown-hook
  "Adds a shutdown hook that will be executed when the JVM is shutdown.
   The supplied function f will be called when the JVM shuts down.
   Please note: when using multiple hooks, there is no guarantee with
   respect to the order in which the hooks will be executed."
  [^java.lang.Runnable f]
  (let [hook (Thread. f)]
    (-> (Runtime/getRuntime) (.addShutdownHook hook))))



;;;
;;; Primitive data types array types
;;;
(def boolean-array-type
  (type (boolean-array 0)))

(def byte-array-type
  (type (byte-array 0)))

(def short-array-type
  (type (short-array 0)))

(def char-array-type
  (type (char-array 0)))

(def int-array-type
  (type (int-array 0)))

(def long-array-type
  (type (long-array 0)))

(def float-array-type
  (type (float-array 0)))

(def double-array-type
  (type (double-array 0)))



;;;
;;; Helpers for converting Clojure specific data structures to their "pure" Java equivalents.
;;;
(defn convert-from-clojure-to-java
  "Converts the given Clojure specific data structure (list, map, set, vector) into the equivalent \"pure\" Java data structure.
   The mapping is as follows: list and vector -> ArrayList, map -> HashMap, set -> HashSet.
   Nested data structures will be converted recursively."
  [input]
  (cond
    (sequential? input) (let [out (ArrayList.)]
                          (doseq [in-element input]
                            (if (coll? in-element)
                              (.add out (convert-from-clojure-to-java in-element))
                              (.add out in-element)))
                          out)
    (map? input) (let [out (HashMap.)]
                   (doseq [in-element input]
                     (if (coll? (val in-element))
                       (.put out (key in-element) (convert-from-clojure-to-java (val in-element)))
                       (.put out (key in-element) (val in-element))))
                   out)
    (set? input) (let [out (HashSet.)]
                   (doseq [in-element input]
                     (if (coll? in-element)
                       (.add out (convert-from-clojure-to-java in-element))
                       (.add out in-element)))
                   out)))



;;;
;;; Accumulate selected map entries.
;;;
(defn reduce-selected-map-entries
  "Reduce the values of all selected keys of a map m by applying the function f."
  [m f selected-keys]
  (reduce f (vals (select-keys m selected-keys))))



;;;
;;; Retrieve values from a Map or default value if key does not exist.
;;;
(defn get-with-default
  "Get a value for the given key k from a map m and if the value does not exist return the default d."
  [^Map m k d]
  (if (.containsKey m k)
    (.get m k)
    d))



;;;
;;; Convenience functionality for writing strings to a file or fifo.
;;;
(defn create-string-to-file-output
  ([out-file] (create-string-to-file-output out-file {}))
  ([out-file arg-map]
    (let [insert-newline (get-with-default arg-map :insert-newline false)
          resume-broken-pipe (get-with-default arg-map :resume-broken-pipe false)
          catch-exceptions (get-with-default arg-map :catch-exceptions false)
          hdr (get-with-default arg-map :header "")
          append (get-with-default arg-map :append false)
          await-open (get-with-default arg-map :await-open true)
          wrtr (atom nil)
          wrtr-opened (prepare-flag)
          open-wrtr-fn (fn []
                         (reset! wrtr nil)
                         (doto (Thread.
                                 #(do
                                    (reset! wrtr (writer out-file :append append))
                                    (set-flag wrtr-opened)
                                    (.write ^java.io.Writer @wrtr ^java.lang.String hdr)))
                           (.setDaemon true)
                           (.start)))
          _ (open-wrtr-fn)
          _ (if await-open
              (await-flag wrtr-opened))
          closed (atom false)
          close-fn (fn []
                     (reset! closed true)
                     (if @wrtr
                       (doto (Thread. #(try
                                         (.close ^BufferedWriter @wrtr)
                                         (catch Exception e
                                           (if catch-exceptions
                                             (println e)
                                             (throw e)))))
                         (.setDaemon true)
                         (.start))))
          handle-exception-fn (if resume-broken-pipe
                                #(if (and
                                       (= IOException (type %))
                                       (= "Broken pipe" (.getMessage ^IOException %))
                                       (not @closed))
                                   (do
                                     (println "Pipe broke. Re-opening writer...")
                                     (open-wrtr-fn))
                                   (if catch-exceptions
                                     (println %)
                                     (throw %)))
                                #(if catch-exceptions
                                   (println %)
                                   (throw %)))]
      (fn
        ([] (close-fn))
        ([data]
          (let [^BufferedWriter w @wrtr]
            (if (and (not (nil? w)) (not @closed))
              (try
                (condp #(instance? %1 %2) data
                  String (do
                           (.write w ^String data)
                           (if insert-newline
                             (.newLine w)))
                  List   (loop [it (.iterator ^List data)]
                           (.write w ^String (.next it))
                           (if insert-newline
                             (.newLine w))
                           (if (.hasNext it)
                             (recur it)))
                  (do
                    (.write w (str data))
                    (if insert-newline
                      (.newLine w))))
                (.flush w)
                (catch Exception e
                  (handle-exception-fn e))))))))))

(defn mkfifo
  [f]
  (exec-blocking (str "mkfifo " f)))

(defn create-threaded-lineseq-reader
  [in-file process-fn]
  (let [running (atom true)
        ^Thread t (doto (Thread. #(while @running (process-line-by-line in-file process-fn)))
                    (.setDaemon true)
                    (.start))]
    (fn []
      (reset! running false)
      (if (.isAlive t)
        (.interrupt t)))))

