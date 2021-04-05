# Apache、Tomcat7集群session共享及负载均衡

> 环境：httpd-2.4.18-win64+tomcat-7.0.X+mod_jk-1.2.41-win64

## 1. 使用mod_jk方式

### 1.1 windows下Apache下载及安装
https://www.apachelounge.com/download/

* 下载httpd-2.4.18-win64
https://www.apachelounge.com/download/VC14/binaries/httpd-2.4.18-win64-VC14.zip

* 下载安装 vc_redist_x64/86.exe
https://www.microsoft.com/en-us/download/details.aspx?id=49984

* 下载mod_jk-1.2.41
https://www.apachelounge.com/download/VC14/modules/mod_jk-1.2.41-win64-VC14.zip

### 1.2 安装Apache为系统服务

* 安装服务
```bash
httpd.exe -k install -n "Apache2.4" -f "C:\Apache24\conf\httpd.conf"
```
* 卸载服务
```bash
httpd.exe -k uninstall -n "Apache2.4"
```

参考文章：https://httpd.apache.org/docs/2.4/platform/windows.html#winsvc

### 1.3 Apache+mod_jk整合配置

* 拷贝mod_jk.so到C:\Apache24\modules目录

* 编辑C:\Apache24\conf\httpd.conf在文件末尾添加

```
Include conf/mod_jk.conf
```
* 创建C:\Apache24\conf\mod_jk.conf

```
LoadModule jk_module modules/mod_jk.so
JkWorkersFile conf/workers.properties
JkLogFile logs/mod_jk.log
JKLogLevel info
JKLogStampFormat "[%a %b %d %H:%M:%S %Y]"
JKOptions +ForwardKeySize +ForwardURICompat -ForwardDirectories
JKRequestLogFormat "%w %V %T %q %U %R"
JKMount /* loadbalancer
JkMount /jkmanager/* jkstatus

<Location /jkmanager/>
    JkMount jkstatus
    Order deny,allow
    Deny from all
    Allow from 127.0.0.1
</Location>
```

* 创建C:\Apache24\conf\workers.properties

```
worker.list=tomcat1,tomcat2,loadbalancer,jkstatus

# tomcat1
# tomcat的server.xml文件中AJP/1.3协议的端口号，默认是8009
worker.tomcat1.port=8009
worker.tomcat1.host=192.168.0.201
worker.tomcat1.type=ajp13
worker.tomcat1.lbfactor=1 #负载均衡权重值（1-100）

# 其他配置参数
# worker.tomcat1.cachesize=100
# worker.tomcat1.cachesize_timeout=600
# worker.tomcat1.reclycle_timeout=300
# worker.tomcat1.socket_keepalive=1
# worker.tomcat1.socket_timeout=300
# worker.tomcat1.local_worker=1
# worker.tomcat1.retries=3

# tomcat2
# tomcat的server.xml文件中AJP/1.3协议的端口号
worker.tomcat2.port=9009
worker.tomcat2.host=192.168.0.201
worker.tomcat2.type=ajp13
worker.tomcat2.lbfactor=1 #负载均衡权重值（1-100）

# load balancer worker
worker.loadbalancer.type=lb
worker.loadbalancer.balance_workers=tomcat1,tomcat2
worker.loadbalancer.sticky_session=false
worker.loadbalancer.sticky_session_force=true

# Add the status worker to the worker list
worker.jkstatus.type=status
```

### 1.4 配置tomcat/conf/server.xml文件

* tomcat1

```xml
<Server port="8005" shutdown="SHUTDOWN">
<Connector port="8080" protocol="HTTP/1.1"
               connectionTimeout="20000"
               redirectPort="8443" />
<Connector port="8009" protocol="AJP/1.3" redirectPort="8443" />
<Engine name="Catalina" defaultHost="localhost" jvmRoute="tomcat1">
<Cluster className="org.apache.catalina.ha.tcp.SimpleTcpCluster"
            channelSendOptions="8"> 
            <Manager className="org.apache.catalina.ha.session.BackupManager"
              expireSessionsOnShutdown="false"
              notifyListenersOnReplication="true"
              mapSendOptions="6"/> 
            <Channel className="org.apache.catalina.tribes.group.GroupChannel"> 
            <Membership className="org.apache.catalina.tribes.membership.McastService"
                        address="228.0.0.4"
                        port="45564"
                        frequency="500"
                        dropTime="3000"/> 
            <Receiver className="org.apache.catalina.tribes.transport.nio.NioReceiver"
                      address="127.0.0.1"
                      port="4000"
                      autoBind="100"
                      selectorTimeout="5000"
                      maxThreads="6"/> 
            <Sender className="org.apache.catalina.tribes.transport.ReplicationTransmitter"> 
              <Transport className="org.apache.catalina.tribes.transport.nio.PooledParallelSender"/> 
            </Sender> 
            <Interceptor className="org.apache.catalina.tribes.group.interceptors.TcpFailureDetector"/> 
            <Interceptor className="org.apache.catalina.tribes.group.interceptors.MessageDispatch15Interceptor"/> 
            </Channel> 
            <Valve className="org.apache.catalina.ha.tcp.ReplicationValve" filter=""/> 
            <Valve className="org.apache.catalina.ha.session.JvmRouteBinderValve"/> 
            <ClusterListener className="org.apache.catalina.ha.session.JvmRouteSessionIDBinderListener"/> 
            <ClusterListener className="org.apache.catalina.ha.session.ClusterSessionListener"/> 
</Cluster>
```

