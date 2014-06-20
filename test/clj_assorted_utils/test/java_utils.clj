;;;
;;;   Copyright 2014, Ruediger Gad
;;;
;;;   This software is released under the terms of the Eclipse Public License 
;;;   (EPL) 1.0. You can find a copy of the EPL at: 
;;;   http://opensource.org/licenses/eclipse-1.0.php
;;;

(ns
  ^{:author "Ruediger Gad",
    :doc "Unit tests for Java utility and helper functions"}
  clj-assorted-utils.test.java-utils
  (:use clojure.test
        clojure.test.junit
        clojure.java.io
        clj-assorted-utils.util)
  (:import (clj_assorted_utils JavaUtils)
           (java.util ArrayList HashMap HashSet)))

(deftest read-clojure-data-structure-string-to-java-list
  (let [in-string "(1 2.3 \"foo\")"
        expected (doto (ArrayList.) (.add 1) (.add 2.3) (.add "foo"))
        out (JavaUtils/readObjectFromClojureString in-string)]
    (is (= expected out))
    (is (= java.util.ArrayList (type out)))))

(deftest read-clojure-data-structure-string-to-java-map
  (let [in-string "{\"a\" 1 2 3}"
        expected (doto (HashMap.) (.put "a" 1) (.put 2 3))
        out (JavaUtils/readObjectFromClojureString in-string)]
    (is (= expected out))
    (is (= java.util.HashMap (type out)))))

(deftest read-clojure-data-structure-string-to-java-set
  (let [in-string "#{1 2.3 \"foo\"}"
        expected (doto (HashSet.) (.add 1) (.add 2.3) (.add "foo"))
        out (JavaUtils/readObjectFromClojureString in-string)]
    (is (= expected out))
    (is (= java.util.HashSet (type out)))))

(deftest read-clojure-data-structure-string-to-java-vector
  (let [in-string "[1 2.3 \"foo\"]"
        expected (doto (ArrayList.) (.add 1) (.add 2.3) (.add "foo"))
        out (JavaUtils/readObjectFromClojureString in-string)]
    (is (= expected out))
    (is (= java.util.ArrayList (type out)))))

