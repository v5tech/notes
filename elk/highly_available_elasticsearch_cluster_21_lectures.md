# 高可用 Elasticsearch集群21讲

## 1 .如何构建一个高可用、低延迟的 Elasticsearch 集群？

我们从 1.x 开始使用 Elasticsearch ，发展到现在大大小小的集群有 5000+，最大的集群物理主机 100+，单集群最大数据量几百 TB，几千亿条 doc。在这个大规模的应用过程中我们积累了很多宝贵经验，在此与大家分享。

相比 Hadoop 系列的大数据平台，Elasticsearch 使用起来要简单得多，你只要修改很少的几个配置就可以让集群运行起来，而且 Elasticsearch 拥有丰富的 REST 接口，你能够看到集群的各种指标和状态，从而了解集群内部的运行状态。

但越是简单易用的系统，它的内部就越隐含着更多的默认配置。系统可以在默认配置下正常运行，但是随着数据量的增加，或者用户访问的高峰期，集群可能忽然出现问题 —— 昨天还是好好的，为什么今天就出问题了？我们什么都没动。

要解决这些问题，你就需要知道故障的根本原因是什么，这需要你对系统有足够的了解。在大型互联网公司中，都有做基础技术的团队负责平台的开发和运维，但中小型企业很少会有这样的团队。

尤其像 Hadoop 这种复杂的系统，没有基础技术团队你很难把它真正用起来。虽然 Elasticsearch 简便许多，但仍然可能会遇到一些问题，排查这些问题可能会耽误你很长时间。

另一方面，一个新业务准备使用 Elasticsearch 时对集群的规划往往没有概念：

- 需要多少个节点来存储数据？
- 索引分片数量多少比较合适？
- 集群 GREEN 就一定是正常的吗？
- 什么时候集群需要扩容？
- 我应该重点关注哪些指标来确认集群的健康状态？

我们往往只是在遇到线上故障的时候才去分析相关原理，而不是空闲的时候进行系统的学习。分析定位问题虽然有趣，但有时我们更希望有人直接告知答案是什么，它为什么会这样，怎么做可以合理地解决问题。

我们写下这门专栏最大的目的，是希望读者能够解决用户的这些烦恼，分享我们在日常运维过程中所遇到的常见问题，大部分的故障是类似的。把这些分享出来，避免他人再重新分析问题。让读者能够合理地规划、使用，以及监控集群。

这些经验是我们在多年开发、运维 Elasticsearch 集群中的原理和实践的沉淀。如果只知道原理，没有实践经验，可能会在遇到问题时不知道具体该怎么做，更不明白原理的内在联系和利害关系，而实践经验可以让读者理解原理的同时有一个更具体的、可实施的参考依据。

### 专栏框架

本专栏由四部分组成，总计 21 篇。

第一部分（第 1-1~1-4 课）新集群诞生

- 合理地规划集群规模、组织结构，以及根据业务特点设计索引和分片是第一步。实际上很多问题是由于集群规划不合理，或者分片配置不合理、mapping 配置不合理导致的。

  一开始的时候就把这些工作做好，可以避免很多后期问题。这部分内容根据我们多年维护经验给读者一些原则性的参考，让集群规划和分片规划等能够相对准确地量化出来。

第二部分 （第 2-1~2-9 课）集群管理

- 新集群组建好之后，最好对集群性能做整体评估，虽然基准测试有一定参考价值，但由于业务数据的差异，往往导致较大的性能差别，因此业务最好根据自己的数据样本进行压力测试，这样你就明确的知道系统能够达到什么能力，做到心中有数。

  这部分内容介绍了如何进行压力测试和对比压测效果，以及后续的日常管理工作，包括集群监控、应该重点关注哪些指标、性能优化都需要做些什么、集群什么时候需要扩容、扩容注意事项、数据迁移等常见的管理工作。

第三部分 （第 3-1~3-4 课） 安全防护

- 业务初期很容易忽视安全问题，在遇到麻烦的时候才去解决。安全包括用户认证、用户鉴权、通讯加密等多方面的内容，最简单的方式可以使用 Nginx 之类的反向代理来过滤请求，实现简单的用户和权限管理。

  X-Pack 中提供了非常完善的安全组件，但是版本之间的变化比较大，中文内容又比较少，这部分内容就是使用 X-Pack 进行安全防护，并接入自己的用户管理体系。

第四部分 （第 4-1~4-4 课）常见问题

- 在这部分中我们把多年运维过程中常见的、比较通用的问题梳理出来，希望给读者一个参考，在遇到类似问题的时候可以从中直接找到答案，以及分析问题的思路和方法、诊断问题所需的工具等，把握住问题的本质。

  虽然一个故障可能是多种因素导致的，这部分内容无法覆盖所有的情况，但是相信可以应对大部分情况。

### 专栏特色

**本专栏内容属于进阶到精通级别，需要读者有一定的基础知识**，在基础知识方面，《Elasticsearch 权威指南》已经很好，我们希望给读者一些切合实际的、注重实战方面的资料。

因此，本专栏重点解决以下实际问题：

- 新集群规模如何决定，我们给出可以量化的指标，以及角色分离建议
- 索引和分片如何设计，在此给出相关注意事项，很多都是我们踩坑后的总结
- 集群 RED 和 YELLOW 等常见问题如何分析和解决
- 内存居高不下，GC 严重，节点不稳定等问题如何分析和解决
- 如何应用安全策略，如何压测，了解你集群能力的上限
- 常见配置的作用及建议值

在阅读本专栏之前，建议读者已经阅读过《Elasticsearch 权威指南》中的大部分内容，对 Elasticsearch 的基本概念如分片、节点角色，以及常见配置项已经有了一定的了解。

### 如何学习 Elasticsearch

关于 Elasticsearch 基础知识的内容，例如：分布式搜索，主从节点，主副分片等基本概念有很多资料可以参考，由于本专栏定位为进阶与精通，在此我们不再重复介绍这些基础知识。

如果读者尚未了解这些基础概念，或者知识体系尚不完善，推荐阅读 Elasticsearch 官方的资料，现在，我们给读者一些具体学习建议，方法论可以让你把握住重点，提升效率。

分布式系统是复杂的，但是 Elasticsearch 的产品设计使得它变得简单易用。当你下载到一个安装包，不用修改任何配置就可以让它运行起来，相对于 Hadoop 系列的平台来说容易的多。

但是当你大规模应用于线上服务时，只使用默认配置是不够的，你需要根据自己的情况进行很多调整，你可以等系统出现问题的时候再去了解相关知识，但是对于入门级用户来说，最好先系统化地熟悉 Elasticsearch 的基础知识。

#### 1. 入门知识

小白用户该如何学习 Elasticsearch 呢？

我们推荐阅读《Elasticsearch 权威指南》，虽然它的内容有些过时，但仍然是最适合入门者的材料，建议从第一页开始通读此书，并且搭建一个集群实际操作一下，这样会有一个直观的感受，加深理解和记忆。读书过程中会产生很多问题，因此可以自己做一些测试，或者与其他人互相交流探讨解决疑问。

每当读完一章，或者一个比较大的部分，你需要用自己的方式整理这部分的重点，可以是思维导图、PPT 或者 Markdown 等你喜欢的形式，这样当读完整本书的时候，你会形成自己的知识体系结构，通过查看笔记就可以掌握所有的关键环节。

学习的过程中一定要有输出，用自己的语言将学习到的知识表达出来。我们团队的同事们在学习完一部分内容后会在周会的时候做一次技术分享，虽然在制作 PPT 的时候会花些时间，但这是一个对知识整理的过程，你需要把它给大家讲明白。

在讲解的时候会面对大家的很多问题，在整个归纳和整理的过程中，你自己就发现很多疑问，连自己的说服不了，这就迫使你去彻底搞明白。但如果你只是被动地阅读，没有主动思考，理解的层次就往往流与表面，并且容易忘记。

这个过程进度不用太快，优先消化和吸收知识点，保证学习效果。最理想的是同时配合一些实践，虽然你刚刚学习完一大部分的内容，但是在线上遇到与这部分相关的问题时可能还是会有点懵，因此处理一些实际问题会让你对系统的理解更加深刻。

有些问题解决起来确实比较花时间，每次解决必须搞清楚到底什么原理，切忌模棱两可的结论，它可能是这样，也可能是那样，虽然有时候搞清楚到底怎么回事可能花费非常多的时间，但是一旦解决，以后你再也不会受它的困扰，否则后续遇到类似问题仍然是原地踏步。

这个时期的参考资料不建议去 baidu 查阅中文博客的文章，因为这些资料经常有很多一知半解的错误，并且内容过时。

官网的 API 手册、GitHub 上的 Issues、Pull Request，以及中文及英文社区是最权威的内容，绝大部分问题都可以找到答案。尤其是在 API 的使用方式上，官网的手册是非常完整的，并且很多 API 在不同版本之间存在较大差异，因此最好在手册中选择与自己一致的版本。

