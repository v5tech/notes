# elasticsearch rest api 快速上手

> 注：本文档中除无特别说明，请求方式均为`GET`。所有的请求均在`Sense`中测试通过

遵循的格式为

```
curl -X<REST Verb> <Node>:<Port>/<Index>/<Type>/<ID>
```

### 集群健康查看

* http://127.0.0.1:9200/_cat/health?v

```
epoch      timestamp cluster       status node.total node.data shards pri relo init unassign pending_tasks 
1441940569 11:02:49  elasticsearch yellow          1         1      7   7    0    0        7             0 
```

* http://127.0.0.1:9200/_cat/nodes?v

```
host ip            heap.percent ram.percent load node.role master name     
acer 169.254.9.202           32          52      d         *      Mys-Tech
```

### 列出所有的indices

* http://127.0.0.1:9200/_cat/indices?v

```
health status index              pri rep docs.count docs.deleted store.size pri.store.size 
yellow open   .marvel-2015.09.11   1   1       3233            0     10.5mb         10.5mb 
yellow open   .marvel-2015.09.10   1   1       1996            0      3.9mb          3.9mb 
yellow open   news                 5   1       3455            0     17.8mb         17.8mb 
```

### 创建索引

使用`PUT`请求创建一个countries的索引

```
curl -XPUT http://127.0.0.1:9200/countries?pretty
```

输出:

```
{
   "acknowledged": true
}
```

查看索引列表

```
curl -XGET http://127.0.0.1:9200/_cat/indices?v
```

输出:

```
health status index              pri rep docs.count docs.deleted store.size pri.store.size 
yellow open   countries              5   1          0            0       575b           575b 
yellow open   .marvel-2015.09.11   1   1       3436            0     11.4mb         11.4mb 
yellow open   .marvel-2015.09.10   1   1       1996            0      3.9mb          3.9mb 
yellow open   news                 5   1       3455            0     17.8mb         17.8mb 
```

### 索引文档

* 使用自定义id索引文档

使用`PUT`请求创建一个索引为`countries`类型为`country`的文档。其文档编号为`1`，文档内容包含`name`和`capital`

```
curl -XPUT http://127.0.0.1:9200/countries/country/1?pretty -d '
{
  "name": "中国",
  "capital": "北京"
}'
```

输出:

```
{
   "_index": "countries",
   "_type": "country",
   "_id": "1",
   "_version": 1,
   "created": true
}
```

* 使用系统分配的id索引文档

```
curl -XPOST http://127.0.0.1:9200/countries/country?pretty -d '
{
  "name": "韩国",
  "capital": "首尔"
}'
```
注意：使用系统分配的id时使用`POST`方式提交文档，且在`索引\类型`url格式中不再有id

输出:

```
{
   "_index": "countries",
   "_type": "country",
   "_id": "AU-6awteDgxJZYVN-E5I",
   "_version": 1,
   "created": true
}
```

### 查询文档

使用自定义id查询文档

```
curl -XGET http://127.0.0.1:9200/countries/country/1?pretty
```

输出:

```
{
   "_index": "countries",
   "_type": "country",
   "_id": "1",
   "_version": 1,
   "found": true,
   "_source": {
      "name": "中国",
      "capital": "北京"
   }
}
```

使用系统分配的id查询

```
GET http://127.0.0.1:9200/countries/country/AU-6awteDgxJZYVN-E5I?pretty

```
输出:

```
{
   "_index": "countries",
   "_type": "country",
   "_id": "AU-6awteDgxJZYVN-E5I",
   "_version": 1,
   "found": true,
   "_source": {
      "name": "韩国",
      "capital": "首尔"
   }
}
```

### 查看索引信息

```
GET http://127.0.0.1:9200/countries/
```

输出:

