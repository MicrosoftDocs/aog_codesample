package com.libao.bigdata;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.hdfs.bolt.HdfsBolt;
import org.apache.storm.hdfs.bolt.format.DefaultFileNameFormat;
import org.apache.storm.hdfs.bolt.format.DelimitedRecordFormat;
import org.apache.storm.hdfs.bolt.format.FileNameFormat;
import org.apache.storm.hdfs.bolt.format.RecordFormat;
import org.apache.storm.hdfs.bolt.rotation.FileRotationPolicy;
import org.apache.storm.hdfs.bolt.rotation.FileSizeRotationPolicy;
import org.apache.storm.hdfs.bolt.sync.CountSyncPolicy;
import org.apache.storm.hdfs.bolt.sync.SyncPolicy;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

public class WordCountTopology{

    public static void main(String[] args) throws Exception {
        TopologyBuilder builder = new TopologyBuilder();



        builder.setSpout("wordcount_spout", new WordCountSpout());


        builder.setBolt("wordcount_splitbolt", new WordCountSplitBolt())
                .shuffleGrouping("wordcount_spout");

        builder.setBolt("wordcount_countbolt", new WordCountCountBolt())
                .fieldsGrouping("wordcount_splitbolt", new Fields("word"));

		builder.setBolt("wordcount_hdfsbolt", createHDFSBolt())
				.shuffleGrouping("wordcount_countbolt");

        StormTopology wc = builder.createTopology();

        Config conf = new Config();

        //If there are arguments, we are running on a cluster
        if (args != null && args.length > 0) {
            //parallelism hint to set the number of workers
            conf.setNumWorkers(3);
            //submit the topology
            StormSubmitter.submitTopology(args[0], conf, builder.createTopology());
        }
        //Otherwise, we are running locally
        else {
            //Cap the maximum number of executors that can be spawned
            //for a component to 3
            conf.setMaxTaskParallelism(3);
            //LocalCluster is used to run locally
            LocalCluster cluster = new LocalCluster();
            //submit the topology
            cluster.submitTopology("word-count", conf, builder.createTopology());
            //sleep
            Thread.sleep(10000);
            //shut down the cluster
            cluster.shutdown();
        }
    }

    private static IRichBolt createHDFSBolt() {
//        HdfsBolt bolt = new HdfsBolt();
//        bolt.withFsUrl("wasb:///");
//
//        bolt.withFileNameFormat(new DefaultFileNameFormat().withPath("/stormdata"));
//
//        bolt.withRecordFormat(new DelimitedRecordFormat().withFieldDelimiter("|"));
//
//        bolt.withRotationPolicy(new FileSizeRotationPolicy(5.0f, FileSizeRotationPolicy.Units.MB));
//
//        bolt.withSyncPolicy(new CountSyncPolicy(1000));

        // use "|" instead of "," for field delimiter
        RecordFormat format = new DelimitedRecordFormat()
                .withFieldDelimiter("|");

// sync the filesystem after every 1k tuples
        SyncPolicy syncPolicy = new CountSyncPolicy(100);

// rotate files when they reach 5MB
        FileRotationPolicy rotationPolicy = new FileSizeRotationPolicy(1.0f, FileSizeRotationPolicy.Units.KB);

        FileNameFormat fileNameFormat = new DefaultFileNameFormat()
                .withPath("/foo/");

        HdfsBolt bolt = new HdfsBolt()
                .withFsUrl("wasb:///")
                .withFileNameFormat(fileNameFormat)
                .withRecordFormat(format)
                .withRotationPolicy(rotationPolicy)
                .withSyncPolicy(syncPolicy);

        return bolt;
    }
}
