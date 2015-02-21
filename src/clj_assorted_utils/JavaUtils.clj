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
  (:use clj-assorted-utils.java-utils
        clj-assorted-utils.util)
  (:import (java.util ArrayList HashMap HashSet List Map Set))
  (:gen-class
    :methods [#^{:static true} [readObjectFromClojureString [String] Object]
              #^{:static true} [readListFromClojureString [String] java.util.List]
              #^{:static true} [readMapFromClojureString [String] java.util.Map]
              #^{:static true} [readSetFromClojureString [String] java.util.Set]]))

(defn -readObjectFromClojureString [input]
  (read-object-from-clojure-string input))

(defn -readListFromClojureString [input]
  (read-list-from-clojure-string input))

(defn -readMapFromClojureString [input]
  (read-map-from-clojure-string input))

(defn -readSetFromClojureString [input]
  (read-set-from-clojure-string input))

