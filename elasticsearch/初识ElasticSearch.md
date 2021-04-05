 # 初识ElasticSearch

本笔记收集于极客时间

# lesson 1-ElasticSearch 简介

# 相关阅读

- Elasticsearch Certification https://www.elastic.co/cn/training/certification
- ElasticSearch Engineering I training  https://www.elastic.co/cn/training/elasticsearch-engineer-1
- ElasticSearch Engineering II training  https://www.elastic.co/cn/training/elasticsearch-engineer-2
- 6.7 发布 https://www.elastic.co/cn/blog/elastic-stack-6-7-0-released?elektra=products&storm=sub2
- 7.0 发布 https://www.elastic.co/blog/elasticsearch-7-0-0-released
- X-Pack开源 https://www.elastic.co/products/x-pack/open
- Elasticsearch上市 https://www.elastic.co/blog/ze-bell-has-rung-thank-you-users-customers-and-partners
- https://www.elastic.co/cn/use-cases/
- https://www.objectrocket.com/blog/elasticsearch/top-elasticsearch-use-cases/
- https://hackernoon.com/elastic-stack-a-brief-introduction-794bc7ff7d4f
- https://dzone.com/articles/elk-stack-overview-and-the-need-for-it



# lesson 2- Elasticsearch 的安装与简单配置

## 课程Demo

```
#启动单节点
bin/elasticsearch -E node.name=node0 -E cluster.name=geektime -E path.data=node0_data

#安装插件
bin/elasticsearch-plugin install analysis-icu

#查看插件
bin/elasticsearch-plugin list
#查看安装的插件
GET http://localhost:9200/_cat/plugins?v

#start multi-nodes Cluster
bin/elasticsearch -E node.name=node0 -E cluster.name=geektime -E path.data=node0_data
bin/elasticsearch -E node.name=node1 -E cluster.name=geektime -E path.data=node1_data
bin/elasticsearch -E node.name=node2 -E cluster.name=geektime -E path.data=node2_data
bin/elasticsearch -E node.name=node3 -E cluster.name=geektime -E path.data=node3_data

#查看集群
GET http://localhost:9200
#查看nodes
GET _cat/nodes
GET _cluster/health
```

## 相关阅读

- 安装指南 https://www.elastic.co/guide/en/elasticsearch/reference/7.1/install-elasticsearch.html
- Elastic Support Matrix(OS / JDK ) https://www.elastic.co/cn/support/matrix
- Elasticsearch 的一些重要配置 https://www.elastic.co/guide/en/elasticsearch/reference/current/important-settings.html
- https://www.elastic.co/guide/en/elasticsearch/reference/current/settings.html
- https://www.elastic.co/guide/en/elasticsearch/reference/current/important-settings.html
- Elasticsearch on Kuvernetes https://www.elastic.co/cn/blog/introducing-elastic-cloud-on-kubernetes-the-elasticsearch-operator-and-beyond
- CAT Plugins API https://www.elastic.co/guide/en/elasticsearch/reference/7.1/cat-plugins.html



# lesson 3- Kibana 的安装与界面快速浏览

## 课程Demo

```
#启动kibana
bin/kibana

#查看插件
bin/kibana-plugin list
```

## 相关阅读

- https://www.elastic.co/guide/en/kibana/current/setup.html
- Kibana 相关插件 https://www.elastic.co/guide/en/kibana/current/known-plugins.html



# lesson 4-Logstash 安装与测试数据导入

## 课程Demo

安装Logstash，并且导入Movielens的测试数据集

- Small: 100,000 ratings and 3,600 tag applications applied to 9,000 movies by 600 users. Last updated 9/2018.
- movielens/ml-latest-small/movies.csv movie数据
- movielens/logstash.conf //logstash 7.x 配置文件，
- movielens/logstash6.conf  //logstash 6.x 配置文件

```
#下载与ES相同版本号的logstash，（7.1.0），并解压到相应目录
#修改movielens目录下的logstash.conf文件
#path修改为,你实际的movies.csv路径
input {
  file {
    path => "YOUR_FULL_PATH_OF_movies.csv"
    start_position => "beginning"
    sincedb_path => "/dev/null"
  }
}

#启动Elasticsearch实例，然后启动 logstash，并制定配置文件导入数据
bin/logstash -f /YOUR_PATH_of_logstash.conf
```

