# 和sar比起来，其他Linux命令都是猹

> 原文链接：[和sar比起来，其他Linux命令都是猹](https://juejin.cn/post/6916300737194491912)

我决定把今年装x的机会，留给`sar`命令。它是一个Linux下的监控工具，一直站在鄙视链的顶端。之所以让人望而生畏，主要是由于它繁多的参数。但，这么强大的命令，并非无章可循，实际上是非常简单的。

今天就和xjjdog一起，寻觅sar命令的隐秘之处，共同沾得帝王之气，以便傲视群cmd，彰显自己侧漏的霸气！

![img](assets/b1d3490777d540ff8a5cdd471b0b50ab~tplv-k3u1fbpfcp-watermark.image)

sar命令很简单，它的参数主要分为四部分。其中，第二部分和第三、四部分，是可选的，也就是说，最终要的参数，就剩下一个，那就是类型。这个参数的值非常的多，我们暂且放在一边，看一下以上命令的简单意义。

1. 类型，也就是我们要获取的是哪个类型的指标数据，这里的`-n`，代表的是监控一些网络信息
2. 类型参数，有的类型带有参数，有的没有。这里的`DEV`，代表的是监控网卡信息
3. 时间间隔，表示多少`秒`采样一次数据，这里的`1`就是1秒
4. 次数，表示采样的次数。比如时间间隔是3，采样次数是4，那么sar命令将会阻塞12秒钟

我们来看小小偷窥一下它的输出。

```bash
# sar -n DEV 1 2
03:10:29 PM     IFACE   rxpck/s   txpck/s    rxkB/s    txkB/s   rxcmp/s   txcmp/s  rxmcst/s   %ifutil
03:10:30 PM        lo     30.00     30.00      2.09      2.09      0.00      0.00      0.00      0.00
03:10:30 PM      eth0      6.00      2.00      0.38      0.32      0.00      0.00      0.00      0.00

03:10:30 PM     IFACE   rxpck/s   txpck/s    rxkB/s    txkB/s   rxcmp/s   txcmp/s  rxmcst/s   %ifutil
03:10:31 PM        lo     39.00     39.00      2.95      2.95      0.00      0.00      0.00      0.00
03:10:31 PM      eth0     11.00     12.00      0.72      5.26      0.00      0.00      0.00      0.00

Average:        IFACE   rxpck/s   txpck/s    rxkB/s    txkB/s   rxcmp/s   txcmp/s  rxmcst/s   %ifutil
Average:           lo     33.00     33.00      2.38      2.38      0.00      0.00      0.00      0.00
Average:         eth0      9.33      8.33      0.60      2.39      0.00      0.00      0.00      0.00
```

非常非常规整的二维数组，不像top命令那种张狂的显示(`top -b -n 1`可以输出当前信息)。可以很方便的使用`sed`，`awk`这样的工具进行处理。

了解这命令构成的各个部分，我们就可以放心大胆的来看具体的参数，都有哪些了。能不能抓到这只猹，在此一举。

之所以说sar命令，站在鄙视链的顶端，那是因为它的参数是非常丰富的。我们再也不需要各种`iostat`、`top`、`vmstat`等五花八门的命令，只需要一个sar，就能统一天下。

Linux下的资源监控，不外乎下面几种。有`磁盘`、`CPU`、`网络`、`内存`、`IO`等。不好意思，sar都能监控到，就是这么目空一切。 

![img](assets/8b36536b670d4806a3132a1670d8f2d1~tplv-k3u1fbpfcp-watermark.image)

接下来，我们就来漫游一小把。

## 1. CPU信息

我们就先从cpu信息开始说起。作为计算机的大脑，作为一个指挥者，我们要监控它的一举一动。实际上，对于CPU有下面三种监控。

![img](assets/fb5082f93259453f9b0c74477867626c~tplv-k3u1fbpfcp-watermark.image)

（1）利用率，使用`sar -u`，我们看下它的执行结果。可以看到每种类型的使用情况，和top命令种的意义，是一样的。

```bash
# sar -u 1 1
03:37:39 PM     CPU     %user     %nice   %system   %iowait    %steal     %idle
03:37:40 PM     all      0.25      0.50      0.50      0.00      0.00     98.75
Average:        all      0.25      0.50      0.50      0.00      0.00     98.75
```

（2）负载，使用`sar -q`，同样的，和top的参数意义是相似的。除了load值，它还显示了等待队列的长度，对于排查排队问题非常有帮助。

```bash
# sar -q  1 1
03:40:15 PM   runq-sz  plist-sz   ldavg-1   ldavg-5  ldavg-15   blocked
03:40:16 PM         0       468      0.02      0.04      0.00         0
Average:            0       468      0.02      0.04      0.00         0
```

（3）中断，使用`sar -I`，注意i是大写的。由于有不同的换算方式，所以中断的参数，分为`默认`、`SUM`、`ALL`等。

```bash
# sar -I SUM 1 2
03:44:36 PM      INTR    intr/s
03:44:37 PM       sum   1118.00
03:44:38 PM       sum   1024.00
Average:          sum   1071.00
```

（4）上下文切换，使用`sar -w`，它经常与监控swap交换分区的使用情况的`sar -W`搞混，所以要注意。

```bash
# sar -w  1
04:08:33 PM    proc/s   cswch/s
04:08:34 PM      0.00   1686.00
```

## 2. 内存信息

![img](assets/089bee2fe75a44c1a13c2b51714ee88e~tplv-k3u1fbpfcp-watermark.image)

看完了CPU就再看内存。CPU跑满了机器可能表现就是慢点，内存跑满了可是要死人的。

内存主要是分为下面这些部分，我们平常监控的，主要是`物理内存`、`虚拟内存`、`内核`等。

（1）内存利用率，使用`sar -r`命令。有些sar版本可能会有`sar -R`，但一般小写的就够了。

```bash
# sar -r 1 1
03:48:39 PM kbmemfree   kbavail kbmemused  %memused kbbuffers  kbcached  kbcommit   %commit  kbactive   kbinact   kbdirty
03:48:40 PM   1663884   2650804   6057692     78.45         0   1001040   6954428     90.06   4915476    582184       100
Average:      1663884   2650804   6057692     78.45         0   1001040   6954428     90.06   4915476    582184       100
```

（2）swap交换分区。对于swap分区来说，就可以使用`sar -S`。效果如下。如果想要看交换分区的使用情况（非容量情况），就要切换到`sar -W`命令。

```bash
# sar -S 1 1
04:05:22 PM kbswpfree kbswpused  %swpused  kbswpcad   %swpcad
04:05:23 PM         0         0      0.00         0      0.00
Average:            0         0      0.00         0      0.00
```

（3）内核使用情况，主要是使用`sar -v`命令。v一般在别的命令中用作版本展示，sar命令用来输出slab区的一些信息，可以说是特立独行，不走寻常路。

```bash
# sar -v  1
04:10:17 PM dentunusd   file-nr  inode-nr    pty-nr
04:10:18 PM    115135      3776    111146         3
04:10:19 PM    115145      3776    111151         3
04:10:20 PM    115149      3776    111155         3
```

（4）sar还能监控到内存分页信息，它有一个牛x的名字`sar -B`，来看看它的效果。

```bash
# sar -B
04:15:39 PM  pgpgin/s pgpgout/s   fault/s  majflt/s  pgfree/s pgscank/s pgscand/s pgsteal/s    %vmeff
04:15:40 PM     20.00     10.00      0.00      0.00      1.00      0.00      0.00      0.00      0.00
04:15:41 PM     16.00      0.00      0.00      0.00      0.00      0.00      0.00      0.00      0.00
04:15:42 PM     20.00    186.00      0.00      0.00      0.00      0.00      0.00      0.00      0.00
```

## 3. I/O信息

IO信息监控，同样是一个响亮的`sar -b`，不过这里的b，变成了小写的。

```bash
# sar -b 1 2

04:17:25 PM       tps      rtps      wtps   bread/s   bwrtn/s
04:17:26 PM      6.00      4.00      2.00     32.00     23.00
04:17:27 PM      5.00      5.00      0.00     48.00      0.00
Average:         5.50      4.50      1.00     40.00     11.50
```

如果你要找问题，就要配合着iowait去找了。

你可能会说，这里面的输出，才有5个选项，完全没有iostat输出的多！有个鸟用？这是因为你还没用到`sar -d`，我们来看他的效果。呵呵，就是个iostat的翻版啊。

```bash
# sar -d   1
04:18:47 PM       DEV       tps     rkB/s     wkB/s   areq-sz    aqu-sz     await     svctm     %util
04:18:48 PM  dev253-0      4.00     16.00      0.00      4.00      0.00      0.50      1.75      0.70
04:18:49 PM  dev253-0      5.00     84.00      0.00     16.80      0.00      0.60      1.80      0.90
```

## 4. 网络信息

接下来，我们看最复杂的网络信息。说它复杂，是因为它的参数非常的多，比如上面说到的DEV，就表示的网络流量。

1. **DEV** 网卡
2. **EDEV** 网卡 (错误)
3. **NFS** NFS 客户端
4. **NFSD** NFS 服务器
5. **SOCK** Sockets (套接字) (v4)
6. **IP** IP 流 (v4)
7. **EIP** IP 流 (v4) (错误)
8. **ICMP** ICMP 流 (v4)
9. **EICMP** ICMP 流 (v4) (错误)
10. **TCP** TCP 流 (v4)
11. **ETCP** TCP 流 (v4) (错误)
12. **UDP** UDP 流 (v4)
13. **SOCK6** Sockets (套接字) (v6)
14. **IP6** IP 流 (v6)
15. **EIP6** IP 流 (v6) (错误)
16. **ICMP6** ICMP 流 (v6)
17. **EICMP6** ICMP 流 (v6) (错误)
18. **UDP6** UDP 流 (v6)

要命的是，这些参数的每个输出，还都不是一样的。可能是26个字母已经无法涵盖这么多参数了吧，所以sar命令统一把它加在了`sar -n`下面。好在我们平常使用的时候，只和DEV参数打交道既可以了。

## 5. 如何安装

我们介绍过各种linux命令，像什么`top`、`vmstat`、`mpstat`、`iostat`...等等等等。

[最常用的一套“Vim“技巧](https://mp.weixin.qq.com/s?__biz=MzA4MTc4NTUxNQ==&mid=2650518612&idx=1&sn=125c2cb9ee6d76a6817fb0ebc5a3c5e4&scene=21#wechat_redirect)

[最常用的一套“Sed“技巧](http://mp.weixin.qq.com/s?__biz=MzA4MTc4NTUxNQ==&mid=2650519751&idx=1&sn=adef39cb108277731608069960692c77&chksm=8780bf03b0f73615adbb3da1fcbd342be465cc80ec6cb06a412714e474748003c3ff319e02e5&scene=21#wechat_redirect)

[最常用的一套“AWK“技巧](http://mp.weixin.qq.com/s?__biz=MzA4MTc4NTUxNQ==&mid=2650519843&idx=1&sn=fe4a5c405a35b42a850054eb4283ff40&chksm=8780bee7b0f737f194d356c155b67d19be574454adcb8ce0d16c84e6246a718c9cf29c223512&scene=21#wechat_redirect)

经过我们上面的介绍。发现，这些都不行。要数能力强，还得看`sar`命令。

sar（System ActivityReporter）是Linux最为全面的系统性能分析工具，可以监控CPU、内存、网络、I/O、文件读写、系统调用等各种资源，算是一个万能的小能手。

sar命令同样是sysstat工具包里的命令，如果你无法执行，需要像下面这样安装。

```bash
yum install sysstat
```

sar对比top这样的命令，有一个非常大的优势，那就是可以显示历史指标。

所以你刚开始安装以后，尝试执行sar。结果报错了。

```bash
[root@localhost ~]# sar
Cannot open /var/log/sa/sa08: No such file or directory
```

这就需要等一小会儿再执行，因为现在它还没有数据。一切面包牛奶，都会有的。

## End

sar命令是可以看到历史记录的。那这些文件存在哪呢？我们可以在`/var/log/sa`目录下找到它们。但可惜的是，vim打开这些文件，是乱码！

可以使用下面的命令导出它们。后面的数字，一般是当天的日期。

```bash
sar -A -f /var/log/sa/sa21 > monitor
```

这个monitor文件，我们可以使用图形化的工具打开，也可以使用文本编辑器打开。这里以`kSar`为例（一个java便携的GUI），选择载入monitor文件，即可出现下面的效果。


![img](assets/6e82aa6186344db39e664e9b54e673e1~tplv-k3u1fbpfcp-watermark.image)


有了sar这个强大的命令，你就可以对系统的参数了如指掌。和sar命令比起来，其他的命令可真的是渣。第一是因为sar能看到历史，第二是因为sar功能强大。但那些命令即使是渣，我也用的很欢。原因也有两个，一个就是用习惯了，不想换；另外一个，就是那么牛x的sar命令，参数实在是有点反人类，真的不好记忆。	