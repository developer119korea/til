# 오버셀링(Overselling) 방지 기술

## 개요

e-commerce 시스템에서 **오버셀링(Overselling)**은 실제 재고보다 더 많은 상품이 판매되는 현상으로, 주로 동시성 문제로 인해 발생합니다.

### 문제 상황

```text
실제 재고: 1개
동시 접근: 사용자 A, B가 동시에 주문 시도

1. A가 재고 확인 → 1개 존재 ✓
2. B가 재고 확인 → 1개 존재 ✓
3. A가 주문 완료 → 재고 0개로 업데이트
4. B가 주문 완료 → 재고 -1개로 업데이트 ❌
```

### 근본 원인

- **Race Condition**: 여러 프로세스가 동시에 같은 자원에 접근
- **원자성 부족**: 재고 확인과 업데이트가 분리된 작업으로 처리
- **동시성 제어 미흡**: 적절한 잠금 메커니즘 부재

---

## 해결 방법

### 1. 트랜잭션(Transaction)

**개념**: 데이터베이스의 ACID 속성을 활용하여 일련의 작업을 원자적으로 처리

**장점**:

- 데이터 일관성 보장
- 실패 시 자동 롤백
- 구현이 비교적 간단

**단점**:

- 트랜잭션 격리 수준에 따른 성능 영향
- 데드락 발생 가능성

```sql
BEGIN TRANSACTION;
SELECT stock FROM products WHERE id = 1;
-- 재고가 충분한 경우에만 업데이트
UPDATE products SET stock = stock - 1 WHERE id = 1 AND stock > 0;
COMMIT;
```

### 2. 비관적 잠금(Pessimistic Locking)

**개념**: 데이터를 읽는 시점에 잠금을 걸어 다른 트랜잭션의 접근을 차단

**장점**:

- 확실한 동시성 제어
- 충돌 방지 보장

**단점**:

- 성능 저하 (대기 시간 증가)
- 데드락 위험
- 확장성 제한

```sql
-- 행 잠금으로 다른 트랜잭션 차단
SELECT stock FROM products WHERE id = 1 FOR UPDATE;
UPDATE products SET stock = stock - 1 WHERE id = 1;
```

### 3. 낙관적 잠금(Optimistic Locking)

**개념**: 버전 필드를 사용하여 업데이트 시점에 충돌을 감지

**장점**:

- 높은 동시성 처리 가능
- 데드락 없음
- 확장성 우수

**단점**:

- 충돌 시 재시도 로직 필요
- 충돌이 빈번한 경우 비효율적

```sql
-- 버전을 포함한 조건부 업데이트
UPDATE products
SET stock = stock - 1, version = version + 1
WHERE id = 1 AND version = @current_version AND stock > 0;

-- 영향받은 행이 0이면 충돌 발생으로 판단
```

### 4. 분산 락(Distributed Locking)

**개념**: 여러 서버 간 동기화를 위해 외부 저장소(Redis, ZooKeeper)를 활용

**적용 상황**:

- 마이크로서비스 아키텍처
- 다중 서버 환경
- 클러스터링된 애플리케이션

**장점**:

- 분산 환경에서 일관성 보장
- 확장 가능한 솔루션

**단점**:

- 외부 의존성 추가
- 네트워크 지연 가능성
- 복잡한 구현

```javascript
// Redis를 이용한 분산 락 예시
const lockKey = `lock:product:${productId}`;
const acquired = await redis.set(lockKey, "locked", "PX", 10000, "NX");

if (acquired) {
  try {
    // 재고 처리 로직
    await processOrder(productId, quantity);
  } finally {
    await redis.del(lockKey); // 락 해제
  }
}
```

### 5. 큐(Queue) 기반 처리

**개념**: 주문을 큐에 적재하여 순차적으로 처리

**장점**:

- 완전한 순서 보장
- 시스템 부하 분산
- 확장성과 안정성

**단점**:

- 즉시 처리 불가 (비동기)
- 큐 인프라 필요
- 복잡한 에러 처리

```javascript
// 메시지 큐를 활용한 순차 처리
await orderQueue.publish({
  productId,
  userId,
  quantity,
  timestamp: Date.now(),
});

// 워커에서 순차 처리
orderQueue.process(async (job) => {
  const { productId, quantity } = job.data;
  await processOrderSequentially(productId, quantity);
});
```

### 6. 원자적 연산(Atomic Operations)

**개념**: 데이터베이스가 제공하는 원자적 연산을 활용

**장점**:

- 단순하고 효율적
- 데이터베이스 레벨에서 보장
- 높은 성능

**단점**:

- 복잡한 비즈니스 로직 처리 제한
- 데이터베이스 종속적

```sql
-- 조건부 원자적 업데이트
UPDATE products
SET stock = stock - @quantity
WHERE id = @product_id AND stock >= @quantity;

-- 영향받은 행 수로 성공/실패 판단
SELECT ROW_COUNT() as affected_rows;
```

---

## 방법별 비교

| 방법        | 성능     | 복잡도 | 확장성   | 일관성 | 권장 상황           |
| ----------- | -------- | ------ | -------- | ------ | ------------------- |
| 트랜잭션    | 보통     | 낮음   | 보통     | 높음   | 단일 DB 환경        |
| 비관적 잠금 | 낮음     | 낮음   | 낮음     | 높음   | 동시 접근 적은 경우 |
| 낙관적 잠금 | 높음     | 중간   | 높음     | 높음   | 동시 접근 많은 경우 |
| 분산 락     | 보통     | 높음   | 높음     | 높음   | 분산 환경           |
| 큐 처리     | 높음     | 높음   | 매우높음 | 높음   | 대용량 처리         |
| 원자적 연산 | 매우높음 | 낮음   | 높음     | 높음   | 단순한 재고 관리    |

---

## 실무 권장사항

### 1단계: 단순한 원자적 연산

```sql
UPDATE products
SET stock = stock - ?
WHERE id = ? AND stock >= ?;
```

### 2단계: 낙관적 잠금 추가

- 빈번한 충돌 발생 시
- 버전 관리가 필요한 경우

### 2-1단계: 트랜잭션 + 비관적 잠금 (가장 간단하고 효과적)

**개념**: 트랜잭션 내에서 비관적 잠금을 사용하여 확실한 동시성 제어를 보장하는 방법

**적용 상황**:

- 중간 규모의 동시 접근
- 확실한 데이터 일관성이 필요한 경우
- 구현 복잡도를 낮추면서 안정성을 확보하고 싶은 경우

**장점**:

- 구현이 간단하고 직관적
- 100% 확실한 동시성 제어
- 별도 인프라 없이 DB만으로 해결
- 디버깅과 트러블슈팅이 용이

**구현 예시**:

```sql
-- SQL 예시
BEGIN TRANSACTION;
SELECT stock FROM products WHERE id = ? FOR UPDATE;
-- 재고 확인 후 충분한 경우에만 업데이트
UPDATE products SET stock = stock - ? WHERE id = ? AND stock >= ?;
COMMIT;
```

```javascript
// Node.js + TypeORM 예시
async function purchaseProduct(productId: number, quantity: number) {
  return await this.dataSource.transaction(async (manager) => {
    // 비관적 잠금으로 상품 조회
    const product = await manager.findOne(Product, {
      where: { id: productId },
      lock: { mode: "pessimistic_write" },
    });

    if (!product || product.stock < quantity) {
      throw new Error("재고 부족");
    }

    // 재고 차감
    product.stock -= quantity;
    await manager.save(product);

    return product;
  });
}
```

```java
// Spring Boot + JPA 예시
@Transactional
public void purchaseProduct(Long productId, int quantity) {
    Product product = productRepository
        .findByIdWithLock(productId)
        .orElseThrow(() -> new ProductNotFoundException());

    if (product.getStock() < quantity) {
        throw new InsufficientStockException();
    }

    product.decreaseStock(quantity);
    productRepository.save(product);
}

// Repository에서
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT p FROM Product p WHERE p.id = :id")
Optional<Product> findByIdWithLock(@Param("id") Long id);
```

**주의사항**:

- 트랜잭션 시간을 최소화 (긴 처리 로직은 트랜잭션 외부에서)
- 데드락 방지를 위한 일관된 락 순서 유지
- 커넥션 풀 크기 고려 (락 대기로 인한 커넥션 고갈 방지)

### 3단계: 분산 환경 고려

- 마이크로서비스 도입 시
- 다중 서버 환경

### 4단계: 큐 기반 아키텍처

- 대용량 트래픽 처리
- 복잡한 주문 프로세스

---

## 추가 고려사항

### 재시도 전략

- **지수 백오프(Exponential Backoff)**: 재시도 간격을 점진적으로 증가
- **최대 재시도 횟수 제한**: 무한 루프 방지
- **사용자 경험**: 적절한 로딩 상태 및 에러 메시지

### 모니터링 및 알림

- 오버셀링 발생 감지
- 재고 부족 임계값 알림
- 성능 메트릭 추적

### 비즈니스 로직 고려

- 예약 주문 시스템
- 재고 안전 마진 설정
- 취소/반품 처리
