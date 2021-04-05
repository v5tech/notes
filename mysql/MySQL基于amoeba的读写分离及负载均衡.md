# MySQL基于amoeba的读写分离及负载均衡

> 前提条件：
 * MySQL数据库已做好主从同步配置
 * 已安装Java环境并配置好环境变量

### 分别在主从数据库上为amoeba用户授权

```
grant all on crm.* to amoeba@'192.168.64.%' identified by 'amoeba';
flush privileges;
```
### 解压安装配置amoeba

```
tar zxvf amoeba-mysql-binary-2.2.0.tar.gz -Camoeba
```

修改dbServers.xml

```xml
<?xml version="1.0" encoding="gbk"?>

<!DOCTYPE amoeba:dbServers SYSTEM "dbserver.dtd">
<amoeba:dbServers xmlns:amoeba="http://amoeba.meidusa.com/">

                <!-- 
                        Each dbServer needs to be configured into a Pool,
                        If you need to configure multiple dbServer with load balancing that can be simplified by the following configuration:
                         add attribute with name virtual = "true" in dbServer, but the configuration does not allow the element with name factoryConfig
                         such as 'multiPool' dbServer   
                -->

        <dbServer name="abstractServer" abstractive="true">
                <factoryConfig class="com.meidusa.amoeba.mysql.net.MysqlServerConnectionFactory">
                        <property name="manager">${defaultManager}</property>
                        <property name="sendBufferSize">64</property>
                        <property name="receiveBufferSize">128</property>
                        <!-- amoeba内部操作管理mysql时的端口 -->
                        <property name="port">3306</property>
                        <!-- amoeba内部操作管理的数据库 -->
                        <property name="schema">crm</property>
                        <!-- amoeba内部连接用户名 -->
                        <property name="user">amoeba</property>
                        <!-- amoeba内部连接密码 -->
                        <property name="password">amoeba</property>
                </factoryConfig>

                <poolConfig class="com.meidusa.amoeba.net.poolable.PoolableObjectPool">
                        <property name="maxActive">500</property>
                        <property name="maxIdle">500</property>
                        <property name="minIdle">10</property>
                        <property name="minEvictableIdleTimeMillis">600000</property>
                        <property name="timeBetweenEvictionRunsMillis">600000</property>
                        <property name="testOnBorrow">true</property>
                        <property name="testOnReturn">true</property>
                        <property name="testWhileIdle">true</property>
                </poolConfig>
        </dbServer>

		<!-- 主数据库地址 -->
        <dbServer name="master"  parent="abstractServer">
                <factoryConfig>
                        <!-- mysql ip -->
                        <property name="ipAddress">192.168.64.131</property>
                </factoryConfig>
        </dbServer>

		<!-- 从数据库地址 -->
        <dbServer name="slave"  parent="abstractServer">
                <factoryConfig>
                        <!-- mysql ip -->
                        <property name="ipAddress">192.168.64.132</property>
                </factoryConfig>
        </dbServer>
		
		<!-- 配置在master和slave之间做负载均衡 -->
        <dbServer name="MultiPool" virtual="true">
                <poolConfig class="com.meidusa.amoeba.server.MultipleServerPool">
                        <!-- Load balancing strategy: 1=ROUNDROBIN , 2=WEIGHTBASED , 3=HA-->
                        <property name="loadbalance">1</property>
                        <!-- Separated by commas,such as: server1,server2,server3 -->
                        <property name="poolNames">master,slave</property>
                </poolConfig>
        </dbServer>

</amoeba:dbServers>
```

修改amoeba.xml

