# MongoDB集群搭建

## MongoDB主从搭建

这个是最简答的集群搭建，不过准确说也不能算是集群，只能说是主备。并且官方已经不推荐这种方式，所以在这里只是简单的介绍下吧，搭建方式也相对简单。纯主从不能高可用，主挂了，则集群挂了，不推荐。

### 新建目录

~~~shell
[root@localhost mongodb]# mkdir mongo-ms/master/data -p
[root@localhost mongodb]# mkdir mongo-ms/master/logs -p
[root@localhost mongodb]# mkdir mongo-ms/slave/logs -p       
[root@localhost mongodb]# mkdir mongo-ms/slave/data -p
~~~

### 主机配置

主机配置   /usr/local/mongodb/mongo-ms/master/mongodb.cfg

~~~properties
#数据库文件位置
dbpath=/usr/local/mongodb/mongo-ms/master/data
#日志文件位置
logpath=/usr/local/mongodb/mongo-ms/master/logs/mongodb.log
# 以追加方式写入日志
logappend=true
# 是否以守护进程方式运行
fork=true
#绑定客户端访问的ip
bind_ip=0.0.0.0
# 默认27017
port=27001
# 主从模式下，指定我自身的角色是主机
master=true
# 主从模式下，从机的地址信息
source=192.168.222.128:27002
~~~

### 从机配置

从机配置   /usr/local/mongodb/mongo-ms/slave/mongodb.cfg

```properties
#数据库文件位置
dbpath=/usr/local/mongodb/mongo-ms/slave/data
#日志文件位置
logpath=/usr/local/mongodb/mongo-ms/slave/logs/mongodb.log
# 以追加方式写入日志
logappend=true
# 是否以守护进程方式运行
fork=true
#绑定客户端访问的ip
bind_ip=0.0.0.0
# 默认27017
port=27001
# 主从模式下，指定我自身的角色是主机
master=true
# 主从模式下，从机的地址信息
source=192.168.222.128:27002
```

### 测试

启动服务

~~~shell
mongod -f /usr/local/mongodb/mongo-ms/master/mongodb.cfg
mongod -f /usr/local/mongodb/mongo-ms/slave/mongodb.cfg
~~~

连接测试

~~~shell
mongo --port=27001
mongo --port=27002
~~~

测试命令

~~~shell
db.isMaster()
~~~

### 读写分离

MongoDB副本集对读写分离的支持是通过Read Preferences特性进行支持的，这个特性非常复杂和灵
活。设置读写分离需要先在从节点SECONDARY 设置

~~~shell
rs.slaveOk()
~~~

基本上只要在主节点和备节点上分别执行这两条命令，Master-Slaver 就算搭建完成了。我没有试过主节点挂掉后备节点是否能变成主节点，不过既然已经不推荐了，大家就没必要去使用了。

## MongoDB副本集集群

中文翻译叫做副本集，不过我并不喜欢把英文翻译成中文，总是感觉怪怪的。其实简单来说就是集群当中包含了多份数据，保证主节点挂掉了，备节点能继续提供数据服务，提供的前提就是数据需要和主节点一致。如下图：
Mongodb(M)表示主节点，Mongodb(S)表示备节点，Mongodb(A)表示仲裁节点。主备节点存储数据，仲裁节点不存储数据。客户端同时连接主节点与备节点，不连接仲裁节点。
默认设置下，主节点提供所有增删查改服务，备节点不提供任何服务。但是可以通过设置使备节点
提供查询服务，这样就可以减少主节点的压力，当客户端进行数据查询时，请求自动转到备节点上。这
个设置叫做 Read Preference Modes，同时 Java 客户端提供了简单的配置方式，可以不必直接对数
据库进行操作。
仲裁节点是一种特殊的节点，它本身并不存储数据，主要的作用是决定哪一个备节点在主节点挂掉之后
提升为主节点，所以客户端不需要连接此节点。这里虽然只有一个备节点，但是仍然需要一个仲裁节点
来提升备节点级别。我开始也不相信必须要有仲裁节点，但是自己也试过没仲裁节点的话，主节点挂了
备节点还是备节点，所以咱们还是需要它的。

> 副本集中有三种角色：主节点、从节点、仲裁节点
>
> 仲裁节点不存储数据，主从节点都存储数据。
>
> 优点：
> 主如果宕机，仲裁节点会选举从作为新的主
> 如果副本集中没有仲裁节点，那么集群的主从切换依然可以进行。
> 缺点：
> 如果副本集中拥有仲裁节点，那么一旦仲裁节点挂了，集群中就不能进行主从切换了。

