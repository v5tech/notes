# 使用Hive将HDFS中的数据导入ES

大概思路描述:

1、首先创建一张`TEXTFILE`存储格式的外部表，然后从`HDFS`中加载数据

2、创建一张`SEQUENCEFILE`存储格式的外部表，然后从上一张`TEXTFILE`存储格式的外部表中拷贝数据到该表中

3、创建目标表，用于从`SEQUENCEFILE`存储格式的外部表中导入数据到该表中，最终将数据写入`ES`

每次导入数据前做一些清理工作：

```sql
USE jc;                         # jc数据库

DROP TABLE temp_text;           # 第一张TEXTFILE数据格式的表

DROP TABLE temp_sequ;           # 第二张SEQUENCEFILE数据格式的表

DROP TABLE jc_es;               # 目标数据表，用于将数据写入ES
```

从`HDFS中`清除`Hive`数据库存放目录下的文件

```bash
hadoop fs -rm -r /user/hive/warehouse/jc.db/*
```

加载`elasticsearch-hadoop-5.6.0.jar`类库

```bash
add jar /usr/local/hadoop/elasticsearch-hadoop-5.6.0/dist/elasticsearch-hadoop-5.6.0.jar;
```

### 1、创建`TEXTFILE`存储格式的外部表

```sql
CREATE EXTERNAL TABLE IF NOT EXISTS temp_text (
    stamp string,
    host string,
    sourceId bigint,
    callAdvTime string,
    orderId bigint,
    orderInputId bigint,
    channelType string,
    source string,
    uuid string,
    fromip string,
    delMode string,
    logtype string,
    repeatTime bigint,
    clientip string,
    scid string,
    responseMsg string,
    orderSourceId bigint,
    level string,
    channelTime string,
    advId bigint,
    ideaId bigint,
    port bigint,
    threadname string,
    levelvalue bigint,
    appid string,
    proId bigint,
    status bigint,
    cid string,
    delPlat string,
    landingPageId bigint,
    clickTime string,
    scname string,
    channelMoney float,
    inputMoney float,
    notifyChannelUrl string,
    callAdvUrl string,
    callChannelUrl string
)
COMMENT 'textfile log details'
ROW FORMAT DELIMITED
LINES TERMINATED BY '\n'
FIELDS TERMINATED BY ' '
STORED AS TEXTFILE;

SET io.compression.codecs=org.apache.hadoop.io.compress.SnappyCodec;
SET mapred.compress.map.output=true;
SET mapred.output.compress=true;
SET mapred.output.compression=org.apache.hadoop.io.compress.SnappyCodec;
SET mapred.output.compression.codec=org.apache.hadoop.io.compress.SnappyCodec;
SET mapred.output.compression.type=BLOCK;
SET hive.exec.compress.output=true;
SET hive.exec.compress.intermediate=true;
SET hive.intermediate.compression.codec=org.apache.hadoop.io.compress.SnappyCodec;
SET hive.intermediate.compression.type=BLOCK;

ALTER TABLE temp_text SET SERDEPROPERTIES('serialization.null.format' = 'null');
```

### 2、创建`SEQUENCEFILE`存储格式的外部表

```sql
CREATE EXTERNAL TABLE IF NOT EXISTS temp_sequ (
    stamp string,
    host string,
    sourceId bigint,
    callAdvTime string,
    orderId bigint,
    orderInputId bigint,
    channelType string,
    source string,
    uuid string,
    fromip string,
    delMode string,
    logtype string,
    repeatTime bigint,
    clientip string,
    scid string,
    responseMsg string,
    orderSourceId bigint,
    level string,
    channelTime string,
    advId bigint,
    ideaId bigint,
    port bigint,
    threadname string,
    levelvalue bigint,
    appid string,
    proId bigint,
    status bigint,
    cid string,
    delPlat string,
    landingPageId bigint,
    clickTime string,
    scname string,
    channelMoney float,
    inputMoney float,
    notifyChannelUrl string,
    callAdvUrl string,
    callChannelUrl string
)
COMMENT 'sequencefile log details'
ROW FORMAT DELIMITED
LINES TERMINATED BY '\n'
FIELDS TERMINATED BY ' '
STORED AS SEQUENCEFILE;

SET io.compression.codecs=org.apache.hadoop.io.compress.SnappyCodec;
SET mapred.compress.map.output=true;
SET mapred.output.compress=true;
SET mapred.output.compression=org.apache.hadoop.io.compress.SnappyCodec;
SET mapred.output.compression.codec=org.apache.hadoop.io.compress.SnappyCodec;
SET mapred.output.compression.type=BLOCK;
SET hive.exec.compress.output=true;
set hive.exec.compress.intermediate=true;
set hive.intermediate.compression.codec=org.apache.hadoop.io.compress.SnappyCodec;
set hive.intermediate.compression.type=BLOCK;

ALTER TABLE temp_sequ SET SERDEPROPERTIES('serialization.null.format' = 'null');
```

从`HDFS`中拷贝数据到`HDFS`文件系统的目标目录中

```bash
hadoop fs -cp -f /online_backup/2018-04-26 /tmp/
```

从`HDFS`中加载数据到`TEXTFILE`存储格式的表中，再从`TEXTFILE`格式的表中拷贝数据到`SEQUENCEFILE`存储格式的外部表中

```sql
LOAD DATA INPATH '/tmp/2018-04-12/jc-a-channelclick-2018.04.12.log.snappy' OVERWRITE INTO TABLE temp_text;

SELECT COUNT(*) FROM temp_text;

INSERT INTO TABLE temp_sequ SELECT * FROM temp_text;
```

### 3、创建最终目标表，将数据导入es

`Hive` 数据写入`ES`

```bash
add jar /usr/local/hadoop/elasticsearch-hadoop-5.6.0/dist/elasticsearch-hadoop-5.6.0.jar;
```

```sql
CREATE EXTERNAL TABLE IF NOT EXISTS jc_es (
    stamp string,
    host string,
    sourceId bigint,
    callAdvTime string,
    orderId bigint,
    orderInputId bigint,
    channelType string,
    source string,
    uuid string,
    fromip string,
    delMode string,
    logtype string,
    repeatTime bigint,
    clientip string,
    scid string,
    responseMsg string,
    orderSourceId bigint,
    level string,
    channelTime string,
    advId bigint,
    ideaId bigint,
    port bigint,
    threadname string,
    levelvalue bigint,
    appid string,
    proId bigint,
    status bigint,
    cid string,
    delPlat string,
    landingPageId bigint,
    clickTime string,
    scname string,
    channelMoney float,
    inputMoney float,
    notifyChannelUrl string,
    callAdvUrl string,
    callChannelUrl string
)
STORED BY 'org.elasticsearch.hadoop.hive.EsStorageHandler'
TBLPROPERTIES('es.resource' = 'jc-history-{logtype}/logs', 'es.nodes' = '192.168.1.155', 'es.read.metadata' = 'true','es.field.read.empty.as.null' ='true');
```

从`SEQUENCEFILE`存储格式的表中导数据最终输出到`ES`

```sql
INSERT INTO TABLE jc_es SELECT * FROM temp_sequ;

SELECT COUNT(*) FROM jc_es;
```