# MySQL主从复制读写分离（基于Atlas2.2实现）

> Ubuntu 12.04.5 LTS + MySQL 5.5.47 + Atlas2.2

### 安装Atlas2.2

```
wget https://github.com/Qihoo360/Atlas/releases/download/2.2/Atlas-2.2-debian7.0-x86_64.deb
sudo dpkg -i Atlas-2.2-debian7.0-x86_64.deb
```

### 分别在主库从库创建帐号并授权

```
grant all on *.* to atlas@'192.168.64.%' identified by 'atlas';
flush privileges;
```

### 使用encrypt加密密码

```
ubuntu@s4:/usr/local/mysql-proxy/bin$ ./encrypt atlas
KsWNCR6qyNk=
```

`atlas`为数据库原始密码，`KsWNCR6qyNk=`为加密后的密码，该值在配置文件的`pwds`项中将用到。

### 修改配置

/usr/local/mysql-proxy/conf/test.conf

```
[mysql-proxy]

#管理接口的用户名
admin-username = mysqlproxy

#管理接口的密码
admin-password = mysqlproxy

#实现管理接口的Lua脚本所在路径
admin-lua-script = /usr/local/mysql-proxy/lib/mysql-proxy/lua/admin.lua

#Atlas后端连接的MySQL主库的IP和端口，可设置多项，用逗号分隔
proxy-backend-addresses = 192.168.64.131:3306

#Atlas后端连接的MySQL从库的IP和端口，@后面的数字代表权重，用来作负载均衡，若省略则默认为1，可设置多项，用逗号分隔
proxy-read-only-backend-addresses = 192.168.64.132:3306@1

#设置Atlas的运行方式，设为true时为守护进程方式，设为false时为前台方式，一般开发调试时设为false，线上运行时设为true
daemon = true

#设置Atlas的运行方式，设为true时Atlas会启动两个进程，一个为monitor，一个为worker，monitor在worker意外退出后会自动将其重启，设为false时只有worker，没有monitor，一般开发调试时设为false，线上运行时设为true
keepalive = true

#工作线程数，推荐设置与系统的CPU核数相等
event-threads = 4

#日志级别，分为message、warning、critical、error、debug五个级别
log-level = message

#日志存放的路径
log-path = /usr/local/mysql-proxy/log

#SQL日志的开关，可设置为OFF、ON、REALTIME，OFF代表不记录SQL日志，ON代表记录SQL日志，REALTIME代表记录SQL日志且实时写入磁盘，默认为OFF
sql-log = REALTIME

#实例名称，用于同一台机器上多个Atlas实例间的区分
instance = test

#Atlas监听的工作接口IP和端口
proxy-address = 0.0.0.0:1234

#Atlas监听的管理接口IP和端口
admin-address = 0.0.0.0:2345

#连接池的最小空闲连接数，应设为event-threads的整数倍，可根据业务请求量大小适当调大或调小
min-idle-connections = 8

#分表设置，此例中person为库名，mt为表名，id为分表字段，3为子表数量，可设置多项，以逗号分隔，若不分表则不需要设置该项
#tables = person.mt.id.3

#用户名与其对应的加密过的MySQL密码，密码使用PREFIX/bin目录下的加密程序encrypt加密，此设置项用于多个用户名同时访问同一个Atlas实例的情况，若只有一个用户名则不需要设置该项
#pwds = user1:+jKsgB3YAG8=, user2:GS+tr4TPgqc=
#用户名为atlas密码为明文使用encrypt加密后的串 需要分别在主从库中创建该帐号并授权，否则使用atals客户端连接不上
pwds = atlas:KsWNCR6qyNk=

#默认字符集，若不设置该项，则默认字符集为latin1
#charset = utf8

#允许连接Atlas的客户端的IP，可以是精确IP，也可以是IP段，以逗号分隔，若不设置该项则允许所有IP连接，否则只允许列表中的IP连接
#client-ips = 127.0.0.1, 192.168.1

#Atlas前面挂接的LVS的物理网卡的IP(注意不是虚IP)，若有LVS且设置了client-ips则此项必须设置，否则可以不设置
#lvs-ips = 192.168.1.1
```

### 启动Atlas

```bash
ubuntu@s4:/usr/local/mysql-proxy/bin$ sudo ./mysql-proxyd test start
OK: MySQL-Proxy of test is started
```

