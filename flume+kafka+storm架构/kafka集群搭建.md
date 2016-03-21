# kafka集群搭建

### 1、zookeeper集群搭建

```bash
tickTime=2000
initLimit=10
syncLimit=5
dataDir=/home/ubuntu/apps/zookeeper-3.4.7/data
clientPort=2181

server.1=s1:2888:3888
server.2=s2:2888:3888
server.3=s3:2888:3888
```
分别在s1、s2、s3三台主机的`dataDir`目录下创建`myid`文件，其内容为分别为1、2、3

### 2、启动zookeeper集群

```bash
zkServer.sh start
zkServer.sh status
```

### 3、kafka核心配置文件(三台机器)

主机s1中的server.properties文件

```bash
broker.id=0
host.name=s1
zookeeper.connect=s1:2181,s2:2181,s3:2181
```

主机s2中的server.properties文件

```bash
broker.id=1
host.name=s2
zookeeper.connect=s1:2181,s2:2181,s3:2181
```

主机s3中的server.properties文件

```bash
broker.id=2
host.name=s3
zookeeper.connect=s1:2181,s2:2181,s3:2181
```

请确保每台kafka配置文件中的`broker.id`唯一。`zookeeper.connect`参数为zookeeper集群的主机地址及端口号。

`host.name`为ip或`hosts`文件中ip与主机地址之间的映射名。程序代码中必须用`host.name`指定的值否则客户端代码连接报错

### 4、启动kafka集群

分别在三台kafka主机上启动kafka server
```bash
kafka-server-start.sh config/server.properties &
```

### 5、在集群环境中创建topic

```bash
kafka-topics.sh --create --zookeeper s1:2181 --replication-factor 3 --partitions 1 --topic kafka-storm
```

### 6、查看集群环境中创建的topic

```bash
kafka-topics.sh --list --zookeeper s1:2181
```

### 7、查看集群环境中指定topic的describe

```bash
kafka-topics.sh --describe --zookeeper s1:2181 --topic kafka-storm
Topic:kafka-storm	PartitionCount:1	ReplicationFactor:3	Configs:
	Topic: kafka-storm	Partition: 0	Leader: 1	Replicas: 1,2,0	Isr: 1,2,0
```

### 8、创建一个终端producer(生产者)

```bash
kafka-console-producer.sh --broker-list s1:9092 --topic kafka-storm
```

### 9、创建一个终端consumer(消费者)

```bash
kafka-console-consumer.sh --zookeeper s1:2181 --from-beginning --topic kafka-storm
```

注:`--from-beginning`将每次从头开始读消息。不加该参数则只获取最新发送的消息。