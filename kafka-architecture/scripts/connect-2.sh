#!/usr/bin/env bash

export MY_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source $MY_DIR/include.sh

${KAFKA_DIR}/bin/connect-distributed.sh ${CONNECT_CONFIG_DIR}/worker-2.properties
