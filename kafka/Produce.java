package com.example.demo;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 *
 * Kafka集群高可用Produce端代码
 *
 * server.Properties
 *
 * ①、broker server config
 *
 * delete.topic.enable=true
 * min.insync.replicas=2
 * unclean.leader.election.enable=false
 *
 * ②、topic config
 *
 * unclean.leader.election.enable=false
 * min.insync.replicas=2
 *
 * ③、create topic
 *
 * bin/kafka-topics.sh --create --zookeeper 192.168.1.128:2181,192.168.1.81:2181,192.168.1.118:2181 --replication-factor 3 --partitions 6 --topic ChannelClick
 *
 * ④、topic describe
 *
 * bin/kafka-topics.sh --zookeeper 192.168.1.128:2181,192.168.1.81:2181,192.168.1.118:2181 --describe --topic ChannelClick
 * Topic:ChannelClick	PartitionCount:6	ReplicationFactor:3	Configs:unclean.leader.election.enable=false,min.insync.replicas=2
 * Topic: ChannelClick	Partition: 0	Leader: 1	Replicas: 1,3,2	Isr: 2,1,3
 * Topic: ChannelClick	Partition: 1	Leader: 2	Replicas: 2,1,3	Isr: 2,1,3
 * Topic: ChannelClick	Partition: 2	Leader: 3	Replicas: 3,2,1	Isr: 2,1,3
 * Topic: ChannelClick	Partition: 3	Leader: 1	Replicas: 1,2,3	Isr: 2,1,3
 * Topic: ChannelClick	Partition: 4	Leader: 2	Replicas: 2,3,1	Isr: 2,1,3
 * Topic: ChannelClick	Partition: 5	Leader: 3	Replicas: 3,1,2	Isr: 2,1,3
 *
 */
public class Produce {


    public static void main(String[] args) {

        Properties props = new Properties();
        props.put("bootstrap.servers", "192.168.1.128:9092,192.168.1.81:9092,192.168.1.118:9092");
        // 保证集群高可用数据不丢失核心参数
        props.put("acks", "all");
        // 不能保证短时间内集群恢复该重试参数尽可能设最大
        props.put("retries", Integer.MAX_VALUE);
        // 尽可能的保证顺序
        props.put("max.in.flight.requests.per.connection", 1);
        // 设置消息压缩算法
        props.put("compression.type", "snappy");
        props.put("batch.size", 16384);
        props.put("buffer.memory", 33554432);
        // key value序列化
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(props);

        for (int i = 1; i <= 10000; i++){

            String json = "{\"clientIP\":\"223.104.186.215\",\"fromIP\":\"127.0.0.1\",\"clickTime\":null,\"channelTime\":\"2018-04-20T15:22:48.902+0800\",\"callAdvTime\":null,\"delPlat\":\"ios\",\"delMode\":\"CPA\",\"sourceId\":39,\"orderId\":35,\"orderInputId\":42,\"ideaId\":null,\"advId\":44,\"proId\":36,\"landingPageId\":null,\"callAdvUrl\":\"https://itunes.apple.com/cn/app/id990531994?mt\\u003d8\",\"callChannelUrl\":null,\"repeatTime\":0,\"responseMsg\":\"防作弊校验失败！\",\"channelType\":\"CPA\",\"logType\":\"ChannelClick\",\"source\":\"changsi\",\"appid\":\"990531994\",\"scid\":\"jcdefault\",\"uuid\":\"CA2606BB-1917-4CCE-968F-U18ACLH3\",\"status\":12,\"cid\":null,\"sc_name\":null,\"orderSourceId\":44}";

            try {
                // 为保证数据不丢失，改异步发送为同步发送
                producer.send(new ProducerRecord<>("ChannelClick", json)).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        producer.close();

    }

}
