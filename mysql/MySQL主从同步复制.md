# MySQL主从同步复制

> Ubuntu 12.04.5 + MySQL 5.5.47

```
master 192.168.64.131
slave 192.168.64.132
```

#### 修改master上mysql配置文件

```
[mysqld]
server-id           = 131				#数据库ID
log_bin             = /var/log/mysql/mysql-bin.log	#启用二进制日志 如果没有var/log/mysql这个目录，则需要创建.
binlog-ignore-db	= mysql				#忽略同步的数据库
expire_logs_days	= 365				#日志文件过期天数，默认是 0，表示不过期
```

#### 修改slave上mysql配置文件

```
[mysqld]
server-id           = 132				#数据库ID
log_bin             = /var/log/mysql/mysql-bin.log	#启用二进制日志 如果没有var/log/mysql这个目录，则需要创建.
binlog-ignore-db	= mysql				#忽略同步的数据库
expire_logs_days	= 365				#日志文件过期天数，默认是 0，表示不过期
```

#### 在master上为slave创建同步数据帐号密码并授权 

```bash
mysql> GRANT REPLICATION SLAVE ON *.* TO 'repuser'@'192.168.64.132' IDENTIFIED BY 'repuser';
Query OK, 0 rows affected (0.00 sec)
mysql> FLUSH PRIVILEGES;
Query OK, 0 rows affected (0.00 sec)
```

#### 查看master状态

```
mysql> SHOW MASTER STATUS;
+------------------+----------+--------------+------------------+
| File             | Position | Binlog_Do_DB | Binlog_Ignore_DB |
+------------------+----------+--------------+------------------+
| mysql-bin.000001 |      107 |              | mysql            |
+------------------+----------+--------------+------------------+
1 row in set (0.00 sec)
mysql> SHOW BINLOG EVENTS IN 'mysql-bin.000001';
+------------------+-----+-------------+-----------+-------------+--------------------------------------------------------+
| Log_name         | Pos | Event_type  | Server_id | End_log_pos | Info                                                   |
+------------------+-----+-------------+-----------+-------------+--------------------------------------------------------+
| mysql-bin.000001 |   4 | Format_desc |       131 |         107 | Server ver: 5.5.47-0ubuntu0.12.04.1-log, Binlog ver: 4 |
+------------------+-----+-------------+-----------+-------------+--------------------------------------------------------+
1 row in set (0.00 sec)
```

#### 在slave上创建同步

```
mysql> CHANGE MASTER TO MASTER_HOST='192.168.64.131',MASTER_PORT=3306,MASTER_USER='repuser',MASTER_PASSWORD='repuser',MASTER_LOG_FILE='mysql-bin.000001',MASTER_LOG_POS=107;
Query OK, 0 rows affected (0.01 sec)
mysql> SHOW SLAVE STATUS\G
*************************** 1. row ***************************
               Slave_IO_State: 
                  Master_Host: 192.168.64.131
                  Master_User: repuser
                  Master_Port: 3306
                Connect_Retry: 60
              Master_Log_File: mysql-bin.000001
          Read_Master_Log_Pos: 107
               Relay_Log_File: mysqld-relay-bin.000001
                Relay_Log_Pos: 4
        Relay_Master_Log_File: mysql-bin.000001
             Slave_IO_Running: No
            Slave_SQL_Running: No
              Replicate_Do_DB: 
          Replicate_Ignore_DB: 
           Replicate_Do_Table: 
       Replicate_Ignore_Table: 
      Replicate_Wild_Do_Table: 
  Replicate_Wild_Ignore_Table: 
                   Last_Errno: 0
                   Last_Error: 
                 Skip_Counter: 0
          Exec_Master_Log_Pos: 107
              Relay_Log_Space: 107
              Until_Condition: None
               Until_Log_File: 
                Until_Log_Pos: 0
           Master_SSL_Allowed: No
           Master_SSL_CA_File: 
           Master_SSL_CA_Path: 
              Master_SSL_Cert: 
            Master_SSL_Cipher: 
               Master_SSL_Key: 
        Seconds_Behind_Master: NULL
Master_SSL_Verify_Server_Cert: No
                Last_IO_Errno: 0
                Last_IO_Error: 
               Last_SQL_Errno: 0
               Last_SQL_Error: 
  Replicate_Ignore_Server_Ids: 
             Master_Server_Id: 131
1 row in set (0.00 sec)
mysql> START SLAVE;
Query OK, 0 rows affected (0.00 sec)
mysql> SHOW SLAVE STATUS\G
*************************** 1. row ***************************
               Slave_IO_State: Waiting for master to send event
                  Master_Host: 192.168.64.131
                  Master_User: repuser
                  Master_Port: 3306
                Connect_Retry: 60
              Master_Log_File: mysql-bin.000001
          Read_Master_Log_Pos: 341
               Relay_Log_File: mysqld-relay-bin.000002
                Relay_Log_Pos: 487
        Relay_Master_Log_File: mysql-bin.000001
             Slave_IO_Running: Yes
            Slave_SQL_Running: Yes
              Replicate_Do_DB: 
          Replicate_Ignore_DB: 
           Replicate_Do_Table: 
       Replicate_Ignore_Table: 
      Replicate_Wild_Do_Table: 
  Replicate_Wild_Ignore_Table: 
                   Last_Errno: 0
                   Last_Error: 
                 Skip_Counter: 0
          Exec_Master_Log_Pos: 341
              Relay_Log_Space: 644
              Until_Condition: None
               Until_Log_File: 
                Until_Log_Pos: 0
           Master_SSL_Allowed: No
           Master_SSL_CA_File: 
           Master_SSL_CA_Path: 
              Master_SSL_Cert: 
            Master_SSL_Cipher: 
               Master_SSL_Key: 
        Seconds_Behind_Master: 0
Master_SSL_Verify_Server_Cert: No
                Last_IO_Errno: 0
                Last_IO_Error: 
               Last_SQL_Errno: 0
               Last_SQL_Error: 
  Replicate_Ignore_Server_Ids: 
             Master_Server_Id: 131
1 row in set (0.00 sec)
```

