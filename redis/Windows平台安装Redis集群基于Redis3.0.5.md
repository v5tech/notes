# Windows平台安装Redis集群（基于Redis 3.0.5）

### 1. 安装Redis

https://github.com/MSOpenTech/redis/releases/download/win-3.0.501/Redis-x64-3.0.501.msi

这里将Redis安装在`C:\Redis`目录下。

### 2. 安装Ruby

http://dl.bintray.com/oneclick/rubyinstaller/rubyinstaller-2.2.4-x64.exe

这里将Ruby安装在`C:\Ruby22-x64`目录下。

### 3. 安装Redis的Ruby库

```ruby
gem install redis
```

### 4. 配置Redis Node

拷贝6份Redis安装目录下的`redis.windows-service.conf`文件依次重命名为redis.6380.conf、redis.6381.conf、redis.6382.conf、redis.6383.conf、redis.6384.conf、redis.6385.conf。并分别修改这6个配置文件。具体修改内容如下:

redis.6380.conf

```
port 6380
appendonly yes
appendfilename "appendonly.6380.aof"
cluster-enabled yes
cluster-config-file nodes-6380.conf
cluster-node-timeout 15000
cluster-slave-validity-factor 10
cluster-migration-barrier 1
cluster-require-full-coverage yes
```

redis.6381.conf

```
port 6381
appendonly yes
appendfilename "appendonly.6381.aof"
cluster-enabled yes
cluster-config-file nodes-6381.conf
cluster-node-timeout 15000
cluster-slave-validity-factor 10
cluster-migration-barrier 1
cluster-require-full-coverage yes
```

redis.6382.conf

```
port 6382
appendonly yes
appendfilename "appendonly.6382.aof"
cluster-enabled yes
cluster-config-file nodes-6382.conf
cluster-node-timeout 15000
cluster-slave-validity-factor 10
cluster-migration-barrier 1
cluster-require-full-coverage yes
```

redis.6383.conf

```
port 6383
appendonly yes
appendfilename "appendonly.6383.aof"
cluster-enabled yes
cluster-config-file nodes-6383.conf
cluster-node-timeout 15000
cluster-slave-validity-factor 10
cluster-migration-barrier 1
cluster-require-full-coverage yes
```

redis.6384.conf

```
port 6384
appendonly yes
appendfilename "appendonly.6384.aof"
cluster-enabled yes
cluster-config-file nodes-6384.conf
cluster-node-timeout 15000
cluster-slave-validity-factor 10
cluster-migration-barrier 1
cluster-require-full-coverage yes
```

redis.6385.conf

```
port 6385
appendonly yes
appendfilename "appendonly.6385.aof"
cluster-enabled yes
cluster-config-file nodes-6385.conf
cluster-node-timeout 15000
cluster-slave-validity-factor 10
cluster-migration-barrier 1
cluster-require-full-coverage yes
```

至此集群节点配置文件配置完毕。

### 5. 注册Redis为Windows Service

在服务中停掉Redis默认安装时已经注册到端口号为6379的服务。

```
redis-server --service-install redis.6380.conf --service-name redis6380
redis-server --service-start --service-name Redis6380

redis-server --service-install redis.6381.conf --service-name redis6381
redis-server --service-start --service-name Redis6381

redis-server --service-install redis.6382.conf --service-name redis6382
redis-server --service-start --service-name Redis6382

redis-server --service-install redis.6383.conf --service-name redis6383
redis-server --service-start --service-name Redis6383

redis-server --service-install redis.6384.conf --service-name redis6384
redis-server --service-start --service-name Redis6384

redis-server --service-install redis.6385.conf --service-name redis6385
redis-server --service-start --service-name Redis6385
```

### 6. 创建Redis Cluster

