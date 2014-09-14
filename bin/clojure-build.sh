#!/bin/sh -x
exec lein do clean, test, test-out junit target/test-results.xml