```xml
<?xml version="1.0" encoding="gbk"?>

<!DOCTYPE amoeba:configuration SYSTEM "amoeba.dtd">
<amoeba:configuration xmlns:amoeba="http://amoeba.meidusa.com/">
        <proxy>
                <!-- service class must implements com.meidusa.amoeba.service.Service -->
                <service name="Amoeba for Mysql" class="com.meidusa.amoeba.net.ServerableConnectionManager">
                        <!-- amoeba对外连接数据库时的端口 -->
                        <property name="port">8066</property>
                        <!-- amoeba对外连接数据库时的IP -->
                        <property name="ipAddress">127.0.0.1</property>
                        <property name="manager">${clientConnectioneManager}</property>
                        <property name="connectionFactory">
                                <bean class="com.meidusa.amoeba.mysql.net.MysqlClientConnectionFactory">
                                        <property name="sendBufferSize">128</property>
                                        <property name="receiveBufferSize">64</property>
                                </bean>
                        </property>
                        <property name="authenticator">
                                <bean class="com.meidusa.amoeba.mysql.server.MysqlClientAuthenticator">
										<!-- amoeba对外连接数据库时的用户名 -->
                                        <property name="user">root</property>
										<!-- amoeba对外连接数据库时的密码 -->
                                        <property name="password"></property>
                                        <property name="filter">
                                                <bean class="com.meidusa.amoeba.server.IPAccessController">
                                                        <property name="ipFile">${amoeba.home}/conf/access_list.conf</property>
                                                </bean>
                                        </property>
                                </bean>
                        </property>

                </service>
                <!-- server class must implements com.meidusa.amoeba.service.Service -->
                <service name="Amoeba Monitor Server" class="com.meidusa.amoeba.monitor.MonitorServer">
                        <!-- port -->
                        <!--  default value: random number
                        <property name="port">9066</property>
                        -->
                        <!-- bind ipAddress -->
                        <property name="ipAddress">127.0.0.1</property>
                        <property name="daemon">true</property>
                        <property name="manager">${clientConnectioneManager}</property>
                        <property name="connectionFactory">
                                <bean class="com.meidusa.amoeba.monitor.net.MonitorClientConnectionFactory"></bean>
                        </property>

                </service>
                <runtime class="com.meidusa.amoeba.mysql.context.MysqlRuntimeContext">
                        <!-- proxy server net IO Read thread size -->
                        <property name="readThreadPoolSize">20</property>
                        <!-- proxy server client process thread size -->
                        <property name="clientSideThreadPoolSize">30</property>
                        <!-- mysql server data packet process thread size -->
                        <property name="serverSideThreadPoolSize">30</property>
                        <!-- per connection cache prepared statement size  -->
                        <property name="statementCacheSize">500</property>
                        <!-- query timeout( default: 60 second , TimeUnit:second) -->
                        <property name="queryTimeout">60</property>
                </runtime>

        </proxy>
        <!-- 
                Each ConnectionManager will start as thread
                manager responsible for the Connection IO read , Death Detection
        -->
        <connectionManagerList>
                <connectionManager name="clientConnectioneManager" class="com.meidusa.amoeba.net.MultiConnectionManagerWrapper">
                        <property name="subManagerClassName">com.meidusa.amoeba.net.ConnectionManager</property>
                        <!-- 
                          default value is avaliable Processors 
                        <property name="processors">5</property>
                         -->
                </connectionManager>
                <connectionManager name="defaultManager" class="com.meidusa.amoeba.net.MultiConnectionManagerWrapper">
                        <property name="subManagerClassName">com.meidusa.amoeba.net.AuthingableConnectionManager</property>

                        <!-- 
                          default value is avaliable Processors 
                        <property name="processors">5</property>
                         -->
                </connectionManager>
        </connectionManagerList>
        <!-- default using file loader -->
        <dbServerLoader class="com.meidusa.amoeba.context.DBServerConfigFileLoader">
                <property name="configFile">${amoeba.home}/conf/dbServers.xml</property>
        </dbServerLoader>

        <queryRouter class="com.meidusa.amoeba.mysql.parser.MysqlQueryRouter">
                <property name="ruleLoader">
                        <bean class="com.meidusa.amoeba.route.TableRuleFileLoader">
                                <property name="ruleFile">${amoeba.home}/conf/rule.xml</property>
                                <property name="functionFile">${amoeba.home}/conf/ruleFunctionMap.xml</property>
                        </bean>
                </property>
                <property name="sqlFunctionFile">${amoeba.home}/conf/functionMap.xml</property>
                <property name="LRUMapSize">1500</property>
                <!--amoeba默认连接的server-->
                <property name="defaultPool">master</property>
                <!--允许在master上写数据-->
                <property name="writePool">master</property>
                <!--允许在MultiPool上读数据-->
                <property name="readPool">MultiPool</property>
                <property name="needParse">true</property>
        </queryRouter>
</amoeba:configuration>
```

