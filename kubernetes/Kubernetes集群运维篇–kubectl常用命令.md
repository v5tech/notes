# Kubernetes集群运维篇–kubectl常用命令

#### 查看帮助

```bash
[root@master1 ~]# kubectl --help
```

#### 查看版本

```bash
[root@master1 ~]# kubectl --version
Kubernetes v1.5.2
```

#### get

> get命令用于获取集群的一个或一些resource信息。 使用–help查看详细信息。

PS：kubectl的帮助信息、示例相当详细，而且简单易懂。建议大家习惯使用帮助信息。kubectl可以列出集群所有resource的详细。resource包括集群节点、运行的pod，ReplicationController，service等。

```bash
[root@master1 ~]# kubectl get po        //查看所有的pods
NAME        READY     STATUS    RESTARTS   AGE
pod-redis   1/1       Running   0          24s
[root@master1 ~]# kubectl get nodes     //查看所有的nodes
NAME      STATUS    AGE
node1     Ready     2d
node2     Ready     2d
[root@master1 ~]# kubectl get pods -o wide      //查看所有的pods更详细些
NAME        READY     STATUS    RESTARTS   AGE       IP         NODE
pod-redis   1/1       Running   0          1m        10.0.8.2   node1
[root@master1 ~]# kubectl get nodes -o wide
NAME      STATUS    AGE       EXTERNAL-IP
node1     Ready     2d        <none>
node2     Ready     2d        <none>
[root@master1 ~]# kubectl get po --all-namespaces       //查看所有的namespace
NAMESPACE     NAME                        READY     STATUS             RESTARTS   AGE
default       pod-redis                   1/1       Running            0          6m
```

#### pod

以yaml文件形式显示一个pod的详细信息

```bash
[root@master1 ~]# kubectl get po pod-redis -o yaml
```

以json格式输出pod的详细信息

```bash
[root@master1 ~]# kubectl get po <podname> -o json 
```

#### describe

describe类似于get，同样用于获取resource的相关信息。不同的是，get获得的是更详细的resource个性的详细信息，describe获得的是resource集群相关的信息。describe命令同get类似，但是describe不支持-o选项，对于同一类型resource，describe输出的信息格式，内容域相同。

注：如果发现是查询某个resource的信息，使用get命令能够获取更加详尽的信息。但是如果想要查询某个resource的状态，如某个pod并不是在running状态，这时需要获取更详尽的状态信息时，就应该使用describe命令。

```bash
[root@master1 ~]# kubectl describe po rc-nginx-3-l8v2r
```

#### create

不多讲了，前面已经说了很多次了。 直接使用create则可以基于rc-nginx-3.yaml文件创建出ReplicationController（rc），rc会创建两个副本：

```bash
[root@master1 ~]# kubectl create -f rc-nginx.yaml 
```

#### replace

replace命令用于对已有资源进行更新、替换。如前面create中创建的nginx，当我们需要更新resource的一些属性的时候，如果修改副本数量，增加、修改label，更改image版本，修改端口等。都可以直接修改原yaml文件，然后执行replace命令。

> 注：名字不能被更更新。另外，如果是更新label，原有标签的pod将会与更新label后的rc断开联系，有新label的rc将会创建指定副本数的新的pod，但是默认并不会删除原来的pod。所以此时如果使用get po将会发现pod数翻倍，进一步check会发现原来的pod已经不会被新rc控制，此处只介绍命令不详谈此问题，好奇者可自行实验。

```bash
[root@master1 ~]# kubectl replace -f rc-nginx.yaml
```

#### patch

如果一个容器已经在运行，这时需要对一些容器属性进行修改，又不想删除容器，或不方便通过replace的方式进行更新。kubernetes还提供了一种在容器运行时，直接对容器进行修改的方式，就是patch命令。

> 如前面创建pod的label是app=nginx-2，如果在运行过程中，需要把其label改为app=nginx-3，这patch命令如下：

```bash
[root@master1 ~]# kubectl patch pod rc-nginx-2-kpiqt -p '{"metadata":{"labels":{"app":"nginx-3"}}}' 
```

#### edit

