# 在CentOS7上搭建MariaDB Galera Cluster

> 环境: MariaDB 10.1.12 +CentOS Linux release 7.2.1511

### 安装MariaDB 10.1.12

配置MariaDB 10.1.12 yum源

```bash
[root@c1 ~]# vim /etc/yum.repos.d/MariaDB.repo
[root@c2 ~]# vim /etc/yum.repos.d/MariaDB.repo
[root@c3 ~]# vim /etc/yum.repos.d/MariaDB.repo

```
其MariaDB.repo文件内容如下
```
[mariadb]
name = MariaDB
baseurl = http://yum.mariadb.org/10.1/centos7-amd64
gpgkey=https://yum.mariadb.org/RPM-GPG-KEY-MariaDB
gpgcheck=1
```

使用yum安装

```
[root@c1 ~]# sudo yum install MariaDB-server MariaDB-client galera
[root@c2 ~]# sudo yum install MariaDB-server MariaDB-client galera
[root@c3 ~]# sudo yum install MariaDB-server MariaDB-client galera
```

安全配置

```bash
[root@c1 ~]# /usr/bin/mysql_secure_installation
[root@c2 ~]# /usr/bin/mysql_secure_installation
[root@c3 ~]# /usr/bin/mysql_secure_installation
```

启动MariaDB

```bash
sudo systemctl start mariadb
```
或
```bash
sudo /etc/init.d/mysql start
```

### 配置 MariaDB Galera Cluster

分别修改三台节点上的`/etc/my.cnf.d/server.cnf`文件

修改c1上的`/etc/my.cnf.d/server.cnf`文件如下

```
[server]

[mysqld]

[galera]
wsrep_provider = /usr/lib64/galera/libgalera_smm.so
wsrep_cluster_address = "gcomm://192.168.64.145,192.168.64.146,192.168.64.147"
wsrep_node_name = c1
wsrep_node_address=192.168.64.145
wsrep_on=ON
binlog_format=ROW
default_storage_engine=InnoDB
innodb_autoinc_lock_mode=2
#bind-address=0.0.0.0
wsrep_slave_threads=1
innodb_flush_log_at_trx_commit=0
innodb_buffer_pool_size=122M
wsrep_sst_method=rsync

[embedded]

[mariadb]

[mariadb-10.1]
```

修改c2上的`/etc/my.cnf.d/server.cnf`文件如下

```
[server]

[mysqld]

[galera]
wsrep_provider = /usr/lib64/galera/libgalera_smm.so
wsrep_cluster_address = "gcomm://192.168.64.145,192.168.64.146,192.168.64.147"
wsrep_node_name = c2
wsrep_node_address=192.168.64.146
wsrep_on=ON
binlog_format=ROW
default_storage_engine=InnoDB
innodb_autoinc_lock_mode=2
#bind-address=0.0.0.0
wsrep_slave_threads=1
innodb_flush_log_at_trx_commit=0
innodb_buffer_pool_size=122M
wsrep_sst_method=rsync

[embedded]

[mariadb]

[mariadb-10.1]
```

修改c3上的`/etc/my.cnf.d/server.cnf`文件如下

```
[server]

[mysqld]

[galera]
wsrep_provider = /usr/lib64/galera/libgalera_smm.so
wsrep_cluster_address = "gcomm://192.168.64.145,192.168.64.146,192.168.64.147"
wsrep_node_name = c3
wsrep_node_address=192.168.64.147
wsrep_on=ON
binlog_format=ROW
default_storage_engine=InnoDB
innodb_autoinc_lock_mode=2
#bind-address=0.0.0.0
wsrep_slave_threads=1
innodb_flush_log_at_trx_commit=0
innodb_buffer_pool_size=122M
wsrep_sst_method=rsync

[embedded]

[mariadb]

[mariadb-10.1]
```

### 启动集群

