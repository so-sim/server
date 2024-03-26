# 🙌 Sosim Server
이 레포지토리는 소심한 총무 서비스의 Server 레포지토리입니다.

##  How To Contribute?

1. Fork this Repository
2. Add Issue on this repository
3. Typing Code
4. Create Pull & Request
5. Merge 🤗

##  How To Run?

1. Git Clone
2. Turn On your MySql(port : 3306)
3. Create DB
4. Turn On your Redis(port : 6379)
7. move directory to /server
8. type code

```bash
$ ./gradlew build
```

9. move to ./build/libs
10. run jar file

```bash
$ java -jar ~.jar
```

or To Run Background

```bash
$ nohup java -jar ~.jar & /dev/null
```

## 📑 Server Functions
1. OAuth 로그인 기능
2. 모임 생성, 초대 기능
3. 벌금 생성 기능
4. 벌금 알림 기능

# 🪢 Server Architecture
<img width="623" src = "https://user-images.githubusercontent.com/123621015/228475042-c890a0ff-6bd6-4ce4-813f-40735263673b.png">

# 🛠️ Server Stack

## Language
* Java 11

## Web Framework
* Web Framework : Spring(Boot)

## DB

* DataBase : MySQl 8.0.30 (InnoDB)

* DataBase Library : JDBC, Spring Data JPA, QueryDsl

## InMemory DB

* InMemory : Redis latest

## Authentication & Authorization

* Security : Spring Security
* JWT
* JWT Library : io.jsonwebtoken:jjwt-api:0.11.2

# 🔗 Reference

* [✅ 서비스 소개](https://steadfast-car-5c9.notion.site/25348117c25b4403842ed533f388179c)


* [✅ API 명세서]()