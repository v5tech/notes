# RabbitMQ 实战教程



## 1.MQ引言



### 1.1 什么是MQ



`MQ`(Message Quene) :  翻译为 `消息队列`,通过典型的 `生产者`和`消费者`模型,生产者不断向消息队列中生产消息，消费者不断的从队列中获取消息。因为消息的生产和消费都是异步的，而且只关心消息的发送和接收，没有业务逻辑的侵入,轻松的实现系统间解耦。别名为 `消息中间件`	通过利用高效可靠的消息传递机制进行平台无关的数据交流，并基于数据通信来进行分布式系统的集成。



### 1.2 MQ有哪些



当今市面上有很多主流的消息中间件，如老牌的`ActiveMQ`、`RabbitMQ`，炙手可热的`Kafka`，阿里巴巴自主开发`RocketMQ`等。



### 1.3 不同MQ特点



**ActiveMQ**

ActiveMQ 是Apache出品，最流行的，能力强劲的开源消息总线。它是一个完全支持JMS规范的的消息中间件。丰富的API,多种集群架构模式让ActiveMQ在业界成为老牌的消息中间件,在中小型企业颇受欢迎!



**Kafka**

Kafka是LinkedIn开源的分布式发布-订阅消息系统，目前归属于Apache顶级项目。Kafka主要特点是基于Pull的模式来处理消息消费，追求高吞吐量，一开始的目的就是用于日志收集和传输。0.8版本开始支持复制，不支持事务，对消息的重复、丢失、错误没有严格要求，适合产生大量数据的互联网服务的数据收集业务。



**RocketMQ**

RocketMQ是阿里开源的消息中间件，它是纯Java开发，具有高吞吐量、高可用性、适合大规模分布式系统应用的特点。RocketMQ思路起源于Kafka，但并不是Kafka的一个Copy，它对消息的可靠传输及事务性做了优化，目前在阿里集团被广泛应用于交易、充值、流计算、消息推送、日志流式处理、binglog分发等场景。



**RabbitMQ**

RabbitMQ是使用Erlang语言开发的开源消息队列系统，基于AMQP协议来实现。AMQP的主要特征是面向消息、队列、路由（包括点对点和发布/订阅）、可靠性、安全。AMQP协议更多用在企业系统内对数据一致性、稳定性和可靠性要求很高的场景，对性能和吞吐量的要求还在其次。



> RabbitMQ比Kafka可靠，Kafka更适合IO高吞吐的处理，一般应用在大数据日志处理或对实时性（少量延迟），可靠性（少量丢数据）要求稍低的场景使用，比如ELK日志收集。



## 2.RabbitMQ 的引言



### 2.1 RabbitMQ



> 基于`AMQP`协议，erlang语言开发，是部署最广泛的开源消息中间件,是最受欢迎的开源消息中间件之一。
> ![image](assets/eca13827-2774-4569-8300-50354cc16513.png)



`官网`: https://www.rabbitmq.com/

`官方教程`: https://www.rabbitmq.com/#getstarted



**AMQP 协议**

AMQP（advanced message queuing protocol）`在2003年时被提出，最早用于解决金融领不同平台之间的消息传递交互问题。顾名思义，AMQP是一种协议，更准确的说是一种binary wire-level protocol（链接协议）。这是其和JMS的本质差别，AMQP不从API层进行限定，而是直接定义网络交换的数据格式。这使得实现了AMQP的provider天然性就是跨平台的。以下是AMQP协议模型:

![img](assets/6694353c-c3ec-4fe1-b70c-1b56655e4a42.png)

### 2.2 RabbitMQ 的安装



#### 2.2.1 下载



`官网下载地址`: https://www.rabbitmq.com/download.html
![image](assets/85e1fae3-8181-4823-913b-a70fd10c1c0c.png)

> `最新版本`: 3.7.18



#### 2.2.2 下载的安装包



![img](assets/1983d692-0ec4-4a38-8cf7-8f77bdd26d19.png)

> `注意`:这里的安装包是centos7安装的包



#### 2.2.3 安装步骤



```
1.将rabbitmq安装包上传到linux系统中
  erlang-22.0.7-1.el7.x86_64.rpm
  rabbitmq-server-3.7.18-1.el7.noarch.rpm

2.安装Erlang依赖包
  rpm -ivh erlang-22.0.7-1.el7.x86_64.rpm

3.安装RabbitMQ安装包(需要联网)
  yum install -y rabbitmq-server-3.7.18-1.el7.noarch.rpm
    注意:默认安装完成后配置文件模板在:/usr/share/doc/rabbitmq-server-3.7.18/rabbitmq.config.example目录中,需要 
        将配置文件复制到/etc/rabbitmq/目录中,并修改名称为rabbitmq.config
