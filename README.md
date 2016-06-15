# cheesecake

[![Build Status](https://travis-ci.org/maizy/cheesecake.svg?branch=master)](https://travis-ci.org/maizy/cheesecake)
[![Coverage Status](https://coveralls.io/repos/github/maizy/cheesecake/badge.svg?branch=master)](https://coveralls.io/github/maizy/cheesecake?branch=master)

_TBA_

## Compile standalone jar

```
sbt assembly
ls server/target/scala-2.11/cheesecake-server-assembly-*.jar
```

## Run in production

```
java -jar cheesecake-server-assembly-*.jar --help

java -jar cheesecake-server-assembly-*.jar \
    --config=path/to/config.conf \
    --host=0.0.0.0 \
    --port=8080
```

### Requirements

* jre 1.8+


## Dev mode

Dev requirements: scala 2.11.8, jdk 1.8, node 4.1+, npm 2.5+.

[Start stub server](server/etc/stub-server/README.md)

Run server in dev mode with sample configs
```
sbt -DassetsMode=dev \
    -Dlogback.configurationFile=server/src/main/etc/logback.dev.xml \
    'server/run --config=server/docs/sample-config.conf'
```

Compile assets:
`sbt server/compile:assets`

or start in the watch mode (after any changes happens assets will be automatically recompiled):
`sbt ~server/compile:assets`
