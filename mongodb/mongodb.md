# MongoDB

* [MongoDB中文社区](https://mongoing.com/)
* [MongoDB 中文网](https://mongodb.net.cn/)
* [MongoDB高可用方案之副本集(Replica Set)](http://blog.itpub.net/29773961/viewspace-2128530/)
* [MongoDB分片(sharding)+副本集(replSet)集群搭建](http://blog.itpub.net/29773961/viewspace-2129100/)

---

MongoDB aggregate的性能优化

```
$match条件需要增加索引，如果是多个，最好用组合索引；
$sort的字段也需要增加索引；
$group的_id也需要增加索引；
limit可以大幅度降低时耗。
```