edit提供了另一种更新resource源的操作，通过edit能够灵活的在一个common的resource基础上，发展出更过的significant resource。例如，使用edit直接更新前面创建的pod的命令为：

```bash
[root@master1 ~]# kubectl edit po rc-nginx-btv4j 
```

上面命令的效果等效于：

```bash
[root@master1 ~]# kubectl get po rc-nginx-btv4j -o yaml >> /tmp/nginx-tmp.yaml   
[root@master1 ~]# vim /tmp/nginx-tmp.yaml   
/*do some changes here */   
[root@master1 ~]# kubectl replace -f /tmp/nginx-tmp.yaml  
```

#### delete

根据resource名或label删除resource。

```bash
[root@master1 ~]# kubectl delete -f rc-nginx.yaml 
[root@master1 ~]# kubectl delete po rc-nginx-btv4j 
[root@master1 ~]# kubectl delete po -lapp=nginx-2 
```

#### apply

apply命令提供了比patch，edit等更严格的更新resource的方式。通过apply，用户可以将resource的configuration使用source control的方式维护在版本库中。每次有更新时，将配置文件push到server，然后使用kubectl apply将更新应用到resource。kubernetes会在引用更新前将当前配置文件中的配置同已经应用的配置做比较，并只更新更改的部分，而不会主动更改任何用户未指定的部分。

apply命令的使用方式同replace相同，不同的是，apply不会删除原有resource，然后创建新的。apply直接在原有resource的基础上进行更新。同时kubectl apply还会resource中添加一条注释，标记当前的apply。类似于git操作。

#### logs

logs命令用于显示pod运行中，容器内程序输出到标准输出的内容。跟docker的logs命令类似。如果要获得tail -f 的方式，也可以使用-f选项。

```bash
[root@master1 ~]# kubectl get pods
NAME                         READY     STATUS    RESTARTS   AGE
mysql-478535978-1dnm2        1/1       Running   0          1h
sonarqube-3574384362-m7mdq   1/1       Running   0          1h
[root@master1 ~]# kubectl logs mysql-478535978-1dnm2
Initializing database
...
2017-06-29T09:04:37.081939Z 0 [Note] Event Scheduler: Loaded 0 events
2017-06-29T09:04:37.082097Z 0 [Note] mysqld: ready for connections.
Version: '5.7.16'  socket: '/var/run/mysqld/mysqld.sock'  port: 3306  MySQL Community Server (GPL)
```

#### rolling-update

rolling-update是一个非常重要的命令，对于已经部署并且正在运行的业务，rolling-update提供了不中断业务的更新方式。rolling-update每次起一个新的pod，等新pod完全起来后删除一个旧的pod，然后再起一个新的pod替换旧的pod，直到替换掉所有的pod。

rolling-update需要确保新的版本有不同的name，Version和label，否则会报错 。

```bash
[root@master1 ~]# kubectl rolling-update rc-nginx-2 -f rc-nginx.yaml
```

如果在升级过程中，发现有问题还可以中途停止update，并回滚到前面版本

```bash
[root@master1 ~]# kubectl rolling-update rc-nginx-2 —rollback
```

rolling-update还有很多其他选项提供丰富的功能，如—update-period指定间隔周期，使用时可以使用-h查看help信息。

#### scale

scale用于程序在负载加重或缩小时副本进行扩容或缩小，如前面创建的nginx有两个副本，可以轻松的使用scale命令对副本数进行扩展或缩小。

扩展副本数到4：

```bash
[root@master1 ~]# kubectl scale rc rc-nginx-3 —replicas=4
```

重新缩减副本数到2：

```bash
[root@master1 ~]# kubectl scale rc rc-nginx-3 —replicas=2
```

#### autoscale

scale虽然能够很方便的对副本数进行扩展或缩小，但是仍然需要人工介入，不能实时自动的根据系统负载对副本数进行扩、缩。autoscale命令提供了自动根据pod负载对其副本进行扩缩的功能。

autoscale命令会给一个rc指定一个副本数的范围，在实际运行中根据pod中运行的程序的负载自动在指定的范围内对pod进行扩容或缩容。如前面创建的nginx，可以用如下命令指定副本范围在1~4