下载[https://raw.githubusercontent.com/MSOpenTech/redis/3.0/src/redis-trib.rb](https://raw.githubusercontent.com/MSOpenTech/redis/3.0/src/redis-trib.rb)脚本到Redis安装目录(C:\Redis)，在命令行执行如下命令:

```
C:\Redis>redis-trib.rb create --replicas 1 127.0.0.1:6380 127.0.0.1:6381 127.0.0.1:6382 127.0.0.1:6383 127.0.0.1:6384 127.0.0.1:6385
>>> Creating cluster
Connecting to node 127.0.0.1:6380: OK
Connecting to node 127.0.0.1:6381: OK
Connecting to node 127.0.0.1:6382: OK
Connecting to node 127.0.0.1:6383: OK
Connecting to node 127.0.0.1:6384: OK
Connecting to node 127.0.0.1:6385: OK
>>> Performing hash slots allocation on 6 nodes...
Using 3 masters:
127.0.0.1:6380
127.0.0.1:6381
127.0.0.1:6382
Adding replica 127.0.0.1:6383 to 127.0.0.1:6380
Adding replica 127.0.0.1:6384 to 127.0.0.1:6381
Adding replica 127.0.0.1:6385 to 127.0.0.1:6382
M: 49060b7f06bd3839895919a06ba43d0508b1149f 127.0.0.1:6380
   slots:0-5460 (5461 slots) master
M: 0bfbefc15a586f1a893ef150af43031a7ce04a9f 127.0.0.1:6381
   slots:5461-10922 (5462 slots) master
M: 5fb098d997e0f0b9e723b09400604344ec65179b 127.0.0.1:6382
   slots:10923-16383 (5461 slots) master
S: e2d74cfcccf88aef1dec16b1922ca2ad6dc16195 127.0.0.1:6383
   replicates 49060b7f06bd3839895919a06ba43d0508b1149f
S: bcbe8bf76a5b0d37768556ed752e30dcfea069f6 127.0.0.1:6384
   replicates 0bfbefc15a586f1a893ef150af43031a7ce04a9f
S: bbfc7026df6822bf3cfd8e4a3549b02ca57f7393 127.0.0.1:6385
   replicates 5fb098d997e0f0b9e723b09400604344ec65179b
Can I set the above configuration? (type 'yes' to accept): yes
>>> Nodes configuration updated
>>> Assign a different config epoch to each node
>>> Sending CLUSTER MEET messages to join the cluster
Waiting for the cluster to join...
>>> Performing Cluster Check (using node 127.0.0.1:6380)
M: 49060b7f06bd3839895919a06ba43d0508b1149f 127.0.0.1:6380
   slots:0-5460 (5461 slots) master
M: 0bfbefc15a586f1a893ef150af43031a7ce04a9f 127.0.0.1:6381
   slots:5461-10922 (5462 slots) master
M: 5fb098d997e0f0b9e723b09400604344ec65179b 127.0.0.1:6382
   slots:10923-16383 (5461 slots) master
M: e2d74cfcccf88aef1dec16b1922ca2ad6dc16195 127.0.0.1:6383
   slots: (0 slots) master
   replicates 49060b7f06bd3839895919a06ba43d0508b1149f
M: bcbe8bf76a5b0d37768556ed752e30dcfea069f6 127.0.0.1:6384
   slots: (0 slots) master
   replicates 0bfbefc15a586f1a893ef150af43031a7ce04a9f
M: bbfc7026df6822bf3cfd8e4a3549b02ca57f7393 127.0.0.1:6385
   slots: (0 slots) master
   replicates 5fb098d997e0f0b9e723b09400604344ec65179b
[OK] All nodes agree about slots configuration.
>>> Check for open slots...
>>> Check slots coverage...
[OK] All 16384 slots covered.

C:\Redis>
```

到此Redis Cluster创建完毕，从控制台输出可以看到：`127.0.0.1:6380`，`127.0.0.1:6381`，`127.0.0.1:6382`为master节点。`127.0.0.1:6383`，`127.0.0.1:6384`，`127.0.0.1:6385`依次为相对应的副节点。

### 参考文档

[https://dotblogs.com.tw/supershowwei/2015/12/29/101928](https://dotblogs.com.tw/supershowwei/2015/12/29/101928 "在Windows上安裝 Redis Cluster")

[https://www.zybuluo.com/phper/note/195558](https://www.zybuluo.com/phper/note/195558 "Redis集群研究和实践（基于redis 3.0.5）")
