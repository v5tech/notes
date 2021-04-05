# Prometheus 学习笔记

## 安装

版本选择：

* ## prometheus

[prometheus-2.24.1.darwin-amd64.tar.gz](https://github.com/prometheus/prometheus/releases/download/v2.24.1/prometheus-2.24.1.darwin-amd64.tar.gz)

[prometheus-2.24.1.linux-amd64.tar.gz](https://github.com/prometheus/prometheus/releases/download/v2.24.1/prometheus-2.24.1.linux-amd64.tar.gz)

[prometheus-2.24.1.windows-amd64.zip](https://github.com/prometheus/prometheus/releases/download/v2.24.1/prometheus-2.24.1.windows-amd64.zip)

* ## alertmanager

[alertmanager-0.21.0.darwin-amd64.tar.gz](https://github.com/prometheus/alertmanager/releases/download/v0.21.0/alertmanager-0.21.0.darwin-amd64.tar.gz)

[alertmanager-0.21.0.linux-amd64.tar.gz](https://github.com/prometheus/alertmanager/releases/download/v0.21.0/alertmanager-0.21.0.linux-amd64.tar.gz)

[alertmanager-0.21.0.windows-amd64.tar.gz](https://github.com/prometheus/alertmanager/releases/download/v0.21.0/alertmanager-0.21.0.windows-amd64.tar.gz)

* ## blackbox_exporter

[blackbox_exporter-0.18.0.darwin-amd64.tar.gz](https://github.com/prometheus/blackbox_exporter/releases/download/v0.18.0/blackbox_exporter-0.18.0.darwin-amd64.tar.gz)

[blackbox_exporter-0.18.0.linux-amd64.tar.gz](https://github.com/prometheus/blackbox_exporter/releases/download/v0.18.0/blackbox_exporter-0.18.0.linux-amd64.tar.gz)

[blackbox_exporter-0.18.0.windows-amd64.tar.gz](https://github.com/prometheus/blackbox_exporter/releases/download/v0.18.0/blackbox_exporter-0.18.0.windows-amd64.tar.gz)

* ## node_exporter

[node_exporter-1.0.1.darwin-amd64.tar.gz](https://github.com/prometheus/node_exporter/releases/download/v1.0.1/node_exporter-1.0.1.darwin-amd64.tar.gz)

[node_exporter-1.0.1.linux-amd64.tar.gz](https://github.com/prometheus/node_exporter/releases/download/v1.0.1/node_exporter-1.0.1.linux-amd64.tar.gz)

[windows_exporter-0.15.0-amd64.exe](https://github.com/prometheus-community/windows_exporter/releases/download/v0.15.0/windows_exporter-0.15.0-amd64.exe)

### 安装prometheus

```bash
./prometheus --config.file=prometheus.yml --web.enable-lifecycle &
```

浏览器访问`http://localhost:9090/`

```
--config.file

--web.listen-address 

--web.external-url

--web.enable-lifecycle

--web.enable-admin-api

-alertmanager.url
```

prometheus.yml

```yml
# my global config
global:
  scrape_interval:     15s # Set the scrape interval to every 15 seconds. Default is every 1 minute.
  evaluation_interval: 15s # Evaluate rules every 15 seconds. The default is every 1 minute.
  # scrape_timeout is set to the global default (10s).

# Alertmanager configuration
alerting:
  alertmanagers:
  - static_configs:
    - targets:
      - localhost:9093

# Load rules once and periodically evaluate them according to the global 'evaluation_interval'.
rule_files:
  # - "first_rules.yml"
  # - "second_rules.yml"

# A scrape configuration containing exactly one endpoint to scrape:
# Here it's Prometheus itself.
scrape_configs:
  # The job name is added as a label `job=<job_name>` to any timeseries scraped from this config.
  
  # 监控prometheus自身
  - job_name: 'prometheus'

    # metrics_path defaults to '/metrics'
    # scheme defaults to 'http'.

    static_configs:
    - targets: ['localhost:9090']

  # 监控node1实例
  - job_name: 'node1'
    static_configs:
    - targets: ['localhost:9100']
      labels:
        instance: 'node1'
  # 监控node2实例
  - job_name: 'node2'
    params:
      # 过滤收集器
      collect[]:
        - cpu
        - meminfo
        - diskstats
        - netstat
        - filefd
        - filesystem
        - xfs
        - systemd
    static_configs:
    - targets: ['192.168.223.2:9100']
      labels:
        instance: 'node2'
```

### 校验配置文件

```bash
./promtool check config prometheus.yml
Checking prometheus.yml
  SUCCESS: 2 rule files found

Checking alerts/memory_over.yml
  SUCCESS: 1 rules found

Checking alerts/server_down.yml
  SUCCESS: 1 rules found
```

### 告警规则文件

alerts/memory_over.yml

```yaml
groups:
  - name: memory_over
    rules:
      - alert: NodeMemoryUsage
        expr: (node_memory_MemTotal_bytes - (node_memory_MemFree_bytes+node_memory_Buffers_bytes+node_memory_Cached_bytes )) / node_memory_MemTotal_bytes * 100 > 80
        for: 20s
        labels:
          user: swfeng
        annotations:
          summary: "{{$labels.instance}}: High Memory usage detected"
          description: "{{$labels.instance}}: Memory usage is above 80% (current value is:{{ $value }})"
```

alerts/server_down.yml

```yaml
groups:
  - name: server_down
    rules:
      - alert: InstanceDown
        expr: up == 0
        for: 20s
        labels:
          user: swfeng
        annotations:
          summary: "Instance {{ $labels.instance }} down"
          description: "{{ $labels.instance }} of job {{ $labels.job }} has been down for more than 20 s."
```

### 配置文件热加载

当`prometheus`启动时开启`--web.enable-lifecycle`配置项后，当`prometheus`主配置文件发生修改后可发送如下的`POST`请求实现`prometheus`的热加载。

```bash
curl -X POST 'http://localhost:9090/-/reload'
```

配置生效后会在`prometheus`的控制台输出`Loading configuration file`信息。若配置文件有误则不会生效

```
level=info ts=2021-02-02T03:31:11.811Z caller=main.go:887 msg="Loading configuration file" filename=prometheus.yml
level=info ts=2021-02-02T03:31:16.320Z caller=main.go:918 msg="Completed loading of configuration file" filename=prometheus.yml totalDuration=4.508586935s remote_storage=2.961µs web_handler=656ns query_engine=2.136µs scrape=4.505752137s scrape_sd=90.392µs notify=21.123µs notify_sd=36.438µs rules=2.408µs
```

### 过滤收集器

```yaml
scrape_configs:
...
- job_name: 'node'
  static_configs:
    - targets: ['192.168.27.136:9100', '192.168.27.138:9100', '192.168.27.139:9100']
  params:
    collect[]:
      - cpu
      - meminfo
      - diskstats
      - netdev
      - netstat
      - filefd
      - filesystem
      - xfs
      - systemd
```

使用`params`块中`collect[]`列表指定，然后将它们作为URL参数传递给抓取请求。你可以使用Node Exporter实例上的curl命令来对此进行测试（只收集`cpu`指标，其它指标忽略）

```bash
curl -g -X GET 'http://192.168.223.2:9100/metrics?collect[]=cpu'
```

### 安装node_exporter

下载对应操作系统的`node_exporter`，解压直接运行即可

```bash
./node_exporter &
```

默认监听端口为`9100`，打开浏览器访问`http://IP:9100/metrics`即可看到监控指标

根据物理主机的不同，具体的监控指标也有差异：

- node_boot_time_seconds：系统启动时间
- node_cpu_*：系统CPU使用量
- node_disk_*：磁盘IO
- node_filesystem_*：文件系统用量
- node_load1：系统负载
- node_memeory_*：内存使用量
- node_network_*：网络带宽
- node_time：当前系统时间
- go_*：node exporter中go相关指标
- process_*：node exporter自身进程相关运行指标



### Management API

* 健康检查

```
GET http://localhost:9090/-/healthy
```

* 准备就绪检查

```
GET http://localhost:9090/-/ready
```

* 热加载

```
PUT  http://localhost:9090/-/reload
POST http://localhost:9090/-/reload
```

会向`Prometheus`进程发送`SIGTERM`信号，需要开启`--web.enable-lifecycle` 选项。

* 退出

```
PUT  http://localhost:9090/-/quit
POST http://localhost:9090/-/quit
```

优雅停机，会向`Prometheus`进程发送`SIGTERM`信号。需要开启`--web.enable-lifecycle` 选项。

# Alertmanager

alertmanager.yml

```yaml
global:
  resolve_timeout: 5m
  smtp_smarthost: 'smtp.exmail.qq.com:465'
  smtp_from: 'fengj@anchnet.com'
  smtp_auth_username: 'fengj@anchnet.com'
  smtp_auth_password: '******'
  smtp_require_tls: false

route:
  group_by: ['alertname']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 1h
  receiver: 'mail-receiver'

receivers:
- name: 'mail-receiver'
  email_configs:
    - to: 'fengj@anchnet.com'
      send_resolved: true
```

### 校验配置文件

```bash
./amtool check-config alertmanager.yml
Checking 'alertmanager.yml'  SUCCESS
Found:
 - global config
 - route
 - 0 inhibit rules
 - 1 receivers
 - 0 templates
```

### 安装Alertmanager

```bash
./alertmanager --config.file=alertmanager.yml
```

浏览器访问`http://localhost:9093`

### Management API

* 健康检查

```
GET http://localhost:9093/-/healthy
```

* 准备就绪检查

```
GET http://localhost:9093/-/ready
```

* 热加载

```
POST http://localhost:9093/-/reload
alertmanager           | level=info ts=2021-02-04T02:26:59.032Z caller=coordinator.go:119 component=configuration msg="Loading configuration file" file=/etc/alertmanager/alertmanager.yml
alertmanager           | level=info ts=2021-02-04T02:26:59.036Z caller=coordinator.go:131 component=configuration msg="Completed loading of configuration file" file=/etc/alertmanager/alertmanager.yml
```

```bash
curl -X POST 'http://localhost:9093/-/reload'
```

会向`Alertmanager`进程发送`SIGTERM`信号。



# Prometheus远程存储

https://prometheus.io/docs/operating/integrations/#remote-endpoints-and-storage

https://github.com/timescale/promscale/blob/master/docs/docker.md

https://github.com/timescale/promscale/blob/master/docker-compose/high-availability/docker-compose.yml

### [PostgreSQL/TimescaleDB](https://github.com/timescale/promscale)

* Docker方式:

```bash
# 创建网络
docker network create --driver bridge promscale-timescaledb
# 运行 TimescaleDB
docker run --name timescaledb -e POSTGRES_PASSWORD=<password> -d -p 5432:5432 --network promscale-timescaledb timescaledev/promscale-extension:latest-pg12 postgres -csynchronous_commit=off
# 运行 Promscale
docker run --name promscale -d -p 9201:9201 --network promscale-timescaledb timescale/promscale:<version-tag> -db-password=<password> -db-port=5432 -db-name=postgres -db-host=timescaledb -db-ssl-mode=allow
```

完整的`docker-compose.yml`文件如下:

```yaml
version: '3'

services:

  db:
    image: timescaledev/promscale-extension:latest-pg12
    ports:
      - 5432:5432/tcp
    environment:
      POSTGRES_PASSWORD: postgres
      POSTGRES_USER: postgres
      POSTGRES_DB: timescale

  prometheus:
    image: prom/prometheus:v2.24.1
    container_name: prometheus
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml:ro
      - ./alerts/memory_over.yml:/etc/prometheus/alerts/memory_over.yml:ro
      - ./alerts/server_down.yml:/etc/prometheus/alerts/server_down.yml:ro
    ports:
      - 9090:9090
    restart: always

  promscale-connector:
    image: timescale/promscale:latest
    ports:
      - 9201:9201/tcp
    restart: on-failure
    depends_on:
      - db
      - prometheus
    environment:
      PROMSCALE_LOG_LEVEL: debug
      PROMSCALE_DB_CONNECT_RETRIES: 10
      PROMSCALE_DB_HOST: db
      PROMSCALE_DB_PASSWORD: postgres
      PROMSCALE_WEB_TELEMETRY_PATH: /metrics-text
      PROMSCALE_DB_SSL_MODE: allow

  alertmanager:
    image: prom/alertmanager:v0.21.0
    container_name: alertmanager
    volumes:
      - ./alertmanager.yml:/etc/alertmanager/alertmanager.yml
    ports:
      - 9093:9093
    restart: always

  grafana:
    image: grafana/grafana
    container_name: grafana
    ports:
      - 3000:3000
    restart: always

networks:
  default:
    driver: bridge
```

* 二进制方式:

[promscale_0.1.4_Darwin_x86_64](https://github.com/timescale/promscale/releases/download/0.1.4/promscale_0.1.4_Darwin_x86_64)

[promscale_0.1.4_Linux_x86_64](https://github.com/timescale/promscale/releases/download/0.1.4/promscale_0.1.4_Linux_x86_64)

下载对应平台的二进制文件。

```bash
chmod +x promscale
./promscale --db-name <DBNAME> --db-password <DB-Password> --db-ssl-mode allow
```

在`prometheus.yml`配置文件中添加如下配置

```yam
remote_write:
  - url: "http://<connector-address>:9201/write"
remote_read:
  - url: "http://<connector-address>:9201/read"
```

### [InfluxDB](https://docs.influxdata.com/influxdb/v1.8/supported_protocols/prometheus)

Docker方式:

```bash
docker run --name influxdb -p 8086:8086 influxdb:1.8.4
```

docker-compose.yml

```yaml
version: '3'

services:

  influxdb:
    image: influxdb:1.8.4
    container_name: influxdb
    ports:
      - "8086:8086"
    environment:
      - INFLUXDB_DB=prometheus
      - INFLUXDB_ADMIN_ENABLED=true
      - INFLUXDB_ADMIN_USER=admin
      - INFLUXDB_ADMIN_PASSWORD=admin
      - INFLUXDB_USER=influxdb
      - INFLUXDB_USER_PASSWORD=influxdb

  chronograf:
    image: chronograf:1.8.8
    container_name: chronograf
    ports:
      - "8888:8888"
    environment:
      - INFLUXDB-URL=http://influxdb:8086

  prometheus:
    image: prom/prometheus:v2.24.1
    container_name: prometheus
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml:ro
      - ./alerts/memory_over.yml:/etc/prometheus/alerts/memory_over.yml:ro
      - ./alerts/server_down.yml:/etc/prometheus/alerts/server_down.yml:ro
    ports:
      - 9090:9090
    restart: always

  alertmanager:
    image: prom/alertmanager:v0.21.0
    container_name: alertmanager
    volumes:
      - ./alertmanager.yml:/etc/alertmanager/alertmanager.yml
    ports:
      - 9093:9093
    restart: always

  grafana:
    image: grafana/grafana
    container_name: grafana
    ports:
      - 3000:3000
    restart: always

networks:
  default:
    driver: bridge
```

在`prometheus.yml`配置文件中添加如下配置

```yam
remote_write:
  - url: "http://influxdb:8086/api/v1/prom/write?db=prometheus&u=influxdb&p=influxdb"

remote_read:
  - url: "http://influxdb:8086/api/v1/prom/read?db=prometheus&u=influxdb&p=influxdb"
```

查看influxdb中的数据

```bash
❯ docker exec -it influxdb /bin/bash
root@bd004204de23:/# influx
Connected to http://localhost:8086 version 1.8.4
InfluxDB shell version: 1.8.4
> help
Usage:
        connect <host:port>   connects to another node specified by host:port
        auth                  prompts for username and password
        pretty                toggles pretty print for the json format
        chunked               turns on chunked responses from server
        chunk size <size>     sets the size of the chunked responses.  Set to 0 to reset to the default chunked size
        use <db_name>         sets current database
        format <format>       specifies the format of the server responses: json, csv, or column
        precision <format>    specifies the format of the timestamp: rfc3339, h, m, s, ms, u or ns
        consistency <level>   sets write consistency level: any, one, quorum, or all
        history               displays command history
        settings              outputs the current settings for the shell
        clear                 clears settings such as database or retention policy.  run 'clear' for help
        exit/quit/ctrl+d      quits the influx shell

        show databases        show database names
        show series           show series information
        show measurements     show measurement information
        show tag keys         show tag key information
        show field keys       show field key information

        A full list of influxql commands can be found at:
        https://docs.influxdata.com/influxdb/latest/query_language/spec/
> show databases
name: databases
name
----
prometheus
_internal
> use prometheus
Using database prometheus
> show measurements
name: measurements
name
----
ALERTS
ALERTS_FOR_STATE
go_gc_duration_seconds
go_gc_duration_seconds_count
go_gc_duration_seconds_sum
go_goroutines
go_info
go_memstats_alloc_bytes
go_memstats_alloc_bytes_total
go_memstats_buck_hash_sys_bytes
go_memstats_frees_total
....
go_memstats_stack_sys_bytes
go_memstats_sys_bytes
go_threads
net_conntrack_dialer_conn_attempted_total
net_conntrack_dialer_conn_closed_total
net_conntrack_dialer_conn_established_total
net_conntrack_dialer_conn_failed_total
net_conntrack_listener_conn_accepted_total
net_conntrack_listener_conn_closed_total
node_arp_entries
node_boot_time_seconds
node_context_switches_total
node_cpu_guest_seconds_total
node_cpu_seconds_total
node_disk_io_now
node_disk_io_time_seconds_total
node_disk_io_time_weighted_seconds_total
node_disk_read_bytes_total
node_disk_read_errors_total
node_disk_read_retries_total
node_disk_read_sectors_total
....
node_filesystem_size_bytes
node_forks_total
node_intr_total
node_load1
node_load15
node_load5
node_memory_Active_anon_bytes
node_memory_Active_bytes
node_memory_Active_file_bytes
node_memory_AnonHugePages_bytes
node_memory_AnonPages_bytes
node_memory_Bounce_bytes
node_memory_Buffers_bytes
node_memory_Cached_bytes
node_memory_CmaFree_bytes
node_memory_CmaTotal_bytes
node_memory_CommitLimit_bytes
node_memory_Committed_AS_bytes
node_memory_DirectMap2M_bytes
node_memory_DirectMap4k_bytes
node_memory_Dirty_bytes
node_memory_HardwareCorrupted_bytes
node_memory_HugePages_Free
node_memory_HugePages_Rsvd
.....
node_network_address_assign_type
node_network_carrier
node_network_carrier_changes_total
node_network_device_id
node_network_dormant
node_network_flags
node_network_iface_id
node_network_iface_link
....
up
> select * from node_load1
name: node_load1
time                __name__   instance job   value
----                --------   -------- ---   -----
1612341680647000000 node_load1 node1    node1 2.44482421875
1612341683852000000 node_load1 node2    node2 0.77
1612341695647000000 node_load1 node1    node1 2.70947265625
1612341698852000000 node_load1 node2    node2 0.6
1612341710647000000 node_load1 node1    node1 2.47802734375
1612341713852000000 node_load1 node2    node2 0.47
1612341788852000000 node_load1 node2    node2 0.24
1612341800647000000 node_load1 node1    node1 2.6875
1612341803857000000 node_load1 node2    node2 0.19
1612342268852000000 node_load1 node2    node2 0.59
>

```

# Prometheus服务发现

### 基于文件的服务发现

prometheus.yml

```yaml
# my global config
global:
  scrape_interval:     15s # Set the scrape interval to every 15 seconds. Default is every 1 minute.
  evaluation_interval: 15s # Evaluate rules every 15 seconds. The default is every 1 minute.
  # scrape_timeout is set to the global default (10s).

remote_write:
  - url: "http://promscale-connector:9201/write"

remote_read:
  - url: "http://promscale-connector:9201/read"

# remote_write:
#   - url: "http://influxdb:8086/api/v1/prom/write?db=prometheus&u=influxdb&p=influxdb"

# remote_read:
#   - url: "http://influxdb:8086/api/v1/prom/read?db=prometheus&u=influxdb&p=influxdb"


# Alertmanager configuration
alerting:
  alertmanagers:
  - static_configs:
    - targets:
      - alertmanager:9093

# Load rules once and periodically evaluate them according to the global 'evaluation_interval'.
rule_files:
  # - "first_rules.yml"
  # - "second_rules.yml"
  - "alerts/*.yml"

# A scrape configuration containing exactly one endpoint to scrape:
# Here it's Prometheus itself.
scrape_configs:
  # The job name is added as a label `job=<job_name>` to any timeseries scraped from this config.
  - job_name: 'prometheus'

    # metrics_path defaults to '/metrics'
    # scheme defaults to 'http'.

    static_configs:
    - targets: ['prometheus:9090']

  # - job_name: 'node1'
  #   static_configs:
  #   - targets: ['172.24.107.47:9100']
  #     labels:
  #       instance: 'node1'

  # - job_name: 'node2'
  #   static_configs:
  #   - targets: ['192.168.223.2:9100']
  #     labels:
  #       instance: 'node2'

  - job_name: 'file_ds'
    file_sd_configs:
    - files:
      - 'targets.json'
      refresh_interval: 1m
```

targets.json

```json
[
  {
    "targets": [
      "172.24.107.47:9100"
    ],
    "labels": {
      "instance": "node1",
      "job": "node1"
    }
  },
  {
    "targets": [
      "192.168.223.2:9100"
    ],
    "labels": {
      "instance": "node2",
      "job": "node2"
    }
  },
  {
    "targets": [
      "192.168.223.6:9100"
    ],
    "labels": {
      "instance": "node3",
      "job": "node3"
    }
  }
]
```

```bash
curl -X POST 'http://localhost:9090/-/reload'
```

### 基于Consul的服务发现

#### 安装consul集群

docker-compose.yml

```yaml
version: '2'

networks:
  consul-net:

services:
  consul1:
    image: consul:latest
    container_name: node1
    command: agent -server -bootstrap-expect=3 -node=node1 -bind=0.0.0.0 -client=0.0.0.0 -datacenter=dc1
    networks:
      - consul-net

  consul2:
    image: consul:latest
    container_name: node2
    command: agent -server -retry-join=node1 -node=node2 -bind=0.0.0.0 -client=0.0.0.0 -datacenter=dc1
    depends_on:
      - consul1
    networks:
      - consul-net

  consul3:
    image: consul:latest
    container_name: node3
    command: agent -server -retry-join=node1 -node=node3 -bind=0.0.0.0 -client=0.0.0.0 -datacenter=dc1
    depends_on:
      - consul1
    networks:
      - consul-net

  consul4:
    image: consul:latest
    container_name: node4
    command: agent -retry-join=node1 -node=ndoe4 -bind=0.0.0.0 -client=0.0.0.0 -datacenter=dc1 -ui
    ports:
      - 8500:8500
    depends_on:
      - consul1
      - consul2
      - consul3
    networks:
      - consul-net
```

运行consul集群

```bash
docker-compose up -d
docker-compose ps -a
Name               Command               State                                              Ports
---------------------------------------------------------------------------------------------------------------------------------------------
node1   docker-entrypoint.sh agent ...   Up      8300/tcp, 8301/tcp, 8301/udp, 8302/tcp, 8302/udp, 8500/tcp, 8600/tcp, 8600/udp
node2   docker-entrypoint.sh agent ...   Up      8300/tcp, 8301/tcp, 8301/udp, 8302/tcp, 8302/udp, 8500/tcp, 8600/tcp, 8600/udp
node3   docker-entrypoint.sh agent ...   Up      8300/tcp, 8301/tcp, 8301/udp, 8302/tcp, 8302/udp, 8500/tcp, 8600/tcp, 8600/udp
node4   docker-entrypoint.sh agent ...   Up      8300/tcp, 8301/tcp, 8301/udp, 8302/tcp, 8302/udp, 0.0.0.0:8500->8500/tcp, 8600/tcp, 8600/udp
```

访问 Consul Web 管理页面

http://localhost:8500/ui/dc1/nodes

通过命令行查看集群状态、以及集群成员状态

```bash
❯ docker exec -it node4 /bin/sh
# 查看集群状态
/ # consul operator raft list-peers
Node   ID                                    Address          State     Voter  RaftProtocol
node1  a29c1227-9437-1c2d-6d4f-e151bf087530  172.20.0.2:8300  leader    true   3
node2  af6c684a-536e-9007-cd2a-2f41b182ff72  172.20.0.3:8300  follower  true   3
node3  b9c04219-dc36-4e12-71bc-de3e2eb808ec  172.20.0.4:8300  follower  true   3
# 查看集群成员状态
/ # consul members
Node   Address          Status  Type    Build  Protocol  DC   Segment
node1  172.20.0.2:8301  alive   server  1.8.4  2         dc1  <all>
node2  172.20.0.3:8301  alive   server  1.8.4  2         dc1  <all>
node3  172.20.0.4:8301  alive   server  1.8.4  2         dc1  <all>
ndoe4  172.20.0.5:8301  alive   client  1.8.4  2         dc1  <default>
```

#### 向consul集群中注册节点

新建`node.json`

```json
{
  "ID": "node3",
  "Name": "node3-192.168.223.5",
  "Tags": [
    "node3"
  ],
  "Address": "192.168.223.5",
  "Port": 9100,
  "Meta": {
    "app": "node3",
    "team": "anchnet",
    "project": "smartops"
  },
  "EnableTagOverride": false,
  "Check": {
    "HTTP": "http://192.168.223.5:9100/metrics",
    "Interval": "10s"
  },
  "Weights": {
    "Passing": 10,
    "Warning": 1
  }
}
```

调用 API 注册服务

```bash
curl --request PUT --data @node.json http://127.0.0.1:8500/v1/agent/service/register?replace-existing-checks=1
```

注册完毕，通过 Consul Web 管理页面可以查看到该服务已注册成功。注意：这里需要启动 node-exporter 否则即使注册成功了，健康检测也不通过。

从consul集群中注销掉某个服务，可以通过如下 API 命令操作，例如注销上边添加的`node3` 服务

```bash
$ curl -X PUT http://127.0.0.1:8500/v1/agent/service/deregister/node3
```

#### 配置 Prometheus 实现自动服务发现

现在 Consul 服务已经启动完毕，并成功注册了一个服务，接下来，我们需要配置 Prometheus 来使用 Consul 自动服务发现，目的就是能够将上边添加的服务自动发现到 Prometheus 的 Targets 中，增加 `prometheus.yml` 配置如下：

```yaml
...
  - job_name: 'consul-sd'
    consul_sd_configs:
    - server: '172.24.107.38:8500'
      services: []
```

使用 `consul_sd_configs` 来配置使用 Consul 服务发现类型，`server` 为 Consul 的服务地址，这里跟上边要对应上。 配置完毕后，重启 Prometheus 服务，此时可以通过 Prometheus UI 页面的 Targets 下查看是否配置成功。

可以看到，在 Targets 中能够成功的自动发现 Consul 中的 Services 信息，后期需要添加新的 Targets 时，只需要通过 API 往 Consul 中注册服务即可，Prometheus 就能自动发现该服务，是不是很方便。

不过，我们会发现有如下几个问题：

1. 会发现 Prometheus 同时加载出来了默认服务 consul，这个是不需要的。
2. 默认只显示 job 及 instance 两个标签，其他标签都默认属于 `before relabeling` 下，有些必要的服务信息，也想要在标签中展示，该如何操作呢？
3. 如果需要自定义一些标签，例如 team、group、project 等关键分组信息，方便后边 alertmanager 进行告警规则匹配，该如何处理呢？
4. 所有 Consul 中注册的 Service 都会默认加载到 Prometheus 下配置的 `consul_prometheus` 组，如果有多种类型的 exporter，如何在 Prometheus 中配置分配给指定类型的组，方便直观的区别它们？

以上问题，我们可以通过 Prometheus 配置中的 `relabel_configs` 参数来解决。

#### 配置 relabel_configs 实现自定义标签及分类

Prometheus 允许用户在采集任务设置中，通过 `relabel_configs` 来添加自定义的 Relabeling 的额过程，来对标签进行指定规则的重写。 Prometheus 加载 Targets 后，这些 Targets 会自动包含一些默认的标签，Target 以 `__` 作为前置的标签是在系统内部使用的，这些标签不会被写入到样本数据中。眼尖的会发现，每次增加 Target 时会自动增加一个 instance 标签，而 instance 标签的内容刚好对应 Target 实例的 `__address__` 值，这是因为实际上 Prometheus 内部做了一次标签重写处理，默认 `__address__` 标签设置为 `<host>:<port>` 地址，经过标签重写后，默认会自动将该值设置为 `instance` 标签，所以我们能够在页面看到该标签。
详细 `relabel_configs` 配置及说明可以参考 [relabel_config](https://prometheus.io/docs/prometheus/latest/configuration/configuration/#relabel_config) 官网说明，这里我简单列举一下里面每个 `relabel_action` 的作用，方便下边演示。

- **replace**: 根据 regex 的配置匹配 `source_labels` 标签的值（注意：多个 `source_label` 的值会按照 separator 进行拼接），并且将匹配到的值写入到 `target_label` 当中，如果有多个匹配组，则可以使用 ${1}, ${2} 确定写入的内容。如果没匹配到任何内容则不对 `target_label` 进行重新， 默认为 replace。
- **keep**: 丢弃 `source_labels` 的值中没有匹配到 regex 正则表达式内容的 Target 实例
- **drop**: 丢弃 `source_labels` 的值中匹配到 regex 正则表达式内容的 Target 实例
- **hashmod**: 将 `target_label` 设置为关联的 `source_label` 的哈希模块
- **labelmap**: 根据 regex 去匹配 Target 实例所有标签的名称（注意是名称），并且将捕获到的内容作为为新的标签名称，regex 匹配到标签的的值作为新标签的值
- **labeldrop**: 对 Target 标签进行过滤，会移除匹配过滤条件的所有标签
- **labelkeep**: 对 Target 标签进行过滤，会移除不匹配过滤条件的所有标签

接下来，我们来挨个处理上述问题。

问题一，我们可以配置 `relabel_configs` 来实现标签过滤，只加载符合规则的服务。以上边为例，可以通过过滤 `__meta_consul_tags` 标签为 `test` 的服务，`relabel_config` 向 Consul 注册服务的时候，只加载匹配 regex 表达式的标签的服务到自己的配置文件。修改 `prometheus.yml` 配置如下：

```yaml
...
- job_name: 'consul-sd'
    consul_sd_configs:
    - server: '172.24.107.38:8500'
      services: []
    relabel_configs:
      - source_labels: [__meta_consul_tags]
        regex: .*node.*
        action: keep
      - regex: __meta_consul_service_metadata_(.+)
        action: labelmap
```

解释下，这里的 `relabel_configs` 配置作用为丢弃源标签中 `__meta_consul_tags` 不包含 `node` 标签的服务，`__meta_consul_tags` 对应到 Consul 服务中的值为 `"tags": ["node3"]`，默认 consul 服务是不带该标签的，从而实现过滤。重启 Prometheus 可以看到现在只获取了 `node3-192.168.223.5` 这个服务了。

完整的`prometheus.yml` 配置如下：

```yaml
# my global config
global:
  scrape_interval:     15s # Set the scrape interval to every 15 seconds. Default is every 1 minute.
  evaluation_interval: 15s # Evaluate rules every 15 seconds. The default is every 1 minute.
  # scrape_timeout is set to the global default (10s).

# remote_write:
#   - url: "http://promscale-connector:9201/write"

# remote_read:
#   - url: "http://promscale-connector:9201/read"

remote_write:
  - url: "http://influxdb:8086/api/v1/prom/write?db=prometheus&u=influxdb&p=influxdb"

remote_read:
  - url: "http://influxdb:8086/api/v1/prom/read?db=prometheus&u=influxdb&p=influxdb"

# Alertmanager configuration
alerting:
  alertmanagers:
  - static_configs:
    - targets:
      - alertmanager:9093

# Load rules once and periodically evaluate them according to the global 'evaluation_interval'.
rule_files:
  # - "first_rules.yml"
  # - "second_rules.yml"
  - "alerts/*.yml"

# A scrape configuration containing exactly one endpoint to scrape:
# Here it's Prometheus itself.
scrape_configs:
  # The job name is added as a label `job=<job_name>` to any timeseries scraped from this config.
  - job_name: 'prometheus'

    # metrics_path defaults to '/metrics'
    # scheme defaults to 'http'.

    static_configs:
    - targets: ['prometheus:9090']

  # - job_name: 'node1'
  #   static_configs:
  #   - targets: ['172.24.107.47:9100']
  #     labels:
  #       instance: 'node1'

  # - job_name: 'node2'
  #   static_configs:
  #   - targets: ['192.168.223.2:9100']
  #     labels:
  #       instance: 'node2'

  - job_name: 'file_ds'
    file_sd_configs:
    - files:
      - 'targets.json'
      refresh_interval: 1m

  - job_name: 'consul-sd'
    consul_sd_configs:
    - server: '172.24.107.38:8500'
      services: []
    relabel_configs:
      - source_labels: [__meta_consul_tags]
        regex: .*node.*
        action: keep
      - regex: __meta_consul_service_metadata_(.+)
        action: labelmap
```

配置完毕后，重新加载 Prometheus 服务，此时可以通过 Prometheus UI 页面的 Targets 下查看是否配置成功。

```bash
curl -X POST 'http://localhost:9090/-/reload'
```

# PromQL 常用查询语句

收集到 node_exporter 的数据后，我们可以使用 PromQL 进行一些业务查询和监控，下面是一些比较常见的查询。

注意：以下查询均以单个节点作为例子，如果大家想查看所有节点，将 `instance="xxx"` 去掉即可。

## 系统正常运行的时间

```
node_time_seconds{instance=~"node1",job=~"node1"} - node_boot_time_seconds{instance=~"node1",job=~"node1"}
```

node_time_seconds 当前系统时间

node_boot_time_seconds 系统启动时间

## CPU物理核数

```
count(count(node_cpu_seconds_total{}) by (cpu))
```

## CPU 使用率

```
100 - (avg by(instance) (irate(node_cpu_seconds_total{instance="node1", mode="idle"}[5m])) * 100)
```

## CPU 各 mode 占比率

```
avg by (instance, mode) (irate(node_cpu_seconds_total{instance="node1"}[5m])) * 100
```

## 机器平均负载

```
node_load1{instance="node1"} // 1分钟负载
node_load5{instance="node1"} // 5分钟负载
node_load15{instance="node1"} // 15分钟负载
```

## 内存使用率

node_memory_MemTotal_bytes：主机上的总内存
node_memory_MemFree_bytes：主机上的可用内存
node_memory_Buffers_bytes：缓冲缓存中的内存
node_memory_Cached_bytes：页面缓存中的内存

(总内存-(可用内存+缓冲缓存中的内存+页面缓存中的内存))÷总内存×100

```
(node_memory_MemTotal_bytes{instance="node2"} - (node_memory_MemFree_bytes{instance="node2"} + node_memory_Cached_bytes{instance="node2"} + node_memory_Buffers_bytes{instance="node2"})) / node_memory_MemTotal_bytes{instance="node2"} * 100
```

## 内存大小

```
node_memory_total_bytes{instance=~"node1",job=~"node1"}
```

## 交换分区大小

```
node_memory_swap_total_bytes{instance=~"node1",job=~"node1"}
```

## 磁盘总大小

```
node_filesystem_size_bytes{instance=~"node1",job=~"node1",mountpoint="/",fstype!="rootfs"}
```

## 磁盘使用率

```
100 - node_filesystem_free_bytes{instance="node1",fstype!~"rootfs|selinuxfs|autofs|rpc_pipefs|tmpfs|udev|none|devpts|sysfs|debugfs|fuse.*"} / node_filesystem_size_bytes{instance="node1",fstype!~"rootfs|selinuxfs|autofs|rpc_pipefs|tmpfs|udev|none|devpts|sysfs|debugfs|fuse.*"} * 100
```

或者你也可以直接使用 {fstype="xxx"} 来指定想查看的磁盘信息

## 网络 IO

```
// 上行带宽
sum by (instance) (irate(node_network_receive_bytes_total{instance="node1",device!~"bond.*?|lo"}[5m])/128)

// 下行带宽
sum by (instance) (irate(node_network_transmit_bytes_total{instance="node1",device!~"bond.*?|lo"}[5m])/128)
```

## 网卡出/入包

```
// 入包量
sum by (instance) (rate(node_network_receive_bytes_total{instance="node1",device!="lo"}[5m]))

// 出包量
sum by (instance) (rate(node_network_transmit_bytes_total{instance="node1",device!="lo"}[5m]))
```



# HTTP API中使用PromQL

## API响应格式

Prometheus API使用了JSON格式的响应内容。 当API调用成功后将会返回2xx的HTTP状态码。

反之，当API调用失败时可能返回以下几种不同的HTTP状态码：

- 404 Bad Request：当参数错误或者缺失时。
- 422 Unprocessable Entity 当表达式无法执行时。
- 503 Service Unavailiable 当请求超时或者被中断时。

所有的API请求均使用以下的JSON格式：

```json
{
  "status": "success" | "error",
  "data": <data>,

  // Only set if status is "error". The data field may still hold
  // additional data.
  "errorType": "<string>",
  "error": "<string>"
}
```

## 在HTTP API中使用PromQL

通过HTTP API我们可以分别通过`/api/v1/query`和`/api/v1/query_range`查询`PromQL`表达式当前或者一定时间范围内的计算结果。

### 瞬时数据查询

通过使用QUERY API我们可以查询PromQL在特定时间点下的计算结果。

```
GET /api/v1/query
```

URL请求参数：

- query=：PromQL表达式。
- time=<rfc3339 | unix_timestamp>：用于指定用于计算PromQL的时间戳。可选参数，默认情况下使用当前系统时间。
- timeout=：超时设置。可选参数，默认情况下使用-query,timeout的全局设置。

例如使用以下表达式查询表达式up在时间点2015-07-01T20:10:51.781Z的计算结果：

```
$ curl 'http://localhost:9090/api/v1/query?query=up&time=2015-07-01T20:10:51.781Z'
{
   "status" : "success",
   "data" : {
      "resultType" : "vector",
      "result" : [
         {
            "metric" : {
               "__name__" : "up",
               "job" : "prometheus",
               "instance" : "localhost:9090"
            },
            "value": [ 1435781451.781, "1" ]
         },
         {
            "metric" : {
               "__name__" : "up",
               "job" : "node",
               "instance" : "localhost:9100"
            },
            "value" : [ 1435781451.781, "0" ]
         }
      ]
   }
}
```

### 响应数据类型

当API调用成功后，Prometheus会返回JSON格式的响应内容，格式如上小节所示。并且在data节点中返回查询结果。data节点格式如下：

```
{
  "resultType": "matrix" | "vector" | "scalar" | "string",
  "result": <value>
}
```

PromQL表达式可能返回多种数据类型，在响应内容中使用resultType表示当前返回的数据类型，包括：

- 瞬时向量：vector

当返回数据类型resultType为vector时，result响应格式如下：

```
[
  {
    "metric": { "<label_name>": "<label_value>", ... },
    "value": [ <unix_time>, "<sample_value>" ]
  },
  ...
]
```

其中metrics表示当前时间序列的特征维度，value只包含一个唯一的样本。

- 区间向量：matrix

当返回数据类型resultType为matrix时，result响应格式如下：

```
[
  {
    "metric": { "<label_name>": "<label_value>", ... },
    "values": [ [ <unix_time>, "<sample_value>" ], ... ]
  },
  ...
]
```

其中metrics表示当前时间序列的特征维度，values包含当前事件序列的一组样本。

- 标量：scalar

当返回数据类型resultType为scalar时，result响应格式如下：

```
[ <unix_time>, "<scalar_value>" ]
```

由于标量不存在时间序列一说，因此result表示为当前系统时间一个标量的值。

- 字符串：string

当返回数据类型resultType为string时，result响应格式如下：

```
[ <unix_time>, "<string_value>" ]
```

字符串类型的响应内容格式和标量相同。

### 区间数据查询

使用QUERY_RANGE API我们则可以直接查询PromQL表达式在一段时间返回内的计算结果。

```
GET /api/v1/query_range
```

URL请求参数：

- query=: PromQL表达式。
- start=<rfc3339 | unix_timestamp>: 起始时间。
- end=<rfc3339 | unix_timestamp>: 结束时间。
- step=: 查询步长。
- timeout=: 超时设置。可选参数，默认情况下使用-query,timeout的全局设置。

当使用QUERY_RANGE API查询PromQL表达式时，返回结果一定是一个区间向量：

```
{
  "resultType": "matrix",
  "result": <value>
}
```

> 需要注意的是，在QUERY_RANGE API中PromQL只能使用瞬时向量选择器类型的表达式。

例如使用以下表达式查询表达式up在30秒范围内以15秒为间隔计算PromQL表达式的结果。

```json
$ curl 'http://localhost:9090/api/v1/query_range?query=up&start=2015-07-01T20:10:30.781Z&end=2015-07-01T20:11:00.781Z&step=15s'
{
   "status" : "success",
   "data" : {
      "resultType" : "matrix",
      "result" : [
         {
            "metric" : {
               "__name__" : "up",
               "job" : "prometheus",
               "instance" : "localhost:9090"
            },
            "values" : [
               [ 1435781430.781, "1" ],
               [ 1435781445.781, "1" ],
               [ 1435781460.781, "1" ]
            ]
         },
         {
            "metric" : {
               "__name__" : "up",
               "job" : "node",
               "instance" : "localhost:9091"
            },
            "values" : [
               [ 1435781430.781, "0" ],
               [ 1435781445.781, "0" ],
               [ 1435781460.781, "1" ]
            ]
         }
      ]
   }
}
```

### 编码查询PromQL

如下示例展示了查询指定时间范围内`node_load15`的指标数据

```go
package main

import (
	"encoding/base64"
	"github.com/go-resty/resty/v2"
)

func main() {
	response, err := resty.New().R().Get("http://localhost:9090/api/v1/query_range?query=node_load15&start=1612487600.024&end=1612489400.024&step=14")
	if err != nil {
		panic(err)
	}
	println(response.String())
}
```

程序运行后返回：

```json
{
  "status": "success",
  "data": {
    "resultType": "matrix",
    "result": [
      {
        "metric": {
          "__name__": "node_load15",
          "instance": "node2",
          "job": "node2"
        },
        "values": [
          [
            1612488496.024,
            "0.19"
          ],
          [
            1612488510.024,
            "0.19"
          ],
          [
            1612488524.024,
            "0.18"
          ],
          [
            1612488538.024,
            "0.18"
          ],
          [
            1612488552.024,
            "0.18"
          ],
          [
            1612488566.024,
            "0.18"
          ],
          [
            1612488580.024,
            "0.18"
          ],
          [
            1612488594.024,
            "0.18"
          ],
          [
            1612488608.024,
            "0.18"
          ],
          [
            1612488622.024,
            "0.18"
          ],
          [
            1612488636.024,
            "0.17"
          ],
          [
            1612488650.024,
            "0.17"
          ],
          [
            1612488664.024,
            "0.18"
          ],
          [
            1612488678.024,
            "0.18"
          ],
          [
            1612488692.024,
            "0.18"
          ],
          [
            1612488706.024,
            "0.18"
          ],
          [
            1612488720.024,
            "0.18"
          ],
          [
            1612488734.024,
            "0.17"
          ],
          [
            1612488748.024,
            "0.17"
          ],
          [
            1612488762.024,
            "0.17"
          ],
          [
            1612488776.024,
            "0.17"
          ],
          [
            1612488790.024,
            "0.16"
          ],
          [
            1612488804.024,
            "0.16"
          ],
          [
            1612488818.024,
            "0.16"
          ],
          [
            1612488832.024,
            "0.16"
          ],
          [
            1612488846.024,
            "0.17"
          ],
          [
            1612488860.024,
            "0.18"
          ],
          [
            1612488874.024,
            "0.2"
          ],
          [
            1612488888.024,
            "0.21"
          ],
          [
            1612488902.024,
            "0.2"
          ],
          [
            1612488916.024,
            "0.2"
          ],
          [
            1612488930.024,
            "0.21"
          ],
          [
            1612488944.024,
            "0.21"
          ],
          [
            1612488958.024,
            "0.21"
          ],
          [
            1612488972.024,
            "0.21"
          ],
          [
            1612488986.024,
            "0.2"
          ],
          [
            1612489000.024,
            "0.2"
          ],
          [
            1612489014.024,
            "0.2"
          ],
          [
            1612489028.024,
            "0.2"
          ],
          [
            1612489042.024,
            "0.2"
          ],
          [
            1612489056.024,
            "0.2"
          ],
          [
            1612489070.024,
            "0.19"
          ],
          [
            1612489084.024,
            "0.2"
          ],
          [
            1612489098.024,
            "0.2"
          ],
          [
            1612489112.024,
            "0.2"
          ],
          [
            1612489126.024,
            "0.21"
          ],
          [
            1612489140.024,
            "0.2"
          ],
          [
            1612489154.024,
            "0.24"
          ],
          [
            1612489168.024,
            "0.24"
          ],
          [
            1612489182.024,
            "0.29"
          ],
          [
            1612489196.024,
            "0.28"
          ],
          [
            1612489210.024,
            "0.29"
          ],
          [
            1612489224.024,
            "0.28"
          ],
          [
            1612489238.024,
            "0.27"
          ],
          [
            1612489252.024,
            "0.27"
          ],
          [
            1612489266.024,
            "0.27"
          ],
          [
            1612489280.024,
            "0.28"
          ],
          [
            1612489294.024,
            "0.28"
          ],
          [
            1612489308.024,
            "0.27"
          ],
          [
            1612489322.024,
            "0.27"
          ],
          [
            1612489336.024,
            "0.3"
          ],
          [
            1612489350.024,
            "0.3"
          ],
          [
            1612489364.024,
            "0.3"
          ],
          [
            1612489378.024,
            "0.3"
          ],
          [
            1612489392.024,
            "0.3"
          ]
        ]
      }
    ]
  }
}
```

将该数据结构转化为对应图表所需的格式即可。

# 附件

### prometheus.yml

```yaml
# my global config
global:
  scrape_interval:     15s # Set the scrape interval to every 15 seconds. Default is every 1 minute.
  evaluation_interval: 15s # Evaluate rules every 15 seconds. The default is every 1 minute.
  # scrape_timeout is set to the global default (10s).

# remote_write:
#   - url: "http://promscale-connector:9201/write"

# remote_read:
#   - url: "http://promscale-connector:9201/read"

remote_write:
  - url: "http://influxdb:8086/api/v1/prom/write?db=prometheus&u=influxdb&p=influxdb"

remote_read:
  - url: "http://influxdb:8086/api/v1/prom/read?db=prometheus&u=influxdb&p=influxdb"

# Alertmanager configuration
alerting:
  alertmanagers:
  - static_configs:
    - targets:
      - alertmanager:9093

# Load rules once and periodically evaluate them according to the global 'evaluation_interval'.
rule_files:
  # - "first_rules.yml"
  # - "second_rules.yml"
  - "alerts/*.yml"

# A scrape configuration containing exactly one endpoint to scrape:
# Here it's Prometheus itself.
scrape_configs:
  # The job name is added as a label `job=<job_name>` to any timeseries scraped from this config.
  - job_name: 'prometheus'

    # metrics_path defaults to '/metrics'
    # scheme defaults to 'http'.

    static_configs:
    - targets: ['prometheus:9090']

  # - job_name: 'node1'
  #   static_configs:
  #   - targets: ['172.24.107.47:9100']
  #     labels:
  #       instance: 'node1'

  # - job_name: 'node2'
  #   static_configs:
  #   - targets: ['192.168.223.2:9100']
  #     labels:
  #       instance: 'node2'

  - job_name: 'file_ds'
    file_sd_configs:
    - files:
      - 'targets.json'
      refresh_interval: 1m
```

校验:

```bash
./promtool check config prometheus.yml
```

### targets.json

```json
[
  {
    "targets": [
      "172.24.107.44:9100"
    ],
    "labels": {
      "instance": "node1",
      "job": "node1"
    }
  },
  {
    "targets": [
      "192.168.223.2:9100"
    ],
    "labels": {
      "instance": "node2",
      "job": "node2"
    }
  },
  {
    "targets": [
      "192.168.223.6:9100"
    ],
    "labels": {
      "instance": "node3",
      "job": "node3"
    }
  }
]
```

### alerts/memory_over.yml

```yaml
groups:
  - name: memory_over
    rules:
      - alert: NodeMemoryUsage
        expr: (node_memory_MemTotal_bytes - (node_memory_MemFree_bytes+node_memory_Buffers_bytes+node_memory_Cached_bytes )) / node_memory_MemTotal_bytes * 100 > 80
        for: 20s
        labels:
          user: swfeng
        annotations:
          summary: "{{$labels.instance}}: High Memory usage detected"
          description: "{{$labels.instance}}: Memory usage is above 80% (current value is:{{ $value }})"
```

校验

```bash
./promtool check rules alerts/memory_over.yml
```

### alerts/server_down.yml

```yaml
groups:
  - name: server_down
    rules:
      - alert: InstanceDown
        expr: up == 0
        for: 20s
        labels:
          user: swfeng
        annotations:
          summary: "Instance {{ $labels.instance }} down"
          description: "{{ $labels.instance }} of job {{ $labels.job }} has been down for more than 20 s."
```

校验

```bash
./promtool check rules alerts/server_down.yml
```

### alerts/alert-rules.yml

```yaml
groups:
- name: monitor_base
  rules:
  - alert: CpuUsageAlert_waring
    expr: sum(avg(irate(node_cpu_seconds_total{mode!='idle'}[5m])) without (cpu)) by (instance) > 0.60
    for: 2m
    labels:
      level: warning
    annotations:
      summary: "Instance {{ $labels.instance }} CPU usage high"
      description: "{{ $labels.instance }} CPU usage above 60% (current value: {{ $value }})"
  - alert: CpuUsageAlert_serious
    #expr: sum(avg(irate(node_cpu_seconds_total{mode!='idle'}[5m])) without (cpu)) by (instance) > 0.85
    expr: (100 - (avg by (instance) (irate(node_cpu_seconds_total{job=~".*",mode="idle"}[5m])) * 100)) > 85
    for: 3m
    labels:
      level: serious
    annotations:
      summary: "Instance {{ $labels.instance }} CPU usage high"
      description: "{{ $labels.instance }} CPU usage above 85% (current value: {{ $value }})"
  - alert: MemUsageAlert_waring
    expr: avg by(instance) ((1 - (node_memory_MemFree_bytes + node_memory_Buffers_bytes + node_memory_Cached_bytes) / node_memory_MemTotal_bytes) * 100) > 70
    for: 2m
    labels:
      level: warning
    annotations:
      summary: "Instance {{ $labels.instance }} MEM usage high"
      description: "{{$labels.instance}}: MEM usage is above 70% (current value is: {{ $value }})"
  - alert: MemUsageAlert_serious
    expr: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes)/node_memory_MemTotal_bytes > 0.90
    for: 3m
    labels:
      level: serious
    annotations:
      summary: "Instance {{ $labels.instance }} MEM usage high"
      description: "{{ $labels.instance }} MEM usage above 90% (current value: {{ $value }})"
  - alert: DiskUsageAlert_warning
    expr: (1 - node_filesystem_free_bytes{fstype!="rootfs",mountpoint!="",mountpoint!~"/(run|var|sys|dev).*"} / node_filesystem_size_bytes) * 100 > 80
    for: 2m
    labels:
      level: warning
    annotations:
      summary: "Instance {{ $labels.instance }} Disk usage high"
      description: "{{$labels.instance}}: Disk usage is above 80% (current value is: {{ $value }})"
  - alert: DiskUsageAlert_serious
    expr: (1 - node_filesystem_free_bytes{fstype!="rootfs",mountpoint!="",mountpoint!~"/(run|var|sys|dev).*"} / node_filesystem_size_bytes) * 100 > 90
    for: 3m
    labels:
      level: serious
    annotations:
      summary: "Instance {{ $labels.instance }} Disk usage high"
      description: "{{$labels.instance}}: Disk usage is above 90% (current value is: {{ $value }})"
  - alert: NodeFileDescriptorUsage
    expr: avg by (instance) (node_filefd_allocated{} / node_filefd_maximum{}) * 100 > 60
    for: 2m
    labels:
      level: warning
    annotations:
      summary: "Instance {{ $labels.instance }} File Descriptor usage high"
      description: "{{$labels.instance}}: File Descriptor usage is above 60% (current value is: {{ $value }})"
  - alert: NodeLoad15
    expr: avg by (instance) (node_load15{}) > 80
    for: 2m
    labels:
      level: warning
    annotations:
      summary: "Instance {{ $labels.instance }} Load15 usage high"
      description: "{{$labels.instance}}: Load15 is above 80 (current value is: {{ $value }})"
  - alert: NodeAgentStatus
    expr: avg by (instance) (up{}) == 0
    for: 2m
    labels:
      level: warning
    annotations:
      summary: "{{$labels.instance}}: has been down"
      description: "{{$labels.instance}}: Node_Exporter Agent is down (current value is: {{ $value }})"
  - alert: NodeProcsBlocked
    expr: avg by (instance) (node_procs_blocked{}) > 10
    for: 2m
    labels:
      level: warning
    annotations:
      summary: "Instance {{ $labels.instance }}  Process Blocked usage high"
      description: "{{$labels.instance}}: Node Blocked Procs detected! above 10 (current value is: {{ $value }})"
  - alert: NetworkTransmitRate
    #expr:  avg by (instance) (floor(irate(node_network_transmit_bytes_total{device="ens192"}[2m]) / 1024 / 1024)) > 50
    expr:  avg by (instance) (floor(irate(node_network_transmit_bytes_total{}[2m]) / 1024 / 1024 * 8 )) > 40
    for: 1m
    labels:
      level: warning
    annotations:
      summary: "Instance {{ $labels.instance }} Network Transmit Rate usage high"
      description: "{{$labels.instance}}: Node Transmit Rate (Upload) is above 40Mbps/s (current value is: {{ $value }}Mbps/s)"
  - alert: NetworkReceiveRate
    #expr:  avg by (instance) (floor(irate(node_network_receive_bytes_total{device="ens192"}[2m]) / 1024 / 1024)) > 50
    expr:  avg by (instance) (floor(irate(node_network_receive_bytes_total{}[2m]) / 1024 / 1024 * 8 )) > 40
    for: 1m
    labels:
      level: warning
    annotations:
      summary: "Instance {{ $labels.instance }} Network Receive Rate usage high"
      description: "{{$labels.instance}}: Node Receive Rate (Download) is above 40Mbps/s (current value is: {{ $value }}Mbps/s)"
  - alert: DiskReadRate
    expr: avg by (instance) (floor(irate(node_disk_read_bytes_total{}[2m]) / 1024 )) > 200
    for: 2m
    labels:
      level: warning
    annotations:
      summary: "Instance {{ $labels.instance }} Disk Read Rate usage high"
      description: "{{$labels.instance}}: Node Disk Read Rate is above 200KB/s (current value is: {{ $value }}KB/s)"
  - alert: DiskWriteRate
    expr: avg by (instance) (floor(irate(node_disk_written_bytes_total{}[2m]) / 1024 / 1024 )) > 20
    for: 2m
    labels:
      level: warning
    annotations:
      summary: "Instance {{ $labels.instance }} Disk Write Rate usage high"
      description: "{{$labels.instance}}: Node Disk Write Rate is above 20MB/s (current value is: {{ $value }}MB/s)"
```

### alerts/node_status.yml

```yaml
groups:
- name: 实例存活告警规则
  rules:
  - alert: 实例存活告警
    expr: up == 0
    for: 20s
    labels:
      user: prometheus
      severity: Disaster
    annotations:
      summary: "Instance {{ $labels.instance }} is down"
      description: "Instance {{ $labels.instance }} of job {{ $labels.job }} has been down for more than 20s."
      value: "{{ $value }}"

- name: 内存告警规则
  rules:
  - alert: "内存使用率告警"
    expr: (node_memory_MemTotal_bytes - (node_memory_MemFree_bytes+node_memory_Buffers_bytes+node_memory_Cached_bytes )) / node_memory_MemTotal_bytes * 100 > 75
    for: 1m
    labels:
      user: prometheus
      severity: warning
    annotations:
      summary: "服务器: {{$labels.alertname}} 内存报警"
      description: "{{ $labels.alertname }} 内存资源利用率大于75%！(当前值: {{ $value }}%)"
      value: "{{ $value }}"

- name: CPU报警规则
  rules:
  - alert: CPU使用率告警
    expr: 100 - (avg by (instance)(irate(node_cpu_seconds_total{mode="idle"}[1m]) )) * 100 > 70
    for: 1m
    labels:
      user: prometheus
      severity: warning
    annotations:
      summary: "服务器: {{$labels.alertname}} CPU报警"
      description: "服务器: CPU使用超过70%！(当前值: {{ $value }}%)"
      value: "{{ $value }}"

- name: 磁盘报警规则
  rules:
  - alert: 磁盘使用率告警
    expr: (node_filesystem_size_bytes - node_filesystem_avail_bytes) / node_filesystem_size_bytes * 100 > 80
    for: 1m
    labels:
      user: prometheus
      severity: warning
    annotations:
      summary: "服务器: {{$labels.alertname}} 磁盘报警"
      description: "服务器:{{$labels.alertname}},磁盘设备: 使用超过80%！(挂载点: {{ $labels.mountpoint }} 当前值: {{ $value }}%)"
      value: "{{ $value }}"
```

### alertmanager.yml

```yaml
global:
  resolve_timeout: 5m
  smtp_smarthost: 'smtp.exmail.qq.com:465'
  smtp_from: 'fengj@anchnet.com'
  smtp_auth_username: 'fengj@anchnet.com'
  smtp_auth_password: '********'
  smtp_require_tls: false

templates:
  - '/etc/alertmanager/templates/*.tmpl'

route:
  group_by: ['alertname']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 30m
  receiver: 'web.hook'

receivers:

- name: 'mail-receiver'
  email_configs:
    - to: 'fengj@anchnet.com'
      html: '{{ template "email.to.html" . }}'
      headers: { Subject: "Prometheus 告警邮件" }
      send_resolved: true

- name: 'web.hook'
  webhook_configs:
  - url: 'http://172.24.107.44:5001/'

inhibit_rules:
  - source_match:
      severity: 'critical'
    target_match:
      severity: 'warning'
    equal: ['alertname', 'dev', 'instance']
```

校验

```bash
./amtool check-config alertmanager.yml
```

### templates/email.tmpl

```
{{ define "email.to.html" }}
{{- if gt (len .Alerts.Firing) 0 -}}
{{ range .Alerts }}
=========start==========<br>
告警程序: prometheus_alert <br>
告警级别: {{ .Labels.severity }} <br>
告警类型: {{ .Labels.alertname }} <br>
告警主机: {{ .Labels.instance }} <br>
告警主题: {{ .Annotations.summary }}  <br>
告警详情: {{ .Annotations.description }} <br>
触发时间: {{ .StartsAt.Format "2006-01-02 15:04:05" }} <br>
=========end==========<br>
{{ end }}{{ end -}}
 
{{- if gt (len .Alerts.Resolved) 0 -}}
{{ range .Alerts }}
=========start==========<br>
告警程序: prometheus_alert <br>
告警级别: {{ .Labels.severity }} <br>
告警类型: {{ .Labels.alertname }} <br>
告警主机: {{ .Labels.instance }} <br>
告警主题: {{ .Annotations.summary }} <br>
告警详情: {{ .Annotations.description }} <br>
触发时间: {{ .StartsAt.Format "2006-01-02 15:04:05" }} <br>
恢复时间: {{ .EndsAt.Format "2006-01-02 15:04:05" }} <br>
=========end==========<br>
{{ end }}{{ end -}}
 
{{- end }}
```

### templates/wechat.tml

```
{{ define "wechat.default.message" }}
{{- if gt (len .Alerts.Firing) 0 -}}
{{- range $index, $alert := .Alerts -}}
{{- if eq $index 0 }}
========= 监控报警 =========
告警状态：{{   .Status }}
告警级别：{{ .Labels.severity }}
告警类型：{{ $alert.Labels.alertname }}
故障主机: {{ $alert.Labels.instance }}
告警主题: {{ $alert.Annotations.summary }}
告警详情: {{ $alert.Annotations.message }}{{ $alert.Annotations.description}};
触发阀值：{{ .Annotations.value }}
故障时间: {{ ($alert.StartsAt.Add 28800e9).Format "2006-01-02 15:04:05" }}
========= = end =  =========
{{- end }}
{{- end }}
{{- end }}
{{- if gt (len .Alerts.Resolved) 0 -}}
{{- range $index, $alert := .Alerts -}}
{{- if eq $index 0 }}
========= 异常恢复 =========
告警类型：{{ .Labels.alertname }}
告警状态：{{   .Status }}
告警主题: {{ $alert.Annotations.summary }}
告警详情: {{ $alert.Annotations.message }}{{ $alert.Annotations.description}};
故障时间: {{ ($alert.StartsAt.Add 28800e9).Format "2006-01-02 15:04:05" }}
恢复时间: {{ ($alert.EndsAt.Add 28800e9).Format "2006-01-02 15:04:05" }}
{{- if gt (len $alert.Labels.instance) 0 }}
实例信息: {{ $alert.Labels.instance }}
{{- end }}
========= = end =  =========
{{- end }}
{{- end }}
{{- end }}
{{- end }}
```

### webhook程序

```go
package main

import (
	"fmt"
	"github.com/gin-gonic/gin"
)

func main() {
	router := gin.Default()
	router.POST("/", func(context *gin.Context) {
		data, err := context.GetRawData()
		if err != nil {
			fmt.Errorf("error:%+v", err)
		}
		fmt.Println(string(data))
	})
	router.Run(":5001")
}
```

### docker-compose.yml

```yaml
version: '3'

services:

  influxdb:
    image: influxdb:1.8.4
    container_name: influxdb
    ports:
      - "8086:8086"
    environment:
      - INFLUXDB_DB=prometheus
      - INFLUXDB_ADMIN_ENABLED=true
      - INFLUXDB_ADMIN_USER=admin
      - INFLUXDB_ADMIN_PASSWORD=admin
      - INFLUXDB_USER=influxdb
      - INFLUXDB_USER_PASSWORD=influxdb

  chronograf:
    image: chronograf:1.8.8
    container_name: chronograf
    ports:
      - "8888:8888"
    environment:
      - INFLUXDB-URL=http://influxdb:8086

  db:
    image: timescaledev/promscale-extension:latest-pg12
    container_name: db
    ports:
      - 5432:5432/tcp
    environment:
      POSTGRES_PASSWORD: postgres
      POSTGRES_USER: postgres
      POSTGRES_DB: timescale

  prometheus:
    image: prom/prometheus:v2.24.1
    container_name: prometheus
    entrypoint: ["/bin/prometheus", "--config.file=/etc/prometheus/prometheus.yml", "--web.enable-lifecycle"]
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml:ro
      - ./targets.json:/etc/prometheus/targets.json:ro
      - ./alerts:/etc/prometheus/alerts:ro
    ports:
      - 9090:9090
    restart: always

  promscale-connector:
    image: timescale/promscale:latest
    container_name: promscale-connector
    ports:
      - 9201:9201/tcp
    restart: on-failure
    depends_on:
      - db
      - prometheus
    environment:
      PROMSCALE_LOG_LEVEL: debug
      PROMSCALE_DB_CONNECT_RETRIES: 10
      PROMSCALE_DB_HOST: db
      PROMSCALE_DB_PASSWORD: postgres
      PROMSCALE_WEB_TELEMETRY_PATH: /metrics-text
      PROMSCALE_DB_SSL_MODE: allow

  alertmanager:
    image: prom/alertmanager:v0.21.0
    container_name: alertmanager
    volumes:
      - ./alertmanager.yml:/etc/alertmanager/alertmanager.yml:ro
      - ./templates:/etc/alertmanager/templates:ro
    ports:
      - 9093:9093
    restart: always

  grafana:
    image: grafana/grafana
    container_name: grafana
    ports:
      - 3000:3000
    restart: always

networks:
  default:
    driver: bridge
```

### docker-compose.yml

```yaml
version: '2'

networks:
  consul-net:

services:
  consul1:
    image: consul:latest
    container_name: node1
    command: agent -server -bootstrap-expect=3 -node=node1 -bind=0.0.0.0 -client=0.0.0.0 -datacenter=dc1
    networks:
      - consul-net

  consul2:
    image: consul:latest
    container_name: node2
    command: agent -server -retry-join=node1 -node=node2 -bind=0.0.0.0 -client=0.0.0.0 -datacenter=dc1
    depends_on:
      - consul1
    networks:
      - consul-net

  consul3:
    image: consul:latest
    container_name: node3
    command: agent -server -retry-join=node1 -node=node3 -bind=0.0.0.0 -client=0.0.0.0 -datacenter=dc1
    depends_on:
      - consul1
    networks:
      - consul-net

  consul4:
    image: consul:latest
    container_name: node4
    command: agent -retry-join=node1 -node=ndoe4 -bind=0.0.0.0 -client=0.0.0.0 -datacenter=dc1 -ui
    ports:
      - 8500:8500
    depends_on:
      - consul1
      - consul2
      - consul3
    networks:
      - consul-net
```

# 参考文档

https://yunlzheng.gitbook.io/prometheus-book/

https://prometheus.io/docs/instrumenting/exporters/

https://prometheus.io/docs/operating/integrations/

https://prometheus.io/docs/prometheus/latest/querying/basics/

https://prometheus.io/docs/prometheus/latest/querying/operators/

https://prometheus.io/docs/prometheus/latest/querying/functions/

https://prometheus.io/docs/prometheus/latest/querying/examples/

https://prometheus.io/docs/prometheus/latest/querying/api/

https://github.com/timescale/promscale

https://www.consul.io/api/agent/service

https://grafana.com/grafana/dashboards/8919

https://grafana.com/grafana/dashboards/1860