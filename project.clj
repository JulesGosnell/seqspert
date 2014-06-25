(defproject seqspert "1.6.0.0-SNAPSHOT"

  :description "Seqspert: Understand the internals of Clojure sequence implementations"

  :url "http://example.com/FIXME"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.6.0"]]

  :plugins [[lein-nodisassemble "0.1.3"]]

  :warn-on-reflection true

  :global-vars {*warn-on-reflection* true
                *assert* false
                *unchecked-math* true}

  :jvm-opts ["-Xms1g" "-Xmx1g" "-server" "-XX:+UsePopCountInstruction"]

  :main seqspert.all

  :java-source-paths ["src/java"]

  :source-paths ["src" "src/clojure"]
  :test-paths ["test" "test/clojure"]

  )
