# elasticsearch analysis ansj分词器的安装及使用

https://github.com/4onni/elasticsearch-analysis-ansj

ansj 分词器

https://github.com/NLPchina/ansj_seg

插件编译安装

### 1. 克隆源码
### 2. 修改pom文件配置
```bash
<elasticsearch.version>1.7.1</elasticsearch.version>
<dependency>
    <groupId>org.ansj</groupId>
    <artifactId>ansj_seg</artifactId>
    <classifier>min</classifier>
    <version>2.0.8</version>
    <scope>compile</scope>
</dependency>
```
### 3.编译插件
```bash
mvn assembly:assembly
```
### 4. 插件安装
```bash
elasticsearch-1.7.1\bin>plugin -u file:///C:\Users\Administrator\Desktop\elasticsearch-analysis-ansj\target\releases\elasticsearch-analysis-ansj-1.x.1-release.zip -i ansj
```
### 5. 配置ansj分词器
```yaml
index:
  analysis:
    analyzer:
      index_ansj:
          type: ansj_index
      query_ansj:
          type: ansj_query
      ik:
          alias: [news_analyzer_ik,ik_analyzer]
          type: org.elasticsearch.index.analysis.IkAnalyzerProvider
      mmseg:
          alias: [news_analyzer, mmseg_analyzer]
          type: org.elasticsearch.index.analysis.MMsegAnalyzerProvider
        
index.analysis.analyzer.default.type : "ansj_index"
```
详细配置可参考[elasticsearch.yml.example](https://github.com/4onni/elasticsearch-analysis-ansj/blob/master/elasticsearch.yml.example)

### 6. 测试及使用

* 索引分词
```bash
http://127.0.0.1:9200/articles/_analyze?analyzer=ansj_index&text=我们是中国人
```
注：其中`articles`是索引名称，除`articles`外的所有请求url参数部分均为固定写法。`analyzer=ansj_index`指定索引分词器，`text`后为要索引的内容
输出：
```json
{
  "tokens": [
    {
      "token": "我们",
      "start_offset": 0,
      "end_offset": 2,
      "type": "word",
      "position": 1
    },
    {
      "token": "是",
      "start_offset": 2,
      "end_offset": 3,
      "type": "word",
      "position": 2
    },
    {
      "token": "中国",
      "start_offset": 3,
      "end_offset": 5,
      "type": "word",
      "position": 3
    },
    {
      "token": "人",
      "start_offset": 5,
      "end_offset": 6,
      "type": "word",
      "position": 4
    }
  ]
}
```
* 查询分词
```bash
http://127.0.0.1:9200/articles/_analyze?analyzer=ansj_query&text=我们是中国人
```
注：其中`articles`是索引名称，除`articles`外的所有请求url参数部分均为固定写法。`analyzer=ansj_query`指定查询分词器，`text`后为要查询的内容
输出：
```json
{
  "tokens": [
    {
      "token": "我们",
      "start_offset": 0,
      "end_offset": 2,
      "type": "word",
      "position": 1
    },
    {
      "token": "是",
      "start_offset": 2,
      "end_offset": 3,
      "type": "word",
      "position": 2
    },
    {
      "token": "中国",
      "start_offset": 3,
      "end_offset": 5,
      "type": "word",
      "position": 3
    },
    {
      "token": "人",
      "start_offset": 5,
      "end_offset": 6,
      "type": "word",
      "position": 4
    }
  ]
}
```