# windows平台下搭建nginx tomcat memcached分布式应用及session共享

### Installing Memcached on Windows

http://downloads.northscale.com/memcached-1.4.5-amd64.zip

### 启动Memcached

```bash
c:\memcached\memcached.exe
```
### 添加依赖、修改配置

核心公共依赖

```
memcached-session-manager-1.8.3.jar 
memcached-session-manager-tc7-1.8.3.jar
spymemcached-2.11.1.jar
```

* java默认序列化tomcat配置

不需要添加任何依赖jar，只添加上述核心公共依赖

```xml
<Manager className="de.javakaffee.web.msm.MemcachedBackupSessionManager"
           memcachedNodes="n1:127.0.0.1:11211"
           sticky="false"
           sessionBackupAsync="false"
		   sessionBackupTimeout="100"
           lockingMode="auto"
           requestUriIgnorePattern=".*\.(ico|png|gif|jpg|css|js)$"
           transcoderFactoryClass="de.javakaffee.web.msm.JavaSerializationTranscoderFactory"
/>
```
* kryo序列化tomcat配置

```
msm-kryo-serializer-1.8.3.jar
kryo-serializers-0.11.jar
kryo-1.04.jar
minlog-1.2.jar
reflectasm-1.01.jar
asm-3.2.jar
```

```xml
<Manager className="de.javakaffee.web.msm.MemcachedBackupSessionManager"
           memcachedNodes="n1:127.0.0.1:11211"
           sticky="false"
           sessionBackupAsync="false"
		   sessionBackupTimeout="100"
           lockingMode="auto"
           requestUriIgnorePattern=".*\.(ico|png|gif|jpg|css|js)$"
		   copyCollectionsForSerialization="true"
           transcoderFactoryClass="de.javakaffee.web.msm.serializer.kryo.KryoTranscoderFactory"
/>
```
* javolution序列化tomcat依赖

```
msm-javolution-serializer-1.8.3.jar
javolution-5.4.3.1.jar
```

```xml
<Manager className="de.javakaffee.web.msm.MemcachedBackupSessionManager"
           memcachedNodes="n1:127.0.0.1:11211"
           sticky="false"
           sessionBackupAsync="false"
		   sessionBackupTimeout="100"
           lockingMode="auto"
           requestUriIgnorePattern=".*\.(ico|png|gif|jpg|css|js)$"
		   copyCollectionsForSerialization="true"
           transcoderFactoryClass="de.javakaffee.web.msm.serializer.javolution.JavolutionTranscoderFactory"
/>
```
* xstream序列化tomcat配置

```
msm-xstream-serializer-1.8.3.jar
xstream-1.4.8.jar
xmlpull-1.1.3.4a.jar
xpp3_min-1.1.4c.jar
```

```xml
<Manager className="de.javakaffee.web.msm.MemcachedBackupSessionManager"
           memcachedNodes="n1:127.0.0.1:11211"
           sticky="false"
           sessionBackupAsync="false"
		   sessionBackupTimeout="100"
           lockingMode="auto"
           requestUriIgnorePattern=".*\.(ico|png|gif|jpg|css|js)$"
           transcoderFactoryClass="de.javakaffee.web.msm.serializer.xstream.XStreamTranscoderFactory"
/>
```
* flexjson序列化tomcat配置

```
msm-flexjson-serializer-1.8.3.jar
flexjson-3.3.jar
```

```xml
<Manager className="de.javakaffee.web.msm.MemcachedBackupSessionManager"
           memcachedNodes="n1:127.0.0.1:11211"
           sticky="false"
           sessionBackupAsync="false"
		   sessionBackupTimeout="100"
           lockingMode="auto"
           requestUriIgnorePattern=".*\.(ico|png|gif|jpg|css|js)$"
           transcoderFactoryClass="de.javakaffee.web.msm.serializer.json.JSONTranscoderFactory"
/>
```

*注：以上所有的修改均为修改tomcat/conf/context.xml文件* 

### 启动nginx、tomcat

### 参考文档

https://commaster.net/content/installing-memcached-windows

http://chenzhou123520.iteye.com/blog/1650212

http://blog.csdn.net/zhu_tianwei/article/details/18033483

https://github.com/magro/memcached-session-manager

https://github.com/magro/memcached-session-manager/wiki/SetupAndConfiguration

https://github.com/magro/memcached-session-manager/wiki/SerializationStrategies