#!/bin/bash
#
# Builds the gRPC/protobuf (proto3) classes from the .proto.  For:
# 1. Java (intel only)
#    i. this requires the java grpc codegen plugin not available on ARM
# 2. Python
#

# current working directory
project_base="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
echo $project_base

# define protoc to use - otherwise it uses your env path
# PROTOC_HOME=/opt/homebrew/Cellar/protobuf/21.5
PROTOC_HOME=C:\protoc-21.6-win64

# python
py_generated_base=${project_base}/python/generated
if [ -d ${py_generated_base} ]; then
  echo -e "\n* removing contents of ${py_generated_base}"
  rm -r ${py_generated_base}/*
else
  echo -e "\n* creating directory ${py_generated_base}"
  mkdir ${py_generated_base}
fi

# java
generated_base=${project_base}/generated
# java_codegen=/Users/gash/Downloads/protoc-gen-grpc-java-1.44.1-osx-aarch_64.exe
java_codegen=C:\Users\saikr\Downloads\CMPE275Labs\CMPE-275-GrandChallenge\protoc-gen-grpc-java-1.49.0-windows-x86_64.exe

if [ -d ${generated_base} ]; then
  echo -e "\n* removing contents of ${generated_base}"
#  rm -r ${generated_base}/*
else
  echo -e "\n* creating directory ${generated_base}"
  mkdir "${generated_base}"
fi

# ---------------------------------------------------------
# for all .proto files in resources

for p in `ls ${project_base}/resources/*.proto`; do
   base=$(basename $p |cut -f1 -d'.')
   echo -e "\n* compiling: $base"
   echo -e "  -- java"
   $PROTOC_HOME/bin/protoc \
        --proto_path=${project_base}/resources \
        --java_out=${generated_base} \
        --plugin=protoc-gen-grpc-java=${java_codegen} \
        --grpc-java_out=${generated_base} $p

   echo -e "\n  -- python"
   $PROTOC_HOME/bin/protoc \
        --proto_path=${project_base}/resources \
        --python_out=${py_generated_base} $p
done

echo -e "\n* java done, created:\n"
find ${generated_base} -name "*.java" | xargs basename
echo -e "\n"

echo -e "\n* python done, created:\n"
find ${py_generated_base} -name "*.py" | xargs basename
echo -e "\n"

