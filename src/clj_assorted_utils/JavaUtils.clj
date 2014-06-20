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
  (:import (java.util ArrayList HashMap HashSet))
  (:gen-class
    :methods [#^{:static true} [readObjectFromClojureString [String] Object]]))

(defn -readObjectFromClojureString
  "Tries to read data from a string that contains data expressed in Clojure data structures."
  [input]
  (let [read-input (read-string input)]
    (convert-from-clojure-to-java read-input)))

