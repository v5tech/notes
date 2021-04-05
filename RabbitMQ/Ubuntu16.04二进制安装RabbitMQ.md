# Ubuntu 16.04二进制安装RabbitMQ

### 下载二进制安装包

```bash
# 下载erlang安装包
wget http://packages.erlang-solutions.com/erlang/debian/pool/esl-erlang_20.3.8.6-1~ubuntu~xenial_amd64.deb
# 下载rabbitmq安装包
wget https://github.com/rabbitmq/rabbitmq-server/releases/download/v3.7.9/rabbitmq-server_3.7.9-1_all.deb
```

### 安装erlang

```bash
sudo dpkg -i esl-erlang_20.3.8.6-1~ubuntu~xenial_amd64.deb
```

安装过程中会报错，根据报错信息安装相关依赖

```bash
sudo apt-get -f install 
```

### 安装rabbitmq

```bash
sudo apt-get install socat
sudo dpkg -i rabbitmq-server_3.7.9-1_all.deb
```

### 安装rabbitmq_management

```bash
rabbitmq-plugins enable rabbitmq_management
```

### 集群搭建

[RabbitMQ 实战教程](https://github.com/ameizi/DevArticles/blob/master/RabbitMQ/RabbitMQ%20%E5%AE%9E%E6%88%98%E6%95%99%E7%A8%8B.md)

### 参考文档

https://www.rabbitmq.com/which-erlang.html

https://www.rabbitmq.com/install-debian.html#manual-installation

[RabbitMQ常用操作](https://github.com/ameizi/DevArticles/blob/master/RabbitMQ/RabbitMQ%E5%B8%B8%E7%94%A8%E6%93%8D%E4%BD%9C.md)

