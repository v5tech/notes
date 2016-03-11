# MySQL主从复制读写分离（基于mysql-proxy实现）

http://mirror.bit.edu.cn/mysql/Downloads/MySQL-Proxy/mysql-proxy-0.8.4-linux-glibc2.3-x86-64bit.tar.gz

### 解压

```bash
tar zxvf mysql-proxy-0.8.4-linux-glibc2.3-x86-64bit.tar.gz
```

### 创建mysql-proxy帐号并授权

分别在主从数据库中创建mysqlproxy帐号

```
mysql> grant all on *.* to mysqlproxy@'192.168.64.%' identified by 'mysqlproxy';
mysql> flush privileges;
mysql> use mysql;
mysql> select User,Password,Host from user;
```

### 启动mysql-proxy

```
sudo ./mysql-proxy \
--daemon \
--log-level=debug \
--keepalive \
--log-file=/var/log/mysql-proxy.log \
--plugins="proxy" \
--proxy-backend-addresses="192.168.64.131:3306" \
--proxy-read-only-backend-addresses="192.168.64.132:3306" \
--proxy-lua-script="/home/ubuntu/apps/mysql-proxy-0.8.4/share/doc/mysql-proxy/rw-splitting.lua" \
--plugins="admin" \
--admin-username="admin" \
--admin-password="admin" \
--admin-lua-script="/home/ubuntu/apps/mysql-proxy-0.8.4/lib/mysql-proxy/lua/admin.lua"
```

查看mysql-proxy进程

```bash
ubuntu@s4:~/apps/mysql-proxy-0.8.4/bin$ ps -ef | grep mysql-proxy
root      18249      1  0 02:22 ?        00:00:00 /home/ubuntu/apps/mysql-proxy-0.8.4/libexec/mysql-proxy --daemon --log-level=debug --keepalive --log-file=/var/log/mysql-proxy.log --plugins=proxy --proxy-backend-addresses=192.168.64.131:3306 --proxy-read-only-backend-addresses=192.168.64.132:3306 --proxy-lua-script=/home/ubuntu/apps/mysql-proxy-0.8.4/share/doc/mysql-proxy/rw-splitting.lua --plugins=admin --admin-username=admin --admin-password=admin --admin-lua-script=/home/ubuntu/apps/mysql-proxy-0.8.4/lib/mysql-proxy/lua/admin.lua
root      18250  18249  0 02:22 ?        00:00:00 /home/ubuntu/apps/mysql-proxy-0.8.4/libexec/mysql-proxy --daemon --log-level=debug --keepalive --log-file=/var/log/mysql-proxy.log --plugins=proxy --proxy-backend-addresses=192.168.64.131:3306 --proxy-read-only-backend-addresses=192.168.64.132:3306 --proxy-lua-script=/home/ubuntu/apps/mysql-proxy-0.8.4/share/doc/mysql-proxy/rw-splitting.lua --plugins=admin --admin-username=admin --admin-password=admin --admin-lua-script=/home/ubuntu/apps/mysql-proxy-0.8.4/lib/mysql-proxy/lua/admin.lua
ubuntu    18252  15744  0 02:22 pts/1    00:00:00 grep --color=auto mysql-proxy
```

查看mysql-proxy端口

```bash
ubuntu@s4:~/apps/mysql-proxy-0.8.4/bin$ sudo netstat -ntlp | grep mysql-proxy
tcp        0      0 0.0.0.0:4040            0.0.0.0:*               LISTEN      18250/mysql-proxy
tcp        0      0 0.0.0.0:4041            0.0.0.0:*               LISTEN      18250/mysql-proxy
```

*4040是proxy端口，4041是admin端口*

### 连接管理端口

```bash
mysql> mysql -uadmin -padmin -h192.168.64.131 -P4041 连接管理端口

```

具体如下

```
ubuntu@s4:~/apps/mysql-proxy-0.8.4/bin$ mysql -uadmin -padmin -h192.168.64.131 -P4041
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
mysql> SELECT * FROM help;
+------------------------+------------------------------------+
| command                | description                        |
+------------------------+------------------------------------+
| SELECT * FROM help     | shows this help                    |
| SELECT * FROM backends | lists the backends and their state |
+------------------------+------------------------------------+
2 rows in set (0.00 sec)

mysql> SELECT * FROM backends;
+-------------+---------------------+---------+------+------+-------------------+
| backend_ndx | address             | state   | type | uuid | connected_clients |
+-------------+---------------------+---------+------+------+-------------------+
|           1 | 192.168.64.131:3306 | up      | rw   | NULL |                 0 |
|           2 | 192.168.64.132:3306 | unknown | ro   | NULL |                 0 |
+-------------+---------------------+---------+------+------+-------------------+
2 rows in set (0.00 sec)
```

