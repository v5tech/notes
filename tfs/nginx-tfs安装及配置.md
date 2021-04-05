https://github.com/alibaba/nginx-tfs

### 1. 安装依赖库

```bash
sudo apt-get install cmake libpcre3 libpcre3-dev build-essential libssl-dev
```
### 2. 安装yajl

```bash
git clone git://github.com/lloyd/yajl
cd yajl
./configure
make
sudo make install
sudo ldconfig
```

### 3. 克隆nginx-tfs

```bash
ubuntu@s1:~/apps$ git clone https://github.com/alibaba/nginx-tfs.git
```

### 4. 编译安装nginx

```bash
./configure --prefix=/home/ubuntu/apps/nginx --add-module=/home/ubuntu/apps/nginx-tfs --with-http_ssl_module
make 
make install
```

### 5. 修改nginx.conf

```
#user  nobody;
worker_processes  1;

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

    tfs_upstream tfs_rc {
        server 192.168.64.136:8100;
        type ns;
        rcs_zone name=tfs1 size=128M;
        rcs_interface eth0;
        rcs_heartbeat lock_file=/logs/lk.file interval=10s;
    }

    server {
        listen       7500;
        server_name  192.168.64.136;
        tfs_keepalive max_cached=100 bucket_count=10;
        #tfs_log "pipe:/usr/sbin/cronolog -p 30min /path/to/nginx/logs/cronolog/%Y/%m/%Y-%m-%d-%H-%M-tfs_access.log";
        
        location / {
            tfs_pass tfs://tfs_rc;
        }
    }

    server {
        listen       80;
        server_name  localhost;

        #charset koi8-r;

        #access_log  logs/host.access.log  main;

        location / {
            root   html;
            index  index.html index.htm;
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
        #location ~ \.php$ {
        #    root           html;
        #    fastcgi_pass   127.0.0.1:9000;
        #    fastcgi_index  index.php;
        #    fastcgi_param  SCRIPT_FILENAME  /scripts$fastcgi_script_name;
        #    include        fastcgi_params;
        #}

        # deny access to .htaccess files, if Apache's document root
        # concurs with nginx's one
        #
        #location ~ /\.ht {
        #    deny  all;
        #}
    }


    # another virtual host using mix of IP-, name-, and port-based configuration
    #
    #server {
    #    listen       8000;
    #    listen       somename:8080;
    #    server_name  somename  alias  another.alias;

    #    location / {
    #        root   html;
    #        index  index.html index.htm;
    #    }
    #}


    # HTTPS server
    #
    #server {
    #    listen       443;
    #    server_name  localhost;

    #    ssl                  on;
    #    ssl_certificate      cert.pem;
    #    ssl_certificate_key  cert.key;

    #    ssl_session_timeout  5m;

    #    ssl_protocols  SSLv2 SSLv3 TLSv1;
    #    ssl_ciphers  HIGH:!aNULL:!MD5;
    #    ssl_prefer_server_ciphers   on;

    #    location / {
    #        root   html;
    #        index  index.html index.htm;
    #    }
    #}

}
```

### 6. 启动nginx

```bash
ubuntu@s1:~/apps/nginx$ sudo sbin/nginx
ubuntu@s1:~/apps/nginx$ ps -ef | grep nginx
```

### 7. 访问资源

http://192.168.64.136:7500/v1/tfs/T1naJTByhT1R4cSCrK

这里值得注意的是端口后面的url一定是/v1/,tfs三个字符可以用任意字符代替。例如一个空格字符，或者aaaa,bbb等，但至少需要存在一个字符。

### 8. 参考文档

http://ylw6006.blog.51cto.com/470441/1558631

https://github.com/alibaba/nginx-tfs