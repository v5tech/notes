# taobao tfs安装及配置

> 环境
> ubuntu 12.04.5
> gcc 4.6.3

### 1. 安装相关依赖

```bash
sudo apt-get install automake autoconf build-essential subversion uuid-dev libncurses5-dev libtool zlib1g-dev libreadline-dev
```

### 2. 设置TBLIB_ROOT环境变量

```bash
vim ~/.bash_profile
export TBLIB_ROOT=/home/ubuntu/lib
source ~/.bash_profile 
```

### 3. 安装tb-common-utils

```bash
svn checkout -r 18 http://code.taobao.org/svn/tb-common-utils/trunk/ tb-common-utils
cd tb-common-utils
chmod +x build.sh
./build.sh
```

### 4. 安装tfs

```bash
svn co http://code.taobao.org/svn/tfs/branches/dev_for_outer_users tfs
cd tfs
./build.sh init
./configure --prefix=/home/ubuntu/apps/tfs --with-release --without-tcmalloc
make
make install
```
*注意:*

```
–prefix 指定tfs安装路径，默认会被安装到~/tfs_bin目录。
–with-release 指定按release版本的参数进行编译，如果不指定这个参数，则会按开发版本比较严格的参数编译，包含-Werror参数，所有的警告都会被当错误，在高版本gcc下会导致项目编译不过，很多开源用户反馈的编译问题都跟这个有关，因为gcc高版本对代码的检查越来越严格，淘宝内部使用的gcc版本是gcc-4.1.2。
```

* make时uuid相关错误

```
session_util.cpp:(.text+0x1e): undefined reference to `uuid_generate'
session_util.cpp:(.text+0x2d): undefined reference to `uuid_unparse'
```

这两个方法一个是zlib的，一个是uuid的。

使用ldconfig检查链接库

```bash
ldconfig -p|grep libz.so  
ldconfig -p|grep liuuid.so 
```

经过测试，发现是gcc链接的时候，-lz -luuid动态链接选项需要放到gcc选项的最后面

修改src/tools/nameserver/Makefile文件，在-lc末尾添加-lz -luuid

修改src/tools/transfer/Makefile文件，在-lc末尾添加-lz -luuid

```bash
vim src/tools/nameserver/Makefile
LIBS = -lrt -lpthread -lm -ldl -lc -lz -luuid
```
```bash
vim src/tools/transfer/Makefile
LIBS = -lrt -lpthread -lm -ldl -lc -lz -luuid
```

最终编译好的目录为

```
drwxrwxr-x 2 ubuntu ubuntu 4096  3月  3 17:29 bin/
drwxrwxr-x 2 ubuntu ubuntu 4096  3月  3 17:29 conf/
drwxrwxr-x 2 ubuntu ubuntu 4096  3月  3 17:29 include/
drwxrwxr-x 2 ubuntu ubuntu 4096  3月  3 17:29 lib/
drwxrwxr-x 2 ubuntu ubuntu 4096  3月  3 17:29 logs/
drwxrwxr-x 3 ubuntu ubuntu 4096  3月  3 17:29 scripts/
drwxrwxr-x 4 ubuntu ubuntu 4096  3月  3 17:29 sql/
```

bin：包含tfs所有的可执行程序文件，如nameserver(NS)、dataserver(DS)、tfstool。

conf：包含tfs的配置文件，如NS的配置文件ns.conf，DS的配置文件ds.conf。

include：包含TFS客户端库相关的头文件，应用程序使用TFS需要包含这些头文件。

lib： 包含TFS客户端的静/动态库，应用程序使用TFS需要连接libtfsclient。

logs：用于存储TFS运行过程中的日志。

script：包含tfs常用的一些运维脚本，如stfs用于格式化DS， tfs启动/停止NS、DS。

### 5. 创建磁盘分区

在虚拟机中新添加三个磁盘,分别为/dev/sdb,/dev/sdc,/dev/sdd

```bash
sudo fdisk /dev/sdb
命令(输入 m 获取帮助)： o
命令(输入 m 获取帮助)： n
Partition type:
   p   primary (0 primary, 0 extended, 4 free)
   e   extended
Select (default p): p
分区号 (1-4，默认为 1)： 1
起始 sector (2048-20971519，默认为 2048)： 
将使用默认值 2048
Last sector, +扇区 or +size{K,M,G} (2048-20971519，默认为 20971519)： 
将使用默认值 20971519
命令(输入 m 获取帮助)： w
The partition table has been altered!
sudo mkfs.ext4 /dev/sdb1
```