4.复制配置文件
  cp /usr/share/doc/rabbitmq-server-3.7.18/rabbitmq.config.example /etc/rabbitmq/rabbitmq.config

5.查看配置文件位置
  ls /etc/rabbitmq/rabbitmq.config

6.修改配置文件(参见下图:)
  vim /etc/rabbitmq/rabbitmq.config
```

![img](assets/44c00148-d2c7-471a-bb45-8af27a25a0e2.png)

将上图中配置文件中红色部分去掉`%%`,以及最后的`,`逗号 修改为下图:
![image](assets/c2ab26cd-8b9c-4e0a-8484-38be14436600.png)

```
7.执行如下命令,启动rabbitmq中的插件管理
  rabbitmq-plugins enable rabbitmq_management
  
  出现如下说明:
    Enabling plugins on node rabbit@localhost:
    rabbitmq_management
    The following plugins have been configured:
      rabbitmq_management
      rabbitmq_management_agent
      rabbitmq_web_dispatch
    Applying plugin configuration to rabbit@localhost...
    The following plugins have been enabled:
      rabbitmq_management
      rabbitmq_management_agent
      rabbitmq_web_dispatch

    set 3 plugins.
    Offline change; changes will take effect at broker restart.

8.启动RabbitMQ的服务
  systemctl start rabbitmq-server
  systemctl restart rabbitmq-server
  systemctl stop rabbitmq-server
  
9.查看服务状态(见下图:)
  systemctl status rabbitmq-server
  ● rabbitmq-server.service - RabbitMQ broker
     Loaded: loaded (/usr/lib/systemd/system/rabbitmq-server.service; disabled; vendor preset: disabled)
     Active: active (running) since 三 2019-09-25 22:26:35 CST; 7s ago
   Main PID: 2904 (beam.smp)
     Status: "Initialized"
     CGroup: /system.slice/rabbitmq-server.service
             ├─2904 /usr/lib64/erlang/erts-10.4.4/bin/beam.smp -W w -A 64 -MBas ageffcbf -MHas ageffcbf -
             MBlmbcs...
             ├─3220 erl_child_setup 32768
             ├─3243 inet_gethost 4
             └─3244 inet_gethost 4
      .........
```

![img](assets/b73bb1c6-06cd-4dd1-b2c4-e746b0e3602a.png)

```
10.关闭防火墙服务
  systemctl disable firewalld
    Removed symlink /etc/systemd/system/multi-user.target.wants/firewalld.service.
    Removed symlink /etc/systemd/system/dbus-org.fedoraproject.FirewallD1.service.
  systemctl stop firewalld   

11.访问web管理界面
  http://10.15.0.8:15672/
```

![img](assets/07824a1b-acd2-42ef-b5bb-a96125ed0c59.png)

```
12.登录管理界面
  username:  guest
  password:  guest
```

![img](assets/77542f07-6d87-48ce-880f-f2b003e8abfc.png)



## 3. RabiitMQ 配置



### 3.1RabbitMQ 管理命令行



```
1.服务启动相关
    systemctl start|restart|stop|status rabbitmq-server

2.管理命令行  用来在不使用web管理界面情况下命令操作RabbitMQ
    rabbitmqctl  help  可以查看更多命令

3.插件管理命令行
    rabbitmq-plugins enable|list|disable
```



### 3.2 web管理界面介绍



#### 3.2.1 overview概览



![image](assets/e4625123-163f-41ab-a59d-0d7296cc85df.png)

- `connections：无论生产者还是消费者，都需要与RabbitMQ建立连接后才可以完成消息的生产和消费，在这里可以查看连接情况`

- `channels：通道，建立连接后，会形成通道，消息的投递获取依赖通道。`

- `Exchanges：交换机，用来实现消息的路由`

- `Queues：队列，即消息队列，消息存放在队列中，等待消费，消费后被移除队列。`



#### 3.2.2 Admin用户和虚拟主机管理



##### 1. 添加用户



![image](assets/ab6e069b-6762-4f80-86ba-62b49946061e.png)

上面的Tags选项，其实是指定用户的角色，可选的有以下几个：

- `超级管理员(administrator)`
  可登陆管理控制台，可查看所有的信息，并且可以对用户，策略(policy)进行操作。
- `监控者(monitoring)`
  可登陆管理控制台，同时可以查看rabbitmq节点的相关信息(进程数，内存使用情况，磁盘使用情况等)
- `策略制定者(policymaker)`
  可登陆管理控制台, 同时可以对policy进行管理。但无法查看节点的相关信息(上图红框标识的部分)。
- `普通管理者(management)`
  仅可登陆管理控制台，无法看到节点信息，也无法对策略进行管理。
- `其他`
  无法登陆管理控制台，通常就是普通的生产者和消费者。



##### 2. 创建虚拟主机



**虚拟主机**

为了让各个用户可以互不干扰的工作，RabbitMQ添加了虚拟主机（Virtual Hosts）的概念。其实就是一个独立的访问路径，不同用户使用不同路径，各自有自己的队列、交换机，互相不会影响。

![image](assets/6ee5f851-0c3b-4df4-9bde-65e4398c353a.png)



##### 3. 绑定虚拟主机和用户



创建好虚拟主机，我们还要给用户添加访问权限：

点击添加好的虚拟主机：
![image](assets/110b6423-18ee-406b-bfa2-5d169e820a0c.png)

进入虚拟机设置界面:

![image](assets/b44984c3-9840-4f22-be4d-05d07ddcb77a.png)



## 4.RabbitMQ 的第一个程序



### 4.0 AMQP协议的回顾

![image](assets/d4d406bd-ceb1-4ef7-a3bc-eac8fe899d2d.png)



### 4.1 RabbitMQ支持的消息模型

![image](assets/071130b3-4ef9-4615-b5b5-b48006ff4965.png)
![image](assets/620484c8-9ff1-485e-82cb-c8dd290bb568.png)



### 4.2 引入依赖

```xml
<dependency>
    <groupId>com.rabbitmq</groupId>
    <artifactId>amqp-client</artifactId>
    <version>5.7.2</version>
