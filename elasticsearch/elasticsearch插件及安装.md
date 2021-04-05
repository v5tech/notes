# elasticsearch插件及安装

### 插件列表

http://www.searchtech.pro/elasticsearch-plugins

https://www.elastic.co/guide/en/elasticsearch/reference/current/modules-plugins.html

### 插件安装教程

https://www.elastic.co/guide/en/elasticsearch/reference/current/modules-plugins.html

* 在线安装

elasticsearch-head 插件在线安装 https://github.com/mobz/elasticsearch-head

```bash
plugin -install mobz/elasticsearch-head
```

bigdesk 插件在线安装 http://bigdesk.org/

```bash
plugin -install lukas-vlcek/bigdesk
```

elasticsearch-HQ 插件在线安装

http://www.elastichq.org/

http://www.elastichq.org/support_plugin.html

https://github.com/royrusso/elasticsearch-HQ

```bash
plugin -install royrusso/elasticsearch-HQ
```

* 从本地安装

```bash
plugin --url file:///C:\Users\Administrator\Desktop\elasticsearch-analysis-ik\target\releases\elasticsearch-analysis-ik-1.4.0.zip --install elasticsearch-analysis-ik
```

```bash
plugin --url file:///C:\Users\Administrator\Desktop\elasticsearch-analysis-ik\target\releases\elasticsearch-analysis-ik-1.4.0.zip --install elasticsearch-analysis-ik
```

* 推荐插件

https://github.com/NLPchina/elasticsearch-sql

https://github.com/medcl/elasticsearch-analysis-mmseg

https://github.com/medcl/elasticsearch-analysis-ik

https://github.com/karmi/elasticsearch-paramedic

https://github.com/polyfractal/elasticsearch-inquisitor

https://github.com/polyfractal/elasticsearch-segmentspy