引导创建集群
```bash
[root@c1 ~]# /usr/sbin/mysqld --wsrep-new-cluster --user=root &
```
查看集群信息
```
[root@c1 ~]# mysql -uroot -proot
Welcome to the MariaDB monitor.  Commands end with ; or \g.
Your MariaDB connection id is 7
Server version: 10.1.12-MariaDB MariaDB Server

Copyright (c) 2000, 2016, Oracle, MariaDB Corporation Ab and others.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

MariaDB [(none)]> SHOW STATUS LIKE 'wsrep_cluster_size';
+--------------------+-------+
| Variable_name      | Value |
+--------------------+-------+
| wsrep_cluster_size | 1     |
+--------------------+-------+
1 row in set (0.00 sec)
MariaDB [(none)]> SHOW STATUS LIKE 'wsrep_%';
+------------------------------+--------------------------------------+
| Variable_name                | Value                                |
+------------------------------+--------------------------------------+
| wsrep_apply_oooe             | 0.000000                             |
| wsrep_apply_oool             | 0.000000                             |
| wsrep_apply_window           | 1.000000                             |
| wsrep_causal_reads           | 0                                    |
| wsrep_cert_deps_distance     | 1.250000                             |
| wsrep_cert_index_size        | 4                                    |
| wsrep_cert_interval          | 0.000000                             |
| wsrep_cluster_conf_id        | 5                                    |
| wsrep_cluster_size           | 1                                    |
| wsrep_cluster_state_uuid     | 40718855-eb7d-11e5-8964-865748aa57ae |
| wsrep_cluster_status         | Primary                              |
| wsrep_commit_oooe            | 0.000000                             |
| wsrep_commit_oool            | 0.000000                             |
| wsrep_commit_window          | 1.000000                             |
| wsrep_connected              | ON                                   |
| wsrep_evs_delayed            |                                      |
| wsrep_evs_evict_list         |                                      |
| wsrep_evs_repl_latency       | 0/0/0/0/0                            |
| wsrep_evs_state              | OPERATIONAL                          |
| wsrep_flow_control_paused    | 0.000000                             |
| wsrep_flow_control_paused_ns | 0                                    |
| wsrep_flow_control_recv      | 0                                    |
| wsrep_flow_control_sent      | 0                                    |
| wsrep_gcomm_uuid             | 4070db5a-eb7d-11e5-9442-673375f48555 |
| wsrep_incoming_addresses     | 192.168.64.145:3306                  |
| wsrep_last_committed         | 4                                    |
| wsrep_local_bf_aborts        | 0                                    |
| wsrep_local_cached_downto    | 1                                    |
| wsrep_local_cert_failures    | 0                                    |
| wsrep_local_commits          | 2                                    |
| wsrep_local_index            | 0                                    |
| wsrep_local_recv_queue       | 0                                    |
| wsrep_local_recv_queue_avg   | 0.083333                             |
| wsrep_local_recv_queue_max   | 2                                    |
| wsrep_local_recv_queue_min   | 0                                    |
| wsrep_local_replays          | 0                                    |
| wsrep_local_send_queue       | 0                                    |
| wsrep_local_send_queue_avg   | 0.000000                             |
| wsrep_local_send_queue_max   | 1                                    |
| wsrep_local_send_queue_min   | 0                                    |
| wsrep_local_state            | 4                                    |
| wsrep_local_state_comment    | Synced                               |
| wsrep_local_state_uuid       | 40718855-eb7d-11e5-8964-865748aa57ae |
| wsrep_protocol_version       | 7                                    |
| wsrep_provider_name          | Galera                               |
| wsrep_provider_vendor        | Codership Oy <info@codership.com>    |
| wsrep_provider_version       | 25.3.14(r3560)                       |
| wsrep_ready                  | ON                                   |
| wsrep_received               | 12                                   |
| wsrep_received_bytes         | 1072                                 |
| wsrep_repl_data_bytes        | 1076                                 |
| wsrep_repl_keys              | 9                                    |
| wsrep_repl_keys_bytes        | 164                                  |
| wsrep_repl_other_bytes       | 0                                    |
| wsrep_replicated             | 4                                    |
| wsrep_replicated_bytes       | 1496                                 |
| wsrep_thread_count           | 2                                    |
+------------------------------+--------------------------------------+
57 rows in set (0.00 sec)

```
向集群中添加其他节点
```bash
[root@c2 ~]# systemctl start mariadb
[root@c3 ~]# systemctl start mariadb
```
查看集群信息
```
[root@c1 ~]# mysql -uroot -proot
Welcome to the MariaDB monitor.  Commands end with ; or \g.
Your MariaDB connection id is 7
Server version: 10.1.12-MariaDB MariaDB Server

Copyright (c) 2000, 2016, Oracle, MariaDB Corporation Ab and others.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

MariaDB [(none)]> SHOW STATUS LIKE 'wsrep_cluster_size';
+--------------------+-------+
| Variable_name      | Value |
+--------------------+-------+
| wsrep_cluster_size | 3     |
+--------------------+-------+
1 row in set (0.00 sec)

MariaDB [(none)]> SHOW STATUS LIKE 'wsrep_%';
+------------------------------+-------------------------------------------------------------+
| Variable_name                | Value                                                       |
+------------------------------+-------------------------------------------------------------+
| wsrep_apply_oooe             | 0.000000                                                    |
| wsrep_apply_oool             | 0.000000                                                    |
| wsrep_apply_window           | 1.000000                                                    |
| wsrep_causal_reads           | 0                                                           |
| wsrep_cert_deps_distance     | 1.250000                                                    |
| wsrep_cert_index_size        | 4                                                           |
| wsrep_cert_interval          | 0.000000                                                    |
| wsrep_cluster_conf_id        | 7                                                           |
| wsrep_cluster_size           | 3                                                           |
| wsrep_cluster_state_uuid     | 40718855-eb7d-11e5-8964-865748aa57ae                        |
| wsrep_cluster_status         | Primary                                                     |
| wsrep_commit_oooe            | 0.000000                                                    |
| wsrep_commit_oool            | 0.000000                                                    |
| wsrep_commit_window          | 1.000000                                                    |
| wsrep_connected              | ON                                                          |
| wsrep_evs_delayed            |                                                             |
| wsrep_evs_evict_list         |                                                             |
| wsrep_evs_repl_latency       | 0.000972602/0.00186067/0.00318854/0.000848294/4             |
| wsrep_evs_state              | OPERATIONAL                                                 |
| wsrep_flow_control_paused    | 0.000000                                                    |
| wsrep_flow_control_paused_ns | 0                                                           |
| wsrep_flow_control_recv      | 0                                                           |
| wsrep_flow_control_sent      | 0                                                           |
| wsrep_gcomm_uuid             | 4070db5a-eb7d-11e5-9442-673375f48555                        |
| wsrep_incoming_addresses     | 192.168.64.145:3306,192.168.64.146:3306,192.168.64.147:3306 |
| wsrep_last_committed         | 4                                                           |
| wsrep_local_bf_aborts        | 0                                                           |
| wsrep_local_cached_downto    | 1                                                           |
| wsrep_local_cert_failures    | 0                                                           |
| wsrep_local_commits          | 2                                                           |
| wsrep_local_index            | 0                                                           |
| wsrep_local_recv_queue       | 0                                                           |
| wsrep_local_recv_queue_avg   | 0.071429                                                    |
| wsrep_local_recv_queue_max   | 2                                                           |
| wsrep_local_recv_queue_min   | 0                                                           |
| wsrep_local_replays          | 0                                                           |
| wsrep_local_send_queue       | 0                                                           |
| wsrep_local_send_queue_avg   | 0.000000                                                    |
| wsrep_local_send_queue_max   | 1                                                           |
| wsrep_local_send_queue_min   | 0                                                           |
| wsrep_local_state            | 4                                                           |
| wsrep_local_state_comment    | Synced                                                      |
| wsrep_local_state_uuid       | 40718855-eb7d-11e5-8964-865748aa57ae                        |
| wsrep_protocol_version       | 7                                                           |
| wsrep_provider_name          | Galera                                                      |
| wsrep_provider_vendor        | Codership Oy <info@codership.com>                           |
| wsrep_provider_version       | 25.3.14(r3560)                                              |
| wsrep_ready                  | ON                                                          |
| wsrep_received               | 14                                                          |
| wsrep_received_bytes         | 1540                                                        |
| wsrep_repl_data_bytes        | 1076                                                        |
| wsrep_repl_keys              | 9                                                           |
| wsrep_repl_keys_bytes        | 164                                                         |
| wsrep_repl_other_bytes       | 0                                                           |
| wsrep_replicated             | 4                                                           |
| wsrep_replicated_bytes       | 1496                                                        |
| wsrep_thread_count           | 2                                                           |
+------------------------------+-------------------------------------------------------------+
57 rows in set (0.00 sec)

```

