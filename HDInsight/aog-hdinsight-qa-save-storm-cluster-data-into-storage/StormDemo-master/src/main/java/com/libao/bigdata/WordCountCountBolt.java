package com.libao.bigdata;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.HashMap;
import java.util.Map;

public class WordCountCountBolt extends BaseRichBolt {

    private Map<String,Integer> result = new HashMap<>();

    private OutputCollector collector;

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void execute(Tuple tuple) {
        String word = tuple.getStringByField("word");
        int count = tuple.getIntegerByField("count");

        if(result.containsKey(word)){
            int total = result.get(word);
            result.put(word, total+count);
        }else{
            result.put(word, count);
        }

        System.out.println("word count ...."+ result);
        this.collector.emit(new Values(word,result.get(word)));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("word","total"));
    }
}
