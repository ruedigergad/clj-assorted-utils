;;;
;;;   Copyright 2014, Ruediger Gad
;;;
;;;   This software is released under the terms of the Eclipse Public License 
;;;   (EPL) 1.0. You can find a copy of the EPL at: 
;;;   http://opensource.org/licenses/eclipse-1.0.php
;;;

(ns
  ^{:author "Ruediger Gad",
    :doc "Java utility and helper functions"}
  clj-assorted-utils.JavaUtils
  (:use clojure.test
        clojure.test.junit
        clojure.java.io
        clj-assorted-utils.util)
  (:import (java.util ArrayList HashMap HashSet List Map Set))
  (:gen-class
    :methods [#^{:static true} [readObjectFromClojureString [String] Object]
              #^{:static true} [readListFromClojureString [String] java.util.List]
              #^{:static true} [readMapFromClojureString [String] java.util.Map]
              #^{:static true} [readSetFromClojureString [String] java.util.Set]]))

(defn -readObjectFromClojureString
  "Tries to read data from a string that contains data expressed in Clojure data structures."
  [input]
  (let [read-input (read-string input)]
    (convert-from-clojure-to-java read-input)))

(defn -readListFromClojureString
  "Tries to read data from a string that contains data expressed in Clojure data structures.
   This method checks for List type of data for the top-level object and returns nil/null otherwise."
  [input]
  (let [read-input (read-string input)]
    (if (or (vector? read-input) (list? read-input))
      (convert-from-clojure-to-java read-input))))

(defn -readMapFromClojureString
  "Tries to read data from a string that contains data expressed in Clojure data structures.
   This method checks for Map type of data for the top-level object and returns nil/null otherwise."
  [input]
  (let [read-input (read-string input)]
    (if (map? read-input)
      (convert-from-clojure-to-java read-input))))

(defn -readSetFromClojureString
  "Tries to read data from a string that contains data expressed in Clojure data structures.
   This method checks for Set type of data for the top-level object and returns nil/null otherwise."
  [input]
  (let [read-input (read-string input)]
    (if (set? read-input)
      (convert-from-clojure-to-java read-input))))

