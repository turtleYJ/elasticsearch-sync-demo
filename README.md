# 유스케이스(Use Case): 메일 저장 및 검색
| 기능         | 설명                     |
| ---------- | ---------------------- |
| ✅ 메일 저장    | 사용자가 새로운 메일을 저장 요청함    |
| ✅ 메일 검색    | 특정 키워드로 메일을 검색 (제목/본문) |
| ✅ 메일 상세 조회 | 메일 ID로 단일 메일을 조회함      |

# 워크플로우
🔹 1단계: Elasticsearch 직접 연동 버전 (RabbitMQ 없이)
```
[사용자]
   └─> POST /api/mail            (메일 저장 요청)
          └─> TestController
                └─> ElasticService.save()
                      └─> ElasticsearchOperations.save()

[사용자]
   └─> GET /api/mail/search?q=보고서
          └─> TestController
                └─> ElasticService.search()
                      └─> ElasticsearchOperations.search()
```
🔹 2단계: RabbitMQ 연동 버전 (비동기 저장)
```
[사용자]
   └─> POST /api/mail            (메일 저장 요청)
          └─> TestController
                └─> RabbitTemplate.convertAndSend()

[RabbitMQ]
   └─> MQ Listener (RabbitMQListener)
         └─> ElasticService.save()
               └─> ElasticsearchOperations.save()
```

# 엘라스틱서치 설계
🧱 인덱스 정보
| 항목     | 내용                                       |
| ------ | ---------------------------------------- |
| 인덱스 이름 | `mails`                                  |
| 생성 방식  | Spring Boot 애플리케이션 실행 시 자동 생성 |
| 매핑 타입  | Dynamic Mapping 비활성화      |

🧩 필드 정의
| 필드명         | 타입        | 설명                   |
| ----------- | --------- | -------------------- |
| `id`        | `keyword` | 메일 고유 ID (정확히 일치 검색) |
| `subject`   | `text`    | 메일 제목 (전문 검색 대상)     |
| `sender`    | `keyword` | 발신자 이메일 (필터링용)       |
| `receiver`  | `keyword` | 수신자 이메일 (필터링용)       |
| `content`   | `text`    | 메일 본문 (전문 검색 대상)     |
| `folder`    | `keyword` | 받은편지함 / 휴지통 등 분류     |
| `timestamp` | `date`    | 메일 발송 일시 (정렬/범위검색)   |

🧾 매핑 JSON
```json
{
  "mappings": {
    "dynamic": "strict",
    "properties": {
      "id":        { "type": "keyword" },
      "subject":   { "type": "text" },
      "sender":    { "type": "keyword" },
      "receiver":  { "type": "keyword" },
      "content":   { "type": "text" },
      "folder":    { "type": "keyword" },
      "timestamp": { "type": "date" }
    }
  }
}
```
