# cheesecake

[![Build Status](https://travis-ci.org/maizy/cheesecake.svg?branch=master)](https://travis-ci.org/maizy/cheesecake)
[![Coverage Status](https://coveralls.io/repos/github/maizy/cheesecake/badge.svg?branch=master)](https://coveralls.io/github/maizy/cheesecake?branch=master)

Service monitoring tool.


## Run as standalone jar

[Download cheesecake-server-assembly.jar](#TODO)

Create `cheesecake.conf`. See an example config & format description into
[`server/docs/sample-config/cheesecake.conf`](server/docs/sample-config/cheesecake.conf)

```
java -jar cheesecake-server-assembly.jar --help

java -jar cheesecake-server-assembly.jar \
    --config=path/to/cheesecake.conf \
    --host=0.0.0.0 \
    --port=8000
```

Requirements: jre 1.8+


## Run in docker container

Create config dir, put `cheesecake.conf` into it.

See an example config & format description into
[`server/docs/sample-config/cheesecake.conf`](server/docs/sample-config/cheesecake.conf)

```
docker run \
    -p 8000:52022 \
    --volume=/path/to/configdir:/configs \
    maizy/cheesecake-server:latest
```


## Dev mode

Dev requirements: scala 2.11.8, jdk 1.8, node 4.1+, npm 2.5+.

[Start stub server](server/etc/stub-server/README.md)

Run server in dev mode with sample configs
```
sbt -DassetsMode=dev \
    -Dlogback.configurationFile=server/src/main/etc/logback.dev.xml \
    'server/run --config=server/docs/sample-config/cheesecake.conf'
```

Compile assets:
`sbt server/compile:assets`

or start in the watch mode (after any changes happens assets will be automatically recompiled):
`sbt ~server/compile:assets`

## Compile standalone jar

```
sbt assembly
ls server/target/scala-2.11/cheesecake-server-assembly-*.jar
```
