# 使用Galera Load Balancer实现高可用MariaDB Galera Cluster

### 安装Galera Load Balancer

```bash
yum install gcc* libtool
git clone https://github.com/codership/glb
cd glb/
./bootstrap.sh
./configure
make
make install
```
### 配置Galera Load Balancer

```bash
cp glb/files/glbd.sh /etc/init.d/glb
cp glb/files/glbd.cfg /etc/sysconfig/glbd
```

### 修改/etc/sysconfig/glbd

```
LISTEN_ADDR="8010"
CONTROL_ADDR="127.0.0.1:8011"
CONTROL_FIFO="/var/run/glbd.fifo"
THREADS="4"
MAX_CONN=256
DEFAULT_TARGETS="192.168.64.145:3306:2 192.168.64.146:3306 192.168.64.147:3306"
OTHER_OPTIONS="--round-robin"
```
### 启动Galera Load Balancer

```bash
[root@c1 ~]# service glb start
[Fri Mar 18 23:38:04 CST 2016] glbd: starting...
glb v1.0.1 (epoll)
Incoming address: 0.0.0.0:8010, control FIFO: /var/run/glbd.fifo
Control  address:  127.0.0.1:8011
Number of threads: 4, max conn: 256, nodelay: ON, keepalive: ON, defer accept: OFF, linger: OFF, daemon: YES, lat.count: 0, policy: 'round-robin', top: NO, verbose: NO
Destinations: 3
   0:  192.168.64.145:3306 , w: 2.000
   1:  192.168.64.146:3306 , w: 1.000
   2:  192.168.64.147:3306 , w: 1.000
   INFO: glb_daemon.c:44: Changing effective user to 'daemon'
[Fri Mar 18 23:38:04 CST 2016] glbd: started, pid=5833
```

查看状态
```
[root@c1 ~]# service glb getinfo
Router:
------------------------------------------------------
        Address       :   weight   usage    map  conns
 192.168.64.145:3306  :    2.000   0.000    N/A      0
 192.168.64.146:3306  :    1.000   0.000    N/A      0
 192.168.64.147:3306  :    1.000   0.000    N/A      0
------------------------------------------------------
Destinations: 3, total connections: 0 of 256 max
```

### 实验测试

客户端使用glb的代理端口`8010`连接`MariaDB Galera Cluster`客户端

```
[root@c1 glb]# mysql -uroot -proot -h127.0.0.1 -P8010
MariaDB [(none)]> SELECT @@wsrep_node_name;
+-------------------+
| @@wsrep_node_name |
+-------------------+
| c1                |
+-------------------+
1 row in set (0.00 sec)
```
glb状态信息
```
[root@c1 ~]# service glb getinfo
Router:
------------------------------------------------------
        Address       :   weight   usage    map  conns
 192.168.64.145:3306  :    2.000   0.500    N/A      1
 192.168.64.146:3306  :    1.000   0.000    N/A      0
 192.168.64.147:3306  :    1.000   0.000    N/A      0
------------------------------------------------------
Destinations: 3, total connections: 1 of 256 max
```

退出，再次连接

```
[root@c1 glb]# mysql -uroot -proot -h127.0.0.1 -P8010
MariaDB [(none)]> SELECT @@wsrep_node_name;
+-------------------+
| @@wsrep_node_name |
+-------------------+
| c2                |
+-------------------+
1 row in set (0.00 sec)

MariaDB [(none)]> 
```
glb状态信息
```
[root@c1 ~]# service glb getinfo
Router:
------------------------------------------------------
        Address       :   weight   usage    map  conns
 192.168.64.145:3306  :    2.000   0.000    N/A      0
 192.168.64.146:3306  :    1.000   0.500    N/A      1
 192.168.64.147:3306  :    1.000   0.000    N/A      0
------------------------------------------------------
Destinations: 3, total connections: 1 of 256 max
```
退出，再次连接
```
[root@c1 glb]# mysql -uroot -proot -h127.0.0.1 -P8010
MariaDB [(none)]> SELECT @@wsrep_node_name;
+-------------------+
| @@wsrep_node_name |
+-------------------+
| c3                |
+-------------------+
1 row in set (0.00 sec)
```
glb状态信息
```
[root@c1 ~]# service glb getinfo
Router:
------------------------------------------------------
        Address       :   weight   usage    map  conns
 192.168.64.145:3306  :    2.000   0.000    N/A      0
 192.168.64.146:3306  :    1.000   0.000    N/A      0
 192.168.64.147:3306  :    1.000   0.500    N/A      1
------------------------------------------------------
Destinations: 3, total connections: 1 of 256 max
```
可见已成功使用`Galera Load Balancer`完成`MariaDB Galera Cluster`负载

### 参考文章

[http://galeracluster.com/documentation-webpages/glb.html](http://galeracluster.com/documentation-webpages/glb.html)

[http://galeracluster.com/documentation-webpages/glbparameters.html#glb-other-options](http://galeracluster.com/documentation-webpages/glbparameters.html#glb-other-options)

[https://mariadb.com/blog/setup-mariadb-enterprise-cluster-part-3-setup-ha-proxy-load-balancer-read-and-write-pools](https://mariadb.com/blog/setup-mariadb-enterprise-cluster-part-3-setup-ha-proxy-load-balancer-read-and-write-pools)

[http://www.fromdual.com/making-haproxy-high-available-for-mysql-galera-cluster](http://www.fromdual.com/making-haproxy-high-available-for-mysql-galera-cluster)

[http://severalnines.com/tutorials/mysql-load-balancing-haproxy-tutorial](http://severalnines.com/tutorials/mysql-load-balancing-haproxy-tutorial)

[http://blog.secaserver.com/2015/09/configure-haproxy-galera-cluster/](http://blog.secaserver.com/2015/09/configure-haproxy-galera-cluster/)

[http://severalnines.com/blog/benchmark-load-balancers-mysqlmariadb-galera-cluster](http://severalnines.com/blog/benchmark-load-balancers-mysqlmariadb-galera-cluster)

[https://extremeshok.com/4845/ubuntu-12-04-lts-3-node-mariadb-cluster-percona-xtradb-cluster-mysql-galera/](https://extremeshok.com/4845/ubuntu-12-04-lts-3-node-mariadb-cluster-percona-xtradb-cluster-mysql-galera/)

[http://www.sebastien-han.fr/blog/2012/04/08/mysql-galera-cluster-with-haproxy/](http://www.sebastien-han.fr/blog/2012/04/08/mysql-galera-cluster-with-haproxy/)