## 相关阅读

- 下载最MovieLens最小测试数据集：https://grouplens.org/datasets/movielens/
- Logstash下载：https://www.elastic.co/cn/downloads/logstash
- Logstash参考文档：https://www.elastic.co/guide/en/logstash/current/index.html

[movielens.zip](https://www.yuque.com/attachments/yuque/0/2020/zip/89015/1598199279214-d1109521-c675-43ee-8180-622c49299b87.zip)



# lesson 5-基本概念(1)- 索引，文档和 REST API

## 课程Demo

- 需要通过Kibana导入Sample Data的电商数据。具体参考“2.2节-Kibana的安装与界面快速浏览”

Index 相关 API

```
#查看索引相关信息
GET kibana_sample_data_ecommerce

#查看索引的文档总数
GET kibana_sample_data_ecommerce/_count

#查看前10条文档，了解文档格式
POST kibana_sample_data_ecommerce/_search
{
}

#_cat indices API
#查看indices
GET /_cat/indices/kibana*?v&s=index

#查看状态为绿的索引
GET /_cat/indices?v&health=green

#按照文档个数排序
GET /_cat/indices?v&s=docs.count:desc

#查看具体的字段
GET /_cat/indices/kibana*?pri&v&h=health,index,pri,rep,docs.count,mt

#How much memory is used per index?
GET /_cat/indices?v&h=i,tm&s=tm:desc
```

## 相关阅读

- 为什么不再支持单个Index下，多个Tyeps https://www.elastic.co/cn/blog/moving-from-types-to-typeless-apis-in-elasticsearch-7-0
- CAT Index API https://www.elastic.co/guide/en/elasticsearch/reference/7.1/cat-indices.html



# lesson 6-基本概念（2）：节点，集群，分片及副本

## 课程Demo

- 需要通过Kibana导入Sample Data的电商数据。具体参考“2.2节-Kibana的安装与界面快速浏览”

```
get _cat/nodes?v
GET /_nodes/es7_01,es7_02
GET /_cat/nodes?v
GET /_cat/nodes?v&h=id,ip,port,v,m


GET _cluster/health
GET _cluster/health?level=shards
GET /_cluster/health/kibana_sample_data_ecommerce,kibana_sample_data_flights
GET /_cluster/health/kibana_sample_data_flights?level=shards

#### cluster state
The cluster state API allows access to metadata representing the state of the whole cluster. This includes information such as
GET /_cluster/state

#cluster get settings
GET /_cluster/settings
GET /_cluster/settings?include_defaults=true

GET _cat/shards
GET _cat/shards?h=index,shard,prirep,state,unassigned.reason
```

## 相关阅读

- CAT Nodes API https://www.elastic.co/guide/en/elasticsearch/reference/7.1/cat-nodes.html
- Cluster API https://www.elastic.co/guide/en/elasticsearch/reference/7.1/cluster.html
- CAT Shards API https://www.elastic.co/guide/en/elasticsearch/reference/7.1/cat-shards.html



# lesson 7-文档的基本 CRUD 与批量操作

## 课程Demo

```
POST users/_doc
{
      "user" : "Mike",
    "post_date" : "2019-04-15T14:12:12",
    "message" : "trying out Kibana"
}


#create document. 指定Id。如果id已经存在，报错
PUT users/_doc/1?op_type=create
{
    "user" : "Jack",
    "post_date" : "2019-05-15T14:12:12",
    "message" : "trying out Elasticsearch"
}

#create document. 指定 ID 如果已经存在，就报错
PUT users/_create/1
{
     "user" : "Jack",
    "post_date" : "2019-05-15T14:12:12",
    "message" : "trying out Elasticsearch"
}

### Get Document by ID
#Get the document by ID
GET users/_doc/1
#_source是文档的完整信息 字段过滤
GET users/_doc/1?_source=false
#包含某些字段
GET users/_doc/1?_source_includes=user
GET users/_doc/1?_source=user
#去除某些字段
GET users/_doc/1?_source_excludes=user
#直接查询_source
GET users/_source/1
GET users/_source/1/?_source_includes=user
GET users/_source/1/?_source_excludes=user




###  Index & Update
#Update 指定 ID  (先删除，在写入)
GET users/_doc/1
# 此时文档只有user属性
PUT users/_doc/1
{
    "user" : "Mike"

}


#GET users/_doc/1
#在原文档上增加字段 
POST users/_update/1/
{
    "doc":{
        "post_date" : "2019-05-15T14:12:12",
        "message" : "trying out Elasticsearch"
    }
}
DELETE test
DELETE test/_doc/1
PUT test/_doc/1
{
  "user" : "Mike111",
    "counter" : 1,
    "tags" : ["red"]
}
GET test/_doc/1/
# 局部字段更新
POST test/_update/1/
{
    "doc":{
        "user" : "Mike222"
    }
  
}

## 支持脚本嵌入 counter 计数
POST test/_update/1
{
    "script" : {
        "source": "ctx._source.counter += params.count",
        "lang": "painless",
        "params" : {
            "count" : 4
        }
    }
}

POST test/_update/1
{
    "script" : {
        "source": "ctx._source.tags.add(params.tag)",
        "lang": "painless",
        "params" : {
            "tag" : "blue"
        }
    }
}

POST test/_update/1
{
    "script" : {
        "source": "if (ctx._source.tags.contains(params.tag)) { ctx._source.tags.remove(ctx._source.tags.indexOf(params.tag)) }",
        "lang": "painless",
        "params" : {
            "tag" : "blue"
        }
    }
}


### Delete by Id
# 删除文档
DELETE users/_doc/1



### Bulk 操作
#执行两次，查看每次的结果

#执行第1次
POST _bulk
{ "index" : { "_index" : "test", "_id" : "1" } }
{ "field1" : "value2" }
{ "delete" : { "_index" : "test", "_id" : "2" } }
{ "create" : { "_index" : "test2", "_id" : "3" } }
{ "field1" : "value3" }
{ "update" : {"_id" : "1", "_index" : "test"} }
{ "doc" : {"field2" : "value3"} }

GET test/_doc/1/
#执行第2次
POST _bulk
{ "index" : { "_index" : "test", "_id" : "1" } }
{ "field1" : "value1" }
{ "delete" : { "_index" : "test", "_id" : "2" } }
{ "create" : { "_index" : "test2", "_id" : "3" } }
{ "field1" : "value3" }
{ "update" : {"_id" : "1", "_index" : "test"} }
{ "doc" : {"field2" : "value2"} }

### mget 操作
GET /_mget
{
    "docs" : [
        {
            "_index" : "test",
            "_id" : "1"
        },
        {
            "_index" : "test",
            "_id" : "2"
        },
         {
            "_index" : "test2",
            "_id" : "3"
        }
    ]
}


#URI中指定index
GET /test/_mget
{
    "docs" : [
        {

            "_id" : "1"
        },
        {

            "_id" : "2"
        }
    ]
}


GET /_mget
{
    "docs" : [
        {
            "_index" : "test",
            "_id" : "2",
            "_source" : false
        },
        {
            "_index" : "test",
            "_id" : "1",
            "_source" : ["field1", "field2"]
        },
        {
            "_index" : "test",
            "_id" : "3",
            "_source" : {
                "include": ["user"],
                "exclude": ["user.location"]
            }
        }
    ]
}

### msearch 操作
POST kibana_sample_data_ecommerce/_msearch
{}
{"query" : {"match_all" : {}},"size":1}
{"index" : "kibana_sample_data_flights"}
{"query" : {"match_all" : {}},"size":2}


### msearch 操作
POST kibana_sample_data_ecommerce/_msearch
{}
{"query" : {"match_all" : {}},"size":1}
{"index" : "kibana_sample_data_flights"}
{"query" : {"match_all" : {}},"size":2}


POST users/_msearch
{}
{"query" : {"match_all" : {}},"size":1}



### 清除测试数据
#清除数据
DELETE users
DELETE test
DELETE test2
```

## 相关阅读

- Document API https://www.elastic.co/guide/en/elasticsearch/reference/7.1/docs.html



# lesson 8-倒排索引入门倒排索引入门

## 课程Demo

```
POST _analyze
{
  "analyzer": "standard",
  "text": "Mastering Elasticsearch"
}

POST _analyze
{
  "analyzer": "standard",
  "text": "Elasticsearch Server"
}

POST _analyze
{
  "analyzer": "standard",
  "text": "Elasticsearch Essentials"
}
```

## 相关阅读

- https://zh.wikipedia.org/wiki/倒排索引
- https://www.elastic.co/guide/cn/elasticsearch/guide/current/inverted-index.html



# lesson 9-**使用分析器进行分词**

## 课程Demo

```
#Simple Analyzer – 按照非字母切分（符号被过滤），小写处理
#Stop Analyzer – 小写处理，停用词过滤（the，a，is）
#Whitespace Analyzer – 按照空格切分，不转小写
#Keyword Analyzer – 不分词，直接将输入当作输出
#Patter Analyzer – 正则表达式，默认 \W+ (非字符分隔)
#Language – 提供了30多种常见语言的分词器
#2 running Quick brown-foxes leap over lazy dogs in the summer evening

#查看不同的analyzer的效果
#standard
GET _analyze
{
  "analyzer": "standard",
  "text": "2 running Quick brown-foxes leap over lazy dogs in the summer evening."
}

#simpe
GET _analyze
{
  "analyzer": "simple",
  "text": "2 running Quick brown-foxes leap over lazy dogs in the summer evening."
}


GET _analyze
{
  "analyzer": "stop",
  "text": "2 running Quick brown-foxes leap over lazy dogs in the summer evening."
}


#stop
GET _analyze
{
  "analyzer": "whitespace",
  "text": "2 running Quick brown-foxes leap over lazy dogs in the summer evening."
}

#keyword
GET _analyze
{
  "analyzer": "keyword",
  "text": "2 running Quick brown-foxes leap over lazy dogs in the summer evening."
}

GET _analyze
{
  "analyzer": "pattern",
  "text": "2 running Quick brown-foxes leap over lazy dogs in the summer evening."
}


#english
GET _analyze
{
  "analyzer": "english",
  "text": "2 running Quick brown-foxes leap over lazy dogs in the summer evening."
}


POST _analyze
{
  "analyzer": "icu_analyzer",
  "text": "他说的确实在理”"
}


POST _analyze
{
  "analyzer": "standard",
  "text": "他说的确实在理”"
}


POST _analyze
{
  "analyzer": "icu_analyzer",
  "text": "这个苹果不大好吃"
}
```

## 相关阅读

- https://www.elastic.co/guide/en/elasticsearch/reference/7.1/indices-analyze.html
- https://www.elastic.co/guide/en/elasticsearch/reference/current/analyzer-anatomy.html



# lesson 10-**URI Search 概览**

## 课程Demo

需要通过Kibana导入Sample Data的电商数据。

具体参考“2.2节-Kibana的安装与界面快速浏览”一节教程

```
#URI Query
GET kibana_sample_data_ecommerce/_search?q=customer_first_name:Eddie
GET kibana*/_search?q=customer_first_name:Eddie
GET /_all/_search?q=customer_first_name:Eddie


#REQUEST Body
POST kibana_sample_data_ecommerce/_search
{
    "profile": true,
    "query": {
        "match_all": {}
    }
}
```

## 相关阅读

- https://www.elastic.co/guide/en/elasticsearch/reference/7.1/search-search.html
- https://searchenginewatch.com/sew/news/2065080/search-engines-101
- https://www.huffpost.com/entry/search-engines-101-part-i_b_1104525
- https://www.entrepreneur.com/article/176398
- https://www.searchtechnologies.com/meaning-of-relevancy
- https://baike.baidu.com/item/搜索引擎发展史/2422574



# lesson 11-**URI Search详解**

## 课程Demo

```
#基本查询
GET /movies/_search?q=2012&df=title&sort=year:desc&from=0&size=10&timeout=1s

#带profile
GET /movies/_search?q=2012&df=title
{
    "profile":"true"
}


#泛查询，正对_all,所有字段
GET /movies/_search?q=2012
{
    "profile":"true"
}

#指定字段
GET /movies/_search?q=title:2012&sort=year:desc&from=0&size=10&timeout=1s
{
    "profile":"true"
}


# 查找美丽心灵, Mind为泛查询
GET /movies/_search?q=title:Beautiful Mind
{
    "profile":"true"
}

# 泛查询
GET /movies/_search?q=title:2012
{
    "profile":"true"
}

#使用引号，Phrase查询
GET /movies/_search?q=title:"Beautiful Mind"
{
    "profile":"true"
}

#分组，Bool查询
GET /movies/_search?q=title:(Beautiful Mind)
{
    "profile":"true"
}


#布尔操作符
# 查找美丽心灵
GET /movies/_search?q=title:(Beautiful AND Mind)
{
    "profile":"true"
}

# 查找美丽心灵
GET /movies/_search?q=title:(Beautiful NOT Mind)
{
    "profile":"true"
}

# 查找美丽心灵
GET /movies/_search?q=title:(Beautiful %2BMind)
{
    "profile":"true"
}


#范围查询 ,区间写法
GET /movies/_search?q=title:beautiful AND year:[2002 TO 2018%7D
{
    "profile":"true"
}


#通配符查询
GET /movies/_search?q=title:b*
{
    "profile":"true"
}

//模糊匹配&近似度匹配
GET /movies/_search?q=title:beautifl~1
{
    "profile":"true"
}

GET /movies/_search?q=title:"Lord Rings"~2
{
    "profile":"true"
}
```

## 相关阅读

- https://www.elastic.co/guide/en/elasticsearch/reference/7.0/search-uri-request.html
- https://www.elastic.co/guide/en/elasticsearch/reference/7.0/search-search.html



# lesson 12-**Request Body 与 Query DSL**

## 课程Demo

- 需要通过 Kibana 导入Sample Data的电商数据。具体参考“2.2节-Kibana的安装与界面快速浏览”
- 需导入Movie测试数据，具体参考“2.4-Logstash安装与导入数据”

```
#ignore_unavailable=true，可以忽略尝试访问不存在的索引“404_idx”导致的报错
#查询movies分页
POST /movies,404_idx/_search?ignore_unavailable=true
{
  "profile": true,
    "query": {
        "match_all": {}
    }
}

POST /kibana_sample_data_ecommerce/_search
{
  "from":10,
  "size":20,
  "query":{
    "match_all": {}
  }
}


#对日期排序
POST kibana_sample_data_ecommerce/_search
{
  "sort":[{"order_date":"desc"}],
  "query":{
    "match_all": {}
  }

}

#source filtering
POST kibana_sample_data_ecommerce/_search
{
  "_source":["order_date"],
  "query":{
    "match_all": {}
  }
}


#脚本字段
GET kibana_sample_data_ecommerce/_search
{
  "script_fields": {
    "new_field": {
      "script": {
        "lang": "painless",
        "source": "doc['order_date'].value+'hello'"
      }
    }
  },
  "query": {
    "match_all": {}
  }
}


POST movies/_search
{
  "query": {
    "match": {
      "title": "last christmas"
    }
  }
}

POST movies/_search
{
  "query": {
    "match": {
      "title": {
        "query": "last christmas",
        "operator": "and"
      }
    }
  }
}

POST movies/_search
{
  "query": {
    "match_phrase": {
      "title":{
        "query": "one love"

      }
    }
  }
}

POST movies/_search
{
  "query": {
    "match_phrase": {
      "title":{
        "query": "one love",
        "slop": 1

      }
    }
  }
}
```

## 相关阅读

- https://www.elastic.co/guide/en/elasticsearch/reference/7.0/search-uri-request.html
- https://www.elastic.co/guide/en/elasticsearch/reference/7.0/search-search.html



# lesson 13-**Query & Simple Query String Query**

## 课程 Demo

- 需导入Movie测试数据，具体参考“2.4-Logstash安装与导入数据”

```
PUT /users/_doc/1
{
  "name":"Ruan Yiming",
  "about":"java, golang, node, swift, elasticsearch"
}

PUT /users/_doc/2
{
  "name":"Li Yiming",
  "about":"Hadoop"
}


POST users/_search
{
  "query": {
    "query_string": {
      "default_field": "name",
      "query": "Ruan AND Yiming"
    }
  }
}


POST users/_search
{
  "query": {
    "query_string": {
      "fields":["name","about"],
      "query": "(Ruan AND Yiming) OR (Java AND Elasticsearch)"
    }
  }
}


#Simple Query 默认的operator是 Or
POST users/_search
{
  "query": {
    "simple_query_string": {
      "query": "Ruan AND Yiming",
      "fields": ["name"]
    }
  }
}


POST users/_search
{
  "query": {
    "simple_query_string": {
      "query": "Ruan Yiming",
      "fields": ["name"],
      "default_operator": "AND"
    }
  }
}


GET /movies/_search
{
    "profile": true,
    "query":{
        "query_string":{
            "default_field": "title",
            "query": "Beafiful AND Mind"
        }
    }
}


# 多fields
GET /movies/_search
{
    "profile": true,
    "query":{
        "query_string":{
            "fields":[
                "title",
                "year"
            ],
            "query": "2012"
        }
    }
}



GET /movies/_search
{
    "profile":true,
    "query":{
        "simple_query_string":{
            "query":"Beautiful +mind",
            "fields":["title"]
        }
    }
}
```



# lesson 14-**Dynamic Mapping 和常见字段类型**

Mapping中的字段一旦设定后，禁止直接修改。因为倒排索引生成后不允许直接修改。需要重新建立新的索引，做reindex操作。

类似数据库中的表结构定义，主要作用

- 定义所以下的字段名字
- 定义字段的类型
- 定义倒排索引相关的配置（是否被索引？采用的Analyzer）

对新增字段的处理

true

false

strict

在object下，支持做dynamic的属性的定义

## 课程Demo

```
#写入文档，查看 Mapping
PUT mapping_test/_doc/1
{
  "firstName":"Chan",
  "lastName": "Jackie",
  "loginDate":"2018-07-24T10:29:48.103Z"
}

#查看 Mapping文件
GET mapping_test/_mapping


#Delete index
DELETE mapping_test

#dynamic mapping，推断字段的类型
PUT mapping_test/_doc/1
{
    "uid" : "123",
    "isVip" : false,
    "isAdmin": "true",
    "age":19,
    "heigh":180
}

#查看 Dynamic
GET mapping_test/_mapping


#默认Mapping支持dynamic，写入的文档中加入新的字段
PUT dynamic_mapping_test/_doc/1
{
  "newField":"someValue"
}

#该字段可以被搜索，数据也在_source中出现
POST dynamic_mapping_test/_search
{
  "query":{
    "match":{
      "newField":"someValue"
    }
  }
}


#修改为dynamic false
PUT dynamic_mapping_test/_mapping
{
  "dynamic": false
}

#新增 anotherField
PUT dynamic_mapping_test/_doc/10
{
  "anotherField":"someValue"
}


#该字段不可以被搜索，因为dynamic已经被设置为false
POST dynamic_mapping_test/_search
{
  "query":{
    "match":{
      "anotherField":"someValue"
    }
  }
}

get dynamic_mapping_test/_doc/10

#修改为strict
PUT dynamic_mapping_test/_mapping
{
  "dynamic": "strict"
}



#写入数据出错，HTTP Code 400
PUT dynamic_mapping_test/_doc/12
{
  "lastField":"value"
}

DELETE dynamic_mapping_test
```

## 相关阅读

- https://www.elastic.co/guide/en/elasticsearch/reference/7.1/dynamic-mapping.html



# lesson 15-**显式Mapping设置与常见参数介绍**

## 课程Demos

```
#设置 index 为 false
DELETE users
PUT users
{
    "mappings" : {
      "properties" : {
        "firstName" : {
          "type" : "text"
        },
        "lastName" : {
          "type" : "text"
        },
        "mobile" : {
          "type" : "text",
          "index": false
        }
      }
    }
}

PUT users/_doc/1
{
  "firstName":"Ruan",
  "lastName": "Yiming",
  "mobile": "12345678"
}

POST /users/_search
{
  "query": {
    "match": {
      "mobile":"12345678"
    }
  }
}




#设定Null_value

DELETE users
PUT users
{
    "mappings" : {
      "properties" : {
        "firstName" : {
          "type" : "text"
        },
        "lastName" : {
          "type" : "text"
        },
        "mobile" : {
          "type" : "keyword",
          "null_value": "NULL"
        }

      }
    }
}

PUT users/_doc/1
{
  "firstName":"Ruan",
  "lastName": "Yiming",
  "mobile": null
}


PUT users/_doc/2
{
  "firstName":"Ruan2",
  "lastName": "Yiming2"

}

GET users/_search
{
  "query": {
    "match": {
      "mobile":"NULL"
    }
  }

}



#设置 Copy to
DELETE users
PUT users
{
  "mappings": {
    "properties": {
      "firstName":{
        "type": "text",
        "copy_to": "fullName"
      },
      "lastName":{
        "type": "text",
        "copy_to": "fullName"
      }
    }
  }
}
PUT users/_doc/1
{
  "firstName":"Ruan",
  "lastName": "Yiming"
}

GET users/_search?q=fullName:(Ruan Yiming)

POST users/_search
{
  "query": {
    "match": {
       "fullName":{
        "query": "Ruan Yiming",
        "operator": "and"
      }
    }
  }
}


#数组类型
PUT users/_doc/1
{
  "name":"onebird",
  "interests":"reading"
}

PUT users/_doc/1
{
  "name":"twobirds",
  "interests":["reading","music"]
}

POST users/_search
{
  "query": {
        "match_all": {}
    }
}

GET users/_mapping
```

## 补充阅读

- Mapping Parameters https://www.elastic.co/guide/en/elasticsearch/reference/7.1/mapping-params.html



# lesson 16-**多字段特性及Mapping中配置自定义Analyzer**

## 课程Demo

```
PUT logs/_doc/1
{"level":"DEBUG"}

GET /logs/_mapping
GET /logs/_settings
POST _analyze
{
  "tokenizer":"keyword",
  "char_filter":["html_strip"],
  "text": "<b>hello world</b>"
}


POST _analyze
{
  "tokenizer":"path_hierarchy",
  "text":"/user/ymruan/a/b/c/d/e"
}



#使用char filter进行替换
POST _analyze
{
  "tokenizer": "standard",
  "char_filter": [
      {
        "type" : "mapping",
        "mappings" : [ "- => _"]
      }
    ],
  "text": "123-456, I-test! test-990 650-555-1234"
}

//char filter 替换表情符号
POST _analyze
{
  "tokenizer": "standard",
  "char_filter": [
      {
        "type" : "mapping",
        "mappings" : [ ":) => happy", ":( => sad"]
      }
    ],
    "text": ["I am felling :)", "Feeling :( today"]
}

// white space and snowball
GET _analyze
{
  "tokenizer": "whitespace",
  "filter": ["stop","snowball"],
  "text": ["The gilrs in China are playing this game!"]
}


// whitespace与stop
GET _analyze
{
  "tokenizer": "whitespace",
  "filter": ["stop","snowball"],
  "text": ["The rain in Spain falls mainly on the plain."]
}


//remove 加入lowercase后，The被当成 stopword删除
GET _analyze
{
  "tokenizer": "whitespace",
  "filter": ["lowercase","stop","snowball"],
  "text": ["The gilrs in China are playing this game!"]
}

//正则表达式
GET _analyze
{
  "tokenizer": "standard",
  "char_filter": [
      {
        "type" : "pattern_replace",
        "pattern" : "http://(.*)",
        "replacement" : "$1"
      }
    ],
    "text" : "http://www.elastic.co"
}



DELETE my_index
PUT my_index
{
  "settings":{
    "analysis": {
        "char_filter": {
            "&_to_and": {
                "type": "mapping",
                "mappings": ["& => and"]
            }
        },
        "filter": {
            "my_stopwords": {
                "type": "stop",
                "stopwords": ["the", "a"]
            }
        },
        "analyzer": {
            "my_analyzer": {    
                "type": "custom",
                "char_filter": ["html_strip", "&_to_and"], 
                "tokenizer": "standard",
                "filter": ["lowercase", "my_stopwords"]    
            }
        }
    }
  },
  "mappings": {
    "properties": {
      "firstName":{
        "type": "text",
        "analyzer": "my_analyzer"
      },
      "lastName":{
        "type": "text",
        "analyzer": "my_analyzer"
      }
    }
  }
}

GET my_index/_analyze
{
    "analyzer": "my_analyzer",   
    "text": "There-is & a DOG<br/> in house"
}

PUT my_index/_doc/1
{
  "firstName":"There-is & a DOG<br/> in house",
  "lastName": "There-is & a DOG<br/> in house"
}
GET my_index/_mapping

GET my_index/_search
{
  "query": {
    "match": {
      "firstName":"and"
    }
  }

}
```



# lesson 17-**Dynamic Template和Index Template**

## 课程Demo

```
#数字字符串被映射成text，日期字符串被映射成日期
PUT ttemplate/_doc/1
{
    "someNumber":"1",
    "someDate":"2019/01/01"
}
GET ttemplate/_mapping


#Create a default template
PUT _template/template_default
{
  "index_patterns": ["*"],
  "order" : 0,
  "version": 1,
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas":1
  }
}


PUT /_template/template_test
{
    "index_patterns" : ["test*"],
    "order" : 1,
    "settings" : {
        "number_of_shards": 1,
        "number_of_replicas" : 2
    },
    "mappings" : {
        "date_detection": false,
        "numeric_detection": true
    }
}

#查看template信息
GET /_template/template_default
GET /_template/temp*


#写入新的数据，index以test开头
PUT testtemplate/_doc/1
{
    "someNumber":"1",
    "someDate":"2019/01/01"
}
GET testtemplate/_mapping
get testtemplate/_settings

PUT testmy
{
    "settings":{
        "number_of_replicas":5
    }
}

put testmy/_doc/1
{
  "key":"value"
}

get testmy/_settings
DELETE testmy
DELETE /_template/template_default
DELETE /_template/template_test



#Dynaminc Mapping 根据类型和字段名
DELETE my_index

PUT my_index/_doc/1
{
  "firstName":"Ruan",
  "isVIP":"true"
}

GET my_index/_mapping
DELETE my_index
PUT my_index
{
  "mappings": {
    "dynamic_templates": [
            {
        "strings_as_boolean": {
          "match_mapping_type":   "string",
          "match":"is*",
          "mapping": {
            "type": "boolean"
          }
        }
      },
      {
        "strings_as_keywords": {
          "match_mapping_type":   "string",
          "mapping": {
            "type": "keyword"
          }
        }
      }
    ]
  }
}


DELETE my_index
#结合路径
PUT my_index
{
  "mappings": {
    "dynamic_templates": [
      {
        "full_name": {
          "path_match":   "name.*",
          "path_unmatch": "*.middle",
          "mapping": {
            "type":       "text",
            "copy_to":    "full_name"
          }
        }
      }
    ]
  }
}


PUT my_index/_doc/1
{
  "name": {
    "first":  "John",
    "middle": "Winston",
    "last":   "Lennon"
  }
}

GET my_index/_search?q=full_name:John
```

## 相关阅读

- Index Templates https://www.elastic.co/guide/en/elasticsearch/reference/7.1/indices-templates.html
- Dynamic Template https://www.elastic.co/guide/en/elasticsearch/reference/7.1/dynamic-mapping.html



# lesson 18-**Elasticsearch聚合分析简介**

## 课程Demo

- 需要通过Kibana导入Sample Data的飞机航班数据。具体参考“2.2节-Kibana的安装与界面快速浏览”

```
#按照目的地进行分桶统计
GET kibana_sample_data_flights/_search
{
    "size": 0,
    "aggs":{
        "flight_dest":{
            "terms":{
                "field":"DestCountry"
            }
        }
    }
}



#查看航班目的地的统计信息，增加平均，最高最低价格
GET kibana_sample_data_flights/_search
{
    "size": 0,
    "aggs":{
        "flight_dest":{
            "terms":{
                "field":"DestCountry"
            },
            "aggs":{
                "avg_price":{
                    "avg":{
                        "field":"AvgTicketPrice"
                    }
                },
                "max_price":{
                    "max":{
                        "field":"AvgTicketPrice"
                    }
                },
                "min_price":{
                    "min":{
                        "field":"AvgTicketPrice"
                    }
                }
            }
        }
    }
}



#价格统计信息+天气信息
GET kibana_sample_data_flights/_search
{
    "size": 0,
    "aggs":{
        "flight_dest":{
            "terms":{
                "field":"DestCountry"
            },
            "aggs":{
                "stats_price":{
                    "stats":{
                        "field":"AvgTicketPrice"
                    }
                },
                "wather":{
                  "terms": {
                    "field": "DestWeather",
                    "size": 5
                  }
                }

            }
        }
    }
}
```

## 相关阅读

- https://www.elastic.co/guide/en/elasticsearch/reference/7.1/search-aggregations.html