```bash
[root@master1 ~]# kubectl autoscale rc rc-nginx-3 --min=1 --max=4
```

#### cordon, drain, uncordon

这三个命令是正式release的1.2新加入的命令，三个命令一起介绍，是因为三个命令配合使用可以实现节点的维护。

在1.2之前，因为没有相应的命令支持，如果要维护一个节点，只能stop该节点上的kubelet将该节点退出集群，是集群不在将新的pod调度到该节点上。如果该节点上本生就没有pod在运行，则不会对业务有任何影响。如果该节点上有pod正在运行，kubelet停止后，master会发现该节点不可达，而将该节点标记为notReady状态，不会将新的节点调度到该节点上。同时，会在其他节点上创建新的pod替换该节点上的pod。

这种方式虽然能够保证集群的健壮性，但是任然有些暴力，如果业务只有一个副本，而且该副本正好运行在被维护节点上的话，可能仍然会造成业务的短暂中断。

1.2中新加入的这3个命令可以保证维护节点时，平滑的将被维护节点上的业务迁移到其他节点上，保证业务不受影响。如下图所示是一个整个的节点维护的流程（为了方便demo增加了一些查看节点信息的操作）：

1）首先查看当前集群所有节点状态，可以看到共四个节点都处于ready状态；

2）查看当前nginx两个副本分别运行在d-node1和k-node2两个节点上；

3）使用cordon命令将d-node1标记为不可调度；

4）再使用kubectl get nodes查看节点状态，发现d-node1虽然还处于Ready状态，但是同时还被禁能了调度，这意味着新的pod将不会被调度到d-node1上。

5）再查看nginx状态，没有任何变化，两个副本仍运行在d-node1和k-node2上；

6）执行drain命令，将运行在d-node1上运行的pod平滑的赶到其他节点上；

7）再查看nginx的状态发现，d-node1上的副本已经被迁移到k-node1上；这时候就可以对d-node1进行一些节点维护的操作，如升级内核，升级Docker等；

8）节点维护完后，使用uncordon命令解锁d-node1，使其重新变得可调度；

9）检查节点状态，发现d-node1重新变回Ready状态。

#### attach

类似于docker attach的功能，用于取得实时的类似于kubectl logs的信息

```bash
[root@master1 ~]# kubectl get pods
NAME                         READY     STATUS    RESTARTS   AGE
mysql-478535978-1dnm2        1/1       Running   0          1h
sonarqube-3574384362-m7mdq   1/1       Running   0          1h
[root@master1 ~]# kubectl attach sonarqube-3574384362-m7mdq
If you don't see a command prompt, try pressing enter.
```

#### exec

exec命令同样类似于docker的exec命令，为在一个已经运行的容器中执行一条shell命令，如果一个pod容器中，有多个容器，需要使用-c选项指定容器。

```bash
[root@master1 ~]# kubectl get pods
NAME                         READY     STATUS    RESTARTS   AGE
mysql-478535978-1dnm2        1/1       Running   0          1h
sonarqube-3574384362-m7mdq   1/1       Running   0          1h
[root@master1 ~]# kubectl exec mysql-478535978-1dnm2 hostname       //查看这个容器的hostname
mysql-478535978-1dnm2
```

#### port-forward

转发一个本地端口到容器端口，博主一般都是使用yaml的方式编排容器，所以基本不使用此命令。

#### proxy

博主只尝试过使用nginx作为kubernetes多master HA方式的代理，没有使用过此命令为kubernetes api server运行过proxy

#### run

类似于docker的run命令，直接运行一个image。

#### label

为kubernetes集群的resource打标签，如前面实例中提到的为rc打标签对rc分组。还可以对nodes打标签，这样在编排容器时，可以为容器指定nodeSelector将容器调度到指定lable的机器上，如如果集群中有IO密集型，计算密集型的机器分组，可以将不同的机器打上不同标签，然后将不同特征的容器调度到不同分组上。

