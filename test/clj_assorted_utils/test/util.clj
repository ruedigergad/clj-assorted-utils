;;;
;;;   Copyright 2012-2021 Ruediger Gad
;;;
;;;   This software is released under the terms of the Eclipse Public License 
;;;   (EPL) 1.0. You can find a copy of the EPL at: 
;;;   http://opensource.org/licenses/eclipse-1.0.php
;;;

(ns
  ^{:author "Ruediger Gad",
    :doc "Unit tests for utility and helper functions"}
  clj-assorted-utils.test.util
  (:require
    (clojure
      [test :as test])
    (clojure.java
      [io :as jio])
    (clj-assorted-utils
      [util :as util]))
  (:import
    (java.util ArrayList HashMap HashSet)))


;(defn junit-output-fixture [f]
;  (with-open [wrtr (writer (str *ns* ".xml"))]
;    (binding [*test-out* wrtr]
;      (with-junit-output
;        (f)))))
;(use-fixtures :once junit-output-fixture)

;;;
;;; Test for getting system properties.
;;;
(test/deftest get-property-test
  (test/is (= java.lang.String (type (util/get-system-property "os.arch"))))) 

(test/deftest is-os-test
  (test/is (not (util/is-os? "FooOS"))))



;;;
;;; Tests for executing commands.
;;;
(test/deftest test-exec-with-out
  (let [command "ls /etc/passwd"
        stdout-run (util/prepare-flag)
        stdout-fn (fn [_] (util/set-flag stdout-run) nil)]
    (util/exec-with-out command stdout-fn)
    (util/sleep 100)
    (test/is (util/flag-set? stdout-run))))
    
(test/deftest test-exec-with-out-process-output
  (let [command "ls /etc/passwd"
        stdout-run (util/prepare-flag)
        stdout-fn (fn [out] (when (= out "/etc/passwd") (util/set-flag stdout-run)))]
    (util/exec-with-out command stdout-fn)
    (util/sleep 100)
    (test/is (util/flag-set? stdout-run))))

(test/deftest test-exec-with-out-process-output-2
  (let [command (into-array java.lang.String ["/bin/sh" "-c" "ls /etc/passwd 1>&2"])
        stderr-run (util/prepare-flag)
        stderr-fn (fn [out] (when (= out "/etc/passwd") (util/set-flag stderr-run)))
        stdout-run (util/prepare-flag)
        stdout-fn (fn [_] (util/set-flag stdout-run))]
    (util/exec-with-out command stdout-fn stderr-fn)
    (util/sleep 100)
    (test/is (util/flag-set? stderr-run))
    (test/is (not (util/flag-set? stdout-run)))))



;;;
;;; Tests for handling files and directories.
;;;
(def test-dirname "test-dir-foo")
(def test-filename "test-file-bar")

(test/deftest test-exists
  (test/is (util/exists? "test/clj_assorted_utils/test/util.clj"))
  (test/is (util/exists? "test")))

(test/deftest test-is-file
  (test/is (util/is-file? "test/clj_assorted_utils/test/util.clj"))
  (test/is (not (util/is-file? "test"))))

(test/deftest test-file-exists
  (test/is (util/file-exists? "test/clj_assorted_utils/test/util.clj"))
  (test/is (not (util/file-exists? "test")))
  (test/is (not (util/file-exists? test-filename))))

(test/deftest test-is-dir
  (test/is (util/is-dir? "test"))
  (test/is (not (util/is-dir? "test/clj_assorted_utils/test/util.clj"))))

(test/deftest test-dir-exists
  (test/is (util/dir-exists? "test"))
  (test/is (not (util/dir-exists? "test/clj_assorted_utils/test/util.clj")))
  (test/is (not (util/dir-exists? test-dirname))))

(test/deftest mkdir-rmdir
  (let [dirname test-dirname]
    (test/is (not (util/dir-exists? dirname)))
    (util/mkdir dirname)
    (test/is (util/dir-exists? dirname))
    (util/rmdir dirname)
    (test/is (not (util/dir-exists? dirname)))))