</dependency>
```



### 4.3 第一种模型(直连)



![img](assets/ed464869-21d9-488e-a8e9-0e441e52aa40.png)

在上图的模型中，有以下概念：

- P：生产者，也就是要发送消息的程序

- C：消费者：消息的接受者，会一直等待消息到来。

- queue：消息队列，图中红色部分。类似一个邮箱，可以缓存消息；生产者向其中投递消息，消费者从其中取出消息。

##### 1. 开发生产者

```java
  //创建连接工厂
  ConnectionFactory connectionFactory = new ConnectionFactory();
  connectionFactory.setHost("10.15.0.9");
  connectionFactory.setPort(5672);
  connectionFactory.setUsername("ems");
  connectionFactory.setPassword("123");
  connectionFactory.setVirtualHost("/ems");
  //获取连接对象
  Connection connection =  connectionFactory.newConnection();
  //获取连接中通道
  Channel channel = connection.createChannel();
  //通道绑定对应消息队列
  //参数1 ：队列名称 如果队列不存在 自动创建
  //参数2 ：用来定义队列特性是否要持久化true持久化队列 false不持久化
  //参数3： exclusive 是否独占队列 true 独占队列 false不独 占
  //参数4： autoDelete： 是否在消费完成后自动删除队列true自动删除 false不自动删除
  //参数5： 额外附加参数

  channel.queueDeclare("hello",true,false,false,null);
  //发布消息
  //参数参数1：交换机名称参数2：队列名称 参数3 ：传递消息额外设置参 数4 ：消息的具体内容
  channel.basicPublish("","hello", null,"hello rabbitmq".getBytes());
  channel.close();
  connection.close();
```

##### 2. 开发消费者

```java
  //创建连接工厂
  ConnectionFactory connectionFactory = new ConnectionFactory();
  connectionFactory.setHost("10.15.0.9");
  connectionFactory.setPort(5672);
  connectionFactory.setUsername("ems");
  connectionFactory.setPassword("123");
  connectionFactory.setVirtualHost("/ems");
  Connection connection = connectionFactory.newConnection();
  Channel channel = connection.createChannel();
  channel.queueDeclare("hello", true, false, false, null);
  //消费消息
  //参数1：消费那个队列的消息队列名称
  //参数2： 开始消息的自动确认机制
  //参数3 ： 消费时的回调接口|
  channel.basicConsume("hello",true,new DefaultConsumer(channel){
    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
      System.out.println(new String(body));
    }
  });
```

##### 3. 参数的说明

```java
  channel.queueDeclare("hello",true,false,false,null);
  '参数1':用来声明通道对应的队列
  '参数2':用来指定是否持久化队列
  '参数3':用来指定是否独占队列
  '参数4':用来指定是否自动删除队列
  '参数5':对队列的额外配置
