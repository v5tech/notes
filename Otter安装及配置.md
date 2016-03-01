> 环境说明

> ubuntu 12.04

> s1 192.168.64.128 zookeeper manager node mysql

> s2 192.168.64.129 zookeeper node mysql

> s3 192.168.64.130 zookeeper

### 1、mysql安装

```
 sudo apt-get install mysql-server
 sudo mysql_install_db
 sudo mysql_secure_installation
```

### 2、配置mysql

```
vi /etc/mysql/my.cnf

server-id		= 1
log-bin                 = mysql-bin
binlog_format           = ROW

server-id		= 2
log-bin                 = mysql-bin
binlog_format           = ROW
```

### 3、查看mysql binlog_format

```
mysql> show variables like '%binlog_format%';
+---------------+-------+
| Variable_name | Value |
+---------------+-------+
| binlog_format | ROW   |
+---------------+-------+
1 row in set (0.00 sec)
```

### 4、创建mysql用户帐号并分配权限

启动数据库，分别在192.168.64.128、192.168.64.129库上分配otter的数据库账号和密码如canal/canal

```
CREATE USER canal IDENTIFIED BY 'canal'; -- 创建数据库帐号密码canal/canal

GRANT SELECT, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'canal'@'%'; -- 赋权限

GRANT ALL PRIVILEGES ON *.* TO 'canal'@'%' ; -- 赋权限

FLUSH PRIVILEGES;

SHOW GRANTS FOR 'canal';
```

### 5、清除匿名用户

```
use mysql;					--进入mysql库
select user,host,password from mysql.user;	--查询用户列表
delete from mysql.user where user='';		--删除匿名用户
```

### 6、修改数据库字符编码

http://stackoverflow.com/questions/3513773/change-mysql-default-character-set-to-utf-8-in-my-cnf

```
mysql> show variables like 'character_set_%';

[client]
default-character-set=utf8

[mysqld]
collation-server = utf8_unicode_ci
init-connect = 'SET NAMES utf8'
character-set-server = utf8
```

### 7、安装otter manager

```
ubuntu@s1:~/apps$ mkdir manager
ubuntu@s1:~/apps$ cd manager
ubuntu@s1:~/apps$ tar zxvf manager.deployer-4.2.12.tar.gz -C manager
```

### 8、安装manager数据库

```
ubuntu@s1:~/apps$ wget https://raw.github.com/alibaba/otter/master/manager/deployer/src/main/resources/sql/otter-manager-schema.sql 
ubuntu@s1:~/apps$ mysql -uroot -proot
mysql> source otter-manager-schema.sql
```

### 9、修改manager的配置文件

```
sudo vim manager/conf/otter.properties

## otter manager domain name
otter.domainName = 192.168.64.128
## otter manager http port
otter.port = 8080
## jetty web config xml
otter.jetty = jetty.xml

## otter manager database config
otter.database.driver.class.name = com.mysql.jdbc.Driver
otter.database.driver.url = jdbc:mysql://192.168.64.128:3306/otter
otter.database.driver.username = canal
otter.database.driver.password = canal

## otter communication port
otter.communication.manager.port = 1099

## otter communication pool size
otter.communication.pool.size = 10

## default zookeeper address
otter.zookeeper.cluster.default = 192.168.64.128:2181
## default zookeeper sesstion timeout = 60s
otter.zookeeper.sessionTimeout = 60000

## otter arbitrate connect manager config
otter.manager.address = ${otter.domainName}:${otter.communication.manager.port}

## should run in product mode , true/false
otter.manager.productionMode = true

## self-monitor enable or disable
otter.manager.monitor.self.enable = true
## self-montir interval , default 120s
otter.manager.monitor.self.interval = 120
## auto-recovery paused enable or disable
otter.manager.monitor.recovery.paused = true
# manager email user config
otter.manager.monitor.email.host = smtp.gmail.com
otter.manager.monitor.email.username = 
otter.manager.monitor.email.password = 
otter.manager.monitor.email.stmp.port = 465
```

### 10、启动otter manager

```
ubuntu@s1:~/apps/manager/bin$ ./startup.sh
```

### 11、查看启动日志

```
ubuntu@s1:~/apps/manager/logs$ cat manager.log 
2016-02-29 15:15:29.527 [] INFO  com.alibaba.otter.manager.deployer.OtterManagerLauncher - ## start the manager server.
2016-02-29 15:15:50.784 [] INFO  com.alibaba.otter.manager.deployer.JettyEmbedServer - ##Jetty Embed Server is startup!
2016-02-29 15:15:50.785 [] INFO  com.alibaba.otter.manager.deployer.OtterManagerLauncher - ## the manager server is running now ......
```

http://192.168.64.128:8080/login.htm
admin/admin

### 12、停止otter manager

```
ubuntu@s1:~/apps/manager/bin$ ./stop.sh
```

### 13、安装g++

```
ubuntu@s1:~/apps$ sudo apt-get install build-essential
```

```
ubuntu@s2:~/apps$ sudo apt-get install build-essential
```

### 14、安装aria2

http://sourceforge.net/projects/aria2/files/stable/
http://nchc.dl.sourceforge.net/project/aria2/stable/aria2-1.19.0/aria2-1.19.0.tar.gz

```
ubuntu@s1:~/apps$ tar zxvf aria2-1.19.0.tar.gz -C aria2
ubuntu@s1:~/apps/aria2$ ./configure
ubuntu@s1:~/apps/aria2$ make 
ubuntu@s1:~/apps/aria2$ make install
```

```
ubuntu@s2:~/apps$ tar zxvf aria2-1.19.0.tar.gz -C aria2
ubuntu@s2:~/apps/aria2$ ./configure
ubuntu@s2:~/apps/aria2$ make 
ubuntu@s2:~/apps/aria2$ make install
```

### 15、安装node

```
ubuntu@s1:~/apps$ mkdir node
ubuntu@s1:~/apps$ tar zxvf node.deployer-4.2.12.tar.gz -C node
```

```
ubuntu@s2:~/apps$ mkdir node
ubuntu@s2:~/apps$ tar zxvf node.deployer-4.2.12.tar.gz -C node
```

### 16、配置node

```
ubuntu@s1:~/apps$ vim node/conf/otter.properties 

## otter arbitrate & node connect manager config
otter.manager.address = 192.168.64.128:1099
```

```
ubuntu@s2:~/apps$ vim node/conf/otter.properties 

## otter arbitrate & node connect manager config
otter.manager.address = 192.168.64.128:1099
```

### 17、启动node

在node/conf目录中写入nid

```
ubuntu@s1:~/apps/node$ echo 1 > conf/nid
ubuntu@s1:~/apps/node$ bin/startup.sh
```

```
ubuntu@s2:~/apps/node$ echo 2 > conf/nid
ubuntu@s2:~/apps/node$ bin/startup.sh
```

https://github.com/alibaba/otter/wiki/Adminguide