在1.2之前的版本中，使用kubectl get nodes则可以列出所有节点的信息，包括节点标签，1.2版本中不再列出节点的标签信息，如果需要查看节点被打了哪些标签，需要使用describe查看节点的信息。

#### cp

> kubectl cp 用于pod和外部的文件交换，比如如下示例了如何在进行内外文件交换。

在pod中创建一个文件message.log

```bash
[root@master1 ~]# kubectl exec -it mysql-478535978-1dnm2 sh
# pwd
/
# cd /tmp
# echo "this is a message from `hostname`" >message.log
# cat message.log
this is a message from mysql-478535978-1dnm2
# exit
```

拷贝出来并确认

```bash
[root@master1 ~]# kubectl cp mysql-478535978-1dnm2:/tmp/message.log message.log
tar: Removing leading `/' from member names
[root@master1 ~]# cat message.log
this is a message from mysql-478535978-1dnm2
```

更改message.log并拷贝回pod

```bash
[root@master1 ~]# echo "information added in `hostname`" >>message.log 
[root@master1 ~]# cat message.log 
this is a message from mysql-478535978-1dnm2
information added in ku8-1
[root@master1 ~]# kubectl cp message.log mysql-478535978-1dnm2:/tmp/message.log
```

确认更改后的信息

```bash
[root@master1 ~]# kubectl exec mysql-478535978-1dnm2 cat /tmp/message.log
this is a message from mysql-478535978-1dnm2
information added in ku8-1
```

#### kubectl cluster-info

使用cluster-info和cluster-info dump也能取出一些信息，尤其是你需要看整体的全部信息的时候一条命令一条命令的执行不如kubectl cluster-info dump来的快一些

```bash
[root@master1 ~]# kubectl cluster-info Kubernetes master is running at http://localhost:8080 To further debug and diagnose cluster problems, use 'kubectl cluster-info dump'.
```

#### 实际操作：

##### 集群构成

一主三从的Kubernetes集群    

```bash
[root@master1 ~]# kubectl get nodes
NAME             STATUS    AGE
192.168.32.132   Ready     12m
192.168.32.133   Ready     11m
192.168.32.134   Ready     11m
```

yaml文件：

```bash
[root@master1 ~]# cat nginx/nginx.yaml 
---
kind: Deployment
apiVersion: extensions/v1beta1
metadata:
  name: nginx
spec:
  replicas: 1
  template:
    metadata:
      labels:
        name: nginx
    spec:
      containers:
      - name: nginx
        image: 192.168.32.131:5000/nginx:1.12-alpine
        ports:
        - containerPort: 80
          protocol: TCP
---
kind: Service
apiVersion: v1
metadata:
  name: nginx
  labels:
    name: nginx
spec:
  type: NodePort
  ports:
  - protocol: TCP
    nodePort: 31001
    targetPort: 80
    port: 80
  selector:
    name: nginx
```

#### kubectl create

##### 创建pod/deployment/service

```bash
[root@master1 ~]# kubectl create -f nginx/
deployment "nginx" created
service "nginx" created
```

确认 创建pod/deployment/service

```bash
[root@master1 ~]# kubectl get service
NAME         CLUSTER-IP        EXTERNAL-IP   PORT(S)        AGE
kubernetes   172.200.0.1       <none>        443/TCP        1d
nginx        172.200.229.212   <nodes>       80:31001/TCP   58s
[root@master1 ~]# kubectl get pod
NAME                     READY     STATUS    RESTARTS   AGE
nginx-2476590065-1vtsp   1/1       Running   0          1m
[root@master1 ~]# kubectl get deploy
NAME      DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
nginx     1         1         1            1           1m
```

#### kubectl edit

edit这条命令用于编辑服务器上的资源，具体是什么意思，可以通过如下使用方式来确认。

#### 编辑对象确认

使用-o参数指定输出格式为yaml的nginx的service的设定情况确认，取得现场情况，这也是我们不知道其yaml文件而只有环境时候能做的事情。

```bash
[root@master1 ~]# kubectl get service |grep nginx
nginx        172.200.229.212   <nodes>       80:31001/TCP   2m
[root@master1 ~]# kubectl get service nginx -o yaml
apiVersion: v1
kind: Service
metadata:
  creationTimestamp: 2017-06-30T04:50:44Z
  labels:
    name: nginx
  name: nginx
  namespace: default
  resourceVersion: "77068"
  selfLink: /api/v1/namespaces/default/services/nginx
  uid: ad45612a-5d4f-11e7-91ef-000c2933b773
