package com.jayway.perfectstorm.storm.bolt.tps;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;

import java.util.HashMap;
import java.util.Map;

public class PrintTweetsPerSecondBolt extends BaseRichBolt {

    private transient OutputCollector outputCollector;
    private transient HazelcastInstance hazelcast;
    private transient IQueue<Object> queue;

    @Override
    public void prepare(Map map, TopologyContext context, OutputCollector outputCollector) {
        this.outputCollector = outputCollector;
        hazelcast = Hazelcast.newHazelcastInstance();
        queue = hazelcast.getQueue("tweets-per-second");
    }

    @Override
    public void execute(Tuple tuple) {
        final Long tps = tuple.getLong(0);
        System.out.printf("Tweets per second: %d\n", tps);

        Map<Object, Object> event = buildEvent(tps);
        queue.offer(event);
        outputCollector.ack(tuple);
    }

    private Map<Object, Object> buildEvent(Long tps) {
        Map<Object, Object> event = new HashMap<>();
        Map<Object, Object> eventData = new HashMap<>();
        event.put("eventName", "tps");
        event.put("data", eventData);
        eventData.put("tps", tps);
        return event;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
    }

    @Override
    public void cleanup() {
        if (hazelcast != null) {
            hazelcast.getLifecycleService().shutdown();
        }
    }
}
