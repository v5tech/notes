# filebeat读取nginx日志配置

> 环境：filebeat 5.6

filebeat配置文件从头读取

1.停止filebeat
2.删除registry文件

过滤掉配置中的注释和空行

```bash
grep -v '#' /etc/filebeat/filebeat.yml | grep -v '^$'
```

filebeat.yml配置如下

```
filebeat.modules:
- module: nginx
  access:
      enabled: true
      var.paths: [ "/opt/nginx/logs/access.log" ]
      prospector:
          include_lines: ["GET /a/", "GET /c/"]
          exclude_lines: ["GET /api/", "GET /static/"]
          
output.elasticsearch:
  hosts: ["10.81.128.213:9500","10.81.128.114:9500","10.81.128.163:9500"]
```







