# Spring Boot日常使用碎片整理


### 1. 创建Spring Boot项目

```xml
<parent>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-parent</artifactId>
	<version>2.1.2.RELEASE</version>
</parent>
```

或者

```xml
<dependencyManagement>
	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-dependencies</artifactId>
			<version>2.1.2.RELEASE</version>
			<type>pom</type>
			<scope>import</scope>
		</dependency>
	</dependencies>
</dependencyManagement>
```

### 2. 创建可执行的jar

```xml
<build>
	<plugins>
		<plugin>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-maven-plugin</artifactId>
		</plugin>
	</plugins>
</build>
```

### 3. @SpringBootApplication

`@SpringBootApplication`等价于同时标注 `@Configuration` `@EnableAutoConfiguration` `@ComponentScan`。

当然也可以使用`@Import({ WebMvcConfig.class, RedisConfig.class })`等方式来引入配置

### 4. 执行可执行jar

```bash
java -jar target/app-1.0.jar
```

开启远程debug

```bash
java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=n -jar target/app-1.0.jar
```

### 5. 热部署

```xml
<dependencies>
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-devtools</artifactId>
		<optional>true</optional>
	</dependency>
</dependencies>
```

### 6. 自定义Banner

在`classpath`目录下添加`banner.txt`文件即可，或者`banner.gif`、`banner.jpg`、`banner.png`

相关配置参数：

```
spring.banner.location

spring.banner.charset

spring.banner.image.location
```

使用如下配置可禁用banner

```
spring.main.banner-mode="off"
```

或者

```java
SpringApplication app = new SpringApplication(Application.class);
app.setBannerMode(Banner.Mode.OFF);
```

### 7. 程序启动时执行代码

实现`ApplicationRunner` or `CommandLineRunner`接口，重写`run`方法，只会在程序启动时执行一次，用于做一些初始化工作。

```java
import org.springframework.boot.*;
import org.springframework.stereotype.*;

@Component
public class InitBean implements CommandLineRunner {

	public void run(String... args) {
		// Do something...
	}

}
```

### 8. 外部化配置

```java
import org.springframework.stereotype.*;
import org.springframework.beans.factory.annotation.*;

@Component
public class MyBean {

    @Value("${name}")
    private String name;

    // ...

}
```

除了直接在`application.properties`文件中赋值外还可在运行时采用如下的方式为变量`nama`赋值

```bash
java -jar app.jar --name="Spring"

SPRING_APPLICATION_JSON='{"acme":{"name":"test"}}' java -jar myapp.jar

java -Dspring.application.json='{"name":"test"}' -jar myapp.jar

java -jar myapp.jar --spring.application.json='{"name":"test"}'
```

### 9. 配置随机数

```
my.secret=${random.value}
my.number=${random.int}
my.bignumber=${random.long}
my.uuid=${random.uuid}
my.number.less.than.ten=${random.int(10)}
my.number.in.range=${random.int[1024,65536]}
```

详见`RandomValuePropertySource` 

### 10. 读取命令行配置参数

默认情况下`SpringApplication`应用程序将以`--`开头的参数会转化为环境变量如`--server.port=8000`

如果想忽略命令行配置参数可在代码中进行关闭

```java
SpringApplication.setAddCommandLineProperties(false)
```

### 11. 应用配置文件加载

默认情况下`SpringApplication`从如下位置加载`application.properties`

* 当前目录同级的`/config`子目录下

* 当前目录

* `classpath`同级的`/config`子目录下

* `classpath`目录下

  

加载顺序优先级依次为：

* `file:./config/`
* `file:./`
* `classpath:/config/`
* `classpath:/`

可以使用`spring.config.name`和`spring.config.location`两个配置参数修改配置文件信息

```
spring.config.name 指定配置文件名称
spring.config.location 指定配置文件加载位置
```

如下所示：

```bash
java -jar app.jar --spring.config.name=application-ha

java -jar app.jar --spring.config.location=file:./config/application-ha.yml,classpath:application-ha.yml
```

### 12. 命令行运行时相关的环境变量参数

* 指定配置文件名

```bash
--spring.config.name=
```

* 指定配置文件位置

```bash
--spring.config.location=classpath:application-ha.yml
```

* 指定激活的profiles

```bash
--spring.profiles.active=
或者命令行参数 -Dspring.profiles.active=
```

* 指定启动端口

```bash
--server.port=
```

* 以debug模式启动

```bash
--debug
```

* 随机端口

```bash
server.port=0
server.port=-1
```

### 13. 配置文件占位符替换

在`application.properties`文件中可以引用之前的配置