```



### 4.4 第二种模型(work quene)



`Work queues`，也被称为（`Task queues`），任务模型。当消息处理比较耗时的时候，可能生产消息的速度会远远大于消息的消费速度。长此以往，消息就会堆积越来越多，无法及时处理。此时就可以使用work 模型：**让多个消费者绑定到一个队列，共同消费队列中的消息**。队列中的消息一旦消费，就会消失，因此任务是不会被重复执行的。

![image](assets/13f3202c-95c2-48c8-8494-c7e390a09903.png)

角色：

- P：生产者：任务的发布者

- C1：消费者-1，领取任务并且完成任务，假设完成速度较慢

- C2：消费者-2：领取任务并完成任务，假设完成速度快

##### 1. 开发生产者

```java
channel.queueDeclare("hello", true, false, false, null);
for (int i = 0; i < 10; i++) {
  channel.basicPublish("", "hello", null, (i+"====>:我是消息").getBytes());
}
```

##### 2.开发消费者-1

```java
channel.queueDeclare("hello",true,false,false,null);
channel.basicConsume("hello",true,new DefaultConsumer(channel){
  @Override
  public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
    System.out.println("消费者1: "+new String(body));
  }
});
```

##### 3.开发消费者-2

```java
channel.queueDeclare("hello",true,false,false,null);
channel.basicConsume("hello",true,new DefaultConsumer(channel){
  @Override
  public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
    try {
      Thread.sleep(1000);   //处理消息比较慢 一秒处理一个消息
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    System.out.println("消费者2: "+new String(body));  
  }
});
```

##### 4.测试结果

![image](assets/6044ec09-b022-44b0-9097-c5cc239e9aa9.png)
![image](assets/e393e444-9046-4727-989d-bef70a17101f.png)

> ```
> 总结:默认情况下，RabbitMQ将按顺序将每个消息发送给下一个使用者。平均而言，每个消费者都会收到相同数量的消息。这种分发消息的方式称为循环。
> ```

##### 5.消息自动确认机制

> Doing a task can take a few seconds. You may wonder what happens if one of the consumers starts a long task and dies with it only partly done. With our current code, once RabbitMQ delivers a message to the consumer it immediately marks it for deletion. In this case, if you kill a worker we will lose the message it was just processing. We'll also lose all the messages that were dispatched to this particular worker but were not yet handled.
>
> But we don't want to lose any tasks. If a worker dies, we'd like the task to be delivered to another worker.

```java
channel.basicQos(1);//一次只接受一条未确认的消息
//参数2:关闭自动确认消息
channel.basicConsume("hello",false,new DefaultConsumer(channel){
  @Override
  public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
    System.out.println("消费者1: "+new String(body));
    channel.basicAck(envelope.getDeliveryTag(),false);//手动确认消息
  }
});
```

- 设置通道一次只能消费一个消息

- 关闭消息的自动确认,开启手动确认消息
  ![image](assets/8cd61887-0363-4ad8-abd5-f3af3dbe8017.png)
  ![image](assets/47e7ba33-7e8c-4337-8412-24c47da64505.png)



### 4.5 第三种模型(fanout)

```
fanout 扇出 也称为广播
```
![img](assets/d695146c-8324-4674-9874-218477928f23.png)

在广播模式下，消息发送流程是这样的：

- 可以有多个消费者

- 每个**消费者有自己的queue**（队列）

- 每个**队列都要绑定到Exchange**（交换机）

- **生产者发送的消息，只能发送到交换机**，交换机来决定要发给哪个队列，生产者无法决定。

- 交换机把消息发送给绑定过的所有队列

- 队列的消费者都能拿到消息。实现一条消息被多个消费者消费

##### 1. 开发生产者

```java
//声明交换机
channel.exchangeDeclare("logs","fanout");//广播 一条消息多个消费者同时消费
//发布消息
channel.basicPublish("logs","",null,"hello".getBytes());
```

##### 2. 开发消费者-1

```java
//绑定交换机
channel.exchangeDeclare("logs","fanout");
//创建临时队列
String queue = channel.queueDeclare().getQueue();
//将临时队列绑定exchange
channel.queueBind(queue,"logs","");
//处理消息
channel.basicConsume(queue,true,new DefaultConsumer(channel){
  @Override
  public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
    System.out.println("消费者1: "+new String(body));
  }
});
```

##### 3. 开发消费者-2

```java
//绑定交换机
channel.exchangeDeclare("logs","fanout");
//创建临时队列
String queue = channel.queueDeclare().getQueue();
//将临时队列绑定exchange
channel.queueBind(queue,"logs","");
//处理消息
channel.basicConsume(queue,true,new DefaultConsumer(channel){
  @Override
  public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
    System.out.println("消费者2: "+new String(body));
  }
});
```

##### 4.开发消费者-3

```java
//绑定交换机
channel.exchangeDeclare("logs","fanout");
//创建临时队列
String queue = channel.queueDeclare().getQueue();
//将临时队列绑定exchange
channel.queueBind(queue,"logs","");
//处理消息
channel.basicConsume(queue,true,new DefaultConsumer(channel){
  @Override
  public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
    System.out.println("消费者3: "+new String(body));
  }
});
```

##### 5. 测试结果

![img](assets/7abbc601-2749-477c-a555-b5dd3a515a86.png)
![img](assets/be9a3c94-06ac-4499-97ab-5c3d480fbfe0.png)
![img](assets/32acb6db-9309-4221-96d1-7edf1974a893.png)



### 4.6 第四种模型(Routing)



#### 4.6.1 Routing 之订阅模型-Direct(直连)

```
在Fanout模式中，一条消息，会被所有订阅的队列都消费。但是，在某些场景下，我们希望不同的消息被不同的队列消费。这时就要用到Direct类型的Exchange。
```

在Direct模型下：

- 队列与交换机的绑定，不能是任意绑定了，而是要指定一个`RoutingKey`（路由key）

- 消息的发送方在 向 Exchange发送消息时，也必须指定消息的 `RoutingKey`。

- Exchange不再把消息交给每一个绑定的队列，而是根据消息的`Routing Key`进行判断，只有队列的`Routingkey`与消息的 `Routing key`完全一致，才会接收到消息

流程:

![img](assets/295bf75a-b89e-4425-952c-ca4258bed387.png)

图解：

- P：生产者，向Exchange发送消息，发送消息时，会指定一个routing key。

- X：Exchange（交换机），接收生产者的消息，然后把消息递交给 与routing key完全匹配的队列

- C1：消费者，其所在队列指定了需要routing key 为 error 的消息

- C2：消费者，其所在队列指定了需要routing key 为 info、error、warning 的消息

##### 1. 开发生产者

```java
//声明交换机  参数1:交换机名称 参数2:交换机类型 基于指令的Routing key转发
channel.exchangeDeclare("logs_direct","direct");
String key = "";
//发布消息
channel.basicPublish("logs_direct",key,null,("指定的route key"+key+"的消息").getBytes());
```

##### 2.开发消费者-1

```java
 //声明交换机
