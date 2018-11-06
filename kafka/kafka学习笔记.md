

# kafka学习笔记

![http://kafka.apachecn.org/10/images/kafka-apis.png](http://kafka.apachecn.org/10/images/kafka-apis.png)



### 1 kafka拓扑架构

- Broker

  Kafka集群包含一个或多个服务器，这种服务器被称为broker

- Topic

  每条发布到Kafka集群的消息都有一个类别，这个类别被称为Topic。（物理上不同Topic的消息分开存储，逻辑上一个Topic的消息虽然保存于一个或多个broker上但用户只需指定消息的Topic即可生产或消费数据而不必关心数据存于何处）

- Partition

  Parition是物理上的概念，每个Topic包含一个或多个Partition.

- Producer

  负责发布消息到Kafka broker

- Consumer

  消息消费者，向Kafka broker读取消息的客户端。

- Consumer Group

  每个Consumer属于一个特定的Consumer Group（可为每个Consumer指定group name，若不指定group name则属于默认的group）。


* Topic & Partition

​	Topic在逻辑上可以理解为是一个queue，每条消费都必须指定它的Topic，可以简单理解为必须指明把这条消息放进哪个queue里。为了使得Kafka的吞吐率可以线性提高，物理上把Topic分成一个或多个Partition，每个Partition在物理上对应一个文件夹，该文件夹下存储这个Partition的所有消息和索引文件。​	

​	一个topic为一类消息，每条消息必须指定一个topic。物理上，一个topic分成一个或多个partition，每个partition有多个副本分布在不同的broker中。

### 2 kafka无消息丢失配置

* broker

  ```
  delete.topic.enable=true
  min.insync.replicas=2
  unclean.leader.election.enable=false
  ```

  unclean.leader.election.enable=false

  关闭unclean leader选举，即不允许非ISR中的副本被选举为leader，从而避免broker端因日志水位截断而造成的消息丢失。

  replication.factor>=3

  设置成3主要是参考了Hadoop及业界通用的三备份原则，其实这里想强调的是一定要使用多个副本来保存分区的消息。

  min.insync.replicas>1

  用于控制某条消息至少被写入到ISR中的多少个副本才算成功，设置成大于1是为了提升producer端发送语义的持久性。记住只有在producer端acks被设置成all或-1时，这个参数才有意义。在实际使用时，不要使用默认值。

  确保replication.factor>min.insync.replicas若两者相等，那么只要有一个副本挂掉，分区就无法正常工作，虽然有很高的持久性但可用性被极大地降低了。推荐配置成 replication.factor=min.insyn.replicas+1。

* produce

  block.on.buffer.full = true  

​	尽管该参数在0.9.0.0已经被标记为“deprecated”，但鉴于它的含义非常直观，所以这里还是显式设置它为true，使得producer将一直等待缓冲区直至其变为可用。否则如果producer生产速度过快耗尽了缓冲区，producer将抛出异常

  acks=all  

​	很好理解，所有follower都响应了才认为消息提交成功，即"committed"

  retries = MAX 

​	无限重试，直到你意识到出现了问题:

  max.in.flight.requests.per.connection = 1 

​	限制客户端在单个连接上能够发送的未响应请求的个数。设置此值是1表示kafka broker在响应请求之前client不能再向同一个broker发送请求。注意：设置此参数是为了避免消息乱序

  使用KafkaProducer.send(record, callback)而不是send(record)方法   自定义回调逻辑处理消息发送失败callback逻辑中最好显式关闭producer：close(0) 注意：设置此参数是为了避免消息乱序

  unclean.leader.election.enable=false   

​	关闭unclean leader选举，即不允许非ISR中的副本被选举为leader，以避免数据丢失

  replication.factor >= 3   

​	这个完全是个人建议了，参考了Hadoop及业界通用的三备份原则

  min.insync.replicas > 1 

​	消息至少要被写入到这么多副本才算成功，也是提升数据持久性的一个参数。与acks配合使用.保证replication.factor > min.insync.replicas  如果两者相等，当一个副本挂掉了分区也就没法正常工作了。通常设置replication.factor = min.insync.replicas + 1即可

* consume

consumer端丢失消息的情形比较简单：如果在消息处理完成前就提交了offset，那么就有可能造成数据的丢失。由于Kafka consumer默认是自动提交位移的，所以在后台提交位移前一定要保证消息被正常处理了，因此不建议采用很重的处理逻辑，如果处理耗时很长，则建议把逻辑放到另一个线程中去做。为了避免数据丢失，现给出两点建议：

  ```
  enable.auto.commit=false  关闭自动提交位移
  在消息被完整处理之后再手动提交位移
  ```

### 3 代码实战部分

#### 3.1 Spring-Boot集成Kafka

* 添加依赖

  ```xml
  <dependency>
      <groupId>org.springframework.kafka</groupId>
      <artifactId>spring-kafka</artifactId>
  </dependency>
  
  <dependency>
      <groupId>org.apache.kafka</groupId>
      <artifactId>kafka-clients</artifactId>
      <version>1.1.0</version>
  </dependency>
  ```

* application.xml中配置

  ```
  spring:
    kafka:
      topic: Channeltopic
      bootstrap-servers: 10.81.128.114:9092,10.81.128.213:9092,10.81.128.163:9092
      producer:
        key-serializer: org.apache.kafka.common.serialization.StringDeserializer
        value-serializer: org.apache.kafka.common.serialization.StringDeserializer
        batch-size: 16384
        buffer-memory: 33554432
  ```

* kakfa配置



  KafkaConfigProperties.java

  ```java
  import lombok.AllArgsConstructor;
  import lombok.Data;
  import lombok.NoArgsConstructor;
  import org.springframework.beans.factory.annotation.Value;
  import org.springframework.stereotype.Component;
  
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Component
  public class KafkaConfigProperties {
  
      @Value("${spring.kafka.bootstrap-servers}")
      private String brokerAddress;
  
      @Value("${spring.kafka.producer.batch-size}")
      private String batchSize;
  
      @Value("${spring.kafka.producer.buffer-memory}")
      private String bufferMemory;
  
      @Value("${spring.kafka.topic}")
      private String kafkaTopic;
  
  }
  ```

  KafkaConfig.java

  ```java
  import org.apache.kafka.clients.producer.KafkaProducer;
  import org.apache.kafka.clients.producer.Producer;
  import org.apache.kafka.clients.producer.ProducerConfig;
  import org.apache.kafka.clients.producer.ProducerRecord;
  import org.apache.kafka.common.serialization.StringSerializer;
  import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.context.annotation.Bean;
  import org.springframework.stereotype.Component;
  
  import java.util.HashMap;
  import java.util.Map;
  
  @Component
  public class KafkaConfig {
  
      @Autowired
      private KafkaConfigProperties configProperties;
  
      @Bean
      public Producer<String, String> producer() {
  
          Map<String, Object> props = new HashMap<>();
  
          // Kafka Broker机器地址
          props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.configProperties.getBrokerAddress());
  
          props.put(ProducerConfig.LINGER_MS_CONFIG, 100);
  
          // 不能保证短时间内集群恢复该重试参数尽可能设最大
          props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
  
          // 保证集群高可用数据不丢失核心参数
          props.put(ProducerConfig.ACKS_CONFIG, "all");
  
          // 尽可能的保证顺序，防止topic同分区下的消息乱序
          props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
  
          // 设置消息压缩算法 lz4 > snappy > gzip
          props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
  
          props.put(ProducerConfig.BATCH_SIZE_CONFIG, configProperties.getBatchSize());
          props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, configProperties.getBufferMemory());
          props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
          props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
  
          // 以下参数配置为broke端配置
  
          // props.put("delete.topic.enable", true);
          //
          // // 控制某条消息至少被写入多少ISR副本才算成功 该参数只有配合acks才生效
          // props.put("min.insync.replicas", 2);
          //
          // // 设置副本数 务必确保 replication.factor > min.insync.replicas 推荐配置为 replication.factor = min.insync.replicas + 1
          // props.put("replication.factor",3);
          //
          // // 关闭unclean leader选举 不允许非ISR中的副本被选举为leader 从而避免broker端造成消息丢失
          // props.put("unclean.leader.election.enable", false);
  
          Producer<String, String> producer = new KafkaProducer<>(props);
  
          return producer;
      }
  
      /**
       * 为保证消息的可靠性，发送方式采用同步发送
       * @param topic
       * @param message
       */
      public void send(String topic, String message){
          try {
              producer().send(new ProducerRecord<>(topic, message), (metadata, exception) -> {
                  if(exception!=null){
                      System.out.println(metadata.timestamp() + "," + metadata.topic() + "," + metadata.partition() + "," + metadata.offset());
                      exception.printStackTrace();
                  }
              });
          } catch (Exception e) {
              e.printStackTrace();
          }
      }
  
  }
  ```

在需要发消息的地址注入`KafkaConfig`中的`Producer`使用内部声明的`send()`方法发送消息

#### 3.2 JAVA API代码示例

消息生产者

```java
package com.example.demo;

import org.apache.kafka.clients.producer.*;

import java.util.Properties;

/**
 *
 * Kafka集群高可用Produce端代码
 *
 * server.properties
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
        props.put("bootstrap.servers", "47.100.76.107:9092,47.100.76.181:9092,47.100.76.204:9092");
        // 保证集群高可用数据不丢失核心参数
        props.put("acks", "all");
        props.put("linger.ms", 100);
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

        // 不确定以下参数在代码端配置是否生效。为了保险起见还是写上
        props.put("delete.topic.enable", true);
        props.put("min.insync.replicas", 2);
        props.put("unclean.leader.election.enable", false);

        Producer<String, String> producer = new KafkaProducer<>(props);

        for (int i = 1; i <= 1000000; i++) {

            String json = "{\"clientIP\":\"223.104.186.215\",\"fromIP\":\"127.0.0.1\",\"clickTime\":null,\"channelTime\":\"2018-04-20T15:22:48.902+0800\",\"callAdvTime\":null,\"delPlat\":\"ios\",\"delMode\":\"CPA\",\"sourceId\":39,\"orderId\":35,\"orderInputId\":42,\"ideaId\":null,\"advId\":44,\"proId\":36,\"landingPageId\":null,\"callAdvUrl\":\"https://itunes.apple.com/cn/app/id990531994?mt\\u003d8\",\"callChannelUrl\":null,\"repeatTime\":0,\"responseMsg\":\"防作弊校验失败！\",\"channelType\":\"CPA\",\"logType\":\"ChannelClick\",\"source\":\"changsi\",\"appid\":\"990531994\",\"scid\":\"jcdefault\",\"uuid\":\"CA2606BB-1917-4CCE-968F-U18ACLH3\",\"status\":12,\"cid\":null,\"sc_name\":null,\"orderSourceId\":44}";

            producer.send(new ProducerRecord<>("topic-1031", json), (metadata, exception) -> {
                if (exception!=null) {
                    exception.printStackTrace();
                }
            });

        }

        producer.close();

    }

}
```

消费者

```java
package com.example.demo;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

public class Consumer {

    public static void main(String[] args) throws Exception {

        AtomicLong atomicLong = new AtomicLong(0);

        Properties props = new Properties();
        props.put("bootstrap.servers", "47.100.76.107:9092,47.100.76.181:9092,47.100.76.204:9092");
        props.put("group.id", "test");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("auto.offset.reset", "earliest");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        consumer.subscribe(Arrays.asList("topic-1031"));

        // 注册JVM关闭时的回调钩子，当JVM关闭时调用此钩子。
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Starting exit...");
            //调用消费者的wakeup方法通知主线程退出
            consumer.wakeup();
            try {
                //等待主线程退出
                Thread.currentThread().join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));


        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(100);
                for (ConsumerRecord<String, String> record : records){
                    System.out.println("-------------->"+atomicLong.incrementAndGet());
                    System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
                }
            }
        } catch (WakeupException e) {
            e.printStackTrace();
        } finally {
            consumer.close();
        }



    }
}
```



### 4 kafka常用操作命令

#### 4.1 启动kafka

```bash
# 开启Kafka JMX监控端口
set JMX_PORT=9999 # windows
export JMX_PORT=9999 # linux
# 启动Kafka
bin/kafka-server-start.sh config/server.properties
```

#### 4.2 创建topic

```bash
bin/kafka-topics.sh --create --zookeeper 10.81.128.114:2181,10.81.128.213:2181,10.81.128.163:2181 --replication-factor 1 --partitions 1 --topic test
```

```bash
bin/kafka-topics.sh --create --zookeeper 10.81.128.114:2181,10.81.128.213:2181,10.81.128.163:2181 --replication-factor 2 --partitions 6 --topic ChannelClick
```

#### 4.3 列出所有的topic

```bash
bin/kafka-topics.sh --list --zookeeper 10.81.128.114:2181,10.81.128.213:2181,10.81.128.163:2181
ChannelClick
__consumer_offsets
```

#### 4.4 查看指定topic的详情

```bash
bin/kafka-topics.sh --zookeeper 10.81.128.114:2181,10.81.128.213:2181,10.81.128.163:2181 --describe --topic ChannelClick
Topic:ChannelClick	PartitionCount:6	ReplicationFactor:3	Configs:unclean.leader.election.enable=false,min.insync.replicas=2
	Topic: ChannelClick	Partition: 0	Leader: 1	Replicas: 1,3,2	Isr: 1
	Topic: ChannelClick	Partition: 1	Leader: 1	Replicas: 2,1,3	Isr: 1
	Topic: ChannelClick	Partition: 2	Leader: 1	Replicas: 3,2,1	Isr: 1
	Topic: ChannelClick	Partition: 3	Leader: 1	Replicas: 1,2,3	Isr: 1
	Topic: ChannelClick	Partition: 4	Leader: 1	Replicas: 2,3,1	Isr: 1
	Topic: ChannelClick	Partition: 5	Leader: 1	Replicas: 3,1,2	Isr: 1
```

#### 4.5 topic常用操作

```bash

创建topic

bin/kafka-topics.sh --zookeeper 10.81.128.114:2181,10.81.128.213:2181,10.81.128.163:2181 --create --topic my_topic --partitions 20 --replication-factor 3 --config x=y

修改topic

bin/kafka-topics.sh --zookeeper 10.81.128.114:2181,10.81.128.213:2181,10.81.128.163:2181 --alter --topic my_topic --partitions 40

增加配置项
	  
bin/kafka-configs.sh --zookeeper 10.81.128.114:2181,10.81.128.213:2181,10.81.128.163:2181 --entity-type topics --entity-name my_topic --alter --add-config x=y

删除配置项

bin/kafka-configs.sh --zookeeper 10.81.128.114:2181,10.81.128.213:2181,10.81.128.163:2181 --entity-type topics --entity-name my_topic --alter --delete-config x

删除topic

bin/kafka-topics.sh --zookeeper 10.81.128.114:2181,10.81.128.213:2181,10.81.128.163:2181 --delete --topic my_topic	  

```

#### 4.6 查看kafka topic消息数量

```bash
bin/kafka-run-class.sh kafka.tools.GetOffsetShell --broker-list 10.81.128.114:9092,10.81.128.213:9092,10.81.128.163:9092 --topic ChannelClick --time -1
```

#### 4.7 topic基本配置

创建topic的时候指定配置信息

```bash
bin/kafka-topics.sh --zookeeper 10.81.128.114:2181,10.81.128.213:2181,10.81.128.163:2181 --create --topic ChannelClick --partitions 1
    --replication-factor 1 --config max.message.bytes=64000 --config flush.messages=1
```

修改指定topic的配置信息

```bash
bin/kafka-configs.sh --zookeeper 10.81.128.114:2181,10.81.128.213:2181,10.81.128.163:2181 --entity-type topics --entity-name ChannelClick
    --alter --add-config max.message.bytes=128000
```

查看topic配置信息的值

```bash
bin/kafka-configs.sh --zookeeper 10.81.128.114:2181,10.81.128.213:2181,10.81.128.163:2181 --entity-type topics --entity-name ChannelClick --describe
```

#### 4.8 启动控制台生产者

```bash
bin/kafka-console-producer.sh --broker-list 10.81.128.114:9092,10.81.128.213:9092,10.81.128.163:9092 --topic test
```

#### 4.9 启动控制台消费者

```bash
bin/kafka-console-consumer.sh --bootstrap-server 10.81.128.114:9092,10.81.128.213:9092,10.81.128.163:9092 --topic test --from-beginning
```

```bash
bin/kafka-console-consumer.sh --bootstrap-server 10.81.128.114:9092,10.81.128.213:9092,10.81.128.163:9092 --topic ChannelClick --from-beginning
```

#### 4.10 查看ConsumerGroup

查看ConsumerGroup列表

```bash
bin/kafka-consumer-groups.sh --bootstrap-server 10.81.128.114:9092,10.81.128.213:9092,10.81.128.163:9092 --list
```

```bash
bin/kafka-consumer-groups.sh --zookeeper 10.81.128.114:2181,10.81.128.213:2181,10.81.128.163:2181 --list
```

查看指定ConsumerGroup详细信息

```bash
bin/kafka-consumer-groups.sh --bootstrap-server 10.81.128.114:9092,10.81.128.213:9092,10.81.128.163:9092 --group logstash --describe
```

其他高级用法

```bash

bin/kafka-consumer-groups.sh --bootstrap-server 10.81.128.114:9092,10.81.128.213:9092,10.81.128.163:9092 --list

bin/kafka-consumer-groups.sh --zookeeper 10.81.128.114:2181,10.81.128.213:2181,10.81.128.163:2181 --list

bin/kafka-consumer-groups.sh --bootstrap-server 10.81.128.114:9092,10.81.128.213:9092,10.81.128.163:9092 --describe --group logstash
 
bin/kafka-consumer-groups.sh --bootstrap-server 10.81.128.114:9092,10.81.128.213:9092,10.81.128.163:9092 --describe --group logstash --members

bin/kafka-consumer-groups.sh --bootstrap-server 10.81.128.114:9092,10.81.128.213:9092,10.81.128.163:9092 --describe --group logstash --members --verbose

bin/kafka-consumer-groups.sh --bootstrap-server 10.81.128.114:9092,10.81.128.213:9092,10.81.128.163:9092 --describe --group logstash --state

bin/kafka-consumer-groups.sh --bootstrap-server 10.81.128.114:9092,10.81.128.213:9092,10.81.128.163:9092 --delete --group my-group --group logstash
```

#### 4.11 查看kafka消息元数据

```bash
bin/kafka-run-class.sh kafka.tools.DumpLogSegments --files logs-data/Channeltopic-5/00000000000000000000.log --print-data-log
```

### 5 监控管理工具

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

http://192.168.1.128:8048/ke admin/123456



### 5 参考文档



http://kafka.apachecn.org/documentation.html

http://www.dengshenyu.com/%E5%88%86%E5%B8%83%E5%BC%8F%E7%B3%BB%E7%BB%9F/2017/11/12/kafka-producer.html

http://www.dengshenyu.com/%E5%88%86%E5%B8%83%E5%BC%8F%E7%B3%BB%E7%BB%9F/2017/11/21/kafka-data-delivery.html

http://www.lpnote.com/2017/01/15/reliability-of-kafka-message/

https://kaimingwan.com/post/framworks/kafka/kafka-producerxing-neng-diao-you

http://matt33.com/2017/09/04/kafka-best-pratice/

https://zhuanlan.zhihu.com/p/38269875

https://www.jianshu.com/p/de4b4cbb0f3c