```
ubuntu@s4:/usr/local/mysql-proxy/bin$ sudo ./mysql-proxyd test stop    #停止
ubuntu@s4:/usr/local/mysql-proxy/bin$ sudo ./mysql-proxyd test restart #重启
```

注意：
(1). 运行文件是：mysql-proxyd(不是mysql-proxy)。
(2). test是conf目录下配置文件的名字，也是配置文件里instance项的名字，三者需要统一。


### 查看Atlas进程

```bash
ps -ef | grep mysql-proxy|grep -v grep
root       4628      1  0 11:54 ?        00:00:00 /usr/local/mysql-proxy/bin/mysql-proxy --defaults-file=/usr/local/mysql-proxy/conf/test.cnf
root       4629   4628  0 11:54 ?        00:00:00 /usr/local/mysql-proxy/bin/mysql-proxy --defaults-file=/usr/local/mysql-proxy/conf/test.cnf
```

### 查看Atlas端口

```bash
sudo netstat -ntlp |grep mysql-proxy
tcp        0      0 0.0.0.0:1234            0.0.0.0:*               LISTEN      5937/mysql-proxy
tcp        0      0 0.0.0.0:2345            0.0.0.0:*               LISTEN      5937/mysql-proxy
```

*其中1234为代理端口，2345为管理端口*

### 连接Atlas管理界面

```bash
ubuntu@s4:/usr/local/mysql-proxy/bin$ mysql -umysqlproxy -pmysqlproxy -h192.168.64.131 -P2345    # 使用Atlas管理帐号登录Atlas管理控制台
Welcome to the MySQL monitor.  Commands end with ; or \g.
Your MySQL connection id is 1
Server version: 5.0.99-agent-admin

Copyright (c) 2000, 2015, Oracle and/or its affiliates. All rights reserved.

Oracle is a registered trademark of Oracle Corporation and/or its
affiliates. Other names may be trademarks of their respective
owners.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.
mysql> show databases;
ERROR 1105 (07000): use 'SELECT * FROM help' to see the supported commands
mysql> select * from help;
+----------------------------+---------------------------------------------------------+
| command                    | description                                             |
+----------------------------+---------------------------------------------------------+
| SELECT * FROM help         | shows this help                                         |
| SELECT * FROM backends     | lists the backends and their state                      |
| SET OFFLINE $backend_id    | offline backend server, $backend_id is backend_ndx's id |
| SET ONLINE $backend_id     | online backend server, ...                              |
| ADD MASTER $backend        | example: "add master 127.0.0.1:3306", ...               |
| ADD SLAVE $backend         | example: "add slave 127.0.0.1:3306", ...                |
| REMOVE BACKEND $backend_id | example: "remove backend 1", ...                        |
| ADD CLIENT $client         | example: "add client 192.168.1.2", ...                  |
| REMOVE CLIENT $client      | example: "remove client 192.168.1.2", ...               |
| SAVE CONFIG                | save the backends to config file                        |
+----------------------------+---------------------------------------------------------+
10 rows in set (0.00 sec)

mysql> SELECT * FROM backends;
+-------------+---------------------+-------+------+
| backend_ndx | address             | state | type |
+-------------+---------------------+-------+------+
|           1 | 192.168.64.131:3306 | up    | rw   |
|           2 | 192.168.64.132:3306 | up    | ro   |
+-------------+---------------------+-------+------+
2 rows in set (0.00 sec)
```

可以看到192.168.64.131:3306可读写，192.168.64.132:3306只读

### 客户端测试

连接atlas代理客户端，插入6条数据，检查能否查询到数据。

```
mysql -uatlas -patlas -P1234 -h192.168.64.131  
mysql> show tables;
+---------------+
| Tables_in_crm |
+---------------+
| t             |
+---------------+
1 row in set (0.00 sec)
# 写入6调数据
mysql> insert into t values(1);
Query OK, 1 row affected (1.01 sec)

mysql> insert into t values(2);
Query OK, 1 row affected (0.15 sec)

mysql> insert into t values(3);
Query OK, 1 row affected (0.01 sec)

mysql> insert into t values(4);
Query OK, 1 row affected (0.01 sec)

mysql> insert into t values(5);
Query OK, 1 row affected (0.00 sec)

mysql> insert into t values(6);
Query OK, 1 row affected (0.00 sec)
mysql> select * from t;
Empty set (0.01 sec)
```

不使用atlas代理连192.168.64.131，查询数据是否写入

```
mysql> select * from t;
+------+
| id   |
+------+
|    1 |
|    2 |
|    3 |
|    4 |
|    5 |
|    6 |
+------+
9 rows in set (0.00 sec)
```
发现数据已写入192.168.64.131库中。

