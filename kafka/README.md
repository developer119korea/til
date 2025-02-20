Kafka를 익히기 위한 학습 순서를 정리해볼게. 실습과 함께 진행하면 더 효과적이니, **이론 → 실습 → 심화 학습** 순으로 진행하는 걸 추천해! 🚀

---

## **1️⃣ Kafka 기본 개념 익히기**

✅ **목표:** Kafka의 기본 개념과 구조 이해

🔹 **Kafka 기본 개념**

- 메시징 시스템이 필요한 이유
- Kafka vs RabbitMQ (메시지 큐와의 차이점)
- Kafka의 주요 용어:
  - **Producer** (메시지 생산자)
  - **Consumer** (메시지 소비자)
  - **Broker** (Kafka 서버)
  - **Topic** (메시지를 구분하는 채널)
  - **Partition** (토픽을 여러 개의 부분으로 나눠 병렬 처리 가능)
  - **Offset** (메시지의 고유한 위치 정보)

🔹 **추천 자료**

- 📖 [Kafka 공식 문서](https://kafka.apache.org/documentation/)
- 📺 유튜브 Kafka 개념 설명 영상

---

## **2️⃣ Kafka 설치 및 기본 실습**

✅ **목표:** 로컬 환경에서 Kafka를 직접 실행해보고 Producer/Consumer 테스트

🔹 **Kafka 설치 (Docker 추천)**

```bash
# Kafka & Zookeeper 컨테이너 실행 (Docker Compose 사용)
docker-compose up -d
```

- Kafka는 Zookeeper가 필요하므로 같이 실행해야 함

🔹 **Kafka 기본 CLI 명령어 실습**

```bash
# 토픽 생성
kafka-topics.sh --create --topic test-topic --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1

# Producer 메시지 전송
kafka-console-producer.sh --topic test-topic --bootstrap-server localhost:9092

# Consumer 메시지 수신
kafka-console-consumer.sh --topic test-topic --bootstrap-server localhost:9092 --from-beginning
```

🔹 **Kafka 주요 개념 실습**

- 단일 Producer → Consumer 구조 실습
- 여러 Consumer 그룹을 만들어 메시지를 분배하는 방식 확인

---

## **3️⃣ Kafka 프로그래밍 실습 (Spring Boot or Python)**

✅ **목표:** Kafka 클라이언트를 사용하여 프로듀서/컨슈머 애플리케이션 개발

🔹 **Kafka Java(Spring Boot) 연동**

- `spring-kafka` 라이브러리 추가
- **Producer 코드** 작성 (KafkaTemplate 사용)
- **Consumer 코드** 작성 (Listener 사용)

🔹 **Kafka Python 연동**

- `confluent-kafka` 라이브러리 사용
- Kafka Producer/Consumer 개발

🔹 **실습 목표**

- JSON 형식의 메시지를 송수신
- 다중 Consumer 그룹 실습
- 오류 처리 및 재시도 (Retry, DLQ)

---

## **4️⃣ Kafka 운영 & 모니터링 이해**

✅ **목표:** Kafka 운영 및 성능 최적화 방법 익히기

🔹 **Kafka 운영 개념**

- **Replication** (데이터 복제)
- **Consumer Group Rebalancing**
- **Offset 관리 (commit, retention 설정 등)**
- **Kafka 성능 튜닝 (Batch Size, Compression 등)**

🔹 **Kafka 모니터링 도구**

- Prometheus + Grafana 대시보드 설정
- Kafka Manager, Kafdrop 사용

---

## **5️⃣ Kafka 심화 학습 (실제 프로젝트 적용)**

✅ **목표:** 마이크로서비스 아키텍처(MSA)에서 Kafka 활용하기

🔹 **고급 기능 익히기**

- **Kafka Streams** (실시간 데이터 처리)
- **Kafka Connect** (외부 DB/MySQL/PostgreSQL 연동)
- **Schema Registry & Avro** (데이터 포맷 일관성 유지)

🔹 **Kafka를 활용한 실전 프로젝트**

- **로그 수집 시스템** (ELK + Kafka)
- **마이크로서비스 이벤트 기반 아키텍처**
- **실시간 데이터 파이프라인 구축**

---

## **🔥 추천 학습 경로 정리**

📌 **1단계 - 기본 개념 학습**
📌 **2단계 - 로컬에서 Kafka 설치 후 기본 실습**
📌 **3단계 - Spring Boot 또는 Python을 사용한 Kafka 연동 실습**
📌 **4단계 - Kafka 운영, 모니터링 및 성능 최적화 학습**
📌 **5단계 - Kafka를 활용한 실제 프로젝트 적용**

---

### **📌 추가로 학습하면 좋은 자료**

📖 **책:**

- _Kafka: The Definitive Guide_ (Kafka 공식 가이드)
- _Designing Data-Intensive Applications_ (Kafka 기반 데이터 시스템 설계)

📺 **강의:**

- Udemy Kafka 강의 (Kafka 기본 개념부터 실습까지)
- Confluent Kafka 공식 강의
