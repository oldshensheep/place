English | [中文](README.zh-CN.md)

# Place

yet another r/place

What is Place? it is a multi-person ~~mash~~ drawing board where each person can only draw one pixel over
a period of time. See

Reddit [r/place](https://reddit.com/r/place)  
哔哩哔哩 [夏日绘板](https://live.bilibili.com/pages/1702/pixel-drawing)

## Run/Deploy

**Note:** The project uses virtual threads and has not been officially released yet so **Please add the
parameter `-enable-preview`** at startup.

such as `java --enable-preview -jar app.jar`

### Minimum runtime requirements

- Java Version >= 19

### Minimal dependency run

The minimal run does not require any external dependencies (Redis/MySQL) to run. Just download the GitHub release jar 

Then run `java --enable-preview -jar filename` in console

```shell
java --enable-preview -jar app.jar
```

By default, a canvas of 1000x1000 pixels will be initialized

If you want to configure the canvas size, backup rate and other parameters you can use a custom configuration file.

Start with a custom configuration file

```shell
java --enable-preview -jar app.jar --spring.config.location=. /config.yml
```

PS:

The application will try to connect to Redis when it starts, and if it doesn't connect, it will use a built-in data type
like HashMap / byte[] instead.

If Redis is not used, the current canvas information will be backed up every 5 minutes to the `image_bitmap_backup.bin`
file in the runtime path, which will be loaded automatically at startup.

This file will also be backed up when the application is closed normally.

**Note:** Dependency-free runtime uses SQLite and HashMap to store relevant information, suitable for scenarios that do
not require high performance.

### Initialize canvas (optional)

The canvas can be initialized manually from an image by HTTP POST http://localhost:8080/init?token=cb2f4c23-5bfb-485c-aa65-e5873f279bab to initialize it from
the `app.init-image` value in the configuration file.
value in the configuration file, which defaults to `dd.png`.

Initializing the canvas requires a token, which is in the `app.token` of the configuration file, and The default is to randomly generate an uuid

**Note:** If the image size is different from the configured canvas size it will be automatically scaled to the canvas
size.

### Use Redis and MySQL (optional)

Redis connects to `localhost:6379` by default  
No configuration is required to use Redis, just start a Redis server.

MyQSL connects to `localhost:3306` by default  
Using MyQSL requires changing the `spring.datasource.url` in the configuration file
to `jdbc:mysql://localhost:3306/app?createDatabaseIfNotExist=true`

### Configuration of the application

For the full configuration file see [application.yml](src%2Fmain%2Fresources%2Fapplication.yml)

```yaml
app:
  width: 1000 # canvas width
  height: 1000 # canvas height
  backup-rate: 300 # backup rate in seconds
  init-image: ". /init.png" # initial image path
  token: "cb2f4c23-5bfb-485c-aa65-e5873f279bab" # token for operation that requires authentication

```

## Development

### Backend

Package `. /gradlew bootJar`

[MQService.java](src%2Fmain%2Fjava%2Fcom%2Foldshensheep%2Fplace%2Fservice%2FMQService.java) is a message queue
interface.  
[MemMQService.java](src%2Fmain%2Fjava%2Fcom%2Foldshensheep%2Fplace%2Fservice%2Fimpl%2FMemMQService.java)
and [RedisMQService.java](src%2Fmain%2Fjava%2Fcom%2Foldshensheep%2Fplace%2Fservice%2Fimpl%2FRedisMQService.java)
implements a message queue using a HashMap-based message queue, and a Redis-based message queue, respectively.

[PlaceRepository.java](src%2Fmain%2Fjava%2Fcom%2Foldshensheep%2Fplace%2Frepo%2FPlaceRepository.java) is an interface
that stores the current canvas
[MemPlaceRepo.java](src%2Fmain%2Fjava%2Fcom%2Foldshensheep%2Fplace%2Frepo%2Fimpl%2FMemPlaceRepo.java)
and [RedisPlaceRepo.java](src%2Fmain%2Fjava%2Fcom%2Foldshensheep%2Fplace%2Frepo%2Fimpl%2FRedisPlaceRepo.java)
implements a canvas using byte[], and a canvas based on Redis, respectively.

### Front-end

api see [api.http](api.http)

```http request
### init canvas
POST http://localhost:8080/init

### put a pixel
PUT http://localhost:8080/pixels
Content-Type: application/json

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

The project architecture is referenced in a Reddit
article [How We Built r/Place](https://www.redditinc.com/blog/how-we-built-rplace/)  
Some technical details are different.

#### is not the same

1. WebSocket -> Server-Sent Events because SSE is easier to develop compared to WS. 2.
2. rate limiting is achieved by using memory/Redis instead of checking database

#### Technical details of this application

- Use byte[] or Redis to store current canvas pixel information
- Each pixel operation is stored to a database (SQLite/MySQL)
- Rate limiting using Redis or HashMap .
- Push new pixels to the client using SSE

### Backend

- Spring Boot
- Spring Data JPA
- Spring Data Redis
- SQLite / MySQL

### Front-end

Use the following ~~three major frameworks~~, mainly using the Canvas API.

- Vanilla JS
- Vanilla CSS
- Vanilla HTML