```
{
   "countries": {
      "aliases": {},
      "mappings": {
         "country": {
            "properties": {
               "capital": {
                  "type": "string"
               },
               "name": {
                  "type": "string"
               }
            }
         }
      },
      "settings": {
         "index": {
            "creation_date": "1441941497754",
            "uuid": "UaoQ_WCATaiy5w736cjw2A",
            "number_of_replicas": "1",
            "number_of_shards": "5",
            "version": {
               "created": "1070199"
            }
         }
      },
      "warmers": {}
   }
}
```

### 删除索引

删除`myindex`索引

```
DELETE http://127.0.0.1:9200/myindex/?pretty
```
输出:

```
{
   "acknowledged": true
}
```

### 索引或替换一个文档

根据文档id索引或替换文档，若存在则修改替换，否则索引该文档。

* 使用已存在的id

修改文档id为1的国家信息。

```
PUT 'http://127.0.0.1:9200/countries/country/1?pretty'
{
    "name": "日本",
    "capital": "东京"
}
```

查询其是否已修改

```
GET http://127.0.0.1:9200/countries/country/1?pretty
```

输出:

```
{
   "_index": "countries",
   "_type": "country",
   "_id": "1",
   "_version": 2,
   "found": true,
   "_source": {
      "name": "日本",
      "capital": "东京"
   }
}
```

可见国家信息已由`中国`变为`日本`，其首都信息也发生了变化

* 使用不存在的id则是索引文档

```
PUT http://127.0.0.1:9200/countries/country/2?pretty
{
    "name": "澳大利亚",
    "capital": "悉尼"
}
```

输出:

```
{
   "_index": "countries",
   "_type": "country",
   "_id": "2",
   "_version": 1,
   "created": true
}
```

### 修改文档

* 按doc方式更新文档

以`doc`方式修改文档id为1的文档

```
POST http://127.0.0.1:9200/countries/country/1/_update?pretty
{
  "doc": { "name": "美国","capital": "华盛顿"}
}
```
其中`doc`是固定写法,其内容为要修改的文档内容

* 按script方式更新文档

以`script`方式修改文档id为1的文档

```
POST http://127.0.0.1:9200/countries/country/1/_update?pretty
{
  "script": "ctx._source.name=\"加拿大\";ctx._source.capital=\"渥太华\""
}
```

### 删除文档

* 按文档id删除

```
DELETE http://127.0.0.1:9200/countries/country/1?pretty
```

输出：

```
{
   "found": true,
   "_index": "countries",
   "_type": "country",
   "_id": "1",
   "_version": 6
}
```


* 根据查询结果删除

```
DELETE http://127.0.0.1:9200/countries/country/_query?pretty
{
    "query": { "match": { "name": "美国" } }
}

```

输出：

```
{
   "_indices": {
      "countries": {
         "_shards": {
            "total": 5,
            "successful": 5,
            "failed": 0
         }
      }
   }
}
```

查询是否还有name为美国的文档

```
GET http://127.0.0.1:9200/countries/country/_query
{
    "query": { "match_all": { "name": "美国" } }
}
```


### 批量处理

_bulk api

https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-bulk.html

遵循格式

/_bulk, /{index}/_bulk, {index}/{type}/_bulk

```
action_and_meta_data\n
optional_source\n
action_and_meta_data\n
optional_source\n
```

支持的action有`index`, `create`, `delete`, `update`

`index`和`create`在下一行跟上要索引的doc
`delete`则不需要
`update`在下一行跟上`doc`或`script`

* 批量索引文档

```
POST http://127.0.0.1:9200/countries/country/_bulk?pretty
{"index":{"_id":"1"}}
{"name": "中国","capital": "北京"}
{"index":{"_id":"2"}}
{"name": "美国","capital": "华盛顿"}
{"index":{"_id":"3"}}
{"name": "日本","capital": "东京"}
{"index":{"_id":"4"}}
{"name": "澳大利亚","capital": "悉尼"}
{"index":{"_id":"5"}}
{"name": "印度","capital": "新德里"}
{"index":{"_id":"6"}}
{"name": "韩国","capital": "首尔"}
```

以上请求将会批量索引6个文档。

输出：

