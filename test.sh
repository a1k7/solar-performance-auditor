#!/usr/bin/env bash
set -e

echo "Compiling main..."
mkdir -p out
javac -d out $(find src -name "*.java")

echo "Compiling tests..."
mkdir -p test-out
javac -cp out -d test-out $(find test -name "*.java")

echo "Running tests..."
java -cp out:test-out com.solar.test.TestRunner
