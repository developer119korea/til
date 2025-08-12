# 분산환경에서의 동시성 제어 (Distributed Concurrency Control)

## 🤔 왜 정리하게 되었나?

단일 데이터베이스에서의 동시성 제어를 공부하다 보니, 마이크로서비스나 분산 시스템 환경에서는 어떻게 동시성을 제어할지 궁금해졌다.
여러 노드에 데이터가 분산되어 있고, 네트워크를 통해 통신해야 하는 환경에서는 기존의 락이나 트랜잭션만으로는 해결할 수 없는 문제들이 많을 것 같아서 정리해보았다.

## 📚 학습 내용 정리

### 1. 분산환경의 특징

- **네트워크 지연**: 노드 간 통신에 시간이 소요됨
- **부분 실패**: 일부 노드만 실패할 수 있음 (단일 시스템과의 차이점)
- **독립성**: 각 노드가 독립적으로 동작
- **확장성**: 수평적 확장이 가능하지만 복잡성 증가
- **네트워크 분할**: ## 🔍 개인적인 정리

### 분산환경 동시성 제어의 핵심 깨달음

- **네트워크는 믿을 수 없다**: 언제든 지연, 패킷 손실, 분할이 발생할 수 있음
- **CAP 정리가 현실**: Consistency, Availability, Partition Tolerance 중 2개만 선택 가능
- **완벽한 동기화는 불가능**: 물리적 한계로 인해 완전한 일관성은 비용이 매우 높음
- **Trade-off 중심 사고**: 성능 vs 일관성, 가용성 vs 일관성의 균형점 찾기
- **장애는 일상**: 부분 실패를 전제로 한 설계가 필수

### 실무에서 적용할 때 고려사항

- **분산 락은 최후의 수단**: 성능 오버헤드가 크므로 정말 필요한 경우에만
- **이벤트 소싱 + SAGA**: 분산 트랜잭션보다 실용적인 대안
- **최종 일관성 수용**: 비즈니스가 허용한다면 성능과 가용성 크게 향상
- **모니터링이 생명**: 분산 시스템에서는 관찰가능성이 더욱 중요
- **Circuit Breaker 패턴**: 장애 전파 방지를 위한 필수 패턴

## 🎯 추가로 깊이 알아보고 싶은 것들

### 1. 실무 적용 관련

- **Netflix, Amazon의 사례**: 대규모 분산 시스템에서의 실제 적용 사례
- **Chaos Engineering**: 장애를 의도적으로 발생시켜 시스템 복원력 테스트
- **분산 시스템 모니터링**: Jaeger, Zipkin 등을 활용한 분산 추적

### 2. 고급 알고리즘

- **Vector Clock**: Lamport Clock의 한계를 극복한 논리적 시계
- **Gossip Protocol**: 대규모 노드 간 정보 전파 방법
- **CRDT (Conflict-free Replicated Data Types)**: 자동으로 충돌을 해결하는 데이터 구조

### 3. 최신 기술 동향

- **Blockchain 합의 알고리즘**: PoW, PoS, DPoS 등
- **Serverless에서의 분산 동시성**: FaaS 환경에서의 상태 관리
- **Edge Computing**: 지리적으로 분산된 환경에서의 동시성 제어

## 📚 학습 리소스

- **도서**: "Designing Data-Intensive Applications" by Martin Kleppmann
- **논문**: "Time, Clocks, and the Ordering of Events in a Distributed System" by Leslie Lamport
- **실습**: Redis Cluster, Kafka, Zookeeper 등을 직접 구축해보기드 간 통신이 불가능한 상황 발생 가능

### 2. 분산환경에서 발생하는 동시성 문제

#### 2.1 분산 데드락 (Distributed Deadlock)

서로 다른 노드의 리소스를 순환적으로 대기하는 상황

```sql
-- Node A에서 실행되는 트랜잭션
BEGIN;
UPDATE accounts SET balance = balance - 100 WHERE user_id = 1; -- Node A의 데이터 락 획득
UPDATE accounts SET balance = balance + 100 WHERE user_id = 2; -- Node B의 데이터 락 대기

-- Node B에서 동시에 실행되는 트랜잭션
BEGIN;
UPDATE accounts SET balance = balance - 50 WHERE user_id = 2;  -- Node B의 데이터 락 획득
UPDATE accounts SET balance = balance + 50 WHERE user_id = 1;  -- Node A의 데이터 락 대기
```

#### 2.2 Split-brain 문제

네트워크 파티션으로 인해 클러스터가 여러 그룹으로 분할되는 상황

```text
Original Cluster: [Node A] ↔ [Node B] ↔ [Node C]
                      ↓ 네트워크 분할 발생
Partition 1: [Node A]    Partition 2: [Node B] ↔ [Node C]
```

- 각 파티션이 독립적으로 쓰기 작업을 수행하여 데이터 불일치 발생
- 네트워크 복구 후 충돌하는 변경사항들을 병합해야 하는 문제

#### 2.3 Clock Synchronization 문제

분산 시스템에서 시간 기반 순서 결정의 어려움

