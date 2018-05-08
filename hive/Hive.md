# Hive基础教程

一、启动服务
加载环境变量

```bash
source /etc/profile
```

启动`Hive`服务

```bash
nohup hive --auxpath /opt/elasticsearch-hadoop-5.6.0/dist/elasticsearch-hadoop-5.6.0.jar --service metastore &
```

二、停服务

```bash
ps -ef|grep hive|grep -v grep |awk '{print $2}'|xargs -n 1 -i kill -9 {}
```

