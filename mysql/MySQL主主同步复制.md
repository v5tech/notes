# MySQL主主同步复制

> Ubuntu 12.04.5 + MySQL 5.5.47

```
master1 192.168.64.131
master2 192.168.64.132
```

### 1. 修改mysql配置文件、创建帐号并授权

#### 1.1 修改master1上mysql配置文件my.conf

```
[mysqld]
server-id           = 131				#数据库ID
log_bin             = /var/log/mysql/mysql-bin.log	#启用二进制日志 如果没有var/log/mysql这个目录，则需要创建.
#binlog-do-db		= tudou1			#需要同步的数据库,这里同步tudou1和tudou2两个数据库
#binlog-do-db		= tudou2
binlog-ignore-db	= mysql				#忽略同步的数据库
log-slave-updates						#把从库的写操作记录到binlog中 （缺少之后，双主创建失败）
expire_logs_days	= 365				#日志文件过期天数，默认是 0，表示不过期
auto-increment-increment= 2				#设定为主服务器的数量，防止auto_increment字段重复
auto-increment-offset	= 1				#自增长字段的初始值，在多台master环境下，不会出现自增长ID重复
```

##### 创建帐号密码并授权

```
GRANT REPLICATION SLAVE ON *.* TO 'repuser'@'192.168.64.132' IDENTIFIED BY 'repuser';
FLUSH PRIVILEGES;
```

##### 在192.168.64.132测试repuser是否能登录192.168.64.131上的数据库

```
ubuntu@192.168.64.132:~/apps$ mysql -urepuser -prepuser -h192.168.64.131
```

#### 1.2 修改master2上mysql配置文件my.conf

```
[mysqld]
server-id           = 132				#数据库ID
log_bin             = /var/log/mysql/mysql-bin.log	#启用二进制日志 如果没有var/log/mysql这个目录，则需要创建.
#binlog-do-db		= tudou1			#需要同步的数据库,这里同步tudou1和tudou2两个数据库
#binlog-do-db		= tudou2
binlog-ignore-db	= mysql				#忽略同步的数据库
log-slave-updates						#把从库的写操作记录到binlog中 （缺少之后，双主创建失败）
expire_logs_days	= 365				#日志文件过期天数，默认是 0，表示不过期
auto-increment-increment= 2				#设定为主服务器的数量，防止auto_increment字段重复
auto-increment-offset	= 1				#自增长字段的初始值，在多台master环境下，不会出现自增长ID重复
```

##### 创建帐号密码并授权

```
GRANT REPLICATION SLAVE ON *.* TO 'repuser'@'192.168.64.131' IDENTIFIED BY 'repuser';
FLUSH PRIVILEGES;
```

##### 在192.168.64.131测试repuser是否能登录192.168.64.132上的数据库

```
ubuntu@192.168.64.131:~/apps$ mysql -urepuser -prepuser -h192.168.64.132
```

*注意：*
 
* log-slave-updates 表示把从库的写操作记录到binlog中，缺少之后，双主创建失败。双主同步时该项必须有

* binlog-do-db 表示需要同步的数据库可出现多个，上述配置中注释掉了，若开启该配置项则格式见上述配置

* binlog-ignore-db 表示忽略同步的数据库

### 2. 配置双主同步

#### 查看master状态

master1中

```
mysql> show master status;
+------------------+----------+--------------+------------------+
| File             | Position | Binlog_Do_DB | Binlog_Ignore_DB |
+------------------+----------+--------------+------------------+
| mysql-bin.000001 |      107 |              | mysql            |
+------------------+----------+--------------+------------------+
1 row in set (0.00 sec)
```

master2中

```
mysql> show master status;
+------------------+----------+--------------+------------------+
| File             | Position | Binlog_Do_DB | Binlog_Ignore_DB |
+------------------+----------+--------------+------------------+
| mysql-bin.000001 |      107 |              | mysql            |
+------------------+----------+--------------+------------------+
1 row in set (0.00 sec)
```

#### 设置master1从master2同步

```
mysql> CHANGE MASTER TO MASTER_HOST='192.168.64.132',MASTER_PORT=3306,MASTER_USER='repuser',MASTER_PASSWORD='repuser',MASTER_LOG_FILE='mysql-bin.000001',MASTER_LOG_POS=107;
mysql> SHOW SLAVE STATUS\G
mysql> START SLAVE;
mysql> SHOW SLAVE STATUS\G
```

如出现以下两项，则说明配置成功！

```
           Slave_IO_Running: Yes
           Slave_SQL_Running: Yes
```

#### 设置master2从master1同步

```
mysql> CHANGE MASTER TO MASTER_HOST='192.168.64.131',MASTER_PORT=3306,MASTER_USER='repuser',MASTER_PASSWORD='repuser',MASTER_LOG_FILE='mysql-bin.000001',MASTER_LOG_POS=107;
mysql> SHOW SLAVE STATUS\G
mysql> START SLAVE;
mysql> SHOW SLAVE STATUS\G
```

如出现以下两项，则说明配置成功！

```
           Slave_IO_Running: Yes
           Slave_SQL_Running: Yes
```

### 3 双主同步测试

进入master1 mysql 数据库

```
mysql>  create database crm;
Query OK, 1 row affected (0.00 sec)

mysql>  use crm;
Database changed
mysql>  create table employee(id int auto_increment,name varchar(10),primary key(id));
Query OK, 0 rows affected (0.00 sec)

mysql>  insert into employee(name) values('a');
Query OK, 1 row affected (0.00 sec)

mysql>  insert into employee(name) values('b');
Query OK, 1 row affected (0.00 sec)

mysql>  insert into employee(name) values('c');
Query OK, 1 row affected (0.06 sec)

mysql>  select * from employee;
+----+------+
| id | name |
+----+------+
|  1 | a    |
|  3 | b    |
|  5 | c    |
+----+------+
3 rows in set (0.00 sec)
 ```

进入master2，查看是否有crm这个数据库和employee表。

 ```
mysql>  show databases;
+--------------------+
| Database           |
+--------------------+
| information_schema |
| crm                |
| mysql              |
| performance_schema |
+--------------------+
4 rows in set (0.00 sec)

mysql>  use crm;
Reading table information for completion of table and column names
You can turn off this feature to get a quicker startup with -A

Database changed
mysql>  show tables;
+---------------+
| Tables_in_crm |
+---------------+
| employee      |
+---------------+
1 row in set (0.00 sec)

mysql>  select * from employee;
+----+------+
| id | name |
+----+------+
|  1 | a    |
|  3 | b    |
|  5 | c    |
+----+------+
3 rows in set (0.00 sec)

mysql>  insert into employee(name) values('d');
Query OK, 1 row affected (0.00 sec)

mysql>  select * from employee;
+----+------+
| id | name |
+----+------+
|  1 | a    |
|  3 | b    |
|  5 | c    |
|  7 | d    |
+----+------+
4 rows in set (0.00 sec)
 ```

在master1的中查看是否有刚刚在master2中插入的数据。

 ```
 mysql>  select * from employee;
+----+------+
| id | name |
+----+------+
|  1 | a    |
|  3 | b    |
|  5 | c    |
|  7 | d    |
+----+------+
4 rows in set (0.00 sec)
 ```