#!/usr/bin/env bash

export MY_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source $MY_DIR/include.sh

ZOOKEEPER_NODE="2"

if [ ! -f /tmp/zookeeper-${ZOOKEEPER_NODE}/myid ]; then
    echo "Creating myid file"
    cp -r ${ZOO_CONFIG_DIR}/tmp/zookeeper-${ZOOKEEPER_NODE} /tmp/
fi

export EXTRA_ARGS="-Djava.security.auth.login.config=${ZOO_CONFIG_DIR}/jaas.config"; \
    ${KAFKA_DIR}/bin/zookeeper-server-start.sh ${ZOO_CONFIG_DIR}/zookeeper-${ZOOKEEPER_NODE}.properties