```python
# 서로 다른 노드에서 발생하는 이벤트
# Node A: timestamp = 1642580400.123 (이벤트 X)
# Node B: timestamp = 1642580400.125 (이벤트 Y)
#
# 실제로는 Y가 먼저 발생했지만, 시계 동기화 오차로 인해
# X가 먼저 발생한 것으로 기록될 수 있음
```

- **Physical Clock 문제**: 각 노드의 시계가 정확히 동기화되기 어려움
- **Logical Clock 필요성**: Lamport Clock, Vector Clock 등의 논리적 시계 사용

## � 분산 동시성 제어 방법

### 1. 분산 락 (Distributed Locking)

#### 1.1 Redis를 이용한 분산 락

```python
import redis
import time
import uuid

class RedisDistributedLock:
    def __init__(self, redis_client, key, timeout=10):
        self.redis = redis_client
        self.key = f"lock:{key}"
        self.timeout = timeout
        self.identifier = str(uuid.uuid4())

    def acquire(self):
        end = time.time() + self.timeout
        while time.time() < end:
            if self.redis.set(self.key, self.identifier, nx=True, ex=self.timeout):
                return True
            time.sleep(0.001)
        return False

    def release(self):
        pipe = self.redis.pipeline(True)
        while True:
            try:
                pipe.watch(self.key)
                if pipe.get(self.key) == self.identifier:
                    pipe.multi()
                    pipe.delete(self.key)
                    pipe.execute()
                    return True
                pipe.unwatch()
                break
            except redis.WatchError:
                pass
        return False

# 사용 예시
lock = RedisDistributedLock(redis_client, "user:123")
if lock.acquire():
    try:
        # 임계 영역 코드
        print("Critical section executed")
    finally:
        lock.release()
```

#### 1.2 Zookeeper를 이용한 분산 락

```python
from kazoo.client import KazooClient
from kazoo.recipe.lock import Lock

# Zookeeper 클라이언트 생성
zk = KazooClient(hosts='127.0.0.1:2181')
zk.start()

# 분산 락 생성
lock = Lock(zk, "/myapp/locks/resource1")

# 락 획득 및 사용
with lock:
    # 임계 영역 코드
    print("Critical section with Zookeeper lock")

zk.stop()
```

### 2. 합의 알고리즘 (Consensus Algorithms)

#### 2.1 Raft 알고리즘

- **Leader Election**: 리더 노드 선출
- **Log Replication**: 로그 복제를 통한 일관성 보장
- **Safety**: 안전성 보장

```python
# Raft 상태 예시
class RaftNode:
    def __init__(self, node_id):
        self.node_id = node_id
        self.state = "follower"  # follower, candidate, leader
        self.current_term = 0
        self.voted_for = None
        self.log = []
        self.commit_index = 0

    def request_vote(self, term, candidate_id):
        if term > self.current_term:
            self.current_term = term
            self.voted_for = None
            self.state = "follower"

        if (self.voted_for is None or self.voted_for == candidate_id) and \
           term >= self.current_term:
            self.voted_for = candidate_id
            return True
        return False
```

#### 2.2 PBFT (Practical Byzantine Fault Tolerance)

- 비잔틴 장애를 허용하는 합의 알고리즘
- 3f+1개의 노드로 f개의 악의적 노드 허용

### 3. 이벤트 소싱 (Event Sourcing)

#### 3.1 개념

- 상태 변경을 이벤트로 저장
- 현재 상태는 이벤트들을 재생하여 계산

```python
# 이벤트 소싱 예시
class BankAccount:
    def __init__(self, account_id):
        self.account_id = account_id
        self.events = []
        self.balance = 0

    def apply_event(self, event):
        if event['type'] == 'AccountCreated':
            self.balance = event['initial_balance']
        elif event['type'] == 'MoneyDeposited':
            self.balance += event['amount']
        elif event['type'] == 'MoneyWithdrawn':
            self.balance -= event['amount']

    def deposit(self, amount):
        event = {
            'type': 'MoneyDeposited',
            'account_id': self.account_id,
            'amount': amount,
            'timestamp': time.time()
        }
        self.events.append(event)
        self.apply_event(event)

    def withdraw(self, amount):
        if self.balance >= amount:
            event = {
                'type': 'MoneyWithdrawn',
                'account_id': self.account_id,
                'amount': amount,
                'timestamp': time.time()
            }
            self.events.append(event)
            self.apply_event(event)
            return True
        return False
```

### 4. SAGA 패턴

#### 4.1 Choreography SAGA

```python
# 주문 처리 SAGA 예시
class OrderSaga:
    def __init__(self):
        self.steps = [
            self.reserve_inventory,
            self.process_payment,
            self.update_delivery
        ]
        self.compensations = [
            self.cancel_inventory_reservation,
            self.refund_payment,
            self.cancel_delivery
        ]

    def execute(self, order_data):
        completed_steps = []
        try:
            for i, step in enumerate(self.steps):
                result = step(order_data)
                if result['success']:
                    completed_steps.append(i)
                else:
                    # 보상 트랜잭션 실행
                    self.compensate(completed_steps, order_data)
                    return False
            return True
        except Exception as e:
            self.compensate(completed_steps, order_data)
            return False

    def compensate(self, completed_steps, order_data):
        # 역순으로 보상 트랜잭션 실행
        for step_index in reversed(completed_steps):
            self.compensations[step_index](order_data)
```

