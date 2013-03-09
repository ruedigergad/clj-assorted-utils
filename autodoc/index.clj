{:namespaces
 ({:source-url nil,
   :wiki-url "clj-assorted-utils.util-api.html",
   :name "clj-assorted-utils.util",
   :author "Ruediger Gad",
   :doc "Utility and helper functions"}),
 :vars
 ({:arglists ([byte-seq]),
   :name "byte-seq-to-int",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/byte-seq-to-int",
   :doc "Convert the byte sequence byte-seq to an integer.",
   :var-type "function",
   :line 251,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([v n f]),
   :name "change-int-in-byte-vector",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/change-int-in-byte-vector",
   :doc
   "In the given vector v, change the byte representation of an integer by applying function f.",
   :var-type "function",
   :line 294,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([o]),
   :name "classname",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/classname",
   :doc
   "Get classname without leading package names of the given object o.",
   :var-type "function",
   :line 230,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([obj] [obj alg]),
   :name "compress-object-to-byte-array",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/compress-object-to-byte-array",
   :doc
   "Write an object into a byte array and compress it.\nA newly allocated byte array with the serialized and compressed object is returned.\nThe compression algorithm can be specified via the second parameter.\nCurrently GZIP (:gzip) and ZIP (:zip) are supported.\nThe default is GZIP.",
   :var-type "function",
   :line 371,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([] [init]),
   :name "counter",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/counter",
   :doc
   "Convenience function for creating a more powerful counter.\nThe created counter allows to have arbitrary single-argument functions passed for manipulating the internal value.\nWhen the created counter is called with no argument the current value is returned.\nExample:\n(use 'clj-assorted-utils.util)\n(let [cntr (counter 123)]\n  (println (cntr))\n  (cntr inc)\n  (cntr dec)\n  (cntr #(- % 123))\n  (cntr))",
   :var-type "function",
   :line 203,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([d & body]),
   :name "delay-eval",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/delay-eval",
   :doc "Evaluates the supplied body with the given delay 'd' ([ms]).",
   :var-type "macro",
   :line 125,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([d]),
   :name "dir-exists?",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/dir-exists?",
   :doc "Returns true if f exists and is a directory.",
   :var-type "function",
   :line 93,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([cmd]),
   :name "exec",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/exec",
   :doc "Execute the given command.",
   :var-type "function",
   :line 27,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([cmd]),
   :name "exec-blocking",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/exec-blocking",
   :doc
   "Execute the given command and block until execution has finished.",
   :var-type "function",
   :line 32,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([cmd stdout-fn]),
   :name "exec-with-out",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/exec-with-out",
   :doc
   "Execute the given command and process the output written to stdout with stdout-fn.\nstdout-fn is run in an own thread.\nThe data written to stdout is passed to stdout-fn line-by-line.\nWhen a value of nil is read processing stops and the process in which cmd was executed is destroyed.",
   :var-type "function",
   :line 37,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([]),
   :name "executor",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/executor",
   :doc "Create an executor for executing fns in an own thread.",
   :var-type "function",
   :line 134,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([f]),
   :name "exists?",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/exists?",
   :doc "Returns true if f exists in the filesystem.",
   :var-type "function",
   :line 71,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([f]),
   :name "file-exists?",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/file-exists?",
   :doc "Returns true if f exists and is a file.",
   :var-type "function",
   :line 81,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([f]),
   :name "flag-set?",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/flag-set?",
   :doc
   "Test if flag had been set.\nReturns true if flag was set and false otherwise.",
   :var-type "function",
   :line 187,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([f]),
   :name "fn-name",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/fn-name",
   :doc "Get the name of the given fn f.",
   :var-type "function",
   :line 236,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([v n]),
   :name "get-int-from-byte-vector",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/get-int-from-byte-vector",
   :doc
   "From given vector of bytes v get the integer starting at offset n.\nStarting at offset n the next 4 bytes will be converted to an integer.",
   :var-type "function",
   :line 269,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([c]),
   :name "inc-counter",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/inc-counter",
   :doc "Increment the given simple counter c.",
   :var-type "function",
   :line 198,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([val]),
   :name "int-to-byte-vector",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/int-to-byte-vector",
   :doc "Convert the given integer val to a vector of 4 bytes.",
   :var-type "function",
   :line 276,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([d]),
   :name "is-dir?",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/is-dir?",
   :doc "Returns true if f is a directory.",
   :var-type "function",
   :line 88,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([f]),
   :name "is-file?",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/is-file?",
   :doc "Returns true if f is a file.",
   :var-type "function",
   :line 76,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([d]),
   :name "mkdir",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/mkdir",
   :doc
   "Create directory d and if required all parent directories.\nThis is equivalent to 'mkdir -p d' on Linux systems.",
   :var-type "function",
   :line 100,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([obj]),
   :name "object-to-byte-array",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/object-to-byte-array",
   :doc
   "Write the given object into a byte array.\nA newly allocated byte arry with the serialized object is returned.",
   :var-type "function",
   :line 362,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([] [init]),
   :name "prepare-counter",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/prepare-counter",
   :doc "Prepare a simple counter. Use @ to access the value.",
   :var-type "function",
   :line 193,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([]),
   :name "prepare-flag",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/prepare-flag",
   :doc "Prepare a flag with default value false.",
   :var-type "function",
   :line 176,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([& s]),
   :name "print-err",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/print-err",
   :doc "print to stderr.",
   :var-type "function",
   :line 345,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([& s]),
   :name "print-err-ln",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/print-err-ln",
   :doc "println to stderr.",
   :var-type "function",
   :line 351,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([location f]),
   :name "process-line-by-line",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/process-line-by-line",
   :doc
   "Process input from location line-by-line.\nEach line is passed to f.\nThe processing is done with a clojure reader.\nSo all locations supported by reader are automatically supported as well.",
   :var-type "function",
   :line 393,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([f]),
   :name "rm",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/rm",
   :doc "Delete file f.",
   :var-type "function",
   :line 106,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([d]),
   :name "rmdir",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/rmdir",
   :doc "Delete d if d is an empty directory.",
   :var-type "function",
   :line 111,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([exec f d] [exec f d tu]),
   :name "run-once",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/run-once",
   :doc
   "Run f using executor exec once with delay d.\nOptionally a time unit tu can be given.\ntu defaults to TimeUnit/MILLISECONDS.",
   :var-type "function",
   :line 149,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([exec f d] [exec f id d] [exec f id d tu]),
   :name "run-repeat",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/run-repeat",
   :doc
   "Run f repeatedly using executor exec with delay d.\nOptionally an initial delay id and a time unit tu can be given.\nTime unit is a static member of TimeUnit, e.g., TimeUnit/SECONDS \nand defaults to TimeUnit/MILLISECONDS.",
   :var-type "function",
   :line 158,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([f]),
   :name "set-flag",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/set-flag",
   :doc "Set flag to true.",
   :var-type "function",
   :line 181,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([exec]),
   :name "shutdown",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/shutdown",
   :doc "Shut executor down.",
   :var-type "function",
   :line 139,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([exec]),
   :name "shutdown-now",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/shutdown-now",
   :doc "Force executor shut down.",
   :var-type "function",
   :line 144,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([ms]),
   :name "sleep",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/sleep",
   :doc "Sleep for the given number of milliseconds.",
   :var-type "function",
   :line 22,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([k]),
   :name "stringify-keyword",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/stringify-keyword",
   :doc
   "If a keyword is passed returns the name of the keyword.\nElse the input is left unchanged.",
   :var-type "function",
   :line 314,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([m]),
   :name "stringify-map",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/stringify-map",
   :doc
   "Convert _all_ keywords in a map to their respective names.\nThis, essentially, is an extended version of clojure.walk/stringify-keys,\nwhich only converts the keys to strings.",
   :var-type "function",
   :line 322,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([f]),
   :name "touch",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/touch",
   :doc "If f does not exist, create it.",
   :var-type "function",
   :line 116,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([v n delta]),
   :name "vec-replace",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/vec-replace",
   :doc
   "In given vector v replace the content of v starting at index n with delta.",
   :var-type "function",
   :line 286,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([xml-str]),
   :name "xml-string-to-map",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/xml-string-to-map",
   :doc
   "Takes an XML definition in form of a string and outputs the corresponding map.",
   :var-type "function",
   :line 307,
   :file "src/clj_assorted_utils/util.clj"}
  {:arglists ([xml-str]),
   :name "xml-string-to-map-stringified",
   :namespace "clj-assorted-utils.util",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/clj-assorted-utils.util-api.html#clj-assorted-utils.util/xml-string-to-map-stringified",
   :doc
   "Convert the XML string xml-str to a map with all keywords converted to strings.",
   :var-type "function",
   :line 334,
   :file "src/clj_assorted_utils/util.clj"})}