```
{
   "took": 4,
   "errors": false,
   "items": [
      {
         "index": {
            "_index": "countries",
            "_type": "country",
            "_id": "1",
            "_version": 1,
            "status": 201
         }
      },
      {
         "index": {
            "_index": "countries",
            "_type": "country",
            "_id": "2",
            "_version": 2,
            "status": 200
         }
      },
      {
         "index": {
            "_index": "countries",
            "_type": "country",
            "_id": "3",
            "_version": 1,
            "status": 201
         }
      },
      {
         "index": {
            "_index": "countries",
            "_type": "country",
            "_id": "4",
            "_version": 1,
            "status": 201
         }
      },
      {
         "index": {
            "_index": "countries",
            "_type": "country",
            "_id": "5",
            "_version": 1,
            "status": 201
         }
      },
      {
         "index": {
            "_index": "countries",
            "_type": "country",
            "_id": "6",
            "_version": 1,
            "status": 201
         }
      }
   ]
}
```

* 批量执行，含index、create、delete、update

```
POST http://127.0.0.1:9200/countries/country/_bulk?pretty
{"index":{"_id":"7"}}
{"name": "新加坡","capital": "渥太华"}
{"create":{"_id":"8"}}
{"name": "德国","capital": "柏林"}
{"update":{"_id":"1"}}
{"doc": {"name": "法国","capital": "巴黎" }}
{"update":{"_id":"3"}}
{"script": "ctx._source.name = \"法国\";ctx._source.capital = \"巴黎\""}
{"delete":{"_id":"2"}}
```

输出：

```
{
   "took": 40,
   "errors": false,
   "items": [
      {
         "index": {
            "_index": "countries",
            "_type": "country",
            "_id": "7",
            "_version": 1,
            "status": 201
         }
      },
      {
         "create": {
            "_index": "countries",
            "_type": "country",
            "_id": "8",
            "_version": 1,
            "status": 201
         }
      },
      {
         "update": {
            "_index": "countries",
            "_type": "country",
            "_id": "1",
            "_version": 2,
            "status": 200
         }
      },
      {
         "update": {
            "_index": "countries",
            "_type": "country",
            "_id": "3",
            "_version": 2,
            "status": 200
         }
      },
      {
         "delete": {
            "_index": "countries",
            "_type": "country",
            "_id": "2",
            "_version": 3,
            "status": 200,
            "found": true
         }
      }
   ]
}
```

* 导入数据

countries.json

```
{"index":{"_id":"1"}}
{"name": "新加坡","capital": "渥太华"}
{"index":{"_id":"2"}}
{"name": "韩国","capital": "首尔"}
{"index":{"_id":"3"}}
{"name": "朝鲜","capital": "平壤"}
{"index":{"_id":"4"}}
{"name": "日本","capital": "东京"}
{"index":{"_id":"5"}}
{"name": "马来西亚","capital": "吉隆坡"}
```

使用curl的`--data-binary`参数导入数据

```
curl XPOST http://127.0.0.1:9200/countries/country/_bulk?pretty --data-binary @countries.json
```

或者使用postman导入

```
http://127.0.0.1:9200/countries/country/_bulk?pretty
{"index":{"_id":"1"}}
{"name": "新加坡","capital": "渥太华"}
{"index":{"_id":"2"}}
{"name": "韩国","capital": "首尔"}
{"index":{"_id":"3"}}
{"name": "朝鲜","capital": "平壤"}
{"index":{"_id":"4"}}
{"name": "日本","capital": "东京"}
{"index":{"_id":"5"}}
{"name": "马来西亚","capital": "吉隆坡"}
```

### search api

* GET方式搜索(queryString)

```
GET http://127.0.0.1:9200/countries/_search?q=*&pretty
```

注:`q=*`将匹配索引中的所有文档

输出:

