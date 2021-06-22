# Consul

---

## Consul常用操作

### 1、查询所有服务列表

```http
http://192.168.101.6:8500/v1/agent/services
```

### 2、查看服务详情

```http
http://192.168.101.6:8500/v1/agent/service/cmdb-318239142
```

### 3、列出所有无效的服务

```http
http://192.168.101.6:8500/v1/health/state/critical
```

### 4、摘除服务

```bash
 curl \
    --request PUT \
    http://192.168.101.6:8500/v1/agent/service/deregister/cmdb-318239142
```

### 5、批量移除无效的服务

```bash
#!/bin/bash

CONSUL_ADDRESS="192.168.101.6:8500"

test -d logs || mkdir logs

echo "---------------" >> logs/`date +%Y%m%d`.log

# 获取当前Consul中状态为critical的ServiceID
CONSUL_CRITICAL_SERVICEID=`curl -s -XGET http://${CONSUL_ADDRESS}/v1/health/state/critical | python -m json.tool | grep ServiceID | awk '{print $2}' |sed 's|"||g' | sed 's|,||g'`

for ServiceID in ${CONSUL_CRITICAL_SERVICEID}
do
  echo "${ServiceID} 已删除" >> logs/`date +%Y%m%d`.log
  # 使用Consul的API删除失效的ServiceID
  curl -XPUT http://${CONSUL_ADDRESS}/v1/agent/service/deregister/${ServiceID}
done
```

---

## 官方文档

* https://www.consul.io/api-docs/agent/service

---

## 教程

* [Consul中文文档—生产环境如何合理地部署Consul集群？](https://blog.csdn.net/shuai_wy/article/details/109190366)
* [Consul中文中档—Consul性能调优参数](https://blog.csdn.net/shuai_wy/article/details/109177867)
* [Consul中文文档—Consul运维常用API及常用CLI命令](https://blog.csdn.net/shuai_wy/article/details/106199295)
* [Consul高可用场景分析及实践总结—SpringCloud篇](https://blog.csdn.net/shuai_wy/article/details/109021820)

---

* [Consul+Registrator+Docker实现服务发现](https://blog.51cto.com/13972012/2446086)
* [基于consul+consul-template+registrator+nginx实现自动服务发现](https://www.cnblogs.com/skyflask/p/11193812.html)
* [基于Consul+Registrator+Nginx实现容器服务自动发现的集群框架](https://blog.51cto.com/ganbing/2086851)
* [Consul+Nginx动态部署重构后的接口](https://juejin.cn/post/6890410605350486030)