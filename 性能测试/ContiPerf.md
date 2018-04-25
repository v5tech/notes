# ContiPerf 结合 Junit 性能测试

http://databene.org/contiperf

一个简单的性能测试例子

```xml
<dependencies>  
    <dependency>  
        <groupId>junit</groupId>  
        <artifactId>junit</artifactId>  
        <version>4.7</version>  
        <scope>test</scope>  
    </dependency>   
    <dependency>  
        <groupId>org.databene</groupId>  
        <artifactId>contiperf</artifactId>  
        <version>2.1.0</version>  
        <scope>test</scope>  
    </dependency>  
</dependencies>  
```


```java
package net.ameizi;

import org.databene.contiperf.PerfTest;
import org.databene.contiperf.Required;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.Rule;
import org.junit.Test;

public class ContiperfTest {

    @Rule
    public ContiPerfRule i = new ContiPerfRule();

    @Test
    @PerfTest(invocations = 1000, threads = 40)
    @Required(max = 1200, average = 250, totalTime = 60000)
    public void test(){
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
```

控制台输出

```
net.ameizi.ContiperfTest.test
samples: 1000
max:     205
average: 199.456
median:  199
```

主要参数介绍

* PerfTest参数

@PerfTest(invocations = 300)：执行300次，和线程数量无关，默认值为1，表示执行1次；
@PerfTest(threads=30)：并发执行30个线程，默认值为1个线程；
@PerfTest(duration = 20000)：重复地执行测试至少执行20s。

* Required参数

@Required(throughput = 20)：要求每秒至少执行20个测试；
@Required(average = 50)：要求平均执行时间不超过50ms；
@Required(median = 45)：要求所有执行的50%不超过45ms； 
@Required(max = 2000)：要求没有测试超过2s；
@Required(totalTime = 5000)：要求总的执行时间不超过5s；
@Required(percentile90 = 3000)：要求90%的测试不超过3s；
@Required(percentile95 = 5000)：要求95%的测试不超过5s； 
@Required(percentile99 = 10000)：要求99%的测试不超过10s; 
@Required(percentiles = "66:200,96:500")：要求66%的测试不超过200ms，96%的测试不超过500ms。

测试结果展示

测试报告在`target/contiperf-report/index.html`目录下
