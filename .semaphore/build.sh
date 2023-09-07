#!/bin/bash

set -e
project_root=$(git rev-parse --show-toplevel)

source $project_root/.semaphore/cache-helpers.sh

restore-gradle-cache

# build jars and run unit tests
./gradlew build --info --stacktrace

store-gradle-cache
