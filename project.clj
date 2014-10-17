(defproject seqspert "1.6.0.0-SNAPSHOT"

  :description "Seqspert: Understand the internals of Clojure sequence implementations"

  :url "http://example.com/FIXME"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0-alpha2"]
                 [junit/junit "4.11"]]

  :plugins [[lein-test-out "0.3.1"]
            [lein-junit "1.1.7"]
            [lein-nodisassemble "0.1.3"]]

  :warn-on-reflection true

  :global-vars {*warn-on-reflection* true
                *assert* false
                *unchecked-math* true}

  :jvm-opts ["-Xms1g" "-Xmx4g" "-server" "-XX:+UsePopCountInstruction"]

  :main seqspert.all
  :aot :all

  :java-source-paths ["src/java" "test/java"]
  :junit ["test/java"]
  :junit-formatter "xml"
  :junit-results-dir "target"

  :source-paths ["src/clojure"]
  :test-paths ["test/clojure"]

  )