![enter image description here](https://images.gitbook.cn/fc2dba10-5cd7-11e9-b663-affa49df89af)

对于集群故障，也尽量不要百度查找答案，搜索技术内容 Google 要要准确的多。另外，有条件的话也可以报名参加官方的培训专栏，并考取 Elasticsearch 工程师认证。官方的培训是线下的形式，基于最新的稳定版，并且对于学习中的问题可以现场得到最权威的解答，是效率最高的学习方式。

#### 2. 进阶学习

当你系统化地学习过基础知识，并且有了一些线上故障处理经验，进一步的学习需要关注更多内容，有时候你会遇到比较深层次的问题，虽然在《Elasticsearch 权威指南》中有许多原理方面的知识，但是仍然不够，有些问题你需要从源码中找到答案。

进阶方面的内容可以阅读我的另一本书《Elasticsearch 源码解析与优化实战》以及本专栏。系统化的学习，以及自己的亲身经历让你成长很多，现在可能集群也比较稳定，你也许不知道接下来应该再学些什么？怎样进一步提升？

现在是时候拓宽眼界，关注社区的发展，以及别人都遇到了哪些问题，不再局限于自己所处的环境。因此建议定期关注以下内容：

- 订阅 Elasticsearch 日报，日报每天会推送一些最近技术动向，以及网络上其他人编写的比较不错的内容，订阅地址：<https://tinyletter.com/elastic-daily>
- 英文社区论坛，不可否认官方英文论坛是最活跃的，你可以在这里看大别人都遇到了什么问题，是怎么解决的，这里有官方的工程师回应：<https://discuss.elastic.co/>
- 英文社区博客，官网的博客大部分是关于新产品的信息，但是也有一些对于底层原理的细致讲解，不可错过。地址：<https://www.elastic.co/blog>
- GitHub PR，其他人对 Elasticsearch 提交的改动，对于实际问题很有参考价值：<https://github.com/elastic/elasticsearch/pulls>
- GitHub issues，其他人的提议和 bug 反馈等：<https://github.com/elastic/elasticsearch/issues>
- 中文社区论坛，国内用户交流比较多，英文不好的同学和一些一般性问题也可以来此提问：<https://elasticsearch.cn>
- 中文社区的精彩分享，线下活动分享的 PPT 会放在这个地方，非常值得借鉴和学习，不可错过：<https://elasticsearch.cn/slides/>
- 社区电台，勇哥会经常对大规模应用 Elasticsearch 的企业做访谈，聊一聊他们的使用情况，遇到的问题及建议，并且普及一些大家可能不了解的知技术点，很多问题和解决方案都是通用的，可以借鉴和参考。订阅地址： <https://www.ximalaya.com/zhubo/111156131/>

此外，建议多参加 Meetup 及技术交流会等线下活动，很多问题还是线下交流效果更好，毕竟 PPT 上的语言太过简练，同时也可以得到比较权威的答案。

很多问题是大规模应用的时候才会遇到，当集群数据量比较小，请求也比较少的时候，Elasticsearch 基本不会出问题。而大规模应用的企业一般都在比较大型的互联网公司，线下聊一聊很大程度上让你可以对业界情况有一定了解。

我们将从入门到进阶的知识体系大致归纳如下：

![avatar](https://images.gitbook.cn/FiiOncIxLTbI_xyFF8CWgNeKTqo2)

努力吧，你也将成为 Elasticsearch 专家！同时也建议提交一些有价值的 PR，共同促进 Elasticsearch 发展。

### 寄语

本专栏着重讨论 Elasticsearch 在实际应用场景中所遇到和关心的问题。下一节将介绍如何规划新集群。

Elasticsearch 本身是一个分布式存储系统，也有一些特点像 NoSQL，同时又是一个全文检索系统，这就需要掌握更多的知识，同时可以思考一些其他类似系统的特点，例如在分段合并方面 HBase 是怎么做的，在磁盘管理、坏盘处理方面 HDFS 是怎么做的，等等。

由于 Elasticsearch 在使用方面的自由度比较大，我们希望告诉读者合理的部署和使用方式应该是什么样的，并且对于常见问题给出分析方法和解决方式，但是希望读者能够更多地思考背后的原理，掌握技术本质。大数据平台的很多理论都是相通的。

一个技术问题有多种解决方式，希望读者深入思考，关心背后原理，而非只是解决眼前问题。

## 2.如何规划新集群

当有一个新的业务准备使用 Elasticsearch，尤其是业务首次建设 Elasticsearch 集群时，往往不知道该如何规划集群大小，应该使用什么样的服务器？规划多少个节点才够用？

集群规模当然是越大越好，但是出于成本考虑，还是希望集群规模规划的尽量准确，能够满足业务需求，又有一些余量，不建议规划一个规模“刚刚好”的集群，因为当负载出现波动，或者一些其他偶然的故障时，会影响到业务的可用性，因此留一些余量出来是必要的。

### 1 规划数据节点规模

Elasticsearch 节点有多种角色，例如主节点，数据节点，协调节点等，默认情况下，每个节点都同时具有全部的角色。对于节点角色的规划我们将在下一个小节讨论，首先考虑一下我们需要多少个数据节点存储我们的数据。规划数据节点数量需要参考很多因素，有一些原则可以帮助我们根据业务情况进行规划。

#### 1.1 数据总量

数据总量是指集群需要存储的数据总大小，如果每天都有新数据入库，总量随之相应地增加。我们之所以要考虑数据总量，并非因为磁盘空间容量的限制，而是 JVM 内存的限制。

为了加快搜索速度，Lucene 需要将每个段的倒排索引都加载到 JVM 内存中，因此每一个 open 状态的索引都会在 JVM 中占据一部分常驻内存，这些是 GC 不掉的，而且这部分空间占用的比较大，并且由于堆内存不建议超过 32G，在磁盘使用率达到极限之前，JVM 占用量会先到达极限。

按照我们的经验，Elasticsearch 中 1TB 的 index 大约占用 2GB 的 JVM 内存，具体和字段数据类型及样本相关，有些会更多。一般情况下，我们可以按照这个比例来做规划。如果想要精确计算，业务可以根据自己的样本数据入库 1TB，然后查看分段所占的内存大小（实际上会比 REST 接口返回的值高一些）

```
curl -X GET "localhost:9200/_cat/nodes?v&h=name,segments.memory"
```

以 1TB 数据占用 2GB 内存，JVM 堆内存配置 31G ，垃圾回收器 CMS 为例，新生代建议配置 10G，old 区 21G，这 21G 内存分配一半给分段内存就已经很多了，想想看还没有做任何读写操作时 old 区就占用了一半，其他几个内存大户例如 bulk 缓冲，Query 缓存，indexing buffer，聚合计算等都可能会使用到 old 区内存，因此为了保证节点稳定，分段内存不超过 10G 比较好，换算成索引数据量为5TB。

因此，我们可以按照单个节点打开的索引数据总量不超过 5TB 来进行规划，如果预计入库 Elasticsearch 中的数据总量有 100TB 的数据（包括副分片所占用空间），那么数据节点的数量至少应该是: 100/5=20，此外出于冗余方面的考虑，还要多加一些数据节点。冗余节点的数量则和日增数据量以及故障转移能力相关。

可以看出这样规划出来的节点数是相对比较多的，带来比较高的成本预算。在新的 6.7 及以上的版本 Elasticsearch 增加了冻结索引的特性，这是一种冷索引机制，平时他可以不占内存，只有查询的时候才去加载到内存，虽然查询慢一些，但是节点可以持有更多的数据总量。

因此，如果你想要节点存储更多的数据量，在超出上述原则后，除了删除或 close 索引之外，一个新的选择是将它变成冻结状态。

#### 1.2 单个节点持有的最大分片数量

单个节点可以持有的最大分片数量并没有明确的界限，但是过多的分片数量会造成比较大的管理压力，官方给出的建议是，单个节点上所持有的分片数按 JVM 内存来计算：每 GB 的内存乘以 20。例如 JVM 内存为 30GB，那么分片数最大为：30*20=600个。当然分片数越少会越稳定。

但是使用这个参考值也会有些问题，当分片大小为 40GB 时，节点所持有的数据量为：40 * 600 = 24TB，按照 1TB 数据量占用 2GB JVM 内存来计算，所占用 JVM 内存为：24 * 2 = 48GB，已经远超 JVM 大小，因此我们认为一般情况下不必将单个节点可以持有的分片数量作为一个参考依据，只需要关心一个原则：让 JVM 占用率和 GC 时间保持在一个合理的范围。

考虑另一个极端情况，每个分片的数据量都很小，同样不必关心每个节点可以持有多少，对大量分片的管理属于主节点的压力。一般情况下，建议整个集群的分片数量建议不超过 10 万。

### 2 规划集群节点角色

一个 Elasticsearch 节点默认拥有所有的角色，分离节点角色可以让集群更加稳定，尤其是更加注重稳定性和可用性的在线业务上，分离节点角色是必要的。

#### 2.1 使用独立的主节点

集群有唯一的活跃主节点，他负责分片管理和集群管理等操作，如果主节点同时作为数据节点的角色，当活跃主节点失效的时候，例如网络故障，硬件故障，新的主节点当选后需要重新分配原主节点上持有的分片数据，导致集群在一段时间内处于 RED 和 YELLOW 状态。而独立部署的主节点不需要这个过程，新节点当选后集群可以迅速 GREEN。

另外，由于数据节点通常有较大的内存占用，GC 的影响也会导致混合部署的工作受到影响。因此如果集群很在意稳定性和可用性，我们建议数据节点有 3 个及以上时，应该独立部署 3 个独立的主节点，共 6 个节点。

#### 2.2 使用独立的协调节点

有时候，你无法预知客户端会发送什么样的查询请求过来，也许他会包括一个深度聚合，这种操作很容易导致节点 OOM，而数据节点离线，或者长时间 GC，都会对业务带来明显影响。

亦或者客户端需要进行许多占用内存很多的聚合操作，虽然不会导致节点 OOM，但也会导致节点 GC 压力较大，如果数据节点长时间 GC，查询延迟就会有明显抖动，影响查询体验。

此时最好的方式就是让客户端所有的请求都发到某些节点，这种节点不存储数据，也不作为主节点，即使 OOM 了也不会影响集群的稳定性，这就是仅查询节点（Coordinating only node）。

#### 2.3 使用独立的预处理节点

Elasticsearch 支持在将数据写入索引之前对数据进行预处理、内容富化等操作，这通过内部的 processor 和 pipeline 实现，如果你在使用这个特性，为了避免对数据节点的影响， 我们同样建议将他独立出来，让写请求发送到仅预处理节点。

独立节点的各个角色后的集群结构如下图所示，其中数据节点也是独立的。

![avatar](https://images.gitbook.cn/FpaZEUJQSLu8M-SGnz2Yjm07P2s1)

如果没有使用预处理功能，可以将读写请求都发送到协调节点。另外数据写入过程最好先进入 Kafka 之类的 MQ，来缓冲一下对集群的写入压力，同时也便于对集群的后期维护。

### 总结

本文介绍了如何规划集群节点数和集群节点角色，依据这些原则进行规划可以较好的保证集群的稳定性，可以适用于组件新集群时评估集群规模，以及在现有集群接入新业务时对集群资源的评估。

关于**索引**和**分片**的规划将在后续章节中介绍。

## 3.Elasticsearch 索引设计

Elasticsearch 开箱即用，上手十分容易。安装、启动、创建索引、索引数据、查询结果，整个过程，无需修改任何配置，无需了解 mapping，运作起来，一切都很容易。

这种容易是建立在 Elasticsearch 在幕后悄悄为你设置了很多默认值，但正是这种容易、这种默认的设置可能会给以后带来痛苦。

例如不但想对 field 做精确查询，还想对同一字段进行全文检索怎么办？shard 数不合理导致无法水平扩展怎么办？出现这些状况，大部分情况下需要通过修改默认的 mapping，然后 reindex 你的所有数据。

这是一个很重的操作，需要很多的资源。索引设计是否合理，会影响以后集群运行的效率和稳定性。

### 1 分析业务

当我们决定引入 Elasticsearch 技术到业务中时，根据其本身的技术特点和应用的经验，梳理出需要预先明确的需求，包括物理需求、性能需求。

在初期应用时，由于对这两方面的需求比较模糊，导致后期性能和扩展性方面无法满足业务需求，浪费了很多资源进行调整。

希望我总结的需求方面的经验能给将要使用 Elasticsearch 的同学提供一些帮助，少走一些弯路。下面分别详细描述。

#### 1.1 物理需求

根据我们的经验，在设计 Elasticsearch 索引之前，首先要合理地估算自己的物理需求，物理需求指数据本身的物理特性，包括如下几方面。

- 数据总量

业务所涉及的领域对象预期有多少条记录，对 Elasticsearch 来说就是有多少 documents 需要索引到集群中。

- 单条数据大小

每条数据的各个属性的物理大小是多少，比如 1k 还是 10k。

- 长文本

明确数据集中是否有长文本，明确长文本是否需要检索，是否可以启用压缩。Elasticsearch 建索引的过程是极其消耗 CPU 的，尤其对长文本更是如此。

明确了长文本的用途并合理地进行相关设置可以提高 CPU、磁盘、内存利用率。我们曾遇见过不合理的长文本处理方式导致的问题，此处在 mapping 设计时会专门讨论。

- 物理总大小

根据上面估算的数据总量和单条数据大小，就可以估算出预期的存储空间大小。

- 数据增量方式

这里主要明确数据是以何种方式纳入 Elasticsearch 的管理，比如平稳增加、定期全量索引、周期性批量导入。针对不同的数据增量方式，结合 Elasticsearch 提供的灵活设置，可以最大化地提高系统的性能。

- 数据生命周期

数据生命周期指进入到系统的数据保留周期，是永久保留、还是随着时间推移进行老化处理？老化的周期是多久？既有数据是否会更新？更新率是多少？根据不同的生命周期，合理地组织索引，会达到更好的性能和资源利用率。

#### 1.2 性能需求

使用任何一种技术，都要确保性能能够满足业务的需求，根据上面提到的业务场景，对于 Elasticssearch 来说，核心的两个性能指标就是索引性能和查询性能。

##### **索引性能需求**

Elasticsearch 索引过程需要对待索引数据进行文本分析，之后建立倒排索引，是个十分消耗 CPU 资源的过程。

对于索引性能来说，我们认为需要明确两个指标，一个是吞吐量，即单位时间内索引的数据记录数；另一个关键的指标是延时，即索引完的数据多久能够被检索到。

Elasticsearch 在索引过程中，数据是先写入 buffer 的，需要 refresh 操作后才能被检索到，所以从数据被索引到能被检索到之间有一个延迟时间，这个时间是可配置的，默认值是 1s。这两个指标互相影响：减少延迟，会降低索引的吞吐量；反之会增加索引的吞吐量。

##### **查询性能需求**

数据索引存储后的最终目的是查询，对于查询性能需求。Elasticsearch 支持几种类型的查询，包括：

- 1. 结构化查询

结构查询主要是回答 yes/no，结构化查询不会对结果进行相关性排序。如 terms 查询、bool 查询、range 查询等。

- 2. 全文检索

全文检索查询主要回答数据与查询的相关程度。如 match 查询、query_string 查询。

- 3. 聚合查询

无论结构化查询和全文检索查询，目的都是找到某些满足条件的结果，聚合查询则不然，主要是对满足条件的查询结果进行统计分析，例如平均年龄是多少、两个 IP 之间的通信情况是什么样的。

对不同的查询来说，底层的查询过程和对资源的消耗是不同的，我们建议根据不同的查询设定不同的性能需求。

### 2 索引设计

**此处索引设计指宏观方面的索引组织方式，即怎样把数据组织到不同的索引，需要以什么粒度建立索引，不涉及如何设计索引的 mapping。（mapping 后文单独讲）**

#### 2.1 按照时间周期组织索引

如果查询中有大量的关于时间范围的查询，分析下自己的查询时间周期，尽量按照周期（小时、日、周、月）去组织索引，一般的日志系统和监控系统都符合此场景。

按照日期组织索引，不但可以减少查询时参与的 shard 数量，而且对于按照周期的**数据老化**、**备份**、**删除**的处理也很方便，基本上相当于文件级的操作性能。

这里有必要提一下 `delete_by_query`，这种数据老化方式性能慢，而且执行后，底层并不一定会释放磁盘空间，后期 merge 也会有很大的性能损耗，对正常业务影响巨大。

#### 2.2 拆分索引

检查查询语句的 filter 情况，如果业务上有大量的查询是基于一个字段 filter，比如 protocol，而该字段的值是有限的几个值，比如 HTTP、DNS、TCP、UDP 等，最好把这个索引拆成多个索引。

这样每次查询语句中就可以去掉 filter 条件，只针对相对较小的索引，查询性能会有很大提高。同时，如果需要查询跨协议的数据，也可以在查询中指定多个索引来实现。

#### 2.3 使用 routing

如果查询语句中有比较固定的 filter 字段，但是该字段的值又不是固定的，我们建议在创建索引时，启用 routing 功能。这样，数据就可以按照 filter 字段的值分布到集群中不同的 shard，使参与到查询中的 shard 数减少很多，极大提高 CPU 的利用率。

#### 2.4 给索引设置别名

我们强烈建议在任何业务中都使用别名，绝不在业务中直接引用具体索引！

##### **别名是什么**

索引别名就像一个快捷方式，可以指向一个或者多个索引，我个人更愿意把别名理解成一个逻辑名称。

##### **别名的好处**

- 方便扩展

对于无法预估集群规模的场景，在初期可以创建单个分片的索引 index-1，用别名 alias 指向该索引，随着业务的发展，单个分片的性能无法满足业务的需求，可以很容易地创建一个两个分片的索引 index-2，在不停业务的情况下，用 alise 指向 index-2，扩展简单至极。

- 修改 mapping

业务中难免会出现需要修改索引 mapping 的情况，修改 mapping 后历史数据只能进行 reindex 到不同名称的索引，如果业务直接使用具体索引，则不得不在 reindex 完成后修改业务索引的配置，并重启服务。业务端只使用别名，就可以在线无缝将 alias 切换到新的索引。

#### 2.5 使用 Rollover index API 管理索引生命周期

对于像日志等滚动生成索引的数据，业务经常以天为单位创建和删除索引。在早期的版本中，由业务层自己管理索引的生命周期。

在 Rollover index API 出现之后，我们可以更方便更准确地进行管理：索引的创建和删除操作在 Elasticsearch 内部实现，业务层先定义好模板和别名，再定期调用一下 API 即可自动完成，索引的切分可以按时间、或者 DOC 数量来进行。

### 总结

在正式接入业务数据之前进行合理的索引设计是一个必要的环节，如果偷懒图方便用最简单的方式进行业务数据接入，问题就会在后期暴露出来，那时再想解决就困难许多。

下一节我们开始介绍**索引层面之下的分片设计**。

## 4.Elasticsearch 分片设计

Elasticsearch 的一个分片对应 Lucene 的一个索引，Elasticsearch 的核心就是将这些 Lucene 索引分布式化，提供索引和检索服务。可见，如何设计分片是至关重要的。

一个索引到底该设置几个主分片呢？由于单个分片只能处于 Elasticsearch 集群中的单个节点，分片太少，影响索引入库的并发度，以及以后的横向扩展性，如果分片过大会引发查询、更新、迁移、恢复、平衡等性能问题。

### 1 主分片数量确定

我们建议综合考虑分片物理大小因素、查询压力因素、索引压力因素，来设计分片数量。

#### 1.1 物理大小因素

建议单个分片的物理大小不大于 50GB，之所以这样建议，基于如下几个因素：

- **更快的恢复速度**

集群故障后，更小的分片相对大分片来讲，更容易使集群恢复到 Green 状态。

- **merge 过程中需要的资源更少**

Lucene 的 segment merge 过程需要两倍的磁盘空间，如果分片过大，势必需要更大的临时磁盘空间用于 merge，同时，分片过大 merge 过程持续时间更长，将对 IO 产生持续的压力。

- **集群分片分布更容易均衡**

分片过大，Elasticsearch 内部的平衡机制需要更多的时间。

- **提高 update 操作的性能**

对 Elasticsearch 索引进行 update 操作，底层 Lucene 采用的是先查找，再删除，最后 index 的过程。如果在分片比较大的索引上有比较多的 update 操作，将会对性能产生很大的影响。

- **影响缓存**

节点的物理内存是有限的，如果分片过大，节点不能缓存分片必要的数据，对一些数据的访问将从物理磁盘加载，可想而知，对性能会产生多大的影响。

#### 1.2 查询压力因素

单个 shard 位于一个节点，如果索引只有一个 shard，则只有一个节点执行查询操作。如果有多个 shard 分部在不同的节点，多个节点可以并行执行，最后归并。

但是过多的分片会增加归并的执行时间，所以考虑这个因素，需要根据业务的数据特点，以贴近真实业务的查询去测试，不断加大分片数量，直到查询性能开始降低。

- **索引压力因素**

单个 shard 只能位于一块单个节点上，索引过程是 CPU 密集型操作，单个节点的入库性能是有限的，所以需要把入库的压力分散到多个节点来满足写入性能。单纯考虑索引性能，可以根据单个节点的索引性能和需要索引的总性能来估算分片数量。

### 2 副本数量

副本是主分片的拷贝，可以响应查询请求、防止数据丢失、提高集群可用性等，但是副本不是“免费”的，需要占用与主分片一样的资源，包括 CPU、内存、磁盘，副本数量的确定等涉及多方面的因素。

#### 2.1 数据可靠性

明确自己的业务需要多高的可靠性和可用性。依据 Elasticsearch 的内部分片分布规则，同一索引相同编号的分片不会处于同一个 node，多一份副本就多一份数据安全性保障。

#### 2.2 索引性能

副本和主分片在索引过程中执行和主分片一样的操作，如果副本过多，有多少副本就会有几倍的 CPU 资源消耗在索引上，会拖累整个集群的索引吞吐量，对于索引密集型的业务场景影响巨大。所以要在数据安全型和索引性能上做权衡处理来确定副本的数量。

#### 2.3 查询性能

副本可以减轻对主分片的查询压力，这里可能说查询次数更为合理。节点加载副本以提供查询服务和加载主分片消耗的内存资源是完全相同的，增加副本的数量势必增加每个 node 所管理的分片数，因此会消耗更多的内存资源，Elasticsearch 的高速运行严重依赖于操作系统的 Cache。

如果节点本身内存不充足，副本数量的增加会导致节点对内存的需求的增加，从而降低 Lucene 索引文件的缓存效率，使 OS 产生大量的换页，最终影响到查询性能。当然，在资源充足的情况下，扩大副本数是肯定可以提高集群整体的 QPS。

### 3 分片分布

ELasticsearch 提供的关于 shard 平衡的两个参数是 `cluster.routing.allocation.balance.shard` 和 `cluster.routing.allocation.balance.index`。

第一个参数的意思是让每个节点维护的分片总数尽量平衡，第二个参数的意思是让每个索引的的分片尽量平均的分散到不同的节点。

如果集群中有不同类型的索引，而且每个类型的索引的索引方式、物理大小不一致，很容易造成节点间磁盘占用不均衡、不同节点间堆内存占用差异大的问题，从而导致集群不稳定。

所以我们建议尽量保证不同索引的 shard 大小尽量相近，以获得实质意义上的均衡分片分布。

### 4 集群分片总数控制

由于 Elasticsearch 的集群管理方式还是中心化的，分片元信息的维护在选举出来的 master 节点上，分片过多会增加查询结果合并的时间，同时增加集群管理的负担。

根据我们的经验，单个集群的分片超过 10 万时，集群维护相关操作例如创建索引、删除索引等就会出现缓慢的情况。所以在实践中，尽量控制单个集群的分片总数在 10 万以内。

### 5 【案例分析】大分片数据更新引发的 IO 100% 异常

分片大小对某些业务类型来讲会产生致命的影响，这里介绍一个我们遇到的一个案例，由于分片不合理导致了很严重的性能问题。

#### 5.1 问题背景

有一个集群由很多业务部门公用，该集群为 10 个节点，单节点 128G 内存，CPU 为 40 线程，硬盘为 4T*12 的配置，存储使用总量在 20%。业务 A 反应他的 bulk 操作很慢，需要分钟级才能完成。

经过与业务沟通后，了解到他们单次 bulk 1000 条数据，单条数据大小为 1k，这种 bulk 并不大，因此速度慢肯定是不正常的现象。

#### 5.2 问题分析

有了以上的背景信息，开始问题排查。该索引大小为 1.2T，5 个主分片。登录到集群服务器后台，在业务运行过程中，通过 top 查看，CPU 利用率在 30%，在正常范围内。但是 iowait 一直持续在 20% 左右，问题初步原因应该在 IO 上。

随即通过 iostat 观察磁盘的状况，发现有一块盘的 IO 持续 100%。登录其他几个服务器，发现每个服务器都有一块盘的 IO 处于 100% 状态。

之后通过分析该索引的分布情况，发现 IO 利用率高的磁盘都有这个索引对应的 shard，难道是这个索引导致的？

因为这些磁盘上还有其他的索引，我们现在也只是推测。打开 iotop，发现有些线程的 io 持续有大量读取。

将线程 tid 转换成 16 进制，通过 jtack 查询对应的线程，发现是 Lucene 的 refresh 操作触发的。但是只通过线程堆栈扔无法确认是由该索引的 bulk 操作引起。之后通过跟踪系统调用：

```
strace -t -T -y -p $tid
```

发现每秒有数百次的 pread 系统调用，而且读取的目录全部为该索引所在目录，使得磁盘 IO 一直处于满负荷状态。bulk 是写操作，不会引起大量的读。

有一种情况是，如果待索引的数据的 id 是应用程序自己生成的，底层 Lucene 在索引时，要去查找对应的文档是否存在。

跟业务人员沟通后，id 确实是由应用程序自己控制。这样问题就清晰了，在这个索引的单个分片上 bulk 200 条数据，底层 Lucene 要进行 200 次查找以确定对应的数据是否存在。

#### 5.3 问题解决

通过以上分析，在目前的情况下，紧急缩小单个分片的物理大小，增加该索引的 shard 数到 200，使数据均衡到更多的磁盘，对旧索引的数据进行 reindex。迁移完成后，同样的 bulk 操作在 1s 左右执行完成。

### 总结

没有不好的技术，只有不合理的使用。通过减少单个分片的物理大小，将数据分散到更多的 shard，从而将 IO 压力分散到了集群内的所有磁盘上，暂时可以解决目前 bulk 慢的问题。

但是考虑到 Lucene 的技术特点，并不适用于有大量更新的业务场景，还是需要重构业务，以追加的方式写入新数据，通过数据的版本或者时间戳来查询最新的数据，并定期对数据进行整理，以达到最优的性能。

## 5.如何设计映射

### 1 映射 （mapping） 基础

mapping 定义了文档的各个字段如何被索引以及如何存储。我们可以把 Elasticsearch 的 mapping 看做 RDBMS 的 schema。

虽然 Elasticsearch 可以根据索引的数据动态的生成 mapping，我们仍然建议在创建索引时明确的定义自己的 mapping，不合理的 mapping 会引发索引和查询性能降低，磁盘占用空间变大。错误的 mapping 会导致与预期不符的查询结果。

### 2 选择合适的数据类型

#### 2.1 分清 text 和 keyword

**text 类型**

- 用于存储全文搜索数据，例如：邮箱内容、地址、代码块、博客文章内容等。
- 默认结合 standard analyzer（标准解析器）对文本进行分词、倒排索引。
- 默认结合标准分析器进行词命中、词频相关度打分。

**keyword 类型**

- 用于存储需要精确匹配的数据。例如手机号码、主机名、状态码、邮政编码、标签、年龄、性别等数据。
- 用于筛选数据（如 `select * from x where status=‘open’`）、排序、聚合（统计）。
- 直接将完整的文本保存到倒排索引中，并不会对字段的数据进行分词。

如果 keyword 能满足需求，尽量使用 keyword 类型。

### 3 mapping 和 indexing

mapping 定义得是否合理，将直接影响 indexing 性能，也会影响磁盘空间的使用。

#### 3.1 mapping 无法修改

Ealsticsearch 的 mapping 一旦创建，只能增加字段，不能修改已有字段的类型。

#### 3.2 几个重要的 meta field

**1. _all**

虽然在 Elasticsearch 6.x 中，`_all` 已经是 deprecated，但是考虑到 6.x 之前的版本创建的索引 `_all` 字段是默认启用的，这里有必要详细说说明下该字段的含义。

`_all` 字段是一个 text 字段，它将你索引的单个文档的所有字段连接成一个超级串，之后进行分词、索引。如果你不指定字段，`query_string` 查询和 `simple_query_string` 查询默认查询 `_all` 字段。

`_all` 字段不是“免费”的，索引过程会占用额外的 CPU 资源，根据测试，在我们的数据集上，禁用 `_all` 字段后，索引性能可以提高 30%+，所以，如果您在没有明确 `_all` 含义的情况下，历史索引没有禁用 `_all` 字段，建议您重新审视该字段，看是否需要禁用，并 reindex，以获取更高的索引性能以及占用更少的磁盘空间。如果 `_all` 提供的功能对于您的业务必不可少，考虑使用 `copy_to` 参数代替 `_all` 字段。

**2. _source**

`_source` 字段存储了你的原始 JSON 文档 `_source` 并不会被索引，也就是说你并不能直接查询 `_source` 字段，它主要用于查询结果展示。所以启用该字段并不会对索引性能造成很大的影响。

除了用于查询结果展示，Ealsticsearch 的很多功能诸如 Reindex API、高亮、`update_by_query` 都依赖该字段。

所以实践中，我们不建议禁用该字段。如果磁盘空间是瓶颈，我们建议优先考虑禁用 `_all` 字段，`_all` 禁用达不到预期后，考虑提高索引的压缩级别，并合理使用 `_source` 字段的 `includes` 和 `excludes` 属性。

#### 3.3 几个重要的 mapping 参数

**1. index 和 store**

首先明确一下，index 属性关乎该字段是否可以用于查询，而 store 属性关乎该字段是否可以在查询结果中单独展示出来。**通过跟一些业务开发人员接触，发现经常有对这两个属性不明确的情况。**

index 后的字段可以用于 search，默认你定义到 mapping 里的字段该属性的值都为 “true”。indexing 过程耗费 cpu 资源，不需要查询的字段请将该属性值设为 “false”，**在业务部门使用过程中，常见的一个问题是一半以上的字段不会用于查询，但是并没有明确设置该属性，导致索引性能下降。**

由于 Elasticsearch 默认提供了 `_source` 字段，所以，大部分情况下，你无须关心 `store` 属性，默认的 “false” 能满足需求。

##### **enabled**

对于不需要查询，也不需要单独 `fetch` 的字段，`enable` 可以简化 mapping 的定义。

例如：

```
"session_data": { 
    "enabled": false
}
```

等同于

```
"session_data": { 
    "type": "text",
    "index": false,
    "store": false
}
```

##### **不要使用 fielddata**

自从 Elasticsearch 加入 `doc_value` 特性后，fielddata 已经没有使用的必要了。

有两点原因，首先，对于非 text 字段，`doc_value` 可以实现同样的功能，但是占用的内存资源更少。

其次，对于 `text` 字段启用 `fielddata`，由于 `text` 字段会被分词，即使启用了 `fielddata`，在其上进行聚合、排序通常是没有意义的，得到的结果也并不是期望的结果。如果确实需要对 `text` 字段进行聚合，通常使用 `fields` 会得到你想要的结果。

```
PUT my_index
{
    "mappings": {
    "my_type": {
            "properties": {
                "city": {
                    "type": "text",
                    "fields": {
                        "raw": { 
                            "type":  "keyword"
                    }
                    }
                }
            }
        }
    }
}
```

之后通过 `city` 字段进行全文查询，通过 `city.raw` 字段进行聚合和排序。

**2. doc_values**

Elasticsearch 默认对所有支持 `doc_values` 的类型启用了该功能，**对于不需要聚合、排序的字段，建议禁用以节省磁盘空间。**

```
PUT my_index
{
  "mappings": {
    "_doc": {
      "properties": {
        "session_id": { 
          "type":       "keyword",
          "doc_values": false
        }
      }
    }
  }
}
```

#### 3.4 `null_value`

null 不能索引，也不能用于检索。null_value 可以让你明确的指定 null 用什么值代替，之后可以用该替代的值进行检索。

### 4 mapping 和 searching

#### 4.1 预热 global ordinals

global ordinals 主要用于加快 keyword 类型的 terms 聚合，由于 global ordinals 占用内存空间比较大，Elasticsearch 并不知道要对哪些字段进行聚合，所以默认情况下，Elasticsearch 不会加载所有字段的 global ordinals。可以通过修改 mapping 进行预加载。如下所示：

```
PUT index
{
  "mappings": {
    "type": {
      "properties": {
        "foo": {
          "type": "keyword",
          "eager_global_ordinals": true
        }
      }
    }
  }
}
```

#### 4.2 将数字标识映射成 keyword 类型

Elasticsearch 在索引过程中，numbers 类型对 range 查询进行了优化，keyword 类型对 terms 查询进行了优化，如果字段字面上看是 numbers 类型，但是并不会用于 range 查询，只用于 terms 查询，将字段映射成 keyword 类型。例如 isbn、邮政编码、数据库主键等。

### 总结

mapping 定义的是否合理，关系到索引性能、磁盘使用效率、查询性能，本章着重讨论了影响这些的关键 mapping 参数和 Elasticsearch 的 meta fileds，深入理解本章的内容后，mapping 应该不会成为系统瓶颈的主因。

第一部分的内容到此结束，这部分内容的目的介绍如何“合理地”使用 Elasticsearch，只要使用方式合理，Elasticsearch 本身问题并不多。很多时候遇到的问题都是初期的规划，使用不当造成的。

## 6.分片管理

一个 shard 本质上就是一个 Lucene 索引，也是 Elasticsearch 分布式化 Lucene 的关键抽象，是 Elasticsearch 管理 Lucene 文件的最小单位。

所以，Elasticsearch 提供了大量的接口，可以对集群内的 shard 进行管理。

### 1 常用 shard 级 REST API 操作

#### 1.1 shard 移动

将分片从一个节点移动到另一个节点，在使用 Elasticsearch 中，鲜有需要使用该接口去移动分片，更多的是使用 AllocationDecider 参数以及平衡参数去自动调整 shard 的位置。

在一些特别的情况下，例如发现大部分热点数据集中在几个节点，可以考虑手工 move 一下。

```
curl -XPOST 'localhost:9200/_cluster/reroute' -d '{
    "commands" : [ {
        "move" :
            {
              "index" : "test", "shard" : 0,
              "from_node" : "node1", "to_node" : "node2"
            }
        }
    ]
}'
```

#### 1.2 explain api

explain api 是 Elasticsearch 5.x 以后加入的非常实用的运维接口，可以用来诊断 shard 为什么没有分配，以及 shard 为什么分配在某个节点。

```
    curl -XGET "http://localhost:9200/_cluster/allocation/explain
      {
          "index": "myindex",
          "shard": 0,
          "primary": true
      }
```

如果不提供参数调用该 api，Elasticsearch 返回第一个 unassigned shard 未分配的原因。

```
    GET /_cluster/allocation/explain
```

#### 1.3 分配 stale 分片

在索引过程中，Elasticsearch 首先在 primary shard 上执行索引操作，之后将操作发送到 replica shards 执行，通过这种方式使 primary 和 replica 数据同步。

对于同一个分片的所有 replicas，Elasticsearch 在集群的全局状态里保存所有处于同步状态的分片，称为 in-sync copies。

如果修改操作在 primary shard 执行成功，在 replica 上执行失败，则 primary 和 replica 数据就不在同步，这时 Elasticsearch 会将修改操作失败的 replica 标记为 stale，并更新到集群状态里。

当由于某种原因，对于某个 shard 集群中可用数据只剩 stale 分片时，集群会处于 red 状态，并不会主动将 stale shard 提升为 primary shard，因为该 shard 的数据不是最新的。这时如果不得不将 stale shard 提升为主分片，需要人工介入：

```
curl -XPOST "http://localhost:9200/_cluster/reroute" -d '{
        "commands":[{
            "allocate_stale_primary":{
                "index":"my_index",
                "shard":"10",
                "node":"node_id",
                "accept_data_loss":true
            }
        }]
    }'
```

#### 1.4 分配 empty 分片

当由于 lucene index 损坏或者磁盘故障导致某个分片的主副本都丢失时，为了能使集群恢复 green 状态，最后的唯一方法是划分一个空 shard。

```
curl -XPOST "http://localhost:9200/_cluster/reroute" -d '{
        "commands":[{
            "allocate_empty_primary":{
                "index":"my_index",
                "shard":"10",
                "node":"node_id",
                "accept_data_loss":true
            }
        }]
    }'
```

一定要慎用该操作，会导致对应分片的数据完全清空。

### 2 控制 shard 数量

一般来说，增加主分片数量可以增加写入速度和查询速度，因为数据分布到了更多的节点，可以利用更多的计算和 IO 资源。增加副分片数量可以提升查询速度，并发的查询可以在多个分片之间轮询。

但是 shard 管理并不是 “免费” 的，shard 数量过多会消耗更多的 cpu、内存资源，引发一系列问题，主要包括如下几个方面。

#### 2.1 shard 过多问题

- **引起 master 节点慢**

任一时刻，一个集群中只有一个节点是 master 节点，master 节点负责维护集群的状态信息，而且状态的更新是在单线程中运行的，大量的 shard 会导致集群状态相关的修改操作缓慢，比如创建索引、删除索引，更新 setting 等。

单个集群 shard 超过 10 万，这些操作会明显变慢。集群在恢复过程中，会频繁更显状态，引起恢复过程漫长。

我们曾经在单个集群维护 30 多万分片，集群做一次完全重启有时候需要2-4个小时的时间，对于业务来说是难以忍受的。

- **查询慢**

查询很多小分片会降低单个 shard 的查询时间，但是如果分片过多，会导致查询任务在队列中排队，最终可能会增加查询的整体时间消耗。

- **引起资源占用高**

Elasticsearch 协调节点接收到查询后，会将查询分发到查询涉及的所有 shard 并行执行，之后协调节点对各个 shard 的查询结果进行归并。

如果有很多小分片，增加协调节点的内存压力，同时会增加整个集群的 cpu 压力，甚至发生拒绝查询的问题。因为我们经常会设置参与搜索操作的分片数上限，以保护集群资源和稳定性，分片数设置过大会更容易触发这个上限。

#### 2.2 如何减少 shard

- **设置合理的分片数**

  创建索引时，可以指定 `number_of_shards`，默认值是 5，对于物理大小只有几个 GB 的索引，完全可以设置成更小的值。

- **shard 合并**

  如果集群中有大量的 MB、KB 级分片，可以通过 Elasticsearch 的 shard 合并功能，将索引的多个分片合并成 1 个分片。

- **删除无用索引** 根据业务场景，每个索引都有自己的生命周期。尤其对于日志型索引，超过一定时间周期后，业务就不再访问，应该及时从集群中删除。

- **控制 replica 数量**

  replica 可以提高数据安全性，并可以负载读请求，但是会增加写入时的资源消耗，同时使集群维护的分片数成倍的增长，引起上面提到的诸多问题。所以要尽量降低 replica 数量。

### 3 shard 分配

Elasticsearch 通过 AllocationDecider 策略来控制 shard 在集群内节点上的分布。

#### 3.1 allocation deciders

- **same shard allocation decider**

  控制一个 shard 的主副本不会分配到同一个节点，提高了数据的安全性。

- **MaxRetryAllocationDecider**

  该 Allocationdecider 防止 shard 分配失败一定次数后仍然继续尝试分配。可以通过 index.allocation.max_retries 参数设置重试次数。当重试次数达到后，可以通过手动方式重新进行分配。

  ```
  curl -XPOST "http://localhost:9200/_cluster/reroute?retry_failed"
  ```

- **awareness allocation decider**

  可以确保主分片及其副本分片分布在不同的物理服务器，机架或区域之间，以尽可能减少丢失所有分片副本的风险。

- **filter allocation decider**

  该 decider 提供了动态参数，可以明确指定分片可以分配到指定节点上。

  ```
  index.routing.allocation.include.{attribute}
  index.routing.allocation.require.{attribute}
  index.routing.allocation.exclude.{attribute}
  ```

  require 表示必须分配到具有指定 attribute 的节点，include 表示可以分配到具有指定 attribute 的节点，exclude 表示不允许分配到具有指定 attribute 的节点。Elasticsearch 内置了多个 attribute，无需自己定义，包括 `_name`, `_host_ip`, `_publish_ip`, `_ip`, `_host`。attribute 可以自己定义到 Elasticsearch 的配置文件。

- **disk threshold allocation decider**

  根据磁盘空间来控制 shard 的分配，防止节点磁盘写满后，新分片还继续分配到该节点。启用该策略后，它有两个动态参数。

  `cluster.routing.allocation.disk.watermark.low`参数表示当磁盘空间达到该值后，新的分片不会继续分配到该节点，默认值是磁盘容量的 85%。

  `cluster.routing.allocation.disk.watermark.high`参数表示当磁盘使用空间达到该值后，集群会尝试将该节点上的分片移动到其他节点，默认值是磁盘容量的 90%。

- **shards limit allocation decider**

  通过两个动态参数，控制索引在节点上的分片数量。其中 `index.routing.allocation.total _ shards_per_node` 控制单个索引在一个节点上的最大分片数；

  `cluster.routing.allocation.total_shards_per_node` 控制一个节点上最多可以分配多少个分片。

  应用中为了使索引的分片相对均衡的负载到集群内的节点，`index.routing.allocation.total_shards_per_node` 参数使用较多。

### 4 shard 平衡

分片平衡对 Elasticsearch 稳定高效运行至关重要。下面介绍 Elasticsearch 提供的分片平衡参数。

#### 4.1 Elasticsearch 分片平衡参数

- **cluster.routing.rebalance.enable**

  控制是否可以对分片进行平衡，以及对何种类型的分片进行平衡。可取的值包括：`all`、`primaries`、`replicas`、`none`，默认值是`all`。

  `all` 是可以对所有的分片进行平衡；`primaries` 表示只能对主分片进行平衡；`replicas` 表示只能对副本进行平衡；`none`表示对任何分片都不能平衡，也就是禁用了平衡功能。该值一般不需要修改。

- **cluster.routing.allocation.balance.shard**

  控制各个节点分片数一致的权重，默认值是 0.45f。增大该值，分配 shard 时，Elasticsearch 在不违反 Allocation Decider 的情况下，尽量保证集群各个节点上的分片数是相近的。

- **cluster.routing.allocation.balance.index**

  控制单个索引在集群内的平衡权重，默认值是 0.55f。增大该值，分配 shard 时，Elasticsearch 在不违反 Allocation Decider 的情况下，尽量将该索引的分片平均的分布到集群内的节点。

- **index.routing.allocation.total_shards_per_node**

  控制单个索引在一个节点上的最大分片数，默认值是不限制。

当使用`cluster.routing.allocation.balance.shard`和`index.routing.allocation.total_shards_per_node`不能使分片平衡时，就需要通过该参数来控制分片的分布。

所以，我们的经验是：**创建索引时，尽量将该值设置的小一些，以使索引的 shard 比较平均的分布到集群内的所有节点。**

但是也要使个别节点离线时，分片能分配到在线节点，对于有 10 个几点的集群，如果单个索引的主副本分片总数为 10，如果将该参数设置成 1，当一个节点离线时，集群就无法恢复成 Green 状态了。

所以我们的建议一般是保证一个节点离线后，也可以使集群恢复到 Green 状态。

#### 4.2 关于磁盘平衡

Elasticsearch 内部的平衡策略都是基于 shard 数量的，所以在运行一段时间后，如果不同索引的 shard 物理大小差距很大，最终会出现磁盘使用不平衡的情况。

所以，目前来说避免该问题的以办法是让集群内的 shard 物理大小尽量保持相近。

### 总结

主节点对 shard 的管理是一种代价相对比较昂贵的操作，因此在满足需求的情况下建议尽量减少 shard 数量，将分片数量和分片大小控制在合理的范围内，可以避免很多问题。

下一节我们将介绍**分片内部的段合并**相关问题。

## 7.段合并优化及注意事项

当新文档被索引到 Elasticsearch，他们被暂存到索引缓冲中。当索引缓冲达到 flush 条件时，缓冲中的数据被刷到磁盘，这在 Elasticsearch 称为 refresh，refresh 会产生一个新的 Lucene 分段，这个分段会包含一系列的记录正排和倒排数据的文件。

当他们还在索引缓冲，没有被 refresh 到磁盘的时候，是无法被搜索到的。因此为了保证较高的搜索可见性，默认情况下，每1秒钟会执行一次 refresh。

因此这会频繁地产生 Lucene 段文件，为了降低需要打开的 fd 的数量，优化查询速度，需要将这些较小的 Lucene 分段合并成较大的段，引用一张官网的示意图如下：

![avatar](https://images.gitbook.cn/FsziqJaho_e2aQYv6APf4sy0kCGK)

在段合并之前，有四个较小的分段对搜索可见，段合并过程选择了其中三个分段进行合并，当合并完成之后，老的段被删除：

![avatar](https://images.gitbook.cn/Fk7gDD8YDlbU8YuhK_Xt_zP1LuFR)

在段合并（也可以称为 merge）的过程中，此前被标记为删除的文档被彻底删除。因此 merge 过程是必要的，但是进行段合并耗费的资源比较高，他不能仅仅进行 io 的读写操作就完成合并过程，而是需要大量的计算，因此数据入库过程中有可能会因为 merge 操作占用了大量 CPU 资源。进而影响了入库速度。我们可以通过 `_nodes/hot_threads` 接口查看节点有多少个线程在执行 merge。

`hot_threads` 接口返回每个节点，或者指定节点的热点线程，对于 merge 来说，他的堆栈长成下面这个样子：

![avatar](https://images.gitbook.cn/FuX5iCYIzPTx432Xeh9N8JF9-o-D)

可以通过红色框中标记出来的文字来找到 merge 线程。

### 1 merge 优化

很多时候我们希望降低 merge 操作对系统的影响，通常从以下几个方面入手：

- 降低分段产生的数量和频率，少生成一些分段，自然就可以少执行一些 merge 操作
- 降低最大分段大小，达到我们指定的大小后，不再进行段合并操作。这可以让较大的段不再参与 merge，节省大量资源，但最终分段数会更多一些

具体来说可以执行以下调整：

**1. refresh**

最简单的是增大 refresh 间隔时间，可以动态的调整索引级别的 `refresh_interval` 参数，-1 代表关闭自动刷新。

具体取值应该参考业务对搜索可见性的要求。在搜索可见性要求不高的业务上，我们将此值设置为分钟级。

**2. indices.memory.index_buffer_size**

索引缓冲用于存储刚刚被索引的文档，当缓冲满的时候，这些数据被刷到磁盘产生新的分段。默认值为整个堆内存的10%，可以适当提高此值，例如调整到30%。该参数不支持动态调整。

**3. 避免更新操作**

尽量避免更新文档，也就是说，尽量避免使用同一个 docid 进行文档更新。

对文档的 update 需要先执行 Get 操作，再执行 Index 操作，执行 Get 操作时，realtime 参数被设置为 true，在 Elasticsearch 5.x 及以后之后的版本中，这会导致一个对索引的 refresh 操作。

同理，Get 操作默认是实时的，应该尽量避免客户端直接发起的 Get 操作，或者将 Get 操作的请求中将 `realtime` 参数设置为 false。

**4. 调整 merge 线程数**

执行 merge 操作的线程池由 Lucene 创建，其最大线程池数由以下公式计算：

```
Math.max(1, Math.min(4, Runtime.getRuntime().availableProcessors() / 2))
```

你可以通过以下配置项来调整：

```
index.merge.scheduler.max_thread_count
```

**5. 调整段合并策略**

Lucene 内置的段合并策略有三种，默认为分层的合并策略：tiered。对于这种策略，我们可以调整下面两个值，来降低段合并次数。

```
index.merge.policy.segments_per_tier
```

该参数设置每层允许存在的分段数量，值越小，就需要更多的合并操作，但是最终分段数越少。默认为10，可以适当增加此值，我们设置为24。

注意该值必须大于等于 `index.merge.policy.max_merge_at_once`(默认为10)。

```
index.merge.policy.max_merged_segment
```

当分段达到此参数配置的大小后，不再参与后续的段合并操作。默认为 5Gb，可以适当降低此值，我们使用 2Gb，但是索引最终会产生相对更多一些的分段，对搜索速度有些影响。

### 2 force merge 成几个？

在理想情况下，我们应该对不再会有新数据写入的索引执行 force merge，force merge 最大的好处是可以提升查询速度，并在一定情况下降低内存占用。

未进行 force merge 的时候，对分片的查询需要遍历查询所有的分段，很明显，在一次查询中会涉及到很多文件的随机 io，force merge 降低分段数量大大降低了所需随机 io 的数量，带来查询性能的提升。

但是对一个分片来说， force merge 成几个分段比较合适？这没有明确的建议值，我们的经验是，维护分片下的分段数量越少越好，理想情况下，你可以 force merge 成一个，但是 merge 过程占用大量的网络、io、以及计算资源。

如果在业务底峰期开始执行的 force merge 到了业务高峰期还没执行完，已经影响到集群的性能，就应该增加 force merge 最终的分段数量。

目前我们让分段合并到 2GB 就不再合并，因此 force merge 的数量为：**分片大小/2GB**

### 3 flush 和 merge 的其他问题

我们总结一下关于 flush 和 merge 的一些原理，这是一些新同学学习 Elasticsearch 过程中的常见问题。

- 从索引缓冲刷到磁盘的 refresh 过程是同步执行的。

  像 hbase 这种从缓冲刷到磁盘的时候是异步的，hbase 会开辟一个新的缓冲去写新数据。但同步执行不意味着这是耗时很久的 io 操作，因为数据会被先写入到系统 cache，因此通常情况下这不会产生磁盘 io，很快就会执行完成。

  但是这个过程中操作系统会判断 page cache 的脏数据是否需要进行落盘，如果需要进行落盘，他先执行异步落盘，如果异步的落盘来不及，此时会阻塞写入操作，执行同步落盘。

  因此在 io 比较密集的系统上，refresh 有可能会产生阻塞时间较长的情况，这种情况下可以调节操作系统内核参数，让脏数据尽早落盘，需要同时调整异步和同步落盘的阈值，具体可以参考《Elasticsearch 源码解析与优化实战》 21.3.3 章节。

- merge 策略和具体的执行过程，以及merge 过程所用的线程池是 Lucene 维护的，而不是在 Elasticsearch 中。

- merge 过程是异步执行的，也就是说，refresh 过程中判断是否需要执行 merge，如果需要执行 merge，merge 不会阻塞 refresh 操作。

- 很多同学对 Elasticsearch 的刷盘与 Lucene 的 commit 之间的关系容易搞混乱，我们在此用一句话总结两者之间概念的关系： Elasticsearch 的 refresh 调用 Lucene 的 flush；Elasticsearch 的 flush 调用 Lucene 的 commit。

### 总结

本章介绍了分段合并的原理及实际使用过程中常见问题，以及应对方法，段合并是必要的，但是堆栈中如果出现过多的 merge 线程，并且在长时间周期内占据堆栈，则需要注意一下，可能需要一些调整，在调整之前，应该首先排查一下如此多的 merge 是什么原因产生的。

第二部分的这两节课程我们深入介绍了 Elasticsearch 分片及分段在实际应用时的原则和注意事项，下一节我们将介绍一些 Elasticsearch 的 Cache 机制和实际应用。

## 8.Elasticsearch Cache

### 1 Elasticsearch Cache 机制

#### 1.1 Cache 类型

Elasticsearch 内部包含三个类型的读缓冲，分别为 **Node Query Cache**、**Shard Request Cache** 以及 **Fielddata Cache**。

**1. Node Query Cache**

Elasticsearch 集群中的每个节点包含一个 Node Query Cache，由该节点的所有 shard 共享。该 Cache 采用 LRU 算法，Node Query Cache 只缓存 filter 的查询结果。

**2. Shard Request Cache**

Shard Request Cache 缓存每个分片上的查询结果跟 Node Query Cache 一样，同样采用 LRU 算法。默认情况下，Shard Request Cache 只会缓存设置了 `size=0` 的查询对应的结果，并不会缓存 hits，但是会缓存命中总数，aggregations，and suggestions。

有一点需要注意的是，Shard Request Cache 把整个查询 JSON 串作为缓存的 key，如果 JSON 对象的顺序发生了变化，也不会在缓存中命中。所以在业务代码中要保证生成的 JSON 是一致的，目前大部分 JSON 开发库都支持 canonical 模式。

**3. Fielddata Cache**

Elasticsearch 从 2.0 开始，默认在非 text 字段开启 `doc_values`，基于 `doc_values` 做排序和聚合，可以极大降低节点的内存消耗，减少节点 OOM 的概率，性能上损失却不多。

5.0 开始，text 字段默认关闭了 Fielddata 功能，由于 text 字段是经过分词的，在其上进行排序和聚合通常得不到预期的结果。所以我们建议 Fielddata Cache 应当只用于 global ordinals。

#### 1.2 Cache 失效

不同的 Cache 失效策略不同，下面分别介绍：

**1. Node Query Cache**

Node Query Cache 在 segment 级缓存命中的结果，当 segment 被合并后，缓存会失效。

**2. Shard Request Cache**

每次 shard 数据发生变化后，在分片 refresh 时，Shard Request Cache 会失效，如果 shard 对应的数据频繁发生变化，该缓存的效率会很差。

**3. Fielddata Cache**

Fielddata Cache 失效机制和 Node Query Cache 失效机制完全相同，当 segment 被合并后，才会失效。

#### 1.3 手动清除 Cache

Elasticsearch 提供手动清除 Cache 的接口：

```
POST /myindex/_cache/clear?query=true      
POST /myindex/_cache/clear?request=true    
POST /myindex/_cache/clear?fielddata=true   
```

Cache 对查询性能很重要，不建议在生产环境中进行手动清除 Cache。这些接口一般在进行性能压测时使用，在每轮测试开始前清除缓存，减少缓存对测试准确性的影响。

### 2 Cache 大小设置

#### 2.1 关键参数

下面几个参数可以控制各个类型的 Cache 占用的内存大小。

- `indices.queries.cache.size`：控制 Node Query Cache 占用的内存大小，默认值为堆内存的10%。
- `index.queries.cache.enabled`：索引级别的设置，是否启用 query cache，默认启用。
- `indices.requests.cache.size`：控制 Shard Request Cache 占用的内存大小，默认为堆内存的 1%。
- `indices.fielddata.cache.size`：控制 Fielddata Cache 占用的内存，默认值为unbounded。

#### 2.2 Cache 效率分析

要想合理调整上面提到的几个参数，首先要了解当前集群的 Cache 使用情况，Elasticsearch 提供了多个接口来获取当前集群中每个节点的 Cache 使用情况。

**cat api**

```
# curl -sXGET 'http://localhost:9200/_cat/nodes?v&h=name,queryCacheMemory,queryCacheEvictions,requestCacheMemory,requestCacheHitCount,request_cache.miss_count'
```

得到如下结果，可以获取每个节点的 Cache 使用情况：

```
name queryCacheMemory queryCacheEvictions requestCacheMemory requestCacheHitCount request_cache.miss_count
test01 1.6gb 52009098 15.9mb 1469672533 205589258
test02 1.6gb 52196513 12.2mb 2052084507 288623357
```

**nodes_stats**

```
curl -sXGET 'http://localhost:9200/_nodes/stats/indices?pretty'
```

从结果中可以分别找到 Query Cache、Request Cache、Fielddata 相关统计信息

```
...
"query_cache" : {
     "memory_size_in_bytes" : 1736567488,
     "total_count" : 14600775788,
     "hit_count" : 9429016073,
     "miss_count" : 5171759715,
     "cache_size" : 292327,
     "cache_count" : 52298914,
     "evictions" : 52006587
}
...
"fielddata" : {
     "memory_size_in_bytes" : 186953184,
     "evictions" : 0
}
...
"request_cache" : {
     "memory_size_in_bytes" : 16369709,
     "evictions" : 307303,
     "hit_count" : 1469518738,
     "miss_count" : 205558017
}
...
```

#### 2.3 设置 Cache 大小

在收集了集群中节点的 Cache 内存占用大小、命中次数、驱逐次数后，就可以根据收集的数据计算出命中率和驱逐比例。

过低的命中率和过高的驱逐比例说明对应 Cache 设置的过小。合理的调整对应的参数，使命中率和驱逐比例处于期望的范围。但是增大 Cache 要考虑到对 GC 的压力。

### 总结

Elasticsearch 并不会缓存每一个查询结果，他只缓存特定的查询方式，如果你增大了 Cache 大小，一定要关注 JVM 的使用率是否在合理的范围，我们建议保持在 60% 以下比较安全，同时关注 GC 指标是否存在异常。

下一节我们介绍一下**如何使用熔断器（breaker）来保护 Elasticsearch 节点的内存使用率**。

## 9.Breaker 限制内存使用量

内存问题 -- OutOfMemoryError 问题是我们在使用 Elasticsearch 过程中遇到的最大问题，Circuit breakers 是 Elasticsearch 用来防止集群出现该问题的解决方案。

Elasticsearch 含多种断路器用来避免因为不合理的操作引起来的 OutOfMemoryError（内存溢出错误）。每个断路器指定可以使用多少内存的限制。 另外，还有一个父级别的断路器，指定可以在所有断路器上使用的内存总量。

### 1 Circuit breadkers 分类

所有的 Circuit breaker 都支持动态配置，例如：

```
curl -XPUT localhost:9200/_cluster/settings -d '{"persistent" : {"indices.breaker.total.limit":"40%"}}'
curl -XPUT localhost:9200/_cluster/settings -d '{"persistent" : {"indices.breaker.fielddata.limit":"10%"}}'
curl -XPUT localhost:9200/_cluster/settings -d '{"persistent" : {"indices.breaker.request.limit":"20%"}}'
curl -XPUT localhost:9200/_cluster/settings -d '{"transient" : {"network.breaker.inflight_requests.limit":"20%"}}'
curl -XPUT localhost:9200/_cluster/settings -d '{"transient" : {"indices.breaker.accounting.limit":"20%"}}'
curl -XPUT localhost:9200/_cluster/settings -d '{"transient" : {"script.max_compilations_rate":"20%"}}'
```

#### 1.1 Parent circuit breaker

**1. 作用**

设置所有 Circuit breakers 可以使用的内存的总量。

**2. 配置项**

- `indices.breaker.total.limit`

默认值为 70% JVM 堆大小。

#### 1.2 Field data circuit breaker

**1. 作用**

估算加载 fielddata 需要的内存，如果超过配置的值就短路。

**2. 配置项**

- `indices.breaker.fielddata.limit`

默认值 是 60% JVM 堆大小。

- `indices.breaker.fielddata.overhead`

所有估算的列数据占用内存大小乘以一个常量得到最终的值。默认值是1.03。

- `indices.fielddata.cache.size`

该配置不属于 Circuit breaker，但是都与 Fielddata 有关，所以有必要在这里提一下。主要控制 Fielddata Cache 占用的内存大小。

默认值是不限制，Elasticsearch 认为加载 Fielddata 是很重的操作，频繁的重复加载会严重影响性能，所以建议分配足够的内存做 field data cache。

该配置和 Circuit breaker 配置还有一个不同点是这是一个静态配置，如果修改需要修改集群中每个节点的配置文件，并重启节点。

可以通过 cat nodes api 监控 field data cache 占用的内存空间：

```
curl -sXGET "http://localhost:9200/_cat/nodes?h=name,fielddata.memory_size&v"
```

输出如下（注：存在 `fielddata.memory_size` 为 0 是因为本集群部署了 5 个查询节点，没有存储索引数据）：

```
name    fielddata.memory_size
node1               224.2mb
node2               225.5mb
node3               168.7mb
node4                    0b
node5                    0b
node6               168.4mb
node7               223.8mb
node8               150.6mb
node9               169.5mb
node10                   0b
node11              224.7mb
node12                   0b
node13                   0b
```

`indices.fielddata.cache.size` 与 `indices.breaker.fielddata.limit` 的**区别**：前者是控制 fielddata 占用内存的大小，后者是防止加载过多大的 fielddata 导致 OOM 异常。

#### 1.3 Request circuit breaker

**1. 作用**

请求断路器允许 Elasticsearch 防止每个请求数据结构（例如，用于在请求期间计算聚合的内存）超过一定量的内存。

**2. 配置项**

- `indices.breaker.request.limit`

默认值是 60% JVM 堆大小。

- `indices.breaker.request.overhead`

所有请求乘以一个常量得到最终的值。默认值是 1。

#### 1.4 In flight circuit breaker

**1. 作用**

请求中的断路器，允许 Elasticsearch 限制在传输或 HTTP 级别上的所有当前活动的传入请求的内存使用超过节点上的一定量的内存。 内存使用是基于请求本身的内容长度。

**2. 配置项**

- `network.breaker.inflight_requests.limit`

默认值是 100% JVM 堆大小，也就是说该 breaker 最终受 `indices.breaker.total.limit` 配置限制。

- `network.breaker.inflight_requests.overhead`

所有 (inflight_requests) 请求中估算的常数乘以确定最终估计，默认值是1。

#### 1.5 Accounting requests circuit breaker

**1. 作用**

估算一个请求结束后不能释放的对象占用的内存。包括底层 Lucene 索引文件需要常驻内存的对象。

**2. 配置项**

- `indices.breaker.accounting.limit`

默认值是 100% JVM 堆大小，也就是说该 breaker 最终受`indices.breaker.total.limit`配置限制。

- `indices.breaker.accounting.overhead`

默认值是1。

#### 1.6 Script compilation circuit breaker

**1. 作用**

与上面的基于内存的断路器略有不同，脚本编译断路器在一段时间内限制脚本编译的数量。

**2. 配置项**

- `script.max_compilations_rate`

默认值是 75/5m。也就是每 5 分钟可以进行 75 次脚本编译。

### 2 Circuit breaker 状态

合理配置 Circuit breaker 大小需要了解当前 breaker 的状态，可以通过 Elasticsearch 的 stats api 获取当前 breaker 的状态，包括配置的大小、当前占用大小、overhead 配置以及触发的次数。

```
curl -sXGET     "http://localhost:9200/_nodes/stats/breaker?pretty"
```

执行上面的命令后，返回各个节点的 Circuit breakers 状态：

```
"breakers" : {
"request" : {
    "limit_size_in_bytes" : 6442450944,
    "limit_size" : "6gb",
    "estimated_size_in_bytes" : 690875608,
    "estimated_size" : "658.8mb",
    "overhead" : 1.0,
    "tripped" : 0
},
"fielddata" : {
    "limit_size_in_bytes" : 11274289152,
    "limit_size" : "10.5gb",
    "estimated_size_in_bytes" : 236500264,
    "estimated_size" : "225.5mb",
    "overhead" : 1.03,
    "tripped" : 0
},
"in_flight_requests" : {
    "limit_size_in_bytes" : 32212254720,
    "limit_size" : "30gb",
    "estimated_size_in_bytes" : 18001,
    "estimated_size" : "17.5kb",
    "overhead" : 1.0,
    "tripped" : 0
},
"parent" : {
    "limit_size_in_bytes" : 17716740096,
    "limit_size" : "16.5gb",
    "estimated_size_in_bytes" : 927393873,
    "estimated_size" : "884.4mb",
    "overhead" : 1.0,
    "tripped" : 0
    }
}
```

其中重点需要关注的是 `limit_size` 与 `estimated_size` 大小是否相近，越接近越有可能触发熔断。tripped 数量是否大于 0，如果大于 0 说明已经触发过熔断。

### 3 Circuit breaker 配置原则

Circuit breaker 的目的是防止**不当的操作**导致进程出现 OutOfMemoryError 问题。不能由于触发了某个断路器就盲目调大对应参数的设置，也不能由于节点经常发生 OutOfMemoryError 错误就盲目调小各个断路器的设置。需要结合业务合理评估参数的设置。

#### 3.1 不同版 circuit breakers 区别

Elasticsearch 从 2.0 版本开始，引入 Circuit breaker 功能，而且随着版本的变化，Circuit breaker 的类型和默认值也有一定的变化，具体如下表所示：

| 版本    | Parent | Fielddata | Request | Inflight | Script    | Accounting |
| ------- | ------ | --------- | ------- | -------- | --------- | ---------- |
| 2.0-2.3 | 70%    | 60%       | 40%     | 无       | 无        | 无         |
| 2.4     | 70%    | 60%       | 40%     | 100%     | 无        | 无         |
| 5.x-6.1 | 70%    | 60%       | 60%     | 100%     | 1分钟15次 | 无         |
| 6.2-6.5 | 70%    | 60%       | 60%     | 100%     | 1分钟15次 | 100%       |

从上表中可见，Elasticsearch 也在不断调整和完善 Circuit breaker 相关的默认值，并不断增加不同类型的 Circuit breaker 来减少 Elasticsearch 节点出现 OOM 的概率。

> **注：** 顺便提一下，Elasticsearch 7.0 增加了 `indices.breaker.total.use_real_memory` 配置项，可以更加精准的分析当前的内存情况，及时防止 OOM 出现。虽然该配置会增加一点性能损耗，但是可以提高 JVM 的内存使用率，增强了节点的保护机制。

#### 3.2 默认值的问题

Elasticsearch 对于 Circuit breaker 的默认值设置的都比较激进、乐观的，尤其是对于 6.2（不包括 6.2）之前的版本，这些版本中没有 **accounting circuit breaker**，节点加载打开的索引后，Lucene 的一些数据结构需要常驻内存，**Parent circuit breakeredit** 配置成堆的 70%，很容易发生 OOM。

#### 3.3 配置思路

不同的业务场景，不同的数据量，不同的硬件配置，Circuit breaker 的设置应该是有差异的。 配置的过大，节点容易发生 OOM 异常，配置的过小，虽然节点稳定，但是会经常出现触发断路的问题，导致一部分合理应用无法完成。这里我们介绍下在配置时需要考虑的问题。

- 1. 了解 Elasticsearch 内存分布

**Circuit breaker** 最主要的作用就是防止节点出现 OOM 异常，所以，掌握 Elasticsearch 中都有哪些组件占用内存是配置好 Circuit breaker 的第一步。

> 具体参见本课程中《常见问题之-内存问题》一章。

- 2. Parent circuit breaker 配置

前面提到 Elasticsearch 6.2 之前的版本是不包含 accounting requests circuit breaker 的，所以需要根据自己的数据特点，评估 Lucene segments 占用的内存量占 JVM heap 、index buffer、Query Cache、Request Cache 占用的内存的百分比，并用 70% 减去评估出的值作为 parent circuit breaker 的值。 对于 6.2 以后的版本，不需要减掉 Lucene segments 占用的百分比。

- 3. Fielddata circuit breaker 配置

在 Elasticsearch 引入 `doc_values` 后，我们十分不建议继续使用 fielddata，一是 feilddata 占用内存过大，二是在 text 字段上排序和聚合没有意义。Fielddata 占用的内存应该仅限于 Elasticsearch 在构建 global ordinals 数据结构时占用的内存。

有一点注意的是，Elasticsearch 只有在单个 shard 包含多个 segments 时，才需要构建 global ordinals，所以对于不再更新的索引，尽量将其 merge 到一个 segments，这样在配置 Fielddata circuit breaker 时只需要评估还有可能变化的索引占用的内存即可。

Fielddata circuit breaker 应该略高于 `indices.fielddata.cache.size`， 防止老数据不能从 Cache 中清除，新的数据不能加载到 Cache。

- 4. Accounting requests circuit breaker 配置

根据自己的数据的特点，合理评估出 Lucene 占用的内存百分比，并在此基础上上浮 5% 左右。

- 5. Request circuit breaker 配置

大数据量高纬度的聚合查询十分消耗内存，需要评估业务的数据量和聚合的维度合理设置。建议初始值 20% 左右，然后出现 breaker 触发时，评估业务的合理性再适当调整配置，或者通过添加物理资源解决。

- 6. In flight requests circuit breaker 配置

该配置涉及传输层的数据传输，包括节点间以及节点与客户端之间的通信，建议保留默认配置 100%。

### 总结

本章简要介绍了 Elasticsearch 不同版本中的 breaker 及其配置，并结合我们的经验，给出了一点配置思路。是否能合理配置 Circuit breaker 是保证 Elasticsearch 能否稳定运行的关键， 由于 Elasticsearch 的节点恢复时间成本较高，提前触发 breaker 要好于节点 OOM。

7.x 之前的版本中，大部分的 breaker 在计算内存使用量时都是估算出来的，这就造成很多时候 breaker 不生效，或者过早介入，7.x 之后 breaker 可以根据实际使用量来计算占用空间，比较精确的控制熔断。

下一章节我们介绍**如何对集群进行压测**，这是业务上线的一个必经过程。

## 10.集群压测

在业务上线之前，压力测试是一个十分重要的环节，他不仅能让你了解集群能够支撑多大的请求量，以便在业务增长过程中提前扩容，同时在压力场景下也能提前发现以下不常见的问题。

由于业务数据的千差万别，除了参考 Elasticsearch 集群的基准测试指标，每个业务都应该使用自己的数据进行全链路的压力测试。

你可以使用很多工具进行压力测试，例如编写 shell 脚本使用 curl、ab 等命令行工具，也可以自己开发压测工具或者使用 Jmeter 进行压测，在此我们建议使用官方的压测工具：esrally 。官方也是使用这个工具进行压力测试的， 使用 esrally 可以做到：

- 得到读写能力，读写 QPS 能达到多少？
- 对压测结果进行对比，例如不同版本，不同数据，不同索引设置下的性能差异，例如关闭 `_all` 之后写入性能可以提高多少？
- 同时监控 JVM 信息，可以观察 GC 等指标是否存在异常。

### 1 esrally 的安装和配置

esrally 是 Elastic 的开源项目，由 python3 编写，安装 esrally 的系统环境需求如下：

- 为了避免多个客户端从磁盘数据成为性能瓶颈，最好使用 SSD；
- 操作系统支持 Linux 以及 MacOS，不支持 Windows；
- Python 3.4 及以上；
- Python3 头文件；
- pip3；
- git 1.9 及以上；
- JDK 8，并且正确设置了 JAVA_HOME 环境变量；

当上述环境准备就绪后，可以通过 pip3 简单安装：

```
pip3 install esrally
```

安装完毕后，esrally 所需的配置文件等已经被安装到默认位置，你可以运行下面的命令重新生成这些默认配置：

```
esrally configure
```

如果想要修改默认的配置文件路径，可以运行下面的命令进行高级设置：

```
esrally configure --advanced-config
```

### 2 基本概念

压测工具引用了很多汽车拉力赛中的概念，要学会使用 esrally 必须理解这些术语。

**track** 赛道，在 esrally 中指测试数据以及对这些测试数据执行哪些操作，esrally 自带了一些测试数据，执行：

```
esrally list tracks
```

命令可以查看目前都有哪些 track

我们以 `geonames/track.json` 为例看看一个 trace 都包含了哪些东西：

![avatar](https://images.gitbook.cn/FtBTfIxIHySOmu43rLuTIMEjY0SI)

在这一堆信息中只需要重点关注几个字段： indices：描述了测试时数据写入到哪个索引，以及测试数据的 json 文件名称 challenges：描述了测试过程中都要执行哪些操作

**challenge** 在赛道上执行哪些挑战。此处只对数据执行哪些压测操作。这些操作的部分截图如下：

![avatar](https://images.gitbook.cn/FsWSS6AxypPrH1ED-aolYcqlzSCe)

可以看到先执行删除索引，然后创建索引，检查集群健康，然后执行索引写入操作。

**car** 赛车，这里待测试的指 Elasticsearch实例，可以为每个实例进行不同的配置。通过下面的命令查看都有哪些自带的 car

```
esrally list cars
```

**race** 进行一次比赛，此处指进行一次压测，进行一次比赛要指定赛道，赛车，进行什么挑战。此处需要指定 track，challenge，car。通过下面的命令可以查看已经执行过的压测：

```
esrally list races
```

**Tournament** 锦标赛，由多次 race 组成一个

### 3 执行压测

esrally 可以自行下载指定版本的 Elasticsearch进 行测试，也可以对已有集群进行测试。如果想要对比不同版本，不同 Elasticsearch 配置，开启`_all` 与否等性能差异，那么建议使用 esrally 管理的 Elasticsearch 实例。

如果只想验证一下读写吞吐量，可以使用外部集群，运行 esrally 的服务器与 Elasticsearch 集群独立部署也可以让测试结果更准确。现在我们先使用 esrally 自己管理的 Elasticsearch 实例快速执行一个简单的压测：

```
esrally --distribution-version=6.5.1   --track=geonames  --challenge=append-no-conflicts --car="4gheap"  --test-mode --user-tag="demo:test"
```

**--distribution-version**

esrally 会下载 6.5.1 版本的 Elasticsearch

**--track**

使用 geonames 这个数据集

**--challenge**

执行 append-no-conflicts 操作序列

**--car** 使用 Elasticsearch 实例配置为 4gheap

**--test-mode**

由于这个数据集比较大，我们为了快速完成压测示例，通过此参数只使用 1000 条数据进行压测。

**--user-tag**

参数为本次压测指定一个标签，便于在多次压测之间进行区分。

压测开始运行后，正常情况下其输出信息如下：

![avatar](https://images.gitbook.cn/FiELHU3CZpfCOGJVyunCQ0mcrkO8)

压测完成后会产生详细的压测结果信息，部分结果如下：

![avatar](https://images.gitbook.cn/Fq4vOkKbMkxI4kJXFGavxEk_3M67)

这些结果包括索引写入速度，写入延迟，以及 JVM 的 GC 情况等我们关心的指标。你可以在运行压测时指定 `--report-file=xx` 来将压测结果单独保存到一个文件中。

如果使用相同的数据集对外部已有集群进行压测，则对应的命令如下：

```
esrally  --pipeline=benchmark-only --target-hosts=hostname:9200 --client-options="basic_auth_user:'elastic',basic_auth_password:'xxxxxx'" --track=geonames  --challenge=append-no-conflicts   --test-mode --user-tag="demo:mycluster"
```

**--pipeline** 简单的理解就是带测试的 Elasticsearch 集群来着哪里，包括直接下载发行版，从源码编译，或者使用外部集群，要对外部已有集群进行压测，此处需要设置为 benchmark-only

**--client-options** 指定客户端附加选项，这些选项会设置到发送到 Elasticsearch 集群的请求中，如果目标集群开启了安全认证，我们需要在此处指定用户名和密码。

启动压测后，esrally 会弹出如下警告，测试外部集群时，esrally 无法收集目标主机的 CPU 利用率等信息，对这个警告不必紧张。

![avatar](https://images.gitbook.cn/FjiEpjhK8e8wSvLzHqyG-KEtdeXA)

### 4 对比压测结果

现在，我们进行了两次压测，可以将两次压测结果进行对比，先执行下面的命令列出我们执行过的 race：

```
esrally list races
```

输出信息如下：

```
Recent races:

Race Timestamp    Track     Track Parameters    Challenge            Car       User Tags
----------------  --------  ------------------  -------------------  --------  -----------------------------
20190212T102025Z  geonames                      append-no-conflicts  external  demo=mycluster
20190212T095938Z  geonames                      append-no-conflicts  4gheap    demo=test
```

User Tags 列可以让我们容易区分两次 race，现在我们以 demo=test 为基准测试，对比 demo=mycluster 的测试结果。在进行对比时，需要指定 race 的时间戳，也就是上面结果中的第一列：

```
esrally compare --baseline=20190212T095938Z --contender=20190212T102025Z
```

输出信息的前几列如下：

![avatar](https://images.gitbook.cn/FnxKBcFVp2Z9SWfjaz3PkzA4VGSD)

对比结果给出了每个指标相对于基准测试的 diff 量，可以非常方便地看到压测结果之间的差异。

### 5 自定义 track

我们通常需要使用自己的数据进行测试，这就需要自己定义 track。esrally 自带的 track 默认存储在 `~/.rally/benchmarks/tracks/default` 目录下，我们自己定义的 track 可以放在这个目录下也可以放到其他目录，使用的时候通过 `--track-path=` 参数指定存储目录。下面我们来定义一个名为 demo 的最简单 track。

**1. 准备样本数据**

先创建用于存储 track 相关文件的目录，目录名就是未来的 trace 名称：

```
mkdir ~/test/demo
```

track 所需的样本数据为 json 格式，结构与 bulk 所需的类似，在最简单的例子中，我们在 documents.json 中写入1000行相同的内容：

```
cat documents.json |head -n 3
{"name":"zhangchao"}
{"name":"zhangchao"}
{"name":"zhangchao"}
```

**2. 定义索引映射**

样本数据准备好之后，我们需要为其配置索引映射和设置信息，我们建立 `index.json` 文件，写入如下内容：

```
{
    "settings": {
        "index.refresh_interval": "120s",
        "index.translog.durability" : "async",
        "index.translog.flush_threshold_size" : "5000mb",
        "index.translog.sync_interval" : "120s",
        "index.number_of_replicas": 0,
        "index.number_of_shards": 8
    },
    "mappings": {
        "doc": {
            "dynamic": false,
            "properties": {
                "name":{"type":"keyword"}
            }
        }
    }
}
```

在 index.json 中添加你自己的索引设置，并在 mappings 中描述字段类型等信息。

**3. 编写 track.json 文件**

该配置文件是 track 的核心配置文件，本例中，编写内容如下：

```
{
  "version": 2,
  "description": "Demo benchmark for Rally",
  "indices": [
    {
      "name": "demo",
      "body": "index.json",
      "types": [ "doc" ]
    }
  ],
  "corpora": [
    {
      "name": "rally-demo",
      "documents": [
        {
          "source-file": "documents.json",
          "document-count": 1000,
          "uncompressed-bytes": 21000
        }
      ]
    }
  ],
"operations": [
    {{ rally.collect(parts="operations/*.json") }}
  ],
  "challenges": [
    {{ rally.collect(parts="challenges/*.json") }}
  ]
}
```

- **description**

  此处的描述是 `esrally list tracks` 命令输出的 tracks 描述信息。

- **indices**

  写入 Elasticsearch 集群时目标索引信息。 name：索引名称； body：索引设置信息文件名； types：索引的 type；

- **corpora**

  指定样本数据文件及文件信息。 source-file：样本数据文件名 document-count：样本数据文件行数，必须与实际文件严格一致，可以通过 `wc -l documents.json`命令来计算。 uncompressed-bytes：样本数据文件字节数，必须与实际文件严格一致，可以通过 `ls -l documents.json` 命令来计算。

- **operations**

  用来自定义 operation 名称，此处我们放到 operations 目录下独立的 json 文件中。

- **challenges**

  描述自定义的 challenges 要执行哪些操作，此处我们放到 challenges 目录下的独立文件中。

**4. 自定义 operations**

这里自定义某个操作应该如何执行，在我们的例子中，我们定义索引文档操作以及两种查询请求要执行的操作：

```
 {
      "name": "index-append",
      "operation-type": "bulk",
      "bulk-size": {{bulk_size | default(5000)}},
      "ingest-percentage": {{ingest_percentage | default(100)}}
},
{
      "name": "default",
      "operation-type": "search",
      "body": {
        "query": {
          "match_all": {}
        }
      }
    },
    {
      "name": "term",
      "operation-type": "search",
      "body": {
        "query": {
          "term": {
            "method": "GET"
          }
        }
      }
    },
```

这个文件不必完全重写，我们可以从 esrally 自带的 track 目录中拷贝 `operations/default.json` 到自己的目录下进行修改。

**5. 自定义 challenges**

我们先定义一个写数据的 challenges，内容如下：

```
cat challenges/index.json

{
  "name": "index",
  "default": false,
  "schedule": [
    {
      "operation": {
        "operation-type": "delete-index"
      }
    },
    {
      "operation": {
        "operation-type": "create-index"
      }
    },
    {
      "operation": {
        "operation-type": "cluster-health",
        "request-params": {
          "wait_for_status": "green"
        }
      }
    },
    {
          "operation": "index-append",
                "warmup-time-period": 120,
          "clients": {{bulk_indexing_clients | default(16)}}
    },
    {
      "operation": {
        "operation-type": "refresh"
      }
    },
    {
      "operation": {
        "operation-type": "force-merge"
      }
    }
  ]
}
```

**name** 指定该 challenge 的名称，在运行 race 的时候， `--challenge` 参数 指定的就是这个名称

**schedule** 指定要执行的操作序列。在我们的例子中，依次执行删除索引、创建索引、检查集群健康，写入数据，执行刷新、执行 force-merge

接下来，我们再创建一个执行查询的 challenge，内容如下：

```
cat operations/default.json

{
  "name": "query",
  "default": true,
  "schedule": [
    {
      "operation": {
        "operation-type": "cluster-health",
        "request-params": {
          "wait_for_status": "green"
        }
      }
    },
    {
      "operation": "term",
      "clients": 8,
      "warmup-iterations": 1000,
      "iterations": 10
    },
    {
      "operation": "match",
      "clients": 8,
      "warmup-iterations": 1000,
      "iterations": 10
    }
  ]
}
```

该 challenge 同样先检查集群状态，然后依次执行我们预定义的 term 和 match 操作。

你也可以不将自定义 operations 放到单独目录中，而是在自定义 challenge 的时候直接合并在一起，但是分开来可以让自定义 challenge 的文件开起来更清晰一些。

同样，自定义的 operations 内容也可以直接写在 track.json 文件中，但是分开更清晰。

到此，我们自定义的 track 已经准备完毕，demo 目录下的文件结构如下：

```
tree demo
demo
├── challenges
│   ├── index.json
│   └── query.json
├── documents.json
├── index.json
├── operations
│   └── default.json
└── track.json
```

执行下面的命令可以看到我们创建完毕的 track：

```
esrally list tracks --track-path=~/test/demo
```

输出信息如下：

![avatar](https://images.gitbook.cn/Fv6twVgBp6ubVFWAHAM8333G4j8H)

现在，我们可以通过下面的命令使用刚刚创建的 track 进行测试：

```
esrally  --track-path=/home/es/test/demo --pipeline=benchmark-only --target-hosts=hostname:9200 --client-options="basic_auth_user:'elastic',basic_auth_password:'xxxxxxx'"  --challenge=index    --user-tag="demo:mycluster_customtrack_index"
```

由于已经使用 `--track-path` 指定 track，因此不再使用 `--track` 来指定 track 名称。

### 总结

使用 esrally 可以很方便的完成我们的压测需求，但是实际使用过程中可能会因为 python3 的环境遇到一些问题，因此也可以在 docker 中运行 esrally，将样本数据放在容器之外，然后将目录挂载到容器中，不会对性能测试产生多少影响。

现在已经有一些安装好 esrally 的 docker 镜像，可以通过 `docker search esrally` 命令来搜索可用镜像。

下一节我们介绍集群监控，Elasticsearch 的监控指标很多，我们将介绍一些需要重点关注的监控项。

### 参考

[官方的压测结果](https://esrally.readthedocs.io/en/stable/#)

[esrally 手册](https://elasticsearch-benchmarks.elastic.co/#)

[Define Custom Workloads: Tracks](https://esrally.readthedocs.io/en/latest/adding_tracks.html)

[rally-tracks](https://github.com/elastic/rally-tracks)

[Elasticsearch 压测方案之 esrally 简介](https://segmentfault.com/a/1190000011174694)

## 11.集群监控

为了能够提前发现问题，以及在出现故障后便于定位问题，我们需要对集群进行监控，对于一个完整的Elasticsearch 集群监控系统来说，需要的的指标非常多，这里我们列出一些比较重要的。

### 1 集群级监控指标

#### 1.1 集群健康

集群健康是最基础的指标，他是快速衡量集群是否正常的依据，当集群 Yellow 的时候，代表有部分副分片尚未分配，导致未分配的原因很多。

例如节点离线等，从分布式系统的角度来说意味着数据缺少副本，系统会尝试将他自动补齐，因此可以不把 Yellow 作为一种报警状态。集群处于 Yellow 状态时也可以正常执行入库操作。

另外当创建新索引时，集群也可能会出现短暂的从 Red 到 Yellow 再到 Green 的状态，因为创建索引时，可能需要分配多个分片，在全部的分片分配完毕之前，该索引在集群状态中属于分片不完整的状态，因此短暂的 Red 也属于正常现象。

集群健康可以通过 `_cluster/health` 接口来查看。

#### 1.2 读写 QPS

Elasticsearch 没有提供实时计算出好读写 QPS 值，实时计算这些值会对集群造成比较大的压力。

他提供了一个接口返回每个节点当前已处理的请求数量，你需要基于此进行计算：发送两次请求，中间间隔一段时间，用两次返回的请求数量做差值，得到间隔时间内的增量，再把每个节点的增量累加起来，除以两次请求的间隔时间，得到整个集群的 QPS。两次请求的间隔时间不要太短，建议在 10s 及以上。

节点的请求统计信息通过 `_nodes/stats` 接口获取，对于计算读写 QPS 来说，我们所需信息如下：

```
"indexing" : {
  "index_total" : 141310,
}
"search" : {
  "query_total" : 5772,
},
```

**index_total：**节点收到的索引请求总量;

**query_total：**节点收到的查询请求总量;

通过这种方式计算出的 QPS 并非业务直接发起的读写请求 QPS，而是分片级别的。例如，只有一个索引，索引只有1个分片，那么我们计算出的 QPS 等于业务发起的请求 QPS，如果索引有5个分片，那么计算出的 QPS 等于业务发起的 QPS*5。因此，无论是查询还是索引请求：

计算出的 `QPS = 业务发起的 QPS * 分片数量`

在 Kibana 的 Monitor 中看到的 Search Rate (/s) 与 Indexing Rate (/s) 的涵义与我们上面的描述相同。

#### 1.3 读写延迟

与读写 QPS 类似，读取延迟也可以通过 `_nodes/stats` 接口返回的信息进行计算，对于读写延迟来说，所需信息如下：

```
"indexing" : {
  "index_time_in_millis" : 54404,
}
"search" : {
  "query_time_in_millis" : 5347,
  "fetch_time_in_millis" : 1465,
},
```

查询由两个阶段完成，因此 query 耗时与 fetch 单独给出，对于整个搜索请求耗时来说需要把它加起来。由于这种方式计算出来的的采样周期内的平均值，因此只能给监控提供大致的参考，如果需要诊断慢请求需要参考慢查询或慢索引日志。

#### 1.4 分片信息

我们还需要关注有多少分片处于异常在状态，这些信息都在 `_cluster/health` 的返回结果中，包括：

- **initializing_shards**

  正在执行初始化的分片数量，当一个分片开始从 UNASSIGNED 状态变为 STARTED 时，从分片分配操作一开始，该分片被标记为 INITIALIZING 状态。例如创建新索引、恢复现有分片时，都会产生这个状态。

- **unassigned_shards**

  待分配的分片数量，包括主分片和副分片。

- **delayed_unassigned_shards**

  由于一些原因延迟分配的分片数量。例如配置了 `index.unassigned.node_left.delayed_timeout`，节点离线时会产生延迟分配的分片。

### 2 节点级别指标

#### 2.1 JVM 指标

JVM 指标也在 `_nodes/stats` API 的返回结果中，每个节点的信息单独给出。需要重点关注的指标如下：

**1. 堆内存使用率**

字段 `heap_used_percent` 代表堆内存使用率百分比，如果堆内存长期居高则意味着集群可能需要扩容。

JVM 内存使用率过高，且无法 GC 掉时，集群处于比较危险的状态，当一个比较大的聚合请求过来，或者短期内读写压力增大时可能会导致节点 OOM。

如果 master 节点的堆内存使用率过高更需要警惕，当重启集群时，master 节点执行 gateway 及 recovery 都可能需要比较多的内存，这和分片数量有关，因此可能在重启集群的时候内存不足，有时需要关闭一些索引才能让集群启动成功。

**2. GC 次数和时长**

年轻代和老年代的回收次数与持续时间最好都被监控，如果年轻代 GC 频繁，可能意味着为年轻代分配的空间过小，如果老年代 GC 频繁，可能意味着需要进行扩容。

```
"gc" : {
  "collectors" : {
    "young" : {
      "collection_count" : 44,
      "collection_time_in_millis" : 2678
    },
    "old" : {
      "collection_count" : 2,
      "collection_time_in_millis" : 493
    }
  }
},
```

正常情况下，通过 REST API 获取这些指标不是问题，但是当节点长时间 GC 时，接口无法返回结果，导致无法发现问题，因此建议使用 jstat 等外部工具对 JVM 进行监控。

#### 2.2 线程池

关注线程池信息可以让我们了解到节点负载状态，有多少个线程正在干活，Elasticsearch 有很多线程池，一般我们可以重点关注执行搜索和索引的线程信息，可以通过 `_nodes/stats` API 或 `_cat/thread_pool` API 来获取线程池信息，建议使用 `_nodes/stats` API，你可以在一个请求的结果中得到很多监控指标，我们最好少发一些 stats 之类的请求到 Elasticsearch 集群。

```
"bulk" : {
  "active" : 0,
  "rejected" : 0,
},
"search" : {
  "active" : 0,
  "rejected" : 0,
},
```

**active：** 正在运行任务的线程个数;

**rejected：** 由于线程池队列已满，拒绝的请求数量;

> 客户端对于被拒绝的请求应该执行延迟重试，更多信息可以参考《Elasticsearch 源码解析与优化实战》

### 3 操作系统及硬件

只监控 Elasticsearch 集群本身的指标是不够的，我们必须结合操作系统和硬件信息一起监控。这里只给出建议重点关注的指标，如何获取这些指标的方法很多，本文不再过多叙述。

#### 3.1 磁盘利用率

这里的磁盘利用率不是指使用了多少空间，而是指 iostat 返回的 `%util`。服务器一般会挂载多个磁盘，你不比经常关心每个磁盘的 `%util` 有多少，但是需要注意下磁盘 util 长时间处于 100%， 尤其是只有个别磁盘的 util 长时间处于100%，这可能是分片不均或热点数据过于集中导致。

#### 3.2 坏盘

目前 Elasticsearch 对磁盘的管理有些不足，因此我们需要外部手段检查、监控坏盘的产生并及时更换。坏盘对集群的稳定性有较大影响

#### 3.3 内存利用率

一般不比对操作系统内存进行监控，Elasticsearch 会占用大量的 page cache，这些都存储在操作系统的物理内存中。因此发现操作系统的 free 内存很少不必紧张，特别注意不要手工回收 cache，这会对集群性能产生较严重影响。

### 总结

本章从 Elasticsearch 集群角度和操作系统角度介绍了需要重点关注的监控项，在设计监控系统的时候，需要注意发起获取指标的请求频率不要太高，有些请求需要 master 节点到各个数据节点去收集，频繁的 `_cat/indices` 之类请求会对集群造成比较大的压力。

下一节课程我们介绍一下**集群应该何时扩容，以及扩容注意事项**。

## 12.集群扩容

随着时间的推移、业务的发展，你存入 Elasticsearch 的数据会越来越多；随着用户量的增加，系统响应时间增加。

总有一天，初始安装的集群规模无法满足日益增长的需求，这时需要考虑对现有的 Elasticsearch 集群进行扩容。

### 1 扩容方式

为了提高系统的处理能力，包括增加系统的 cpu、内存、存储等资源，通常有两种扩容方式：垂直扩容和水平扩容。

#### 1.1 垂直扩容

增加单机处理能力，如购买更好的 cpu，增加 cpu 核心数；将机械硬盘换成 SSD，提高 IO 能力；购买更大容量的内存条，提高内存容量满足计算需求；升级万兆网卡，提高网络带宽等。

#### 1.2 水平扩容

通过增加服务器的数量，将服务器形成分布式的集群，以提高整个系统的计算、存储、IO 能力，满足业务的需求。水平扩容通常需要软件在架构层面上的支持。

### 2 定位硬件瓶颈

我们现在集群的处理能力能够满足业务需求吗？何时需要扩容？你肯定不希望你的线上业务由于硬件资源不够挂掉。为了解答这些问题，我们首先要定位出当前的硬件资源是否存在瓶颈。下面列出我们常用的定位 Elasticsearch 存在硬件资源瓶颈的一些办法。

#### 2.1 **cpu**

Elasticsearch 索引和查询过程都是 cpu 密集型的操作，如果 cpu 存在瓶颈，系统性能会受到很大影响。那采用什么指标来定位 cpu 瓶颈呢？我们一般通过如下几个方法来定位：

- **通过操作系统的监控命令**

  `sar` 命令

```
bash-4.2$ sar -u
```

执行该命令后，输出如下：

```
    07:44:02 PM     CPU     %user     %nice   %system   %iowait    %steal     %idle
    07:46:01 PM     all     11.39      0.00      1.64      0.21      0.00     86.76
    07:48:01 PM     all     10.40      0.00      1.50      0.07      0.00     88.03
    07:50:01 PM     all      9.37      0.00      1.34      0.07      0.00     89.23
    07:52:01 PM     all     12.01      0.00      1.55      0.05      0.00     86.40
    07:54:01 PM     all      9.58      0.00      1.41      0.02      0.00     88.98
    07:56:01 PM     all     10.16      0.00      1.49      0.15      0.00     88.20
    07:58:01 PM     all     10.04      0.00      1.44      0.02      0.00     88.50
    08:00:01 PM     all     10.39      0.00      1.51      0.03      0.00     88.07
    08:02:01 PM     all     10.90      0.00      1.55      0.05      0.00     87.50
    08:04:01 PM     all      9.29      0.00      1.43      0.02      0.00     89.26
    08:06:01 PM     all      9.91      0.00      1.40      0.05      0.00     88.64
    08:08:01 PM     all     10.56      0.00      1.53      0.08      0.00     87.84
```

如果系统 cpu 使用率持续超过 60%，而且后期压力会随着业务的发展持续增加，就需要考虑扩容来减轻 cpu 的压力。

- **通过查看 Elasticsearch 的 Threadpool**

```
    curl -sXGET 'http://localhost:9200/_cat/thread_pool?v' | grep -E "node_name|search|bulk"
```

如果 active 一列在一段时间内持续达到了线程数的最大值，或者 rejected 不为 0，则意味着可能 cpu 资源不足，导致了请求拒绝。也可能瞬时写入并发过大，mapping 设置不合理等。

#### 2.2 **内存**

Elasticsearch 高效稳定的运行，十分依赖内存，包括 Java 进程占用的内存和操作系统 Cache Lucene 索引文件占用的内存。

- Java 堆内内存不足

  **对于 Java 进程来说，我们一般从如下几个方面判断是否运行正常：**

1. minor gc 耗时超过 50ms

2. minor gc 执行很频繁，10s 以内会执行一次

3. minor gc 后 eden 还占用很大比例空间

4. minor gc 后，survior 容纳不下 eden 和另一个 survior 的存活对象，发生了过早提升

5. fullgc 平均执行时间超过1s

6. fullgc 10分钟以内会执行一次

   如果调优 GC 参数后，gc 仍然存在问题，则需要适当增加 Java 进程的内存，但是单个节点的内存要小于 32G，继续观察，直到运行平稳。

- 操作系统 Cache 抖动

  Elasticsearch 底层基于 Lucene，Lucene 的高效运行需要依赖操作系统 Cache，操作系统频繁发生换页，Cache 抖动严重，对运行速度会产生很大的影响，产生一系列的问题，导致内存使用效率降低，引发磁盘 IO 升高，cpu 的 IO Wait 增加，从而使系统的整体吞吐量和响应时间收到极大的影响。换页行为可以通过操作系统的 sar 命令来定位。

  ```
  sar -B 
  ```

  执行该命令后，输入如下：

  ```
  07:44:01 PM  pgpgin/s pgpgout/s   fault/s  majflt/s  pgfree/s pgscank/s pgscand/s pgsteal/s    %vmeff
  07:46:01 PM  43267.29    389.22 293717.84      0.19  23414.69      0.00      0.00      0.00      0.00
  07:48:01 PM  49302.07    982.05 305202.18      0.08  32241.47   3183.44     41.29   3223.85     99.97
  07:50:01 PM  49409.41    524.31 297695.93      0.05  40182.40   8982.75    155.12   9126.79     99.88
  07:52:01 PM  54349.32    432.02 309908.67      0.02  39272.66   9135.48    567.22   8363.67     86.20
  07:54:01 PM  61406.73    348.05 326887.23      0.15  45224.42  20481.31   3296.59  15797.72     66.44
  07:56:01 PM  58327.98    130.05 294911.57      0.12  41707.75  15614.09   1915.41  14558.13     83.05
  07:58:01 PM  53790.35    442.78 293988.20      0.09  38539.34  13727.40   1719.69  13166.00     85.23
  08:00:01 PM  59534.64    279.43 304241.40      0.03  41120.01  19764.74   1896.83  14958.18     69.05
  08:02:01 PM  57026.47    204.59 292543.72      0.06  40701.89  18893.40   2479.08  14743.70     68.98
  08:04:01 PM  39415.95    447.89 224081.73      0.07  36275.20  10158.59   1542.77  10016.37     85.60
  08:06:01 PM  25112.09    668.31 204173.12      0.38  20699.20   3939.11   1371.64   5310.75    100.00
  08:08:01 PM  24780.95    656.56 200126.48      0.02  54840.20   1024.04    213.75   1237.79    100.00
  ```

  如果 `%vmeff` 一列持续低于 30，同时伴有比较高的 `pgpgin/s`、`pgpgout/s`、`pgscand/s`，说明系统内存很紧张了，如果通过优化配置无法有效降低该列的值，需要扩容来缓解系统内存不足的情况。

#### 2.3 磁盘

磁盘瓶颈一般分为两种:磁盘空间不足和磁盘读写压力大。

**磁盘空间** 磁盘间不足，这很容易理解，现有硬件的磁盘容量已经无法满足业务的需求。

**读写压力大** 磁盘 IO 读写压力比较大，磁盘使用率长期处于较高状态，导致系统的 IO Wait 增加，此时需要增加节点，将 shard 分布到更多的硬件磁盘上，以降低磁盘的 IO 压力。

#### 2.4 网络

Elasticsearch 在运行过程中，很多操作都会占用很大的网络带宽，比如大批量的索引操作、scroll 查询拉取大量的结果、分片恢复拷贝索引文件、分片平衡操作。

如果网络存在瓶颈，不但会影响这些操作的执行效率，而且影响节点间的内部通信的稳定性，会使集群出现不稳定的情况，频繁发生节点离线的情况。网络瓶颈定位使用 `iftop`、`iptraf`、`sar` 命令。下面以 `sar` 命令为例：

```
bash-4.2$ sar -n DEV
```

输出结果如下：

```
07:12:01 AM     IFACE   rxpck/s   txpck/s    rxkB/s    txkB/s   rxcmp/s   txcmp/s  rxmcst/s
07:14:01 AM      eth0   5130.99   8146.84   2933.22   7403.37      0.00      0.00      0.54
07:14:01 AM      eth1      0.00      0.00      0.00      0.00      0.00      0.00      0.00
07:14:01 AM        lo    296.09    296.09    121.74    121.74      0.00      0.00      0.00
07:16:01 AM      eth0   4240.99   7463.91   2490.03   7436.86      0.00      0.00      0.53
07:16:01 AM      eth1      0.00      0.00      0.00      0.00      0.00      0.00      0.00
07:16:01 AM        lo    300.13    300.13    129.30    129.30      0.00      0.00      0.00
07:18:01 AM      eth0   4529.47   7955.89   2639.50   7795.74      0.00      0.00      0.53
07:18:01 AM      eth1      0.00      0.00      0.00      0.00      0.00      0.00      0.00
07:18:01 AM        lo    303.97    303.97    123.36    123.36      0.00      0.00      0.00
```

重点关注 `rxkB/s`、`txkB/s` 两列，如果这两列的值持续接近网络带宽的极限，那就必须提升集群的网络配置，比如升级万兆网卡、交换机，如果集群跨机房，申请更多的跨机房带宽。

### 3 扩容注意事项

当新的服务器准备好之后，在新的 Elasticsearch 节点加入到集群之前，集群扩容的过程中有几点需要注意。

#### 3.1 调整最小主节点数

最小主节点数的配置约束为多数，由于扩容后集群节点总数增加，有可能导致原来配置的最小主节点数不足多数，因此可能需要对该参数进行调整。

如果不进行调整，集群可能会脑裂，对该参数的调整非常重要。现在可以通过 REST API 来调整。

```
PUT /_cluster/settings
{
    "persistent" : {
        "discovery.zen.minimum_master_nodes" : $X
    }
}
```

该配置会立即更新，并且持久化到集群状态中，此处的配置会优先于配置文件中的相同配置。也就是说如果配置文件中的值不同，最终会以 REST API 的设置为准。这样，你原来的集群就无需重启。

#### 3.2 调整节点分片总数

`total_shards_per_node` 用来限制某个索引的分片在单个节点上最多允许分配多少个。当集群扩容后，为了让分片分布到更多的节点，利用更多的资源，该值可能需要进行调整，可以通过 REST API 来动态调整。下列参数调整单个索引的配置。

```
index.routing.allocation.total_shards_per_node
```

或者通过下列参数调整整个集群的配置，对所有的索引都生效：

```
cluster.routing.allocation.total_shards_per_node
```

如果你原来的集群没有配置 `total_shards_per_node`，那么在扩容之前我们强烈建议你先计算好该值设置进去，因为 Elasticsearch 的分片分配策略下会尽量保证节点上的分片数大致相同，而扩容进来的新节点上还没有任何分片，这会导致新创建的索引集中在扩容进来的新节点，热点数据过于集中，产生性能问题。

#### 3.3 集群原有的分片会自动迁移到新节点吗？

答案是会的，Elasticsearch 会把分片迁移到新增的节点上，最终让节点间的分片数量大致均衡，这个过程称为 rebalance 。默认情况下，执行 rebalance 的并发数为 2，可以通过下面的参数进行调整：

```
cluster.routing.allocation.cluster_concurrent_rebalance
```

Elasticsearch 中，Peer recovery 负责副分片的数据恢复，增加副分片，以及 rebalance 等所有把数据从主分片拷贝到另一个节点的过程。因此 rebalance 期间的流量限速可以通过 Peer recovery 的限速开关进行调整：

```
indices.recovery.max_bytes_per_sec
```

同理，你也可以使用 `_cat/recovery` API 查看数据迁移的状态和进度。

数据均衡策略并不会让节点间的分片数量分布完全一致，而是允许存在一定量的差异，有时候我们可能希望集群自己少做一些 rebalance 的操作，容忍节点间的分片数差异更多一点，可以通过调整一些权重值来实现：

- **cluster.routing.allocation.balance.shard**

基于分片数量的权重因子，提高此值使集群中所有节点之间的分片数量更接近相等，默认值 0.45f

- **cluster.routing.allocation.balance.index**

基于某个索引所有分片的权重因子，提高此值使集群中所有节点上某个索引的分片数量更接近相等，默认值0.55f

- **cluster.routing.allocation.balance.threshold**

内部根据权重计算之后的值如果大于 threshold，就执行 rebalance，因此提高此值可以降低执行 rebalance 操作的积极性。

### 总结

本课重点分析了定位系统硬件瓶颈的方法，当从软件层面不能有效改善系统运行性能时，可以采用本课提供的方式去分析是否存在硬件瓶颈。

对 Elasticsearch 集群的扩容是平滑的过程，期间不会影响业务使用，但是一定要注意到本文提及的几个事项，避免线上事故。下一节课我们介绍下集群数据迁移。

## 13.集群迁移

在使用 Elasticsearch 过程中，你会发现你偶尔需要将一个集群的数据迁移到另一个集群，或者把索引的数据迁移到另一个具有不同 mapping 或者分片数的索引。本章总结常见的迁移场景和迁移方法。

### 1. 数据迁移场景

**(1) mapping 发生了改变**

Elasticsearch 的 scheme 十分灵活，支持给类型动态添加新的字段， **6.x** 之前的版本支持给索引动态的添加新的类型。但是不支持修改已有的字段的类型，也不能使用新的分词器对已有的字段进行分析，因未这会影响到已有数据的搜索。

所以，如果业务中 mapping 发生了变化，而你又必须保留历史数据，最简单和直接的办法就是根据新的 mapping 创建好新索引，然后降历史数据从历史索引迁移到新索引。

**(2) 调整分片数**

默认情况下，Elasticsearch 创建的索引分片数为 5 个。或许你在创建索引初期也评估了分片数的设置，但是后期仍然需要调大索引的分片数，如果您使用的是 6.1 之后的版本，那可以采用 shard split 功能。否则只能按照合理的分片数，建立好目标索引，然后降索引数据从历史索引迁移到新索引。

**(3) 拆分索引**

随着业务的发展，前期的设计无法满足目前的性能要求和业务场景。比如索引需要按天划分而不是按月或按周。索引需要按类型划分而不是降多个类型存储到单个索引中等，需要按照合理的方式拆分好目标索引，并将数据从历史索引迁移到新索引

**(4) 机房变化**

由于某种原因，数据从一个数据中心迁移到另一个数据中心。涉及集群数据的整体搬迁。

### 2. 迁移前需要考虑的问题

**(1) _source是否启用？**

Elasticsearch 默认启用 `\_source`字段，`\_source`字段存储了原始 json 文档，`\_source`并不会被索引，它主要用于查询结果展示。Elasticsearch 的 reindex api依赖该字段；而且在没有原始数据的情况下，如果_source没有启用，有些场景的迁移无法完成。

**(2) 版本兼容情况**

Elasticsearch 不支持加载跨版本的索引数据，比如 6.x 可以加载 5.x 的索引文件，但是不能加载 1.x 及 2.x 的索引文件。snapshot/restore 功能也是如此，不支持跨版本的备份和恢复。

所以，在跨集群迁移数据前要明确目标集群和源集群的 Elasticsearch 版本。如果包含跨越大版本的索引，这部分索引只能通过 reindex 来迁移。

### 3. 迁移方法

#### 3.1 snapshot/restore

snapshot/restore 是 Elasticsearch 用于对数据进行备份和恢复的一组 api 接口，可以通过 snapshot/restore 接口进行跨集群的数据迁移，该方法支持索引级、集群级的 snapshot/restore。

**(1) 前提条件**

目的集群的 Elasticsearch 版本号要大于等于源端集群索引版本号且不能跨越大版本。

**(2) 操作步骤**

- 源集群配置仓库路径 修改源集群中 Elasticsearch 的配置文件，`elasticsearch.yml`，添加如下配置：

```
path.repo: ["/data/to/backup/location"]
```

- 源集群中创建仓库

```
 curl -XPUT "http://[source_cluster]:[source_port]/_snapshot/backup"  -d '{
     "type": "fs",
     "settings": {
         "location": "/data/to/backup/location" 
         "compress": true
     }
 }'
```

- 源集群创建 snapshot

```
 curl -XPUT http://[source_cluster]:[source_port]/_snapshot/backup/indices_snapshot_1?wait_for_completion=true
```

- 目标集群配置和创建仓库

该步骤与源集群中步骤类似, 不再赘述。

- 将 snapshot 从源集群仓库移动到目的集群仓库
- 目的集群执行 restore

```
curl -XPUT "http://[dest_cluster]:[dest_port]/_snapshot/backup/indices_snapshot/_restore"
```

- 检查恢复状态

```
 curl -sXGET "http://[dest_cluster]:[dest_port]/_snapshot/_status"
```

**(3) 适用场景**

该迁移方式适合大量数据的迁移，支持增量迁移，但是需要比较大的存储空间来存放 snapshot。

#### 3.2 reindex

reindex 支持集群内和集群间的索引数据迁移。

**(1) 前提条件**

源端索引要启用 `\_source`字段, 如果没有则不能进行 reindex；reindex 需要事先在目标集群(源集群和目标集群可以是同一个集群)按照要求建立好目标索引，reindex 过程并不会自动降源端索引的设置拷贝到目标索引，否则 reindex 就失去了意义。所以在 reindex 前，要设置好目标索引的 mapping、分片数。

**(2) 集群内 reindex**

集群内 reindex 比较简单，按照新的需求创建好目标索引后，执行如下命令即可：

```
POST _reindex
{
  "source": {
    "index": "twitter"
  },
  "dest": {
    "index": "new_twitter"
  }
}
```

**(3) 跨集群 reindex**

跨集群 reindex 与集群内 reindex 的主要区别是源端集群的信息需要配置到目标集群的 Elasticsearch 配置文件里，例如：

```
reindex.remote.whitelist: "otherhost:9200, another:9200"
```

由于这是静态配置，配置完成后需要重启节点。之后可以通过如下命令进行数据迁移：

```
POST _reindex
{
  "source": {
    "remote": {
      "host": "http://otherhost:9200",
      "username": "user",
      "password": "pass"
    },
    "index": "source",
    "query": {
      "match": {
        "test": "data"
      }
    }
  },
  "dest": {
    "index": "dest"
  }
}
```

**(4) 控制参数**

reindex 提供了很多控制参数，下面介绍几个常用的配置：

- `size`

指定迁移的数据条数。

- `_source`

指定需要迁移数据中的哪些字段。

- `size in source`

可以指定一次 scroll 的数据条数，用来控制 reindex 对源、目的集群资源消耗压力。 默认值为 1000。由于 index 过程比较消耗 cpu 资源，所以需要根据硬件环境合理配置，可以先配置一个较小的值，如果资源压力不大，逐步加大到合适的值，然后重新启动 reindex 过程。

- `connect_timeout`

跨集群 reindex 时，远端集群连接超时时间，可以根据网络情况进行调整。默认值时 30s。

- `socket_timeout`

跨集群 reindex 时，远端集群的读超时时间，可以根据网络情况进行调整。默认值是 30s。

- `slices`

可以控制 reindex 的并发度。应用官方文档的例子：

```
POST _reindex?slices=5&refresh
{
  "source": {
    "index": "twitter"
  },
  "dest": {
    "index": "new_twitter"
  }
}
```

> 注意：较大的值会对源端和目的端带来资源压力。需要逐步加大，观察源集群和目的集群的资源适用情况。

**(5) 适用场景**

该方法依赖源端索引启用 `_source` 字段，能够提供 query 来迁移索引的一部分数据。适用于迁移数据量和索引数都比较小的场景。

#### 3.3 拷贝索引文件

**(1) 前提条件**

源端集群和目标集群索引版本兼容，该方法不适用于集群内迁移，也不能改变目标索引的相关设置。

**(2) 迁移步骤**

迁移步骤以单个索引为例。如果有多个，可以根据步骤实现自动化脚本来完成。

- 禁止源集群中索引的分片移动

```
curl -XPUT "http://[source_cluster]:[source_port]/_cluster/settings" -d
'{
  "persistent": {
    "cluster.routing.allocation.enable": "none"
  }
}'
```

- 源集群停止写入、更新操作，并在源端集群执行执行 `sync_flush`

```
curl -XPOST "http://[source_cluster]:[source_port]/$index/_flush/synced"
```

- 查找主分片目录

Elasticsearch 5.x 之前，索引数据是存放在以索引名为目录的文件夹下，5.x 起是存放在 uuid 目录下，索引首先确定源端索引的 uuid：

```
curl -sXGET "http://[source_cluster]:[source_port]/_cat/indices/$index?v"
```

之后确定所有主分片所在节点：

```
curl -sXGET "http://[source_cluster]:[source_port]/_cat/shards/$index" | grep " p "
```

最后一列为各个主分片所在的节点。这样就可以根据节点配置的 path.data 及索引的 uuid 找到索引存放的位置。

- 将索引主分片的所有数据拷贝到目标集群。

采用 rsync 方式，将索引文件从源集群数据目录拷贝到目的集群中的数据目录中。Elasticsearch 会加载拷贝的索引文件。

- 检查迁移结果

待目的集群将迁移过去的索引加载完成，集群状态恢复成 Green 后，检查目的集群中该索引的 doc 数是否与源集群中对应索引的 doc 数是否相等。

- 打开副本

迁移过程只迁移了主分片，如果索引在目的集群中有副本需求，需要根据需求设置合理的副本数量。一般保留一个副本即可：

```
curl -XPUT "http://[dest_cluster]:[dest_port]/$index/_settings" -d '{"index.number_of_replicas": 1}'
```

**(3) 注意事项**

当删除一个索引时，Elasticsearch 会在集群状态里保存删除的索引的名称，防止被删除的索引被重新加载到集群，

`cluster.indices.tombstones.size`, 默认值500。如果目标集群删除的索引列表中包含同名代迁移的索引，则拷贝的索引文件会出现不能加载的情况。检查方法

```
curl -sXGET 'http://localhost:9200/_cluster/state/metadata?pretty'
```

执行以上命令，在 “index-graveyard” 部分查找是否有和要导入索引同名的索引。如果存在，可以减少 `cluster.indices.tombstones.size` 的配置，或者通过脚本创建删除索引使该索引名从 index graveyard 里移除，例如：

```
for((i=0;i<500;i++>))
do
    curl -XPUT "http://localhost:9200/index_should_not_exists_$i";
    curl -XDELETE "http://localhost:9200/index_should_not_exists_$i;
done
```

**(4) 适用场景**

该方法采用直接拷贝索引文件的方式，迁移速度完全取决于带宽， 对带宽占用较高，对 cpu 和内存资源占用很低。适用于有大量数据需要迁移的场景。源端索引不需要启用 `\_source` 字段。我们做过多次数据迁移，优先都是采用此方式。

### 总结

本课介绍了数据迁移的方法及不同方法适用的场景，希望可以帮助需要迁移数据的同学找到合适的迁移方法。

下一节我们介绍下常用的**重要配置及注意事项。**

## 14.常用配置及注意事项

Elasticsearch 各个模块提供了很多配置参数，用来满足不同的业务场景。本篇我们结合我们的经验对参数进行归类介绍，方便读者进行配置。主要包括索引性能参数、查询参数、稳定性参数、数据安全参数、内存参数、allocation 参数、集群恢复参数。

### 1. 索引性能参数

**1. index.translog.durability**

为了保证写入的可靠性，该值的默认参数值为 request，即，每一次 bulk、index、delete 请求都会执行 translog 的刷盘，会极大的影响整体入库性能。

如果在对入库性能要求较高的场景，系统可以接受一定几率的数据丢失，可以将该参数设置成 “async” 方式，并适当增加 translog 的刷盘周期。

**2. index.refresh_interval**

数据索引到 Elasticsearch 集群中后，要经过 refresh 这个刷新过程才能被检索到。为了提供近实时搜索，默认情况下，该参数的值为 1 秒，没次 refresh 都会产生一个新的 Lucene segment，会导致后期索引过程中频繁的 merge。

对于入库性能要求较高，实时性要求不太高的业务场景，可以结合`indices.memory.index_buffer_size`参数的大小，适当增加该值。

**3. index.merge.policy.max_merged_segment**

索引过程中，随着新数据不断加入，Lucene 会根据 merge 策略不断的对已产生的段进行 merge 操作。`index.merge.policy.max_merged_segment` 参数控制此 merge 过程中产生的最大的段的大小，默认值为 5gb。为了提高入库性能，可以适当降低配置的大小。

**4. threadpool.write.queue_size**

index、bulk、delete、updaet 等操作的队列大小，默认值为 200。队列满时，说明已经没有足够的 CPU 资源做写入操作。加大配置并不能有效提高索引性能，但是会增加节点 OOM 的几率。

有一点要注意的是，Elasticsearch 首先在主分片上进行写入操作，然后同步到副本。为了保持主副本一致，在主分片写入后，副本并不会受该设置的限制，所以一个节点如果堆积了大量的副本写入操作，会增加节点 OOM 的几率。

### 2. 查询参数

**1. threadpool.search.queue_size**

查询队列大小，默认值为1000。不建议将该值设置的过大，如果 search queue 持续有数据，需要通过其他策略提高集群的并发度，比如增加节点、同样的数据减少分片数等。

**2. search.default_search_timeout**

控制全局查询超时时间，默认没有全局超时。可用通过 Cluster Update Settings 动态修改。在生产集群，建议设置该值防止耗时很长的查询语句长期占用集群资源，讲集群拖垮。

**3. cluster.routing.use_adaptive_replica_selection**

在查询过程中，Elasticsearch 默认采用 round robin 方式查询同一个分片的多个副本，该参数可以考虑多种因素，将查发送到最合适的节点, 比如对于包含查询分片的多个节点，优先发送到查询队列较小的节点。生产环境中，建议打开该配置。

```
PUT /_cluster/settings
{
    "transient": {
        "cluster.routing.use_adaptive_replica_selection": true
    }
} 
```

**4. action.search.max_concurrent_shard_requests**

限制一个查询同时查询的分片数。防止一个查询占用整个集群的查询资源。该值需要根据业务场景进行合理设置。

**5. search.low_level_cancellation**

Elasticsearch 支持结束正在执行的查询任务，但是在默认情况下，只在 segments 之间有是否结束查询的检查点。默认为 false。将该参数设置成 true 后，会在更多的位置进行是否结束的检查，这样会更快的结束查询。

如果集群中没有很多大的 segment，不建议修改该值的默认设置，设置后过多的检查任务是否停止会对查询性能有很大的影响。

**6. execution_hint**

Elasticsearch 有两种方式执行 terms 聚合，默认会采用 `global_ordinals` 动态分配 bucket。大部分情况下，采用 `global_ordinals` 的方式是最快的。

但是对于查询命中结果数量比较小的时候，采用 map 方式会极大减少内存的占用。引用官方文档的例子，使用方式如下：

```
GET /_search
{
    "aggs" : {
        "tags" : {
             "terms" : {
                 "field" : "tags",
                 "execution_hint": "map" 
             }
         }
    }
}
```

### 3. 稳定性参数

**1. discovery.zen.minimum_master_nodes**

假设一个集群中的节点数为n，则至少要将该值设置为`n/2 + 1`，防止发生脑裂的现象。

**2. index.max_result_window**

在查询过程中控制 from + size 的大小。查询过程消耗 CPU 和堆内内存。一个很大大值，比如 1000 万，很容易导致节点 OOM，默认值为 10000。

**3. action.search.shard_count.limit**

用于限制一次操作过多的分片，防止过多的占用内存和 CPU 资源。建议合理设计分片和索引的大小。尽量查询少量的大的分片，有利于提高集群的并发吞吐量。

### 4. 数据安全参数

**1. action.destructive_requires_name**

强烈建议在线上系统将该参数设置成 true，禁止对索引进行通配符和 _all 进行删除，目前 Elasticsearch 还不支持回收站功能，程序bug或者误操作很可能带来灾难性的结果——**数据被清空**！

**2. index.translog.durability**

如果对数据的安全性要求高，则该值应该配置成 “request”，保证所有操作及时写入 translog。

### 5. 内存参数

**1. indices.fielddata.cache.size**

限制 Field data 最大占用的内存，可以按百分比和绝对值进行设置，默认是不限制的, 也就是整个堆内存。 如果业务场景中持续有数据加载到 Fielddata Cache，很容易引起 OOM。所以我建议初始时将该值设置的比较保守一些，当遇到查询性能瓶颈时再结合软硬件资源调整。

**2. indices.memory.index_buffer_size**

在索引过程中，新添加的文档会先写入索引缓冲区。默认值为堆内存的 10%。更大的 index buffer 通常会有更高的索引效率。

但是单个 shard的index buffer 超过 512M 以后，索引性能几乎就没有提升了。所以，如果为了提高索引性能，可以根据节点上执行索引操作的分片数来合理设置整个参数。

**3. indices.breaker.total.limit**

父级断路器内存限制，默认值为堆内存的 70%。 对于内存比较小的集群，为了集群的稳定性，建议该值设置到 50% 以下。

**4. indices.breaker.fielddata.limit**

防止过多的 Fielddata 加载导致节点 OOM，默认值为堆内存的 60%。 在生产集群，建议将该值设置成一个比较保守的值，比如 20%，在性能确实由于该值配置较小出现瓶颈时，合理考虑集群内存资源后，谨慎调大。

### 6. allocation 参数

**1. cluster.routing.allocation.disk.watermark.low**

该参数表示当磁盘使用空间达到该值后，新的分片不会继续分配到该节点，默认值是磁盘容量的 85%。

**2. cluster.routing.allocation.disk.watermark.high**

参数表示当磁盘使用空间达到该值后，集群会尝试将该节点上的分片移动到其他节点，默认值是磁盘容量的90%。对于索引量比较大的场景，该值不宜设置的过高。可能会导致写入速度大于移动速度，使磁盘写满，引发入库失败、集群状态异常的问题。

`index.routing.allocation.include.{attribute}；`

`index.routing.allocation.require.{attribute};`

`index.routing.allocation.exclude.{attribute} ;`

- include 表示可以分配到具有指定 attribute 的节点；
- require 表示必须分配到具有指定 attribute 的节点；
- exclude 表示不允许分配到具有指定 attribute 的节点。

Elasticsearch 内置了多个 attribute，无需自己定义，包括 `_name`,`_host_ip`,`_publish_ip`, `_ip`, `_host`。attribute 可以自己定义到 Elasticsearch 的配置文件。

**3. total shards per node**

控制单个索引在一个节点上的最大分片数，默认值是不限制。我们的经验是，创建索引时，尽量将该值设置的小一些，以使索引的 shard 比较平均的分布到集群内的所有节点。

### 7. 集群恢复参数

**1. indices.recovery.max_bytes_per_sec**

该参数控制恢复速度，默认值是 40MB。如果是集群重启阶段，可以将该值设置大一些。但是如果由于某些节点掉线，过大大值会占用大量的带宽北恢复占用，会影响集群的查询、索引及稳定性。

**2. cluster.routing.allocation.enable**

控制是否可以对分片进行平衡，以及对何种类型的分片进行平衡。可取的值包括：all、primaries、replicas、none，默认值是 all。

- all 是可以对所有的分片进行平衡；
- primaries 表示只能对主分片进行平衡；
- replicas 表示只能对副本进行平衡；
- none 表示对任何分片都不能平衡，也就是禁用了平衡功能。

有一个小技巧是，在重启集群之前，可以将该参数设置成 **primaries**，由于主分片是从本地磁盘恢复数据，速度比较快，可以使集群迅速恢复到 Yellow 状态，之后设置成 **all**，开始恢复副本数据。

**3. cluster.routing.allocation.node_concurrent_recoveries**

控制单个节点可以同时恢复的副本的数量，默认值为 2。副本恢复主要瓶颈在于网络，对于网络带宽不大的环境，不需要修改该值。

**4. cluster.routing.allocation.node_initial_primaries_recoveries**

控制一个节点可以同时恢复的主分片个数，默认值为 4。由于主分片是从本地存储恢复，为了提高恢复速度，完全可以加大设置。

**5. cluster.routing.allocation.cluster_concurrent_rebalance**

该参数控制在平衡过程中，同时移动的分片数，加大可以提高平衡的速度。一般在集群扩容节点、下线节点后，可以加大，使集群尽快的进行平衡。

### 总结

本课主要把我们在实践中常用的参数进行了分类，并对结合我们的经验对参数的设置及影响做了说明。希望可以给读者提供一些设置参考。

这部分内容主要介绍在业务正式上线前如何压测，上线后对于分片和节点层面的管理工作，在集群运行过程中合适扩容等，第一部分和第二部分组合起来就是一个集群的生命周期。在此期间，“安全”是无法回避的话题，下一部分我们介绍下如何使用 X-Pack 进行安全防护。

## 15.使用 X-Pack 提供安全保护

Elasticsearch 的免费版本中不包含安全防护机制，安全保护功能在商业组件 X-Pack 中提供，属于付费功能。

如果不使用 X-Pack 作为安全组件，也可以使用 Nginx 反向代理，自己编写插件，或者其他厂商开发的插件，如 SG 等方式。

官方的 X-Pack 提供安全保护比较全面，包括：

- 身份认证，鉴定用户是否合法
- 用户鉴权，指定某个用户可以访问哪个索引
- 传输加密，使用 SSL/TLS 加密节点间的传输，防止监听和篡改
- 审计日志，记录用户对系统执行了哪些操作

如果反向代理不能满足你的需求，我们推荐使用 X-Pack 作为 Elasticsearch 的安全防护组件。

### 1. 身份认证

验证用户身份，这是安全策略的第一步。为了让具有合法身份的用户才可以访问系统，在认证体系中通常考虑三种类型：

- 你知道什么。这种类型让用户提供用户名和密码的方式
- 你有什么。这种类型一般让用户提供秘钥或 kerberos 票据等方式
- 你是谁。一般使用生物识别技术

X-Pack 中使用前两种对用户进行身份认证。对于客户端提交的用户名和密码如何进行校验？在 X-Pack 中可以使用 LDAP，或者 Active Directory 等后端认证服务器，这些认证服务在 X-Pack 中被称为 Realms，你可以使用内置的基于文件的认证，或者通过插件实现自定义 Realm。

除了使用用户和名密码进行身份认证之外，客户端也可以通过 Kerberos 票据等方式进行认证，这种方式需要准备好票据文件。

内置的 Realms 有如下类型：

#### 1.1 内置 Realm

内置 Realm 的身份认证过程完全由 X-Pack 实现，无需借助于外部系统，用户信息在内部管理。

**native**

支持以用户名和密码的方式进行认证，Elasticsearch 将用户信息存储到集群的索引中。

**file**

与 native 的区别是将用户信息存储到本地磁盘的文件中，这个文件需要同步到集群的每个节点。

#### 1.2 外部 Realm

外部认证使用其他的第三方进行身份认证，常见的场景是接入企业内部已有的认证服务器进行统一的用户管理。

例如企业内部有许多的大数据平台，每个平台都需要进行身份认证，为每个平台单独建立用户体系是不合理的，因此通常使用统一的用户体系进行管理。目前我们使用 ldap。

**1. ldap**

使用后端 ldap 服务器进行认证，认证方式也是用户名和密码的方式，许多公司有内部的 ldap 服务器来管理用户，这种方式可以使用统一的账号管理体系。

**2. active_directory**

通过微软的活动目录服务器验证用户名和密码，在 Windows 系统中，活动目录服务器用于域模式下的用户管理。

**3. pki**

基于公钥进行身份认证。

**4. saml**

使用 SAML 2.0 Web SSO 进行身份认证

**5. kerberos**

使用 kerberos 进行身份认证，客户端需要准备好 kerberos 票据文件。

经过对用户的身份认证之后，系统已经安全很多，只有合法用户才能访问集群，不会被人任意操作。

现在面临新的问题是：所有的用户对集群都拥有完整的管理权限，有时候我们希望现在用户的某些行为。

例如，我希望某个用户只访问他自己的索引，并且限制他不能执行集群管理操作，这就需要我们对用户进行鉴权，对于某个特定用户，他可以有哪些权限。

### 2. 用户鉴权

X-Pack 提供的用户鉴权是基于角色的访问控制，简称为 RBAC。RBAC 首先定义一个角色，为这个角色分配一组权限，权限支持索引级和字段级。然后再将角色分配给用户或用户组。

例如定义一个角色 role_weblog，为这个角色分配可以访问索引 `weblog-*` 的写入和读取权限，然后为用户 A 和用户 B 都分配这个角色，他们就都拥有了索引 `weblog-*`的读写权限。

一个用户可以同时拥有多个角色，某个角色也可以分配给多个用户。

**RBAC 有以下重要的概念：**

**1. Secured Resource**

要描述对资源有哪些权限，首先需要定义资源是什么。在 Elasticsearch 中，资源包括：索引、别名、文档、字段、用户、以及集群本身。这些都是受保护的资源。

**2. Privilege**

用于描述用户可以对资源执行什么样的操作，每个资源有自己的一组 Privilege 名称，例如：

对于索引这种资源有：*create*，*delete*，*read*，*write* 等。 对于集群，有：*manage*，*monitor* 等。

**3. Permissions**

由一个或多个对资源的特定 Privilege 组成，例如，对索引 `weblog-*` 的 read 权限。

**4. Role** 一系列 Permissions 的组合，例如，对索引 `weblog-*` 的 read 权限，以及集群的 monitor 权限组成一个角色。

**5. User** 被鉴权的用户

**6. Group** 用户可以属于一个或多个组，不过有些 realms 不支持用户组，例如 native、file、以及 PKI 。

当某个角色分配给用户组时，组中的用户所拥有的最终角色为：分配给用户组的角色加分配给该用户的角色的组合。同样，用户拥有的权限为其所拥有的全部角色的并集。

X-Pack 还支持基于属性的访问控制，把属性分配给用户和文档，再到角色中定义访问策略，具有该角色的用户只有在具备必需的属性时才能访问特定文档。不在本文论述范围。

### 3. 通讯加密

早期的 X-Pack 版本中，身份认证可以单独开启，现在则要求在开启身份认证的同时必须同时开启内部节点之间的通讯加密（单节点的集群除外）。

X-Pack 安全组件中的通讯加密支持客户端到集群的通讯加密，以及集群节点之间内部通讯的加密，这些都通过 TLS/SSL 实现。其中客户端到集群的通讯加密是可选的。

如果不加密节点之间的通讯，攻击者可以通过抓包获取到明文的数据，包括集群状态，索引数据内容等，这些数据都可能是机密信息。攻击者甚至可以篡改这些数据。

另外攻击者也可以尝试自己启动节点加入集群。节点间的通讯加密就是为了阻止这些情况。

开启节点间的通讯加密大致需要以下两个步骤：

- 生成节点证书
- 修改节点配置，启用 TLS

配置完毕后需要对集群执行完全重启。

### 总结

当集群开启身份认证之后，Kibana，Logstash，Beats，java 客户端等都需要调整相应的配置。

本文介绍了目前的版本中 X-Pack 提供的安全防护功能，并重点介绍了身份认证和用户鉴权的基础知识，接下来我们会以一些例子实际操作一下身份认证、用户鉴权、以及通信加密的具体配置方法及注意事项。

下一课程我们介绍如何部署和应用身份认证。

### 参考

[1] [Realms](https://www.elastic.co/guide/en/elastic-stack-overview/6.5/realms.html#_internal_and_external_realms)

[2] [Security privilegesedit](https://www.elastic.co/guide/en/elastic-stack-overview/6.5/security-privileges.html)

[3][User authorizationedit](https://www.elastic.co/guide/en/elastic-stack-overview/6.5/authorization.html)

## 16.X-Pack 实战：身份认证

先前的版本中，X-Pack 作为 Elasticsearch 插件的形式存在，在 6.x 中，已经做为一个内部模块默认携带。要开启身份认证，需要将下列两个配置设置为 true：

```
xpack.security.enabled: true
xpack.security.transport.ssl.enabled: true
```

早期的版本中，身份认证可以单独开启，现在的版本中，会强制要求开启集群节点间的通讯加密，关于通讯加密请参考《通讯加密》

开启上述配置后重启集群，安全机制生效，必须使用正确的用户名和密码才能访问集群。早期版本中，可以使用使用超级用户: elastic 和密码：changeme 来登录系统，从 6.0 开始，默认密码被禁用，用户必须首先初始化内置用户的密码才能使用安全模块。

### 1. 初始化内置账号

X-Pack 中有几个内置用户，其中 elastic 为超级用户，其他更多可以参考官方手册。

使用下面的命令来初始化所有内置用户的密码：

```
bin/elasticsearch-setup-passwords interactive
```

该命令以交互的方式要求为每个内置账号输入两次密码来初始化，如下图所示：

![avatar](https://images.gitbook.cn/Fu7cfkr8bZU77fOrAzNEhfPmZTnY)

注意，该命令只能执行一次，后续可以通过 Kibana 或者 API 来管理用户和角色。

在 Elasticsearch 6.x 的版本中，这些内置用户以及加密后的密码存储在 Elasticsearch 的索引 `.security-6` 中。

### 2. 配置基于文件的用户身份认证

基于文件的用户身份认证类似 Linux 操作系统默认的身份认证，将用户名和密码记录到本地磁盘文件中，然后通过一些命令管理用户。

也正因为这种特点，他需要管理员来自行保证每个节点上的用户名密码文件保持同步， X-Pack 本身不会做这个工作。如果你在 A 节点添加了一个用户，但在 B 节点上没有添加，结果将会在 A 节点认证成功，而 B 节点认证失败。

虽然有这种明显的缺点，但他仍然是一种非常重要的认证：唯一不依赖其他系统的认证方式。因此当认证系统出现异常时他可以用做故障恢复。通过下面的命令，我们添加一个超级管理员用户 admin：

```
bin/elasticsearch-users useradd admin -r superuser
```

根据提示输入两次密码，新用户创建成功。如果有一天你忘记了密码，可以简单地通过下面的命令重设：

```
bin/elasticsearch-users passwd admin
```

重设密码时不会用到旧密码，直接输入新密码即可修改。基于文件的用户认证会将用户名和密码存储到文件：`config/users` 中，将用户所属的角色记录到文件 ：`config/users_roles` 中。在这个例子中，两个文件的内容为：

```
cat  config/users
admin:$2a$10$UJa7OKDinHjO0Pu4S9xHfO/oV5vr793.mV2JXGcgD1spnkhFHe9nS
```

```
cat  config/users_roles
superuser:admin
```

X-Pack 内置的两种 Realm 默认开启，因此我们无需再 elasticsearch.yml 中添加额外的配置。添加完新用户后，无需重启集群，用户或密码会稍后生效（默认 5 秒钟重新加载一次这两个文件，可以通过配置项：`resource.reload.interval.high` 来调整），现在你可以使用新创建的用户访问集群。

### 3. 配置 LDAP 身份认证

LDAP 以分层的方式存储用户和用户组，类似文件系统的树形结构。DN 用户标识一条纪录，他描述了一条数据的详细路径。例如，有如下 DN：

```
"cn=admin,dc=example,dc=com"
```

按照文件系统树形结构的理解方式，该 DN 的结构为：

```
com/example/admin
```

其中，com 、example 可以理解为文件夹，admin 为文件，因此，cn 通常代表用户名或组名，dc 为路径。

LDAP 支持用户组，当客户端传递用户名和密码过来的时候，并不携带用户组信息，因此服务器需要找到这个用户所属组的完整 DN，然后再进行验证。X-Pack 的 ldap realm 支持两种模式：

#### 3.1 User search mode

用户搜索模式是最常见的方式，这种模式需要配置一个具有搜索 LDAP 目录权限的用户，来搜索待认证用户的 DN，然后用找到的 DN 和用户提交的密码，到配置的 LDAP 服务器进行身份认证。

下面是一个用户搜索模式的配置案例 elasticsearch.yml ：

```
xpack:
  security:
    authc:
      realms:
        ldap1:
          type: ldap
          order: 0
          url: "ldap://ldap.xxx.com:389"
          bind_dn: "cn=admin,dc=sys,dc=example, dc=com"
          user_search:
            base_dn: "dc=sys,dc=example,dc=com"
            filter: "(cn={0})"
          group_search:
            base_dn: "dc=sys,dc=example,dc=com"
          files:
            role_mapping: "ES_PATH_CONF/role_mapping.yml"
          unmapped_groups_as_roles: false
```

修改配置文件后需要重启集群使他生效，各个字段的含义如下：

- **type**

  realm 类型必须设置为 ldap

- **order**

  X-Pack 允许配置多个 realm，此配置用于指定当前 realm 的顺序，0为最先执行

- **url**

  LDAP 服务器地址，为了灾备和负载均衡的考虑，可以按数组的形式配置多个地址：[ "ldaps://server1:636", "ldaps://server2:636" ]。

  当配置了多个地址，X-Pack 默认会使用第一个地址进行认证，如果 LDAP 服务器连接失败，再尝试第二个。你可以通过修改 `load_balance.type` 配置来调整使用故障转移，还是轮询方式。

- **bind_dn**

  通过哪个用户搜索 LDAP，这个选项用于配置该用户的 DN

- **user_search.base_dn**

  用于指定到哪个路径进行搜索

- **filter**

  过滤器中的 {0} 会替换为待认证用户的用户名。

- **files.role_mapping**

  指定 `role_mapping` 文件的位置，默认为：`ES_PATH_CONF/role_mapping.yml` 该文件中描述了某个角色下都有哪些用户，当用户登录成功后，根据这个文件计算用户都有哪些角色。

除此之外，我们还需要为 `bind_dn` 配置密码，密码不适合明文配置到文件中，Elasticsearch 设计了一种称为安全配置的方式，将敏感配置信息加密存储到 Elasticsearch keystore，在这个例子中，我们使用下面的命令为 `bind_dn` 添加密码配置：

```
bin/elasticsearch-keystore add \
xpack.security.authc.realms.ldap1.secure_bind_password
```

根据提示输入密码完成设置。这里要注意一点，当你需要删除 ldap 配置时，从 elasticsearch.yml 删除 ldap1 的相关配置后，同时也需要执行 elasticsearch-keystore remove 从 keystore 中删除密码配置，否则 Elasticsearch 会认为 ldap1 的配置不完整，无法启动节点。

X-Pack 内置的两种 realms 无需单独配置，默认会启用，但是当你在 elasticsearch.yml 明确配置了 realms，那么身份认证过程只会使用你配置的 realms， native 以及 file 不再生效，不过系统内置账户除外，如 elastic。

如果需要同时使用 native 或 file，需要明确配置。我们建议将 file 作为第一验证方式，下面的例子中，先使用 file 进行认证，如果认证失败，再使用 ldap 进行认证：

```
xpack:
  security:
    authc:
      realms:
        file:
          type: file
          order: 0
        ldap1:
          type: ldap
          order: 1
          ....
```

#### 3.2 DN templates mode

如果你的 LDAP 环境定义了标准的命名规则，那么可以考虑使用 DN 模板方式，这种方式的优点是不用执行搜索就可以找到用户 DN，但是，可能需要多个绑定操作来找到正确的用户 DN。

DN 模板方式的配置示例如下：

```
xpack:
  security:
    authc:
      realms:
        ldap1:
          type: ldap
          order: 0
          url: "ldap://ldap.xxx.com:389"
          user_dn_templates:
            - "cn={0},dc=sys,dc=example,dc=com"
            - "cn={0},dc=ops,dc=example,dc=com"
          group_search:
            base_dn: "dc=example,dc=com"
          files:
            role_mapping: "ES_PATH_CONF/role_mapping.yml"
          unmapped_groups_as_roles: false
```

该配置与用户搜索模式的区别很少：

- 删除了用不到的 `bind_dn`，`user_search` 两个字段
- 增加了 `user_dn_templates` 配置

`user_dn_templates` 至少需要指定一个。DN 模板将会使用待认证的用户名替换字符串 {0}

### 总结

身份认证是保护集群的基础措施，除了身份认证之外，还可以配合 IP 黑名单、白名单等方式来进行基于 IP 地址或子网的过滤， X-Pack 提供了非常全面的集群安全防护措施。

在识别了用户的合法身份之后，我们就可以在用户的基础上限制各种操作和资源，下一节我们介绍用户鉴权。

### 参考

[1] [Configuring an LDAP realm](https://www.elastic.co/guide/en/elasticsearch/reference/6.5/configuring-ldap-realm.html)

[2][Security settings in Elasticsearch](https://www.elastic.co/guide/en/elasticsearch/reference/6.5/security-settings.html)

## 17.X-Pack 实战：用户鉴权

在上一课中，我们解决了身份认证的问题，现在，我们能够保证访问集群的是合法用户，但是任何用户都可以访问集群的全部索引，在多个业务使用同一个集群时，可能某个业务的数据不希望被其他人看到，也不希望被其他人写入，删除数据，这就需要控制用户的权限，让某个用户只能访问某些索引。

在 X-Pack 中，这通过 RBAC 实现，他支持字段级别的权限配置，并且可以限制用户对集群本身的管理操作。

**X-Pack 中对用户进行授权包括两个步骤：**

- 创建一个角色，为角色赋予某些权限。
- 将用户映射到角色下。

### 1. 内置角色

如同内置用户一样，X-Pack 中内置一些预定义角色，这些角色是只读的，不可修改。内置用户与角色的映射关系如下图所示：

![avatar](https://images.gitbook.cn/FsJW9j7QwD6qzLc-ycUwJkC-aawj)

**下面介绍一些常用的内置角色：**

- **superuser**

  对集群具有完全的访问权限，包括集群管理、所有的索引，模拟其他用户，并且可以管理其他用户和角色。默认情况下，只有 elastic 用户具有此角色。

- **kibana_system**

  具有读写Kibana索引，管理索引模板，检查集群可用性等权限，对 `.monitoring-*` 索引具有读取权限，对 `.reporting-*` 索引具有读写权限。

- **logstash_system**

  允许向 Elasticsearch 发送系统级数据，例如监控数据等。

### 2. Security privilege

我们首先需要了解一下对 Elasticsearch 中的资源都有哪些权限，例如在 Linux 文件系统中，对文件有读/写，执行等权限，Elasticsearch 中资源的权限也有特定的名称，下面列出一些常用的 privilege，完整的列表请参考官方手册。

#### 2.1 Cluster privileges

对集群的操作权限常见的有下列类型：

- **all**

  所有集群管理操作，如快照、节点关闭/重启、更新设置、reroute 或管理用户和角色。

- **monitor**

  所有的对集群的只读操作，例如集群健康，集群状态，热点线程，节点信息、节点状态、集群状态，挂起的集群任务等。

- **manage**

  基于 monitor 权限，并增加了一些对集群的修改操作，例如快照，更新设置，reroute，获取快照及恢复状态，但不包括安全相关管理权限。

- **manage_index_templates**

  对索引模块的所有操作

- **manage_rollup**

  所有的 rollup 操作，包括创建、启动、停止和删除 rollup 作业。

- **manage_security**

  所有与安全相关的操作，如用户和角色上的 CRUD 操作和缓存清除。

- **transport_client**

  传输客户端所需的所有权限，在启用跨集群搜索时会用到。

#### 2.2 Indices privileges

- **all**

  对索引的所有操作。

- **create**

  索引文档，以及更新索引映射的权限。

- **create_index**

  创建索引的权限。创建索引请求可能包含添加到别名的信息。在这种情况下，请求还需要对相关索引和别名的 **manage** 权限。

- **delete**

  删除文档的权限。

- **delete_index**

  删除索引。

- **index**

  索引，及更新文档，以及更新索引映射的权限。

- **monitor**

  监控所需的所有操作（recovery, segments info, index stats and status）。

- **manage**

  在 **monitor** 权限基础上增加了索引管理权限（aliases, analyze, cache clear, close, delete, exists, flush, mapping, open, force merge, refresh, settings, search shards, templates, validate）。

- **read**

  只读操作（count, explain, get, mget, get indexed scripts, more like this, multi percolate/search/termvector, percolate, scroll, clear_scroll, search, suggest, tv）

- **read_cross_cluster**

  对来着远程集群的只读操作。

- **view_index_metadata**

  对索引元数据的只读操作（aliases, aliases exists, get index, exists, field mappings, mappings, search shards, type exists, validate, warmers, settings, ilm）此权限主要给 Kibana 用户使用。

- **write**

  对索引的所有写操作，包括索引，更新，删除文档，bulk 操作，更新索引映射。

#### 2.3 Run as privilege

该权限允许一个已经进行了身份认证的合法用户代表另一个用户提交请求。取值可以是一个用户名，或用户名列表。

#### 2.4 Application privileges

应用程序权限在 Elasticsearch 管理，但是与 Elasticsearch 中的资源没有任何关系，他的目的是让应用程序在 Elasticsearch 的角色中表示和存储自己的权限模型。

### 3. 定义新角色

你可以在 Kibana中 创建一个新角色，也可以用 REST API 来创建，为了比较直观的演示，我们先来看一下 Kibana 中创建角色的界面，如下图所示。

![avatar](https://images.gitbook.cn/FhIXy4wjTNTmdCqdqYqdeIxKFxFK)

需要填写的信息包括：

角色名称：此处我们定义为 weblog_user；

集群权限：可以多选，此处我们选择 monitor，让用户可以查看集群信息，也可以留空；

Run As 权限：不需要的话可以留空；

索引权限：填写索引名称，支持通配，再从索引权限下拉列表选择权限，可以多选。如果要为多个索引授权，通过 “Add index privilege” 点击按钮来添加。

如果需要控制字段级权限，在字段栏中填写字段名称，可以填写多个。

如果希望角色只能访问索引的部分文档怎么办？可以通过定义一个查询语句，让角色只能访问匹配查询结果的文档。

类似的，通过 REST API 创建新角色时的语法基本上就是上述要填写的内容：

```
{
  "run_as": [ ... ], 
  "cluster": [ ... ], 
  "global": { ... }, 
  "indices": [ ... ], 
  "applications": [ ... ] 
}
```

其中 global 只在 applications 权限中才可能会使用，因此暂时不用关心 global 字段与 applications 字段。indices 字段中需要描述对哪些索引拥有哪些权限，他有一个单独的语法结构：

```
{
  "names": [ ... ], 
  "privileges": [ ... ], 
  "field_security" : { ... }, 
  "query": "..." 
}
```

names：要对那些索引进行授权，支持索引名称表达式；

privileges：权限列表；

field_security：指定需要授权的字段；

query：指定一个查询语句，让角色只能访问匹配查询结果的文档；

引用官方的一个的例子如下：

```
POST /_xpack/security/role/clicks_admin
{
  "run_as": [ "clicks_watcher_1" ],
  "cluster": [ "monitor" ],
  "indices": [
    {
      "names": [ "events-*" ],
      "privileges": [ "read" ],
      "field_security" : {
        "grant" : [ "category", "@timestamp", "message" ]
      },
      "query": "{\"match\": {\"category\": \"click\"}}"
    }
  ]
}
```

- 创建的角色名称为 `clicks_admin`；
- 以 `clicks_watcher_1` 身份执行请求；
- 对集群有 `monitor` 权限；
- 对索引 `events-*` 有 read 权限；
- 查询语句指定，在匹配的索引中，只能读取 `category` 字段值为 `click` 的文档；
- 在匹配的文档中只能读取 `category`，`@timestamp message` 三个字段；

除了使用 REST API 创建角色，你也可以把将新建的角色放到本地配置文件 `$ES_PATH_CONF/roles.yml` 中，配置的例子如下：

```
click_admins:
  run_as: [ 'clicks_watcher_1' ]
  cluster: [ 'monitor' ]
  indices:
    - names: [ 'events-*' ]
      privileges: [ 'read' ]
      field_security:
        grant: ['category', '@timestamp', 'message' ]
      query: '{"match": {"category": "click"}}'
```

**总结一下 X-Pack 中创建角色的三种途径：**

- 通过 REST API 来创建和管理，称为 Role management API，角色信息保存到 Elasticsearch 的一个名为 `.security-` 的索引中。这种方式的优点是角色集中存储，管理方便，缺点是如果需要维护的角色非常多，并且需要频繁操作时，REST 接口返回可能会比较慢，毕竟每个角色都需要一次单独的 REST 请求。

- 通过记录到本地配置文件 `roles.yml` 中，然后自己用 Ansible 等工具同步到各个节点，Elasticsearch 会定期加载这个文件。

  这种方式的优点是适合大量的角色更新操作，缺点是由于需要自己将 `roles.yml` 同步到集群的各个节点，同步过程中个别节点遇到的异常可能会导致部分节点的角色更新不够及时，最终表现是用户访问某些节点可以操作成功，而某些访问节点返回失败。

- 通过 Kibana 界面来创建和管理，实际上是基于 Role management API 来实现的。

你也可以将角色信息存储到 Elasticsearch 之外的系统，例如存储到 S3，然后编写一个 Elasticsearch 插件来使用这些角色，这种方式的优点是角色集中存储，并且适合大量角色更新操作，缺点是你需要自己动手开发一个插件，并且对引入了新的外部依赖，对外部系统稳定性也有比较高的要求。

### 4. 将用户映射到角色

经过上面步骤，我们已经创建了一个新角色 weblog_user，现在需要把某个用户映射到这个角色下。仍然以 Kibana 图形界面为例，我们可以直接在 Roles 的下拉列表中为用户选取角色，可以多选。

![img](media/15474561832698/15498900722741.jpg)

这是一个非常简单的映射关系，但是映射方法很多：

- 对于 native 或 file 两种类型 realms 进行验证的用户，需要使用 User Management APIs 或 users 命令行来映射。
- 对于其他 realms ，需要使用 role mapping API 或者 一个本地文件 `role_mapping.yml` 来管理映射关系（早期的版本中只支持本地配置文件方式）。
- role mapping API ：基于 REST 接口，映射信息保存到 Elasticsearch 的索引中。
- 本地角色映射文件 `role_mapping.yml`：需要自行同步到集群的各个节点，Elasticsearch 定期加载。

通过REST API 或 `role_mapping.yml` 本地配置文件进行映射的优缺点与创建角色时使用 API 或本地文件两种方式优缺点相同，不再赘述。

使用角色映射文件时，需要在 `elasticsearch.yml` 中配置 `role_mapping.yml` 文件的路径，例如：

```
xpack:
  security:
    authc:
      realms:
        ldap1:
          type: ldap
          order: 0
          url: "ldap://ldap.xxx.com:389"
          bind_dn: "cn=admin,dc=sys,dc=example, dc=com"
          user_search:
            base_dn: "dc=sys,dc=example,dc=com"
            filter: "(cn={0})"
          group_search:
            base_dn: "dc=sys,dc=example,dc=com"
          files:
            role_mapping: "ES_PATH_CONF/role_mapping.yml"
          unmapped_groups_as_roles: false
```

在 `role_mapping.yml` 配置中简单地描述某个角色下都有哪些用户，以 LDAP 用户为例：

```
monitoring: 
  - "cn=admins,dc=example,dc=com" 
weblog_user:
  - "cn=John Doe,cn=contractors,dc=example,dc=com" 
  - "cn=users,dc=example,dc=com"
  - "cn=admins,dc=example,dc=com"
```

上面的例子中，admins 组被映射到了 monitoring 和 `weblog_user` 两个角色，users 组和 John Doe 用户被映射到了 `weblog_user` 角色。这个例子使用角色映射 API 的话则需要执行以下两个：

```
PUT _xpack/security/role_mapping/admins
{
  "roles" : [ "monitoring", "weblog_user" ],
  "rules" : { "field" : { "groups" : "cn=admins,dc=example,dc=com" } },
  "enabled": true
}
```

```
PUT _xpack/security/role_mapping/basic_users
{
  "roles" : [ "user" ],
  "rules" : { "any" : [
      { "field" : { "dn" : "cn=John Doe,cn=contractors,dc=example,dc=com" } },
      { "field" : { "groups" : "cn=users,dc=example,dc=com" } }
  ] },
  "enabled": true
}
```

### 总结

本章重点介绍了 X-Pack 中创建角色，已经将用户映射到角色下的方法和注意事项，无论使用 REST API还是使用本地配置文件，每种方式都有它的优缺点，如果你的环境中已经有一些文件同步任务，那么可以统一使用同步本地配置文件的方式，或者无论创建角色，还是映射角色，全都使用 REST API。

X-Pack 中无法为用户指定特定的索引模板权限，用户要么可以读写所有的模板，要么无法读写模板。而通常来说对于日志等按日期滚动生成索引的业务都需要先创建自己的索引模板，这可能是因为无法预期用户创建索引模板的时候会将模板匹配到哪些索引。

因此推荐不给业务分配索引模板写权限，由管理员角色的用户来检查索引模板规则，管理索引模板。

下一节我们介绍一下节点间的通讯如何加密，在早期版本中，这是一个可选项，现在的版本中要求必须开启。

### 参考

[Defining roles](https://www.elastic.co/guide/en/elastic-stack-overview/6.5/defining-roles.html)

[Security privileges](https://www.elastic.co/guide/en/elastic-stack-overview/current/security-privileges.html)

[Mapping users and groups to roles](https://www.elastic.co/guide/en/elastic-stack-overview/6.5/mapping-roles.html)

[Custom roles provider extension](https://www.elastic.co/guide/en/elastic-stack-overview/6.5/custom-roles-provider.html)

[Setting up field and document level security](https://www.elastic.co/guide/en/elastic-stack-overview/6.5/field-and-document-access-control.html)

## 18.X-Pack 实战：通讯加密

本节课我们主要讨论节点间通讯加密的细节，以及如何配置，使用它们。TLS 使用 X.509 证书来加密通讯数据，为了避免攻击者伪造或篡改证书，还需要验证证书的有效性，与常见的 HTTPS 类似，这里使用 CA 证书来验证节点加密证书的合法性。

因此，当一个新的节点加入到集群时，如果他使用的证书使用相同的 CA 进行签名，节点就可以加入集群。这里涉及到两种证书，为了便于区分理解，暂且这样称呼他们：

- 节点证书，用于节点间的通讯加密
- CA 证书，用于验证节点证书的合法性

### 1. 生成节点证书

Elasticsearch 提供了 elasticsearch-certutil 命令来简化证书的生成过程。它负责生成 CA 并使用 CA 签署证书。你也可以不生成新的 CA，而是使用已有的 CA 来签名证书。我们这里以生成新的 CA 为例。

**1. 执行下面的命令可以生成 CA 证书：**

```
bin/elasticsearch-certutil ca
```

该命令在当前路径下生成一个 PKCS#12 格式的单个文件，默认文件名为 `elastic-stack-ca.p12` 这是一种复合格式的文件，其中包含 CA 证书，以及私钥，这个私钥用于为节点证书进行签名。执行上述命令时会要求输入密码，用来保护这个复合格式的文件，密码可以直接回车留空。

**2. 接下来执行下面的命令生成节点证书：**

```
bin/elasticsearch-certutil cert --ca elastic-stack-ca.p12
```

`--ca` 参数用于指定 CA 证书文件。

该命令同样生成一个 PKCS#12 格式的单个文件，这个文件中包含了节点证书、节点秘钥、以及 CA 证书。此处同样会提示输入密码保护这个复合文件。

节点证书生成完毕后，你可以把他拷贝到每个 Elasticsearch 的节点上，每个节点使用相同的节点证书。

为了进一步增强安全性，也可以为每个节点生成各自的节点证书，这种情况下，需要在生成证书时指定每个节点的 ip 地址或者主机名称。这可以在 `elasticsearch-certutil cert` 命令添加 `--name`， `--dns` 或 `--ip` 参数来指定。

如果准备为每个节点使用相同的节点证书，也可以将上面的两个步骤合并为一个命令：

```
bin/elasticsearch-certutil cert
```

上述命令会输出单个节点证书文件，其中包含自动生成的 CA 证书，以及节点证书，节点密钥。但不会输出单独的 CA 证书文件。

**3. 将节点证书拷贝到各个节点**

将第二步生成的扩展名为 .p12 节点证书文件（默认文件名 elastic-certificates.p12）拷贝到 Elasticsearch 各个节点。

例如：`$ES_HOME/config/certs/` 后续我们需要在 `elasticsearch.yml` 中配置这个证书文件路径。注意只拷贝节点证书文件就可以，无需拷贝 CA 证书文件，因为 CA 证书的内容已经包含在节点证书文件中。

### 2. 配置节点间通讯加密

节点证书生成完毕后，我们首先在 `elasticsearch.yml` 配置文件中指定证书文件的位置。

**1. 启用 TLS 并指定节点的证书信息。**

根据节点证书的不同格式，需要进行不同的配置，以 PKCS#12 格式的证书为例，配置如下：

```
xpack.security.transport.ssl.enabled: true
xpack.security.transport.ssl.verification_mode: certificate 
xpack.security.transport.ssl.keystore.path: certs/elastic-certificates.p12 
xpack.security.transport.ssl.truststore.path: certs/elastic-certificates.p12 
```

**xpack.security.transport.ssl.verification_mode**

验证所提供的证书是否由受信任的授权机构 (CA) 签名，但不执行任何主机名验证。如果生成节点证书时使用了 --dns 或 --ip 参数，则可以开启主机名或 ip 地址验证，这种情况下需要将本选项配置为 `full`

**xpack.security.transport.ssl.keystore.path** **xpack.security.transport.ssl.truststore.path**

由于我们生成的节点证书中已经包含 CA 证书，因此 keystore 和 truststore 配置为相同值。如果为每个节点配置单独的证书，有可能在每个节点上使用不同的证书文件名，这会有些不方便管理，但是可以使用变量的方式，例如：`certs/${node.name}.p12`

如果生成证书的时候指定为 PEM 格式（在 `bin/elasticsearch-certutil cert`中添加 `-pem` 参数），则对应的配置略有不同：

```
xpack.security.transport.ssl.enabled: true
xpack.security.transport.ssl.verification_mode: certificate 
xpack.security.transport.ssl.key: /home/es/config/node01.key 
xpack.security.transport.ssl.certificate: /home/es/config/node01.crt 
xpack.security.transport.ssl.certificate_authorities: [ "/home/es/config/ca.crt" ] 
```

**xpack.security.transport.ssl.key**

指定节点秘钥文件路径；

**xpack.security.transport.ssl.certificate**

指定节点证书文件路径；

**xpack.security.transport.ssl.certificate_authorities**

指定 CA 证书文件路径；

**2. 配置节点证书密码**

如果生成节点证书的时候设置了密码，还需要在 Elasticsearch keystore 中添加密码配置。

对于 PKCS#12 格式的证书，执行如下命令，以交互的方式配置证书密码：

```
bin/elasticsearch-keystore add xpack.security.transport.ssl.keystore.secure_password

bin/elasticsearch-keystore add xpack.security.transport.ssl.truststore.secure_password
```

对于 PEM 格式的证书，则应使用如下命令：

```
bin/elasticsearch-keystore add xpack.security.transport.ssl.secure_key_passphrase
```

你无需再每个节点上以交互方式配置 Elasticsearch keystore，只需要在一个节点配置完毕后，将 `config/elasticsearch.keystore` 拷贝到各个节点的相同路径下覆盖文件即可。

**3. 对集群执行完全重启**

要启用 TLS，必须对集群执行完全重启，启用 TLS 的节点无法与未启用 TLS 的节点通讯，因此无法通过滚动重启来生效。

### 3. 加密与 HTTP 客户端的通讯

你同样也可以通过 TLS 来加密 HTTP 客户端与 Elasticsearch 节点之间的通讯，让客户端使用 HTTPS，而非 HTTP 来访问 Elasticsearch 节点。

这是可选项，不是必须的。当配置完节点间的通讯加密后，我们可以使用同一个节点证书来进行 HTTP，也就是 9200 端口加密。

**1. 启用 TLS 并指定节点证书信息。**

对于 PKCS#12 格式的节点证书，我们在 `elasticsearch.yml` 中添加如下配置：

```
xpack.security.http.ssl.enabled: true
xpack.security.http.ssl.keystore.path: certs/elastic-certificates.p12 
xpack.security.http.ssl.truststore.path: certs/elastic-certificates.p12 
```

配置项与节点间通讯加密中的类似，在此不再重复解释。类似的，对于 PEM 格式的证书，配置方式如下：

```
xpack.security.http.ssl.enabled: true
xpack.security.http.ssl.key:  /home/es/config/node01.key 
xpack.security.http.ssl.certificate: /home/es/config/node01.crt 
xpack.security.http.ssl.certificate_authorities: [ "/home/es/config/ca.crt" ] 
```

**2. 在 Elasticsearch keystore 中配置证书密码**

与节点间通讯加密配置类似，对于 PKCS#12 格式的证书，执行如下命令，以交互的方式配置证书密码：

```
bin/elasticsearch-keystore add xpack.security.http.ssl.keystore.secure_password

bin/elasticsearch-keystore add xpack.security.http.ssl.truststore.secure_password
```

对于 PEM 格式证书，使用如下命令进行配置：

```
bin/elasticsearch-keystore add xpack.security.http.ssl.secure_key_passphrase
```

**3.重启节点**

修改完 `elasticsearch.yml` 配置文件后，需要重启节点来使他生效。此处可以使用滚动重启的方式，每个节点的 HTTP 加密是单独开启的。

如果只有部分节点配置了 HTTP 加密，那么没有配置 HTTP 加密的节点只能通过 HTTP 协议来访问节点。

当 HTTP 加密生效后，客户端只能通过 HTTPS 协议来访问节点。

### 4. 加密与活动目录或 LDAP 服务器之间的通讯

为了加密 Elasticsearch 节点发送到认证服务器的用户信息，官方建议开启 Elasticsearch 到活动目录（AD）或 LDAP 之间的通讯。

通过 TLS/SSL 连接可以对 AD 或 LDAP 的身份进行认证，防止中间人攻击，并对传输的数据进行加密。因此这是一种服务端认证，确保 AD 或 LDAP 是合法的服务器。

这需要在 Elasticsearch 节点中配置 AD 或 LDAP 服务器证书或服务器 CA 根证书。同时将 ldap 改为 ldaps。例如：

```
xpack:
  security:
    authc:
      realms:
        ldap1:
          type: ldap
          order: 0
          url: "ldaps://ldap.example.com:636"
          ssl:
            certificate_authorities: [ "ES_PATH_CONF/cacert.pem" ]
```

### 总结

本章重点叙述了开启节点间通讯加密的具体操作方式，由于这是开启 X-Pack 安全特性的必须项，因此需要读者理解节点证书与 CA 证书，大家可以按自己的安全性需求为节点配置不同证书或者相同证书，HTTP 加密虽然不是必须，但是官方极力推荐开启。

到本节为止，安全这部分主题介绍完毕，本课程的最后一部分与大家分享一些我们在多年运维过程中的常见问题，给读者一些借鉴和参考，这些问题都是比较通用的。

### 参考

[1] [Installing X-Pack in Elasticsearch](https://www.elastic.co/guide/en/elasticsearch/reference/6.2/installing-xpack-es.html)

[2] [Setting Up TLS on a Cluster] (https://www.elastic.co/guide/en/x-pack/6.2/ssl-tls.html)

[3] [Encrypting communications in Elasticsearch] (https://www.elastic.co/guide/en/elasticsearch/reference/6.5/configuring-tls.html#tls-transport)

[4][Security settings in Elasticsearch](https://www.elastic.co/guide/en/elasticsearch/reference/6.5/security-settings.html#ssl-tls-settings)

## 19.常见问题之 - 集群 RED 与 YELLOW

当你已经熟悉系统原理，在集群遇到故障的时候仍然需要花一些时间进行定位，在我们处理过的问题中，有很多是重复性的问题，我们将这些问题进行了汇总，目的是让运维同学可以根据错误类型找到现有的解决方案，快速解决问题。

因此本文分享在我们运维过程中遇到的一些比较通用的问题，希望可以给读者借鉴和参考。

### 1. 原理与诊断

集群 RED 和 YELLOW 是 Elasticsearch 集群最常见的问题之一，无论 RED 还是 YELLOW，原因只有一个：有部分分片没有分配。

如果有一个以上的主分片没有被分配，集群以及相关索引被标记为 RED 状态，如果所有主分片都已成功分配，有部分副分片没有被分配，集群以及相关索引被标记为 YELLOW 状态。

对于集群 RED 或 YELLOW 的问题诊断推荐使用 Cluster Allocation Explain API，该 API 可以给出造成分片未分配的具体原因。例如，如下请求可以返回第一个未分配的分片的具体原因：

```
GET /_cluster/allocation/explain
```

也可以只查看特定分片未分配的原因：

```
GET /_cluster/allocation/explain
{
  "index": "myindex",
  "shard": 0,
  "primary": true
}
```

引用一个官网的例子，API 的返回信息如下：

```
{
  "index" : "idx",
  "shard" : 0,
  "primary" : true,
  "current_state" : "unassigned",                 
  "unassigned_info" : {
    "reason" : "INDEX_CREATED",                   
    "at" : "2017-01-04T18:08:16.600Z",
    "last_allocation_status" : "no"
  },
  "can_allocate" : "no",                          
  "allocate_explanation" : "cannot allocate because allocation is not permitted to any of the nodes",
  "node_allocation_decisions" : [
    {
      "node_id" : "8qt2rY-pT6KNZB3-hGfLnw",
      "node_name" : "node-0",
      "transport_address" : "127.0.0.1:9401",
      "node_attributes" : {},
      "node_decision" : "no",                     
      "weight_ranking" : 1,
      "deciders" : [
        {
          "decider" : "filter",                   
          "decision" : "NO",
          "explanation" : "node does not match index setting [index.routing.allocation.include] filters [_name:\"non_existent_node\"]"  
        }
      ]
    }
  ]
}
```

在返回结果中给出了导致分片未分配的详细信息，`reason` 给出了分片最初未分配的原因，可以理解成 unassigned 是什么操作触发的；`allocate_explanation`则进一步的说明，该分片无法被分配到任何节点，而无法分配的具体原因在 deciders 的 `explanation` 信息中详细描述。这些信息足够我们诊断问题。

**分片没有被分配的最初原因有下列类型：**

**1. INDEX_CREATED**

由于 create index api 创建索引导致，索引创建过程中，把索引的全部分片分配完毕需要一个过程，在全部分片分配完毕之前，该索引会处于短暂的 RED 或 YELLOW 状态。因此监控系统如果发现集群 RED，不一定代表出现了故障。

**2. CLUSTER_RECOVERED**

集群完全重启时，所有分片都被标记为未分配状态，因此在集群完全重启时的启动阶段，reason属于此种类型。

**3. INDEX_REOPENED**

open 一个之前 close 的索引， reopen 操作会将索引分配重新分配。

**4. DANGLING_INDEX_IMPORTED**

正在导入一个 dangling index，什么是 dangling index？

磁盘中存在，而集群状态中不存在的索引称为 dangling index，例如从别的集群拷贝了一个索引的数据目录到当前集群，Elasticsearch 会将这个索引加载到集群中，因此会涉及到为 dangling index 分配分片的过程。

**5. NEW_INDEX_RESTORED**

从快照恢复到一个新索引。

**6. EXISTING_INDEX_RESTORED**,

从快照恢复到一个关闭状态的索引。

**7. REPLICA_ADDED**

增加分片副本。

**8. ALLOCATION_FAILED**

由于分配失败导致。

**9. NODE_LEFT**

由于节点离线。

**10. REROUTE_CANCELLED**

由于显式的cancel reroute命令。

**11. REINITIALIZED**

由于分片从 started 状态转换到 initializing 状态。

**12. REALLOCATED_REPLICA**

由于迁移分片副本。

**13. PRIMARY_FAILED**

初始化副分片时，主分片失效。

**14. FORCED_EMPTY_PRIMARY**

强制分配一个空的主分片。

**15. MANUAL_ALLOCATION**

手工强制分配分片。

### 2. 解决方式

对于不同原因导致的未分配要采取对应的处理措施，因此需要具体问题具体分析。需要注意的是每个索引也有 GREEN，YELLOW，RED 状态，只有全部索引都 GREEN 时集群才 GREEN，只要有一个索引 RED 或 YELLOW，集群就会处于 RED 或 YELLOW。如果是一些测试索引导致的 RED，你直接简单地删除这个索引。

因此单个的未分配分片就会导致集群 RED 或 YELLOW，一些常见的未分配原因如下：

- 由于配置问题导致的，需要修正相应的配置
- 由于节点离线导致的，需要重启离线的节点
- 由于分片规则限制的，例如 total*shards*per_node，或磁盘剩余空间限制等，需要调整相应的规则
- 分配主分片时，由于找不到最新的分片数据，导致主分片未分配，这种要观察是否有节点离线，极端情况下只能手工分片陈旧的分片为主分片，这会导致丢失一些新入库的数据。

集群 RED 或 YELLOW 时，一般我们首先需要看一下是否有节点离线，对于节点无法启动或无法加入集群的问题我们单独讨论。下面我们分享一些 RED 与 YELLOW 的案例及相应的处理方式。

### 3. 案例分析

#### 【案例A】

**1. 故障诊断**

首先大致看一下分片未分配原因：

```
curl -sXGET "localhost:9200/_cat/shards?h=index,shard,prirep,state,unassigned.*&pretty"|grep UNASSIGNED
```

结果显示分片大都是因为 node_left 导致未分配，然后通过 explain API 查看分片 myindex[3]不自动分配的具体原因：

```
curl -sXGET localhost:9200/_cluster/allocation/explain?pretty -d '{"index":"myindex","shard":3,"primary":true}' 
```

我们在 explain api 中指定了只显示 分片 myindex[3] 的信息，诊断结果的主要信息如下：

```
 "can_allocate" : "no_valid_shard_copy",
  "allocate_explanation" : "cannot allocate because all found copies of the shard are either stale or corrupt",
```

意味着 Elasticsearch 找到了这个分片在磁盘的数据，但是由于分片数据不是最新的，无法将其分配为主分片。

从多个副本中选择哪个分片作为主分片的策略在 2.x 及 6.x 中有较大变化，具体可以参阅《Elasticsearch 源码解析与优化实战》

**2. 解决方式**

如果有离线的节点，启动离线的节点可能会将该分片分配下去，在我们的例子中，所有节点都在线，且分片分配过程执行完成，原来拥有最新数据的主分片无法成功分配，例如坏盘的原因，可以将主分片分配到一个 stale 的节点上。这会导致丢失一些最新写入的数据。

首先记录一下 stale 分片所在的节点，这个信息也在 explain api 的返回信息中：

```
"node_allocation_decisions" : [
    {
      "node_id" : "xxxxxx",
      "node_name" : "node2",
      "transport_address" : "127.0.0.1:9301",
      "node_decision" : "no",
      "store" : {
        "in_sync" : false,
        "allocation_id" : "HNeGpt5aS3W9it3a7tJusg"
      }
    }
  ]
```

分片所在节点为 node2，接下来将该 stale 分片分配为主分片：

```
curl -sXPOST "http://localhost:9200/_cluster/reroute?pretty" -d '
{
    "commands" : [ {
        "allocate_stale_primary" : {
            "index" : "myindex",
            "shard" :3,
            "node" : "node2",
            "accept_data_loss" : true
        }
    }]
}'
```

#### 【案例 B】

**1. 故障诊断**

分片分配失败，查看日志有如下报错：

```
failed recovery, failure RecoveryFailedException[[log4a-20181026][2]: Recovery failed from {NSTDAT12.1}{fQM-UDjPSHu6-IMwuKg0nw}{7LNwMfeIT8uhPW8AUEPs-w}{NSTDAT12}{x.x.212.61:9301} into {NSTDAT12.0}{N8ubqgIxSvezL652baPT3w}{5uQzcvuwTOeV01_hnwipxQ}{NSTDAT12}{x.x.212.61:9300}]; nested: RemoteTransportException[[NSTDAT12.1][x.x.212.61:9301][internal:index/shard/recovery/start_recovery]]; nested: RecoveryEngineException[Phase[1] phase1 failed]; nested: RecoverFilesRecoveryException[Failed to transfer [0] files with total size of [0b]]; nested: IllegalStateException[try to recover [log4a-20181026][2] from primary shard with sync id but number of docs differ: 1483343 (NSTDAT12.1, primary) vs 1483167(NSTDAT12.0)]; 
```

产生该错误的原因是副分片与主分片 `sync_id` 相同，但是 doc 数量不一样，导致 recovery 失败。造成 `sync_id` 相同，但 doc 数量不同的原因可能有多种，例如下面的情况：

1. 写入过程使用自动生成 docid
2. 主分片写 doc 完成，转发请求到副分片
3. 在此期间，并行的一条 delete by query 删除了主分片上刚刚写完的 doc，同时副分片也执行了这个删除请求
4. 主分片转发的索引请求到达副分片，由于是自动生成 id 的，副分片将直接写入该 doc，不做检查。最终导致副分片与主分片 doc 数量不一致。

导致此类问题的一些 case 已经在 6.3.0 版本中修复，具体可以参考[此处](https://discuss.elastic.co/t/try-to-recover-test-20181128-2-from-primary-shard-with-sync-id-but-number-of-docs-differ-59432-10-1-1-189-primary-vs-60034-10-1-1-190/158983)

**2. 解决方式**

当出现线上出现这种故障时，解决这个问题也比较容易，先将分片副本数修改为 0

```
curl -sXPUT "http://localhost:9200/log4a-20181026/_settings?pretty&master_timeout=3m"  -d '{"index.number_of_replicas":0}}'
```

再将副本数恢复成原来的值，我们这里是 1：

```
curl -sXPUT "http://localhost:9200/log4a-20181026/_settings?pretty&master_timeout=3m"  -d '{"index.number_of_replicas":1}}'
```

这样重新分配副分片，副分片从主分片拉取数据进行恢复。

### 总结

集群 RED 与 YELLOW 是运维过程中最常见的问题，除了集群故障，正常的创建索引，增加副分片数量等操作都会导致集群 RED 或 YELLOW，在这种情况下，短暂的 RED与 YELLOW 属于正常现象，如果你监控集群颜色，需要考虑到这一点，可以参考持续时间，Explain API的具体原因等因素制定报警规则。

集群颜色问题是最常见，也是最简单的问题，在我们处理过的其他问题中，大部分都是内存问题，下一节我们介绍下这部分内容。

### 参考

[https://www.elastic.co/guide/en/elasticsearch/reference/master/cluster-allocation-explain.html]()

## 20.常见问题之 - 内存问题

Elasticsearch 高效运行依赖于内存的合理分配，包括堆内内存和堆外内存。无论堆内内存不足、还是堆外内存不足，都会影响运行效率，甚至是节点的稳定性。

在安装 Elasticsearch 前进行内存分配时，我们一般把可用内存的一半分配给 JVM，剩余一半留给操作系统缓存索引文件。本课主要关注由于 JVM 内存问题引起的长时间 GC，从而导致的节点响应缓慢问题。

### 1. 原理与诊断

#### 1.1 原理

当集群出现响应缓慢时，在排除掉硬件资源不足的因素后，接下来就要重点分析节点的 GC 情况了。ES 内部占用内存较多的数据结构主要包括如下几个部分：

- **Lucene segments**

  Elasticsearch 底层依赖于 Lucene 全文索引库，为了提供快速的检索服务，需要把 Lucene 的特定数据结构加载到内存，集群内的数据量越大，需要加载到内存的信息越多。

- **Query Cache**

  Elasticsearch 集群中的每个节点包含一个 Node Query Cache，由该节点的所有 shard 共享。该 Cache 采用 LRU 算法，Node Query Cache 只缓存 filter 的查询结果。默认大小为堆内存的 10%。

- **Fielddata**

  Elasticsearch 从 2.0 开始，默认在非 text 字段开启 `doc_values`，基于 `doc_values` 做排序和聚合，可以极大降低节点的内存消耗，减少节点 OOM 的概率，性能上损失却不多。

  5.0开始，text 字段默认关闭了 Fielddata 功能，由于 text 字段是经过分词的，在其上进行排序和聚合通常得不到预期的结果。所以我们建议 Fielddata Cache 应当只用于 global ordinals。Fielddata 占用内存大小由 `indices.fielddata.cache.size` 控制，默认不限制大小。

- **Index Buffer**

  在索引过程中，新索引的数据首先会被放入缓存，并在合适的时机将缓存的数据刷入磁盘，这个缓存就是 Index Buffer。默认值为堆内存的 10%。

- **查询**

在执行查询过程中，尤其聚合查询，中间数据存储也会占用很大的内存空间。

- **其他**

除了上面个提到的几个部分外，transport、集群状态，索引和分片管理等也会占用一部分内存。

#### 1.2 诊断

**查看堆内内存状态**

执行下面的命令可以查看各个节点的内存状态：

```
curl -sXGET "http://localhost:9200/_cat/nodes?v"
```

该命令后输出如下(本文中命令的输出会隐去节点 ip，name 等信息）

```
 heap.percent ram.percent cpu load_1m load_5m load_15m node.role master
           36          94   7    2.65    2.79     3.06 di        -     
           57          99  15   11.37    6.58     4.75 di        -     
           40          96   6    1.58    2.12     2.39 mdi       -     
           38         100   7    2.35    2.38     2.32 di        -     
           57          95   8    2.89    3.29     3.76 mdi       -     
           60          95   7    2.98    3.51     4.14 di        -     
           60          99   7    2.18    2.60     2.65 di        -     
           52          95   7    2.98    3.51     4.14 mdi       -     
           63          99   7    2.18    2.60     2.65 mdi       -     
           48          94   7    2.65    2.79     3.06 mdi       -     
           50          95   8    2.89    3.29     3.76 di        -     
           53          98   7    2.80    2.63     2.67 mdi       -     
           59          96   6    1.58    2.12     2.39 di        -     
           60          98   7    2.80    2.63     2.67 di        -     
           55          96   8    4.74    3.53     3.29 mdi       -     
           49          97  12    3.79    4.00     3.31 di        -     
           47          96   8    4.74    3.53     3.29 di        -     
           71          99  15   11.37    6.58     4.75 mdi       -     
           51         100   7    2.35    2.38     2.32 mdi       *     
           56          97  12    3.79    4.00     3.31 mdi       -     
```

**查看 GC 状态**

```
$JAVA_HOME/bin/jstat -gc $pid
```

执行该命令后输出如下：

```
 S0C    S1C    S0U    S1U      EC       EU        OC         OU       MC     MU    CCSC   CCSU   YGC     YGCT    FGC    FGCT     GCT
1083456.0 1083456.0  0.0   391740.9 8668032.0 7715466.1 21670912.0 15789191.1 86684.0 80325.8 10908.0 9375.5 290445 24067.090 3920  14579.724 38646.814
1083456.0 1083456.0  0.0   391740.9 8668032.0 8000968.4 21670912.0 15789191.1 86684.0 80325.8 10908.0 9375.5 290445 24067.090 3920  14579.724 38646.814
1083456.0 1083456.0  0.0   391740.9 8668032.0 8230172.2 21670912.0 15789191.1 86684.0 80325.8 10908.0 9375.5 290445 24067.090 3920  14579.724 38646.814
1083456.0 1083456.0 268584.8 391740.9 8668032.0 8667696.3 21670912.0 15789440.7 86684.0 80325.8 10908.0 9375.5 290446 24067.090 3920  14579.724 38646.814
1083456.0 1083456.0 412489.4  0.0   8668032.0 1049588.2 21670912.0 15790123.0 86684.0 80325.8 10908.0 9375.5 290446 24067.147 3920  14579.724 38646.872
1083456.0 1083456.0 412489.4  0.0   8668032.0 2169211.9 21670912.0 15790123.0 86684.0 80325.8 10908.0 9375.5 290446 24067.147 3920  14579.724 38646.872
1083456.0 1083456.0 412489.4  0.0   8668032.0 3077474.4 21670912.0 15790123.0 86684.0 80325.8 10908.0 9375.5 290446 24067.147 3920  14579.724 38646.872
1083456.0 1083456.0 412489.4  0.0   8668032.0 4109532.1 21670912.0 15790123.0 86684.0 80325.8 10908.0 9375.5 290446 24067.147 3920  14579.724 38646.872
1083456.0 1083456.0 412489.4  0.0   8668032.0 5050874.2 21670912.0 15790123.0 86684.0 80325.8 10908.0 9375.5 290446 24067.147 3920  14579.724 38646.872
1083456.0 1083456.0 412489.4  0.0   8668032.0 5497027.7 21670912.0 15790123.0 86684.0 80325.8 10908.0 9375.5 290446 24067.147 3920  14579.724 38646.872
1083456.0 1083456.0 412489.4  0.0   8668032.0 5850000.6 21670912.0 15790123.0 86684.0 80325.8 10908.0 9375.5 290446 24067.147 3920  14579.724 38646.872
```

**Elasticsearch 内部内存使用状况**

```
curl -sXGET "http://localhost:9200/_cat/nodes?h=name,port,segments.memory,segments.index_writer_memory,fielddata.memory_size,query_cache.memory_size,request_cache.memory_size&v"
```

执行该命令后输出如下：

```
port segments.memory segments.index_writer_memory fielddata.memory_size query_cache.memory_size request_cache.memory_size
9301           2.1gb                      112.9mb               193.1mb                 362.2mb                    52.7mb
9301           2.7gb                        162mb               191.7mb                 372.8mb                      48mb
9300           2.4gb                        182mb                 191mb                 350.7mb                    35.7mb
9301           2.4gb                      165.6mb                 2.9mb                 264.6mb                    72.9mb
9300           3.2gb                      329.7mb               192.7mb                 402.4mb                    40.6mb
9300           2.6gb                      116.3mb                   4mb                 334.5mb                    36.8mb
9300           2.3gb                      164.8mb                 2.7mb                 210.2mb                    64.4mb
9300             3gb                      152.7mb                 3.4mb                 369.9mb                    37.3mb
9301             3gb                      153.7mb                 4.3mb                   364mb                    44.6mb
9300           2.3gb                        151mb                 3.3mb                 300.4mb                    40.1mb
9301           2.1gb                      113.6mb               190.9mb                 379.7mb                    30.6mb
9300           2.3gb                      176.9mb                 192mb                 329.2mb                    40.5mb
9301           2.8gb                      136.9mb                 3.1mb                   341mb                    27.2mb
9301           3.1gb                      137.5mb                 193mb                 370.6mb                    42.1mb
9301           4.1gb                      165.4mb                 4.2mb                 356.1mb                    52.8mb
9300           3.2gb                      140.4mb               194.8mb                   566mb                      28mb
9301           3.2gb                      153.2mb                 4.1mb                 363.9mb                    55.8mb
9300           2.4gb                      147.8mb               191.3mb                   371mb                    45.3mb
9300           2.5gb                        150mb                 3.9mb                 414.2mb                      44mb
9301           2.8gb                      140.3mb               194.6mb                 552.7mb                    48.4mb
```

### 2. 案例分析

下面提供我们遇到的一些关于 GC 问题导致集群响应缓慢的案例。大部分都是通过导出堆，加载到 MAT 中进行分析定位的。希望遇到相似问题的同学不用再导出堆重复分析。

#### 2.1 案例 A

**分段过多导致 GC**

- **现象：**节点响应缓慢，也没有大量的入库和查询操作。通过查看节点 GC 状态，发现节点在持续进行 FullGC。
- **分析：**通过 REST 接口查看 Elasticsearch 进程内存使用状况，发现 `segments.memory` 占用了很大的空间。
- **解决方案：**对索引进行 forcemerge 操作，将 segment 合并成一个段。随着 merge 的进行，进程堆内内存逐步降下来。
- **总结：**对于不再写入和更新的索引，尽量通过 forcemerge api 将其 merge 为一个单一的段。 如果在把段 merge 完后，`segments.memory` 仍然占用很大的空间，则需要考虑扩容来解决。

#### 2.2 案例 B

**fieldata 导致 gc[1]**

- **现象：**节点响应缓慢，也没有大量的入库和查询操作。通过查看节点 gc 状态，发现节点在持续进行 FullGC。
- **分析：**查看 Elasticsearch 进程内存使用状况，发现 `fielddata.memory_size` 占用了很大的空间。业务中对 keyword 类型字段进行排序或者聚合，如果 shard 包含多个段， Elasticsearch 需要构建 global_ordinals 数据结构，会占用比较大的内存。对于 merge 成一个段的shard，则不需要构建。
- **解决方案：**对索引进行 forcemerge 操作，将 segment 合并成一个段。随着 merge 的进行，进程堆内内存逐步降下来。
- **总结：**对于不再写入和更新的索引，尽量通过 forcemerge api 将其 merge 为一个段。通过【案例A】和【案例 B】我们可以明确，段要及时合并，可以减少节点内存压力，提高节点稳定性。同时在搜索过程中，可以减少磁盘随机 IO。

**fieldata 导致 gc[2]**

- **现象：**节点响应缓慢，也没有大量的入库和查询操作。通过查看节点 GC 状态，发现节点在持续进行 FullGC。
- **分析：**查看 Elasticsearch 进程内存使用状况，发现 `fielddata.memory_size` 占用了很大的空间。同时，数据不写入和更新的索引，segment 都已经做过 merge。这种情况，一般是 `indices.fielddata.cache.size` 参数没有做限制导致的。
- **解决方案：**将 `indices.fielddata.cache.size` 设置降低，重启集群，节点堆内内存恢复正常。
- **总结：**由于 Fielddata cache 构建是一个比较重的操作，如果在 `indices.fielddata.cache.size` 设置的范围内，Elasticsearch 并不会主动释放，所以需要把该值设置的保守一些。如果业务确实需要比较大的值，则需要增加节点内存，或者添加节点进行水平扩容来解决。

#### 2.3 案例 C

**bulk queue 过大 gc**

- **现象：**节点响应缓慢，此时系统 cpu 利用率处于 80% 以上，通过查看节点 GC 状态，发现节点在持续进行 FullGC 。
- **分析：**查看 Elasticsearch 进程内存使用状况，并没有发现占用内存不正常的情况。登陆服务器，通过如下命令，查看 `thread_pool` 使用情况：

```
curl -sXGET "http://localhost:9200/_cat/thread_pool?v"
```

发现全部入库线程都处于 Active 状态，且出现了大量的拒绝操作。之后导出堆进行分析，发现堆内有大量的 IndexRequest，如下图所示：

```
![](media/15569515713986/15569531048045.jpg)
```

由此可以确定 FullGC 的原因是入库资源不足且 bulk queue 过大导致的。

- **解决方案：**降低 bulk queue 大小，从500降低到官方的默认值 50。
- **总结：**bulk queue 不建议设置的特别大，如果设置的特别大，且每次bulk的数据条数很多，一旦出现资源不够导致数据进入 bulk queue，说明系统资源已经利用很充分，大量数据滞留在队列内，很可能导致节点频繁 FullGC，引起节点响应慢，甚至离线。

#### 2.4 案例 D

**嵌套聚合导致 GC**

- **现象：**节点响应缓慢，通过查看节点 GC 状态，发现节点在持续进行 FullGC。
- **分析：**查看 Elasticsearch 进程内存使用状况，发现各个内存项都比较正常。导出堆分析，发现内存中有大量的 bucket 对象。初步推测是聚合导致了 GC 问题。通过搜索 Elasticsearch 日志，发现每次执行完一个查询语句后，就开始有大量的 GC 日志打印出来。发现那是在一个 10 亿+ 的索引上执行了4层聚合。
- **解决方案：**业务方去掉了这个功能，以其他的方式实现。
- **总结：**对于在大数据集上进行的嵌套聚合，需要很大的堆内内存来完成。如果业务场景确实无法以其他方式实现，也只能增加更多的硬件，分配更多的堆内内存给 Elasticsearch 进程。

#### 2.5 案例 F

**更新导致 GC**

- **现象：**节点响应缓慢，也没有大量的入库和查询操作。通过查看节点 GC 状态，发现节点在持续进行FullGC。
- **分析：**查看 Elasticsearch 进程内存使用状况，并没有发现内存占用异常的情况。不写入的索引的 segment 也进行了 merge。此种情况，除了分析堆，没有更好的办法了。经过分析，发现有大量的 PerThreadIDAndVersionLookup 占用大量的内存，如下图所示： ![img](media/15569515713986/15569516098070.jpg) 而且该对象都跟几个正在写入和更新的索引有关。通过查看 Lucene 代码，发现如果在索引过程中是采用自定义 id 而非自动生成 id，每个入库线程对每个 segment 会持有一个 PerThreadIDAndVersionLookup 对象。查看这几个索引的 segment 数量，都在2万+以上。
- **解决方案：**由于索引持续有写入和更新，定时对这类型的索引进行适当的 merge，不强制merge成一个段，以免对业务产生比较大的影响。之后观察几天，节点 GC 情况正常。
- **总结：**对于索引数据持续发生变化，且 ID 是业务自定义的索引，要定期将其段的数量 merge 到一个比较小数量，以免发生 FullGC 的问题。

#### 2.6 案例 G

**堆外内存不足导致 GC**

- **现象：**节点响应缓慢，也没有大量的入库和查询操作。通过查看节点 GC 状态，发现节点在持续进行 FullGC，但是与前面案例不同的是，在发生 FullGC 时，Java 进程的 old 区使用不足 50%。
- **分析：**由于old区还有很多剩余空间，则应该不是堆内内存问题。查看 Elasticsearch 进程内存使用状况，果然一切正常。随即怀疑是堆外内存导致。查看进程的启动参数，发现有 `-XX:MaxDirectMemorySize=2048m`，由于 Elasticsearch 底层采用 netty 作为通信框架， netty 为了提高效率，很多地方采用了堆外内存，很可能是该参数配置过小导致频繁的 GC。
- **解决方案：**去掉 `-XX:MaxDirectMemorySize=2048m` 启动参数，重启进程，GC 恢复正常。
- **总结：**不要在线上系统随意添加优化参数，需要经过充分的测试验证。

### 总结

本文介绍了 Elasticsearch 内部的内存分布，以及我们在使用过程中因为 GC 问题引起的节点相应缓慢问题的案例分析方法和解决方案。

节点 GC 情况对节点稳定性和请求延迟密切相关，保证 GC 时长在一个合理的时间范围至关重要。

下一节我们介绍下一些其他比较通用的问题。

## 21.常见问题之 - 其他

### 1. 节点启动问题

节点启动问题主要是启动失败，可能的原因非常多，下面整理的是一些常见故障，大部分问题的详细信息可以在日志中找到。

#### 1.1 案例 A

- **故障现象**

集群恢复到 90% 多后，节点频繁重启。查看节点日志，发现报错如下：

```
exit due to io error
```

Elasticsearch 进程主动退出。同时日志中有大量的 `too many open files` 报错。

- **故障分析**

通过 nodes stats API 可以查看节点打开的 fd 数量：

```
curl -sXGET "localhost:9200/_nodes/stats/process?filter_path=**.max_file_descriptors,**.open_file_descriptors&pretty"

{
  "nodes" : {
    "es.76.0" : {
      "process" : {
        "open_file_descriptors" : 65536,
        "max_file_descriptors" : 65536
      }
    },
    ...
}
```

该 API 返回每个节点的 fd 情况，其中 `open_file_descriptors` 代表进程当前打开的 fd 数量，`max_file_descriptors` 为最大可以打开的 fd 数量。该结果显示，节点打开的 fd 确实已达到 65536，怀疑索引 segment未 进行合并，查看节点是上 segment 的数量：

```
curl -sXGET "localhost:9200/_nodes/stats/indices/segments?filter_path=**.count&pretty"
```

发现非常多，大量的段文件是由于没有对冷索引执行 force merge 导致。

- **解决方式**

该业务索引数据为日期轮转型，先关闭一批早期的索引，让节点正常启动，待集群 Green 后，对索引执行 force merge 操作，降低段文件数量。

#### 1.2 案例 B

- **故障现象**

节点启动失败，查看日志输出有报错如下：

```
Caused by: java.nio.file.FileAlreadyExistsException: /data01/es/nodes/0/indices/K1KZ1kgpRTCczI5rm03Q1g/1/.es_temp_file
```

- **故障诊断**

Elasticsearch 节点启动过程中会尝试在分片目录下创建临时文件，如果文件已经存在就会启动失败，导致这个问题的原因可能是 Elasticsearch 进程在启动阶段被强制杀掉。

- **解决方式**

Elasticsearch v5.6.0 以上的版本已经解决了这个问题，当低版本中出现时可以直接删除 `.es_temp_file` 这个临时文件。

#### 1.3 案例 C

- **故障现象**

集群完全重启时，当全部节点加入集群后，master 节点开始频繁 fullgc，通过 jstat 命令观察 master 节点内存的 old 区持续增长，知道全部占满，节点停止响应

- **故障诊断**

由于故障可以重现，因此重启 master 节点，当全部节点加入后，old 区开始快速增长期间，查看 master 节点的 `hot_threads` ：

```
curl -X GET "localhost:9200/_nodes/master_node_name/hot_threads"
```

`hot_threads` 结果显示 master 节点正在处理数据节点发送过来的集群状态，也就是说 master 当前处于 gateway 阶段，该阶段中，master 节点主动向每个具备 master 资格的节点索取集群状态，然后选举版本号最高的最为最终集群状态。

- **解决方式**

猜测因为集群状态过大，master 索取各个节点的集群状态后 JVM 内存无法容纳。原集群没有做角色分离，每个节点都可以被选为主节点，因此调整集群结构，仅让 3 个节点具备 master 资格，重启集群后，集群正常启动。

#### 1.4 案例 D

- **故障现象**

节点启动失败，查看日志文件，发现是由于 OOM 异常导致：

```
unable to create new native thread
```

- **故障诊断**

执行 `ulimit -u` 命令查看 es 用户的 max user processes，发现值为：1024，按照官网的意见，该值至少应该被设置为 2048.

- **解决方式**

修改 `/etc/security/limits.conf` 文件，将 nproc 值修改为 8192

```
* - nproc 8192
```

#### 1.5 案例 E

- **故障现象**

节点启动失败，查看日志文件，存在如下错误信息：

```
failed to open a socket, too many open files
```

- **故障诊断**

与案例 A 类似，先通过 nodes stats API 查看fd 数量，发现 `max_file_descriptors` 值为 1024，Elasticsearch 官方建议将 fd 的限制调整为 65536

- **解决方式**

修改 `/etc/security/limits.conf` 文件，将 nofile 值修改为 65536

```
* - nofile 65536
```

#### 1.6 案例 F

- **故障现象**

节点启动失败，查看日志文件，存在如下错误信息：

```
java.io.IOException: Input/output error
```

- **故障诊断**

这日志一般意味着存在坏盘，坏盘有几种情况，一种是整块盘无法读写，还有一种是该盘的部分文件无法读写。存在坏盘的情况下，Elasticsearch 会中止启动过程，需要将坏盘的路径从配置文件的 `path.data` 中排除出去。

- **解决方式**

从配置文件的 `path.data` 中删除坏盘的路径，重启节点。Elasticsearch 会自动补齐部分不足的数据。原来存在于坏盘上的主分片会被重新分配，分片数据不足的副分片会自动补齐。

但是假如仅存的一个主分片在坏盘上面，可以尝试将坏盘上的分片 数据拷贝出来，在命令行调用 Lucene 的 CheckIndex 尝试修复索引数据，修复完成后再拷贝到节点的数据目录下，Elasticsearch 自动将他加载。

### 2. Recovery 问题

Elasticsearch 的 Recovery 发生在集群完全重启，以及 reopen 一个索引，增加副本等时机，执行 Recovery 的目的是：

- 对于主分片来说，需要从事务日志恢复没有来得及刷盘的数据。
- 对于副分片来说，需要恢复成和主分片一致。

在不同的大版本中，副分片执行 Recovery 的机制存在较大差异。

#### 2.1 Recovery 慢

为了不影响正常读写数据，索引恢复期间是有限速的，有时候我们希望集群尽快恢复，例如在集群完全重启阶段，一般会先停止入库操作。可以把默认的限速阈值增大。这些设置都可以动态调节，即时生效。

节点之间拷贝数据时的限速，默认为 40Mb，我们这里的网络环境为万兆，调整为 100Mb

```
indices.recovery.max_bytes_per_sec
```

单个节点上执行副分片 Recovery 时的最大并发数量，默认值为 2，我们设置为 100

```
cluster.routing.allocation.node_concurrent_recoveries
```

单个节点上执行主分片 Recovery 时的最大并发数量，默认值为 4，我们设置为 100

```
cluster.routing.allocation.node_initial_primaries_recoveries
```

### 3. 系统 load 很高

- **故障现象**

敲任何命令，操作系统响应都很慢，top 命令查看系统 load 很高，CPU 指标中的 sys 占用很大

- **故障分析**

通过 `sar -B 1` 分析内存页面置换效率，发现 %vmeff 异常，该值正常情况下应该为 0 或接近 100 ，当该值异常低的时候代表页面置换效率存在问题，一般产生这种情况的原因与 NUMA 有关。关于该问题的更多分析可以参考[这篇文章](https://elasticsearch.cn/article/348)

- **解决方式**

查看内核参数的 `vm.zone_reclaim_mode` 值，发现该值为1：

```
sysctl -A |grep vm.zone_reclaim_mode
```

该值在 CentOS7 系统下默认为 0，CentOS6 中默认为 1，将该值修改为 0，观察一段时间后，问题解决。编辑 `/etc/sysctl.conf`，添加 `vm.zone_reclaim_mode = 0`，然后执行 `sysctl -p` 参数立即生效，无需重启系统。对内核参数的临时性调整可以使用 `sysctl -w`，设置不会持久化，系统重启后会恢复原来的设置。

### 4. 请求响应很慢

- **故障现象**

查询请求，以及 `_cat/thread_pool` 等请求长时间阻塞，平时可以秒内返回的查询请求都长时间没反应

- **故障诊断**

产生故障之前该集群的数据总量比较高，JVM 内存使用率在 70% 以上，怀疑是节点 GC 停顿导致，由于此时 REST 接口的请求无法返回，因此通过

```
jstack -gcutil es_pid
```

命令查看 Elasticsearch 各个节点的 GC 情况，发现有一个节点 JVM 利用率已经 100%，导致查询请求的协调节点再等待此节点返回数据，对于部分 _cat API 请求来说，主节点需要向各个节点抓取数据，因此一个 GC 异常的节点导致整个集群的响应都很缓慢

- **解决方式**

由于数据总量越大，JVM 常驻内存占用约高，根本解决方式需要对集群扩容，或者关闭、删除一些索引，临时解决方式就是观察故障节点的 GC 是否正常进行，可以重启节点。

### 5. 入库慢

导致写入速度慢的原因很多，我们有专门的一篇讨论入库速度的优化建议，现在我们分享一些运维过程中的实际案例。

#### 5.1 案例 A

- **故障分析**

观察监控系统的 CPU 指标，发现只有个别节点 CPU 很高，查看热索引的分片在各个节点上的分布情况：

```
curl -sXGET ‘http://localhost:9200/_cat/shards/$index/'
```

发现大部分分片分到了两个节点上，没有均匀地分配的整个集群，进一步发现没有限制索引分片在节点的数量。

- **解决方式**

为了保证新建索引的分片在集群中均匀分配，我们调整索引的 `total_shards_per_node` 参数如下：

```
curl -XPUT 'http://localhost:9200/$indices/_settings?pretty' -d '{"index.routing.allocation.total_shards_per_node":2}' 
```

将 `total_shards_per_node` 设置为 2，意味着索引在单个节点上最多只能否分片 2 个。我们的集群有10 个节点，索引主分片数为 5，副本数量为 1，如果均匀分布的话，每个节点应该有 (5×2)/10=1 个分片，考虑到节点离线等异常情况，将该值设置为 2，读者需要根据自己的实际情况进行调整。

#### 5.2 案例 B

- **故障分析**

通过 `_cat/shards/$index` 发现分片在各个节点分布均匀，然后通过 `_nodes/hot_threads` 接口查看热点线程，发现有很多 merge 操作在 merge 前一天的索引。

我们会在每天凌晨开始 force merge 前一天索引，热点线程说明到了上午仍然没有 merge 完毕，ssh 到相关节点执行 `iostat -xd 1` 查看 utils，发现个别磁盘长期处于 100，通过 `_cat/indices/index` 查看前一天索引大小，发现索引大于 2TB，除以分片个数，单个分片大小为 200G 以上，而 force merge 的 `max_num_segments=1`，这种合并操作消耗的 CPU 和 IO 太高，导致和入库资源竞争。

- **解决方式**

控制 force merge 后的分片数量，单个分段大小最好不要超过5G，我们将 `max_num_segments` 修改为 200/5 = 40，观察一段时间后问题得到缓解。

分段数量较多会对查询性能有负面影响，不过为了降低 merge 的影响，也可以将分段大小限制为 2G 或更小一点，这样 `max_num_segments` 可以设置为 100 左右。

#### 5.3 案例 C

- **故障分析**

通过 `_nodes/hot_threads` 接口查看热点线程，发现很多 merge 操作，但并非 force merge 触发，而是正常的入库引起的。

分段合并是非常消耗 IO 和 CPU 的操作，对入库速度影响非常明显，默认情况下，入库引起的 merge 操作会将分段合并到 5G，超过5G 的不再合并。

- **解决方式**

由于业务需要快速入库，避免数据堆积，因此调整 merge 策略，将目标最大分段大小由 5GB 修改为 500MB：

```
curl -sXPUT "http://localhost:9200/*2019.01.31/_settings" -d '{"index.merge.policy.max_merged_segment":"500mb"}' 
```

并将 `segments_per_tier` 修改为 24

```
curl -sXPUT "http://localhost:9200/*2019.01.31/_settings" -d '{"index.merge.policy.segments_per_tier":"24"}'
```

在分层的合并策略中，`segments_per_tier` 代表每层分段的数量，值越小则最终 segment 越少，因此需要 merge 的操作更多，默认为 10

这样修改后分片的最终分段较多，可以在业务低峰期的时候通过 force merge 来进一步合并。

### 总结

到本节为止，本部分内容到此结束，在运维 Elasticsearch 集群的过程中，大部分问题的起因是比较简单，并且容易解决的，少数情况下需要分析内存，甚至阅读源码。

希望我们分享的这些案例可以给读者带来一些借鉴，并有所启发。

## 22.用好 Elasticsearch 的几点建议

由于我去年出版的《Elasticsearch 源码解析与优化实战》比较偏重原理，所以本套课程决定偏重应用和实践，主要讨论实际场景中应该如何正确使用 Elasticsearch，避免产生不要必要的问题，以及当线上集群出现故障时，如何分析和解决问题。

读者可以参考课程中的故障处理案例来解决遇到的类似问题，但是一定要和结合实际情况，避免照搬套用，只要你清楚问题的本质，结合实际场景，就能找到适合自己的解决方法。

本课程并未涉及到写入速度优化，查询速度优化等方面的内容，因为关于“优化”方面的话题在《Elasticsearch 源码解析与优化实战》中已经系统的讨论过，因此不再写重复的内容，读者有这方面的兴趣可以参考此书。

其实只要使用方法正确，Elasticsearch 很少会出问题，中小型公司 hold 住自己的集群完全没有问题。

Elastic 毕竟是一家商业公司，产品的质量控制比 Apache 的开源软件要好的多，重大 bug 很少，用户调研做的也很细致，对于用户在社区反馈痛点问题也一定会尽快解决，这也是 Elasticsearch 版本更新如此之快的原因。关于如何“正确地”使用 Elasticsearch，我们归纳为以下几个重点：

**1. 合理设置 mapping**

最基础的，你要了解分词和不分词的区别，他们可能会在你的使用过程中造成完全不同的系统压力，例如我们曾经帮客户处理的一个性能问题中，客户需求是从 url 中根据域名能够检索到内容，由于不了解分词与不分词的区别，对 url 字段使用了 keyword 类型，查询时使用通配符查询： **www.url.cn** 的形式，这种查询由于需要遍历所有词，造成单个查询就把磁盘 IO 负载跑满，查询延迟巨大。

而这种查询本来用 text 类型加 match 查询就完全满足。类似这种低级的问题一定要避免，如果你不清楚某些技术点，就要把它搞懂，不要实验了一种方式满足了业务需求，完全不关心背后的代价。

就像这个例子中，业务使用通配符查询确实也能查到预期结果，以为这样就完成了自己的工作目标，结果是给后续工作挖了坑。

又例如数字类型，我们说对于没有范围查询需求的数字类型应该设置为 keyword，因为 Lucene 内部会把所有的数字类型都转为字符串进行处理，对于范围查找这种功能，Lucene 内部将数字字符串分解为多个，范围查询本质上是一个 term 查询，由于数字被分词为多个字符串，因此数字类型产生更多的倒排索引，最终占用更多的 JVM 常驻内存。

**2. 了解搜索的代价**

搜索最容易产生问题的地方在于深度搜索和深度翻页。在我们处理过的各种问题中，很多业务确实不了解深层聚合会给集群带来什么样的压力，曾经有一个业务的集群总是在某个特定的时间点有节点离线的情况，业务抱怨集群不稳定，经过检查后发现业务的聚合请求的 bucket 为 1000000 * 1000000，这就很容易造成节点 OOM，又或者业务进行3层或4层的深度聚合，一样会造成协调节点内存暴涨。

7.x 之前的版本的断路器是限制不住内存使用量的，只能分离出单独的查询节点来解决问题，7.x 的版本中断路器可以更精确的控制内存使用。有些业务说深层聚合避免不了，必须要用，只能给协调节点配置上百 GB 的内存。

**3. 控制好 JVM 内存**

绝大部分问题的根源是内存问题引起的，夸张一点说：只要你控制住 JVM 内存使用率在合理的范围，没有产生严重的 GC，你就可以拥有一个稳定的 Elasticsearch 集群。

造成 JVM 占用量很高的的因素很多，最大的因素是节点所持有的数据量，在不使用冻结索引的情况下，一个 open 状态的索引其 FST 结构要加载到内存中，这些是无法 GC 掉的，当节点的数据总量达到一定规模，你什么都不做，节点的 JVM 占用率就已经很高，一般我们建议单个节点持有的数据量不超过 5TB。

因此如果单个节点需要持有的数据量很多，就必须使用冻结索引。Lucene 8 宣称词典将由堆外内存的方式加载（off-heap），实际效果尚需验证。值得期待。

**4. 避免数据热点**

要避免热点数据集中在少数几节点上，从而导致性能瓶颈，这通过 `index.total_shards_per_node` 来进行控制，他限制某个索引在单个节点上的分片数来让分片在节点间更均匀一些。如果集群已经产生热点问题，可以通过 reroute 来手工移动分片。

但是如果热点数据集中在某个磁盘，会比较难处理一些，如果不改动核心代码，只能 reroute 或尝试使用 raid 来解决，目前 Elasticsearch 对于多数据盘的管理确实还有待改进，相信后续的版本会解决这个问题。

上面我们总结了“正确地”使用 Elasticsearch 的要点，这是常见问题中占比重最多的问题，除此之外还会有些其他的细节，例如一个 bulk 请求达到 1GB，单条 doc 上百 M 同样导致节点 OOM，这些都是使用上的问题，类似的细节问题我们已经在课程中详细的讨论过。

对于任何一个大数据平台，要想用好它，都要了解他的技术特点，例如 hbase 的 rowkey 设计要避免热点，合理地控制 split 和 compation 等，只有足够的了解，才能避免不必要的问题。

## 23.附录：Elasticsearch 7 的重大更新及新特性解析

Elasticsearch 7版本发布已经有一段时间，按照官方的说法，他是迄今为止最快、最安全、最有弹性、最容易使用的 Elasticsearch 版本，下面我们看一下这个版本的重大变更及新特性，并分享我们的一些测试结论。

### 基于 Lucene 8.0

Elasticsearch 7 基于 Lucene 8.0.0，在索引的兼容性上，他可以直接加载 Elasticsearch 6.0以上的版本创建的索引，Elasticsearch 5.x 创建的索引需要 reindex 到 Elasticsearch 7.x

#### TOP-K 优化

Lucene 8.0.0做了大量的新能优化，主要亮点是 TOP-K 的查询优化。在之前的版本中，查询会计算所有命中的文档，但是用户经常查询 'a' , 'the' 等词汇，这种词汇不会增加多少文档得分，但迫使查询过程为大量的文档进行打分。

因此，如果检索结果只需要返回 TOP-K 的结果，而非范围准确的命中数量，可以对此进行优化，Lucene 8 中引入了 WAND 算法来实现此特性。当检索结果小于指定的结果总数时，该优化不会生效。

在停止计算命中文档总数之后，查询 QPS 得到大幅提升，以下结果来自 [lucene 官方基准测试](http://people.apache.org/~mikemccand/lucenebench/)

Bool AND 查询，提升 2.3 倍左右。 ![enter image description here](https://images.gitbook.cn/0db3ce90-b9a9-11e9-ad84-d52f8a9d7052)

Bool OR 查询，提升 2.5 倍左右。 ![enter image description here](https://images.gitbook.cn/1e73fc00-b9a9-11e9-ad84-d52f8a9d7052)

Term 查询，提升 40 倍左右。 ![enter image description here](https://images.gitbook.cn/2d448e70-b9a9-11e9-b261-53935f63522b)

在 Elasticsearch 7中，要在查询中返回 TOP-K 的结果，通过 track*total*hits 参数来指定，默认值为10000，根据自己的需要设置返回前 K 个命中结果，或者设置为 true，返回全部命中结果数量。例如：

```
curl -X GET "localhost:9200/twitter/_search" -H 'Content-Type: application/json' -d'
{
    "track_total_hits": 100,
     "query": {
        "match" : {
            "message" : "Elasticsearch"
        }
     }
}
'
```

计算 TOP-K 的过程中需要评估文档的最大得分，这需要在索引过程中写入一些额外的信息。Lucene 将词典划分一个个的 block，并构建了一个跳跃表，在查询的时候跳过不匹配的文档，现在，索引过程中会为每个块中最高影响(impacts）的摘要添加到该跳表中，可以计算出该块可能产生的最大得分，如果该得分不具有竞争力，则可以跳过它。更多信息可以阅读[此处](https://www.elastic.co/blog/faster-retrieval-of-top-hits-in-elasticsearch-with-block-max-wand)

#### 词典支持 off-heap 方式加载

Lucene8 现在支持以 mmap 的方式加载词典索引，倒排表和 Docvalues，节约常驻 JVM 内存，由于使用 mmap 方式使用对外内存，可能会被置换到磁盘上，因此可能引发从磁盘读取，所以查找词典会稍微慢一些，不过查找一个 term 只是查询过程的一小部分，这影响不大。

但是对查找主键（docid）影响很大， 这在更新索引时会用到。更多信息可以参考[此处](https://issues.apache.org/jira/browse/LUCENE-8635)

除此TOP-K 优化之外，Lucene8 对 DocValues 的随机访问性能也进行了优化，以及更快的自定义评分。关于 Lucene8的新特性和优化项的完整描述参阅[此处](http://lucene.apache.org/core/8_0_0/changes/Changes.html#v8.0.0.optimizations)

我们使用 7.1.1 与 6.8 版本进行对比测试，在默认配置下，并没有发现7.x版本常驻JVM内存有明显的降低。

> 测试过程中向 Elasticsearch 写入5TB 索引，然后通过 REST 接口查看 segment 内存占用9.3GB。平均1 TB索引占用约2GB内存（内存占用和具体的业务数据相关）。

我们尝试修改 lucene 文件的加载方式，让tip（FST 保存在 tip 文件）也采用mmap方式加载，将索引的store类型设置成mmapfs，对比内存变化，Lucene segments占用内存大概有15%的降低。

### 增强弹性和稳定性

#### 新的集群协调子系统

Elasticsearch 6.x 及之前的版本使用名为 Zen Discovery 的集群协调子系统，这个系统已经比较成熟，但是存在一些缺点。Zen Discovery 通过用户配置 discovery.zen.minimum*master*nodes 来明确指定多少个符合主节点条件的节点可以形成法定数量，但是在集群扩容时，用户可能会忘记调整。其次， Zen Discovery的选主过程也有些慢。

Elasticsearch 7重新设计了集群协调子系统，移除了minimum*master*nodes设置，由集群自己选择可以形成法定数量的节点。并且新的子系统可以在很短时间内完成选主。

要使用新的协调子系统，需要以下步骤（从6.x升级除外）：

**1. 配置集群引导**

如果使用默认配置启动节点，他会自动查找本机的其他节点，并形成集群。如果在生产环境，要启动一个全新的集群时，必须指定集群初次选举中用到的具有主节点资格的节点，称为集群引导，这只在第一次形成集群时需要，例如下面的配置：

```
cluster.initial_master_nodes:
  - master-a
  - master-b
  - master-c
```

注意：

1. initial*master*nodes配置只在集群首次启动时使用，后续将忽略此配置
2. 不具备主节点资格的节点，以及新节点加入现有集群，无需配置 initial*master*nodes
3. 各个节点配置的initial*master*nodes值应该相同

一定要小心的配置 initial*master*nodes，否则集群可能脑裂。

**2. 配置节点发现**

节点发现需要配置一些种子节点，这个概念和原来配置的 discovery.zen.ping.unicast.hosts 类似。例如：

```
discovery.seed_hosts:
   - 192.168.1.10:9300
   - 192.168.1.11 
   - seeds.mydomain.com 
```

#### 真实的内存断路器

Elasticsearch 6.x 及之前版本的circuit breaker估算内存用量时与实际内存占用误差很大，因此circuit breaker实际上很难发挥作用。Elasticsearch 7版本使用 jvmMemoryPoolMXBean提供的getUsage()获取内存使用情况，实现了比较准确的circuit breaker，可以通过indices.breaker.total.use*real*memory配置是否启用，默认为 true

如果说 ES 比较大的缺点，节点稳定性是其中之一，线上经常因为各种请求导致节点挂掉，例如深度聚合、以及巨大的 bulk 等，但是我们希望任何请求到达节点时可以失败，但是不能引起节点或集群异常。因此为了维护节点稳定性，Elasticsearch 6.x 之前需要自己去控制或过滤业务发起的请求，新版本的circuit breaker是一个很重要的更新。经过初步测试，新版本的circuit breaker确实可以准确地根据内存用量去拒绝客户端请求，不过由于节点经常产生大量临时对象，JVM 占用情况可能会在短期内飙升，并在稍后恢复，因此需要为断路器设置较高一些的阈值。

#### 限制 aggregation bucket数量

Elasticsearch 6.x以前的版本中，对聚合结果bucket的数量是不限制的，这容易在深度聚合时产生 OOM。为了保护节点稳定性，7.x开始添加配置项search.max_buckets，默认值为10000。

#### 限制每个节点打开的分片数量

Elasticsearch 6.x 及之前的版本中，每个节点可以拥有的分片数量没有上线。新版本中，添加cluster.max*shards*per_node设置，控制节点的分片总数，默认值为1000。

close 状态索引持有的分片不会被计算在内，但是unassigned状态的分片会被计算进去。判断分片数量是否超过阈值时是以集群当前总分片数与max*shards*per_node*节点数来比较：

```
int maxShardsInCluster = maxShardsPerNode * nodeCount;
if ((currentOpenShards + newShards) > maxShardsInCluster) {
}
```

### 性能优化

#### 默认启用 ARS

自适应副本选择（ARS）在6.x 中实现，但默认关闭，现在他已默认开启。未启用 ARS 的情况下，查询请求在分片的多个副本之间轮询执行，但是可能某个节点的负载较大，ARS 会选择负载较小的节点来转发请求。这类似负载均衡器中智能路由的概念。

配置项：cluster.routing.use*adaptive*replica_selection，默认true

#### 搜索空闲时跳过 refresh

以前的 refresh 策略为定期 refresh，默认1秒。过多的 refresh 会产生过多 lucene 分段，导致后期 merge 产生较大压力。在新的refresh策略中，某个 shard 在30秒内没有查询请求时，被标记为search idle，跳过周期的 refresh。当一个搜索请求到来时，会先对search idle的分片执行 refresh，保证数据对搜索的可见性。

如果设置了 refresh_interval，则此新策略不生效，将按照原来的周期性 refresh 执行。

配置项：index.search.idle.after，默认值 30s

#### 默认 1 个分片

Elasticsearch 遇到比较多的问题是集群分片太多，由于很多场景下索引是按周期生成的，默认的5个分片有些多，现在调整为1个。

#### 跨集群搜索（CCS）降低了请求往返次数

CCS 增加了 ccs*minimize*roundtrips 模式，该模式降低了不必要的请求，降低了搜索延迟。

#### index.store.type 增加 hybridfs 类型

index.store.type 的默认类型为 fs，他会依据环境自动选择存储类型，目前所有受支持系统上的操作环境都是 hybridfs，但也有可能不同。Elasticsearch 6.x 及之前的版本会选择 mmap 类型。

hybridfs 类型是 niofs 和 mmapfs 的混合实现，它根据读取访问模式为每种类型的文件选择最佳的类型。目前，只有 term dictionary, norms 和 doc values 文件使用 mmap 方式。所有其他文件都使用 Lucene NIOFSDirectory 打开。与 mmapfs 类似，hybridfs 类型需要确保您配置了正确的 vm.max*map*count

### 使用更简便

#### 内置 JAVA

由于一些小白用户不知道 Elasticsearch 是一个 java 程序，新版本会将 JDK 一起打包。如果你设置了JAVA_HOME 环境变量，则使用你指定的 JDK。Elasticsearch 7打包的 JDK 为 OpenJDK 12.0.1

#### 索引生命周期管理( ILM) 做为正式功能

在日志等场景中，索引会按周期创建，写入，和删除，一般情况下使用 Elasticsearch 的团队会写一些脚本由 crontab 驱动来完成周期性的操作。Elasticsearch 从 6.6 开始内置了这些功能（beta），将索引的生命周期划分为：Hot、Warm、Cold、Delete。ILM 很大程度上降低了 Elasticsearch 的使用门槛，这个功能从 Elasticsearch 7开始称为正式特性。

#### SQL 转正

Elasticsearch 从 6.3 开始加入 SQL 接口，该特性在 7.x 成为正式功能。

#### 日志输出支持 json 格式

没啥好说的

#### java high-level REST client 已具备完整的 API

官方一直建议将 TransportClient 更换为 REST Client，现在，high-level REST client已具备完整的 API，使用 TransportClient 的客户端现在可以开始迁移。

### 功能变化

- 移除了 type 的概念。从6.x 开始为移除 type 已经只支持一个 type，现在正式移除。
- 集群名称和索引名称不允许包括冒号 :
- `_all` `_uid` 两个元字段被删除
- mappings 中的 `_default_` 被删除
- 限制了 term query 中 terms 的最大值为 65536
- `max_concurrent_shard_request` 语义更改，由原来的限制单个搜索请求并发分片请求的总数，变成每个结点最大并发分片请求数； 也就是说之前单个查询涉及n个分片，同时并行请求的数量不会超过该设置，不关心有多少节点参与到这一批查询；修改后，会将查询平摊到所有节点，每个节点查询的分片数不超过该值。
- fielddata circuit breaker 默认配置由 60% 降低到 30%
- 移除了 tribe node（部落节点），使用跨集群查询代替。
- `index.unassigned.node_left.delayed_timeout` 不能设置成负数
- 为了防止OOM，单个文档内嵌套 json 对象的数量被限制为10000个。可以通过索引设置`index.mapping.nested_objects.limit` 更改此默认限制。
- 节点名默认为hostname, 之前为node.id的前8个字符
- 去掉index thread pool, 单个文档 index 也使用 bulk thread pool，同时删除了`thread_pool.index.size`、 `thread_pool.index.queue_size` 两个配置
- query_string 查询不再支持 `use_dismax`, `split_on_whitespace`, `all_fields` and `lowercase_expanded_terms`
- 分片优先级去掉了 `_primary`, `_primary_first`, `_replica`, and `_replica_first`
- 查询响应 hits.total 改成对象

### 索引性能的差异

在入库性能方面（写入速度），基于 Elasticsearch 7.1.1 进行写入压力测试，验证与 Elasticsearch 6.8版本的差异，通过3轮测试对比，在同样的环境下，Elasticsearch 7.1.1 入库性能降低约20%，这可能是受 TOP-K优化的影响，他需要在写入过程中进行更多的计算。

### 参考资料

[Elasticsearch 7.0.0 released](https://www.elastic.co/cn/blog/elasticsearch-7-0-0-released)

[Breaking changes in 7.0](https://www.elastic.co/guide/en/elasticsearch/reference/current/breaking-changes-7.1.html)

[Elasticsearch 7.0.0 Beta 1 Released](https://www.elastic.co/cn/blog/elasticsearch-7-0-0-beta1-released)

[What's new in Lucene 8](https://www.elastic.co/cn/blog/whats-new-in-lucene-8)

[Elasticsearch 集群协调迎来新时代](https://www.elastic.co/cn/blog/a-new-era-for-cluster-coordination-in-elasticsearch)

[EFFICIENT TOP-K QUERY PROCESSING IN LUCENE 8](http://mocobeta.github.io/slides-html/search-tech-talk-1/search-tech-talk-1.html)

[Lucene 8的Top-k查询处理优化简介](https://medium.com/@mocobeta/lucene-8-%E3%81%AE-top-k-%E3%82%AF%E3%82%A8%E3%83%AA%E3%83%97%E3%83%AD%E3%82%BB%E3%83%83%E3%82%B7%E3%83%B3%E3%82%B0%E6%9C%80%E9%81%A9%E5%8C%96-1-%E5%B0%8E%E5%85%A5%E7%B7%A8-5a6387076e8e)