### 有仲裁节点的副本集

### 新建目录

```shell
[root@localhost mongodb]# mkdir mongo-rs/rs01/node1/data -p
[root@localhost mongodb]# mkdir mongo-rs/rs01/node1/logs -p
[root@localhost mongodb]# mkdir mongo-rs/rs01/node2/data -p       
[root@localhost mongodb]# mkdir mongo-rs/rs01/node2/logs -p
[root@localhost mongodb]# mkdir mongo-rs/rs01/node3/data -p       
[root@localhost mongodb]# mkdir mongo-rs/rs01/node3/logs -p
```

### 节点1配置

```properties
#数据库文件位置
dbpath=/usr/local/mongodb/mongo-rs/rs01/node1/data
#日志文件位置
logpath=/usr/local/mongodb/mongo-rs/rs01/node1/logs/mongodb.log
# 以追加方式写入日志
logappend=true
# 是否以守护进程方式运行
fork=true
#绑定客户端访问的ip
bind_ip=0.0.0.0
# 默认27017
port=27003
#注意：不需要显式的去指定主从，主从是动态选举的
#副本集集群，需要指定一个名称，在一个副本集下，名称是相同的
replSet=rs001
```

### 节点2配置

```properties
#数据库文件位置
dbpath=/usr/local/mongodb/mongo-rs/rs01/node2/data
#日志文件位置
logpath=/usr/local/mongodb/mongo-rs/rs01/node2/logs/mongodb.log
# 以追加方式写入日志
logappend=true
# 是否以守护进程方式运行
fork=true
#绑定客户端访问的ip
bind_ip=0.0.0.0
# 默认27017
port=27004
#注意：不需要显式的去指定主从，主从是动态选举的
#副本集集群，需要指定一个名称，在一个副本集下，名称是相同的
replSet=rs001
```

### 节点3配置

```properties
#数据库文件位置
dbpath=/usr/local/mongodb/mongo-rs/rs01/node3/data
#日志文件位置
logpath=/usr/local/mongodb/mongo-rs/rs01/node3/logs/mongodb.log
# 以追加方式写入日志
logappend=true
# 是否以守护进程方式运行
fork=true
#绑定客户端访问的ip
bind_ip=0.0.0.0
# 默认27017
port=27005
#注意：不需要显式的去指定主从，主从是动态选举的
#副本集集群，需要指定一个名称，在一个副本集下，名称是相同的
replSet=rs001
```

启动副本集节点

~~~shell
mongod -f /usr/local/mongodb/mongo-rs/rs01/node1/mongodb.cfg
mongod -f /usr/local/mongodb/mongo-rs/rs01/node2/mongodb.cfg
mongod -f /usr/local/mongodb/mongo-rs/rs01/node3/mongodb.cfg
~~~

### 配置主备和仲裁

需要登录到mongodb的客户端进行配置主备和仲裁角色。
**注意创建dbpath和logpath**

~~~shell
mongo --port=27003
use admin
cfg={_id:"rs001",members: [
{_id:0,host:"192.168.222.128:27003",priority:2}, #主的可能性大
{_id:1,host:"192.168.222.128:27004",priority:1},
{_id:2,host:"192.168.222.128:27005",arbiterOnly:true}
]}
rs.initiate(cfg);
~~~

说明：
cfg中的_id的值是【副本集名称】
priority：数字越大，优先级越高。优先级最高的会被选举为主库
arbiterOnly:true，如果是仲裁节点，必须设置该参数

### 测试

~~~shell
rs.status()
~~~

### 无仲裁副本集

和有仲裁的副本集基本上完全一样，只是在admin数据库下去执行配置的时候，不需要指定优先级和仲
裁节点。这种情况，如果节点挂掉，那么他们都会进行选举。

### 新建目录

```shell
[root@localhost mongodb]# mkdir mongo-rs/rs02/node1/data -p
[root@localhost mongodb]# mkdir mongo-rs/rs02/node1/logs -p
[root@localhost mongodb]# mkdir mongo-rs/rs02/node2/data -p       
[root@localhost mongodb]# mkdir mongo-rs/rs02/node2/logs -p
[root@localhost mongodb]# mkdir mongo-rs/rs02/node3/data -p       
[root@localhost mongodb]# mkdir mongo-rs/rs02/node3/logs -p
```

