;;;
;;;   Copyright 2012, 2013, 2014, Ruediger Gad
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
      [test :refer :all])
    (clojure.test
      [junit :refer :all])
    (clojure.java
      [io :refer :all])
    (clj-assorted-utils
      [util :refer :all]))
  (:import
    (java.util ArrayList HashMap HashSet)
    (java.io StringWriter)))


;(defn junit-output-fixture [f]
;  (with-open [wrtr (writer (str *ns* ".xml"))]
;    (binding [*test-out* wrtr]
;      (with-junit-output
;        (f)))))
;(use-fixtures :once junit-output-fixture)

;;;
;;; Test for getting system properties.
;;;
(deftest get-property-test
  (is (= java.lang.String (type (get-system-property "os.arch"))))) 

(deftest is-os-test
  (is (not (is-os? "FooOS"))))



;;;
;;; Tests for executing commands.
;;;
(deftest test-exec-with-out
  (let [command "ls /etc/passwd"
        stdout-run (prepare-flag)
        stdout-fn (fn [_] (set-flag stdout-run) nil)]
    (exec-with-out command stdout-fn)
    (sleep 100)
    (is (flag-set? stdout-run))))
    
(deftest test-exec-with-out-process-output
  (let [command "ls /etc/passwd"
        stdout-run (prepare-flag)
        stdout-fn (fn [out] (if (= out "/etc/passwd") (set-flag stdout-run)))]
    (exec-with-out command stdout-fn)
    (sleep 100)
    (is (flag-set? stdout-run))))

(deftest test-exec-with-out-process-output-2
  (let [command (into-array java.lang.String ["/bin/sh" "-c" "ls /etc/passwd 1>&2"])
        stderr-run (prepare-flag)
        stderr-fn (fn [out] (if (= out "/etc/passwd") (set-flag stderr-run)))
        stdout-run (prepare-flag)
        stdout-fn (fn [out] (set-flag stdout-run))]
    (exec-with-out command stdout-fn stderr-fn)
    (sleep 100)
    (is (flag-set? stderr-run))
    (is (not (flag-set? stdout-run)))))



;;;
;;; Tests for handling files and directories.
;;;
(def test-dirname "test-dir-foo")
(def test-filename "test-file-bar")

(deftest test-exists
  (is (exists? "test/clj_assorted_utils/test/util.clj"))
  (is (exists? "test")))

(deftest test-is-file
  (is (is-file? "test/clj_assorted_utils/test/util.clj"))
  (is (not (is-file? "test"))))

(deftest test-file-exists
  (is (file-exists? "test/clj_assorted_utils/test/util.clj"))
  (is (not (file-exists? "test")))
  (is (not (file-exists? test-filename))))

(deftest test-is-dir
  (is (is-dir? "test"))
  (is (not (is-dir? "test/clj_assorted_utils/test/util.clj"))))

(deftest test-dir-exists
  (is (dir-exists? "test"))
  (is (not (dir-exists? "test/clj_assorted_utils/test/util.clj")))
  (is (not (dir-exists? test-dirname))))

(deftest mkdir-rmdir
  (let [dirname test-dirname]
    (is (not (dir-exists? dirname)))
    (mkdir dirname)
    (is (dir-exists? dirname))
    (rmdir dirname)
    (is (not (dir-exists? dirname)))))

(deftest touch-rm
  (let [filename test-filename]
    (is (not (file-exists? filename)))
    (touch filename)
    (is (file-exists? filename))
    (rm filename)
    (is (not (file-exists? filename)))))



;;;
;;; Tests for setting and querying flags and a simple counter.
;;;
(deftest flag-not-set
  (let [flag (prepare-flag)]
    (is (not (flag-set? flag)))))

(deftest flag-set
  (let [flag (prepare-flag)]
    (set-flag flag)
    (is (flag-set? flag))))

