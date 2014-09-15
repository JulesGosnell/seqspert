#!/bin/sh -x
exec mvn clean cobertura:clean test cobertura:cobertura $@
