#!/bin/bash
# compile and start the application
cd "$(dirname "$0")"
mkdir -p out
javac -d out $(find src -name "*.java") && java -cp out app.Main