### 节点1配置

```properties
#数据库文件位置
dbpath=/usr/local/mongodb/mongo-rs/rs02/node1/data
#日志文件位置
logpath=/usr/local/mongodb/mongo-rs/rs02/node1/logs/mongodb.log
# 以追加方式写入日志
logappend=true
# 是否以守护进程方式运行
fork=true
#绑定客户端访问的ip
bind_ip=0.0.0.0
# 默认27017
port=27006
#注意：不需要显式的去指定主从，主从是动态选举的
#副本集集群，需要指定一个名称，在一个副本集下，名称是相同的
replSet=rs002
```

### 节点2配置

```properties
#数据库文件位置
dbpath=/usr/local/mongodb/mongo-rs/rs02/node2/data
#日志文件位置
logpath=/usr/local/mongodb/mongo-rs/rs02/node2/logs/mongodb.log
# 以追加方式写入日志
logappend=true
# 是否以守护进程方式运行
fork=true
#绑定客户端访问的ip
bind_ip=0.0.0.0
# 默认27017
port=27007
#注意：不需要显式的去指定主从，主从是动态选举的
#副本集集群，需要指定一个名称，在一个副本集下，名称是相同的
replSet=rs002
```

### 节点3配置

```properties
#数据库文件位置
dbpath=/usr/local/mongodb/mongo-rs/rs02/node3/data
#日志文件位置
logpath=/usr/local/mongodb/mongo-rs/rs02/node3/logs/mongodb.log
# 以追加方式写入日志
logappend=true
# 是否以守护进程方式运行
fork=true
#绑定客户端访问的ip
bind_ip=0.0.0.0
# 默认27017
port=27008
#注意：不需要显式的去指定主从，主从是动态选举的
#副本集集群，需要指定一个名称，在一个副本集下，名称是相同的
replSet=rs002
```

启动副本集节点

```shell
mongod -f /usr/local/mongodb/mongo-rs/rs02/node1/mongodb.cfg
mongod -f /usr/local/mongodb/mongo-rs/rs02/node2/mongodb.cfg
mongod -f /usr/local/mongodb/mongo-rs/rs02/node3/mongodb.cfg
```

### 配置主备

需要登录到mongodb的客户端进行配置主备。

```shell
mongo --port=27006
use admin
cfg={_id:"rs001",members: [
{_id:0,host:"192.168.222.128:27006"}, #主的可能性大
{_id:1,host:"192.168.222.128:27007"},
{_id:2,host:"192.168.222.128:27008"}
]}
rs.initiate(cfg);
```

## MongoDB混合方式集群

### 数据服务器配置（副本集）

在副本集中每个数据节点的mongodb.cfg配置文件【追加】以下内容（仲裁节点除外）：

~~~shell
shardsvr=true
~~~

### 配置服务器配置(先启动配置集再启动数据副本集)

配置三个配置服务器，配置信息如下，端口和path单独指定：

~~~properties
#数据库文件位置
dbpath=/usr/local/mongodb/mongo-conf/node1/data
#日志文件位置
logpath=/usr/local/mongodb/mongo-conf/node1/logs/mongodb.log
# 以追加方式写入日志
logappend=true
# 是否以守护进程方式运行
fork = true
bind_ip=0.0.0.0
# 默认27017
port=28001
# 表示是一个配置服务器
configsvr=true
#配置服务器副本集名称
replSet=configsvr
~~~

```properties
#数据库文件位置
dbpath=/usr/local/mongodb/mongo-conf/node2/data
#日志文件位置
logpath=/usr/local/mongodb/mongo-conf/node2/logs/mongodb.log
# 以追加方式写入日志
logappend=true
# 是否以守护进程方式运行
fork = true
bind_ip=0.0.0.0
# 默认27017
port=28002
# 表示是一个配置服务器
configsvr=true
#配置服务器副本集名称
replSet=configsvr
```

```properties
#数据库文件位置
dbpath=/usr/local/mongodb/mongo-conf/node3/data
#日志文件位置
logpath=/usr/local/mongodb/mongo-conf/node3/logs/mongodb.log
# 以追加方式写入日志
logappend=true
# 是否以守护进程方式运行
fork = true
bind_ip=0.0.0.0
# 默认27017
port=28003
# 表示是一个配置服务器
configsvr=true
#配置服务器副本集名称
replSet=configsvr
```

**注意创建dbpath和logpath**