```
{
   "took": 1,
   "timed_out": false,
   "_shards": {
      "total": 5,
      "successful": 5,
      "failed": 0
   },
   "hits": {
      "total": 10,
      "max_score": 1,
      "hits": [
         {
            "_index": "countries",
            "_type": "country",
            "_id": "4",
            "_score": 1,
            "_source": {
               "name": "日本",
               "capital": "东京"
            }
         },
         {
            "_index": "countries",
            "_type": "country",
            "_id": "_query",
            "_score": 1,
            "_source": {
               "query": {
                  "match_all": {
                     "name": "美国"
                  }
               }
            }
         },
         {
            "_index": "countries",
            "_type": "country",
            "_id": "5",
            "_score": 1,
            "_source": {
               "name": "印度",
               "capital": "新德里"
            }
         },
         {
            "_index": "countries",
            "_type": "country",
            "_id": "6",
            "_score": 1,
            "_source": {
               "name": "韩国",
               "capital": "首尔"
            }
         },
         {
            "_index": "countries",
            "_type": "country",
            "_id": "1",
            "_score": 1,
            "_source": {
               "name": "新加坡",
               "capital": "渥太华"
            }
         },
         {
            "_index": "countries",
            "_type": "country",
            "_id": "7",
            "_score": 1,
            "_source": {
               "name": "新加坡",
               "capital": "渥太华"
            }
         },
         {
            "_index": "countries",
            "_type": "country",
            "_id": "2",
            "_score": 1,
            "_source": {
               "name": "韩国",
               "capital": "首尔"
            }
         },
         {
            "_index": "countries",
            "_type": "country",
            "_id": "AU-6awteDgxJZYVN-E5I",
            "_score": 1,
            "_source": {
               "name": "韩国",
               "capital": "首尔"
            }
         },
         {
            "_index": "countries",
            "_type": "country",
            "_id": "8",
            "_score": 1,
            "_source": {
               "name": "德国",
               "capital": "柏林"
            }
         },
         {
            "_index": "countries",
            "_type": "country",
            "_id": "3",
            "_score": 1,
            "_source": {
               "name": "朝鲜",
               "capital": "平壤"
            }
         }
      ]
   }
}
```

* POST方式搜索(含请求体query)

```
POST http://127.0.0.1:9200/countries/_search?pretty
{
    "query": {
        "match_all": {}
    }
}
```

输出:

```
{
   "took": 1,
   "timed_out": false,
   "_shards": {
      "total": 5,
      "successful": 5,
      "failed": 0
   },
   "hits": {
      "total": 10,
      "max_score": 1,
      "hits": [
         {
            "_index": "countries",
            "_type": "country",
            "_id": "4",
            "_score": 1,
            "_source": {
               "name": "日本",
               "capital": "东京"
            }
         },
         {
            "_index": "countries",
            "_type": "country",
            "_id": "_query",
            "_score": 1,
            "_source": {
               "query": {
                  "match_all": {
                     "name": "美国"
                  }
               }
            }
         },
         {
            "_index": "countries",
            "_type": "country",
            "_id": "5",
            "_score": 1,
            "_source": {
               "name": "印度",
               "capital": "新德里"
            }
         },
         {
            "_index": "countries",
            "_type": "country",
            "_id": "6",
            "_score": 1,
            "_source": {
               "name": "韩国",
               "capital": "首尔"
            }
         },
         {
            "_index": "countries",
            "_type": "country",
            "_id": "1",
            "_score": 1,
            "_source": {
               "name": "新加坡",
               "capital": "渥太华"
            }
         },
         {
            "_index": "countries",
            "_type": "country",
            "_id": "7",
            "_score": 1,
            "_source": {
               "name": "新加坡",
               "capital": "渥太华"
            }
         },
         {
            "_index": "countries",
            "_type": "country",
            "_id": "2",
            "_score": 1,
            "_source": {
               "name": "韩国",
               "capital": "首尔"
            }
         },
         {
            "_index": "countries",
            "_type": "country",
            "_id": "AU-6awteDgxJZYVN-E5I",
            "_score": 1,
            "_source": {
               "name": "韩国",
               "capital": "首尔"
            }
         },
         {
            "_index": "countries",
            "_type": "country",
            "_id": "8",
            "_score": 1,
            "_source": {
               "name": "德国",
               "capital": "柏林"
            }
         },
         {
            "_index": "countries",
            "_type": "country",
            "_id": "3",
            "_score": 1,
            "_source": {
               "name": "朝鲜",
               "capital": "平壤"
            }
         }
      ]
   }
}
```

