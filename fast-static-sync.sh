#!/usr/bin/env bash
#
# A fast web-ui static sync in the dev env
#
PROJECT_ROOT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$PROJECT_ROOT"
DEST="./server/target/scala-2.11/classes/web-ui"
mkdir -p "$DEST" 2>&1 >/dev/null
cp -rf ./server/src/main/resources/web-ui "$DEST"
RES=$?
if [ "$RES" == "0" ]; then
    echo "Done"
else
    echo "Some errors"
fi

exit $RES
