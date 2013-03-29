(defproject clj-assorted-utils "1.2.0"
;(defproject clj-assorted-utils "1.3.0-SNAPSHOT"
  :description "Unsorted bunch of helper and utility functions."
  :dependencies [[org.clojure/clojure "1.4.0"]]
  :dev-dependencies [[lein-autodoc "0.9.0"]]
  :autodoc {:copyright "Copyright 2012, 2013 Ruediger Gad --
                        clj-assorted-utils is released under terms of the Eclipse Public License, the same as Clojure." }
  :html5-docs-docs-dir "autodoc"
  :html5-docs-ns-includes #"^clj-assorted-utils.*"
  :html5-docs-repository-url "https://github.com/ruedigergad/clj-assorted-utils/blob/master"
)
