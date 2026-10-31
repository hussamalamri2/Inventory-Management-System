#!/bin/bash
# compile and run the tests
cd "$(dirname "$0")"
mkdir -p out
javac -d out $(find src -name "*.java") test/AllTests.java && java -ea -cp out AllTests