channel.exchangeDeclare("logs_direct","direct");
//创建临时队列
String queue = channel.queueDeclare().getQueue();
//绑定队列和交换机
channel.queueBind(queue,"logs_direct","error");
channel.queueBind(queue,"logs_direct","info");
channel.queueBind(queue,"logs_direct","warn");

//消费消息
channel.basicConsume(queue,true,new DefaultConsumer(channel){
  @Override
  public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
    System.out.println("消费者1: "+new String(body));
  }
});
```

##### 3.开发消费者-2

```java
//声明交换机
channel.exchangeDeclare("logs_direct","direct");
//创建临时队列
String queue = channel.queueDeclare().getQueue();
//绑定队列和交换机
channel.queueBind(queue,"logs_direct","error");
//消费消息
channel.basicConsume(queue,true,new DefaultConsumer(channel){
  @Override
  public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
    System.out.println("消费者2: "+new String(body));
  }
});
```

##### 4.测试生产者发送Route key为error的消息时

![image](assets/2224e049-a049-4838-8bff-4c9f92436a45.png)
![image](assets/da2b3ee8-b9a2-4a18-899b-9538476b6697.png)

##### 5.测试生产者发送Route key为info的消息时

![img](assets/f26fe559-9b75-4fbe-b0cd-ec3cff79668a.png)
![image](assets/cbf39a7c-543b-43b4-b52e-e843461bcb38.png)



#### 4.6.2 Routing 之订阅模型-Topic



Topic`类型的`Exchange`与`Direct`相比，都是可以根据`RoutingKey`把消息路由到不同的队列。只不过`Topic`类型`Exchange`可以让队列在绑定`Routing key` 的时候使用通配符！这种模型`Routingkey` 一般都是由一个或多个单词组成，多个单词之间以”.”分割，例如： `item.insert

![image](assets/c6c4faed-120c-4fdb-b5eb-58322a2f2cc2.png)

```
# 统配符
     * (star) can substitute for exactly one word.    匹配不多不少恰好1个词
     # (hash) can substitute for zero or more words.  匹配一个或多个词
# 如:
     audit.#    匹配audit.irs.corporate或者 audit.irs 等
     audit.*   只能匹配 audit.irs
```

##### 1.开发生产者

```java
//生命交换机和交换机类型 topic 使用动态路由(通配符方式)
channel.exchangeDeclare("topics","topic");
String routekey = "user.save";//动态路由key
//发布消息
channel.basicPublish("topics",routekey,null,("这是路由中的动态订阅模型,route key: ["+routekey+"]").getBytes());
```

##### 2.开发消费者-1

Routing Key中使用*通配符方式

```java
 //声明交换机
channel.exchangeDeclare("topics","topic");
//创建临时队列
String queue = channel.queueDeclare().getQueue();
//绑定队列与交换机并设置获取交换机中动态路由
channel.queueBind(queue,"topics","user.*");