~~~shell
[root@localhost mongodb]# mkdir mongo-conf/node1/data -p
[root@localhost mongodb]# mkdir mongo-conf/node1/logs -p
[root@localhost mongodb]# mkdir mongo-conf/node2/data -p
[root@localhost mongodb]# mkdir mongo-conf/node2/logs -p
[root@localhost mongodb]# mkdir mongo-conf/node3/data -p
[root@localhost mongodb]# mkdir mongo-conf/node3/logs -p
#先启动配置集再启动数据副本集
#启动配置集
mongod -f /usr/local/mongodb/mongo-conf/node1/mongodb.cfg
mongod -f /usr/local/mongodb/mongo-conf/node2/mongodb.cfg
mongod -f /usr/local/mongodb/mongo-conf/node3/mongodb.cfg
#启动数据副本集--shard1
mongod -f /usr/local/mongodb/mongo-rs/rs01/node1/mongodb.cfg
mongod -f /usr/local/mongodb/mongo-rs/rs01/node2/mongodb.cfg
mongod -f /usr/local/mongodb/mongo-rs/rs01/node3/mongodb.cfg
#启动数据副本集--shard2
mongod -f /usr/local/mongodb/mongo-rs/rs02/node1/mongodb.cfg
mongod -f /usr/local/mongodb/mongo-rs/rs02/node2/mongodb.cfg
mongod -f /usr/local/mongodb/mongo-rs/rs02/node3/mongodb.cfg
~~~

#### 配置副本集

~~~shell
mongo --port=28001
use admin
cfg={_id:"configsvr",members: [
{_id:0,host:"192.168.222.128:28001"},
{_id:1,host:"192.168.222.128:28002"},
{_id:2,host:"192.168.222.128:28003"}
]}
rs.initiate(cfg);
~~~

### 路由服务器配置

~~~shell
configdb=configsvr/192.168.222.128:28001,192.168.222.128:28002,192.168.222.128:28003
#日志文件位置
logpath=/usr/local/mongodb/mongo-router/node01/logs/mongodb.log
# 以追加方式写入日志
logappend=true
# 是否以守护进程方式运行
fork = true
bind_ip=0.0.0.0
# 默认28001
port=30000
~~~

路由服务器启动（**注意这里是mongos命令而不是mongod命令**）

~~~shell
mongos -f /usr/local/mongodb/mongo-router/node01/mongodb.cfg
~~~

#### 关联切片和路由

登录到路由服务器中，执行关联切片和路由的相关操作。

~~~shell
mongo  --port=30000 
#查看shard相关的命令
sh.help()
sh.addShard("切片名称/地址")
#数据副本集 
sh.addShard("rs001/192.168.222.128:27003");
sh.addShard("rs002/192.168.222.128:27006");
use kkb
sh.enableSharding("kkb");
#新的集合
sh.shardCollection("kkb.citem",{name:"hashed"});
for(var i=1;i<=1000;i++) db.citem.insert({name:"iphone"+i,num:i});
#分片效果
mongos> db.citem.count()
1000
mongo --port=27003
use kkb
rs001:PRIMARY> db.citem.count()
516
#从库
mongo --port=27004
use kkb
db.getMongo().setSlaveOk() # 设置主从读写分离 
rs001:SECONDARY> db.citem.count()
516 
mongo --port=27006
use kkb
db.citem.count()
484
~~~

## MongoDB集群之复制集(基于docker)

### 简介

一组Mongodb复制集，就是一组mongod进程，这些进程维护同一个数据集合。复制集提供了数据冗余和高等级的可靠性，这是生产部署的基础。

> 目的

* 保证数据在生产部署时的冗余和可靠性，通过在不同的机器上保存副本来保证数据的不会因为单点损坏而丢

失。能够随时应对数据丢失、机器损坏带来的风险。

* 还能提高读取能力，用户的读取服务器和写入服务器在不同的地方，而且，由不同的服务器为不同的用户提供

服务，提高整个系统的负载。

> 机制

* 一组复制集就是一组mongod实例掌管同一个数据集，实例可以在不同的机器上面。实例中包含一个主导

（Primary），接受客户端所有的写入操作，其他都是副本实例（Secondary），从主服务器上获得数据并保持
同步。

* 主服务器很重要，包含了所有的改变操作（写）的日志。但是副本服务器集群包含有所有的主服务器数据，因

此当主服务器挂掉了，就会在副本服务器上重新选取一个成为主服务器。

