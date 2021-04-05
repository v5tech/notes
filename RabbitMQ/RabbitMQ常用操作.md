# RabbitMQ常用操作

## 启动、停止、状态
```bash
# 前台启动
rabbitmq-server

# 后台启动
rabbitmq-server -deched

# 用于停止运行RabbitMQ的Erlang虚拟机和RabbitMQ服务应用。如果指定了pid_file,还需要等待指定进程的结束。其中pid_file是通过调用rabbitmq-server启动RabbitMQ服务时创建的，默认情况下存放于Mnesia目录中，可以通过RABBITMQ_PID_FILE这个环境变量来改变存放路径。注意，如果是rabbitmq-server –detach启动的RabbitMQ服务则不会生成pid_file这个文件。
rabbitmqctl stop [pid_file]

# 用于停止运行RabbitMQ的Erlang虚拟机和RabbitMQ服务应用。执行这个命令会阻塞直到Erlang虚拟机进程的退出。如果RabbitMQ没有成功关闭，则会返回一个非零值。这个命令和rabbitmqctl stop的不同的是，它不需要指定pid_file而可以阻塞等待指定进程的关闭。
rabbitmqctl shutdown

# 查询节点状态
rabbitmqctl status

# 启动RabbitMQ应用。此命令典型的用途是在执行了其他管理操作之后，重新启动之前停止的RabbitMQ应用，譬如rabbitmqctl reset。
rabbitmqctl start_app

# 停止RabbitMQ服务应用，但是Erlang虚拟机还是处于运行状态。此命令优先执行其他管理操作（这些管理操作需要先停止RabbitMQ应用），比如rabbitmqctl reset。
rabbitmqctl stop_app

# 等待RabbitMQ应用的启动。它会等到pid_file的创建，然后等待pid_file中的所代表的进程启动。当指定的进程没有启动RabbitMQ应用而关闭时将会返回失败。
rabbitmqctl wait [pid_file]

# 将RabbitMQ节点重置还原到最初状态，包括从原所在的集群中删除此节点，从管理数据库中删除所有的配置数据，如已配置的用户、vhost等，以及删除所有的持久化消息。执行rabbitmqctl reset命令前必须停止RabbitMQ应用（比如先执行rabbitmqctl stop_app）。
rabbitmqctl reset

# 强制将RabbitMQ节点重置还原到最初状态。不同于rabbitmqctl reset，rabbitmqctl force_reset命令不论当前管理数据库的状态和集群配置是什么，会无条件地重置节点。它只能在数据库或集群配置已损坏的情况下才可使用。与rabbitmqctl reset命令一下，执行rabbitmqctl force_reset命令前必须先停止RabbitMQ应用。
rabbitmqctl force_reset
```

## 集群操作

### 从集群中移除节点

```bash
# 停止RabbitMQ服务应用，但是Erlang虚拟机还是处于运行状态。此命令优先执行其他管理操作（这些管理操作需要先停止RabbitMQ应用），比如rabbitmqctl reset。
rabbitmqctl stop_app
# 将RabbitMQ节点重置还原到最初状态，包括从原所在的集群中删除此节点，从管理数据库中删除所有的配置数据，如已配置的用户、vhost等，以及删除所有的持久化消息。执行rabbitmqctl reset命令前必须停止RabbitMQ应用（比如先执行rabbitmqctl stop_app）。
rabbitmqctl reset
# 启动RabbitMQ应用。此命令典型的用途是在执行了其他管理操作之后，重新启动之前停止的RabbitMQ应用，譬如rabbitmqctl reset。
rabbitmqctl start_app
```

### 向集群中添加节点

```bash
# 关闭从节点
rabbitmqctl stop_app

# 重置节点
rabbitmqctl reset

# 将节点加入指定集群中。在这个命令执行前需要停止RabbitMQ应用并重置节点。RabbitMQ集群中的节点只有两种类型：内存节点(ram)/磁盘节点(disc)
rabbitmqctl join_cluster {cluster_node} [--ram|--disc]
# 如：rabbitmqctl join_cluster --ram rabbitmq@mqnode1

rabbitmqctl start_app

# 显示集群的状态。
rabbitmqctl cluster_status
```

### 从集群中剔除某节点

```bash
# 将节点从集群中删除，允许离线执行
rabbitmqctl forget_cluster_node [–offline] {rabbit_node_name}
```

### 重命名节点名称

```bash
rabbitmqctl rename_cluster_node 'rabbit@node1' 'rabbit@node1update'
```

### 更新节点

在集群中的节点应用启动前咨询clusternode节点的最新信息，并更新相应的集群信息。这个和join_cluster不同，它不加入集群。考虑这样一种情况，节点A和节点B都在集群中，当节点A离线了，节点C又和节点B组成了一个集群，然后节点B又离开了集群，当A醒来的时候，它会尝试联系节点B，但是这样会失败，因为节点B已经不在集群中了。rabbitmqctl update_cluster_nodes -n A C可以解决这种场景。

```bash
rabbitmqctl update_cluster_nodes {clusternode}
```

### 确保节点可以启动

确保节点可以启动，即使它不是最后一个关闭的节点。通常情况下，当关闭整个RabbitMQ集群时，重启的第一个节点应该是最后关闭的节点，因为它可以看到其它节点所看不到的事情。但是有时会有一些异常情况出现，比如整个集群都掉电而所有节点都认为它不是最后一个关闭的。在这种情况下，可以调用rabbitmqctl force_boot命令，这就告诉节点可以无条件的启动节点。在此节点关闭后，集群的任何变化，它都会丢失。如果最后一个关闭的节点永久丢失了，那么你需要优先使用rabbitmqctl forget_cluster_node --offline命令，因为它可以确保镜像队列的正常运转。