(test/deftest touch-rm
  (let [filename test-filename]
    (test/is (not (util/file-exists? filename)))
    (util/touch filename)
    (test/is (util/file-exists? filename))
    (util/rm filename)
    (test/is (not (util/file-exists? filename)))))



;;;
;;; Tests for setting and querying flags and a simple counter.
;;;
(test/deftest flag-not-set
  (let [flag (util/prepare-flag)]
    (test/is (not (util/flag-set? flag)))))

(test/deftest flag-set
  (let [flag (util/prepare-flag)]
    (util/set-flag flag)
    (test/is (util/flag-set? flag))))

(test/deftest test-set-flag-twice
  (let [flag (util/prepare-flag)]
    (util/set-flag flag)
    (util/set-flag flag)
    (test/is (util/flag-set? flag))))

(test/deftest await-flag-test
  (let [flag (util/prepare-flag)]
    (util/run-once (util/executor) #(util/set-flag flag) 200)
    (util/await-flag flag)
    (test/is (util/flag-set? flag))))

(test/deftest await-flag-n-test
  (let [flag (util/prepare-flag 4)
        cntr (util/counter)]
    (util/run-repeat
      (util/executor)
      #(when (not (util/flag-set? flag))
         (cntr inc)
         (util/set-flag flag))
      200)
    (util/await-flag flag)
    (test/is (util/flag-set? flag))
    (test/is (= 4 (cntr)))))

(test/deftest counter-test
  (let [my-counter (util/prepare-counter)]
    (dotimes [_ 1000] (util/inc-counter my-counter))
    (test/is (= 1000 @my-counter))))

(test/deftest counter-with-initial-value-test
  (let [my-counter (util/prepare-counter 1000)]
    (dotimes [_ 1000] (util/inc-counter my-counter))
    (test/is (= 2000 @my-counter))))

(test/deftest counter-convenience-test
  (let [my-counter (util/counter)]
    (dotimes [_ 1000] (my-counter inc))
    (test/is (= 1000 (my-counter)))))

