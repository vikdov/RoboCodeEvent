#!/bin/bash
set -e

echo "Compiling..."
mkdir -p out
javac -d out src/Main.java

echo "Running..."
java -cp out Main