```bash
rabbitmqctl force_boot
```

### 更改节点类型

```bash
# 修改集群节点的类型。在这个命令执行前需要停止RabbitMQ应用。
rabbitmqctl change_cluster_node_type {disc|ram}

# 更改集群节点为硬盘模式
rabbitmqctl stop_app
rabbitmqctl change_cluster_node_type disc
rabbitmqctl start_app

# 更改集群节点为内存模式
rabbitmqctl stop_app
rabbitmqctl change_cluster_node_type ram
rabbitmqctl start_app
```

### 设置集群名称

设置集群名称。集群名称在客户端连接时会通报给客户端。Federation和Shovel插件也会有用到集群名称的地方。集群名称默认是集群中第一个节点的名称，通过这个命令可以重新设置。

```bash
rabbitmqctl set_cluster_name {name}
```

### 手动同步队列

指示未同步队列queue的slave镜像可以同步master镜像行的内容。同步期间此队列会被阻塞（所有此队列的生产消费者都会被阻塞），直到同步完成。此条命令执行成功的前提是队列queue配置了镜像。注意，未同步队列中的消息被耗尽后，最终也会变成同步，此命令主要用于未耗尽的队列。

```bash
rabbitmqctl sync_queue [-p vhost] {queue}
```

### 取消队列queue同步镜像的操作

```bash
rabbitmqctl cancel_sync_queue [-p vhost] {queue}
# 如：
rabbitmqctl cancel_sync_queue queue
```

## 配置镜像队列

设置集群为镜像模式

将所有队列设置为镜像队列，即队列会被复制到各个节点，各个节点状态一致

语法：set_policy {name} {pattern} {definition}

name：策略名，可自定义

pattern：队列的匹配模式（正则表达式）比如"^queue_" 表示对队列名称以“queue_”开头的所有队列进行镜像，而"^"表示匹配所有的队列

definition：镜像定义，包括三个部分ha-mode, ha-params, ha-sync-mode

ha-mode：（High Available，高可用）模式，指明镜像队列的模式，有效值为all/exactly/nodes，当前策略模式为 all，即复制到所有节点，包含新增节点

* all：表示在集群中所有的节点上进行镜像

* exactly：表示在指定个数的节点上进行镜像，节点的个数由ha-params指定

* nodes：表示在指定的节点上进行镜像，节点名称通过ha-params指定

ha-params：ha-mode模式需要用到的参数

ha-sync-mode：进行队列中消息的同步方式，有效值为automatic和manual

```bash
# 设置镜像策略
rabbitmqctl set_policy ha-all "^" '{"ha-mode":"all"}'

# 设置权限
rabbitmqctl set_permissions -p "/" admin ".*" ".*" ".*"
```

## 镜像策略


```bash
# 设置集群镜像策略
rabbitmqctl set_policy ha-all "^" '{"ha-mode":"all"}'

# 清除镜像策略
rabbitmqctl clear_policy [-p <vhost>] <name>

# 查看镜像策略
rabbitmqctl list_policies [-p <vhost>]
```

## 插件管理

```bash
# 启用管理插件
rabbitmq-plugins enable rabbitmq_management

# 禁用管理插件
rabbitmq-plugins disable rabbitmq_management

# 查看插件列表
rabbitmq-plugins list
```

## 队列管理

```bash
# 查看所有队列
rabbitmqctl list_queues

# 清除所有队列
rabbitmqctl reset
```

## 虚拟主机管理

```bash
# 查询虚拟主机
rabbitmqctl list_vhosts

# 创建虚拟主机
rabbitmqctl add_vhost <vhost_name>

# 删除虚拟主机
rabbitmqctl delete_vhost <vhost_name>
```

## 用户管理

```bash
# 添加用户
rabbitmqctl add_user <username> <password>

# 删除用户
rabbitmqctl delete_user <username>

# 查看用户
rabbitmqctl list_users

# 修改密码
rabbitmqctl change_password <username> <password>

# 清除用户密码
rabbitmqctl clear_password {username}

# 验证用户密码
rabbitmqctl authenticate_user <username> <password>
```

## 权限管理

```bash
# 给用户赋权限
rabbitmqctl set_permissions -p <VHostPath> <username> <conf-pattern> <write-pattern> <read-pattern>

# 示例如下
rabbitmqctl set_permissions -p "/" <username> ".*" ".*" ".*"

# 查看单个用户权限
rabbitmqctl list_user_permissions <username>

# 查看所有用户权限
rabbitmqctl list_permissions -p /

# 清除用户权限
rabbitmqctl clear_permissions [-p VHostPath] <username>
```

## 为用户赋角色

RabbitMQ 中的角色分为五类：none、management、policymaker、monitoring、administrator

RabbitMQ各类角色描述：

- none：无任何角色。新创建的用户的角色默认为none。
- management：可以访问Web管理页面。Web管理页面在5.3章节中会有详细介绍。
- policymaker：包含management的所有权限，并且可以管理策略（policy）和参数（parameter）。
- monitoring：包含management的所有权限，并且可以看到所有连接（connections）、信道（channels）以及节点相关的信息。
- administartor：包含monitoring的所有权限，并且可以管理用户、虚拟主机、权限、策略、参数等等。administator代表了最高的权限。

```bash
# 给指定用户赋予角色
rabbitmqctl set_user_tags <username> administrator

# 给用户赋角色，设置admin为administrator权限。
rabbitmqctl set_user_tags admin administrator
```

# 参考文档

https://hiddenpps.blog.csdn.net/article/details/54743481

