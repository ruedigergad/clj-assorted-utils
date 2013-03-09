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
  (:import (java.io ByteArrayOutputStream ObjectOutputStream)
           (java.util.concurrent Executors TimeUnit)
           (java.util.zip GZIPOutputStream ZipOutputStream)))


(defn sleep [ms]
  (Thread/sleep ms))

(defn exec [cmd]
  (-> (Runtime/getRuntime) (.exec cmd)))
(defn exec-blocking [cmd]
  (-> (exec cmd) (.waitFor)))

(defn exec-with-out [cmd stdout-fn]
  (let [proc (exec cmd)
        stdout-reader (reader (.getInputStream proc))
        stdout-thread (Thread. (fn []
                                 (try 
                                   (while (not (nil? (stdout-fn (.readLine stdout-reader))))))
                                 (.destroy proc)))]
    (.start stdout-thread)))


;;;
;;; Helper functions for handling system properties.
;;;
(defn get-system-property [p]
  "Get system property p. p is a String naming the property go get."
  (System/getProperty p))
(def get-arch (partial get-system-property "os.arch"))
(def get-os (partial get-system-property "os.name"))

(defn is-os? [os]
  (-> (get-os) (.toLowerCase) (.startsWith os)))



;;;
;;; Helper functions for handling files and directories accepting names as 
;;; Strings or URI (Essentially everything clojure.java.io/file can handle.).
;;; Well actually these had only been tested using Strings... o_X
;;;
(defn exists? [f]
  "Returns true if f exists in the filesystem."
  (.exists (file f)))

(defn is-file? [f]
  "Returns true if f is a file."
  (.isFile (file f)))

(defn file-exists? [f]
  "Returns true if f exists and is a file."
  (and 
    (exists? f)
    (is-file? f)))

(defn is-dir? [d]
  "Returns true if f is a directory."
  (.isDirectory (file d)))

(defn dir-exists? [d]
  "Returns true if f exists and is a directory."
  (and
    (exists? d)
    (is-dir? d)))

(defn mkdir [d]
  "Create directory d and if required all parent directories.
   This is equivalent to 'mkdir -p d' on Linux systems."
  (.mkdirs (file d)))

(defn rm [f]
  "Delete file f."
  (delete-file (file f)))

(defn rmdir [d]
  "Delete d if d is an empty directory."
  (if (is-dir? d) (rm d)))

(defn touch [f]
  "If f does not exist, create it."
  (.createNewFile (file f)))


;;;
;;; Delayed and repeated evaluation.
;;;
(defmacro delay-eval [d & body]
  "Evaluates the supplied body with the given delay 'd' ([ms])."
  `(doto (Thread. 
           #(do
              (Thread/sleep ~d)
              ~@body))
     (.start)))

(defn executor []
  "Create an executor for executing fns in an own thread."
  (Executors/newSingleThreadScheduledExecutor))

(defn shutdown [exec]
  "Shut executor down."
  (.shutdown exec))

(defn shutdown-now [exec]
  "Force executor shut down."
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
(defn prepare-flag []
  "Prepare a flag with default value false."
  (ref {:flag false}))

(defn set-flag [f]
  "Set flag to true."
  (dosync
    (alter f assoc :flag true)))

(defn flag-set? [f]
  "Test if flag had been set."
  (:flag @f))

(defn prepare-counter 
  "Prepare a simple counter. Use @ to access the value."
  ([] (prepare-counter 0))
  ([init] (ref init)))

(defn inc-counter [c]
  "Increment the given counter c."
  (dosync (alter c inc)))

(defn counter
  "Convenience function for creating a more powerful counter.
   The created counter allows to have arbitrary functions passed for manipulating the internal value.
   When the created counter is called with no argument the current value is returned."
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
(defn classname [o]
  "Get classname without leading package names of the given object o."
  (-> (type o) (str) (str/split #"\.") (last)))


(defn fn-name [f]
  "Get the name of the given fn f."
  (-> 
    (.getClass f) 
    (.getName) 
    (str/split #"\$") 
    (last) 
    (str/replace "_" "-")))



;;;
;;; Byte to Int
;;;
(defn byte-seq-to-int [byte-seq]
  "Convert the byte sequence byte-seq to an integer."
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
(defn get-int-from-byte-vector [v n] 
  "From given vector of bytes v get the integer starting at offset n.
   Starting at offset n the next 4 bytes will be converted to an integer."
  (byte-seq-to-int 
    (subvec v n (+ n 4))))

(defn int-to-byte-vector [val]
  "Convert the given integer val to a vector of 4 bytes."
  (loop [acc [] shift 0] 
    (if (= 8 shift)
      acc
      (recur 
        (conj acc (bit-and (bit-shift-right val (* 8 shift)) 0xff))
        (inc shift)))))

(defn vec-replace [v n delta]
  "In given vector v replace the content of v starting at index n with delta."
  (loop [i 0 acc v]
    (if (= i (count delta))
      acc
      (recur (inc i) (assoc acc (+ n i) (delta i))))))

(defn change-int-in-byte-vector [v n f]
  "In the given vector v, change the byte representation of an integer by applying function f."
  (let [int-val (get-int-from-byte-vector v n)
        new-int (f int-val)
        byte-vec (subvec (int-to-byte-vector new-int) 0 4)]
    (vec-replace v n byte-vec)))



;;;
;;; Functions for messing with XML data.
;;;
(defn xml-string-to-map [xml-str]
  "Takes an XML definition in form of a string and outputs the corresponding map."
  (with-open [xml-in (clojure.java.io/input-stream 
                       (.getBytes xml-str "UTF-8"))] 
    (clojure.xml/parse xml-in)))

(defn stringify-keyword [k]
  "If a keyword is passed returns the name of the keyword.
   Else the input is left unchanged."
  (if (keyword? k)
    (name k)
    k))

(defn stringify-map [m]
  "Convert _all_ keywords in a map to their respective names.
   This, essentially, is an extended version of clojure.walk/stringify-keys,
   which only converts the keys to strings."
  (let [map-fn (fn [[k v]] [(stringify-keyword k) (stringify-keyword v)])
        walk-fn (fn [m] 
                  (if (map? m)
                    (into {} (map map-fn m))
                    m))]
  (clojure.walk/postwalk walk-fn m)))

(defn xml-string-to-map-stringified [xml-str]
  "Convert the XML string xml-str to a map with all keywords converted to strings."
  (let [xml-map (xml-string-to-map xml-str)]
    (stringify-map xml-map)))


;;;
;;; Print to stderr.
;;;
(defn print-err [& s]
  "print to stderr."
  (binding [*out* *err*]
    (apply print s)))

(defn print-err-ln [& s]
  "println to stderr."
  (binding [*out* *err*]
    (apply println s)))




;;;
;;; Functions for serializing objects.
;;;
(defn object-to-byte-array [obj]
  (let [byte-out (ByteArrayOutputStream.)
        obj-out (ObjectOutputStream. byte-out)]
    (doto obj-out (.writeObject obj) .flush .close)
    (.toByteArray byte-out)))

(defn compress-object-to-byte-array 
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

