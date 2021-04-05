# Graylog

docker-compose.yml

```yml
version: '2'
services:
  # MongoDB: https://hub.docker.com/_/mongo/
  mongodb:
    image: mongo:3
    restart: always
    volumes:
      - mongo_data:/data/db
  # Elasticsearch: https://www.elastic.co/guide/en/elasticsearch/reference/6.x/docker.html
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch-oss:6.8.2
    restart: always
    volumes:
      - es_data:/usr/share/elasticsearch/data
    environment:
      - http.host=0.0.0.0
      - transport.host=localhost
      - network.host=0.0.0.0
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    ulimits:
      memlock:
        soft: -1
        hard: -1
    mem_limit: 1g
  # Graylog: https://hub.docker.com/r/graylog/graylog/
  graylog:
    image: graylog/graylog:3.1
    restart: always
    volumes:
      - graylog_journal:/usr/share/graylog/data/journal
    environment:
      # CHANGE ME (must be at least 16 characters)!
      - GRAYLOG_PASSWORD_SECRET=somepasswordpepper
      # Password: admin
      - GRAYLOG_ROOT_PASSWORD_SHA2=8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918
      - GRAYLOG_HTTP_EXTERNAL_URI=http://192.168.223.14:9000/
      - GRAYLOG_ROOT_TIMEZONE=Asia/Shanghai
    links:
      - mongodb:mongo
      - elasticsearch
    depends_on:
      - mongodb
      - elasticsearch
    ports:
      # Graylog web interface and REST API
      - 9000:9000
      # Syslog TCP
      - 1514:1514
      # Syslog UDP
      - 1514:1514/udp
      # GELF TCP
      - 12201:12201
      # GELF UDP
      - 12201:12201/udp
# Volumes for persisting data, see https://docs.docker.com/engine/admin/volumes/volumes/
volumes:
  mongo_data:
    driver: local
  es_data:
    driver: local
  graylog_journal:
    driver: local
```

logback-spring.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>

<configuration scan="true" scanPeriod="60 seconds" debug="false">
    <include resource="org/springframework/boot/logging/logback/base.xml"/>
    <!-- 获取服务名 -->
    <springProperty scope="context" name="APP_NAME" source="spring.application.name"/>
    <!-- 彩色日志依赖的渲染类 -->
    <conversionRule conversionWord="clr" converterClass="org.springframework.boot.logging.logback.ColorConverter"/>
    <conversionRule conversionWord="wex" converterClass="org.springframework.boot.logging.logback.WhitespaceThrowableProxyConverter"/>
    <conversionRule conversionWord="wEx" converterClass="org.springframework.boot.logging.logback.ExtendedWhitespaceThrowableProxyConverter"/>
    <!-- 彩色日志格式 -->
    <property name="CONSOLE_LOG_PATTERN" value="${CONSOLE_LOG_PATTERN:-%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} %clr(${LOG_LEVEL_PATTERN:-%5p}) %clr(${PID:- }){magenta} %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{50}){cyan} %clr(:){faint} %file:%line - %m%n${LOG_EXCEPTION_CONVERSION_WORD:-%wEx}}"/>
    <!-- graylog全日志格式 -->
    <property name="GRAY_LOG_FULL_PATTERN" value="%n%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%logger{50}] %file:%line%n%-5level: %msg%n"/>
    <!-- graylog简化日志格式 -->
    <property name="GRAY_LOG_SHORT_PATTERN" value="%m%nopex"/>

    <!-- graylog 日志收集 -->
    <appender name="GELF" class="de.siegmar.logbackgelf.GelfUdpAppender">
        <graylogHost>172.30.31.246</graylogHost>
        <graylogPort>12201</graylogPort>
        <maxChunkSize>508</maxChunkSize>
        <useCompression>true</useCompression>
        <encoder class="de.siegmar.logbackgelf.GelfEncoder">
            <includeRawMessage>true</includeRawMessage>
            <includeMarker>true</includeMarker>
            <includeMdcData>true</includeMdcData>
            <includeCallerData>false</includeCallerData>
            <includeRootCauseData>false</includeRootCauseData>
            <includeLevelName>true</includeLevelName>
            <shortPatternLayout class="ch.qos.logback.classic.PatternLayout">
                <pattern>${GRAY_LOG_SHORT_PATTERN}</pattern>
            </shortPatternLayout>
            <fullPatternLayout class="ch.qos.logback.classic.PatternLayout">
                <pattern>${GRAY_LOG_FULL_PATTERN}</pattern>
            </fullPatternLayout>
            <staticField>app_name:${APP_NAME}</staticField>
            <staticField>os_arch:${os.arch}</staticField>
            <staticField>os_name:${os.name}</staticField>
            <staticField>os_version:${os.version}</staticField>
        </encoder>
    </appender>

    <springProfile name="dev">
        <logger name="org.hibernate.type.descriptor.sql.BasicBinder" level="ERROR"/>
        <logger name="org.hibernate.type.descriptor.sql.BasicExtractor" level="ERROR"/>
        <logger name="org.hibernate.SQL" level="ERROR"/>
        <logger name="org.hibernate.engine.QueryParameters" level="DEBUG"/>
        <logger name="org.hibernate.engine.query.HQLQueryPlan" level="DEBUG"/>
        <logger name="com.alibaba.nacos.client" level="ERROR"/>
        <logger name="net.sf.log4jdbc.log.log4j2" level="ERROR"/>
        <logger name="jdbc.connection" level="OFF"/>
        <logger name="jdbc.resultset" level="OFF"/>
        <logger name="jdbc.resultsettable" level="OFF"/>
        <logger name="jdbc.audit" level="OFF"/>
        <logger name="jdbc.sqltiming" level="OFF"/>
        <logger name="jdbc.sqlonly" level="INFO"/>
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
            <appender-ref ref="GELF"/>
        </root>
    </springProfile>

    <springProfile name="test">
        <root level="INFO">
            <appender-ref ref="FILE"/>
        </root>
    </springProfile>

    <springProfile name="prod">
        <root level="INFO">
            <appender-ref ref="FILE"/>
            <appender-ref ref="GELF"/>
        </root>
    </springProfile>
</configuration>
```