```bash
sudo fdisk /dev/sdc
命令(输入 m 获取帮助)： o
命令(输入 m 获取帮助)： n
Partition type:
   p   primary (0 primary, 0 extended, 4 free)
   e   extended
Select (default p): p
分区号 (1-4，默认为 1)： 1
起始 sector (2048-20971519，默认为 2048)： 
将使用默认值 2048
Last sector, +扇区 or +size{K,M,G} (2048-20971519，默认为 20971519)： 
将使用默认值 20971519
命令(输入 m 获取帮助)： w
The partition table has been altered!
sudo mkfs.ext4 /dev/sdc1
```

```bash
sudo fdisk /dev/sdd
命令(输入 m 获取帮助)： o
命令(输入 m 获取帮助)： n
Partition type:
   p   primary (0 primary, 0 extended, 4 free)
   e   extended
Select (default p): p
分区号 (1-4，默认为 1)： 1
起始 sector (2048-20971519，默认为 2048)： 
将使用默认值 2048
Last sector, +扇区 or +size{K,M,G} (2048-20971519，默认为 20971519)： 
将使用默认值 20971519
命令(输入 m 获取帮助)： w
The partition table has been altered!
sudo mkfs.ext4 /dev/sdd1
```

具体操作参阅http://askubuntu.com/questions/154180/how-to-mount-a-new-drive-on-startup

修改/etc/fstab文件实现开机自动挂载

```bash
sudo vim /etc/fstab
/dev/sdb1       /home/ubuntu/data/disk1       ext4    defaults    0    1
/dev/sdc1       /home/ubuntu/data/disk2       ext4    defaults    0    1
/dev/sdd1       /home/ubuntu/data/disk3       ext4    defaults    0    1
```

### 6. 挂载硬盘

创建挂载点

```bash
ubuntu@s1:~/data$ mkdir disk1 disk2 disk3
ubuntu@s1:~/data$ ls
disk1  disk2  disk3
```

挂载磁盘

```bash
ubuntu@s1:~/data$ sudo mount /dev/sdb1 /home/ubuntu/data/disk1
ubuntu@s1:~/data$ sudo mount /dev/sdc1 /home/ubuntu/data/disk2
ubuntu@s1:~/data$ sudo mount /dev/sdd1 /home/ubuntu/data/disk3
```

查看磁盘

```bash
ubuntu@s1:~/data$ sudo df -h
文件系统        容量  已用  可用 已用% 挂载点
/dev/sda1        18G  3.4G   14G   21% /
udev            2.0G  4.0K  2.0G    1% /dev
tmpfs           394M  788K  394M    1% /run
none            5.0M     0  5.0M    0% /run/lock
none            2.0G     0  2.0G    0% /run/shm
overflow        1.0M  4.0K 1020K    1% /tmp
/dev/sdb1       9.8G   23M  9.2G    1% /home/ubuntu/data/disk1
/dev/sdc1       9.8G   23M  9.2G    1% /home/ubuntu/data/disk2
/dev/sdd1       9.8G   23M  9.2G    1% /home/ubuntu/data/disk3
```

取消挂载

```bash
ubuntu@s1:~/data/disk$ sudo umount /home/ubuntu/data/disk1
ubuntu@s1:~/data/disk$ sudo umount /home/ubuntu/data/disk2
ubuntu@s1:~/data/disk$ sudo umount /home/ubuntu/data/disk3
```

### 7. 修改tfs/conf/ns.conf和tfs/conf/ds.conf

ns.conf

```
[public]

#日志文件的级别, default info，上线使用建议设置为INFO，调试设为DEBUG
log_level=info

#监听端口
port = 8100

#工作目录
work_dir= /home/ubuntu/apps/tfs

#网络设备
dev_name= eth0

#本机IP地址(vip)，配置ha时为vip，没配置可以为主ns的ip
ip_addr = 192.168.64.136

[nameserver]

#nameserver IP地址列表(master, salve的ip地址，只能以'|'分隔)
#单台nameserver时，另一个ip配置为无效ip即可
ip_addr_list = 192.168.64.136|192.168.0.2

#用于区分dataserver所在的子网，选择不同子网的dataserver备份数据
group_mask = 255.255.255.255

#Block 最大备份数, default: 2
#单台dataserver时，需要配置为1
max_replication = 1

#Block 最小备份数, default: 2
#单台dataserver时，需要配置为1
min_replication = 1
```
ds.conf

