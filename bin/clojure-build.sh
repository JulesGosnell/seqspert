#!/bin/sh -x
exec lein do clean, javac, junit, test-out junit target/clojure-testreports.xml
