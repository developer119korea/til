# RDBMS 동시성 제어 (Concurrency Control)

## 📚 학습 내용 정리

### 1. 동시성 제어란?

## 🔍 개인적인 정리

- 동시성 제어는 결국 **데이터 정합성**과 **성능** 사이의 균형을 맞추는 것
- 각 프로젝트의 **비즈니스 요구사항**에 따라 적절한 수준을 선택하는 것이 중요
- 이론만으로는 부족하고, 실제 **모니터링**과 **성능 측정**을 통해 최적화해야 함
- ACID 속성 중 **Isolation**과 가장 밀접하게 연관되어 있다는 점을 항상 기억하자 트랜잭션이 동시에 실행될 때 데이터의 일관성과 격리성을 보장하는 기법
- 동시 실행으로 인한 문제점들을 해결하여 ACID 속성을 보장

### 2. 동시성 문제점들

#### 2.1 Dirty Read (더티 리드)

```sql
-- Transaction A가 데이터를 수정했지만 아직 커밋하지 않은 상태에서
-- Transaction B가 해당 데이터를 읽는 경우
```

#### 2.2 Non-Repeatable Read (반복 불가능한 읽기)

```sql
-- Transaction A가 같은 쿼리를 두 번 실행했을 때
-- 다른 결과가 나오는 경우
```

#### 2.3 Phantom Read (팬텀 리드)

```sql
-- Transaction A가 범위 검색을 두 번 실행했을 때
-- 새로운 행이 추가되어 다른 결과가 나오는 경우
```

## 🛠 동시성 제어 방법

### 1. 격리 수준 (Isolation Level)

#### 1.1 READ UNCOMMITTED

- 가장 낮은 격리 수준
- 모든 동시성 문제 발생 가능
- 성능이 가장 좋음

#### 1.2 READ COMMITTED

- 커밋된 데이터만 읽기 가능
- Dirty Read 방지
- 대부분의 DBMS 기본값

#### 1.3 REPEATABLE READ

- 트랜잭션 내에서 같은 데이터 반복 읽기 보장
- Non-Repeatable Read 방지
- MySQL InnoDB 기본값

#### 1.4 SERIALIZABLE

- 가장 높은 격리 수준
- 모든 동시성 문제 해결
- 성능이 가장 낮음

```sql
-- 격리 수준 설정 예시
SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
```

### 2. 락 (Lock) 메커니즘

#### 2.1 Shared Lock (S-Lock, 공유 락)

- 읽기 전용 락
- 여러 트랜잭션이 동시에 획득 가능
- 쓰기 락과는 배타적

#### 2.2 Exclusive Lock (X-Lock, 배타 락)

- 읽기/쓰기 모두 가능한 락
- 한 번에 하나의 트랜잭션만 획득 가능
- 다른 모든 락과 배타적

#### 2.3 Intent Lock (의도 락)

- 하위 레벨에서 락을 획득할 의도를 표시
- 계층적 락킹에서 사용

### 3. 락의 범위 (Lock Granularity)

#### 3.1 Database Level Lock

- 전체 데이터베이스 단위

#### 3.2 Table Level Lock

- 테이블 단위 락

```sql
LOCK TABLES users READ;
LOCK TABLES users WRITE;
```

#### 3.3 Page Level Lock

- 페이지(블록) 단위 락

#### 3.4 Row Level Lock

- 행 단위 락 (가장 세밀한 제어)

```sql
-- MySQL에서 행 레벨 락 예시
SELECT * FROM users WHERE id = 1 FOR UPDATE;
```

### 4. 다중 버전 동시성 제어 (MVCC)

#### 4.1 MVCC 개념

- Multiple Version Concurrency Control
- 데이터의 여러 버전을 유지하여 동시성 향상
- 읽기와 쓰기 간의 블로킹 최소화

#### 4.2 MVCC 장점

- 읽기 작업이 쓰기 작업을 블로킹하지 않음
- 쓰기 작업이 읽기 작업을 블로킹하지 않음
- 높은 동시성 달성

#### 4.3 MVCC 구현 예시

```sql
-- PostgreSQL의 트랜잭션 ID를 이용한 MVCC
BEGIN;
SELECT txid_current(); -- 현재 트랜잭션 ID 확인
UPDATE users SET name = 'New Name' WHERE id = 1;
COMMIT;
```

## 🔧 실무 적용 방안

### 1. 적절한 격리 수준 선택

```sql
-- 일반적인 웹 애플리케이션: READ COMMITTED
SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED;

-- 금융 시스템 등 데이터 정합성이 중요한 경우: REPEATABLE READ 또는 SERIALIZABLE
SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ;
```

### 2. 명시적 락 사용

```sql
-- 중요한 비즈니스 로직에서 명시적 락 사용
BEGIN;
SELECT balance FROM accounts WHERE id = 1 FOR UPDATE;
-- 비즈니스 로직 수행
UPDATE accounts SET balance = balance - 100 WHERE id = 1;
COMMIT;
```

### 3. 데드락 방지

```sql
-- 항상 같은 순서로 리소스에 접근
-- 트랜잭션 시간 최소화
-- 적절한 인덱스 사용으로 락 범위 최소화
```

## 📊 성능 고려사항

### 1. 락 경합 최소화

- 트랜잭션 시간 단축
- 적절한 인덱스 설계
- 배치 처리 시간 분산

### 2. 격리 수준과 성능 트레이드오프

- 높은 격리 수준 = 높은 일관성, 낮은 성능
- 낮은 격리 수준 = 낮은 일관성, 높은 성능

## 📚 참고사항

- 동시성 제어는 acid 속성 중 isolation과 밀접한 관련
- 비즈니스 요구사항에 따라 적절한 수준의 동시성 제어 선택 필요
- 성능과 일관성 간의 균형점 찾기가 중요
