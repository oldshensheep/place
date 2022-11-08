[English](README.md) | 中文

# Place

yet another r/place

什么是Place？简单的说就是多人~~捣乱~~画板，每个人在一段时间内只能画一个像素。见  
Reddit [r/place](https://reddit.com/r/place)  
哔哩哔哩 [夏日绘板](https://live.bilibili.com/pages/1702/pixel-drawing)

## 运行/部署

**注意：** 项目使用了虚拟线程，还未正式发布所以 **在启动时请加上参数`--enable-preview`**

如`java --enable-preview -jar app.jar`

### 最小运行要求

- Java Version >= 19

### 最小依赖运行

最小运行运行不需要任何外部依赖（Redis/MySQL）。只需下载github release的 jar 文件  
然后在控制台运行`java -jar 文件名`

```shell
java --enable-preview -jar app.jar
```

默认会初始化一个1000x1000像素大小的画布

如果要配置画布大小，备份速率等参数可以使用自定义配置文件

使用一个自定义配置文件启动

```shell
java --enable-preview -jar app.jar --spring.config.location=./config.yml
```

PS：

应用启动时会尝试连接Redis，如果连接不上则会使用HashMap / byte[] 等内置数据类型代替。

如果没有使用Redis默认每5分钟会备份当前画布信息到运行路径下的`image_bitmap_backup.bin`文件，启动时会自动加载此文件。

正常关闭应用程序也会备份到此文件。

**注意：** 无依赖运行使用SQLite和HashMap存储相关信息，适合不需要高性能的场景。

### 初始化画布 (可选)

可以手动从一张图片初始化画布，POST 访问 http://localhost:8080/init?token=cb2f4c23-5bfb-485c-aa65-e5873f279bab 即可从配置文件中的`app.init-image`
值初始化，默认为`dd.png`。

**注意:** 如果图像大小和配置的画布大小不同会自动缩放到画布大小。

初始化画布需要一个token，在配置文件的`app.token`中，默认为`cb2f4c23-5bfb-485c-aa65-e5873f279bab`。
为了安全请配置另外一个token

### 使用 Redis 和 MySQL (可选)

Redis 默认连接到`localhost:6379`  
使用Redis不需要配置，只需要启动一个Redis服务器。

MyQSL 默认连接到`localhost:3306`  
使用MyQSL需要更改配置文件中的`spring.datasource.url` 为 `jdbc:mysql://localhost:3306/app?createDatabaseIfNotExist=true`

### 应用的配置

完整配置文件请见 [application.yml](src%2Fmain%2Fresources%2Fapplication.yml)

```yaml
app:
  width: 1000                                   # canvas width
  height: 1000                                  # canvas height
  backup-rate: 300                              # backup rate in seconds
  init-image: "./init.png"                        # initial image path
  token: "cb2f4c23-5bfb-485c-aa65-e5873f279bab" # token for operation that requires authentication

```

## 开发

### 后端

打包 `./gradlew bootJar`

[MQService.java](src%2Fmain%2Fjava%2Fcom%2Foldshensheep%2Fplace%2Fservice%2FMQService.java) 是一个消息队列接口。  
[MemMQService.java](src%2Fmain%2Fjava%2Fcom%2Foldshensheep%2Fplace%2Fservice%2Fimpl%2FMemMQService.java)
和[RedisMQService.java](src%2Fmain%2Fjava%2Fcom%2Foldshensheep%2Fplace%2Fservice%2Fimpl%2FRedisMQService.java)
分别实现了使用HashMap为基础的信息队列，和以Redis为基础的消息队列。

[PlaceRepository.java](src%2Fmain%2Fjava%2Fcom%2Foldshensheep%2Fplace%2Frepo%2FPlaceRepository.java) 是一个存储当前画布的接口，
[MemPlaceRepo.java](src%2Fmain%2Fjava%2Fcom%2Foldshensheep%2Fplace%2Frepo%2Fimpl%2FMemPlaceRepo.java)
和[RedisPlaceRepo.java](src%2Fmain%2Fjava%2Fcom%2Foldshensheep%2Fplace%2Frepo%2Fimpl%2FRedisPlaceRepo.java)
分别实现了使用byte[]的画布，和以Redis为基础的画布。

### 前端

api 见 [api.http](api.http)

```http request
### init canvas
POST http://localhost:8080/init

### put a pixel
PUT http://localhost:8080/pixels
Content-Type:  application/json

{
  "x": 50,
  "y": 50,
  "color": [255,0,0,255]
}

### get all pixels
GET http://localhost:8080/pixels/all

### sse pixel
GET http://localhost:8080/time

```

## How I Built Place Again

项目架构参考 Reddit 公司的一篇文章 [How We Built r/Place](https://www.redditinc.com/blog/how-we-built-rplace/)  
部分技术细节不一样。

#### 不一样的地方

1. WebSocket -> Server-Sent Events 因为SSE相较于WS更易于开发。
2. 速率限制使用内存/Redis而不是通过查数据库实现

#### 本应用技术细节

- 使用 byte[] 或者 Redis 存储当前画布像素信息
- 每一个像素操作存储到数据库（SQLite/MySQL）
- 速率限制使用 Redis 或者 HashMap 。
- 使用SSE推送新的像素到客户端

### 后端

- Spring Boot
- Spring Data JPA
- Spring Data Redis
- SQLite / MySQL

### 前端

使用以下~~三大框架~~，主要使用Canvas API。

- Vanilla JS
- Vanilla CSS
- Vanilla HTML