* 每个复制集还有一个仲裁者（Arbiter），仲裁者不存储数据，只是负责通过心跳包来确认集群中集合的数量，并在主服务器选举的时候作为仲裁决定结果。

### 架构

基本的架构由3台服务器组成，一个三成员的复制集，由三个有数据，或者两个有数据，一个作为仲裁者。

#### 三个存储数据的复制集

一个主，两个从库组成，主库宕机时，这两个从库都可以被选为主库。

当主库宕机后,两个从库都会进行竞选，其中一个变为主库，当原主库恢复后，作为从库加入当前的复制集群即可。

#### 存在arbiter节点的复制集

一个主库，一个从库，可以在选举中成为主库，一个arbiter节点，在选举中，只进行投票，不能成为主库。

> 说明：由于arbiter节点没有复制数据，因此这个架构中仅提供一个完整的数据副本。arbiter节点只需要更少的资源，代价是更有限的冗余和容错。

当主库宕机时，将会选择从库成为主，主库修复后，将其加入到现有的复制集群中即可。

### Primary选举

复制集通过replSetInitiate命令（或mongo shell的rs.initiate()）进行初始化，初始化后各个成员间开始发送心跳消息，并发起Priamry选举操作，获得『大多数』成员投票支持的节点，会成为Primary，其余节点成为Secondary。

> 『大多数』的定义

假设复制集内投票成员数量为N，则大多数为 N/2 + 1，当复制集内存活成员数量不足大多数时，整个复制集将无法选举出Primary，复制集将无法提供写服务，处于只读状态。

### 成员说明

| 成员          | 说明                                                         |
| ------------- | ------------------------------------------------------------ |
| **Primary**   | Priamry的作用是接收用户的写入操作，将自己的数据同步给其他的Secondary。 |
| **Secondary** | 正常情况下，复制集的Seconary会参与Primary选举（自身也可能会被选为Primary），并从Primary同步最新写入的数据，以保证与Primary存储相同的数据。Secondary可以提供读服务，增加Secondary节点可以提供复制集的读服务能力，同时提升复制集的可用性。另外，Mongodb支持对复制集的Secondary节点进行灵活的配置，以适应多种场景的需求。 |
| **Arbiter**   | Arbiter节点只参与投票，不能被选为Primary，并且不从Primary同步数据。比如你部署了一个2个节点的复制集，1个Primary，1个Secondary，任一节点宕机，复制集将不能提供服务了（无法选出Primary），这时可以给复制集添加一个Arbiter节点，即使有节点宕机，仍能选出Primary。Arbiter本身不存储数据，是非常轻量级的服务，当复制集成员为偶数时，最好加入一个Arbiter节点，以提升复制集可用性。 |
| **Priority0** | Priority0节点的选举优先级为0，不会被选举为Primary。比如你跨机房A、B部署了一个复制集，并且想指定Primary必须在A机房，这时可以将B机房的复制集成员Priority设置为0，这样Primary就一定会是A机房的成员。（注意：如果这样部署，最好将『大多数』节点部署在A机房，否则网络分区时可能无法选出Primary） |
| **Vote0**     | Mongodb 3.0里，复制集成员最多50个，参与Primary选举投票的成员最多7个，其他成员（Vote0）的vote属性必须设置为0，即不参与投票。 |
| **Hidden**    | Hidden节点不能被选为主（Priority为0），并且对Driver不可见。因Hidden节点不会接受Driver的请求，可使用Hidden节点做一些数据备份、离线计算的任务，不会影响复制集的服务。 |
| **Delayed**   | Delayed节点必须是Hidden节点，并且其数据落后与Primary一段时间（可配置，比如1个小时）。因Delayed节点的数据比Primary落后一段时间，当错误或者无效的数据写入Primary时，可通过Delayed节点的数据来恢复到之前的时间点。 |

### 搭建复制集