### 测试数据同步

在c1上建库、建表并插入数据，观察c2、c3是否可以查询到

```
MariaDB [galeratest]> CREATE DATABASE galera;
Query OK, 1 row affected (0.00 sec)
MariaDB [galera]> USE galera;
Database changed
MariaDB [galera]> CREATE TABLE t (id int primary key);
Query OK, 0 rows affected (0.02 sec)
MariaDB [galera]> insert into t values(1);
Query OK, 1 row affected (0.00 sec)

MariaDB [galera]> insert into t values(2);
Query OK, 1 row affected (0.00 sec)

MariaDB [galera]> insert into t values(3);
Query OK, 1 row affected (0.00 sec)

MariaDB [galera]> insert into t values(4);
Query OK, 1 row affected (0.01 sec)

MariaDB [galera]> insert into t values(5);
Query OK, 1 row affected (0.01 sec)

MariaDB [galera]> insert into t values(6);
Query OK, 1 row affected (0.00 sec)

MariaDB [galera]> select * from t;
+----+
| id |
+----+
|  1 |
|  2 |
|  3 |
|  4 |
|  5 |
|  6 |
+----+
6 rows in set (0.00 sec)
```
在c2、c3上查询数据

```
MariaDB [(none)]> show databases;
+--------------------+
| Database           |
+--------------------+
| galera             |
| information_schema |
| mysql              |
| performance_schema |
+--------------------+
4 rows in set (0.00 sec)

MariaDB [(none)]> USE galera;
Reading table information for completion of table and column names
You can turn off this feature to get a quicker startup with -A

Database changed
MariaDB [galera]> SELECT * FROM t;
+----+
| id |
+----+
|  1 |
|  2 |
|  3 |
|  4 |
|  5 |
|  6 |
+----+
6 rows in set (0.00 sec)
```

