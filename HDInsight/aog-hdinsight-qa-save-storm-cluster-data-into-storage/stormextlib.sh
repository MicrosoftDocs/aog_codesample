#!/bin/bash -x
sudo cp -f /usr/hdp/current/hadoop-client/*.jar /usr/hdp/current/storm-client/extlib
sudo cp -f /usr/hdp/current/hadoop-client/lib/*.jar /usr/hdp/current/storm-client/extlib
sudo cp -f /usr/hdp/current/hadoop-hdfs-client/*.jar /usr/hdp/current/storm-client/extlib
sudo cp -f /usr/hdp/current/hadoop-hdfs-client/lib/*.jar /usr/hdp/current/storm-client/extlib
sudo cp -f /usr/hdp/current/storm-client/contrib/storm-hbase/storm-hbase*.jar /usr/hdp/current/storm-client/extlib
sudo cp -f /usr/hdp/current/phoenix-client/lib/phoenix*.jar /usr/hdp/current/storm-client/extlib
sudo cp -f /usr/hdp/current/hbase-client/lib/hbase*.jar /usr/hdp/current/storm-client/extlib