* 限定返回条目

```
POST http://127.0.0.1:9200/countries/_search?pretty
{
    "query": {
        "match_all": {}
    },
    "size": 1
}
```

`size`控制返回条目，默认为10

输出:

```
{
   "took": 1,
   "timed_out": false,
   "_shards": {
      "total": 5,
      "successful": 5,
      "failed": 0
   },
   "hits": {
      "total": 10,
      "max_score": 1,
      "hits": [
         {
            "_index": "countries",
            "_type": "country",
            "_id": "4",
            "_score": 1,
            "_source": {
               "name": "日本",
               "capital": "东京"
            }
         }
      ]
   }
}
```

* 分页(form,size)

```
POST http://127.0.0.1:9200/countries/_search?pretty
{
    "query": {
        "match_all": {}
    },
    "from": 2,
    "size": 2
}
```
使用`from`和`size`来翻页。其中`form`默认为`0`,`size`默认为`10`

* 排序(sort)

```
POST http://127.0.0.1:9200/countries/_search?pretty
{
    "query": {
        "match_all": {}
    },
    "sort": [
       {
          "name": {
             "order": "desc"
          }
       }
    ]
}
```
其中`name`为排序字段

### 限定返回字段

```
POST http://127.0.0.1:9200/countries/_search?pretty
{
    "query": {
        "match_all": {}
    },
    "_source": ["name"]
}
```
使用`_source`来限定返回的字段。这里只返回`name`



### 高级查询

* match_phrase

```
POST http://127.0.0.1:9200/countries/_search?pretty
{
    "query": {
        "match_phrase": {
           "name": "韩国"
        }
    }
}
```

* must

```
POST http://127.0.0.1:9200/countries/_search?pretty
{
    "query": {
        "bool": {
            "must": [
               {"match": {
                  "name": "日本"
               }},
               {"match": {
                  "capital": "东京"
               }}
            ]
        }
    }
}
```

* should

```
POST http://127.0.0.1:9200/countries/_search?pretty
{
    "query": {
        "bool": {
            "should": [
               {"match": {
                  "name": "日本"
               }},
               {"match": {
                  "name": "韩国"
               }}
            ]
        }
    }
}
```
* must_not
```
POST http://127.0.0.1:9200/countries/_search?pretty
{
    "query": {
        "bool": {
            "must_not": [
               {"match": {
                  "name": "日本"
               }},
               {"match": {
                  "name": "韩国"
               }}
            ]
        }
    }
}
```

* filter

```
POST http://127.0.0.1:9200/countries/_search?pretty
{
    "query": {
        "match_all": {}
    },
    "filter": {
        "term": {
           "capital": "东京"
        }
    }
}
```

* 聚合(aggs)

```
POST http://127.0.0.1:9200/countries/_search?pretty
{
  "size": 0,
  "aggs": {
    "group_by_name": {
      "terms": {
        "field": "name"
      }
    }
  }
}
```
按`name`统计分组文档数

输出:

```
{
   "took": 1,
   "timed_out": false,
   "_shards": {
      "total": 5,
      "successful": 5,
      "failed": 0
   },
   "hits": {
      "total": 10,
      "max_score": 0,
      "hits": []
   },
   "aggregations": {
      "group_by_name": {
         "doc_count_error_upper_bound": 0,
         "sum_other_doc_count": 0,
         "buckets": [
            {
               "key": "韩国",
               "doc_count": 3
            },
            {
               "key": "新加坡",
               "doc_count": 2
            },
            {
               "key": "印度",
               "doc_count": 1
            },
            {
               "key": "德国",
               "doc_count": 1
            },
            {
               "key": "日本",
               "doc_count": 1
            },
            {
               "key": "朝鲜",
               "doc_count": 1
            }
         ]
      }
   }
}
```

