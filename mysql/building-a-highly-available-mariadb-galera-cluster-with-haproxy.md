# 使用HAProxy搭建高可用MariaDB Galera Cluster

### 安装haproxy

```bash
yum install haproxy
```

### 配置/etc/haproxy/haproxy.cfg

```
global
    log         127.0.0.1 local2
    chroot      /var/lib/haproxy
    pidfile     /var/run/haproxy.pid
    maxconn     4000
    user        haproxy
    group       haproxy
    daemon
    stats socket /var/lib/haproxy/stats

defaults
    mode                    tcp
    log                     global
    option                  httplog
    option                  dontlognull
    option http-server-close
    option forwardfor       except 127.0.0.0/8
    option                  redispatch
    retries                 3
    timeout http-request    10s
    timeout queue           1m
    timeout connect         10s
    timeout client          1m
    timeout server          1m
    timeout http-keep-alive 10s
    timeout check           10s
    maxconn                 3000

listen stats 0.0.0.0:80
	mode http
	stats enable
	stats uri /stats  
	stats realm HAProxy\ Statistics 
	stats auth haproxy:haproxy
	stats admin if TRUE

listen mariadb_cluster_write 0.0.0.0:3307
    mode tcp
    server c1 192.168.64.145:3306 check port 3306
    server c2 192.168.64.146:3306 check port 3306 backup
    server c3 192.168.64.147:3306 check port 3306 backup

listen mariadb_cluster_read 0.0.0.0:3308
	mode tcp
    balance leastconn 
	server c1 192.168.64.145:3306 check port 3306
    server c2 192.168.64.146:3306 check port 3306
    server c3 192.168.64.147:3306 check port 3306
```

*注:*

1. `listen stats 0.0.0.0:80` 为haproxy的stats，用户名密码haproxy:haproxy
2. `listen mariadb_cluster_write 0.0.0.0:3307`，使用`mysql -uroot -proot -h127.0.0.1 -P3307`每次连接时都只会访问到c1即192.168.64.145，当c1宕机后会切换到c2，当c2宕机后会切换到c3
2. `listen mariadb_cluster_read 0.0.0.0:3308`，使用`mysql -uroot -proot -h127.0.0.1 -P3308`负载轮询

### 启动haproxy

```
service haproxy start
```

访问http://192.168.64.145/stats
![](http://i.imgur.com/zcChp9M.png)
### 实验测试

* 测试3307

```
mysql -uroot -proot -h127.0.0.1 -P3307
MariaDB [(none)]> SELECT @@wsrep_node_name;
+-------------------+
| @@wsrep_node_name |
+-------------------+
| c1                |
+-------------------+
1 row in set (0.00 sec)
```

c1宕机后再次查询切换到c2

![](http://i.imgur.com/zc53EKc.png)
```
mysql -uroot -proot -h127.0.0.1 -P3307
MariaDB [(none)]> SELECT @@wsrep_node_name;
+-------------------+
| @@wsrep_node_name |
+-------------------+
| c2                |
+-------------------+
1 row in set (0.00 sec)

```
c2宕机后再次查询切换到c3

![](http://i.imgur.com/XleRzpt.png)
```
mysql -uroot -proot -h127.0.0.1 -P3307
MariaDB [(none)]> SELECT @@wsrep_node_name;
+-------------------+
| @@wsrep_node_name |
+-------------------+
| c3                |
+-------------------+
1 row in set (0.00 sec)
```

c1再次上线后切换到c1

![](http://i.imgur.com/48NPiUN.png)
```
mysql -uroot -proot -h127.0.0.1 -P3307
MariaDB [(none)]> SELECT @@wsrep_node_name;
+-------------------+
| @@wsrep_node_name |
+-------------------+
| c1                |
+-------------------+
1 row in set (0.00 sec)

```

* 测试3308

```
mysql -uroot -proot -h127.0.0.1 -P3308
MariaDB [(none)]> SELECT @@wsrep_node_name;
+-------------------+
| @@wsrep_node_name |
+-------------------+
| c3                |
+-------------------+
1 row in set (0.00 sec)

mysql -uroot -proot -h127.0.0.1 -P3308
MariaDB [(none)]> SELECT @@wsrep_node_name;
+-------------------+
| @@wsrep_node_name |
+-------------------+
| c2                |
+-------------------+
1 row in set (0.00 sec)

mysql -uroot -proot -h127.0.0.1 -P3308
MariaDB [(none)]> SELECT @@wsrep_node_name;
+-------------------+
| @@wsrep_node_name |
+-------------------+
| c1                |
+-------------------+
1 row in set (0.00 sec)

MariaDB [(none)]> 
```
在三台服务器上进行轮询，达到负载效果。


### 参考文章

[http://www.fromdual.com/making-haproxy-high-available-for-mysql-galera-cluster](http://www.fromdual.com/making-haproxy-high-available-for-mysql-galera-cluster)

[https://mariadb.com/blog/setup-mariadb-enterprise-cluster-part-3-setup-ha-proxy-load-balancer-read-and-write-pools](https://mariadb.com/blog/setup-mariadb-enterprise-cluster-part-3-setup-ha-proxy-load-balancer-read-and-write-pools)

[http://galeracluster.com/documentation-webpages/haproxy.html](http://galeracluster.com/documentation-webpages/haproxy.html)