//消费消息
channel.basicConsume(queue,true,new DefaultConsumer(channel){
  @Override
  public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
    System.out.println("消费者1: "+new String(body));
  }
});
```

##### 3.开发消费者-2

Routing Key中使用#通配符方式

```java
//声明交换机
channel.exchangeDeclare("topics","topic");
//创建临时队列
String queue = channel.queueDeclare().getQueue();
//绑定队列与交换机并设置获取交换机中动态路由
channel.queueBind(queue,"topics","user.#");

//消费消息
channel.basicConsume(queue,true,new DefaultConsumer(channel){
  @Override
  public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
    System.out.println("消费者2: "+new String(body));
  }
});
```

##### 4.测试结果

![image](assets/78589ef7-ef34-45e4-bbe4-843375342322.png)
![image](assets/ffd7ef50-0c4e-4b48-a7f3-78d9ed173d0e.png)



## 5. SpringBoot中使用RabbitMQ



### 5.0 搭建初始环境



##### 1. 引入依赖

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

##### 2. 配置配置文件

```yaml
spring:
  application:
    name: springboot_rabbitmq
  rabbitmq:
    host: 10.15.0.9
    port: 5672
    username: ems
    password: 123
    virtual-host: /ems
```

`RabbitTemplate`  用来简化操作   使用时候直接在项目中注入即可使用



### 5.1 第一种hello world模型使用



1. ##### 开发生产者

```java
@Autowired
   private RabbitTemplate rabbitTemplate;
   
   @Test
   public void testHello(){
     rabbitTemplate.convertAndSend("hello","hello world");
   }
```

2. ##### 开发消费者

```java
@Component
   @RabbitListener(queuesToDeclare = @Queue("hello"))
   public class HelloCustomer {
   
       @RabbitHandler
       public void receive1(String message){
           System.out.println("message = " + message);
       }
   }
```



### 5.2 第二种work模型使用



1. ##### 开发生产者

```java
@Autowired
   private RabbitTemplate rabbitTemplate;
   
   @Test
   public void testWork(){
     for (int i = 0; i < 10; i++) {
       rabbitTemplate.convertAndSend("work","hello work!");
     }
   }
```

2. ##### 开发消费者

```java
@Component
   public class WorkCustomer {
       @RabbitListener(queuesToDeclare = @Queue("work"))
       public void receive1(String message){
           System.out.println("work message1 = " + message);
       }
   
       @RabbitListener(queuesToDeclare = @Queue("work"))
       public void receive2(String message){
           System.out.println("work message2 = " + message);
       }
   }
```

> `说明:默认在Spring AMQP实现中Work这种方式就是公平调度,如果需要实现能者多劳需要额外配置`



### 5.3 Fanout 广播模型



1. ##### 开发生产者

```java
@Autowired
   private RabbitTemplate rabbitTemplate;
   
   @Test
   public void testFanout() throws InterruptedException {
     rabbitTemplate.convertAndSend("logs","","这是日志广播");
   }
```

2. ##### 开发消费者

```java
@Component
   public class FanoutCustomer {
   
       @RabbitListener(bindings = @QueueBinding(
               value = @Queue,
               exchange = @Exchange(name="logs",type = "fanout")
       ))
       public void receive1(String message){
           System.out.println("message1 = " + message);
       }
   
       @RabbitListener(bindings = @QueueBinding(
               value = @Queue, //创建临时队列
               exchange = @Exchange(name="logs",type = "fanout")  //绑定交换机类型
       ))
       public void receive2(String message){
           System.out.println("message2 = " + message);
       }
   }
```



### 5.4 Route 路由模型



1. ##### 开发生产者

```java
@Autowired
   private RabbitTemplate rabbitTemplate;
   
   @Test
   public void testDirect(){
     rabbitTemplate.convertAndSend("directs","error","error 的日志信息");
   }
```

2. ##### 开发消费者

```java
@Component
   public class DirectCustomer {
   
       @RabbitListener(bindings ={
               @QueueBinding(
                       value = @Queue(),
                       key={"info","error"},
                       exchange = @Exchange(type = "direct",name="directs")
               )})
       public void receive1(String message){
           System.out.println("message1 = " + message);
       }
   
       @RabbitListener(bindings ={
               @QueueBinding(
                       value = @Queue(),
                       key={"error"},
                       exchange = @Exchange(type = "direct",name="directs")
               )})
       public void receive2(String message){
           System.out.println("message2 = " + message);
       }
   }
```



### 5.5 Topic 订阅模型(动态路由模型)



1. ##### 开发生产者

```java
@Autowired
   private RabbitTemplate rabbitTemplate;
   
   //topic
   @Test
   public void testTopic(){
     rabbitTemplate.convertAndSend("topics","user.save.findAll","user.save.findAll 的消息");
   }