### 高亮查询(highlight)

```
POST http://127.0.0.1:9200/news/_search?q=李克强
{
    "query" : {
        match_all:{}
    },
    "highlight" : {
        "pre_tags" : ["<font color='red'>", "<b>", "<em>"],
        "post_tags" : ["</font>", "<b>", "</em>"],
        "fields" : [
            {"title" : {}},
            {"content" : {
                "fragment_size" : 350,
                "number_of_fragments" : 3,
                "no_match_size": 150
            }}
        ]
    }
}
```

```
POST http://127.0.0.1:9200/news/_search?q=李克强
{
    "query" : {
        match_all:{}
    },
    "highlight" : {
        "pre_tags" : ["<font color='red'><b><em>"],
        "post_tags" : ["</font><b></em>"],
        "fields" : [
            {"title" : {}},
            {"content" : {
                "fragment_size" : 350,
                "number_of_fragments" : 3,
                "no_match_size": 150
            }}
        ]
    }
}
```

### 删除索引

https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-delete-index.html

```
DELETE http://127.0.0.1:9200/news
```

### 创建索引

https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-create-index.html

```
PUT http://127.0.0.1:9200/news
```

### 创建或修改mapping

https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-put-mapping.html

```
PUT /{index}/_mapping/{type}
```

```
PUT http://127.0.0.1:9200/news/_mapping/article
{
  "article": {
    "properties": {
      "pubdate": {
        "type": "date",
        "format": "dateOptionalTime"
      },
      "author": {
        "type": "string"
      },
      "content": {
        "type": "string"
      },
      "id": {
        "type": "long"
      },
      "source": {
        "type": "string"
      },
      "title": {
        "type": "string"
      },
      "url": {
        "type": "string"
      }
    }
  }
}
```

### 查看mapping

https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-get-mapping.html


```
GET http://127.0.0.1:9200/_all/_mapping

GET http://127.0.0.1:9200/_mapping
```

```
GET http://127.0.0.1:9200/news/_mapping/article
```

输出:

```
{
  "news": {
    "mappings": {
      "article": {
        "properties": {
          "author": {
            "type": "string"
          },
          "content": {
            "type": "string"
          },
          "id": {
            "type": "long"
          },
          "pubdate": {
            "type": "date",
            "store": true,
            "format": "yyyy-MM-dd HH:mm:ss"
          },
          "source": {
            "type": "string"
          },
          "title": {
            "type": "string"
          },
          "url": {
            "type": "string"
          }
        }
      }
    }
  }
}
```

### 删除mapping

https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-delete-mapping.html

```
[DELETE] /{index}/{type}

[DELETE] /{index}/{type}/_mapping

[DELETE] /{index}/_mapping/{type}
```

```
DELETE http://127.0.0.1:9200/news/_mapping/article
```

### ansj分词器测试

http://127.0.0.1:9200/news/_analyze?analyzer=ansj_index&text=白居易

http://127.0.0.1:9200/news/_analyze?analyzer=ansj_index&text=我是中国人

http://127.0.0.1:9200/news/_analyze?analyzer=ansj_index&text=Elasticsearch是一个分布式、RESTful风格的搜索和数据分析引擎，能够解决不断涌现出的各种用例。作为ElasticStack的核心，它集中存储您的数据，帮助您发现意料之中以及意料之外的情况。

### ansj分词器查询

* 普通查询

http://127.0.0.1:9200/news/_search?q=白居易&analyzer=ansj_index&size=50

* 指定term查询

http://127.0.0.1:9200/news/_search?q=content:李世民&analyzer=ansj_index&size=50

http://127.0.0.1:9200/news/_search?q=title:李世民&analyzer=ansj_index&size=50

http://127.0.0.1:9200/news/_search?q=source:新华网&analyzer=ansj_index&size=50

* 其中`ansj_index`为在`elasticsearch.yml`文件中配置的`ansj`分词器