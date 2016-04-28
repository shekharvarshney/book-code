#!/bin/bash

mvn clean install
if [[ "$?" -ne 0 ]]; then
      echo "ERROR: $1 project build failed. BUILD FAILED"; exit -1;
fi

echo "module built successfully"
