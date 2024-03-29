<p align="center">
  <a href="https://search.maven.org/artifact/cn.yiynx/xif">
    <img alt="maven" src="https://img.shields.io/maven-central/v/cn.yiynx/xif.svg?style=flat-square">
  </a>
  <a target="_blank" href="http://license.coscl.org.cn/MulanPSL2/">
    <img alt="license" src="https://img.shields.io/:license-MulanPSL2-blue.svg" />
  </a>
  <a target="_blank" href="https://www.oracle.com/technetwork/java/javase/downloads/index.html">
	<img alt="java version" src="https://img.shields.io/badge/JDK-1.8+-green.svg" />
  </a>
</p>

# 简介 | Intro
Java if 扩展工具包 - if处理逻辑解耦
# 快速开始

## 添加依赖
引入Xif依赖
``` xml
<dependency>
    <groupId>cn.yiynx</groupId>
    <artifactId>xif</artifactId>
    <version>1.0.7</version>
</dependency>
```
## 配置
在 Spring Boot 启动类中添加 @XifScan 注解
``` java
package cn.yiynx.demo;

import cn.yiynx.xif.scan.XifScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@XifScan
@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
```

## 编码使用示例【代码片段】
1、使用if的方式【重构前】：
``` java
public void testIf() {
    log.info("testIf");
    Message message = new Message().setType("type1");
    if ("type1".equals(message.getType())) {
        log.info("【if】type等于type1 处理:{}", message);
    } else if ("type2".equals(message.getType())) {
        log.info("【if】type等于type2 处理:{}", message);
    } else {
        log.info("【if】type else 处理:{}", message);
    }
}
```
2、使用xif的方式【重构后】
``` java
    public void testXif() {
        log.info("testXif");
        Message message = new Message().setType("type1");
        Xif.handler("xif-group-message", message);
    }
    
    @XifListener(group = "xif-group-message", condition = "#message.type eq 'type1'")
    public void type1(Message message) {
        log.info("【xif】type等于type1处理:{}", message);
    }

    @XifListener(group = "xif-group-message", condition = "#message.type eq 'type2'")
    public void type2(Message message) {
        log.info("【xif】type等于type2 处理:{}", message);
    }

    @XifListener(group = "xif-group-message")
    public void typeElse(Message message) {
        log.info("【xif】type else 处理:{}", message);
    }
```

## 编码使用示例

[Xif使用代码示例](https://gitee.com/yiynx/example)

日志
```
logging.level.cn.yiynx.xif=debug
``` 

# 第三方依赖关系
| 名称                        | 开源许可证          | 版本     | 
| ---------------------------| ----------------- |--------|
| Spring Framework（aop、beans、context、core、expression、jcl）           | Apache-2.0        | 5.3.19 | 
| Apache Log4j               | Apache-2.0        | 2.17.2 |     
| slf4j                      | MIT               | 1.7.25 |
