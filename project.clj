(defproject clj-assorted-utils "1.18.6"
;(defproject clj-assorted-utils "1.19.0-SNAPSHOT"
  :description "Unsorted bunch of helper and utility functions."
  :dependencies [[org.clojure/clojure "1.10.3"]]
  :global-vars {*warn-on-reflection* true}
  :html5-docs-docs-dir "ghpages/doc"
  :html5-docs-ns-includes #"^clj-assorted-utils.*"
  :html5-docs-repository-url "https://github.com/ruedigergad/clj-assorted-utils/blob/master"
  :test2junit-output-dir "ghpages/test-results"
  :test2junit-run-ant true
  :aot [clj-assorted-utils.JavaUtils]
  :plugins [[lein-cloverage "1.0.6"]]
  :profiles  {:repl  {:dependencies  [[jonase/eastwood "0.3.4" :exclusions  [org.clojure/clojure]]]}}
  :license {:name "Eclipse Public License (EPL) - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "This is the same license as used for Clojure."}
)
