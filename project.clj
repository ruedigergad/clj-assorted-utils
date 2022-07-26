;(defproject clj-assorted-utils "1.19.0"
(defproject clj-assorted-utils "1.19.1-SNAPSHOT"
  :description "Unsorted bunch of helper and utility functions."
  :dependencies [[org.clojure/clojure "1.11.1"]]
  :global-vars {*warn-on-reflection* true}
  :html5-docs-docs-dir "docs/doc"
  :html5-docs-ns-includes #"^clj-assorted-utils.*"
  :html5-docs-repository-url "https://github.com/ruedigergad/clj-assorted-utils/blob/master"
  :test2junit-output-dir "docs/test-results"
  :test2junit-run-ant true
  :aot [clj-assorted-utils.JavaUtils]
  :plugins [[lein-cloverage "1.0.9"] [test2junit "1.4.2"] [lein-html5-docs "3.0.3"]]
  :profiles  {:repl  {:dependencies  [[jonase/eastwood "1.2.4" :exclusions  [org.clojure/clojure]]]}}
  :license {:name "Eclipse Public License (EPL) - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "This is the same license as used for Clojure."}
)
