#!/bin/sh
hdfs dfs -rm -R  hdfs://10.10.18.1:9000/storm/results
hdfs dfs -rm -R  hdfs://10.10.18.1:9000/storm/data/.lock
hdfs dfs -rm -R  hdfs://10.10.18.1:9000/storm/timer
hdfs dfs -mv  hdfs://10.10.18.1:9000/storm/done/*  hdfs://10.10.18.1:9000/storm/data/

