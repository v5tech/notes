# nginx流量镜像ngx_http_mirror_module

## 背景

nginx官网公布了nginx1.13.4最新的ngx_http_mirror_module模块，利用mirror模块，可以将线上实时访问流量拷贝至其他环境，基于这些流量可以做版本发布前的预先验证，进行流量放大后的压测等等。

## mirror模块配置

mirror模块配置分为两部分，源地址和镜像地址，配置位置可以为nginx配置文件的http, server, location上下文，配置示例为：

```
# original配置

location / {
    mirror /mirror;
    mirror_request_body off;
    proxy_pass http://127.0.0.1:9502;
}
```

```
# mirror配置

location /mirror {
    internal;
    proxy_pass http://127.0.0.1:8081$request_uri;
    proxy_set_header X-Original-URI $request_uri;
}
```

* original配置

location /                    指定源uri为/

mirror /mirror                指定镜像uri为/mirror

mirror_request_body off | on  指定是否镜像请求body部分，此选项与proxy_request_buffering、fastcgi_request_buffering、scgi_request_buffering和 uwsgi_request_buffering冲突，一旦开启mirror_request_body为on，则请求自动缓存;

proxy_pass                    指定上游server的地址

* mirror配置

internal                   指定此location只能被“内部的”请求调用，外部的调用请求会返回”Not found” (404)

proxy_pass                 指定上游server的地址

proxy_set_header           设置镜像流量的头部


nginx支持配置多个mirror uri，示例为:

```
location / {
    mirror /mirror1;
    mirror /mirror2;
    mirror_request_body off;
    proxy_pass http://127.0.0.1:9502;
}

location /mirror1 {
    internal;
    proxy_pass http://127.0.0.1:8081$request_uri;
}

location /mirror2 {
    internal;
    proxy_pass http://127.0.0.1:9081$request_uri;
}
```

