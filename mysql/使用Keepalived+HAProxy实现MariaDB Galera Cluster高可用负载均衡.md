软件环境

soft|c1(192.168.64.145)|c2(192.168.64.146)|c3(192.168.64.147)
---|---|---|---
CentOS 7.2.1511|c1|c2|c3
MariaDB Galera Cluster|c1|c2|c3
Keepalived(192.168.64.100)|c1|c2
HAProxy|c1|c2

# 使用Keepalived+HAProxy实现MariaDB Galera Cluster高可用负载均衡

安装

```bash
yum install keepalived
yum install haproxy
```

修改c1上的`/etc/keepalived/keepalived.conf`

```
global_defs {
    lvs_id haproxy
}
vrrp_script check_haproxy {
    script "killall -0 haproxy"
    interval 2
    weight 2
}
vrrp_instance VI {
    state MASTER
    interface eth0
    virtual_router_id 100 
    priority 101 

    virtual_ipaddress {
        192.168.64.100
    }   
    
    track_script {
        check_haproxy
    }   
}
```

修改c2上的`/etc/keepalived/keepalived.conf`

```
global_defs {
    lvs_id haproxy
}
vrrp_script check_haproxy {
    script "killall -0 haproxy"
    interval 2
    weight 2
}
vrrp_instance VI {
    state SLAVE
    interface eth0
    virtual_router_id 100 
    priority 100 
    
    virtual_ipaddress {
        192.168.64.100
    }   
    
    track_script {
        check_haproxy
    }   
}
```

分别启动c1、c2的HAProxy、Keepalived

```
service haproxy start
service keepalived start
```

观察`c1`上的`eth0`

```
[root@c1 keepalived]# ip addr show eth0
2: eth0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state UP qlen 1000
    link/ether 00:0c:29:6f:9a:fa brd ff:ff:ff:ff:ff:ff
    inet 192.168.64.145/24 brd 192.168.64.255 scope global eth0
       valid_lft forever preferred_lft forever
    inet 192.168.64.100/32 scope global eth0
       valid_lft forever preferred_lft forever
    inet6 fe80::20c:29ff:fe6f:9afa/64 scope link 
       valid_lft forever preferred_lft forever
```

停掉`c1`上的`HAProxy`或`Keepalived`

```
[root@c1 keepalived]# service haproxy stop
Redirecting to /bin/systemctl stop  haproxy.service
[root@c1 keepalived]# ip addr show eth0
2: eth0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state UP qlen 1000
    link/ether 00:0c:29:6f:9a:fa brd ff:ff:ff:ff:ff:ff
    inet 192.168.64.145/24 brd 192.168.64.255 scope global eth0
       valid_lft forever preferred_lft forever
    inet6 fe80::20c:29ff:fe6f:9afa/64 scope link 
       valid_lft forever preferred_lft forever
```

观察`c2`上的`eth0`

```
[root@c2 keepalived]# ip addr show eth0
2: eth0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state UP qlen 1000
    link/ether 00:0c:29:f8:5a:5b brd ff:ff:ff:ff:ff:ff
    inet 192.168.64.146/24 brd 192.168.64.255 scope global eth0
       valid_lft forever preferred_lft forever
    inet 192.168.64.100/32 scope global eth0
       valid_lft forever preferred_lft forever
    inet6 fe80::20c:29ff:fef8:5a5b/64 scope link 
       valid_lft forever preferred_lft forever
```

可见IP`192.168.64.100`转向到了`c2`IP`192.168.64.100`漂移成功

测试

```
mysql --user=root --password=root --host=192.168.64.100 --port=3308 --execute="SELECT @@wsrep_node_name;"
+-------------------+
| @@wsrep_node_name |
+-------------------+
| c1                |
+-------------------+
mysql --user=root --password=root --host=192.168.64.100 --port=3308 --execute="SELECT @@wsrep_node_name;"
+-------------------+
| @@wsrep_node_name |
+-------------------+
| c2                |
+-------------------+
mysql --user=root --password=root --host=192.168.64.100 --port=3308 --execute="SELECT @@wsrep_node_name;"
+-------------------+
| @@wsrep_node_name |
+-------------------+
| c3                |
+-------------------+
```

### 使用`pen`来做负载均衡

启动`pen`

```bash
pen -r -l pen.log -p pen.pid 127.0.0.1:3838 \
      192.168.64.145:3306 \
      192.168.64.146:3306 \
      192.168.64.147:3306
```

测试负载

```bash
mysql --user=root --password=root --host=127.0.0.1 --port=3838 --execute="SELECT @@wsrep_node_name;"
+-------------------+
| @@wsrep_node_name |
+-------------------+
| c2                |
+-------------------+
mysql --user=root --password=root --host=127.0.0.1 --port=3838 --execute="SELECT @@wsrep_node_name;"
+-------------------+
| @@wsrep_node_name |
+-------------------+
| c3                |
+-------------------+
mysql --user=root --password=root --host=127.0.0.1 --port=3838 --execute="SELECT @@wsrep_node_name;"
+-------------------+
| @@wsrep_node_name |
+-------------------+
| c1                |
+-------------------+
```

### 参考文档

http://dasunhegoda.com/how-to-setup-haproxy-with-keepalived/833/