(test/deftest counter-convenience-test-custom-fn
  (let [my-counter (util/counter)]
    (dotimes [_ 1000] (my-counter #(+ 2 %)))
    (test/is (= 2000 (my-counter)))))

(test/deftest counter-convenience-test-init-value-decrementing
  (let [my-counter (util/counter 1000)]
    (dotimes [_ 1000] (my-counter dec))
    (test/is (= 0 (my-counter)))))

(test/deftest counter-invalid-operation-test
  (let [cntr (util/counter)
        out-string (with-out-str (cntr "foo"))]
    (test/is (= "No function passed: foo\n" out-string)))) 

(test/deftest simple-delta-counter-test
  (let [cntr (util/counter)
        delta-cntr (util/delta-counter)]
    (test/is (= 0 (delta-cntr :cntr (cntr))))
    (cntr inc)
    (cntr inc)
    (cntr inc)
    (test/is (= 3 (delta-cntr :cntr (cntr))))
    (test/is (= 0 (delta-cntr :cntr (cntr))))))

(test/deftest two-counters-delta-counter-test
  (let [cntr-a (util/counter)
        cntr-b (util/counter)
        delta-cntr (util/delta-counter)]
    (test/is (= 0 (delta-cntr :cntr-a (cntr-a))))
    (test/is (= 0 (delta-cntr :cntr-b (cntr-b))))
    (cntr-a inc)
    (cntr-a inc)
    (cntr-a inc)
    (test/is (= 0 (delta-cntr :cntr-b (cntr-b))))
    (test/is (= 3 (delta-cntr :cntr-a (cntr-a))))
    (test/is (= 0 (delta-cntr :cntr-a (cntr-a))))))



;;;
;;; Tests for getting class and fn names.
;;;
(test/deftest get-classname
  (let [o (Object.)
        n (util/classname o)]
    (test/is (= "Object" n))))

(defn test-fn [] (println "It's -O3 the letter not -03 the number."))

(test/deftest test-fn-name
  (test/is (= "test-fn" (util/fn-name test-fn))))



;;;
;;; Tests for getting information about function arguments.
;;;
(defn test-args-fn [a b c] (+ a b c))

(test/deftest get-defn-arglists-test
  (let [ret (util/get-defn-arglists-m test-args-fn)]
    (test/is (vector? ret))
    (test/is (= '[[a b c]] ret))))

(test/deftest get-fn-arglists-test
  (let [ret (util/get-fn-arglists-m (fn [a b c] (+ a b c)))]
    (test/is (vector? (:args ret)))
    (test/is (= '[[a b c]] (:args ret)))
    (test/is (= 6 ((:fn ret) 1 2 3)))))

(defn test-multi-arity-args-fn 
  ([a] a)
  ([a b] (+ a b))
  ([a b c] (+ a b c)))

(test/deftest get-defn-arglists-multi-arity-test
  (let [ret (util/get-defn-arglists-m test-multi-arity-args-fn)]
    (test/is (vector? ret))
    (test/is (= '[[a] [a b] [a b c]] ret))))

(test/deftest get-fn-arglists-mulit-arity-test
  (let [ret (util/get-fn-arglists-m (fn 
                                      ([a] a)
                                      ([a b] (+ a b))
                                      ([a b c] (+ a b c))))]
    (test/is (vector? (:args ret)))
    (test/is (= '[[a] [a b] [a b c]] (:args ret)))
    (test/is (= 1 ((:fn ret) 1)))
    (test/is (= 3 ((:fn ret) 1 2)))
    (test/is (= 6 ((:fn ret) 1 2 3)))))



;;;
;;; Tests for manipulating vectors.
;;;
(test/deftest test-byte-seq-to-int
  (let [byte-vec [82 17 0 0]
        int-val 4434]
    (test/is (= int-val (util/byte-seq-to-int byte-vec)))))

(test/deftest test-get-int-from-byte-vector
  (let [byte-vec [121 -110 84 79 0 0 0 0 -23 -71 8 0 0 0 0 0 82 17 0 0 82 17 0 0]
        int-val 4434]
    (test/is (= int-val (util/get-int-from-byte-vector byte-vec 16))))) 

(test/deftest test-int-to-byte-vector
  (let [int-val 4434
        byte-vec [82 17 0 0]]
    (test/is (= byte-vec (subvec (util/int-to-byte-vector int-val) 0 4)))))

(test/deftest test-vec-replace
  (let [original-vec [1 2 3 4 5 6]
        expected-vec [1 2 "a" "b" "c" 6]
        changed-vec (util/vec-replace original-vec 2 ["a" "b" "c"])]
    (test/is (= expected-vec changed-vec))))

(test/deftest test-change-int-in-byte-vector
  (let [original-vec [121 -110 84 79 0 0 0 0 -23 -71 8 0 0 0 0 0 82 17 0 0 82 17 0 0]
        expected-vec [121 -110 84 79 0 0 0 0 -23 -71 8 0 0 0 0 0 70 17 0 0 82 17 0 0]
        changed-vec (util/change-int-in-byte-vector original-vec 16 #(- % 12))]
    (test/is (= expected-vec changed-vec))))



;;;
;;; Tests for messing with XML.
;;; Primarily for transforming XML data in string format to maps.
;;;
(test/deftest test-xml-string-to-map
  (let [xml-str "<foo fubar=\"snafu\">bar</foo>"
        expected-map {:tag :foo :attrs {:fubar "snafu"} :content ["bar"]}]
    (test/is (= expected-map (util/xml-string-to-map xml-str)))))

(test/deftest test-stringify-keyword
  (test/is (= "foo" (util/stringify-keyword :foo))))

(test/deftest test-stringify-map
  (let [input-map {:tag :foo :attrs {:fubar "snafu"} :content ["bar"]}
        expected-map {"tag" "foo" "attrs" {"fubar" "snafu"} "content" ["bar"]}]
    (test/is (= expected-map (util/stringify-map input-map)))))

(test/deftest test-xml-string-to-map-stringified
  (let [xml-str "<foo fubar=\"snafu\">bar</foo>"
        expected-map {"tag" "foo" "attrs" {"fubar" "snafu"} "content" ["bar"]}]
    (test/is (= expected-map (util/xml-string-to-map-stringified xml-str)))))



;;;
;;; Tests for running fn's regularly
;;;
(test/deftest test-simple-executor
  (let [flag (util/prepare-flag)
        run-fn #(util/set-flag flag)
        exec (util/executor)]
    (util/run-once exec run-fn 100)
    (util/sleep 300)
    (test/is (util/flag-set? flag))))

(test/deftest test-repeating-executor
  (let [cntr (util/counter)
        run-fn #(cntr inc)
        exec (util/executor)]
    (util/run-repeat exec run-fn 200)
    (util/sleep 500)
    (test/is (= 3 (cntr)))
    (util/sleep 200)
    (test/is (= 4 (cntr)))
    (util/shutdown-now exec)
    (util/sleep 400)
    (test/is (= 4 (cntr)))))

(test/deftest test-repeating-executor-2
  (let [cntr (util/counter)
        run-fn #(cntr inc)
        exec (util/executor)]
    (util/run-repeat exec run-fn 200)
    (util/sleep 500)
    (test/is (= 3 (cntr)))
    (util/sleep 200)
    (test/is (= 4 (cntr)))
    (util/shutdown exec)
    (util/sleep 400)
    (test/is (= 4 (cntr)))))



;;;
;;; Tests for object serialization.
;;;
(test/deftest test-object-to-byte-array
  (let [obj-int (int 123)
        obj-string "abc"
        ba-int (util/object-to-byte-array obj-int)
        ba-string (util/object-to-byte-array obj-string)]
    (test/is (not (nil? ba-int)))
    (test/is (not (nil? ba-string)))))

(test/deftest test-compress-object-to-byte-array
  (let [obj-int (int 123)
        obj-string "abc"
        ba-int (util/compress-object-to-byte-array obj-int :gzip)
        ba-string (util/compress-object-to-byte-array obj-string :gzip)]
    (test/is (not (nil? ba-int)))
    (test/is (not (nil? ba-string)))))



;;;
;;; Tests for reading input line-by-line
;;;
(test/deftest process-line-by-line-test
  (let [cntr (util/counter)]
    (test/is (= 0 (cntr)))
    (util/process-line-by-line "test-file.txt" (fn [_] (cntr inc)))
    (test/is (= 10 (cntr)))))

(test/deftest process-line-by-line-test2
  (let [cntr (util/counter)]
    (test/is (= 0 (cntr)))
    (util/process-line-by-line "test-file.txt" (fn [x] (cntr #(+ % (read-string x)))))
    (test/is (= 55 (cntr)))))



;;;
;;; Tests for converting Clojure specific data structures to their "pure" Java equivalents.
;;;
(test/deftest convert-from-clojure-to-java-list-test
  (let [in '("a" 1 1.23)
        expected (doto (ArrayList.) (.add "a") (.add 1) (.add 1.23))
        out (util/convert-from-clojure-to-java in)]
    (test/is (= expected out))
    (test/is (= java.util.ArrayList (type out)))))

(test/deftest convert-from-clojure-to-java-map-test
  (let [in {"a" 1 "b" 1.23}
        expected (doto (HashMap.) (.put "a" 1) (.put "b" 1.23))
        out (util/convert-from-clojure-to-java in)]
    (test/is (= expected out))
    (test/is (= java.util.HashMap (type out)))))

(test/deftest convert-from-clojure-to-java-set-test
  (let [in #{"a" 1 1.23}
        expected (doto (HashSet.) (.add "a") (.add 1) (.add 1.23))
        out (util/convert-from-clojure-to-java in)]
    (test/is (= expected out))
    (test/is (= java.util.HashSet (type out)))))

(test/deftest convert-from-clojure-to-java-vector-test
  (let [in ["a" 1 1.23]
        expected (doto (ArrayList.) (.add "a") (.add 1) (.add 1.23))
        out (util/convert-from-clojure-to-java in)]
    (test/is (= expected out))
    (test/is (= java.util.ArrayList (type out)))))

(test/deftest convert-from-clojure-to-java-nested-map-test
  (let [in {"a" {"b" 1}}
        expected-nested (doto (HashMap.) (.put "b" 1))
        expected (doto (HashMap.) (.put "a" expected-nested))
        out (util/convert-from-clojure-to-java in)]
    (test/is (= expected out))
    (test/is (= java.util.HashMap (type out)))
    (test/is (= expected-nested (.get out "a")))
    (test/is (= java.util.HashMap (type (.get out "a"))))))

(test/deftest convert-from-clojure-to-java-nested-set-test
  (let [in #{"a" #{1 1.23}}
        expected-nested (doto (HashSet.) (.add 1) (.add 1.23))
        expected (doto (HashSet.) (.add "a") (.add expected-nested))
        out (util/convert-from-clojure-to-java in)]
    (test/is (= expected out))
    (test/is (= java.util.HashSet (type out)))))

(test/deftest convert-from-clojure-to-java-nested-vector-test
  (let [in ["a" [1 1.23]]
        expected-nested (doto (ArrayList.) (.add 1) (.add 1.23))
        expected (doto (ArrayList.) (.add "a") (.add expected-nested))
        out (util/convert-from-clojure-to-java in)]
    (test/is (= expected out))
    (test/is (= java.util.ArrayList (type out)))
    (test/is (= expected-nested (.get out 1)))
    (test/is (= java.util.ArrayList (type (.get out 1))))))



;;;
;;; Tests for selectively accumulating map entries.
;;;
(test/deftest reduce-selected-map-entries-sum-test
  (let [in {"a" 1 "b" 2 "c" 3 "d" 4}]
    (test/is (= 6 (util/reduce-selected-map-entries in + ["a" "b" "c"])))))

(test/deftest reduce-selected-map-entries-sum-nil-test
  (let [in {"a" 1 "b" 2 "c" 3 "d" 4}]
    (test/is (= 7 (util/reduce-selected-map-entries in + ["c" "xyz" "d"])))))

(test/deftest reduce-selected-map-entries-concat-string-test
  (let [in {"a" 1 "b" 2 "c" 3 "d" 4}]
    (test/is (= "234" (util/reduce-selected-map-entries in str ["b" "c" "d"])))))

(test/deftest reduce-selected-map-entries-concat-string-nil-test
  (let [in {"a" 1 "b" 2 "c" 3 "d" 4}]
    (test/is (= "23" (util/reduce-selected-map-entries in str ["b" "abc" "c"])))))



;;;
;;; Tests for printing to stderr.
;;;
(test/deftest print-err-no-stdout-test
  (test/is (= "" (with-out-str (util/print-err "foo")))))

(test/deftest print-err-test
  (test/is (= "foo" (util/with-err-str (util/print-err "foo")))))

(test/deftest println-err-test
  (test/is (= "foo\n" (util/with-err-str (util/println-err "foo")))))



;;;
;;; Tests for with-out-str-custom that allows to execute a function on each added string.
;;;
(test/deftest with-out-str-custom-single-println-test
  (let [intercepted-input (atom "")
        write-fn (fn [s]
                   (swap! intercepted-input str s)
                   s)]
    (test/is (= "foo\n" (util/with-out-str-custom write-fn (println "foo"))))
    (test/is (= "foo\n" @intercepted-input))))

(test/deftest with-out-str-custom-double-println-test
  (let [intercepted-input (atom "")
        write-fn (fn [s]
                   (swap! intercepted-input str s)
                   s)]
    (test/is (= "foo\nbar\n" (util/with-out-str-custom write-fn (println "foo") (println "bar"))))
    (test/is (= "foo\nbar\n" @intercepted-input))))

(test/deftest with-out-str-custom-single-print-test
  (let [intercepted-input (atom "")
        write-fn (fn [s]
                   (swap! intercepted-input str s)
                   s)]
    (test/is (= "foo" (util/with-out-str-custom write-fn (print "foo"))))
    (test/is (= "foo" @intercepted-input))))

(test/deftest with-out-str-custom-double-print-test
  (let [intercepted-input (atom "")
        write-fn (fn [s]
                   (swap! intercepted-input str s)
                   s)]
    (test/is (= "foobar" (util/with-out-str-custom write-fn (print "foo") (print "bar"))))
    (test/is (= "foobar" @intercepted-input))))

(test/deftest with-out-str-custom-manipulated-string-test
  (let [intercepted-input (atom "")
        write-fn (fn [s]
                   (swap! intercepted-input str s)
                   (str s s))]
    (test/is (= "foofoo" (util/with-out-str-custom write-fn (print "foo"))))
    (test/is (= "foo" @intercepted-input))))

(test/deftest with-out-str-custom-nil-test
  (let [intercepted-input (atom "")
        write-fn (fn [s]
                   (swap! intercepted-input str s)
                   nil)]
    (test/is (= "" (util/with-out-str-custom write-fn (print "foo"))))
    (test/is (= "foo" @intercepted-input))))

(test/deftest with-err-str-custom-single-print-test
  (let [intercepted-input (atom "")
        write-fn (fn [s]
                   (swap! intercepted-input str s)
                   (str "bar" s "baz"))]
    (test/is (= "barfoobaz" (util/with-err-str-custom write-fn (util/print-err "foo"))))
    (test/is (= "foo" @intercepted-input))))

(test/deftest with-out-str-cb-double-println-test
  (let [intercepted-input (atom "")
        write-fn (fn [s]
                   (swap! intercepted-input str s))]
    (test/is (= "foo\nbar\n" (util/with-out-str-cb write-fn (println "foo") (println "bar"))))
    (test/is (= "foo\nbar\n" @intercepted-input))))

(test/deftest with-err-str-cb-double-println-test
  (let [intercepted-input (atom "")
        write-fn (fn [s]
                   (swap! intercepted-input str s))]
    (test/is (= "foo\nbar\n" (util/with-err-str-cb write-fn (util/println-err "foo") (util/println-err "bar"))))
    (test/is (= "foo\nbar\n" @intercepted-input))))

(test/deftest with-eo-str-test
  (let [{:keys [stdout stderr all ret]}
        (util/with-eo-str
          (println "foo")
          (util/println-err "bar")
          (+ 1 2 3))]
    (test/is (= "foo\nbar\n" all))
    (test/is (= "foo\n" stdout))
    (test/is (= "bar\n" stderr))
    (test/is (= 6 ret)))

  (test/testing "captures compiler output"
    (test/testing "reflection warnings"
      (let [{:keys [stdout stderr all ret]}
            (util/with-eo-str
              (println "foo-out")
              (util/println-err "bar-err")
              (eval '(.toString 42))
              (+ 1 2 3))]
        (test/is (.startsWith all "foo-out\nbar-err\nReflection warning,"))
        (test/is (= "foo-out\n" stdout))
        (test/is (.startsWith stderr "bar-err\nReflection warning,")
            (pr-str stderr))
        (test/is (re-find #"bar-err\nReflection warning, .* - reference to field toString on long can't be resolved.\n"
                     stderr))
        (test/is (= 6 ret))))

    (test/testing "var replacement warning"
      (let [{:keys [stdout stderr all ret]}
            (util/with-eo-str
              (println "foo-out")
              (util/println-err "bar-err")
              (eval '(def rationalize clojure.core/rationalize))
              (+ 1 2 3))]
        (test/is (= all "foo-out\nbar-err\nWARNING: rationalize already refers to: #'clojure.core/rationalize in namespace: user, being replaced by: #'user/rationalize\n"))
        (test/is (= "foo-out\n" stdout))
        (test/is (= stderr "bar-err\nWARNING: rationalize already refers to: #'clojure.core/rationalize in namespace: user, being replaced by: #'user/rationalize\n"))
        (test/is (= 6 ret))))))



;;;
;;; Tests for getting data from a Map with default values when the entry does not exist.
;;;
(test/deftest get-with-default-key-exists-test
  (let [m {"a" "A" "b" 6}]
    (test/is (= "A" (util/get-with-default m "a" "X")))
    (test/is (= 6 (util/get-with-default m "b" 1)))))

(test/deftest get-with-default-key-does-not-exist-test
  (let [m {"c" "C"}]
    (test/is (= "X" (util/get-with-default m "a" "X")))
    (test/is (= 1 (util/get-with-default m "b" 1)))))



;;;
;;; Tests for convenience functions for writing to files.
;;;
(def test-out-file "file-out.test.file")

(test/deftest simple-str-to-file-without-newline-test
  (util/rm test-out-file)
  (let [str-file-out (util/create-string-to-file-output test-out-file)]
    (str-file-out "my-string")
    (test/is (= "my-string" (slurp test-out-file)))
    (str-file-out)))

(test/deftest simple-str-to-file-with-newline-test
  (util/rm test-out-file)
  (let [str-file-out (util/create-string-to-file-output test-out-file {:insert-newline true})]
    (str-file-out "my-string")
    (test/is (= "my-string\n" (slurp test-out-file)))
    (str-file-out)))

(test/deftest simple-str-to-file-no-append-test
  (util/rm test-out-file)
  (let [str-file-out (util/create-string-to-file-output test-out-file)]
    (str-file-out "my-string")
    (test/is (= "my-string" (slurp test-out-file)))
    (str-file-out)
    ((util/create-string-to-file-output test-out-file) "my-new-string")
    (test/is (= "my-new-string" (slurp test-out-file)))))

(test/deftest simple-str-to-file-append-test
  (util/rm test-out-file)
  (let [str-file-out (util/create-string-to-file-output test-out-file)]
    (str-file-out "my-string")
    (test/is (= "my-string" (slurp test-out-file)))
    (str-file-out)
    ((util/create-string-to-file-output test-out-file {:append true}) "my-new-string")
    (test/is (= "my-stringmy-new-string" (slurp test-out-file)))))

(test/deftest simple-str-list-to-file-test
  (util/rm test-out-file)
  (let [str-file-out (util/create-string-to-file-output test-out-file)
        str-lst '("foo" "bar" "blah")]
    (str-file-out str-lst)
    (test/is (= "foobarblah" (slurp test-out-file)))
    (str-file-out)))

(test/deftest simple-map-to-file-test
  (util/rm test-out-file)
  (let [str-file-out (util/create-string-to-file-output test-out-file)
        str-lst {"foo" "bar"}]
    (str-file-out str-lst)
    (test/is (= "{\"foo\" \"bar\"}" (slurp test-out-file)))
    (str-file-out)))

(test/deftest simple-fifo-str-to-file-test
  (util/rm test-out-file)
  (util/mkfifo test-out-file)
  (let [str-file-out (util/create-string-to-file-output test-out-file {:append true :await-open false})]
    (util/run-once (util/executor) #(str-file-out "my-string\n") 100)
    (with-open [rdr (clojure.java.io/reader test-out-file)]
      (test/is (= "my-string" (first (line-seq rdr)))))
    (str-file-out)))

(test/deftest fifo-write-and-read-repeatedly-test
  (util/rm test-out-file)
  (util/mkfifo test-out-file)
  (let [str-file-out (util/create-string-to-file-output test-out-file {:append true :await-open false})
        ctr (util/counter)]
    (util/create-threaded-lineseq-reader test-out-file (fn [_] (ctr inc)))
    (util/sleep 100)
    (str-file-out "my-string\n")
    (str-file-out "my-string\n")
    (str-file-out "my-string\n")
    (util/sleep 100)
    (test/is (= 3 (ctr)))
    (str-file-out)))

(test/deftest close-and-re-open-fifo-writer-test
  (util/rm test-out-file)
  (util/mkfifo test-out-file)
  (let [str-file-out (util/create-string-to-file-output test-out-file {:append true :await-open false})
        ctr (util/counter)]
    (util/create-threaded-lineseq-reader test-out-file (fn [_] (ctr inc)))
    (util/sleep 100)
    (str-file-out "my-string\n")
    (util/sleep 100)
    (str-file-out)
    (util/sleep 100)
    (let [str-file-out-2 (util/create-string-to-file-output test-out-file {:append true :await-open false})]
      (util/sleep 100)
      (str-file-out-2 "my-string\n")
      (str-file-out-2 "my-string\n")
      (util/sleep 100)
      (str-file-out-2))
    (test/is (= 3 (ctr)))))