~~~shell
docker create --name mongo01 -p 27017:27017 -v mongo-data-01:/data/db mongo:4.0.3 --replSet "rs0" --bind_ip_all
docker create --name mongo02 -p 27018:27017 -v mongo-data-02:/data/db mongo:4.0.3 --replSet "rs0" --bind_ip_all
docker create --name mongo03 -p 27019:27017 -v mongo-data-03:/data/db mongo:4.0.3 --replSet "rs0" --bind_ip_all
#启动容器
docker start mongo01 mongo02 mongo03
#进入容器操作
docker exec -it mongo01 /bin/bash
#登录到mongo服务
mongo 172.17.0.1:27017
#初始化复制集集群
rs.initiate( {
   _id : "rs0",
   members: [
      { _id: 0, host: "172.17.0.1:27017" },
      { _id: 1, host: "172.17.0.1:27018" },
      { _id: 2, host: "172.17.0.1:27019" }
   ]
})
#响应
{
        "ok" : 1,  #成功
        "operationTime" : Timestamp(1551619334, 1),
        "$clusterTime" : {
                "clusterTime" : Timestamp(1551619334, 1),
                "signature" : {
                        "hash" : BinData(0,"AAAAAAAAAAAAAAAAAAAAAAAAAAA="),
                        "keyId" : NumberLong(0)
                }
        }
}
~~~

测试复制集群：

~~~shell
#在主库插入数据
rs0:PRIMARY> use test
rs0:PRIMARY> db.user.insert({"id":1001,"name":"zhangsan"})
WriteResult({ "nInserted" : 1 })
rs0:PRIMARY> db.user.find()
{ "_id" : ObjectId("5c7bd5965504bcd309686907"), "id" : 1001, "name" : "zhangsan" }
#在复制库查询数据
mongo 172.17.0.1:27018
rs0:SECONDARY> use test
rs0:SECONDARY> db.user.find()
Error: error: { #出错，默认情况下从库是不允许读写操作的
        "operationTime" : Timestamp(1551619556, 1),
        "ok" : 0,
        "errmsg" : "not master and slaveOk=false",
        "code" : 13435,
        "codeName" : "NotMasterNoSlaveOk",
        "$clusterTime" : {
                "clusterTime" : Timestamp(1551619556, 1),
                "signature" : {
                        "hash" : BinData(0,"AAAAAAAAAAAAAAAAAAAAAAAAAAA="),
                        "keyId" : NumberLong(0)
                }
        }
}
rs0:SECONDARY> rs.slaveOk()  #设置允许从库读取数据
rs0:SECONDARY> db.user.find()
{ "_id" : ObjectId("5c7bd5965504bcd309686907"), "id" : 1001, "name" : "zhangsan" }

~~~

### 故障转移

* 测试一：从节点宕机
  * 集群依然可以正常使用，可以读写操作。
* 测试二：主节点宕机
  * 选举出新的主节点继续提供服务
* 测试三：停止集群中的2个节点
  * 当前集群无法选举出Priamry，无法提供写操作，只能进行读操作

### 增加arbiter节点

当集群中的节点数为偶数时，如一主一从情况下，任意一节点宕机都无法选举出Priamry，无法提供写操作，加入
arbiter节点后即可解决该问题。

~~~shell
docker create --name mongo04 -p 27020:27017 -v mongo-data-04:/data/db mongo:4.0.3 --replSet "rs0" --bind_ip_all
docker start mongo04
#在主节点执行
rs0:PRIMARY> rs.addArb("172.17.0.1:27020")
{
        "ok" : 1,
        "operationTime" : Timestamp(1551627454, 1),
        "$clusterTime" : {
                "clusterTime" : Timestamp(1551627454, 1),
                "signature" : {
                        "hash" : BinData(0,"AAAAAAAAAAAAAAAAAAAAAAAAAAA="),
                        "keyId" : NumberLong(0)
                }
        }
}
#查询集群状态
rs.status()
~~~

通过测试，添加arbiter节点后，如果集群节点数不满足N/2+1时，arbiter节点可作为“凑数”节点，可以选出主节点，继续提供服务。

## MongoDB集群之分片集群(基于docker)

分片（sharding）是MongoDB用来将大型集合分割到不同服务器（或者说一个集群）上所采用的方法。尽管分片起源于关系型数据库分区，但MongoDB分片完全又是另一回事。
和MySQL分区方案相比，MongoDB的最大区别在于它几乎能自动完成所有事情，只要告诉MongoDB要分配数据，它就能自动维护数据在不同服务器之间的均衡。

### 简介

高数据量和吞吐量的数据库应用会对单机的性能造成较大压力,大的查询量会将单机的CPU耗尽,大的数据量对单机的存储压力较大,最终会耗尽系统的内存而将压力转移到磁盘IO上。
为了解决这些问题,有两个基本的方法: 垂直扩展和水平扩展。

* 垂直扩展：增加更多的CPU和存储资源来扩展容量。
* 水平扩展：将数据集分布在多个服务器上。水平扩展即分片。

