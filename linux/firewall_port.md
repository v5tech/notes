# Linux Firewall & Port Configuration

## 前言

最近在配服务器环境的时候，动不动遇到端口无法访问的情况，折腾了老半天，经过分析判断最后基本是防火墙把网络端口给拦了……

于是就来简要记录一下Linux下的防火墙配置，以及端口开放、关闭的配置方法。

整理自网络啦。

------

## CentOS

CentOS下用的是`firewall-cmd`

### 常用命令

#### 启动firewall

```bash
systemctl start firewalld.service
```

#### 开机自启firewall

```bash
systemctl enable firewalld.service
```

#### 查看开机自启是否设置成功

```bash
systemctl is-enabled firewalld.service; echo $?
```

> 返回如下则开启成功
> ```none
> >enabled
> >0
> ```

#### 重启firewall

```bash
systemctl restart firewalld.service
```

#### 关闭firewall

```bash
systemctl stop firewalld.service
```

#### 检测firewall状态

```bash
firewall-cmd --state
```

> 例如正在运行：
> ```bash
> [root@localhost ~]# firewall-cmd --state
> running
> ```
>
> 不在运行则为`not running`

### 端口配置

#### 查看开放（监听）的端口

```bash
firewall-cmd --list-ports
```

> example：
> ```bash
> [root@localhost ~]# firewall-cmd --list-ports
> 80/tcp 7650/tcp 3306/tcp
> ```
>
> 或

```bash
netstat -ntlp
```

> 如果没有的话（*centos7默认没有 netstat 命令*），先安装`net-tools`。
> ```bash
> >yum install -y net-tools
> ```
> ```bash
> >[root@localhost ~]# netstat -ntlp
> >Active Internet connections (only servers)
> >Proto Recv-Q Send-Q Local Address           Foreign Address         State       PID/Program name
> >tcp        0      0 0.0.0.0:3306            0.0.0.0:*               LISTEN      14301/mysqld
> >tcp        0      0 0.0.0.0:22              0.0.0.0:*               LISTEN      1840/sshd
> >tcp6       0      0 :::6011                 :::*                    LISTEN      8037/sshd: root@pts
> >tcp6       0      0 :::22                   :::*                    LISTEN      1840/sshd
> 
> >……
> ```
>
> 其中`tcp`为ipv4，`tcp6`为ipv6.

遇到过几次没有开放firewall端口时，只有`tcp6`而无`tcp`的情况，导致访问失败。放行端口后访问正常。

还遇到过只能从本地 127.0.0.1 或 localhost（实际上二者一样的，默认写在了host里）访问，通过（网卡的）IP地址无法访问的情况。表现在 Local Address 上是`127.0.0.1:xxxx`，通过修改该程序的网络设置把Address修改为对应的IP或 `0.0.0.0`，即可解决访问的问题。

------

#### **以下操作之后都要执行 重载 或 重启 才能生效**！！！

#### 开启特定端口

```bash
firewall-cmd --zone=public --add-port=80/tcp --permanent  # 开放端口
systemctl restart firewalld.service    # 重启firewall
```

其中， 参数含义为
`--zone` 作用域
`--add-port=80/tcp` 添加端口，格式为：端口/通讯协议（`tcp`或`udp`）
`--permanent` 永久生效，没有此参数重启后失效

> ```bash
> [root@localhost ~]# firewall-cmd --zone=public --add-port=80/tcp --permanent
> success
> ```
>
> 成功则返回 success

#### 开放多个端口

```bash
firewall-cmd --zone=public --add-port=8080-8083/tcp --permanent
firewall-cmd --reload   # 配置立即生效
```

#### 关闭特定端口

```bash
firewall-cmd --zone=public --remove-port=5672/tcp --permanent  #关闭5672端口
firewall-cmd --reload   # 配置立即生效
# 也可以用restart
```

#### 针对某个 IP 开放端口

```bash
firewall-cmd --permanent --add-rich-rule="rule family="ipv4" source  address="192.168.142.166" port protocol="tcp" port="6379" accept"
firewall-cmd --permanent --add-rich-rule="rule family="ipv4" source address="192.168.0.233" accept"
```

#### 删除某个 IP

```bash
firewall-cmd --permanent --remove-rich-rule="rule family="ipv4" source address="192.168.1.51" accept"
```

#### 针对一个 IP 段访问

```bash
firewall-cmd --permanent --add-rich-rule="rule family="ipv4" source address="192.168.0.0/16" accept"
firewall-cmd --permanent --add-rich-rule="rule family="ipv4" source  address="192.168.1.0/24" port protocol="tcp" port="9200" accept"
```

