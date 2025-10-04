# distributed-transaction

이 레포지토리는 [주문시스템으로 알아보는 분산트랜잭션](https://www.inflearn.com/course/%EC%A3%BC%EB%AC%B8%EC%8B%9C%EC%8A%A4%ED%85%9C%EC%9C%BC%EB%A1%9C-%EC%95%8C%EC%95%84%EB%B3%B4%EB%8A%94-%EB%B6%84%EC%82%B0%ED%8A%B8%EB%9E%9C%EC%9E%AD%EC%85%98/news?srsltid=AfmBOooQslkAOakFKOdbYBUOMbsuAL8eOwz9iumPmyCearvhyhLLedBb) 강의를 바탕으로 정리한 내용입니다.

## 목차
- [프로젝트 세팅](#프로젝트-세팅)
  - [1. DB 세팅](#1-db-세팅)
  - [2. 요구사항 정의](#2-요구사항-정의)
  - [3.주문 로직 구현](#주문-로직-구현)
    - [3-1. 데이터 정합성 문제](#3-1-데이터-정합성-문제)
    - [3-2. 동일한 주문 문제](#3-2-동일한-주문-문제)
    - [3-3. 여러 번 실행되는 문제](#3-3-여러-번-실행되는-문제)
      - [Redis 세팅](#redis-세팅)
      - [Redis Lock 구현](#redis-lock-구현)
- [MSA로 전환하기](#msa로-전환하기)
  - [1. Monolithic](#1-monolithic)
    - [장점](#장점)
    - [단점](#단점)
  - [2. MSA](#2-msa)
    - [장점](#장점-1)
    - [단점](#단점-1)
  - [3. MSA로 전환하면서 발생하는 문제점](#3-msa로-전환하면서-발생하는-문제점)
    - [3-1. Monolithic 트랜잭션 처리](#3-1-monolithic-트랜잭션-처리)
    - [3-2. MSA 트랜잭션 처리](#3-2-msa-트랜잭션-처리)
    - [3-3. 분산 트랜잭션을 보장하기 위한 방법](#3-3-분산-트랜잭션을-보장하기-위한-방법)

# 프로젝트 세팅
## 1. DB 세팅
```
# Docker Run
$ docker run -d -p 3306:3306 \
    -e MYSQL_ROOT_PASSWORD=1234 --name mysql mysql
    
# 실행 결과 확인
$ docker ps

# DB 테이블 생성
$ docker exec -it mysql bash     # Docker Container Bash 접속
$ mysql -u root -p 1234           # mysql 접속
$ CREATE DATABASE commerce_example; # DB Table 생성
$ USE commerce_example;         
```

## 2. 요구사항 정의
- 주문 데이터를 저장해야 한다.
- 재고 관리를 해야 한다.
- 포인트를 사용해야 한다.
- 주문, 재고, 포인트 데이터의 정합성이 맞아야 한다.
- 동일한 주문은 1번만 이루어져야 한다.

## 3. 주문 로직 구현
```java
public void placeOrder(PlaceOrderCommand command) {
    Order order = orderRepository.save(new Order());
    Long totalPrice = 0L;

    for (PlaceOrderCommand.OrderItem item : command.orderItems()) {
        OrderItem orderItem = new OrderItem(order.getId(), item.productId(), item.quantity());
        orderItemRepository.save(orderItem);

        Long price = productService.buy(item.productId(), item.quantity());
        totalPrice += price;
    }

    pointService.use(1L, totalPrice);
}
```

```http request
POST http://localhost:8080/order/place
Content-Type: application/json

{
    "orderItems": [
        {
            "productId": 1,
            "quantity": 2
        },
        {
            "productId": 2,
            "quantity": 2
        }
    ]
}
```

<img width="500" height="300" alt="Image" src="https://github.com/user-attachments/assets/f60b3068-1002-4151-9c88-a254b4619faa" />

### 3-1. 데이터 정합성 문제
- Order 저장과 Product 저장은 성공했지만 Point 저장이 실패하면 Point 저장이 되지 않고 Order, Product 저장은 된 상태가 됨
- 해결책: 트랜잭션의 원자성을 이용해서 모두 성공하거나 모두 실패하게 만들기

```mermaid
graph LR
  A[Order 저장] --> B[Product 저장]
  B -->|💣| C[Point 저장]
```

```java
@Transactional
public void placeOrder(PlaceOrderCommand command) {
    ...
}
```

### 3-2. 동일한 주문 문제
- 동일한 주문이 여러 번 실행됨
- 해결책: 주문 id를 클라이언트에게 반환하여 동일한 주문인지 판별

```mermaid
sequenceDiagram
    participant 클라이언트
    participant 서버
    participant DB
    
    클라이언트->>서버: 주문하기 요청
    서버->>DB: 주문 생성, 주문 아이템 정보 저장
    DB->>서버: 
    서버->>클라이언트: 생성된 주문 id 반환
    클라이언트->>서버: 결제 요청 (with 주문 id)
    서버->>DB: 재고 차감, 포인트 사용
    DB->>서버: 
    서버->>클라이언트: 결과 반환
```

### 3-3. 여러 번 실행되는 문제
- 위 과정을 통해 동일한 주문을 판별할 수 있지만, 여러 번 실행되는 문제는 여전히 존재
- 해결책: 여러 번 실행되지 않도록 Lock 활용

```mermaid
sequenceDiagram
    participant Client
    participant Server
    participant Redis
    
    Client ->> Server: 요청 1 - 주문 요청
    Server ->> Redis: 요청 1 - Lock 점유
    Server ->> Server: 요청 1 로직 수행 중
    
    Client ->> Server: 요청 2 - 주문 요청
    Server ->> Redis: 요청 2 - Lock 점유시도
    Redis --x Server: ❌
    Server --x Client: ❌
    
    Server ->> Redis: 요청 1 - Lock 해제
    Redis -->> Server: 
```

#### Redis 세팅
```
docker pull redis
docker run --name myredis -p 6379:6379 -d redis
docker exec -it myredis redis-cli
```

#### Redis Lock 구현
```java
@Service
public class RedisLockService {

    private final StringRedisTemplate stringRedisTemplate;

    public RedisLockService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public boolean tryLock(String key, String value) {
        return stringRedisTemplate.opsForValue().setIfAbsent(key, value);
    }

    public void releaseLock(String key) {
        stringRedisTemplate.delete(key);
    }
}
```

# MSA로 전환하기
## 1. Monolithic
- 현재 아키텍처는 모놀리식 아키텍처
- 재고, 주문, 결제가 하나의 애플리케이션에 포함된 구조
- 서비스 전체가 단일 애플리케이션으로 묶여 있어 결합도가 높음.
- 빌드와 배포가 한 번에 가능
- 특정 기능의 성능을 높이려면 전체 애플리케이션을 확장해야 함.

```mermaid
flowchart LR
    subgraph Monolithic
        Order[Order]
        Point[Point]
        Product[Product]
    end

    Order --> DB[(DB)]
    Point --> DB[(DB)]
    Product --> DB[(DB)]
```

### 장점
- 구조가 단순해서 빠른 개발과 테스트 가능
- 트랜잭션 관리 용이

### 단점
- 서비스가 커질수록 유지보수와 확장이 어려움

## 2. MSA
- MSA(Microservices Architecture)는 기능을 여러 개의 작고 독립적인 서비스로 나누어 운영하는 아키텍처
- 각 서비스는 도메인 단위로 분리되어 독립적으로 운영
- 개별적으로 빌드와 배포 가능
- 다른 기술 스택 사용 가능
- 성능 확장이 필요한 부분만 선택적으로 확장 가능

```mermaid
flowchart LR
  Order[Order] --> OrderDB[(DB)]
  Point[Point] --> PointDB[(DB)]
  Product[Product] --> ProductDB[(DB)]
```

### 장점
- 도메인 별로 팀 구성, 기술스택 선택 -> 유연성 증가

### 단점
- 초기 인프라 구성과 모니터링 운영 복잡도 증가
- 트랜잭션 관리의 어려움

## 3. MSA로 전환하면서 발생하는 문제점
- 서비스 간 데이터 정합성을 보장하기가 까다로워짐.

### 3-1. Monolithic 트랜잭션 처리
```mermaid
sequenceDiagram
    participant Client
    participant Server

    Client ->> Server: 주문 생성
    Server -->> Client: 주문 id 반환
    Client ->> Server: 주문 + 결제 요청

    rect rgba(200,200,200,0.3)
        Server ->> Server: Transaction Start
        Server ->> Server: 재고 차감
        Server ->> Server: 포인트 사용
        Server ->> Server: 주문 상태 변화
        Server ->> Server: Transaction End
    end

    Server -->> Client: 응답
```

### 3-2. MSA 트랜잭션 처리
```mermaid
sequenceDiagram
  participant Client
  participant OrderServer
  participant ProductServer
  participant PointServer

  Client ->> OrderServer: 주문정보 생성
  OrderServer -->> Client: 주문 id 반환
  Client ->> OrderServer: 주문 + 결제 요청

  OrderServer ->> ProductServer: 재고 차감 요청
  rect rgba(200,200,200,0.3)
    ProductServer ->> ProductServer: Transaction Start
    ProductServer ->> ProductServer: 재고 차감
    ProductServer ->> ProductServer: Transaction End
  end

  ProductServer -->> OrderServer: 재고 차감 완료

  OrderServer ->> PointServer: 포인트 사용 요청
  rect rgba(200,200,200,0.3)
    PointServer ->> PointServer: Transaction Start
    PointServer ->> PointServer: 포인트 차감
    PointServer ->> PointServer: Transaction End
  end

  PointServer -->> OrderServer: 포인트 사용 완료

  rect rgba(200,200,200,0.3)
    OrderServer ->> OrderServer: Transaction Start
    OrderServer ->> OrderServer: 주문 상태 변경
    OrderServer ->> OrderServer: Transaction End
  end

  OrderServer -->> Client: 응답
```

### 3-3. 분산 트랜잭션을 보장하기 위한 방법
- 2PC
- TCC
- SAGA
