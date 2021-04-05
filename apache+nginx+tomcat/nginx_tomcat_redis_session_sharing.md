# windows平台下搭建nginx tomcat redis分布式应用及session共享

> 环境准备: nginx1.8 + tomcat-7.0.67 + Redis-x64-3.0 + tomcat-redis-session-manager-2.0.0.jar

相关下载链接：

http://nginx.org/download/nginx-1.8.0.zip

http://mirror.bit.edu.cn/apache/tomcat/tomcat-7/v7.0.67/bin/apache-tomcat-7.0.67.zip

https://github.com/MSOpenTech/redis/releases/download/win-3.0.500/Redis-x64-3.0.500.zip

https://github.com/jcoleman/tomcat-redis-session-manager

# 编译tomcat-redis-session-manager

克隆https://github.com/jcoleman/tomcat-redis-session-manager 并编译最终得到


```
tomcat-redis-session-manager-2.0.0.jar
commons-pool2-2.2.jar
jedis-2.5.2.jar
```

拷贝上述jar到tomcat/lib目录下。

**tomcat-redis-session-manager的编译参考 http://www.cnblogs.com/lengfo/p/4260363.html**

# 配置tomcat

分别修改tomcat/conf/context.xml

```xml
<Context>
    <Valve className="com.orangefunction.tomcat.redissessions.RedisSessionHandlerValve" />
    <Manager className="com.orangefunction.tomcat.redissessions.RedisSessionManager"
           host="localhost"
           port="6379"
           database="0"
           maxInactiveInterval="60" />
</Context>
```

修改tomcat/conf/server.xml文件中的端口

# 配置nginx

nginx.conf

```bash
#user  nobody;
worker_processes  4;

#error_log  logs/error.log;
#error_log  logs/error.log  notice;
#error_log  logs/error.log  info;

#pid        logs/nginx.pid;

events {
    worker_connections  1024;
}

http {
    include       mime.types;
    default_type  application/octet-stream;

    #log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
    #                  '$status $body_bytes_sent "$http_referer" '
    #                  '"$http_user_agent" "$http_x_forwarded_for"';

    #access_log  logs/access.log  main;

    sendfile        on;
    #tcp_nopush     on;

    #keepalive_timeout  0;
    keepalive_timeout  65;

    #gzip  on;

    upstream site {  
        server localhost:8080; 
        server localhost:9080; 
    }

    server {
        listen       80;
        server_name  localhost;

        #charset koi8-r;

        #access_log  logs/host.access.log  main;

        location / {
            #root   html;
            index index.jsp index.html index.htm index.php;
            proxy_redirect          off;    
            proxy_set_header        Host            $host;    
            proxy_set_header        X-Real-IP       $remote_addr;    
            proxy_set_header        X-Forwarded-For $proxy_add_x_forwarded_for;    
            client_max_body_size    10m;    
            client_body_buffer_size 128k;    
            proxy_buffers           32 4k;  
            proxy_connect_timeout   3;    
            proxy_send_timeout      30;    
            proxy_read_timeout      30;   
            proxy_pass http://site;
        }

        #error_page  404              /404.html;

        # redirect server error pages to the static page /50x.html
        #
        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
        }

        # proxy the PHP scripts to Apache listening on 127.0.0.1:80
        #
        #location ~ \.php$ {
        #    proxy_pass   http://127.0.0.1;
        #}

        # pass the PHP scripts to FastCGI server listening on 127.0.0.1:9000
        #
        location ~ \.php$ {
            root           html;
            fastcgi_pass   127.0.0.1:9000;
            fastcgi_index  index.php;
            fastcgi_param  SCRIPT_FILENAME  $document_root$fastcgi_script_name;
            include        fastcgi_params;
        }
    }
}
```
**注意观察`upstream site`及`proxy_pass http://site;`配置**

# 测试

启动redis、tomcat、nginx访问nginx服务地址

# 参考文章

http://www.dwhd.org/20150604_095952.html

http://www.cnblogs.com/lengfo/p/4260363.html

https://dzone.com/articles/load-balance-tomcat-nginx-and

https://github.com/jcoleman/tomcat-redis-session-manager