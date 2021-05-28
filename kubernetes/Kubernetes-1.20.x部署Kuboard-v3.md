# Kubernetes-1.20.x部署Kuboard-v3

创建storageClassName

```yaml
# vi kuboard-pv-sc.yaml 
apiVersion: v1
kind: PersistentVolume
metadata:
  name: data-kuboard1
spec:
  storageClassName: data-kuboard
  capacity:
    storage: 5Gi
  accessModes:
    - ReadWriteMany
  hostPath:
    path: /data-kuboard1
---
apiVersion: v1
kind: PersistentVolume
metadata:
  name: data-kuboard2
spec:
  storageClassName: data-kuboard
  capacity:
    storage: 5Gi
  accessModes:
    - ReadWriteMany
  hostPath:
    path: /data-kuboard2
---
apiVersion: v1
kind: PersistentVolume
metadata:
  name: data-kuboard3
spec:
  storageClassName: data-kuboard
  capacity:
    storage: 5Gi
  accessModes:
    - ReadWriteMany
  hostPath:
    path: /data-kuboard3
---
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: data-kuboard
provisioner: fuseim.pri/ifs
```

获取部署 Kuboard 所需的 YAML 文件

```bash
curl -o kuboard-v3.yaml https://addons.kuboard.cn/kuboard/kuboard-v3.yaml
```

修改参数KUBOARD_ENDPOINT和storageClassName

```bash
sed -i "s#KUBOARD_ENDPOINT.*#KUBOARD_ENDPOINT: 'http://10.24.10.57:30080'#g" kuboard-v3.yaml
sed -i 's#storageClassName.*#storageClassName: data-kuboard#g' kuboard-v3.yaml
```

创建kuboard-v3

```bash
kubectl apply -f kuboard-pv-sc.yaml 
kubectl apply -f kuboard-v3.yaml
```

浏览器访问kuboard-v3

```
http://10.24.10.57:30080
用户名：admin
密码：Kuboard123
```