看到以下内容表示配置成功

```
             Slave_IO_Running: Yes
            Slave_SQL_Running: Yes
```

#### 主从同步测试

在master上建库建表插入数据，观察slave上是否有库、表、数据存在

```
mysql> create database crm;
Query OK, 1 row affected (0.00 sec)

mysql> use crm;
Database changed
mysql> create table employee(
    -> id int primary key auto_increment,
    -> empname varchar(100) not null,
    -> email varchar(100)
    -> );
Query OK, 0 rows affected (0.00 sec)
mysql> insert into employee (empname,email) values('xinru','xinru@crm.com');
Query OK, 1 row affected (0.01 sec)

mysql> insert into employee (empname,email) values('shishi','shishi@crm.com');
Query OK, 1 row affected (0.01 sec)

mysql> insert into employee (empname,email) values('tangyan','tangyan@crm.com');
Query OK, 1 row affected (0.00 sec)

mysql> insert into employee (empname,email) values('yuanyuan','yuanyuan@crm.com');
Query OK, 1 row affected (0.00 sec)

mysql> insert into employee (empname,email) values('bingbing','bingbing@crm.com');
Query OK, 1 row affected (0.01 sec)

mysql> insert into employee (empname,email) values('liutao','liutao@crm.com');
Query OK, 1 row affected (0.00 sec)
mysql> select * from employee;
+----+----------+------------------+
| id | empname  | email            |
+----+----------+------------------+
|  1 | liuyan   | liuyan@crm.com   |
|  2 | xinru    | xinru@crm.com    |
|  3 | shishi   | shishi@crm.com   |
|  4 | tangyan  | tangyan@crm.com  |
|  5 | yuanyuan | yuanyuan@crm.com |
|  6 | bingbing | bingbing@crm.com |
|  7 | liutao   | liutao@crm.com   |
+----+----------+------------------+
7 rows in set (0.00 sec)
```

在slave上验证库表数据是否存在

```
mysql> show databases;
+--------------------+
| Database           |
+--------------------+
| information_schema |
| crm                |
| mysql              |
| performance_schema |
+--------------------+
4 rows in set (0.00 sec)

mysql> use crm;
Database changed
mysql> show tables;
+---------------+
| Tables_in_crm |
+---------------+
| employee      |
+---------------+
1 row in set (0.00 sec)

mysql> select * from employee;
+----+----------+------------------+
| id | empname  | email            |
+----+----------+------------------+
|  1 | liuyan   | liuyan@crm.com   |
|  2 | xinru    | xinru@crm.com    |
|  3 | shishi   | shishi@crm.com   |
|  4 | tangyan  | tangyan@crm.com  |
|  5 | yuanyuan | yuanyuan@crm.com |
|  6 | bingbing | bingbing@crm.com |
|  7 | liutao   | liutao@crm.com   |
+----+----------+------------------+
7 rows in set (0.00 sec)
```

#### mysql主从复制错误常见解决方案

```
mysql> STOP SLAVE; SET GLOBAL SQL_SLAVE_SKIP_COUNTER=1; START SLAVE;
```

http://blog.arganzheng.me/posts/mysql-replication-errors-fixed.html