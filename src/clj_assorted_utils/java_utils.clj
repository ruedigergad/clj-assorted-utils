;;;
;;;   Copyright 2012-2021 Ruediger Gad
;;;
;;;   This software is released under the terms of the Eclipse Public License 
;;;   (EPL) 1.0. You can find a copy of the EPL at: 
;;;   http://opensource.org/licenses/eclipse-1.0.php
;;;

(ns
  ^{:author "Ruediger Gad",
    :doc "Utility and helper functions for Java"}
  clj-assorted-utils.java-utils
  (:require
    (clj-assorted-utils [util :as util])))

(defn no-eval-read-string
  [input]
  (binding [*read-eval* false] (read-string input)))

(defn read-object-from-clojure-string
  "Tries to read data from a string that contains data expressed in Clojure data structures.
   Note that *read-eval* is set to false for reading the string in order to avoid unintended code execution."
  [input]
  (let [read-input (no-eval-read-string input)]
    (util/convert-from-clojure-to-java read-input)))

(defn read-list-from-clojure-string
  "Tries to read data from a string that contains data expressed in Clojure data structures.
   This method checks for List type of data for the top-level object and returns nil/null otherwise.
   Note that *read-eval* is set to false for reading the string in order to avoid unintended code execution."
  [input]
  (let [read-input (no-eval-read-string input)]
    (when (or (vector? read-input) (list? read-input))
      (util/convert-from-clojure-to-java read-input))))

(defn read-map-from-clojure-string
  "Tries to read data from a string that contains data expressed in Clojure data structures.
   This method checks for Map type of data for the top-level object and returns nil/null otherwise.
   Note that *read-eval* is set to false for reading the string in order to avoid unintended code execution."
  [input]
  (let [read-input (no-eval-read-string input)]
    (when (map? read-input)
      (util/convert-from-clojure-to-java read-input))))

(defn read-set-from-clojure-string
  "Tries to read data from a string that contains data expressed in Clojure data structures.
   This method checks for Set type of data for the top-level object and returns nil/null otherwise.
   Note that *read-eval* is set to false for reading the string in order to avoid unintended code execution."
  [input]
  (let [read-input (no-eval-read-string input)]
    (when (set? read-input)
      (util/convert-from-clojure-to-java read-input))))
