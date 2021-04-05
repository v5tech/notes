# storm集群搭建

首先需要搭建zookeeper集群

### 核心配置文件

```bash
storm.zookeeper.servers:
     - "s1"
     - "s2"
     - "s3"
nimbus.host: "s1"
```

其中s1、s2、s3为zookeeper集群。`nimbus.host`指定`nimbus`所在主机

### 启动storm集群

在`nimbus`所在主机上启动

```bash
storm nimbus &
storm ui &
```

在storm集群中其他主机上启动`supervisor`

```bash
storm supervisor &
```