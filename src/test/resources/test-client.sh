#!/bin/bash
# Required HTTPie installed (https://httpie.io/)
for i in $(seq 1 100);
do
    (
    echo $i
    uuid=$(uuidgen)
    http GET :8080/test-client X-Amzn-Trace-Id:"$uuid" ) &
done