# linux系统时间设置

### 设置时区

```bash
sudo ln -s /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
```

### 同步时间

```bash
ntpdate ntp1.aliyun.com
```

阿里云提供如下的时间同步服务器

```
ntp1.aliyun.com
ntp2.aliyun.com
ntp3.aliyun.com
ntp4.aliyun.com
ntp5.aliyun.com
ntp6.aliyun.com
ntp7.aliyun.com
```

写个for循环检测如下:

```bash
for n in {1..7};do /usr/sbin/ntpdate ntp$n.aliyun.com;sleep 1;done
......
4 May 14:30:26 ntpdate[3544]: adjust time server 182.92.12.11 offset 0.021030 sec
4 May 14:30:33 ntpdate[3677]: adjust time server 120.25.115.19 offset 0.021037 sec
4 May 14:30:40 ntpdate[3880]: adjust time server 203.107.6.88 offset 0.011986 sec
4 May 14:30:48 ntpdate[4032]: adjust time server 203.107.6.88 offset 0.011444 sec
4 May 14:30:55 ntpdate[4235]: adjust time server 182.92.12.11 offset 0.008113 sec
4 May 14:31:02 ntpdate[4467]: adjust time server 203.107.6.88 offset 0.002121 sec
4 May 14:31:09 ntpdate[4630]: adjust time server 120.25.115.19 offset 0.000891 sec
```

企业中配置时间同步任务，尽量配置两个地址，防止某一个出问题

```bash
*/5 * * * * root /usr/sbin/ntpdate ntp1.aliyun.com &>/dev/null
*/5 * * * * root /usr/sbin/ntpdate ntp3.aliyun.com &>/dev/null
```

### crontab定时任务

* 列出`crontab`任务

```bash
crontab -l
*/5 * * * * root /usr/sbin/ntpdate ntp1.aliyun.com &>/dev/null
*/5 * * * * root /usr/sbin/ntpdate ntp3.aliyun.com &>/dev/null
```

* 添加`crontab`任务

```bash
crontab -e
*/5 * * * * root /usr/sbin/ntpdate ntp1.aliyun.com &>/dev/null
*/5 * * * * root /usr/sbin/ntpdate ntp3.aliyun.com &>/dev/null
```

或直接修改`/etc/crontab`文件

* `crond`服务

```bash
systemctl status crond # 查看crontab服务状态
systemctl start crond # 启动crontab服务
systemctl stop crond # 停止crontab服务
systemctl restart crond # 重启crontab服务
systemctl enable crond # 设置crontab服务开机自启动
```