# cheesecake

[![Build Status](https://travis-ci.org/maizy/cheesecake.svg?branch=master)](https://travis-ci.org/maizy/cheesecake)
[![Coverage Status](https://coveralls.io/repos/github/maizy/cheesecake/badge.svg?branch=master)](https://coveralls.io/github/maizy/cheesecake?branch=master)

_TBA_

## Run tests

`sbt test`


## Start the server in dev mode

Dev requirements: node 4.1+, npm 2.5+.

Run server in dev mode
`sbt -DassetsMode=dev -Dlogback.configurationFile=server/src/main/etc/logback.dev.xml server/run`

Compile assets:
`sbt server/compile:assets`

or start in the watch mode (after any changes happens assets will be automatically recompiled):
`sbt ~server/compile:assets`


## Compile all artifacts

```
sbt assembly
ls */target/scala-2.11/cheesecake*assembly*.jar
```
