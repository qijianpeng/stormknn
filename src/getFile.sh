#!/bin/sh
hdfs dfs -get  hdfs://10.10.18.1:9000/storm/results/* ./
hdfs dfs -get  hdfs://10.10.18.1:9000/storm/timer/* ./