多开几个客户端后其状态变为

```
mysql>  SELECT * FROM backends;
+-------------+---------------------+-------+------+------+-------------------+
| backend_ndx | address             | state | type | uuid | connected_clients |
+-------------+---------------------+-------+------+------+-------------------+
|           1 | 192.168.64.131:3306 | up    | rw   | NULL |                 0 |
|           2 | 192.168.64.132:3306 | up    | ro   | NULL |                 0 |
+-------------+---------------------+-------+------+------+-------------------+
2 rows in set (0.00 sec)

```
state都为up表正常

### 连接同步端口

```bash
mysql> mysql -umysqlproxy -pmysqlproxy -h192.168.64.131 -P4040 
```

多开启几个同步端口，在同步端口连接的客户端中插入和查询数据，观察读写分离。

结论：192.168.64.131:3306只写，192.168.64.132:3306只读。

### 操作演示

不使用proxy连接数据库，查询192.168.64.131:3306上的数据

```
mysql> select * from zhang;
+------+-------+----------------+
| id   | name  | address        |
+------+-------+----------------+
|    1 | zhang | this_is_master |
|    1 | zhang | this_is_master |
|    1 | zhang | this_is_master |
|    1 | zhang | this_is_master |
|    1 | zhang | this_is_master |
|    1 | zhang | this_is_master |
|    1 | zhang | this_is_master |
|    1 | zhang | this_is_master |
|    1 | zhang | this_is_master |
|    1 | zhang | this_is_master |
|    1 | zhang | this_is_master |
|    2 | zhang | this_is_master |
|    3 | zhang | this_is_master |
|    4 | zhang | this_is_master |
|    5 | zhang | this_is_master |
|    6 | zhang | this_is_master |
|    7 | zhang | this_is_master |
+------+-------+----------------+
17 rows in set (0.00 sec)
```

不使用proxy连接数据库，查询192.168.64.132:3306上的数据

```
mysql> select * from zhang;
+------+-------+---------------+
| id   | name  | address       |
+------+-------+---------------+
|    2 | zhang | this_is_slave |
+------+-------+---------------+
1 row in set (0.00 sec)
```

使用proxy连接数据库，执行查询和插入操作

```
ubuntu@s4:~/apps$ mysql -umysqlproxy -pmysqlproxy -h192.168.64.131 -P4040 
Welcome to the MySQL monitor.  Commands end with ; or \g.
    server default db: crm
    client default db: 
    syncronizing
Your MySQL connection id is 45
Server version: 5.5.47-0ubuntu0.12.04.1-log (Ubuntu)

Copyright (c) 2000, 2015, Oracle and/or its affiliates. All rights reserved.

Oracle is a registered trademark of Oracle Corporation and/or its
affiliates. Other names may be trademarks of their respective
owners.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

mysql> use crm;
Reading table information for completion of table and column names
You can turn off this feature to get a quicker startup with -A

Database changed
mysql> select * from zhang;
+------+-------+---------------+
| id   | name  | address       |
+------+-------+---------------+
|    2 | zhang | this_is_slave |
+------+-------+---------------+
1 row in set (0.00 sec)

# 此处数据为192.168.64.132:3306中的数据

mysql> insert into zhang values('8','zhang','this_is_master');
Query OK, 1 row affected (0.00 sec)
# 该数据将插入192.168.64.131:3306数据库中

mysql> select * from zhang;
    server default db: 
    client default db: crm
    syncronizing
+------+-------+---------------+
| id   | name  | address       |
+------+-------+---------------+
|    2 | zhang | this_is_slave |
+------+-------+---------------+
1 row in set (0.00 sec)
# 该数据仍来自192.168.64.132:3306中数据

```

不使用proxy连接192.168.64.131:3306观察数据是否插入

```
mysql> select * from zhang;
+------+-------+----------------+
| id   | name  | address        |
+------+-------+----------------+
|    1 | zhang | this_is_master |
|    1 | zhang | this_is_master |
|    1 | zhang | this_is_master |
|    1 | zhang | this_is_master |
|    1 | zhang | this_is_master |
|    1 | zhang | this_is_master |
|    1 | zhang | this_is_master |
|    1 | zhang | this_is_master |
|    1 | zhang | this_is_master |
|    1 | zhang | this_is_master |
|    1 | zhang | this_is_master |
|    2 | zhang | this_is_master |
|    3 | zhang | this_is_master |
|    4 | zhang | this_is_master |
|    5 | zhang | this_is_master |
|    6 | zhang | this_is_master |
|    7 | zhang | this_is_master |
|    8 | zhang | this_is_master |
+------+-------+----------------+
18 rows in set (0.00 sec)
```

由此可见使用mysql-proxy读写分离成功。

### 参考文档

http://blog.itpub.net/22039464/viewspace-1708258/




