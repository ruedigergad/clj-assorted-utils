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
  (:require
    (clojure
      [test :refer :all])
    (clojure.test
      [junit :refer :all])
    (clj-assorted-utils
      [JavaUtils :refer :all]
      [java-utils :refer :all]))
  (:import (clj_assorted_utils JavaUtils)
           (java.util ArrayList HashMap HashSet)))

(deftest read-clojure-data-structure-string-to-java-list
  (let [in-string "(1 2.3 \"foo\")"
        expected (doto (ArrayList.) (.add 1) (.add 2.3) (.add "foo"))
        out (read-object-from-clojure-string in-string)]
    (is (= expected out))
    (is (= java.util.ArrayList (type out)))))

(deftest read-clojure-data-structure-string-to-java-list-2
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

(deftest read-clojure-data-structure-string-to-java-list-type-safe
  (let [in-string "(1 2.3 \"foo\")"
        expected (doto (ArrayList.) (.add 1) (.add 2.3) (.add "foo"))
        out (read-list-from-clojure-string in-string)]
    (is (= expected out))
    (is (= java.util.ArrayList (type out)))))

(deftest read-clojure-data-structure-string-to-java-list-type-safe-2
  (let [in-string "(1 2.3 \"foo\")"
        expected (doto (ArrayList.) (.add 1) (.add 2.3) (.add "foo"))
        out (JavaUtils/readListFromClojureString in-string)]
    (is (= expected out))
    (is (= java.util.ArrayList (type out)))))

(deftest read-clojure-data-structure-string-to-java-map-type-safe
  (let [in-string "{\"a\" 1 2 3}"
        expected (doto (HashMap.) (.put "a" 1) (.put 2 3))
        out (read-map-from-clojure-string in-string)]
    (is (= expected out))
    (is (= java.util.HashMap (type out)))))

(deftest read-clojure-data-structure-string-to-java-map-type-safe-2
  (let [in-string "{\"a\" 1 2 3}"
        expected (doto (HashMap.) (.put "a" 1) (.put 2 3))
        out (JavaUtils/readMapFromClojureString in-string)]
    (is (= expected out))
    (is (= java.util.HashMap (type out)))))

(deftest read-clojure-data-structure-string-to-java-set-type-safe
  (let [in-string "#{1 2.3 \"foo\"}"
        expected (doto (HashSet.) (.add 1) (.add 2.3) (.add "foo"))
        out (read-set-from-clojure-string in-string)]
    (is (= expected out))
    (is (= java.util.HashSet (type out)))))

(deftest read-clojure-data-structure-string-to-java-set-type-safe-2
  (let [in-string "#{1 2.3 \"foo\"}"
        expected (doto (HashSet.) (.add 1) (.add 2.3) (.add "foo"))
        out (JavaUtils/readSetFromClojureString in-string)]
    (is (= expected out))
    (is (= java.util.HashSet (type out)))))

(deftest read-clojure-data-structure-string-to-java-vector-type-safe
  (let [in-string "[1 2.3 \"foo\"]"
        expected (doto (ArrayList.) (.add 1) (.add 2.3) (.add "foo"))
        out (JavaUtils/readListFromClojureString in-string)]
    (is (= expected out))
    (is (= java.util.ArrayList (type out)))))

(deftest read-clojure-data-structure-string-to-java-list-type-safe-nil
  (let [in-string "#{1 2.3 \"foo\"}"
        out (JavaUtils/readListFromClojureString in-string)]
    (is (= nil out))))

(deftest read-clojure-data-structure-string-to-java-map-type-safe-nil
  (let [in-string "[\"a\" 1 2 3]"
        out (JavaUtils/readMapFromClojureString in-string)]
    (is (= nil out))))

(deftest read-clojure-data-structure-string-to-java-set-type-safe-nil
  (let [in-string "[1 2.3 \"foo\"]"
        out (JavaUtils/readSetFromClojureString in-string)]
    (is (= nil out))))

(deftest read-clojure-data-structure-string-to-java-vector-type-safe-nil
  (let [in-string "#{1 2.3 \"foo\"}"
        out (JavaUtils/readListFromClojureString in-string)]
    (is (= nil out))))

(deftest check-for-non-eval-on-read-object
  (let [in-string "#=(+ 1 2 3)"]
    (is (thrown? RuntimeException (JavaUtils/readObjectFromClojureString in-string)))))

(deftest check-for-non-eval-on-read-list
  (let [in-string "#=(+ 1 2 3)"]
    (is (thrown? RuntimeException (JavaUtils/readListFromClojureString in-string)))))

(deftest check-for-non-eval-on-read-map
  (let [in-string "#=(+ 1 2 3)"]
    (is (thrown? RuntimeException (JavaUtils/readMapFromClojureString in-string)))))

(deftest check-for-non-eval-on-read-set
  (let [in-string "#=(+ 1 2 3)"]
    (is (thrown? RuntimeException (JavaUtils/readSetFromClojureString in-string)))))