------

## Ubuntu

Ubuntu 系统默认提供了一个基于`iptables`之上的防火墙工具`ufw`。

### 常用命令

#### 安装`ufw`

（默认已安装）

```bash
sudo apt-get install ufw
```

#### 启用`ufw`，且在系统启动时自启

```bash
sudo ufw enable
```

#### 关闭`ufw`

```bash
sudo ufw disable
```

#### 查看防火墙状态

```bash
sudo ufw status
```

#### 日志

系统日志保存于`/var/log/ufw.log`。LEVEL指定不同的级别 ，默认级别是‘低’

```bash
sudo ufw logging on|off  LEVEL   
```

### 端口相关

#### 设置默认策略

（比如 “mostly open” vs “mostly closed”）

```bash
sudo ufw default allow  # 允许所有外部对本机的访问，且本机访问外部正常。
sudo ufw default deny   # 关闭所有外部对本机的访问，但本机访问外部正常。
```

#### 打开某个端口

```bash
sudo ufw allow smtp　   # 允许所有的外部IP访问本机的25/tcp （smtp）端口
sudo ufw allow 22/tcp   # 允许所有的外部IP访问本机的22/tcp （ssh）端口
sudo ufw allow 53       # 允许外部访问53端口（tcp/udp）
sudo ufw allow from 192.168.1.100  # 允许此IP访问所有的本机端口
sudo ufw allow proto udp 192.168.0.1 port 53 to 192.168.0.2 port 53
sudo ufw allow in on eth0 from 192.168.0.0/16  # 允许来自192.168.0.0-192.168.255.255的数据通过eth0网卡进入主机
sudo ufw allow out on eth1 to 10.0.0.0/8   # 允许指向10.0.0.0-10.255.255.255的数据通过eth1网卡从本机发出
```

可以用`less /etc/services`列出所有服务信息，其中包括该服务使用了哪个端口和哪种协议

#### 关闭端口

```bash
sudo ufw deny smtp          # 禁止外部访问smtp服务
sudo ufw delete allow smtp  # 删除上面建立的某条规则
sudo ufw delete allow 80    # 禁止外部访问80端口
sudo ufw deny proto tcp from 10.0.0.0/8 to 192.168.0.1 port 25  # 拒绝来自10.0.0.0/8域tcp协议指向192.168.0.1端口25的数据进入本机
```

#### 路由

```bash
sudo ufw route allow in on eth1 out on eth2   # 允许经eth1进入，eth2发出的数据经本机路由
```

------

## 进程相关命令

### 查看进程的详细信息

```bash
[root@localhost ~]# ps 1
  PID TTY      STAT   TIME COMMAND
    1 ?        Ss     7:25 /usr/lib/systemd/systemd --switched-root --system --deserialize 22
```

### 杀死进程

```bash
kill -9 1234
```

1234为对应的PID

> 只有第9种信号(SIGKILL)才可以无条件终止进程，其他信号进程都有权利忽略。
>
> 下面是常用的信号：
> HUP 1 终端断线
> INT 2 中断（同 Ctrl + C）
> QUIT 3 退出（同 Ctrl + \）
> TERM 15 终止
> KILL 9 强制终止
> CONT 18 继续（与STOP相反， fg/bg命令）
> STOP 19 暂停（同 Ctrl + Z）

或者

**杀死指定名字的进程**（kill processes by name），可以批量结束某个服务程序带有的全部进程。

1. 杀死所有同名进程

   ```bash
   killall nginx
   killall -9 bash
   ```

2. 向进程发送指定信号

   ```bash
   killall -TERM ngixn  # 或者  killall -KILL nginx
   ```

   > 命令参数：
   > ```none
   > -Z 只杀死拥有scontext 的进程
   > -e 要求匹配进程名称
   > -I 忽略小写
   > -g 杀死进程组而不是进程
   > -i 交互模式，杀死进程前先询问用户
   > -l 列出所有的已知信号名称
   > -q 不输出警告信息
   > -s 发送指定的信号
   > -v 报告信号是否成功发送
   > -w 等待进程死亡
   > --help 显示帮助信息
   > --version 显示版本显示
   > ```

------

## Reference

https://blog.csdn.net/zll_0405/article/details/81208606

https://blog.csdn.net/bigdata_mining/article/details/80699180

etc.

```
来源: MiaoTony's小窝
文章作者: MiaoTony
文章链接: https://miaotony.xyz/2020/01/29/Server_firewall/
本文章著作权归作者MiaoTony所有，任何形式的转载都请注明出处。
```