### 修改vim amoeba/bin/amoeba文件

修改`-Xss128k`为以下内容

```
DEFAULT_OPTS="-server -Xms256m -Xmx256m -Xss256k"
```

### 启动amoeba到后台

```
amoeba start &
```

### 测试读写分离及负载均衡

在主库上crm数据库中创建zhang表

```
create table zhang (id int(10) ,name varchar(10),address varchar(20));
insert into zhang values('1','zhang','this_is_master');
```

停掉从库复制进程

```
stop slave;
insert into zhang values('2','zhang','this_is_slave');
```

### 连接amoeba客户端

```
mysql -uroot -p -h127.0.0.1 -P8066
mysql> use crm;
Database changed
# 因为默认连的数据库是主库，第一次查询查的是主库的数据
mysql> select * from zhang;
+------+-------+----------------+
| id   | name  | address        |
+------+-------+----------------+
|    1 | zhang | this_is_master |
|    2 | zhang | this_is_master |
|    3 | zhang | this_is_master |
|    4 | zhang | this_is_master |
+------+-------+----------------+
4 rows in set (0.00 sec)
# 因为开启了负载均衡并采用轮询模式该查询查的是从库的数据
mysql> select * from zhang;
+------+-------+---------------+
| id   | name  | address       |
+------+-------+---------------+
|    2 | zhang | this_is_slave |
+------+-------+---------------+
1 row in set (0.00 sec)

#  插入两条数据，观察数据插入到那个库中

mysql> insert into zhang values('5','zhang','this_is_master');
Query OK, 1 row affected (0.00 sec)

mysql> insert into zhang values('6','zhang','this_is_master');
Query OK, 1 row affected (0.01 sec)

# 查询的是主库

mysql> select * from zhang;
+------+-------+----------------+
| id   | name  | address        |
+------+-------+----------------+
|    1 | zhang | this_is_master |
|    2 | zhang | this_is_master |
|    3 | zhang | this_is_master |
|    4 | zhang | this_is_master |
|    5 | zhang | this_is_master |
|    6 | zhang | this_is_master |
+------+-------+----------------+
6 rows in set (0.00 sec)

# 查询的是从库

mysql> select * from zhang;
+------+-------+---------------+
| id   | name  | address       |
+------+-------+---------------+
|    2 | zhang | this_is_slave |
+------+-------+---------------+
1 row in set (0.00 sec)

```
可见数据插入到了主库上，因为配置了主库可读写、从库为只读，且主从同步中从库的同步进程已被停用。

查看主库数据

```
ubuntu@master:~$ mysql -uroot -h192.168.64.131 -P3306
mysql> use crm;
Database changed
mysql> select * from zhang;
+------+-------+----------------+
| id   | name  | address        |
+------+-------+----------------+
|    1 | zhang | this_is_master |
|    2 | zhang | this_is_master |
|    3 | zhang | this_is_master |
|    4 | zhang | this_is_master |
|    5 | zhang | this_is_master |
|    6 | zhang | this_is_master |
+------+-------+----------------+
6 rows in set (0.00 sec)
```

查看从库数据

```
ubuntu@slave:~$ mysql -uroot -h192.168.64.132 -P3306
mysql> show slave status\G
*************************** 1. row ***************************
               Slave_IO_State: 
                  Master_Host: 192.168.64.131
                  Master_User: repuser
                  Master_Port: 3306
                Connect_Retry: 60
              Master_Log_File: mysql-bin.000002
          Read_Master_Log_Pos: 4681
               Relay_Log_File: mysqld-relay-bin.000009
                Relay_Log_Pos: 4119
        Relay_Master_Log_File: mysql-bin.000002
             Slave_IO_Running: No
            Slave_SQL_Running: No
            ......
1 row in set (0.00 sec)
mysql> use crm;
Database changed
mysql> select * from zhang;
+------+-------+---------------+
| id   | name  | address       |
+------+-------+---------------+
|    2 | zhang | this_is_slave |
+------+-------+---------------+
1 row in set (0.00 se
```

参考文档

http://docs.hexnova.com/amoeba/