分片为应对高吞吐量与大数据量提供了方法。使用分片减少了每个分片需要处理的请求数，因此，通过水平扩展，集群可以提高自己的存储容量和吞吐量。举例来说，当插入一条数据时，应用只需要访问存储这条数据的分片。
使用分片减少了每个分片存储的数据。例如，如果数据库1tb的数据集，并有4个分片，然后每个分片可能仅持有256GB的数据。如果有40个分片，那么每个切分可能只有25GB的数据。

### 优势

* 对集群进行抽象，让集群“不可见”

  * MongoDB自带了一个叫做mongos的专有路由进程。mongos就是掌握统一路口的路由器，其会将客户端

  发来的请求准确无误的路由到集群中的一个或者一组服务器上，同时会把接收到的响应拼装起来发回到客
  户端。

* 保证集群总是可读写

  * MongoDB通过多种途径来确保集群的可用性和可靠性。

  * 将MongoDB的分片和复制功能结合使用，在确保数据分片到多台服务器的同时，也确保了每分数据都有

    相应的备份，这样就可以确保有服务器换掉时，其他的从库可以立即接替坏掉的部分继续工作。

* 使集群易于扩展

  当系统需要更多的空间和资源的时候，MongoDB使我们可以按需方便的扩充系统容量。

### 架构

| 组件              | 说明                                                         |
| ----------------- | ------------------------------------------------------------ |
| **Config Server** | 存储集群所有节点、分片数据路由信息。默认需要配置3个Config Server节点。 |
| **Mongos**        | 提供对外应用访问，所有操作均通过mongos执行。一般有多个mongos节点。数据迁移和数据自动平衡。 |
| **Mongod**        | 存储应用数据记录。一般有多个Mongod节点，达到数据分片目的。   |

Mongos本身并不持久化数据，Sharded cluster所有的元数据都会存储到Config Server，而用户的数据会分散存储到各个shard。Mongos启动后，会从配置服务器加载元数据，开始提供服务，将用户的请求正确路由到对应的分片。

当数据写入时，MongoDB Cluster根据分片键设计写入数据。当外部语句发起数据查询时，MongoDB根据数据分布自动路由至指定节点返回数据。

### 集群中数据分布

在一个shard server内部，MongoDB会把数据分为chunks，每个chunk代表这个shard server内部一部分数据。
chunk的产生，会有以下两个用途：

* Splitting：当一个chunk的大小超过配置中的chunk size时，MongoDB的后台进程会把这个chunk切分成更小

的chunk，从而避免chunk过大的情况

* Balancing：在MongoDB中，balancer是一个后台进程，负责chunk的迁移，从而均衡各个shard server的负

载，系统初始1个chunk，chunk size默认值64M,生产库上选择适合业务的chunk size是最好的。mongoDB会
自动拆分和迁移chunks。

#### chunk分裂及迁移

随着数据的增长，其中的数据大小超过了配置的chunk size，默认是64M，则这个chunk就会分裂成两个。数据的增长会让chunk分裂得越来越多。

这时候，各个shard 上的chunk数量就会不平衡。mongos中的一个组件balancer 就会执行自动平衡。把chunk从
chunk数量最多的shard节点挪动到数量最少的节点。

#### chunksize

chunk的分裂和迁移非常消耗IO资源；chunk分裂的时机：在插入和更新，读数据不会分裂。

* 小的chunksize：数据均衡是迁移速度快，数据分布更均匀。数据分裂频繁，路由节点消耗更多资源。
* 大的chunksize：数据分裂少。数据块移动集中消耗IO资源。

适合业务的chunksize是最好的。

> chunkSize 对分裂及迁移的影响

* MongoDB 默认的 chunkSize 为64MB，如无特殊需求，建议保持默认值；chunkSize 会直接影响到 chunk 分裂、迁移的行为。

* chunkSize 越小，chunk 分裂及迁移越多，数据分布越均衡；反之，chunkSize 越大，chunk 分裂及迁移会更少，但可能导致数据分布不均。

* chunk 自动分裂只会在数据写入时触发，所以如果将 chunkSize 改小，系统需要一定的时间来将 chunk 分裂到指定的大小。
* chunk 只会分裂，不会合并，所以即使将 chunkSize 改大，现有的 chunk 数量不会减少，但 chunk 大小会随着写入不断增长，直到达到目标大小。

### 搭建集群

