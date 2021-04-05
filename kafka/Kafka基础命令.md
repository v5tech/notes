# Kafka基础命令

### 官方&中文文档&docker

https://kafka.apache.org

http://kafka.apachecn.org

https://github.com/wurstmeister/kafka-docker

### 监控管理工具

* KafkaOffsetMonitor

  https://github.com/quantifind/KafkaOffsetMonitor

```bash
java -cp KafkaOffsetMonitor-assembly-0.2.1.jar \
     com.quantifind.kafka.offsetapp.OffsetGetterWeb \
     --zk 192.168.1.128:2181,192.168.1.81:2181,192.168.1.118:2181 \
     --port 8080 \
     --refresh 10.seconds \
     --retain 7.days
```

* kafka-manager

  https://github.com/yahoo/kafka-manager

  https://github.com/sheepkiller/kafka-manager-docker

```bash
docker run \
     -it \
     --rm \
     -p 9000:9000 \
     -e ZK_HOSTS="192.168.1.128:2181,192.168.1.81:2181,192.168.1.118:2181" \
     sheepkiller/kafka-manager
```

* kafka-eagle-web

  https://github.com/smartloli/kafka-eagle

  https://ke.smartloli.org/2.Install/2.Installing.html

  http://192.168.1.128:8048/ke   admin/123456

### 常用操作

启动内置的`zookeeper`

```
bin/zookeeper-server-start.sh config/zookeeper.properties
```

启动`kafka`

```
# 开启Kafka JMX监控端口
set JMX_PORT=9999 # windows
export JMX_PORT=9999 # linux
# 启动Kafka
bin/kafka-server-start.sh config/server.properties
```

创建topic

```
bin/kafka-topics.sh --create --zookeeper 192.168.1.128:2181,192.168.1.81:2181,192.168.1.118:2181 --replication-factor 1 --partitions 1 --topic test
```

```
bin/kafka-topics.sh --create --zookeeper 192.168.1.128:2181,192.168.1.81:2181,192.168.1.118:2181 --replication-factor 2 --partitions 6 --topic ChannelClick
```

列出所有的topic

```
bin/kafka-topics.sh --list --zookeeper 192.168.1.128:2181,192.168.1.81:2181,192.168.1.118:2181
ChannelClick
__consumer_offsets
```

查看指定topic的详情

```
bin/kafka-topics.sh --zookeeper 192.168.1.128:2181,192.168.1.81:2181,192.168.1.118:2181 --describe --topic ChannelClick
Topic:ChannelClick	PartitionCount:6	ReplicationFactor:3	Configs:unclean.leader.election.enable=false,min.insync.replicas=2
	Topic: ChannelClick	Partition: 0	Leader: 1	Replicas: 1,3,2	Isr: 1
	Topic: ChannelClick	Partition: 1	Leader: 1	Replicas: 2,1,3	Isr: 1
	Topic: ChannelClick	Partition: 2	Leader: 1	Replicas: 3,2,1	Isr: 1
	Topic: ChannelClick	Partition: 3	Leader: 1	Replicas: 1,2,3	Isr: 1
	Topic: ChannelClick	Partition: 4	Leader: 1	Replicas: 2,3,1	Isr: 1
	Topic: ChannelClick	Partition: 5	Leader: 1	Replicas: 3,1,2	Isr: 1
```

启动控制台生产者

```
bin/kafka-console-producer.sh --broker-list 192.168.1.128:9092,192.168.1.81:9092,192.168.1.118:9092 --topic test
```

启动控制台消费者

```
bin/kafka-console-consumer.sh --bootstrap-server 192.168.1.128:9092,192.168.1.81:9092,192.168.1.118:9092 --topic test --from-beginning
```

```
bin/kafka-console-consumer.sh --bootstrap-server 192.168.1.128:9092,192.168.1.81:9092,192.168.1.118:9092 --topic ChannelClick --from-beginning
```

查看kafka topic消息数量

```
bin/kafka-run-class.sh kafka.tools.GetOffsetShell --broker-list 192.168.1.128:9092,192.168.1.81:9092,192.168.1.118:9092 --topic ChannelClick --time -1
```

删除topic

```
bin/kafka-topics.sh --delete --zookeeper 192.168.1.128:2181,192.168.1.81:2181,192.168.1.118:2181 --topic ChannelClick
```