spec:
  clusterIP: 172.200.229.212
  ports:
  - nodePort: 31001
    port: 80
    protocol: TCP
    targetPort: 80
  selector:
    name: nginx
  sessionAffinity: None
  type: NodePort
status:
  loadBalancer: {}
```

使用edit命令对nginx的service设定进行编辑，得到如下信息

可以看到当前端口为31001，在此编辑中，我们把它修改为31002

```bash
[root@master1 ~]# kubectl edit service nginx 
service "nginx" edited
```

编辑之后确认结果发现，此服务端口已经改变

```bash
[root@master1 ~]# kubectl get service
NAME         CLUSTER-IP        EXTERNAL-IP   PORT(S)        AGE
kubernetes   172.200.0.1       <none>        443/TCP        1d
nginx        172.200.229.212   <nodes>       80:31002/TCP   8m
```

确认后发现能够立连通

```html
[root@master1 ~]# curl http://192.168.32.132:31002/
<!DOCTYPE html>
<html>
<head>
<title>Welcome to nginx!</title>
<style>
    body {
        width: 35em;
        margin: 0 auto;
        font-family: Tahoma, Verdana, Arial, sans-serif;
    }
</style>
</head>
<body>
<h1>Welcome to nginx!</h1>
<p>If you see this page, the nginx web server is successfully installed and
working. Further configuration is required.</p>

<p>For online documentation and support please refer to
<a href="http://nginx.org/">nginx.org</a>.<br/>
Commercial support is available at
<a href="http://nginx.com/">nginx.com</a>.</p>