不使用atlas代理连192.168.64.132，查询数据是否写入数据

```
# 查询132是否有数据存在，若不存在则证明使用atlas代理连接查询数据读的是132上的数据
mysql> select * from t;
Empty set (0.01 sec)
# 插入4条数据
mysql> insert into t values(7);
Query OK, 1 row affected (0.02 sec)

mysql> insert into t values(8);
Query OK, 1 row affected (0.00 sec)

mysql> insert into t values(9);
Query OK, 1 row affected (0.00 sec)

mysql> insert into t values(10);
Query OK, 1 row affected (0.00 sec)

mysql> select * from t;
+------+
| id   |
+------+
|    7 |
|    8 |
|    9 |
|   10 |
+------+
4 rows in set (0.00 sec)
```

使用atlas代理连接查询数据

```
mysql> select * from t;
+------+
| id   |
+------+
|    7 |
|    8 |
|    9 |
|   10 |
+------+
4 rows in set (0.00 sec)
# 查询到在132上插入的数据
# 再次插入数据
mysql> insert into t values(11);
Query OK, 1 row affected (0.01 sec)

mysql> insert into t values(12);
Query OK, 1 row affected (0.00 sec)

mysql> insert into t values(13);
Query OK, 1 row affected (0.22 sec)
# 再次查询数据
mysql> select * from t;
+------+
| id   |
+------+
|    7 |
|    8 |
|    9 |
|   10 |
+------+
4 rows in set (0.00 sec)
仍没有插入的数据
```
不使用atlas代理连192.168.64.131，查询数据是否写入

```
mysql> select * from t;
+------+
| id   |
+------+
|    1 |
|    2 |
|    3 |
|    4 |
|    5 |
|    6 |
|   11 |
|   12 |
|   13 |
+------+
9 rows in set (0.00 sec)
```
数据确实再次写入到131中至此证明atlas数据读写分离成功。

通过日志也可以观察到数据读写分离情况

```
ubuntu@s4:/usr/local/mysql-proxy/bin$ sudo tail -f ../log/sql_test.log 
[03/11/2016 14:00:04] C:192.168.64.131:55230 S:192.168.64.132:3306 OK 2.214 "show databases"
[03/11/2016 14:00:04] C:192.168.64.131:55230 S:192.168.64.132:3306 OK 0.609 "show tables"
[03/11/2016 14:00:04] C:192.168.64.131:55230 S:192.168.64.132:3306 OK 0.719 "select * from t"
[03/11/2016 14:00:42] C:192.168.64.131:55230 S:192.168.64.131:3306 OK 32.234 "insert into t values(14)"
[03/11/2016 14:00:44] C:192.168.64.131:55230 S:192.168.64.131:3306 OK 2.235 "insert into t values(15)"
[03/11/2016 14:00:46] C:192.168.64.131:55230 S:192.168.64.131:3306 OK 2.026 "insert into t values(16)"
[03/11/2016 14:00:49] C:192.168.64.131:55230 S:192.168.64.131:3306 OK 2.020 "insert into t values(17)"
[03/11/2016 14:00:51] C:192.168.64.131:55230 S:192.168.64.131:3306 OK 2.007 "insert into t values(18)"
[03/11/2016 14:00:54] C:192.168.64.131:55230 S:192.168.64.131:3306 OK 3.723 "insert into t values(19)"
[03/11/2016 14:00:58] C:192.168.64.131:55230 S:192.168.64.131:3306 OK 3.926 "insert into t values(20)"
[03/11/2016 14:01:01] C:192.168.64.131:55230 S:192.168.64.132:3306 OK 0.581 "select * from t"
[03/11/2016 14:01:49] C:192.168.64.131:55230 S:192.168.64.131:3306 OK 2.223 "delete from t where id=5"
[03/11/2016 14:01:57] C:192.168.64.131:55230 S:192.168.64.131:3306 OK 2.053 "insert into t select 5"
[03/11/2016 14:02:10] C:192.168.64.131:55230 S:192.168.64.131:3306 OK 65.436 "update t set id=8 where id=5"
[03/11/2016 14:02:18] C:192.168.64.131:55230 S:192.168.64.132:3306 OK 0.595 "select * from t"
```
可见所有的查询操作在132上。增、删、改操作在131上。

### 参考文档

http://blog.itpub.net/27000195/viewspace-1421262/