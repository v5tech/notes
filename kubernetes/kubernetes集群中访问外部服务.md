# kubernetes集群中访问外部服务

## 服务间通讯

集群内部服务之间的通讯。参考官方文档 https://kubernetes.io/zh/docs/concepts/services-networking/dns-pod-service

默认情况下，k8s 会根据服务名，为每个服务创建一个内部的 FQDN，格式为 `svc-name.namespace-name.svc.cluster.local` 集群内的其他服务可以通过此 FQDN 域名直接访问。

## 访问集群外服务

* Service - ExternalName

```yaml
kind: Service
apiVersion: v1
metadata:
  name: pgsql
  namespace: default
  ports:
    - port: 5432
spec:
  type: ExternalName
  externalName: pgsql.mydomain.com
```

这是一个 Service，只不过没有指定选择器。访问这个服务所对应的 FQDN 时，实际将解析到 externalName 所指定的域名对应的 IP。最常见的用例应该就是购买云计算厂商的数据库，通常会给我们一个地址用于连接。通过这种方式，相当于把外部地址和内部地址做了映射。

这种方式内部其实利用了 DNS 的 CNAME 技术。这就意味着 externalName 必须是一个有效的域名，而不能是 IP 地址。 如果就是想要 IP 呢？

* Endpoint

其实 Service 一直都有 Endpoint，并且是自动创建的。通常情况下，Service 通过选择器与 Pod 关联，这时候 kubernetes 就会根据 Pod 的信息搞出一个 Endpoint 作为访问点。

```yaml
apiVersion: v1
kind: Service
metadata:
  name: pgsql
spec:
  ports:
    - port: 5432
---
apiVersion: v1
kind: Endpoints
metadata:
  name: pgsql
subsets:
  - addresses:
      - ip: 200.1.2.3
    ports:
      - port: 5432
```

同样创建了一个没有选择器的 Service，但手动指定了 Endpoint。可以理解为，手动给 FQDN 设置 A 记录。

通过上面两种方式，我们成功在外部服务和 Pod 中创建了一个抽象层。未来如果外部服务也容器化，只需要改变 Service 的选择器即可，使用到这些服务的组件可以自动适配。
