#!/usr/bin/env bash
#
# run tests on travis-ci
#
PROJECT_ROOT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $PROJECT_ROOT'/server'
echo 'run tests in '`pwd`
sbt ++$TRAVIS_SCALA_VERSION test
exit $?
