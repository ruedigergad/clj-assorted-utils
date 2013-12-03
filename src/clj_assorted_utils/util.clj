;;;
;;;   Copyright 2012, Ruediger Gad
;;;
;;;   This software is released under the terms of the Eclipse Public License 
;;;   (EPL) 1.0. You can find a copy of the EPL at: 
;;;   http://opensource.org/licenses/eclipse-1.0.php
;;;

(ns
  ^{:author "Ruediger Gad",
    :doc "Utility and helper functions"}
  clj-assorted-utils.util
  (:use clojure.java.io)
  (:use clojure.walk)
  (:use clojure.xml)
  (:require (clojure [string :as str]))
  (:import (java.io ByteArrayOutputStream ObjectOutputStream BufferedReader)
           (java.util.concurrent Executors TimeUnit)
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
  (-> (exec cmd) (.waitFor)))

(defn process-input-stream-line-by-line
  "Process the given input stream line-by-line.
   The data read from the stream is passed to the given function.
   The function is executed in an own thread."
  [input-stream function]
  (let [^BufferedReader rdr (reader input-stream)
        running (ref true)
        thread (Thread. (fn []
                          (try 
                            (while running
                              (let [line (.readLine rdr)]
                                (if (not (nil? line))
                                  (function line)
                                  (dosync (ref-set running false))))))))]
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
          stderr-thread (process-input-stream-line-by-line (.getErrorStream proc) stdout-fn)]
      proc)))

;;;
;;; Helper functions for handling system properties.
;;;
(defn get-system-property [p]
  "Get system property p. p is a String naming the property go get."
  (System/getProperty p))
(def get-arch (partial get-system-property "os.arch"))
(def get-os (partial get-system-property "os.name"))

(defn is-os? [^String os]
  (-> (get-os) (.toLowerCase) (.startsWith os)))



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
  (delete-file (file f)))

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
  (Executors/newSingleThreadScheduledExecutor))

(defn shutdown
  "Shut executor down."
  [exec]
  (.shutdown exec))

(defn shutdown-now
  "Force executor shut down."
  [exec]
  (.shutdownNow exec))

(defn run-once
  "Run f using executor exec once with delay d.
   Optionally a time unit tu can be given.
   tu defaults to TimeUnit/MILLISECONDS."
  ([exec f d]
   (run-once exec f d TimeUnit/MILLISECONDS))
  ([exec f d tu]
   (.schedule exec f d tu)))

(defn run-repeat
  "Run f repeatedly using executor exec with delay d.
   Optionally an initial delay id and a time unit tu can be given.
   Time unit is a static member of TimeUnit, e.g., TimeUnit/SECONDS 
   and defaults to TimeUnit/MILLISECONDS."
  ([exec f d]
   (run-repeat exec f 0 d))
  ([exec f id d]
   (run-repeat exec f id d TimeUnit/MILLISECONDS))
  ([exec f id d tu]
   (.scheduleAtFixedRate exec f id d tu)))



;;;
;;; Flags and a simple counter.
;;; I use these primarily for unit testing to check if something happened or not.
;;;
(defn prepare-flag
  "Prepare a flag with default value false."
  []
  (ref {:flag false}))

(defn set-flag
  "Set flag to true."
  [f]
  (dosync
    (alter f assoc :flag true)))

(defn flag-set?
  "Test if flag had been set.
   Returns true if flag was set and false otherwise."
  [f]
  (:flag @f))

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



;;;
;;; Convenience functions for getting class and fn names.
;;;
(defn classname
  "Get classname without leading package names of the given object o."
  [o]
  (-> (type o) (str) (str/split #"\.") (last)))


(defn fn-name
  "Get the name of the given fn f."
  [f]
  (-> 
    (.getClass f) 
    (.getName) 
    (str/split #"\$") 
    (last) 
    (str/replace "_" "-")))



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
  [f]
  (let [hook (Thread. f)]
    (-> (Runtime/getRuntime) (.addShutdownHook hook))))

