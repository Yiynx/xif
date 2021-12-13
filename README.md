<p align="center">
  <a href="https://search.maven.org/artifact/cn.yiynx/xif">
    <img alt="maven" src="https://img.shields.io/maven-central/v/cn.yiynx/xif.svg?style=flat-square">
  </a>
  <a target="_blank" href="https://license.coscl.org.cn/MulanPSL2/">
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
    <version>1.0.3</version>
</dependency>
```
## 配置
在 Spring Boot 启动类中添加 @XifScan 注解
``` java
package cn.yiynx.demo;

import cn.yiynx.xif.scan.XifScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@XifScan
@EnableAsync
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
    Abc<Integer> abc = new Abc<Integer>().setType("type1").setData(2021);
    if ("type1".equals(abc.getType()) && Objects.equals(2021, abc.getData())) {
        log.info("【if】type等于type1 且 data等于2021 处理");
    } else if ("type2".equals(abc.getType())) {
        log.info("【if】type等于type2 处理");
    } else {
        log.info("【if】type eslse 处理");
    }
}
```
2、使用xif的方式【重构后】
``` java
    public void testXif() {
        log.info("testXif");
        Abc<Integer> abc = new Abc<Integer>().setType("type1").setData(2021);
        Xif.handler("xif-group-abc", abc);
    }
    
    @Async
    @XifListener(group = "xif-group-abc", condition = "#abc.type eq 'type1' and #abc.data eq 2021")
    public void type1(Abc abc) {
        log.info("【xif】type等于type1 且 data等于2021 处理");
    }

    @XifListener(group = "xif-group-abc", condition = "#abc.type eq 'type2'")
    public void type2(Abc abc) {
        log.info("【xif】type等于type2 处理");
    }
    
    @XifListener(group = "xif-group-abc")
    public void typeElse(Abc abc) {
        log.info("【xif】type else 处理");
    }
```

## 编码使用示例【完整代码】

``` java
package cn.yiynx.demo;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class Abc<T> {
    private String type;
    private T data;
}
```

``` java
package cn.yiynx.demo;

import cn.yiynx.xif.core.XifListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.Future;

@Slf4j
@Component
public class AbcXifHandler {

    @Async
    @XifListener(group = "xif-group-abc", condition = "#abc.type eq 'type1' and #abc.data eq 2021")
    public Future<Boolean> type1(Abc abc) {
        log.info("【xif】type等于type1 且 data等于2021 处理");
        return new AsyncResult<>(true);
    }

    @XifListener(group = "xif-group-abc", condition = "#abc.type eq 'type2'")
    public Boolean type2(Abc abc) {
        log.info("【xif】type等于type2 处理");
        return true;
    }
    
    @XifListener(group = "xif-group-abc")
    public void typeElse(Abc abc) {
        log.info("【xif】type else 处理");
    }
}
```
测试代码
``` java
package cn.yiynx.demo;

import cn.yiynx.xif.core.Xif;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class DemoApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void testXif() {
		log.info("testXif");
		Abc<Integer> abc = new Abc<Integer>().setType("type1").setData(2021);
		Xif.handler("type", abc);
	}
}
```
日志
```
logging.level.cn.yiynx.xif=debug
``` 

# 第三方依赖关系
| 名称                        | 开源许可证          | 版本              | 
| ---------------------------| ----------------- | ---------------- |
| Spring Framework（aop、beans、context、core、expression、jcl）           | Apache-2.0        | 5.3.13            | 
| Apache Log4j               | Apache-2.0        | 2.15.0           |     
| slf4j                      | MIT               | 1.7.25           |
