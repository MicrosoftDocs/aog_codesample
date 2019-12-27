package com.libao.bigdata;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

import java.util.Map;
import java.util.Random;

public class WordCountSpout extends BaseRichSpout {
    private SpoutOutputCollector collector;

    private static String[] data = {"I love Beijing","I love China","Beijing is the capital of China"};

    @Override
    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void nextTuple() {
        Utils.sleep(3000);

        int random = (new Random()).nextInt(3);

        String sentence = data[random];

        this.collector.emit(new Values(sentence));

        System.out.println("Spout data ..."+ sentence);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("sentence"));
    }
}