* tomcat2

```xml
<Server port="9005" shutdown="SHUTDOWN">
<Connector port="9080" protocol="HTTP/1.1"
               connectionTimeout="20000"
               redirectPort="8443" />
<Connector port="9009" protocol="AJP/1.3" redirectPort="8443" />
<Engine name="Catalina" defaultHost="localhost" jvmRoute="tomcat2">
<Cluster className="org.apache.catalina.ha.tcp.SimpleTcpCluster"
            channelSendOptions="8"> 
            <Manager className="org.apache.catalina.ha.session.BackupManager"
              expireSessionsOnShutdown="false"
              notifyListenersOnReplication="true"
              mapSendOptions="6"/> 
            <Channel className="org.apache.catalina.tribes.group.GroupChannel"> 
            <Membership className="org.apache.catalina.tribes.membership.McastService"
                        address="228.0.0.4"
                        port="45564"
                        frequency="500"
                        dropTime="3000"/> 
            <Receiver className="org.apache.catalina.tribes.transport.nio.NioReceiver"
                      address="127.0.0.1"
                      port="5000"
                      autoBind="100"
                      selectorTimeout="5000"
                      maxThreads="6"/> 
            <Sender className="org.apache.catalina.tribes.transport.ReplicationTransmitter"> 
              <Transport className="org.apache.catalina.tribes.transport.nio.PooledParallelSender"/> 
            </Sender> 
            <Interceptor className="org.apache.catalina.tribes.group.interceptors.TcpFailureDetector"/> 
            <Interceptor className="org.apache.catalina.tribes.group.interceptors.MessageDispatch15Interceptor"/> 
            </Channel> 
            <Valve className="org.apache.catalina.ha.tcp.ReplicationValve" filter=""/> 
            <Valve className="org.apache.catalina.ha.session.JvmRouteBinderValve"/> 
            <ClusterListener className="org.apache.catalina.ha.session.JvmRouteSessionIDBinderListener"/> 
            <ClusterListener className="org.apache.catalina.ha.session.ClusterSessionListener"/> 
</Cluster>
```

### 1.5 在web.xml的`<web-app></web-app>`中添加`<distributable/>`

### 1.6 查看集群状态

http://127.0.0.1/jkmanager/

### 1.7 参考文章

https://httpd.apache.org/docs/2.4/platform/windows.html#winsvc

https://zybuluo.com/Fancy-Bai/note/118782

https://zybuluo.com/Fancy-Bai/note/118883

http://xiexiaojun.blog.51cto.com/2305291/1717850

http://blog.csdn.net/chaijunkun/article/details/6987443

http://my.oschina.net/heartdong/blog/98416

## 2. 使用mod_proxy方式

### 2.1 修改Apache配置文件（Apache24\conf\httpd.conf），加载以下模块

```
LoadModule proxy_module modules/mod_proxy.so
LoadModule proxy_ajp_module modules/mod_proxy_ajp.so
LoadModule proxy_balancer_module modules/mod_proxy_balancer.so
LoadModule proxy_connect_module modules/mod_proxy_connect.so
LoadModule proxy_http_module modules/mod_proxy_http.so
LoadModule slotmem_shm_module modules/mod_slotmem_shm.so
LoadModule lbmethod_bybusyness_module modules/mod_lbmethod_bybusyness.so
LoadModule lbmethod_byrequests_module modules/mod_lbmethod_byrequests.so
LoadModule lbmethod_bytraffic_module modules/mod_lbmethod_bytraffic.so
LoadModule status_module modules/mod_status.so
```

### 2.2 配置（Apache24\conf\httpd.conf）

* 使用mod_proxy_http

```
ProxyPass "/" "balancer://httpproxy/" stickysession=JSESSIONID|jsessionid nofailover=On
<Proxy "balancer://httpproxy/">
  BalancerMember "http://127.0.0.1:8080/" loadfactor=1
  BalancerMember "http://127.0.0.1:9080/" loadfactor=1
  ProxySet lbmethod=byrequests
</Proxy>

<Location "/balancer-manager">
  SetHandler balancer-manager
  Allow from 127.0.0.1
</Location>
```

* 使用mod_proxy_ajp

