## **📌 Spring Boot에서 Kafka 연동 - 프로젝트 생성부터**

---

## **1️⃣ Spring Boot 프로젝트 생성**

먼저 Kafka 연동을 위한 Spring Boot 프로젝트를 생성하자.

### **1. Spring Initializr 사용 (자동 생성)**

Spring Boot 프로젝트를 쉽게 생성하는 방법은 [Spring Initializr](https://start.spring.io/)를 이용하는 거야.

**🔹 설정값**

- **Project**: Gradle - Kotlin
- **Language**: Kotlin
- **Spring Boot Version**: 최신 LTS 선택 (예: 3.2.0)
- **Dependencies**:
  - Spring Web
  - Spring for Apache Kafka (spring-kafka)

👉 **[프로젝트 다운로드]** 버튼을 눌러서 압축을 푼 다음, IntelliJ IDEA에서 `build.gradle.kts`를 열어 확인하면 돼.

---

### **2. CLI 사용 (터미널에서 생성)**

CLI로 프로젝트를 생성할 수도 있어. 터미널에서 실행해 보자.

```bash
spring init --dependencies=web,kafka --language=kotlin --type=gradle my-kafka-app
```

그럼 `my-kafka-app` 폴더에 프로젝트가 생성될 거야. 이제 해당 폴더로 이동해서 IDE에서 열어보자.

```bash
cd my-kafka-app
```

---

## **2️⃣ Kafka 의존성 추가**

이제 `build.gradle.kts`에 필요한 의존성을 추가하자.

📌 **`build.gradle.kts` 수정**

```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.kafka:spring-kafka")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
}
```

✅ `spring-boot-starter-web` → REST API 사용을 위해 필요
✅ `spring-kafka` → Kafka와 연동을 위해 필요
✅ `spring-kafka-test` → 테스트를 위한 Kafka 라이브러리

**⚡ 의존성 적용하기**

```bash
./gradlew build
```

또는 IntelliJ IDEA에서 **Gradle** 탭을 열고 **Refresh** 버튼 클릭!

---

## **3️⃣ Kafka 설정 추가**

Kafka 클러스터에 연결하기 위해 `application.yml` 또는 `application.properties` 파일을 설정해야 해.

📌 **`src/main/resources/application.yml` 추가**

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
    consumer:
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      group-id: my-kafka-group
```

✅ `bootstrap-servers: localhost:9092` → Kafka 브로커 주소
✅ `key/value-serializer` → 메시지를 String으로 변환
✅ `group-id` → Consumer 그룹 이름

---

## **4️⃣ Kafka Producer & Consumer 코드 작성**

이제 실제로 Kafka Producer와 Consumer를 만들어 보자.

### **📌 Producer (메시지 전송)**

```kotlin
package com.example.kafkademo.producer

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KafkaProducer(private val kafkaTemplate: KafkaTemplate<String, String>) {

    fun sendMessage(topic: String, message: String) {
        kafkaTemplate.send(topic, message)
        println("Sent message: $message to topic: $topic")
    }
}
```

---

### **📌 Consumer (메시지 수신)**

```kotlin
package com.example.kafkademo.consumer

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class KafkaConsumer {

    @KafkaListener(topics = ["test-topic"], groupId = "my-kafka-group")
    fun listen(record: ConsumerRecord<String, String>) {
        println("Received message: ${record.value()} from topic: ${record.topic()}")
    }
}
```

---

## **5️⃣ Kafka 실행 및 테스트**

이제 Kafka를 실행하고 Producer & Consumer를 테스트해 보자!

### **1. Docker로 Kafka 실행**

터미널에서 Kafka를 실행하자. (Docker가 설치되어 있어야 해!)

```bash
docker-compose up -d
```

📌 `docker-compose.yml` 파일이 없으면 아래처럼 작성해.

```yaml
version: "3"
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
    ports:
      - "2181:2181"

  kafka:
    image: confluentinc/cp-kafka:latest
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
```

---

### **2. Spring Boot 애플리케이션 실행**

```bash
./gradlew bootRun
```

이제 Producer에서 메시지를 보내고, Consumer가 정상적으로 메시지를 받는지 확인해 보자!

---

### **3. 메시지 전송 (Producer 실행)**

Spring Boot 애플리케이션이 실행된 상태에서 REST API로 메시지를 전송해 보자.

```bash
curl -X POST "http://localhost:8080/send?message=HelloKafka"
```

👉 `KafkaProducer`에서 `"HelloKafka"` 메시지를 `test-topic`으로 전송할 거야.

---

### **4. 메시지 수신 (Consumer 확인)**

터미널 로그에서 Consumer가 메시지를 수신하는 걸 확인할 수 있어.

```plaintext
Received message: HelloKafka from topic: test-topic
```

---

## **📌 정리**

✅ **Spring Boot 프로젝트 생성** (`spring-kafka` 의존성 추가)

✅ **Kafka 설정 (`application.yml`)**

✅ **Kafka Producer/Consumer 코드 작성**

✅ **Docker로 Kafka 실행**

✅ **테스트 (Producer → Consumer 확인)**