~~~shell
#创建3个config节点
docker create --name configsvr01  -p 17010:27019 -v mongoconfigsvr-data-01:/data/configdb mongo:4.0.3 --configsvr --replSet "rs_configsvr" --bind_ip_all
docker create --name configsvr02  -p 17011:27019 -v mongoconfigsvr-data-02:/data/configdb mongo:4.0.3 --configsvr --replSet "rs_configsvr" --bind_ip_all
docker create --name configsvr03  -p 17012:27019 -v mongoconfigsvr-data-03:/data/configdb mongo:4.0.3 --configsvr --replSet "rs_configsvr" --bind_ip_all
#启动服务
docker start configsvr01 configsvr02 configsvr03
#进去容器进行操作
docker exec -it configsvr01 /bin/bash
mongo 172.17.0.1:17010
#集群初始化
rs.initiate(
  {
    _id: "rs_configsvr",
    configsvr: true,
    members: [
      { _id : 0, host : "172.17.0.1:17010" },
      { _id : 1, host : "172.17.0.1:17011" },
      { _id : 2, host : "172.17.0.1:17012" }
    ]
  }
)
#创建2个shard集群，每个集群都有3个数据节点
#集群一
docker create --name shardsvr01 -p 37000:27018 -v mongoshardsvr-data-01:/data/db mongo:4.0.3 --replSet "rs_shardsvr1" --bind_ip_all --shardsvr
docker create --name shardsvr02 -p 37001:27018 -v mongoshardsvr-data-02:/data/db mongo:4.0.3 --replSet "rs_shardsvr1" --bind_ip_all --shardsvr
docker create --name shardsvr03 -p 37002:27018 -v mongoshardsvr-data-03:/data/db mongo:4.0.3 --replSet "rs_shardsvr1" --bind_ip_all --shardsvr

#集群二
docker create --name shardsvr04 -p 37003:27018 -v mongoshardsvr-data-04:/data/db mongo:4.0.3 --replSet "rs_shardsvr2" --bind_ip_all --shardsvr
docker create --name shardsvr05 -p 37004:27018 -v mongoshardsvr-data-05:/data/db mongo:4.0.3 --replSet "rs_shardsvr2" --bind_ip_all --shardsvr
docker create --name shardsvr06 -p 37005:27018 -v mongoshardsvr-data-06:/data/db mongo:4.0.3 --replSet "rs_shardsvr2" --bind_ip_all --shardsvr
#启动容器
docker start shardsvr01 shardsvr02 shardsvr03
docker start shardsvr04 shardsvr05 shardsvr06
#进去容器执行
docker exec -it shardsvr01 /bin/bash
mongo 172.17.0.1:37000
#集群初始化
rs.initiate(
  {
    _id: "rs_shardsvr1",
    members: [
      { _id : 0, host : "172.17.0.1:37000" },
      { _id : 1, host : "172.17.0.1:37001" },
      { _id : 2, host : "172.17.0.1:37002" }
    ]
  }
)
#集群初始化二
mongo 172.17.0.1:37003
rs.initiate(
  {
    _id: "rs_shardsvr2",
    members: [
      { _id : 0, host : "172.17.0.1:37003" },
      { _id : 1, host : "172.17.0.1:37004" },
      { _id : 2, host : "172.17.0.1:37005" }
    ]
  }
)
#创建mongos节点容器，需要指定config服务
docker create --name mongos -p 6666:27017 --entrypoint "mongos" mongo:4.0.3 --configdb rs_configsvr/172.17.0.1:17010,172.17.0.1:17011,172.17.0.1:17012 --bind_ip_all
docker start mongos
#进入容器执行
docker exec -it mongos /bin/bash
mongo 172.17.0.1:6666
#添加shard节点
sh.addShard("rs_shardsvr1/172.17.0.1:37000,172.17.0.1:37001,172.17.0.1:37002")
sh.addShard("rs_shardsvr2/172.17.0.1:37003,172.17.0.1:37004,172.17.0.1:37005")
#启用分片
sh.enableSharding("test")
#设置分片规则，按照_id的hash进行区分
sh.shardCollection("test.order", {"_id": "hashed" })
#插入测试数据
use test
for (i = 1; i <= 1000; i=i+1){
    db.order.insert({'id':i , 'price': 100+i})
}
#分别在2个shard集群中查询数据进行测试
db.order.count()
#集群操作（在mongos中执行）
use config
db.databases.find()  #列出所有数据库分片情况
db.collections.find() #查看分片的片键
sh.status() #查询分片集群的状态信息
~~~