### 故障测试

```bash
[root@c3 ~]# systemctl stop mariadb

[root@c1 ~]# mysql -uroot -proot
Welcome to the MariaDB monitor.  Commands end with ; or \g.
Your MariaDB connection id is 7
Server version: 10.1.12-MariaDB MariaDB Server

Copyright (c) 2000, 2016, Oracle, MariaDB Corporation Ab and others.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

MariaDB [galera]> SHOW STATUS LIKE 'wsrep_cluster_size';
+--------------------+-------+
| Variable_name      | Value |
+--------------------+-------+
| wsrep_cluster_size | 2     |
+--------------------+-------+
1 row in set (0.00 sec)

MariaDB [galera]> SHOW STATUS LIKE 'wsrep_%';
+------------------------------+-----------------------------------------+
| Variable_name                | Value                                   |
+------------------------------+-----------------------------------------+
| wsrep_apply_oooe             | 0.000000                                |
| wsrep_apply_oool             | 0.000000                                |
| wsrep_apply_window           | 1.000000                                |
| wsrep_causal_reads           | 0                                       |
| wsrep_cert_deps_distance     | 2.142857                                |
| wsrep_cert_index_size        | 10                                      |
| wsrep_cert_interval          | 0.000000                                |
| wsrep_cluster_conf_id        | 8                                       |
| wsrep_cluster_size           | 2                                       |
| wsrep_cluster_state_uuid     | 40718855-eb7d-11e5-8964-865748aa57ae    |
| wsrep_cluster_status         | Primary                                 |
| wsrep_commit_oooe            | 0.000000                                |
| wsrep_commit_oool            | 0.000000                                |
| wsrep_commit_window          | 1.000000                                |
| wsrep_connected              | ON                                      |
| wsrep_evs_delayed            |                                         |
| wsrep_evs_evict_list         |                                         |
| wsrep_evs_repl_latency       | 0/0/0/0/0                               |
| wsrep_evs_state              | OPERATIONAL                             |
| wsrep_flow_control_paused    | 0.000000                                |
| wsrep_flow_control_paused_ns | 0                                       |
| wsrep_flow_control_recv      | 0                                       |
| wsrep_flow_control_sent      | 0                                       |
| wsrep_gcomm_uuid             | 4070db5a-eb7d-11e5-9442-673375f48555    |
| wsrep_incoming_addresses     | 192.168.64.145:3306,192.168.64.146:3306 |
| wsrep_last_committed         | 14                                      |
| wsrep_local_bf_aborts        | 0                                       |
| wsrep_local_cached_downto    | 1                                       |
| wsrep_local_cert_failures    | 0                                       |
| wsrep_local_commits          | 8                                       |
| wsrep_local_index            | 0                                       |
| wsrep_local_recv_queue       | 0                                       |
| wsrep_local_recv_queue_avg   | 0.062500                                |
| wsrep_local_recv_queue_max   | 2                                       |
| wsrep_local_recv_queue_min   | 0                                       |
| wsrep_local_replays          | 0                                       |
| wsrep_local_send_queue       | 0                                       |
| wsrep_local_send_queue_avg   | 0.000000                                |
| wsrep_local_send_queue_max   | 1                                       |
| wsrep_local_send_queue_min   | 0                                       |
| wsrep_local_state            | 4                                       |
| wsrep_local_state_comment    | Synced                                  |
| wsrep_local_state_uuid       | 40718855-eb7d-11e5-8964-865748aa57ae    |
| wsrep_protocol_version       | 7                                       |
| wsrep_provider_name          | Galera                                  |
| wsrep_provider_vendor        | Codership Oy <info@codership.com>       |
| wsrep_provider_version       | 25.3.14(r3560)                          |
| wsrep_ready                  | ON                                      |
| wsrep_received               | 16                                      |
| wsrep_received_bytes         | 2197                                    |
| wsrep_repl_data_bytes        | 2762                                    |
| wsrep_repl_keys              | 32                                      |
| wsrep_repl_keys_bytes        | 555                                     |
| wsrep_repl_other_bytes       | 0                                       |
| wsrep_replicated             | 13                                      |
| wsrep_replicated_bytes       | 4149                                    |
| wsrep_thread_count           | 2                                       |
+------------------------------+-----------------------------------------+
57 rows in set (0.00 sec)
```
观察`wsrep_cluster_size`，`wsrep_incoming_addresses`的值

