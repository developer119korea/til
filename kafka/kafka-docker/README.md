## **1️⃣ Kafka 설치하기 (Docker 이용)**

Kafka는 기본적으로 **Zookeeper**가 필요하므로 함께 설치
Docker를 사용하면 빠르고 쉽게 설치할 수 있음

### **🔹 사전 준비 (Docker 설치)**

```sh
brew install --cask docker
```

설치 확인 (터미널에서 실행)

```bash
docker -v
```

출력 예시 (버전이 나오면 정상 설치됨)

```
Docker version 24.0.5, build 1234567
```

---

### **🔹 Docker Compose로 Kafka & Zookeeper 설치**

`docker-compose.yml` 파일을 작성

1️⃣ **docker-compose.yml 파일 생성**

```bash
mkdir kafka-docker && cd kafka-docker
vi docker-compose.yml
```

2️⃣ **다음 내용을 복사해서 붙여넣기**

```yaml
version: "3"
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    container_name: zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
    ports:
      - "2181:2181"

  kafka:
    image: confluentinc/cp-kafka:latest
    container_name: kafka
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: "zookeeper:2181"
      KAFKA_ADVERTISED_LISTENERS: "PLAINTEXT://localhost:9092"
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
```

3️⃣ **Kafka 실행하기**

```bash
docker-compose up -d
```

✔️ `-d` 옵션은 백그라운드에서 실행하도록 해줌.

4️⃣ **실행 확인**

```bash
docker ps
```

출력 예시

```
CONTAINER ID   IMAGE                          COMMAND                  CREATED       STATUS       PORTS                     NAMES
12345abc       confluentinc/cp-kafka:latest   "/etc/confluent/dock…"   2 minutes ago Up 2 minutes 0.0.0.0:9092->9092/tcp   kafka
67890xyz       confluentinc/cp-zookeeper:latest "/etc/confluent/dock…" 2 minutes ago Up 2 minutes 0.0.0.0:2181->2181/tcp   zookeeper
```

✔️ 위와 같이 **`kafka`와 `zookeeper` 컨테이너**가 실행되고 있으면 정상 설치

---

## **2️⃣ Kafka 기본 실습 (CLI 명령어 사용)**

Kafka가 잘 설치되었으니, **기본적인 Kafka 동작을 실습**

### **📌 (1) Kafka 토픽 생성**

토픽은 Kafka에서 메시지를 주고받을 때 사용되는 **메시지 채널**
아래 명령어를 실행해서 `test-topic`이라는 토픽 생성

```bash
docker exec -it kafka kafka-topics --create --topic test-topic --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
```

✔️ **명령어 설명**

- `kafka-topics --create` : 새로운 토픽 생성
- `--topic test-topic` : 토픽 이름 지정
- `--partitions 3` : 3개의 파티션으로 분할
- `--replication-factor 1` : 복제본 1개 설정
- `--bootstrap-server localhost:9092` : Kafka 브로커 주소 설정

---

### **📌 (2) 토픽 목록 확인**

Kafka에 어떤 토픽들이 있는지 확인

```bash
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092
```

출력 예시

```
test-topic
```

✔️ `test-topic`이 보이면 정상적으로 생성

---

### **📌 (3) Producer 실행 (메시지 보내기)**

이제 Kafka **Producer(생산자)**를 실행해서 메시지를 발ㅇ
아래 명령어를 실행하면 입력 대기 상태가 된다.

```bash
docker exec -it kafka kafka-console-producer --topic test-topic --bootstrap-server localhost:9092
```

> ✅ 메시지 입력 예시 (입력 후 Enter)

```
안녕하세요, Kafka!
Kafka 실습 중입니다.
```

✔️ 메시지를 입력하면, Kafka에 저장되지만 아직 Consumer가 없어서 확인할 수 없어.
다음 단계에서 **Consumer(소비자)**를 실행

---

### **📌 (4) Consumer 실행 (메시지 읽기)**

이제 Kafka **Consumer(소비자)**를 실행해서 **Producer가 보낸 메시지를 수신**해보자!

```bash
docker exec -it kafka kafka-console-consumer --topic test-topic --bootstrap-server localhost:9092 --from-beginning
```

> ✅ 실행 후 출력 예시

```
안녕하세요, Kafka!
Kafka 실습 중입니다.
```

✔️ 방금 **Producer가 보낸 메시지**가 Consumer에서 출력되면 성공! 🎉

---

### **📌 (5) 여러 개의 Consumer 실행해보기 (Consumer Group 테스트)**

Kafka는 여러 개의 Consumer를 **같은 그룹**에 포함시켜서 메시지를 **분산 처리**할 수 있어.

#### **1️⃣ 첫 번째 Consumer 실행**

```bash
docker exec -it kafka kafka-console-consumer --topic test-topic --group my-group-1 --bootstrap-server localhost:9092
```

#### **2️⃣ 두 번째 Consumer 실행 (새 터미널에서 실행)**

```bash
docker exec -it kafka kafka-console-consumer --topic test-topic --group my-group-2 --bootstrap-server localhost:9092
```

#### **3️⃣ Producer 실행 후 메시지 전송**

이제 Producer를 실행해서 메시지를 발송.

```bash
docker exec -it kafka kafka-console-producer --topic test-topic --bootstrap-server localhost:9092
```

> ✅ 메시지 입력 예시

```
Kafka Consumer Group 테스트 중입니다.
```

✔️ **두 개의 Consumer가 메시지를 나눠서 받는지 확인**해보자.
✔️ 같은 Consumer Group 내에서는 **한 개의 메시지가 하나의 Consumer에서만 처리**됨.

---

## **3️⃣ Kafka 정리 및 종료**

### **🔹 실행 중인 Kafka 종료**

```bash
docker-compose down
```

### **🔹 생성된 Kafka 데이터 삭제 (초기화)**

```bash
rm -rf /tmp/kafka-logs /tmp/zookeeper
```

---

## **4️⃣ Kafka 실습 정리**

✔️ **Kafka 설치** (Docker 활용)
✔️ **Kafka 기본 개념 학습** (Topic, Producer, Consumer)
✔️ **CLI를 활용한 Kafka 실습** (메시지 송수신)
✔️ **Consumer Group을 활용한 분산 처리 실습**
