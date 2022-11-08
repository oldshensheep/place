#!/bin/sh
mkdir ./src/main/resources/public
cp ./frontend/* ./src/main/resources/public -r
./gradlew build
rm ./src/main/resources/public -r