### 参考文档

[https://www.vultr.com/docs/install-mariadb-on-centos-7](https://www.vultr.com/docs/install-mariadb-on-centos-7)

[https://downloads.mariadb.org/mariadb/repositories/#mirror=digitalocean-nyc&distro=CentOS&distro_release=centos7-amd64--centos7&version=10.1](https://downloads.mariadb.org/mariadb/repositories/#mirror=digitalocean-nyc&distro=CentOS&distro_release=centos7-amd64--centos7&version=10.1)

[https://mariadb.com/kb/en/mariadb/yum/#installing-mariadb-galera-cluster-with-yum](https://mariadb.com/kb/en/mariadb/yum/#installing-mariadb-galera-cluster-with-yum)

[https://mariadb.com/kb/en/mariadb/getting-started-with-mariadb-galera-cluster/](https://mariadb.com/kb/en/mariadb/getting-started-with-mariadb-galera-cluster/)

[http://galeracluster.com/documentation-webpages/configuration.html](http://galeracluster.com/documentation-webpages/configuration.html)

[http://www.sebastien-han.fr/blog/2012/04/01/mysql-multi-master-replication-with-galera/](http://www.sebastien-han.fr/blog/2012/04/01/mysql-multi-master-replication-with-galera/)

[http://www.nnbbxx.net/post-3817.html](http://www.nnbbxx.net/post-3817.html)