```
[public]

#日志文件的级别, default info，线上使用建议设为info，调试设为debug 
log_level=info

#监听端口
port = 8200 

#工作目录
work_dir= /home/ubuntu/apps/tfs

#网络设备
dev_name= eth0

#本机IP地址
ip_addr = 192.168.64.136

[dataserver]

#nameserver ip地址
ip_addr = 192.168.64.136

#nameserver ip地址列表(master, salve的ip地址，只能以'|'分隔)
ip_addr_list = 192.168.64.136|192.168.0.2 

#nameserver 监听端口
port = 8100

block_max_size = 8625464 

#备件类型, 1: tfs, 2: nfs
backup_type = 1

#备件路径
#backup_path = /home/admin/tfs

#mount路径
mount_name = /home/ubuntu/data/disk

#mount 时磁盘的大小, 单位(KB)，不要要过文件系统实际剩余空间
mount_maxsize = 8625464

#主块的大小, 单位(字节)
mainblock_size = 8625464 

#扩展块的大小, 单位(字节) 
extblock_size = 4194304
```

具体配置参考

http://code.taobao.org/p/tfs/wiki/deploy/

http://code.taobao.org/p/tfs/wiki/deploy/ns.conf/

http://code.taobao.org/p/tfs/wiki/deploy/ds.conf/

### 8. 启动nameserver

```bash
ubuntu@s1:~/apps/tfs/scripts$ ./tfs  start_ns
 nameserver is up SUCCESSFULLY pid: 4249 
```

### 9. 查看nameserver

```bash
ubuntu@s1:~/apps/tfs/scripts$ netstat -ntpl |grep name
（并非所有进程都能被检测到，所有非本用户的进程信息将不会显示，如果想看到所有信息，则必须切换到 root 用户）
Proto Recv-Q Send-Q Local Address           Foreign Address         State       PID/Program name
tcp        0      0 0.0.0.0:8100            0.0.0.0:*               LISTEN      4249/nameserver
```

### 10. 格式化dataserver

```bash
ubuntu@s1:~/apps/tfs/scripts$ ./stfs format 1-3
```

### 11. 启动dataserver

```bash
ubuntu@s1:~/apps/tfs/scripts$ ./tfs start_ds 1-3
 dataserver 1 is up SUCCESSFULLY pid: 5852 
 dataserver 2 is up SUCCESSFULLY pid: 5904 
 dataserver 3 is up SUCCESSFULLY pid: 5956
```

### 12. 查看dataserver

```bash
ubuntu@s1:~/apps/tfs/scripts$ netstat -ntpl |grep data
（并非所有进程都能被检测到，所有非本用户的进程信息将不会显示，如果想看到所有信息，则必须切换到 root 用户）
tcp        0      0 0.0.0.0:8205            0.0.0.0:*               LISTEN      5956/dataserver 
tcp        0      0 0.0.0.0:8200            0.0.0.0:*               LISTEN      5852/dataserver 
tcp        0      0 0.0.0.0:8201            0.0.0.0:*               LISTEN      5852/dataserver 
tcp        0      0 0.0.0.0:8202            0.0.0.0:*               LISTEN      5904/dataserver 
tcp        0      0 0.0.0.0:8203            0.0.0.0:*               LISTEN      5904/dataserver 
tcp        0      0 0.0.0.0:8204            0.0.0.0:*               LISTEN      5956/dataserver 
```

### 13. tfs client java编译

```bash
svn co http://code.taobao.org/svn/tfs-client-java/tags/tfs-client-java-2.1.3/  tfs-client-java
```

下载http://code.taobao.org/p/tair-client-java/file/40/tair-client-2.3.1.jar

修改pom.xml文件

删除`<parent></parent>`块

修改pom.xml中的依赖为

```xml
<dependency>
  <groupId>com.taobao.common.tair</groupId>
  <artifactId>common-tair</artifactId>
  <version>2.3.1</version>
  <scope>system</scope>
  <systemPath>${basedir}/tair-client-2.3.1.jar</systemPath> 
</dependency>
```

http://code.taobao.org/p/tair-client-java/wiki/index/

```bash
cd tfs-client-java
mvn package
```
或者直接下载http://code.taobao.org/p/tfs-client-java/file/404/tfs-javaclient-2.1.1.jar

tfs-client-java使用具体见https://github.com/sxyx2008/taobao-tfs-example

### 13. 参考文档

http://code.taobao.org/p/tfs/wiki/index

http://blog.yunnotes.net/index.php/install_document_for_tfs

http://www.qinglin.net/25.html

http://ylw6006.blog.51cto.com/470441/d-18

http://www.cnblogs.com/starlitnext/p/4132069.html

http://code.taobao.org/p/tfs/issue/1835

http://www.cnphp6.com/archives/20772

http://askubuntu.com/questions/154180/how-to-mount-a-new-drive-on-startup

http://code.taobao.org/p/tfs/issue/250/

http://xwsoul.com/posts/548 UBUNTU 12.04下编译安装 GCC4.1.2

http://code.taobao.org/p/tfs/wiki/jclient/ tfs-client-java wiki