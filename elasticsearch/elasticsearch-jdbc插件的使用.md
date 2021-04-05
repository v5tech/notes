# elasticsearch-jdbc插件的使用

> elasticsearch-1.7.1 + elasticsearch-jdbc-1.7.1

https://github.com/jprante/elasticsearch-jdbc

* 插件下载及安装

http://xbib.org/repository/org/xbib/elasticsearch/importer/elasticsearch-jdbc/1.7.1.0/elasticsearch-jdbc-1.7.1.0-dist.zip

* 解压及使用

该插件不需要安装，直接解压即可。

注：

使用该插件时需要禁用`shield`插件

windows平台下使用时需要修改其示例脚本

```bash
@echo off

set DIR=%~dp0
set LIB="%DIR%\..\lib\*"
set BIN="%DIR%\..\bin\*"

REM ???
echo {^
    "type" : "jdbc",^
    "jdbc" : {^
        "url" : "jdbc:mysql://localhost:3306/xiaoboedu",^
        "user" : "root",^
        "password" : "root",^
        "sql" :  "SELECT title,subtitle FROM course",^
        "autocommit" : true,^
        "treat_binary_as_string" : true,^
        "elasticsearch" : {^
             "cluster" : "elasticsearch",^
             "host" : "localhost",^
             "port" : 9300^
        },^
        "index" : "course",^
        "type" : "course"^
      }^
}^ | "%JAVA_HOME%\bin\java" -cp "%LIB%" -Dlog4j.configurationFile="file://%DIR%\log4j2.xml" "org.xbib.tools.Runner" "org.xbib.tools.JDBCImporter"
```
其具体使用及配置参见https://github.com/jprante/elasticsearch-jdbc

* 增量更新脚本

```bash
@echo off

set DIR=%~dp0
set LIB="%DIR%\..\lib\*"
set BIN="%DIR%\..\bin\*"

REM ???
echo {^
    "type" : "jdbc",^
    "jdbc" : {^
        "url" : "jdbc:mysql://localhost:3306/news",^
        "user" : "root",^
        "password" : "root",^
        "sql" :  [^
             {"statement":"SELECT title,content,url,source,author,pubdate FROM news"},^
             {^
                "statement":"SELECT title,content,url,source,author,pubdate FROM news where pubdate > ?",^
                "parameter" : [ "$metrics.lastexecutionstart" ]^
             }^
	],^
	"autocommit" : true,^
        "treat_binary_as_string" : true,^
        "elasticsearch" : {^
             "cluster" : "elasticsearch",^
             "host" : "localhost",^
             "port" : 9300^
        },^
        "index" : "news",^
        "type" : "article"^
      }^
}^ | "%JAVA_HOME%\bin\java" -cp "%LIB%" -Dlog4j.configurationFile="file://%DIR%\log4j2.xml" "org.xbib.tools.Runner" "org.xbib.tools.JDBCImporter"
```