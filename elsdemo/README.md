# Elasticsearch Sync Demo

메일 데이터를 Elasticsearch에 동기화하는 Spring Boot 애플리케이션입니다.

## 🚀 주요 기능

### 1. 메일 저장 API
- **동기 저장**: `/api/mail` (POST)
- **비동기 저장**: `/api/mail/mq` (POST) - RabbitMQ를 통한 비동기 처리

### 2. 메일 조회 API
- **검색**: `/api/mail/search?q={keyword}` (GET)
- **단건 조회**: `/api/mail/{id}` (GET)

### 3. MQ → Elasticsearch 연계
- RabbitMQ 메시지 수신 → Elasticsearch 저장

## 🛠️ 기술 스택

- **Spring Boot 3.5.0**
- **Spring Data Elasticsearch**
- **Spring AMQP (RabbitMQ)**
- **Elasticsearch Java Client 8.18.1**
- **Java 17**

## 📋 사전 요구사항

1. **Elasticsearch** 실행 (기본: http://localhost:9200)
2. **RabbitMQ** 실행 (기본: localhost:5672)

### Docker로 실행하는 경우:

```bash
# Elasticsearch 실행
docker run -d --name elasticsearch -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" elasticsearch:8.11.0

# RabbitMQ 실행
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

## 🚀 실행 방법

1. **애플리케이션 실행**
```bash
./gradlew bootRun
```

2. **API 테스트**
```bash
# test-api.http 파일을 사용하여 API 테스트
```

## 📡 API 사용법

### 1. 메일 저장 (동기)
```bash
curl -X POST http://localhost:8080/api/mail \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "테스트 메일",
    "sender": "sender@example.com",
    "receiver": "receiver@example.com",
    "content": "메일 내용",
    "folder": "INBOX",
    "timestamp": "2024-01-15T10:30:00Z"
  }'
```

### 2. 메일 저장 (비동기 - MQ)
```bash
curl -X POST http://localhost:8080/api/mail/mq \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "MQ 테스트 메일",
    "sender": "sender@example.com",
    "receiver": "receiver@example.com",
    "content": "MQ를 통한 메일 내용",
    "folder": "INBOX",
    "timestamp": "2024-01-15T11:00:00Z"
  }'
```

### 3. 메일 검색
```bash
curl "http://localhost:8080/api/mail/search?q=테스트"
```

### 4. 메일 단건 조회
```bash
curl http://localhost:8080/api/mail/{mail-id}
```

## 🔧 설정

`src/main/resources/application.yml`에서 다음 설정을 변경할 수 있습니다:

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

  elasticsearch:
    uris: http://localhost:9200
```

## 📊 모니터링

애플리케이션 로그를 통해 다음을 확인할 수 있습니다:
- API 요청/응답
- MQ 메시지 발행/수신
- Elasticsearch 저장 상태
- 에러 발생 시 상세 정보

## 🧪 테스트

```bash
# 단위 테스트 실행
./gradlew test

# 통합 테스트 실행
./gradlew integrationTest
```

## 📝 주의사항

1. **Elasticsearch 인덱스**: `mails` 인덱스가 자동으로 생성됩니다.
2. **MQ 큐**: `mail-queue` 큐가 자동으로 생성됩니다.
3. **로깅**: 모든 주요 작업이 로그로 기록됩니다.
4. **예외 처리**: API 응답에 적절한 HTTP 상태 코드가 반환됩니다. 