```
app.name=MyApp
app.description=${app.name} is a Spring Boot application
```

### 14. YAML替换Properties文件

```yaml
environments:
	dev:
		url: http://dev.example.com
		name: Developer Setup
	prod:
		url: http://another.example.com
		name: My Cool App
```

等价于

```properties
environments.dev.url=http://dev.example.com
environments.dev.name=Developer Setup
environments.prod.url=http://another.example.com
environments.prod.name=My Cool App
```

```yaml
my:
servers:
	- dev.example.com
	- another.example.com
```

等价于

```properties
my.servers[0]=dev.example.com
my.servers[1]=another.example.com
```

对应的java代码为

```java
@ConfigurationProperties(prefix="my")
public class Config {

	private List<String> servers = new ArrayList<String>();

	public List<String> getServers() {
		return this.servers;
	}
}
```

核心相关类`YamlPropertySourceLoader`

### 15. Multi-profile YAML Documents

```yaml
server:
	address: 192.168.1.100
---
spring:
	profiles: development
server:
	address: 127.0.0.1
---
spring:
	profiles: production & eu-central
server:
	address: 192.168.1.120
```

如上所示可以在一个`YAML`文档中使用`spring.profiles`指定多份配置。

如若激活了`development` 那么`server.address` 属性为 `127.0.0.1`

如若激活了`production` **和** `eu-central`那么`server.address` 属性为 `192.168.1.120`

如若`development`, `production` 和 `eu-central` 都没有激活那么`server.address` 属性为 `192.168.1.100`

**profile表达式**

* &  and

* !    not

* |   or

  

```yaml
server:
  port: 8000
---
spring:
  profiles: default
  security:
    user:
      password: weak
```

等价于

```yaml
server:
  port: 8000
spring:
  security:
    user:
      password: weak
```

### 16. @ConfigurationProperties Validation

```java
@ConfigurationProperties(prefix="acme")
@Validated
public class AcmeProperties {

	@NotNull
	private InetAddress remoteAddress;

	@Valid
	private final Security security = new Security();

	// ... getters and setters

	public static class Security {

		@NotEmpty
		public String username;

		// ... getters and setters

	}
}
```

### 17. @ConfigurationProperties VS @Value

* @ConfigurationProperties 主要用于POJO，不支持SpEL表达式
* @Value 主要用于类似环境变量的配置，支持SpEL表达式

### 18. Profiles

```java
@Configuration
@Profile("production")
public class ProductionConfiguration {

}
```

`@Profile`注解主要用在`@Component`或`@Configuration`

可以在`application.properties`文件中使用`spring.profiles.active`激活

```bash
spring.profiles.active=dev,hsqldb
```

或者命令行`--spring.profiles.active=dev,hsqldb`

Adding Active Profiles

```yaml
---
my.property: fromyamlfile
---
spring.profiles: prod
spring.profiles.include:
  - proddb
  - prodmq
```

如若使用`--spring.profiles.active=prod`那么`proddb` 和 `prodmq`也将被激活

代码参考 `SpringApplication.setAdditionalProfiles(…)`

### 19. 日志框架

Log Level: `ERROR`, `WARN`, `INFO`, `DEBUG`, or `TRACE`

Logback日志框架没有`FATAL`对应的为`ERROR`

控制台日志输出

```bash
java -jar app.jar --debug
```

同样可以在``application.properties`中设置`debug=true`

默认将输出`ERROR`-level, `WARN`-level, and `INFO`-level 级别的日志。

修改日志级别为trace具体做法为控制台直接设置`--trace`或者在`application.properties`中设置`trace=true`

#### 19.1 文件输出

```
logging.file
logging.path
logging.file.max-size
```

默认输出`ERROR`-level, `WARN`-level, and `INFO`-level 级别日志

#### 19.2 日志级别

在`application.properties`文件中可以使用`logging.level.<logger-name>=<level>`方式设置日志级别。

可设置的级别为`TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`, `FATAL`, or `OFF`

`root`级别的配置为`logging.level.root`

```properties
logging.level.root=WARN
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate=ERROR
```

#### 19.3 日志组

```properties
logging.group.tomcat=org.apache.catalina, org.apache.coyote, org.apache.tomcat
logging.level.tomcat=TRACE
```

#### 19.4 日志配置文件

| Logging System          | Customization                                                |
| ----------------------- | ------------------------------------------------------------ |
| Logback                 | `logback-spring.xml`, `logback-spring.groovy`, `logback.xml`, or `logback.groovy` |
| Log4j2                  | `log4j2-spring.xml` or `log4j2.xml`                          |
| JDK (Java Util Logging) | `logging.properties`                                         |

#### 19.5 Profile-specific

```xml
<springProfile name="staging">
	<!-- configuration to be enabled when the "staging" profile is active -->