#### 4.2 Orchestration SAGA

```python
class SagaOrchestrator:
    def __init__(self):
        self.saga_state = {}

    def handle_order_created(self, order_id, order_data):
        self.saga_state[order_id] = {
            'status': 'started',
            'steps_completed': [],
            'order_data': order_data
        }
        # 첫 번째 단계 시작
        self.send_command('inventory-service', 'ReserveItems', order_data)

    def handle_items_reserved(self, order_id, event_data):
        saga = self.saga_state[order_id]
        saga['steps_completed'].append('inventory_reserved')
        # 다음 단계 진행
        self.send_command('payment-service', 'ProcessPayment', saga['order_data'])
```

### 5. 분산 트랜잭션

#### 5.1 2PC (Two-Phase Commit)

```python
class TwoPhaseCommitCoordinator:
    def __init__(self, participants):
        self.participants = participants

    def execute_transaction(self, transaction_data):
        # Phase 1: Prepare
        prepare_results = []
        for participant in self.participants:
            result = participant.prepare(transaction_data)
            prepare_results.append(result)
            if not result['can_commit']:
                # 하나라도 실패하면 전체 중단
                self.send_abort_to_all()
                return False

        # Phase 2: Commit
        try:
            for participant in self.participants:
                participant.commit(transaction_data)
            return True
        except Exception:
            # 커밋 중 실패 시 복구 로직 필요
            self.handle_commit_failure()
            return False

    def send_abort_to_all(self):
        for participant in self.participants:
            participant.abort()
```

#### 5.2 3PC (Three-Phase Commit)

- 2PC의 블로킹 문제를 해결
- Prepare → Pre-commit → Commit 3단계

## 📊 분산환경 성능 고려사항

### 1. CAP 정리 (CAP Theorem)

- **Consistency**: 일관성
- **Availability**: 가용성
- **Partition Tolerance**: 분할 내성

세 가지 중 최대 2가지만 동시에 보장 가능

### 2. BASE vs ACID

#### ACID (전통적 데이터베이스)

- **Atomicity**: 원자성
- **Consistency**: 일관성
- **Isolation**: 격리성
- **Durability**: 지속성

#### BASE (분산 시스템)

- **Basically Available**: 기본적 가용성
- **Soft state**: 유연한 상태
- **Eventually consistent**: 최종 일관성

### 3. 일관성 모델

#### 3.1 Strong Consistency

- 모든 읽기가 최신 쓰기 결과 반환
- 성능 비용이 높음

#### 3.2 Eventual Consistency

- 시간이 지나면 결국 일관성 달성
- 높은 가용성과 성능 제공

#### 3.3 Weak Consistency

- 일관성 보장이 약함
- 실시간 애플리케이션에 적합

## 🛠 실무 적용 사례

### 1. 마이크로서비스에서의 적용

```python
# 분산 락을 이용한 재고 관리
class InventoryService:
    def __init__(self, redis_client):
        self.redis = redis_client

    def reserve_item(self, item_id, quantity):
        lock_key = f"inventory:{item_id}"
        lock = RedisDistributedLock(self.redis, lock_key)

        if lock.acquire():
            try:
                current_stock = self.get_stock(item_id)
                if current_stock >= quantity:
                    self.update_stock(item_id, current_stock - quantity)
                    return {'success': True, 'reserved_quantity': quantity}
                else:
                    return {'success': False, 'reason': 'insufficient_stock'}
            finally:
                lock.release()
        else:
            return {'success': False, 'reason': 'lock_timeout'}
```

### 2. 이벤트 드리븐 아키텍처

```python
# 이벤트 기반 주문 처리
class OrderEventHandler:
    def __init__(self, event_bus):
        self.event_bus = event_bus

    def handle_order_created(self, event):
        order_id = event['order_id']

        # 각 서비스에 이벤트 발행
        self.event_bus.publish('inventory.reserve', {
            'order_id': order_id,
            'items': event['items']
        })

    def handle_inventory_reserved(self, event):
        if event['success']:
            self.event_bus.publish('payment.process', event)
        else:
            self.event_bus.publish('order.failed', event)
```

## 🔍 개인적인 정리 (분산환경)

- 분산환경에서는 **네트워크 지연**과 **부분 실패**를 항상 고려해야 함
- **CAP 정리**를 이해하고 비즈니스 요구사항에 맞는 선택을 해야 함
- **분산 락**은 성능 오버헤드가 있으므로 꼭 필요한 경우에만 사용
- **이벤트 소싱**과 **SAGA 패턴**은 분산 트랜잭션의 좋은 대안
- **최종 일관성**을 받아들일 수 있다면 성능과 가용성을 크게 향상시킬 수 있음
- 실무에서는 **모니터링**과 **관찰가능성**이 더욱 중요해진다

## 📚 참고사항

- 동시성 제어는 ACID 속성 중 Isolation과 밀접한 관련
