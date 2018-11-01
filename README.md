

#### SQL注入过滤器
> 过滤器将拦截所有请求，解析请求包含的查询参数和JSON请求Body，使用正则表达式检查请求参数中是否包含SQL关键字和SQL注入语句，如检查到，直接返回400错误  

```
# 第一组正则，当参数满足分别匹配1、2中的关键字时（1、2各至少命中一个），则认为有SQL注入
1. "insert|delete|update|select|create|drop|truncate|grant|alter|deny|revoke|call|execute|exec|declare|show|rename|set"
2. "into|from|set|where|table|database|view|index|on|cursor|procedure|trigger|for|password|union|and|or"

# 第二组正则，针对字符串拼接时，使用'符号截断SQL，后接注释、OR或UNION关键字进行SQL注入
"'.*(or|union|--|#|/*|;)"
```

- pom.xml

```
<!-- 添加依赖（先上传到私服） -->
<dependency>
    <groupId>com.zlikun.jee</groupId>
    <artifactId>zlikun-jee-sql-injection</artifactId>
    <version>1.0.0</version>
</dependency>
```

- spring-boot

```
# 启动类或配置类上添加如下注解即可
@EnableSqlInjectionFilter
```

- spring-framework

```
# 添加如下配置即可（也可以在xml中配置）
@ComponentScan(basePackages = "com.zlikun.jee.web")
```