<p><em>Thank you for using nginx.</em></p>
</body>
</html>
```

而之前的端口已经不通

```bash
[root@master1 ~]# curl http://192.168.32.132:31001/
curl: (7) Failed connect to 192.168.32.132:31001; Connection refused
```

#### kubectl replace

了解到edit用来做什么之后，我们会立即知道replace就是替换，我们使用上个例子中的service的port，重新把它改回31001

```bash
[root@master1 ~]# kubectl get service
NAME         CLUSTER-IP        EXTERNAL-IP   PORT(S)        AGE
kubernetes   172.200.0.1       <none>        443/TCP        1d
nginx        172.200.229.212   <nodes>       80:31002/TCP   17m
```

取得当前的nginx的service的设定文件，然后修改port信息

```bash
[root@master1 ~]# kubectl get service nginx -o yaml >nginx_forreplace.yaml
[root@master1 ~]# cp -p nginx_forreplace.yaml nginx_forreplace.yaml.org
[root@master1 ~]# vi nginx_forreplace.yaml
[root@master1 ~]# diff nginx_forreplace.yaml nginx_forreplace.yaml.org
15c15
<   - nodePort: 31001
---
>   - nodePort: 31002
```

#### 执行replace命令

##### 提示被替换了

```bash
[root@master1 ~]# kubectl replace -f nginx_forreplace.yaml
service "nginx" replaced
```

确认之后发现port确实重新变成了31001

```bash
[root@master1 ~]# kubectl get service
NAME         CLUSTER-IP        EXTERNAL-IP   PORT(S)        AGE
kubernetes   172.200.0.1       <none>        443/TCP        1d
nginx        172.200.229.212   <nodes>       80:31001/TCP   20m
```

#### kubectl patch

当部分修改一些设定的时候patch非常有用，尤其是在1.2之前的版本，port改来改去好无聊，这次换个image

当前port中使用的nginx是alpine的1.12版本

```bash
[root@master1 ~]# kubectl exec nginx-2476590065-1vtsp  -it sh
/ # nginx -v
nginx version: nginx/1.12.0
```

#### 执行patch进行替换

```bash
[root@master1 ~]# kubectl patch pod nginx-2476590065-1vtsp -p '{"spec":{"containers":[{"name":"nginx","image":"192.168.32.131:5000/nginx:1.13-alpine"}]}}'
"nginx-2476590065-1vtsp" patched
```

确认当前pod中的镜像已经patch成了1.13

```bash
[root@master1 ~]# kubectl exec nginx-2476590065-1vtsp  -it sh
/ # nginx -v
nginx version: nginx/1.13.1
```

#### kubectl scale

scale命令用于横向扩展，是kubernetes或者swarm这类容器编辑平台的重要功能之一，让我们来看看是如何使用的

事前设定nginx的replica为一，而经过确认此pod在192.168.32.132上运行

```bash
[root@master1 ~]# kubectl delete -f nginx/
deployment "nginx" deleted
service "nginx" deleted
[root@master1 ~]# kubectl create -f nginx/
deployment "nginx" created
service "nginx" created
[root@master1 ~]# 
[root@master1 ~]# kubectl get pods -o wide
NAME                     READY     STATUS    RESTARTS   AGE       IP             NODE
nginx-2476590065-74tpk   1/1       Running   0          17s       172.200.26.2   192.168.32.132
[root@master1 ~]# kubectl get deployments -o wide
NAME      DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
nginx     1         1         1            1           27s
```

#### 执行scale命令

使用scale命令进行横向扩展，将原本为1的副本，提高到3。

```bash
[root@master1 ~]# kubectl scale --current-replicas=1 --replicas=3 deployment/nginx
deployment "nginx" scaled
```

通过确认发现已经进行了横向扩展，除了192.168.132.132，另外133和134两台机器也各有一个pod运行了起来，这正是scale命令的结果。

```bash
[root@master1 ~]# kubectl get deployment
NAME      DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
nginx     3         3         3            3           2m
[root@master1 ~]# kubectl get pod -o wide
NAME                     READY     STATUS    RESTARTS   AGE       IP             NODE
nginx-2476590065-74tpk   1/1       Running   0          2m        172.200.26.2   192.168.32.132
nginx-2476590065-cm5d9   1/1       Running   0          16s       172.200.44.2   192.168.32.133
nginx-2476590065-hmn9j   1/1       Running   0          16s       172.200.59.2   192.168.32.134
```

#### kube autoscale ★★★★

autoscale命令用于自动扩展确认，跟scale不同的是前者还是需要手动执行，而autoscale则会根据负载进行调解。而这条命令则可以对Deployment/ReplicaSet/RC进行设定，通过最小值和最大值的指定进行设定，这里只是给出执行的结果，不再进行实际的验证。

```bash
[root@master1 ~]# kubectl autoscale deployment nginx --min=2 --max=5
deployment "nginx" autoscaled
```

当然使用还会有一些限制，比如当前3个，设定最小值为2的话会出现什么样的情况？

```bash
[root@master1 ~]# kubectl get pods -o wide
NAME                     READY     STATUS    RESTARTS   AGE       IP             NODE
nginx-2476590065-74tpk   1/1       Running   0          5m        172.200.26.2   192.168.32.132
nginx-2476590065-cm5d9   1/1       Running   0          2m        172.200.44.2   192.168.32.133
nginx-2476590065-hmn9j   1/1       Running   0          2m        172.200.59.2   192.168.32.134
[root@master1 ~]# kubectl autoscale deployment nginx --min=2 --max=2
Error from server (AlreadyExists): horizontalpodautoscalers.autoscaling "nginx" already exists
```

#### kubectl cordon 与 uncordon ★★★

在实际维护的时候会出现某个node坏掉，或者做一些处理，暂时不能让生成的pod在此node上运行，需要通知kubernetes让其不要创建过来，这条命令就是cordon，uncordon则是取消这个要求。例子如下：

创建了一个nginx的pod，跑在192.168.32.133上。

```bash
[root@master1 ~]# kubectl create -f nginx/
deployment "nginx" created
service "nginx" created
[root@master1 ~]# kubectl get pods -o wide
NAME                     READY     STATUS    RESTARTS   AGE       IP             NODE
nginx-2476590065-dnsmw   1/1       Running   0          6s        172.200.44.2   
192.168.32.133
```

#### 执行scale命令 ★★★

横向扩展到3个副本，发现利用roundrobin策略每个node上运行起来了一个pod，134这台机器也有一个。

```bash
[root@master1 ~]# kubectl scale --replicas=3 deployment/nginx
deployment "nginx" scaled
[root@master1 ~]# kubectl get pods -o wide
NAME                     READY     STATUS    RESTARTS   AGE       IP             NODE
nginx-2476590065-550sm   1/1       Running   0          5s        172.200.26.2   192.168.32.132
nginx-2476590065-bt3bc   1/1       Running   0          5s        172.200.59.2   192.168.32.134
nginx-2476590065-dnsmw   1/1       Running   0          17s       172.200.44.2   192.168.32.133
[root@master1 ~]# kubectl get pods -o wide |grep 192.168.32.134
nginx-2476590065-bt3bc   1/1       Running   0          12s       172.200.59.2   192.168.32.134
```

#### 执行cordon命令

设定134，使得134不可使用，使用get node确认，其状态显示SchedulingDisabled。

```bash
[root@master1 ~]# kubectl cordon 192.168.32.134
node "192.168.32.134" cordoned
[root@master1 ~]# kubectl get nodes -o wide
NAME             STATUS                     AGE       EXTERNAL-IP
192.168.32.132   Ready                      1d        <none>
192.168.32.133   Ready                      1d        <none>
192.168.32.134   Ready,SchedulingDisabled   1d        <none>
```

#### 执行scale命令

再次执行横向扩展命令，看是否会有pod漂到134这台机器上，结果发现只有之前的一个pod，再没有新的pod漂过去。

```bash
[root@master1 ~]# kubectl scale --replicas=6 deployment/nginx
deployment "nginx" scaled
[root@master1 ~]# kubectl get pods -o wide
NAME                     READY     STATUS    RESTARTS   AGE       IP             NODE
nginx-2476590065-550sm   1/1       Running   0          32s       172.200.26.2   192.168.32.132
nginx-2476590065-7vxvx   1/1       Running   0          3s        172.200.44.3   192.168.32.133
nginx-2476590065-bt3bc   1/1       Running   0          32s       172.200.59.2   192.168.32.134
nginx-2476590065-dnsmw   1/1       Running   0          44s       172.200.44.2   192.168.32.133
nginx-2476590065-fclhj   1/1       Running   0          3s        172.200.44.4   192.168.32.133
nginx-2476590065-fl9fn   1/1       Running   0          3s        172.200.26.3   192.168.32.132
[root@master1 ~]# kubectl get pods -o wide |grep 192.168.32.134
nginx-2476590065-bt3bc   1/1       Running   0          37s       172.200.59.2   192.168.32.134
```

#### 执行uncordon命令

使用uncordon命令解除对134机器的限制，通过get node确认状态也已经正常。

```bash
[root@master1 ~]# kubectl uncordon 192.168.32.134
node "192.168.32.134" uncordoned
[root@master1 ~]# 
[root@master1 ~]# kubectl get nodes -o wide
NAME             STATUS    AGE       EXTERNAL-IP
192.168.32.132   Ready     1d        <none>
192.168.32.133   Ready     1d        <none>
192.168.32.134   Ready     1d        <none>
```

#### 执行scale命令

再次执行scale命令，发现有新的pod可以创建到134node上了。

```bash
[root@master1 ~]# kubectl scale --replicas=10 deployment/nginx
deployment "nginx" scaled
[root@master1 ~]# kubectl get pods -o wide
NAME                     READY     STATUS    RESTARTS   AGE       IP             NODE
nginx-2476590065-550sm   1/1       Running   0          1m        172.200.26.2   192.168.32.132
nginx-2476590065-7vn6z   1/1       Running   0          3s        172.200.44.4   192.168.32.133
nginx-2476590065-7vxvx   1/1       Running   0          35s       172.200.44.3   192.168.32.133
nginx-2476590065-bt3bc   1/1       Running   0          1m        172.200.59.2   192.168.32.134
nginx-2476590065-dnsmw   1/1       Running   0          1m        172.200.44.2   192.168.32.133
nginx-2476590065-fl9fn   1/1       Running   0          35s       172.200.26.3   192.168.32.132
nginx-2476590065-pdx91   1/1       Running   0          3s        172.200.59.3   192.168.32.134
nginx-2476590065-swvwf   1/1       Running   0          3s        172.200.26.5   192.168.32.132
nginx-2476590065-vdq2k   1/1       Running   0          3s        172.200.26.4   192.168.32.132
nginx-2476590065-wdv52   1/1       Running   0          3s        172.200.59.4   192.168.32.134
```

#### kubectl drain ★★★★★

drain命令用于对某个node进行设定，是为了设定此node为维护做准备。英文的drain有排干水的意思，下水道的水之后排干后才能进行维护。那我们来看一下kubectl”排水”的时候都作了什么

将nginx的副本设定为4，确认发现134上启动了两个pod。

```bash
[root@master1 ~]# kubectl create -f nginx/
deployment "nginx" created
service "nginx" created
[root@master1 ~]# kubectl get pod -o wide
NAME                     READY     STATUS    RESTARTS   AGE       IP             NODE
nginx-2476590065-d6h8f   1/1       Running   0          8s        172.200.59.2   192.168.32.134
[root@master1 ~]# 
[root@master1 ~]# kubectl get nodes -o wide
NAME             STATUS    AGE       EXTERNAL-IP
192.168.32.132   Ready     1d        <none>
192.168.32.133   Ready     1d        <none>
192.168.32.134   Ready     1d        <none>
[root@master1 ~]# 
[root@master1 ~]# kubectl scale --replicas=4 deployment/nginx
deployment "nginx" scaled
[root@master1 ~]# kubectl get pods -o wide
NAME                     READY     STATUS    RESTARTS   AGE       IP             NODE
nginx-2476590065-9lfzh   1/1       Running   0          12s       172.200.59.3   192.168.32.134
nginx-2476590065-d6h8f   1/1       Running   0          1m        172.200.59.2   192.168.32.134
nginx-2476590065-v8xvf   1/1       Running   0          43s       172.200.26.2   192.168.32.132
nginx-2476590065-z94cq   1/1       Running   0          12s       172.200.44.2   192.168.32.133
```
#### 执行drain命令

执行drain命令，发现这条命令做了两件事情:

1. 设定此node不可以使用（cordon)

2. evict了其上的两个pod

```bash
[root@master1 ~]# kubectl drain 192.168.32.134
node "192.168.32.134" cordoned
pod "nginx-2476590065-d6h8f" evicted
pod "nginx-2476590065-9lfzh" evicted
node "192.168.32.134" drained
```

#### 结果确认

evict的意思有驱逐和回收的意思，让我们来看一下evcit这个动作的结果到底是什么。 结果是134上面已经不再有pod，而在132和133上新生成了两个pod，用以替代在134上被退场的pod，而这个替代的动作应该是replicas的机制保证的。所以drain的结果就是退场pod和设定node不可用（排水），这样的状态则可以进行维护了，执行完后重新uncordon即可。

```bash
[root@master1 ~]# kubectl get pods -o wide
NAME                     READY     STATUS    RESTARTS   AGE       IP             NODE
nginx-2476590065-1ld9j   1/1       Running   0          13s       172.200.44.3   192.168.32.133
nginx-2476590065-ss48z   1/1       Running   0          13s       172.200.26.3   192.168.32.132
nginx-2476590065-v8xvf   1/1       Running   0          1m        172.200.26.2   192.168.32.132
nginx-2476590065-z94cq   1/1       Running   0          55s       172.200.44.2   192.168.32.133
[root@master1 ~]# kubectl get nodes -o wide
NAME             STATUS                     AGE       EXTERNAL-IP
192.168.32.132   Ready                      1d        <none>
192.168.32.133   Ready                      1d        <none>
192.168.32.134   Ready,SchedulingDisabled   1d        <none>
```

文章来源: https://cloud.tencent.com/developer/article/1140076