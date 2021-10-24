#!/bin/bash

set -e

# Change the current working directory to the location of the present file
cd "$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

./../docker/apache-maven-3.6.3/bin/mvn clean compile assembly:single
mv target/da_project-1.0-SNAPSHOT-jar-with-dependencies.jar bin/da_proc.jar
