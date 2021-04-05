# Linux查看硬件信息超强命令sar，以及可视化工具ksar

> 原文链接：[Linux查看硬件信息超强命令sar，以及可视化工具ksar](https://juejin.cn/post/6947470401135968286)

## 一、概述

sar（System Activity Reporter，系统活动情况报告）是Linux下系统运行状态统计工具，可从多方面对系统的活动进行报告，包括：文件的读写情况、系统调用的使用情况、磁盘I/O、CPU效率、内存使用状况、进程活动及IPC有关的活动等。算是一个万能的小能手。

## 二、安装

ubuntu下：

```bash
root@ubuntu:/home/peng# apt-get install sysstat
root@ubuntu:/home/peng# sar -r
Cannot open /var/log/sysstat/sa07: No such file or directory
Please check if data collecting is enabled
```

执行后会遇到以下错误，sa07中的07是当天的日期，原因是由于没有创建该文件。解决方法：

```bash
root@ubuntu:/home/peng# chmod o+w /etc/default/sysstat 
root@ubuntu:/home/peng# vim /etc/default/sysstat 
```

![ ](assets/f99e2ea36dca41b98d5ed982e7c39a72~tplv-k3u1fbpfcp-zoom-1.image)

![ ](assets/bb3151d0c9b6490ba75ef575cb8d15d3~tplv-k3u1fbpfcp-zoom-1.image)

## 三、 命令

**语法** 

![sar](assets/b1c4dd8d958843dabb266f01fab8b00c~tplv-k3u1fbpfcp-zoom-1.image)

1. 类型

就是我们要获取的是哪个类型的指标数据，这里的-n，代表的是监控一些网络信息

```bash
-a：文件读写情况
-A：所有报告的总和
-B：分页状况
-b：显示I/O和传送速率的统计信息
-c：输出进程统计信息，每秒创建的进程数
-d：块设备状况
-F [ MOUNT ]：文件系统统计信息
-H：交换空间利用率
-I { <中断> | SUM | ALL | XALL }：中断信息状况
-n：汇报网络情况
-P：设定CPU
-q：队列长度和平均负载
-R：输出内存页面的统计信息
-r [ ALL ]：输出内存和交换空间的统计信息
-S：交换空间利用率信息
-u [ ALL ]：输出CPU使用情况的统计信息
-v：输出inode、文件和其他内核表的统计信息
-W：输出系统交换活动信息
-w：任务创建与系统转换信息
-y：终端设备活动情况
```

2. 类型参数

有的类型带有参数，有的没有。这里的DEV，代表的是监控网卡信息

3. 间隔时间

每次报告的间隔时间（秒）

4. 次数

显示报告的次数。 如：时间间隔是2，采样次数是3，那么sar命令将阻塞12秒钟。

**帮助**

```bash
root@ubuntu:/home/peng# sar --help
Usage: sar [ options ] [ <interval> [ <count> ] ]
Options are:
[ -A ] [ -B ] [ -b ] [ -C ] [ -D ] [ -d ] [ -F [ MOUNT ] ] [ -H ] [ -h ]
[ -p ] [ -q ] [ -R ] [ -r [ ALL ] ] [ -S ] [ -t ] [ -u [ ALL ] ] [ -V ]
[ -v ] [ -W ] [ -w ] [ -y ] [ --sadc ]
[ -I { <int> [,...] | SUM | ALL | XALL } ] [ -P { <cpu> [,...] | ALL } ]
[ -m { <keyword> [,...] | ALL } ] [ -n { <keyword> [,...] | ALL } ]
[ -j { ID | LABEL | PATH | UUID | ... } ]
[ -f [ <filename> ] | -o [ <filename> ] | -[0-9]+ ]
[ -i <interval> ] [ -s [ <hh:mm[:ss]> ] ] [ -e [ <hh:mm[:ss]> ] ]
```

## 四、举例

Linux下的资源监控，主要有下面几种：有磁盘、CPU、网络、内存、IO等。

## 1. CPU信息

我们就先从cpu信息开始说起。作为计算机的大脑，作为一个指挥者，我们要监控它的一举一动。实际上，对于CPU有下面三种监控。

（1）CPU利用率

使用sar -u，我们看下它的执行结果。可以看到每种类型的使用情况，和top命令中的意义，是一样的。

```bash
root@ubuntu:/home/peng# sar -u 1 1
Linux 4.15.0-112-generic (ubuntu) 	03/07/2021 	_x86_64_	(1 CPU)

05:28:09 AM     CPU     %user     %nice   %system   %iowait    %steal     %idle
05:28:10 AM     all      1.01      0.00      2.02      0.00      0.00     96.97
Average:        all      1.01      0.00      2.02      0.00      0.00     96.97
```

**字段说明**

- %user #用户空间的CPU使用
- %nice 改变过优先级的进程的CPU使用率
- %system 内核空间的CPU使用率
- %iowait CPU等待IO的百分比
- %steal 虚拟机的虚拟机CPU使用的CPU
- %idle 空闲的CPU
- 在以上的显示当中，主要看%iowait和%idle，%iowait过高表示存在I/O瓶颈，即磁盘IO无法满足业务需求，如果%idle过低表示CPU使用率比较严重，需要结合内存使用等情况判断CPU是否瓶颈。

（2）报个每个CPU的使用状态：

```bash
root@ubuntu:/home/peng#  sar -p 1 3
Linux 4.15.0-112-generic (ubuntu) 	03/07/2021 	_x86_64_	(1 CPU)

05:29:21 AM     CPU     %user     %nice   %system   %iowait    %steal     %idle
05:29:22 AM     all      1.00      0.00      0.00      0.00      0.00     99.00
05:29:23 AM     all      1.02      0.00      0.00      0.00      0.00     98.98
05:29:24 AM     all      1.01      0.00      1.01      0.00      0.00     97.98
Average:        all      1.01      0.00      0.34      0.00      0.00     98.65
```

**字段说明**

- CPU: 所有CPU的统计
- %user 用户态的CPU使用统计
- %nice 更改过优先级的进程的CPU使用统计
- %iowait CPU等待IO数据的百分比
- %steal 虚拟机的vCPU占用的物理CPU的百分比
- %idle 空闲的CPU百分比

（3）CPU负载 使用sar -q，同样的，和top的参数意义是相似的。除了load值，它还显示了等待队列的长度，对于排查排队问题非常有帮助。

```bash
root@ubuntu:/home/peng# sar -q  1 1
Linux 4.15.0-112-generic (ubuntu) 	03/07/2021 	_x86_64_	(1 CPU)

05:30:20 AM   runq-sz  plist-sz   ldavg-1   ldavg-5  ldavg-15   blocked
05:30:21 AM         0       440      0.01      0.02      0.00         0
Average:            0       440      0.01      0.02      0.00         0
```

**字段说明**

- runq-sz 运行队列的长度（等待运行的进程数，每核的CP不能超过3个）
- plist-sz 进程列表中的进程（processes）和线程数（threads）的数量
- ldavg-1 最后1分钟的CPU平均负载，即将多核CPU过去一分钟的负载相加再除以核心数得出的平均值，5分钟和15分钟以此类推
- ldavg-5 最后5分钟的CPU平均负载
- ldavg-15 最后15分钟的CPU平均负载

（4）中断

使用sar -I，注意i是大写的。由于有不同的换算方式，所以中断的参数，分为默认、SUM、ALL等。

```bash
root@ubuntu:/home/peng# sar -I SUM 1 2
Linux 4.15.0-112-generic (ubuntu) 	03/07/2021 	_x86_64_	(1 CPU)

05:31:01 AM      INTR    intr/s
05:31:02 AM       sum    250.52
05:31:03 AM       sum    338.38
Average:          sum    294.90
```

（5）上下文切换

使用sar -w，它经常与监控swap交换分区的使用情况的sar -W(注意大小写)搞混，所以要注意。

```bash
root@ubuntu:/home/peng# sar -w  1
Linux 4.15.0-112-generic (ubuntu) 	03/07/2021 	_x86_64_	(1 CPU)

05:31:53 AM    proc/s   cswch/s
05:31:54 AM      0.00    433.67
05:31:55 AM      0.00    734.38
05:31:56 AM      0.00    582.65
05:31:57 AM      0.00    886.46
```

## 2. 内存信息

内存主要是分为下面这些部分，我们平常监控的，主要是物理内存、虚拟内存、内核等。

（1）内存利用率

使用sar -r命令。有些sar版本可能会有sar -R，但一般小写的就够了。

```bash
root@ubuntu:/home/peng# sar -r 1 1
Linux 4.15.0-112-generic (ubuntu) 	03/07/2021 	_x86_64_	(1 CPU)

05:32:54 AM kbmemfree kbmemused  %memused kbbuffers  kbcached  kbcommit   %commit  kbactive   kbinact   kbdirty
05:32:55 AM    281108   1736408     86.07    109040    675176   3345488    110.93    730964    591392         0
Average:       281108   1736408     86.07    109040    675176   3345488    110.93    730964    591392         0

```

**字段说明**

- kbmemfree：可用的空闲内存大小
- kbmemused：已使用的内存大小（不包含内核使用的内存）
- %memused：已使用内存的百分数
- kbbuffers ：内核缓冲区（buffer）使用的内存大小
- kbcached ：内核高速缓存（cache）数据使用的内存大小
- kbswpfree ：可用的空闲交换空间大小
- kbswpused：已使用的交换空间大小
- %swpused：已使用交换空间的百分数
- kbswpcad ：交换空间的高速缓存使用的内存大小
- kbcommit 保证当前系统正常运行所需要的最小内存，即为了确保内存不溢出而需要的最少内存（物理内存+Swap分区）
- commit 这个值是kbcommit与内存总量（物理内存+swap分区）的一个百分比的值

（2）swap交换分区

对于swap分区来说，就可以使用sar -S。效果如下。如果想要看交换分区的使用情况（非容量情况），就要切换到sar -W命令。

```bash
root@ubuntu:/home/peng# sar -S 1 1
Linux 4.15.0-112-generic (ubuntu) 	03/07/2021 	_x86_64_	(1 CPU)

05:34:15 AM kbswpfree kbswpused  %swpused  kbswpcad   %swpcad
05:34:16 AM    962556     35840      3.59      2808      7.83
Average:       962556     35840      3.59      2808      7.83
```

（3）内核使用情况

主要是使用sar -v命令。 v一般在别的命令中用作版本展示，sar命令用来输出slab区的一些信息，可以说是特立独行，不走寻常路。

```bash
root@ubuntu:/home/peng# sar -v  1
Linux 4.15.0-112-generic (ubuntu) 	03/07/2021 	_x86_64_	(1 CPU)

05:34:46 AM dentunusd   file-nr  inode-nr    pty-nr
05:34:47 AM     47183      6816     53938        17
05:34:48 AM     47183      6816     53938        17
```

**字段说明**

- dentunusd 在缓冲目录条目中没有使用的条目数量
- file-nr 被系统使用的文件句柄数量
- inode-nr 已经使用的索引数量
- pty-nr 使用的pty数量

（4）监控内存分页信息， 主要是使用sar -B命令。（注意他的发音！很牛掰！） 执行结果如下：

```bash
root@ubuntu:/home/peng# sar -B
Linux 4.15.0-112-generic (ubuntu) 	03/07/2021 	_x86_64_	(1 CPU)

01:51:34 AM  LINUX RESTART	(1 CPU)

01:55:01 AM  pgpgin/s pgpgout/s   fault/s  majflt/s  pgfree/s pgscank/s pgscand/s pgsteal/s    %vmeff
02:05:01 AM      0.00      0.29     23.98      0.00     10.50      0.00      0.00      0.00      0.00
02:15:01 AM      0.00      0.23      1.03      0.00      1.90      0.00      0.00      0.00      0.00
02:25:01 AM      0.00      0.47      1.73      0.00      2.71      0.00      0.00      0.00      0.00
```

（5）查看系统swap分区的统计信息：

```bash
root@ubuntu:/home/peng# sar -W 
Linux 4.15.0-112-generic (ubuntu) 	03/07/2021 	_x86_64_	(1 CPU)

01:51:34 AM  LINUX RESTART	(1 CPU)

01:55:01 AM  pswpin/s pswpout/s
02:05:01 AM      0.00      0.00
02:15:01 AM      0.00      0.00
02:25:01 AM      0.00      0.00
02:35:01 AM      0.00      0.00
02:45:01 AM      0.00      0.00
02:55:01 AM      0.00      0.00
```

**字段说明**

- pswpin/s 每秒从交换分区到系统的交换页面（swap page）数量
- pswpout/s 每秒从系统交换到swap的交换页面（swap page）的数量

（6）查看I/O和传递速率的统计信息

```bash
root@ubuntu:/home/peng# sar -b 
Linux 4.15.0-112-generic (ubuntu) 	03/07/2021 	_x86_64_	(1 CPU)

01:51:34 AM  LINUX RESTART	(1 CPU)

01:55:01 AM       tps      rtps      wtps   bread/s   bwrtn/s
02:05:01 AM      0.04      0.00      0.04      0.00      0.59
02:15:01 AM      0.03      0.00      0.03      0.00      0.47
```

**字段说明**

- tps 磁盘每秒钟的IO总数，等于iostat中的tps
- rtps 每秒钟从磁盘读取的IO总数
- wtps 每秒钟从写入到磁盘的IO总数
- bread/s 每秒钟从磁盘读取的块总数
- bwrtn/s 每秒钟此写入到磁盘的块总数

（7）磁盘使用详情统计

```bash
root@ubuntu:/home/peng# sar -d
Linux 4.15.0-112-generic (ubuntu) 	03/07/2021 	_x86_64_	(1 CPU)

01:51:34 AM  LINUX RESTART	(1 CPU)

01:55:01 AM       DEV       tps  rd_sec/s  wr_sec/s  avgrq-sz  avgqu-sz     await     svctm     %util
02:05:01 AM    dev7-0      0.00      0.00      0.00      0.00      0.00      0.00      0.00      0.00
02:05:01 AM    dev8-0      0.04      0.00      0.59     13.54      0.00      0.00      0.00      0.00
```

**字段说明**

- DEV 磁盘设备的名称，如果不加-p，会显示dev253-0类似的设备名称，因此加上-p显示的名称更直接
- tps：每秒I/O的传输总数
- rd_sec/s 每秒读取的扇区的总数
- wr_sec/s 每秒写入的扇区的 总数
- avgrq-sz 平均每次次磁盘I/O操作的数据大小（扇区）
- avgqu-sz 磁盘请求队列的平均长度
- await 从请求磁盘操作到系统完成处理，每次请求的平均消耗时间，包括请求队列等待时间，单位是毫秒（1秒等于1000毫秒），等于寻道时间+队列时间+服务时间
- svctm I/O的服务处理时间，即不包括请求队列中的时间
- %util I/O请求占用的CPU百分比，值越高，说明I/O越慢

## 3. I/O信息

IO信息监控，同样是一个响亮的sar -b，不过这里的b，变成了小写的。

```bash
root@ubuntu:/home/peng# sar -b 1 2
Linux 4.15.0-112-generic (ubuntu) 	03/07/2021 	_x86_64_	(1 CPU)

05:41:22 AM       tps      rtps      wtps   bread/s   bwrtn/s
05:41:23 AM      0.00      0.00      0.00      0.00      0.00
05:41:24 AM      2.06      0.00      2.06      0.00     65.98
Average:         1.02      0.00      1.02      0.00     32.65
```

**字段说明**

- tps 磁盘每秒钟的IO总数，等于iostat中的tps
- rtps 每秒钟从磁盘读取的IO总数
- wtps 每秒钟从写入到磁盘的IO总数
- bread/s 每秒钟从磁盘读取的块总数
- bwrtn/s 每秒钟此写入到磁盘的块总数

sar -d命令非常类似于iostat命令，结果更多。

```bash
root@ubuntu:/home/peng# sar -d   1
Linux 4.15.0-112-generic (ubuntu) 	03/07/2021 	_x86_64_	(1 CPU)

05:42:03 AM       DEV       tps  rd_sec/s  wr_sec/s  avgrq-sz  avgqu-sz     await     svctm     %util
05:42:04 AM    dev7-0      0.00      0.00      0.00      0.00      0.00      0.00      0.00      0.00
05:42:04 AM    dev8-0      0.00      0.00      0.00      0.00      0.00      0.00      0.00      0.00
```

**字段说明**

- DEV 磁盘设备的名称，如果不加-p，会显示dev253-0类似的设备名称，因此加上-p显示的名称更直接
- tps：每秒I/O的传输总数
- rd_sec/s 每秒读取的扇区的总数
- wr_sec/s 每秒写入的扇区的 总数
- avgrq-sz 平均每次次磁盘I/O操作的数据大小（扇区）
- avgqu-sz 磁盘请求队列的平均长度
- await 从请求磁盘操作到系统完成处理，每次请求的平均消耗时间，包括请求队列等待时间，单位是毫秒（1秒等于1000毫秒），等于寻道时间+队列时间+服务时间
- svctm I/O的服务处理时间，即不包括请求队列中的时间
- %util I/O请求占用的CPU百分比，值越高，说明I/O越慢

## 4. 网络信息

### (1) 统计网络信息

```bash
 sar -n 
```

接下来，我们看最复杂的网络信息。说它复杂，是因为它的参数非常的多，比如上面说到的DEV，就表示的网络流量。

要命的是，这些参数的每个输出，还都不是一样的。可能是26个字母已经无法涵盖这么多参数了吧，所以sar命令统一把它加在了sar -n下面。好在我们平常使用的时候，只和DEV参数打交道既可以了。

\#sar -n选项使用6个不同的开关：DEV，EDEV，NFS，NFSD，SOCK，IP，EIP，ICMP，EICMP，TCP，ETCP，UDP，SOCK6，IP6，EIP6，ICMP6，EICMP6和UDP6 ，DEV显示网络接口信息，EDEV显示关于网络错误的统计数据，NFS统计活动的NFS客户端的信息，NFSD统计NFS服务器的信息，SOCK显示套接字信息，ALL显示所有5个开关。它们可以单独或者一起使用。

### (2) 每间隔1秒统计一次，总计统计1次

```bash
root@ubuntu:/home/peng# sar -n DEV 1 1
Linux 4.15.0-112-generic (ubuntu) 	03/07/2021 	_x86_64_	(1 CPU)

05:45:36 AM     IFACE   rxpck/s   txpck/s    rxkB/s    txkB/s   rxcmp/s   txcmp/s  rxmcst/s   %ifutil
05:45:37 AM     ens33      0.00      0.00      0.00      0.00      0.00      0.00      0.00      0.00
05:45:37 AM        lo      0.00      0.00      0.00      0.00      0.00      0.00      0.00      0.00

Average:        IFACE   rxpck/s   txpck/s    rxkB/s    txkB/s   rxcmp/s   txcmp/s  rxmcst/s   %ifutil
Average:        ens33      0.00      0.00      0.00      0.00      0.00      0.00      0.00      0.00
Average:           lo      0.00      0.00      0.00      0.00      0.00      0.00      0.00      0.00
```

**字段说明** 下面的average是在多次统计后的平均值

- IFACE 本地网卡接口的名称
- rxpck/s 每秒钟接受的数据包
- txpck/s 每秒钟发送的数据库
- rxKB/S 每秒钟接受的数据包大小，单位为KB
- txKB/S 每秒钟发送的数据包大小，单位为KB
- rxcmp/s 每秒钟接受的压缩数据包
- txcmp/s 每秒钟发送的压缩包
- rxmcst/s 每秒钟接收的多播数据包

### (3) 统计网络设备通信失败信息：

```bash
root@ubuntu:/home/peng# sar -n EDEV  1 1
Linux 4.15.0-112-generic (ubuntu) 	03/07/2021 	_x86_64_	(1 CPU)

05:46:22 AM     IFACE   rxerr/s   txerr/s    coll/s  rxdrop/s  txdrop/s  txcarr/s  rxfram/s  rxfifo/s  txfifo/s
05:46:23 AM     ens33      0.00      0.00      0.00      0.00      0.00      0.00      0.00      0.00      0.00
05:46:23 AM        lo      0.00      0.00      0.00      0.00      0.00      0.00      0.00      0.00      0.00

Average:        IFACE   rxerr/s   txerr/s    coll/s  rxdrop/s  txdrop/s  txcarr/s  rxfram/s  rxfifo/s  txfifo/s
Average:        ens33      0.00      0.00      0.00      0.00      0.00      0.00      0.00      0.00      0.00
Average:           lo      0.00      0.00      0.00      0.00      0.00      0.00      0.00      0.00      0.00
```

**字段说明**

- IFACE 网卡名称
- rxerr/s 每秒钟接收到的损坏的数据包
- txerr/s 每秒钟发送的数据包错误数
- coll/s 当发送数据包时候，每秒钟发生的冲撞（collisions）数，这个是在半双工模式下才有
- rxdrop/s 当由于缓冲区满的时候，网卡设备接收端每秒钟丢掉的网络包的数目
- txdrop/s 当由于缓冲区满的时候，网络设备发送端每秒钟丢掉的网络包的数目
- txcarr/s 当发送数据包的时候，每秒钟载波错误发生的次数
- rxfram 在接收数据包的时候，每秒钟发生的帧对其错误的次数
- rxfifo 在接收数据包的时候，每秒钟缓冲区溢出的错误发生的次数
- txfifo 在发生数据包 的时候，每秒钟缓冲区溢出的错误发生的次数

### (4) 统计socket连接信息

```bash
root@ubuntu:/home/peng# sar -n SOCK 1 1
Linux 4.15.0-112-generic (ubuntu) 	03/07/2021 	_x86_64_	(1 CPU)

05:47:21 AM    totsck    tcpsck    udpsck    rawsck   ip-frag    tcp-tw
05:47:22 AM      1393         2         6         0         0         0
Average:         1393         2         6         0         0         0
```

**字段说明**

- totsck 当前被使用的socket总数
- tcpsck 当前正在被使用的TCP的socket总数
- udpsck 当前正在被使用的UDP的socket总数
- rawsck 当前正在被使用于RAW的skcket总数
- ip-frag 当前的IP分片的数目
- tcp-tw TCP套接字中处于TIME-WAIT状态的连接数量

使用FULL关键字，相当于上述DEV、EDEV和SOCK三者的综合。

### (5) TCP连接的统计

```bash
root@ubuntu:/home/peng# sar -n TCP 1 3
Linux 4.15.0-112-generic (ubuntu) 	03/07/2021 	_x86_64_	(1 CPU)

05:48:05 AM  active/s passive/s    iseg/s    oseg/s
05:48:06 AM      0.00      0.00      0.00      0.00
05:48:07 AM      0.00      0.00      0.00      0.00
05:48:08 AM      0.00      0.00      0.00      0.00
Average:         0.00      0.00      0.00      0.00
```

**字段说明**

- active/s 新的主动连接
- passive/s 新的被动连接
- iseg/s 接受的段
- oseg/s 输出的段

### (6) sar -n 使用总结

1. DEV 网络接口统计信息
2. EDEV 网络接口错误
3. NFS NFS 客户端
4. NFSD NFS 服务器
5. SOCK Sockets (套接字) (v4)套接字使用
6. IP IP 流 (v4) IP数据报统计信息
7. EIP IP 流 (v4) (错误) IP错误统计信息
8. ICMP ICMP 流 (v4)
9. EICMP ICMP 流 (v4) (错误)
10. TCP TCP 流 (v4) TCP统计信息
11. ETCP TCP 流 (v4) (错误)TCP错误统计信息
12. UDP UDP 流 (v4)
13. SOCK6 Sockets (套接字) (v6)
14. IP6 IP 流 (v6)
15. EIP6 IP 流 (v6) (错误)
16. ICMP6 ICMP 流 (v6)
17. EICMP6 ICMP 流 (v6) (错误)
18. UDP6 UDP 流 (v6)

## 五、ksar

Ksar可以用来分析系统性能数据，其优势在于不需要单独去收集性能数据，系统自带有sar包，通过命令转换即可使用Ksar展现。

安装该软件需要先安装java，如果已经安装调到第5步。

### 1. 下载java

jdk-8u202-linux-x64.tar.gz

### 2. 解压

拷贝jdk-8u202-linux-x64.tar.gz到ubuntu的/home/peng/jdk下

![](assets/444ce6de2bb0400592f7258bc5595a64~tplv-k3u1fbpfcp-zoom-1.image)

```bash
tar -zxvf jdk-8u202-linux-x64.tar.gz
```

### 3. 设置环境变量

```bash
$ sudo vim /etc/profile
```

在文件尾加入以下内容

```bash
#set java env
export JAVA_HOME=/home/peng/jdk/jdk1.8.0_202
export JRE_HOME=${JAVA_HOME}/jre    
export CLASSPATH=.:${JAVA_HOME}/lib:${JRE_HOME}/lib    
export PATH=${JAVA_HOME}/bin:$PATH
```

使环境变量生效

```bash
$ sudo source /etc/profile
```

### 4. 测试java

![ ](assets/87a93bfa9d444d9abdd1327e361c9155~tplv-k3u1fbpfcp-zoom-1.image) 

java版本为1.8.0_202

### 5. 下载ksar源码

```bash
wget http://jaist.dl.sourceforge.net/project/ksar/ksar/5.0.6/ksar-5.0.6.zip
unzip ksar-5.0.6.zip
```

然后解压并进入源码根目录，执行脚本：

```bash
sh run.sh 
```

可启动此软件

![ksar](assets/7b5cd42ee1454260b9713c948d295929~tplv-k3u1fbpfcp-zoom-1.image)

### 6. 操作

执行命令，点击Data->Run local command 

![](assets/89570f2d646344ea83271db315c2bcec~tplv-k3u1fbpfcp-zoom-1.image)

可以执行以下命令：

```bash
sar -A
```

![](assets/8a51db2a78dc4b418cb3bcbadaa54376~tplv-k3u1fbpfcp-zoom-1.image)

点击对应的硬件信息，就可以以图形化形式查看对应的硬件信息内容。 

![ksar](assets/c93afdb23d9f483e82e63dd817e6f350~tplv-k3u1fbpfcp-zoom-1.image)