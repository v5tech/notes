# MySQL主从复制（半同步）

默认情况下MySQL的复制是异步的，Master上所有的更新操作写入Binlog之后并不确保所有的更新都被复制到Slave之上。异步操作虽然效率高，但是在Master/Slave出现问题的时候，存在很高数据不同步的风险，甚至可能丢失数据。

MySQL5.5引入半同步复制功能的目的是为了保证在master出问题的时候，至少有一台Slave的数据是完整的。在超时的情况下也可以临时转入异步复制，保障业务的正常使用，直到一台salve追赶上之后，继续切换到半同步模式。

具体配置

MySQL5.5半同步插件是由谷歌提供，具体位置/usr/lib/mysql/plugin/下，一个是master用的semisync_master.so，一个是slave用的semisync_slave.so，具体配置如下

master:

(1).安装插件

```
mysql> INSTALL PLUGIN rpl_semi_sync_master SONAME 'semisync_master.so';  
Query OK, 0 rows affected (0.39 sec)
mysql> SET GLOBAL rpl_semi_sync_master_enabled = 1; 
Query OK, 0 rows affected (0.00 sec)
mysql> SET GLOBAL rpl_semi_sync_master_timeout = 1000; 
Query OK, 0 rows affected (0.00 sec)
```

(2).修改配置文件

```
[root@node1 ~]# sudo vim /etc/mysql/my.cnf
[mysqld]
rpl_semi_sync_master_enabled=1 #启用半同步
rpl_semi_sync_master_timeout=1000 #超时时间为1s
```

(3).重新启动服务

```
[root@node1 ~]# service mysqld restart 
Shutting down MySQL... SUCCESS!   
Starting MySQL.. SUCCESS!
```

slave:

(1).安装插件

```
mysql> INSTALL PLUGIN rpl_semi_sync_slave SONAME 'semisync_slave.so';  
Query OK, 0 rows affected (0.38 sec)
mysql> SET GLOBAL rpl_semi_sync_slave_enabled = 1;  
Query OK, 0 rows affected (0.00 sec)
mysql> STOP SLAVE IO_THREAD; 
Query OK, 0 rows affected (0.00 sec)
mysql> START SLAVE IO_THREAD;
Query OK, 0 rows affected (0.01 sec)
```

(2).修改配置文件

```
[root@node2 ~]# sudo vim /etc/mysql/my.cnf
[mysqld]
rpl_semi_sync_slave_enabled=1  #启用半同步复制
```

(3).重新启动服务

```
[root@node2 ~]# service mysqld restart 
Shutting down MySQL. SUCCESS!   
Starting MySQL.. SUCCESS!
```

查看状态

master:

```
mysql> SHOW GLOBAL STATUS LIKE 'rpl_semi%'; 
+--------------------------------------------+-------+  
| Variable_name                              | Value |  
+--------------------------------------------+-------+  
| Rpl_semi_sync_master_clients               | 1     |  
| Rpl_semi_sync_master_net_avg_wait_time     | 0     |  
| Rpl_semi_sync_master_net_wait_time         | 0     |  
| Rpl_semi_sync_master_net_waits             | 0     |  
| Rpl_semi_sync_master_no_times              | 0     |  
| Rpl_semi_sync_master_no_tx                 | 0     |  
| Rpl_semi_sync_master_status                | ON    |  
| Rpl_semi_sync_master_timefunc_failures     | 0     |  
| Rpl_semi_sync_master_tx_avg_wait_time      | 0     |  
| Rpl_semi_sync_master_tx_wait_time          | 0     |  
| Rpl_semi_sync_master_tx_waits              | 0     |  
| Rpl_semi_sync_master_wait_pos_backtraverse | 0     |  
| Rpl_semi_sync_master_wait_sessions         | 0     |  
| Rpl_semi_sync_master_yes_tx                | 0     |  
+--------------------------------------------+-------+  
14 rows in set (0.00 sec)
```

slave:

```
mysql> SHOW GLOBAL STATUS LIKE 'rpl_semi%'; 
+----------------------------+-------+  
| Variable_name              | Value |  
+----------------------------+-------+  
| Rpl_semi_sync_slave_status | ON    |  
+----------------------------+-------+  
1 row in set (0.01 sec)
```

测试

master:

```
mysql> use crm;
Database changed
mysql> create table user (id int(10)); 
Query OK, 0 rows affected (0.01 sec)
mysql> show tables;
+---------------+
| Tables_in_crm |
+---------------+
| employee      |
| user          |
+---------------+
2 rows in set (0.00 sec)
mysql> insert user value (1);
Query OK, 1 row affected (0.00 sec)
```

模拟故障

slave:

```
mysql> STOP SLAVE IO_THREAD; 
Query OK, 0 rows affected (0.02 sec)
```
master:

```
mysql> create table dept (id int(10));
Query OK, 0 rows affected (1.01 sec)
```

可以看到主服务器会卡1s，我们超时时间设置的为1s。

查看状态

master:

```
mysql> SHOW GLOBAL STATUS LIKE 'rpl_semi%'; 
+--------------------------------------------+-------+
| Variable_name                              | Value |
+--------------------------------------------+-------+
| Rpl_semi_sync_master_clients               | 1     |
| Rpl_semi_sync_master_net_avg_wait_time     | 542   |
| Rpl_semi_sync_master_net_wait_time         | 1628  |
| Rpl_semi_sync_master_net_waits             | 3     |
| Rpl_semi_sync_master_no_times              | 1     |
| Rpl_semi_sync_master_no_tx                 | 1     |
| Rpl_semi_sync_master_status                | OFF   |
| Rpl_semi_sync_master_timefunc_failures     | 0     |
| Rpl_semi_sync_master_tx_avg_wait_time      | 597   |
| Rpl_semi_sync_master_tx_wait_time          | 1194  |
| Rpl_semi_sync_master_tx_waits              | 2     |
| Rpl_semi_sync_master_wait_pos_backtraverse | 0     |
| Rpl_semi_sync_master_wait_sessions         | 0     |
| Rpl_semi_sync_master_yes_tx                | 2     |
+--------------------------------------------+-------+
14 rows in set (0.00 sec)
```

slave:

```
mysql> SHOW GLOBAL STATUS LIKE 'rpl_semi%'; 
+----------------------------+-------+
| Variable_name              | Value |
+----------------------------+-------+
| Rpl_semi_sync_slave_status | OFF   |
+----------------------------+-------+
1 row in set (0.00 sec)

mysql> START SLAVE IO_THREAD;     # 启动SLAVE IO_THREAD
Query OK, 0 rows affected (0.00 sec)

mysql> SHOW GLOBAL STATUS LIKE 'rpl_semi%'; 
+----------------------------+-------+
| Variable_name              | Value |
+----------------------------+-------+
| Rpl_semi_sync_slave_status | ON    |
+----------------------------+-------+
1 row in set (0.00 sec)

mysql> show tables;				# 可见dept表已同步过来了
+---------------+
| Tables_in_crm |
+---------------+
| dept          |
| employee      |
| user          |
+---------------+
3 rows in set (0.00 sec)
```