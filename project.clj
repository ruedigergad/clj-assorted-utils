(defproject clj-assorted-utils "1.4.0"
;(defproject clj-assorted-utils "1.5.0-SNAPSHOT"
  :description "Unsorted bunch of helper and utility functions."
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :global-vars {*warn-on-reflection* true}
  :html5-docs-docs-dir "ghpages/doc"
  :html5-docs-ns-includes #"^clj-assorted-utils.*"
  :html5-docs-repository-url "https://github.com/ruedigergad/clj-assorted-utils/blob/master"
  :test2junit-output-dir "ghpages/test-results"
  :test2junit-run-ant true
)
