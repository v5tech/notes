# 解决 Nginx 413 Request Entity Too Large 问题

使用 Spring Boot 文件上传的时候，前端使用 nginx 代理后端请求，出现如下 413 Request Entity Too Large 这个错误。

解决方法其实也十分简单，只需要在 nginx 配置文件里添加如下内容，重启 nginx，即可解决。

```nginx
server {  
    # ...
    listen       80;
    server_name  localhost;
    client_max_body_size 20M;
    # ...
}
```

重启 nginx，`nginx -s reload`，大功告成！