</springProfile>

<springProfile name="dev | staging">
	<!-- configuration to be enabled when the "dev" or "staging" profiles are active -->
</springProfile>

<springProfile name="!production">
	<!-- configuration to be enabled when the "production" profile is not active -->
</springProfile>
```

### 20. 静态内容

默认情况下返回`classpath`目录下`/static` (or `/public` or `/resources` or `/META-INF/resources`) 

```properties
spring.mvc.static-path-pattern
spring.resources.static-locations
spring.resources.chain.strategy.content.enabled=true
spring.resources.chain.strategy.content.paths=/**
```

### 21. 异常处理

```java
@ControllerAdvice(basePackageClasses = AcmeController.class)
public class AcmeControllerAdvice extends ResponseEntityExceptionHandler {

	@ExceptionHandler(YourException.class)
	@ResponseBody
	ResponseEntity<?> handleControllerException(HttpServletRequest request, Throwable ex) {
		HttpStatus status = getStatus(request);
		return new ResponseEntity<>(new CustomErrorType(status.value(), ex.getMessage()), status);
	}

	private HttpStatus getStatus(HttpServletRequest request) {
		Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
		if (statusCode == null) {
			return HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return HttpStatus.valueOf(statusCode);
	}

}
```

相关类`BasicErrorController`和`ErrorController`

自定义错误页面

404

```
src/
 +- main/
     +- java/
     |   + <source code>
     +- resources/
         +- public/
             +- error/
             |   +- 404.html
             +- <other public assets>
```

5xx

```
src/
 +- main/
     +- java/
     |   + <source code>
     +- resources/
         +- templates/
             +- error/
             |   +- 5xx.ftl
             +- <other templates>
```

实现``ErrorViewResolver` `

```java
public class MyErrorViewResolver implements ErrorViewResolver {

	@Override
	public ModelAndView resolveErrorView(HttpServletRequest request,
			HttpStatus status, Map<String, Object> model) {
		// Use the request or status to optionally return a ModelAndView
		return ...
	}

}
```

更多参考`@ExceptionHandler`和`@ControllerAdvice`、`ErrorController`

### 22. 内嵌容器

#### 22.1 Servlet Context Initialization

核心类

|                Servlet 3.0                |                            Spring                            |
| :---------------------------------------: | :----------------------------------------------------------: |
| javax.servlet.ServletContainerInitializer |      org.springframework.web.WebApplicationInitializer       |
|                                           | org.springframework.boot.web.servlet.ServletContextInitializer |
|                                           |      org.springframework.web.WebApplicationInitializer       |
|                                           | org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext |
|                                           |    org.springframework.web.context.WebApplicationContext     |

* `WebApplicationInitializer` 
* `ServletWebServerApplicationContext`
* ``ServletContextInitializer` `

#### 22.2 自定义Servlet容器

application.properties

```properties
server.port
server.address
server.error.path
server.servlet.session.persistence
server.servlet.session.timeout
server.servlet.session.store-dir
server.servlet.session.cookie.*
```

代码方式

```java
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.stereotype.Component;

@Component
public class CustomizationBean implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

	@Override
	public void customize(ConfigurableServletWebServerFactory server) {
		server.setPort(9000);
	}

}
```





























压缩

# 开启压缩
server.compression.enabled=true

text/html
text/xml
text/plain
text/css
text/javascript
application/javascript
application/json
application/xml

# 默认为2048bytes
server.compression.min-response-size=2048

配置容器访问日志

server.tomcat.basedir=my-tomcat
server.tomcat.accesslog.enabled=true
server.tomcat.accesslog.pattern=%t %a "%r" %s (%D ms)

server.undertow.accesslog.enabled=true
server.undertow.accesslog.pattern=%t %a "%r" %s (%D ms)
# 默认为logs目录
server.undertow.accesslog.directory=logs

server.jetty.accesslog.enabled=true
server.jetty.accesslog.filename=/var/log/jetty-access.log


创建一个可部署的war包

```java
@SpringBootApplication
public class Application extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
```

同时在pom.xml中声明<packaging>war</packaging>

```xml
<dependencies>
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-tomcat</artifactId>
		<scope>provided</scope>
	</dependency>
</dependencies>
```


linux系统服务的方式启动

```bash
sudo ln -s /opt/app.jar /etc/init.d/app
service app start
```