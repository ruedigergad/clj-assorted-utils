;;;
;;;   Copyright 2014-2021 Ruediger Gad
;;;
;;;   This software is released under the terms of the Eclipse Public License 
;;;   (EPL) 1.0. You can find a copy of the EPL at: 
;;;   http://opensource.org/licenses/eclipse-1.0.php
;;;

(ns
  ^{:author "Ruediger Gad",
    :doc "Java utility and helper functions"}
  clj-assorted-utils.JavaUtils
  (:require
    (clj-assorted-utils
      [java-utils :as jutil]
      [util :as util]))
  (:gen-class
    :methods [#^{:static true} [convertFromClojureToJava [Object] Object]
              #^{:static true} [readObjectFromClojureString [String] Object]
              #^{:static true} [readListFromClojureString [String] java.util.List]
              #^{:static true} [readMapFromClojureString [String] java.util.Map]
              #^{:static true} [readSetFromClojureString [String] java.util.Set]]))

(defn -convertFromClojureToJava [input]
  (util/convert-from-clojure-to-java input))

(defn -readObjectFromClojureString [input]
  (jutil/read-object-from-clojure-string input))

(defn -readListFromClojureString [input]
  (jutil/read-list-from-clojure-string input))

(defn -readMapFromClojureString [input]
  (jutil/read-map-from-clojure-string input))

(defn -readSetFromClojureString [input]
  (jutil/read-set-from-clojure-string input))