```

2. ##### 开发消费者

```java
@Component
   public class TopCustomer {
       @RabbitListener(bindings = {
               @QueueBinding(
                       value = @Queue,
                       key = {"user.*"},
                       exchange = @Exchange(type = "topic",name = "topics")
               )
       })
       public void receive1(String message){
           System.out.println("message1 = " + message);
       }
   
       @RabbitListener(bindings = {
               @QueueBinding(
                       value = @Queue,
                       key = {"user.#"},
                       exchange = @Exchange(type = "topic",name = "topics")
               )
       })
       public void receive2(String message){
           System.out.println("message2 = " + message);
       }
   }
```



## 6. MQ的应用场景



### 6.1 异步处理



```
场景说明：用户注册后，需要发注册邮件和注册短信,传统的做法有两种 1.串行的方式 2.并行的方式
```

- `串行方式:` 将注册信息写入数据库后,发送注册邮件,再发送注册短信,以上三个任务全部完成后才返回给客户端。 这有一个问题是,邮件,短信并不是必须的,它只是一个通知,而这种做法让客户端等待没有必要等待的东西.

![image](assets/eff89b6a-1138-4f19-b80f-1ae101f2fe08.png)

- `并行方式:`将注册信息写入数据库后,发送邮件的同时,发送短信,以上三个任务完成后,返回给客户端,并行的方式能提高处理的时间。

![img](assets/ab3acc56-c38a-408a-8017-a40724409c12.png)

- `消息队列:`假设三个业务节点分别使用50ms,串行方式使用时间150ms,并行使用时间100ms。虽然并行已经提高的处理时间,但是,前面说过,邮件和短信对我正常的使用网站没有任何影响，客户端没有必要等着其发送完成才显示注册成功,应该是写入数据库后就返回.  `消息队列`: 引入消息队列后，把发送邮件,短信不是必须的业务逻辑异步处理

![img](assets/ab3acc56-c38a-408a-8017-a40724409c12.png)
由此可以看出,引入消息队列后，用户的响应时间就等于写入数据库的时间+写入消息队列的时间(可以忽略不计),引入消息队列后处理后,响应时间是串行的3倍,是并行的2倍。



### 6.2 应用解耦



```
场景：双11是购物狂节,用户下单后,订单系统需要通知库存系统,传统的做法就是订单系统调用库存系统的接口.
```

![img](assets/ab3acc56-c38a-408a-8017-a40724409c12.png)
这种做法有一个缺点:

当库存系统出现故障时,订单就会失败。 订单系统和库存系统高耦合.  引入消息队列

![image](assets/ba134596-8a18-425a-8491-56949f75a48d.png)

- `订单系统:`用户下单后,订单系统完成持久化处理,将消息写入消息队列,返回用户订单下单成功。

- `库存系统:`订阅下单的消息,获取下单消息,进行库操作。  就算库存系统出现故障,消息队列也能保证消息的可靠投递,不会导致消息丢失.



### 6.3 流量削峰



`场景:` 秒杀活动，一般会因为流量过大，导致应用挂掉,为了解决这个问题，一般在应用前端加入消息队列。

作用:

1.可以控制活动人数，超过此一定阀值的订单直接丢弃(我为什么秒杀一次都没有成功过呢^^)

2.可以缓解短时间的高流量压垮应用(应用程序按自己的最大处理能力获取订单)

![img](assets/da796e96-be17-4ca8-88bf-1d530b1c264d.png)
1.用户的请求,服务器收到之后,首先写入消息队列,加入消息队列长度超过最大值,则直接抛弃用户请求或跳转到错误页面.

2.秒杀业务根据消息队列中的请求信息，再做后续处理.



## 7. RabbitMQ的集群



### 7.1 集群架构



#### 7.1.1 普通集群(副本集群)



> All data/state required for the operation of a RabbitMQ broker is replicated across all nodes. An exception to this are message queues, which by default reside on one node, though they are visible and reachable from all nodes. To replicate queues across nodes in a cluster  --摘自官网

```
默认情况下:RabbitMQ代理操作所需的所有数据/状态都将跨所有节点复制。这方面的一个例外是消息队列，默认情况下，消息队列位于一个节点上，尽管它们可以从所有节点看到和访问
```

1. ##### 架构图

![image](assets/1c05d8c5-d42c-42e9-95f8-11845e6731d7.png)

核心解决问题:  `当集群中某一时刻master节点宕机,可以对Quene中信息,进行备份`

2. ##### 集群搭建

```
0.集群规划
node1: 10.15.0.3  mq1  master 主节点
node2: 10.15.0.4  mq2  repl1  副本节点
node3: 10.15.0.5  mq3  repl2  副本节点
   