(deftest await-flag-test
  (let [flag (prepare-flag)]
    (run-once (executor) #(set-flag flag) 200)
    (await-flag flag)
    (is (flag-set? flag))))

(deftest await-flag-n-test
  (let [flag (prepare-flag 4)
        cntr (counter)]
    (run-repeat
      (executor)
      #(when (not (flag-set? flag))
         (cntr inc)
         (set-flag flag))
      200)
    (await-flag flag)
    (is (flag-set? flag))
    (is (= 4 (cntr)))))

(deftest counter-test
  (let [my-counter (prepare-counter)]
    (dotimes [_ 1000] (inc-counter my-counter))
    (is (= 1000 @my-counter))))

(deftest counter-with-initial-value-test
  (let [my-counter (prepare-counter 1000)]
    (dotimes [_ 1000] (inc-counter my-counter))
    (is (= 2000 @my-counter))))

(deftest counter-convenience-test
  (let [my-counter (counter)]
    (dotimes [_ 1000] (my-counter inc))
    (is (= 1000 (my-counter)))))

(deftest counter-convenience-test-custom-fn
  (let [my-counter (counter)]
    (dotimes [_ 1000] (my-counter #(+ 2 %)))
    (is (= 2000 (my-counter)))))

(deftest counter-convenience-test-init-value-decrementing
  (let [my-counter (counter 1000)]
    (dotimes [_ 1000] (my-counter dec))
    (is (= 0 (my-counter)))))

(deftest counter-invalid-operation-test
  (let [cntr (counter)
        out-string (with-out-str (cntr "foo"))]
    (is (= "No function passed: foo\n" out-string)))) 

(deftest simple-delta-counter-test
  (let [cntr (counter)
        delta-cntr (delta-counter)]
    (is (= 0 (delta-cntr :cntr (cntr))))
    (cntr inc)
    (cntr inc)
    (cntr inc)
    (is (= 3 (delta-cntr :cntr (cntr))))
    (is (= 0 (delta-cntr :cntr (cntr))))))

(deftest two-counters-delta-counter-test
  (let [cntr-a (counter)
        cntr-b (counter)
        delta-cntr (delta-counter)]
    (is (= 0 (delta-cntr :cntr-a (cntr-a))))
    (is (= 0 (delta-cntr :cntr-b (cntr-b))))
    (cntr-a inc)
    (cntr-a inc)
    (cntr-a inc)
    (is (= 0 (delta-cntr :cntr-b (cntr-b))))
    (is (= 3 (delta-cntr :cntr-a (cntr-a))))
    (is (= 0 (delta-cntr :cntr-a (cntr-a))))))



;;;
;;; Tests for getting class and fn names.
;;;
(deftest get-classname
  (let [o (Object.)
        n (classname o)]
    (is (= "Object" n))))

(defn test-fn [] (println "It's -O3 the letter not -03 the number."))

(deftest test-fn-name
  (is (= "test-fn" (fn-name test-fn))))



;;;
;;; Tests for getting information about function arguments.
;;;
(defn test-args-fn [a b c] (+ a b c))

(deftest get-defn-arglists-test
  (let [ret (get-defn-arglists-m test-args-fn)]
    (is (vector? ret))
    (is (= '[[a b c]] ret))))

(deftest get-fn-arglists-test
  (let [ret (get-fn-arglists-m (fn [a b c] (+ a b c)))]
    (is (vector? (:args ret)))
    (is (= '[[a b c]] (:args ret)))
    (is (= 6 ((:fn ret) 1 2 3)))))

(defn test-multi-arity-args-fn 
  ([a] a)
  ([a b] (+ a b))
  ([a b c] (+ a b c)))

(deftest get-defn-arglists-multi-arity-test
  (let [ret (get-defn-arglists-m test-multi-arity-args-fn)]
    (is (vector? ret))
    (is (= '[[a] [a b] [a b c]] ret))))

(deftest get-fn-arglists-mulit-arity-test
  (let [ret (get-fn-arglists-m (fn 
                                 ([a] a)
                                 ([a b] (+ a b))
                                 ([a b c] (+ a b c))))]
    (is (vector? (:args ret)))
    (is (= '[[a] [a b] [a b c]] (:args ret)))
    (is (= 1 ((:fn ret) 1)))
    (is (= 3 ((:fn ret) 1 2)))
    (is (= 6 ((:fn ret) 1 2 3)))))



;;;
;;; Tests for manipulating vectors.
;;;
(deftest test-byte-seq-to-int
  (let [byte-vec [82 17 0 0]
        int-val 4434]
    (is (= int-val (byte-seq-to-int byte-vec)))))

(deftest test-get-int-from-byte-vector
  (let [byte-vec [121 -110 84 79 0 0 0 0 -23 -71 8 0 0 0 0 0 82 17 0 0 82 17 0 0]
        int-val 4434]
    (is (= int-val (get-int-from-byte-vector byte-vec 16))))) 

(deftest test-int-to-byte-vector
  (let [int-val 4434
        byte-vec [82 17 0 0]]
    (is (= byte-vec (subvec (int-to-byte-vector int-val) 0 4)))))

(deftest test-vec-replace
  (let [original-vec [1 2 3 4 5 6]
        expected-vec [1 2 "a" "b" "c" 6]
        changed-vec (vec-replace original-vec 2 ["a" "b" "c"])]
    (is (= expected-vec changed-vec))))

(deftest test-change-int-in-byte-vector
  (let [original-vec [121 -110 84 79 0 0 0 0 -23 -71 8 0 0 0 0 0 82 17 0 0 82 17 0 0]
        expected-vec [121 -110 84 79 0 0 0 0 -23 -71 8 0 0 0 0 0 70 17 0 0 82 17 0 0]
        changed-vec (change-int-in-byte-vector original-vec 16 #(- % 12))]
    (is (= expected-vec changed-vec))))



;;;
;;; Tests for messing with XML.
;;; Primarily for transforming XML data in string format to maps.
;;;
(deftest test-xml-string-to-map
  (let [xml-str "<foo fubar=\"snafu\">bar</foo>"
        expected-map {:tag :foo :attrs {:fubar "snafu"} :content ["bar"]}]
    (is (= expected-map (xml-string-to-map xml-str)))))

(deftest test-stringify-keyword
  (is (= "foo" (stringify-keyword :foo))))

(deftest test-stringify-map
  (let [input-map {:tag :foo :attrs {:fubar "snafu"} :content ["bar"]}
        expected-map {"tag" "foo" "attrs" {"fubar" "snafu"} "content" ["bar"]}]
    (is (= expected-map (stringify-map input-map)))))

(deftest test-xml-string-to-map-stringified
  (let [xml-str "<foo fubar=\"snafu\">bar</foo>"
        expected-map {"tag" "foo" "attrs" {"fubar" "snafu"} "content" ["bar"]}]
    (is (= expected-map (xml-string-to-map-stringified xml-str)))))



;;;
;;; Tests for running fn's regularly
;;;
(deftest test-simple-executor
  (let [flag (prepare-flag)
        run-fn #(set-flag flag)
        exec (executor)]
    (run-once exec run-fn 100)
    (sleep 300)
    (is (flag-set? flag))))

(deftest test-repeating-executor
  (let [cntr (counter)
        run-fn #(cntr inc)
        exec (executor)]
    (run-repeat exec run-fn 200)
    (sleep 500)
    (is (= 3 (cntr)))
    (sleep 200)
    (is (= 4 (cntr)))
    (shutdown-now exec)
    (sleep 400)
    (is (= 4 (cntr)))))

(deftest test-repeating-executor-2
  (let [cntr (counter)
        run-fn #(cntr inc)
        exec (executor)]
    (run-repeat exec run-fn 200)
    (sleep 500)
    (is (= 3 (cntr)))
    (sleep 200)
    (is (= 4 (cntr)))
    (shutdown exec)
    (sleep 400)
    (is (= 4 (cntr)))))



;;;
;;; Tests for object serialization.
;;;
(deftest test-object-to-byte-array
  (let [obj-int (int 123)
        obj-string "abc"
        ba-int (object-to-byte-array obj-int)
        ba-string (object-to-byte-array obj-string)]
    (is (not (nil? ba-int)))
    (is (not (nil? ba-string)))))

(deftest test-compress-object-to-byte-array
  (let [obj-int (int 123)
        obj-string "abc"
        ba-int (compress-object-to-byte-array obj-int :gzip)
        ba-string (compress-object-to-byte-array obj-string :gzip)]
    (is (not (nil? ba-int)))
    (is (not (nil? ba-string)))))



;;;
;;; Tests for reading input line-by-line
;;;
(deftest process-line-by-line-test
  (let [cntr (counter)]
    (is (= 0 (cntr)))
    (process-line-by-line "test-file.txt" (fn [_] (cntr inc)))
    (is (= 10 (cntr)))))

(deftest process-line-by-line-test2
  (let [cntr (counter)]
    (is (= 0 (cntr)))
    (process-line-by-line "test-file.txt" (fn [x] (cntr #(+ % (read-string x)))))
    (is (= 55 (cntr)))))



;;;
;;; Tests for converting Clojure specific data structures to their "pure" Java equivalents.
;;;
(deftest convert-from-clojure-to-java-list-test
  (let [in '("a" 1 1.23)
        expected (doto (ArrayList.) (.add "a") (.add 1) (.add 1.23))
        out (convert-from-clojure-to-java in)]
    (is (= expected out))
    (is (= java.util.ArrayList (type out)))))

(deftest convert-from-clojure-to-java-map-test
  (let [in {"a" 1 "b" 1.23}
        expected (doto (HashMap.) (.put "a" 1) (.put "b" 1.23))
        out (convert-from-clojure-to-java in)]
    (is (= expected out))
    (is (= java.util.HashMap (type out)))))

(deftest convert-from-clojure-to-java-set-test
  (let [in #{"a" 1 1.23}
        expected (doto (HashSet.) (.add "a") (.add 1) (.add 1.23))
        out (convert-from-clojure-to-java in)]
    (is (= expected out))
    (is (= java.util.HashSet (type out)))))

(deftest convert-from-clojure-to-java-vector-test
  (let [in ["a" 1 1.23]
        expected (doto (ArrayList.) (.add "a") (.add 1) (.add 1.23))
        out (convert-from-clojure-to-java in)]
    (is (= expected out))
    (is (= java.util.ArrayList (type out)))))

(deftest convert-from-clojure-to-java-nested-map-test
  (let [in {"a" {"b" 1}}
        expected-nested (doto (HashMap.) (.put "b" 1))
        expected (doto (HashMap.) (.put "a" expected-nested))
        out (convert-from-clojure-to-java in)]
    (is (= expected out))
    (is (= java.util.HashMap (type out)))
    (is (= expected-nested (.get out "a")))
    (is (= java.util.HashMap (type (.get out "a"))))))

(deftest convert-from-clojure-to-java-nested-set-test
  (let [in #{"a" #{1 1.23}}
        expected-nested (doto (HashSet.) (.add 1) (.add 1.23))
        expected (doto (HashSet.) (.add "a") (.add expected-nested))
        out (convert-from-clojure-to-java in)]
    (is (= expected out))
    (is (= java.util.HashSet (type out)))))

(deftest convert-from-clojure-to-java-nested-vector-test
  (let [in ["a" [1 1.23]]
        expected-nested (doto (ArrayList.) (.add 1) (.add 1.23))
        expected (doto (ArrayList.) (.add "a") (.add expected-nested))
        out (convert-from-clojure-to-java in)]
    (is (= expected out))
    (is (= java.util.ArrayList (type out)))
    (is (= expected-nested (.get out 1)))
    (is (= java.util.ArrayList (type (.get out 1))))))



;;;
;;; Tests for selectively accumulating map entries.
;;;
(deftest reduce-selected-map-entries-sum-test
  (let [in {"a" 1 "b" 2 "c" 3 "d" 4}]
    (is (= 6 (reduce-selected-map-entries in + ["a" "b" "c"])))))

(deftest reduce-selected-map-entries-sum-nil-test
  (let [in {"a" 1 "b" 2 "c" 3 "d" 4}]
    (is (= 7 (reduce-selected-map-entries in + ["c" "xyz" "d"])))))

(deftest reduce-selected-map-entries-concat-string-test
  (let [in {"a" 1 "b" 2 "c" 3 "d" 4}]
    (is (= "234" (reduce-selected-map-entries in str ["b" "c" "d"])))))

(deftest reduce-selected-map-entries-concat-string-nil-test
  (let [in {"a" 1 "b" 2 "c" 3 "d" 4}]
    (is (= "23" (reduce-selected-map-entries in str ["b" "abc" "c"])))))



;;;
;;; Tests for printing to stderr.
;;;
(deftest print-err-no-stdout-test
  (is (= "" (with-out-str (print-err "foo")))))

(deftest print-err-test
  (is (= "foo" (with-err-str (print-err "foo")))))

(deftest println-err-test
  (is (= "foo\n" (with-err-str (println-err "foo")))))

;;;
;;; Tests for with-out-str-custom that allows to execute a function on each added string.
;;;
(deftest with-out-str-custom-single-println-test
  (let [intercepted-input (atom "")
        write-fn (fn [s]
                   (swap! intercepted-input str s)
                   s)]
    (is (= "foo\n" (with-out-str-custom write-fn (println "foo"))))
    (is (= "foo\n" @intercepted-input))))

(deftest with-out-str-custom-double-println-test
  (let [intercepted-input (atom "")
        write-fn (fn [s]
                   (swap! intercepted-input str s)
                   s)]
    (is (= "foo\nbar\n" (with-out-str-custom write-fn (println "foo") (println "bar"))))
    (is (= "foo\nbar\n" @intercepted-input))))

(deftest with-out-str-custom-single-print-test
  (let [intercepted-input (atom "")
        write-fn (fn [s]
                   (swap! intercepted-input str s)
                   s)]
    (is (= "foo" (with-out-str-custom write-fn (print "foo"))))
    (is (= "foo" @intercepted-input))))

(deftest with-out-str-custom-double-print-test
  (let [intercepted-input (atom "")
        write-fn (fn [s]
                   (swap! intercepted-input str s)
                   s)]
    (is (= "foobar" (with-out-str-custom write-fn (print "foo") (print "bar"))))
    (is (= "foobar" @intercepted-input))))

(deftest with-out-str-custom-manipulated-string-test
  (let [intercepted-input (atom "")
        write-fn (fn [s]
                   (swap! intercepted-input str s)
                   (str s s))]
    (is (= "foofoo" (with-out-str-custom write-fn (print "foo"))))
    (is (= "foo" @intercepted-input))))

(deftest with-out-str-custom-nil-test
  (let [intercepted-input (atom "")
        write-fn (fn [s]
                   (swap! intercepted-input str s)
                   nil)]
    (is (= "" (with-out-str-custom write-fn (print "foo"))))
    (is (= "foo" @intercepted-input))))



;;;
;;; Tests for getting data from a Map with default values when the entry does not exist.
;;;
(deftest get-with-default-key-exists-test
  (let [m {"a" "A" "b" 6}]
    (is (= "A" (get-with-default m "a" "X")))
    (is (= 6 (get-with-default m "b" 1)))))

(deftest get-with-default-key-does-not-exist-test
  (let [m {"c" "C"}]
    (is (= "X" (get-with-default m "a" "X")))
    (is (= 1 (get-with-default m "b" 1)))))



;;;
;;; Tests for convenience functions for writing to files.
;;;
(def test-out-file "file-out.test.file")

(deftest simple-str-to-file-without-newline-test
  (rm test-out-file)
  (let [str-file-out (create-string-to-file-output test-out-file)]
    (str-file-out "my-string")
    (is (= "my-string" (slurp test-out-file)))
    (str-file-out)))

(deftest simple-str-to-file-with-newline-test
  (rm test-out-file)
  (let [str-file-out (create-string-to-file-output test-out-file {:insert-newline true})]
    (str-file-out "my-string")
    (is (= "my-string\n" (slurp test-out-file)))
    (str-file-out)))

(deftest simple-str-to-file-no-append-test
  (rm test-out-file)
  (let [str-file-out (create-string-to-file-output test-out-file)]
    (str-file-out "my-string")
    (is (= "my-string" (slurp test-out-file)))
    (str-file-out)
    ((create-string-to-file-output test-out-file) "my-new-string")
    (is (= "my-new-string" (slurp test-out-file)))))

(deftest simple-str-to-file-append-test
  (rm test-out-file)
  (let [str-file-out (create-string-to-file-output test-out-file)]
    (str-file-out "my-string")
    (is (= "my-string" (slurp test-out-file)))
    (str-file-out)
    ((create-string-to-file-output test-out-file {:append true}) "my-new-string")
    (is (= "my-stringmy-new-string" (slurp test-out-file)))))

(deftest simple-str-list-to-file-test
  (rm test-out-file)
  (let [str-file-out (create-string-to-file-output test-out-file)
        str-lst '("foo" "bar" "blah")]
    (str-file-out str-lst)
    (is (= "foobarblah" (slurp test-out-file)))
    (str-file-out)))

(deftest simple-map-to-file-test
  (rm test-out-file)
  (let [str-file-out (create-string-to-file-output test-out-file)
        str-lst {"foo" "bar"}]
    (str-file-out str-lst)
    (is (= "{\"foo\" \"bar\"}" (slurp test-out-file)))
    (str-file-out)))

(deftest simple-fifo-str-to-file-test
  (rm test-out-file)
  (mkfifo test-out-file)
  (let [str-file-out (create-string-to-file-output test-out-file {:append true :await-open false})]
    (run-once (executor) #(str-file-out "my-string\n") 100)
    (with-open [rdr (clojure.java.io/reader test-out-file)]
      (is (= "my-string" (first (line-seq rdr)))))
    (str-file-out)))

(deftest fifo-write-and-read-repeatedly-test
  (rm test-out-file)
  (mkfifo test-out-file)
  (let [str-file-out (create-string-to-file-output test-out-file {:append true :await-open false})
        ctr (counter)
        threaded-rdr (create-threaded-lineseq-reader test-out-file (fn [_] (ctr inc)))]
    (sleep 100)
    (str-file-out "my-string\n")
    (str-file-out "my-string\n")
    (str-file-out "my-string\n")
    (sleep 100)
    (is (= 3 (ctr)))
    (str-file-out)))

(deftest close-and-re-open-fifo-writer-test
  (rm test-out-file)
  (mkfifo test-out-file)
  (let [str-file-out (create-string-to-file-output test-out-file {:append true :await-open false})
        ctr (counter)
        threaded-rdr (create-threaded-lineseq-reader test-out-file (fn [_] (ctr inc)))]
    (sleep 100)
    (str-file-out "my-string\n")
    (sleep 100)
    (str-file-out)
    (sleep 100)
    (let [str-file-out-2 (create-string-to-file-output test-out-file {:append true :await-open false})]
      (sleep 100)
      (str-file-out-2 "my-string\n")
      (str-file-out-2 "my-string\n")
      (sleep 100)
      (str-file-out-2))
    (is (= 3 (ctr)))))