```
ProxyPass "/" "balancer://ajpproxy/"
<Proxy "balancer://ajpproxy/">
  BalancerMember "ajp://127.0.0.1:8009/" loadfactor=1 route=tomcat1
  BalancerMember "ajp://127.0.0.1:9009/" loadfactor=1 route=tomcat2
</Proxy>

<Location "/balancer-manager">
  SetHandler balancer-manager
  Allow from 127.0.0.1
</Location>
```

### 2.3 修改tomcat/conf/server.xml

tomcat1

```xml
<Server port="8005" shutdown="SHUTDOWN">
<Connector port="8080" protocol="HTTP/1.1"
               connectionTimeout="20000"
               redirectPort="8443" />
<Connector port="8009" protocol="AJP/1.3" redirectPort="8443" />
<Engine name="Catalina" defaultHost="localhost" jvmRoute="tomcat1">
<Cluster className="org.apache.catalina.ha.tcp.SimpleTcpCluster"
            channelSendOptions="8"> 
            <Manager className="org.apache.catalina.ha.session.BackupManager"
              expireSessionsOnShutdown="false"
              notifyListenersOnReplication="true"
              mapSendOptions="6"/> 
            <Channel className="org.apache.catalina.tribes.group.GroupChannel"> 
            <Membership className="org.apache.catalina.tribes.membership.McastService"
                        address="228.0.0.4"
                        port="45564"
                        frequency="500"
                        dropTime="3000"/> 
            <Receiver className="org.apache.catalina.tribes.transport.nio.NioReceiver"
                      address="127.0.0.1"
                      port="4000"
                      autoBind="100"
                      selectorTimeout="5000"
                      maxThreads="6"/> 
            <Sender className="org.apache.catalina.tribes.transport.ReplicationTransmitter"> 
              <Transport className="org.apache.catalina.tribes.transport.nio.PooledParallelSender"/> 
            </Sender> 
            <Interceptor className="org.apache.catalina.tribes.group.interceptors.TcpFailureDetector"/> 
            <Interceptor className="org.apache.catalina.tribes.group.interceptors.MessageDispatch15Interceptor"/> 
            </Channel> 
            <Valve className="org.apache.catalina.ha.tcp.ReplicationValve" filter=""/> 
            <Valve className="org.apache.catalina.ha.session.JvmRouteBinderValve"/> 
            <ClusterListener className="org.apache.catalina.ha.session.JvmRouteSessionIDBinderListener"/> 
            <ClusterListener className="org.apache.catalina.ha.session.ClusterSessionListener"/> 
</Cluster>
```

tomcat2

```xml
<Server port="9005" shutdown="SHUTDOWN">
<Connector port="9080" protocol="HTTP/1.1"
               connectionTimeout="20000"
               redirectPort="8443" />
<Connector port="9009" protocol="AJP/1.3" redirectPort="8443" />
<Engine name="Catalina" defaultHost="localhost" jvmRoute="tomcat2">
<Cluster className="org.apache.catalina.ha.tcp.SimpleTcpCluster"
            channelSendOptions="8"> 
            <Manager className="org.apache.catalina.ha.session.BackupManager"
              expireSessionsOnShutdown="false"
              notifyListenersOnReplication="true"
              mapSendOptions="6"/> 
            <Channel className="org.apache.catalina.tribes.group.GroupChannel"> 
            <Membership className="org.apache.catalina.tribes.membership.McastService"
                        address="228.0.0.4"
                        port="45564"
                        frequency="500"
                        dropTime="3000"/> 
            <Receiver className="org.apache.catalina.tribes.transport.nio.NioReceiver"
                      address="127.0.0.1"
                      port="5000"
                      autoBind="100"
                      selectorTimeout="5000"
                      maxThreads="6"/> 
            <Sender className="org.apache.catalina.tribes.transport.ReplicationTransmitter"> 
              <Transport className="org.apache.catalina.tribes.transport.nio.PooledParallelSender"/> 
            </Sender> 
            <Interceptor className="org.apache.catalina.tribes.group.interceptors.TcpFailureDetector"/> 
            <Interceptor className="org.apache.catalina.tribes.group.interceptors.MessageDispatch15Interceptor"/> 
            </Channel> 
            <Valve className="org.apache.catalina.ha.tcp.ReplicationValve" filter=""/> 
            <Valve className="org.apache.catalina.ha.session.JvmRouteBinderValve"/> 
            <ClusterListener className="org.apache.catalina.ha.session.JvmRouteSessionIDBinderListener"/> 
            <ClusterListener className="org.apache.catalina.ha.session.ClusterSessionListener"/> 
</Cluster>
```
### 2.4 查看balancer-manager

http://127.0.0.1/balancer-manager

### 2.5 参考文章

http://toplchx.iteye.com/blog/1928390

http://blog.csdn.net/wangjunjun2008/article/details/38268483