1.克隆三台机器主机名和ip映射
vim /etc/hosts加入:
10.15.0.3 mq1
10.15.0.4 mq2
10.15.0.5 mq3
node1: vim /etc/hostname 加入:  mq1
node2: vim /etc/hostname 加入:  mq2
node3: vim /etc/hostname 加入:  mq3
   
2.三个机器安装rabbitmq,并同步cookie文件,在node1上执行:
scp /var/lib/rabbitmq/.erlang.cookie root@mq2:/var/lib/rabbitmq/
scp /var/lib/rabbitmq/.erlang.cookie root@mq3:/var/lib/rabbitmq/
   
3.查看cookie是否一致:
node1: cat /var/lib/rabbitmq/.erlang.cookie 
node2: cat /var/lib/rabbitmq/.erlang.cookie 
node3: cat /var/lib/rabbitmq/.erlang.cookie 
   
4.后台启动rabbitmq所有节点执行如下命令,启动成功访问管理界面:
rabbitmq-server -detached 
   
5.在node2和node3执行加入集群命令:
  1.关闭       rabbitmqctl stop_app
  2.加入集群    rabbitmqctl join_cluster rabbit@mq1
  3.启动服务    rabbitmqctl start_app
   
6.查看集群状态,任意节点执行:
rabbitmqctl cluster_status
   
7.如果出现如下显示,集群搭建成功:
  Cluster status of node rabbit@mq3 ...
  [{nodes,[{disc,[rabbit@mq1,rabbit@mq2,rabbit@mq3]}]},
  {running_nodes,[rabbit@mq1,rabbit@mq2,rabbit@mq3]},
  {cluster_name,<<"rabbit@mq1">>},
  {partitions,[]},
  {alarms,[{rabbit@mq1,[]},{rabbit@mq2,[]},{rabbit@mq3,[]}]}]
   
8.登录管理界面,展示如下状态:
```

![image](assets/cd4de134-59af-48f0-b834-1a6c6eb8a872.png)

```
9.测试集群在node1上,创建队列
```

![image](assets/ae6d9aac-9d78-4053-a13a-a16845424042.png)

```
10.查看node2和node3节点:
```

![image](assets/144572e4-2581-427a-9715-4059576f2bae.png)

![image](assets/6d3b3cc5-1a35-495e-a10b-b80a4d89f9dd.png)

```bash
11.关闭node1节点,执行如下命令,查看node2和node3:
rabbitmqctl stop_app
```

![image](assets/9bbd8ae7-a65d-429d-8071-872c9105e350.png)

![image](assets/d96f326f-d0bb-4433-a73c-5067dcc9f031.png)



#### 7.1.2 镜像集群



> This guide covers mirroring (queue contents replication) of classic queues  --摘自官网
>
> By default, contents of a queue within a RabbitMQ cluster are located on a single node (the node on which the queue was declared). This is in contrast to exchanges and bindings, which can always be considered to be on all nodes. Queues can optionally be made *mirrored* across multiple nodes. --摘自官网

```
镜像队列机制就是将队列在三个节点之间设置主从关系，消息会在三个节点之间进行自动同步，且如果其中一个节点不可用，并不会导致消息丢失或服务不可用的情况，提升MQ集群的整体高可用性。
```

1. ##### 集群架构图

![image](assets/4b42952c-f231-45a3-b49b-a712ed4320a6.png)

2. ##### 配置集群架构

```
0.策略说明
rabbitmqctl set_policy [-p <vhost>] [--priority <priority>] [--apply-to <apply-to>] <name> <pattern>  <definition> 
-p Vhost： 可选参数，针对指定vhost下的queue进行设置
Name:     policy的名称
Pattern: queue的匹配模式(正则表达式)
Definition：镜像定义，包括三个部分ha-mode, ha-params, ha-sync-mode
            ha-mode:指明镜像队列的模式，有效值为 all/exactly/nodes
                    all：表示在集群中所有的节点上进行镜像
                    exactly：表示在指定个数的节点上进行镜像，节点的个数由ha-params指定
                    nodes：表示在指定的节点上进行镜像，节点名称通过ha-params指定
            ha-params：ha-mode模式需要用到的参数
            ha-sync-mode：进行队列中消息的同步方式，有效值为automatic和manual
            priority：可选参数，policy的优先级       
                   
                    
1.查看当前策略
rabbitmqctl list_policies
   
2.添加策略
rabbitmqctl set_policy ha-all '^hello' '{"ha-mode":"all","ha-sync-mode":"automatic"}' 
说明:策略正则表达式为 “^” 表示所有匹配所有队列名称  ^hello:匹配hello开头队列
   
3.删除策略
rabbitmqctl clear_policy ha-all
   
4.测试集群
```