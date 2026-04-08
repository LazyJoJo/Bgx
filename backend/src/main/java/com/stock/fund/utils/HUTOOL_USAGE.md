# Bgx后端工具类使用规范

## 项目规范

本项目严格遵循Hutool依赖治理规范，**禁止重复创建Hutool已提供的通用工具类**，同时使用行业标准的第三方库替代自建工具类。

##禁止重复创建的工具类（使用Hutool替代）

以下功能应直接使用Hutool对应API：

|功能领域 | 自建工具类 | Hutool替代方案 | 使用示例 |
|---------|-----------|---------------|----------|
| 日期处理 | DateUtils | `cn.hutool.core.date.DateUtil` | `DateUtil.now()` |
| JSON操作 | JsonUtils | `cn.hutool.json.JSONUtil` | `JSONUtil.toJsonStr(obj)` |
| 字符串处理 | StringUtils | `cn.hutool.core.util.StrUtil` | `StrUtil.isEmpty(str)` |
|集合操作 | CollectionUtils | `cn.hutool.core.collection.CollUtil` | `CollUtil.isEmpty(collection)` |
| 加密解密 | CryptoUtils | `cn.hutool.crypto.SecureUtil` | `SecureUtil.md5(str)` |
| HTTP请求 | HttpUtils | OkHttp3 | `OkHttpClient client = new OkHttpClient()` |
| 配置读取 | ConfigUtils | `cn.hutool.setting.Setting` | `SettingUtil.get(settingFile)` |

##✅ 保留的业务特有工具类

仅保留以下金融数据处理项目特有的工具类：

### 1. NumberUtils - 数字工具类（保留）
**用途**: 金融数据特有计算、格式化
**特点**: 
-的小数计算
-百比增长率计算
-格式化
-安全数学运算

### 2. AssertUtils -断言工具类（保留）
**用途**: 业务逻辑参数验证
**特点**: 
- 自定义业务断言
-异常处理包装
-安全执行机制

### 3. ThreadPoolUtils -线程池工具类（保留）
**用途**:线程池管理
**特点**: 
-全线程池复用
-异步执行框架
-资源管理

### 4. FileUtils - 文件工具类（保留）
**用途**: 文件操作
**特点**: 
- 读写安全性
-大处理
-目录操作

## 📋具使用使用示例

### 使用Hutool处理日期（替代自建DateUtils）
```java
// 使用Hutool（推荐）
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;

// 日期格式化
String now = DateUtil.now();
String formatDate = DateUtil.format(date, "yyyy-MM-dd");
LocalDateTime parseDate = LocalDateTimeUtil.parse("2024-01-15 10:30:00");

// 日计器后求
LocalDateTime future = LocalDateTimeUtil.offset(now, 30, ChronoUnit.DAYS);
long daysBetween = LocalDateTimeUtil.between(start, end, ChronoUnit.DAYS);
```

### 使用Hutool处理JSON（替代自建JsonUtils）
```java
// 使用Hutool（推荐）
import cn.hutool.json.JSONUtil;
import cn.hutool.json.JSONObject;

// 对象转JSON
String json = JSONUtil.toJsonStr(object);
// JSON转对象
MyObject obj = JSONUtil.toBean(json, MyObject.class);
// JSON转Map
JSONObject jsonObj = JSONUtil.parseObj(json);
```

### 使用Hutool处理字符串（替代自建StringUtils）
```java
// 使用Hutool（推荐）
import cn.hutool.core.util.StrUtil;

// 字符串验证
boolean isEmpty = StrUtil.isEmpty(str);
boolean isBlank = StrUtil.isBlank(str);
boolean hasText = StrUtil.hasText(str);

// 字符串处理
String safeStr = StrUtil.nullToEmpty(obj);
String trimmed = StrUtil.trim(str);
String joined = StrUtil.join(",", "a", "b", "c");
```

### 使用Hutool处理集合（替代自建CollectionUtils）
```java
// 使用Hutool（推荐）
import cn.hutool.core.collection.CollUtil;

//验证
boolean isEmpty = CollUtil.isEmpty(collection);
//操作
String first = CollUtil.getFirst(list);
//
List<String> filtered = CollUtil.filter(list, predicate);
```

### 使用Hutool加密（替代自建CryptoUtils）
```java
// 使用Hutool（推荐）
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.DigestAlgorithm;

//算法
String md5 = SecureUtil.md5(input);
String sha256 = SecureUtil.sha256(input);
// Base64
String encoded = SecureUtil.encodeBase64(input.getBytes());
byte[] decoded = SecureUtil.decodeBase64(encoded);
```

### 使用OkHttp3处理HTTP请求（替代自建HttpUtils）
```java
// 使用OkHttp3（推荐）
import okhttp3.*;

// 创建客户端
OkHttpClient client = new OkHttpClient();

// GET请求
Request getRequest = new Request.Builder()
    .url("https://api.example.com/data")
    .build();

// POST请求
RequestBody requestBody = RequestBody.create("data", MediaType.get("text/plain"));
Request postRequest = new Request.Builder()
    .url("https://api.example.com/data")
    .post(requestBody)
    .build();

//头的请求
Request headerRequest = new Request.Builder()
    .url("https://api.example.com/data")
    .header("Authorization", "Bearer token")
    .build();
```

##⚠重要提醒

1. **严格遵守规范**: 不得重复创建Hutool/行业标准库已提供的功能
2. **优先使用Hutool**: 通用工具类首选Hutool
3. **HTTP请求使用OkHttp**:请求使用OkHttp3替代自建工具类
4. **保留业务特性**: 仅保留金融数据处理特有的工具类
5. **统一依赖管理**: 通过pom.xml统一管理版本

## 📚参文档

- [Hutool官方文档](https://www.hutool.cn/docs/)
- [Hutool GitHub](https://github.com/dromara/hutool)