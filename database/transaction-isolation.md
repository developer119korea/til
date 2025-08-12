# 트랜잭션 격리 수준

## 기본 개념 한 줄씩

- 격리 수준은 트랜잭션 간 간섭을 얼마나 막을지 결정한다(강할수록 안전하지만 느려짐).
- 표준 4단계: Read Uncommitted < Read Committed < Repeatable Read < Serializable.
- 이상 현상 키워드: Dirty Read, Non-Repeatable Read, Phantom Read, Lost Update.

## 수준

1. Read Uncommitted: 커밋 전 값도 읽음(Dirty Read까지 허용). 거의 안 쓴다.
1. Read Committed: 커밋된 것만 읽음. 같은 행을 다시 읽으면 바뀔 수 있음(Non-Repeatable), 조건 결과 개수도 바뀔 수 있음(Phantom).
1. Repeatable Read: 같은 “행”을 재조회하면 값이 안 바뀌도록. 구현체에 따라 Phantom 다룸.
1. Serializable: 논리적으로 직렬 실행과 동일한 결과. 충돌이 잦으면 롤백이 늘어남.

## 구현 메모

- MVCC로 “읽기”는 보통 락을 잡지 않음. 시점 스냅샷을 본다.
- MySQL(InnoDB): 기본 RR, next-key/gap lock으로 범위 쓰기 충돌을 제어.
- Postgres: 기본 RC, RR은 스냅샷 격리(SI), SZ는 SSI(충돌 시 트랜잭션 abort).

---

## 실험 준비 (공통 스키마)

```sql
-- 공통 테이블
CREATE TABLE products (
  id    BIGINT PRIMARY KEY,
  name  TEXT,
  stock INT
);

INSERT INTO products (id, name, stock) VALUES (1, 'item', 1)
ON CONFLICT (id) DO UPDATE SET stock = EXCLUDED.stock; -- Postgres
-- MySQL에서는 INSERT ... ON DUPLICATE KEY UPDATE 사용
```

테스트는 세션 A, 세션 B 두 창으로 진행(한쪽에서 BEGIN 후 커밋 지연).

---

## MySQL(InnoDB) 실험

- 기본 격리 수준 확인

```sql
SELECT @@transaction_isolation; -- 보통 'REPEATABLE-READ'
```

- RC에서 Non-Repeatable/Phantom 체크

```sql
-- 세션 A
SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED;
START TRANSACTION;
SELECT stock FROM products WHERE id = 1; -- 예: 1

-- 세션 B (별도 창)
START TRANSACTION;
UPDATE products SET stock = 0 WHERE id = 1;
COMMIT;

-- 세션 A
SELECT stock FROM products WHERE id = 1; -- 0으로 바뀜 (Non-Repeatable)
COMMIT;
```

- RR에서 같은 행 재조회 안정성

```sql
-- 세션 A
SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ;
START TRANSACTION;
SELECT stock FROM products WHERE id = 1; -- 예: 1

-- 세션 B
START TRANSACTION;
UPDATE products SET stock = 0 WHERE id = 1; COMMIT;

-- 세션 A: 같은 쿼리 재실행 시 스냅샷 기준 값 그대로(1) 보일 수 있음
SELECT stock FROM products WHERE id = 1; -- 여전히 1 (스냅샷)
COMMIT;
```

- 범위 + 잠금 읽기에서의 next-key/gap lock

```sql
-- 세션 A
START TRANSACTION;
SELECT * FROM products WHERE id BETWEEN 1 AND 10 FOR UPDATE; -- 범위 잠금(넥스트키)

-- 세션 B: 해당 범위에 INSERT/UPDATE 시도 시 대기/충돌 가능
INSERT INTO products (id, name, stock) VALUES (2, 'item2', 5); -- 범위에 걸리면 대기
```

메모: InnoDB RR + 일반 SELECT는 스냅샷이어서 Phantom 체감이 적고, 잠금 읽기(SELECT ... FOR UPDATE)/범위 갱신에서 next-key가 작동.

---

## Postgres 실험

- 기본 격리 수준 확인

```sql
SHOW default_transaction_isolation; -- 보통 'read committed'
```

- RC에서 Non-Repeatable

```sql
-- 세션 A
BEGIN; -- read committed
SELECT stock FROM products WHERE id = 1; -- 예: 1

-- 세션 B
BEGIN;
UPDATE products SET stock = 0 WHERE id = 1; COMMIT;

-- 세션 A: 다시 읽으면 바뀐 값
SELECT stock FROM products WHERE id = 1; -- 0 (Non-Repeatable)
COMMIT;
```

- RR(=SI)에서 재조회 안정성과 Write Skew 주의

```sql
-- 세션 A
SET TRANSACTION ISOLATION LEVEL REPEATABLE READ; -- 스냅샷 고정
BEGIN;
SELECT stock FROM products WHERE id = 1; -- 예: 1

-- 세션 B (같이 RR)
SET TRANSACTION ISOLATION LEVEL REPEATABLE READ;
BEGIN;
SELECT stock FROM products WHERE id = 1; -- 1 (각자 스냅샷)
UPDATE products SET stock = stock - 1 WHERE id = 1 AND stock > 0; -- 조건 패스, 0으로
COMMIT;

-- 세션 A: 여전히 스냅샷은 1이라 조건 통과 가능 -> 커밋 시 충돌 없이 -1 더해질 수 있음
UPDATE products SET stock = stock - 1 WHERE id = 1 AND stock > 0; -- 실제로는 음수 방지 로직/제약 필요
COMMIT; -- SI에선 이런 패턴(Write Skew)이 가능
```

- Serializable(SSI)에서 충돌 시 abort

```sql
-- 두 세션이 같은 조건을 기반으로 서로 모순되는 갱신을 하려 하면
SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;
BEGIN;
-- ... 갱신들 ...
COMMIT; -- 한쪽이 serialization_failure로 롤백될 수 있음
```

메모: Postgres RR은 스냅샷 격리로 Non-Repeatable/Phantom을 체감하기 어렵지만, Write Skew 패턴은 허용. 확실히 막으려면 고유 제약/체크 제약/직렬화 사용.

---

## 내가 기억할 것들

- “읽기 일관성”은 스냅샷 시점에 좌우됨(각 DB의 MVCC 구현 차이 체감).
- Lost Update는 격리 수준만으로 항상 막히지 않는다: 버전 컬럼(낙관적 락), SELECT ... FOR UPDATE, 고유/체크 제약으로 보강.
- MySQL RR은 next-key/gap lock으로 범위 충돌을 잘 막음. Postgres는 SSI에서 충돌 시 abort.

## 참고 명령 모음

```sql
-- MySQL
SELECT @@transaction_isolation;
SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED;
SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ;
SET SESSION TRANSACTION ISOLATION LEVEL SERIALIZABLE;

-- Postgres
SHOW default_transaction_isolation;
SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
SET TRANSACTION ISOLATION LEVEL REPEATABLE READ;
SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;
```

---

## Nest.js + Knex + Postgres

- 의도: Nest 환경에서 Knex로 Postgres를 쓸 때 트랜잭션/격리/락/재시도 실전 패턴만 메모.

### 1) Knex 초기화 (DI로 주입)

```ts
// knex.ts
import knex, { Knex } from "knex";

export const createKnex = (): Knex =>
  knex({
    client: "pg",
    connection: process.env.DATABASE_URL ?? {
      host: "127.0.0.1",
      port: 5432,
      user: "postgres",
      password: "postgres",
      database: "app",
    },
    pool: { min: 2, max: 10 },
  });

// app.module.ts (선호 DI 방식대로 provider 등록)
import { Module } from "@nestjs/common";
import { createKnex } from "./knex";

@Module({
  providers: [
    {
      provide: "KNEX",
      useFactory: () => createKnex(),
    },
  ],
  exports: ["KNEX"],
})
export class DatabaseModule {}
```

### 2) 트랜잭션 + 격리 수준 설정

```ts
// service.ts
import { Inject, Injectable } from "@nestjs/common";
import type { Knex } from "knex";

@Injectable()
export class OrderService {
  constructor(@Inject("KNEX") private readonly db: Knex) {}

  async placeOrder(orderId: number) {
    return this.db.transaction(async (trx) => {
      // 트랜잭션 시작 직후에 격리수준 설정 (아무 SELECT 전에)
      await trx.raw("SET TRANSACTION ISOLATION LEVEL REPEATABLE READ");

      // 재고 잠금 후 차감 (행 잠금)
      const row = await trx("products")
        .where({ id: 1 })
        .forUpdate() // SELECT ... FOR UPDATE
        .first();

      if (!row || row.stock <= 0) throw new Error("OUT_OF_STOCK");

      await trx("products")
        .where({ id: 1 })
        .update({ stock: trx.raw("stock - 1") });

      await trx("orders").insert({ id: orderId, product_id: 1 });
    });
  }
}
```

메모: Postgres에서 격리 수준은 트랜잭션마다 `SET TRANSACTION ...`으로 지정. 직렬화가 필요하면 `SERIALIZABLE`로 바꾸고 재시도 로직을 반드시 둔다.

### 3) 직렬화 실패/데드락 재시도 패턴

```ts
// util/retry.ts
export async function withTxRetry<T>(
  db: import("knex").Knex,
  fn: (trx: import("knex").Knex.Transaction) => Promise<T>,
  maxRetries = 3
): Promise<T> {
  let attempt = 0;
  // Postgres 오류 코드: 40001(serialization_failure), 40P01(deadlock_detected)
  const RETRY_CODES = new Set(["40001", "40P01"]);

  while (true) {
    try {
      return await db.transaction(async (trx) => {
        await trx.raw("SET TRANSACTION ISOLATION LEVEL SERIALIZABLE");
        return fn(trx);
      });
    } catch (e: any) {
      const code = e?.code ?? e?.original?.code;
      if (RETRY_CODES.has(code) && attempt < maxRetries) {
        attempt++;
        // 간단한 지수 백오프
        const backoff = 50 * Math.pow(2, attempt);
        await new Promise((r) => setTimeout(r, backoff));
        continue;
      }
      throw e;
    }
  }
}
```

사용 예시:

```ts
await withTxRetry(this.db, async (trx) => {
  // 직렬화 보장 구간 (충돌 시 withTxRetry가 알아서 재시도)
  const p = await trx("products").where({ id: 1 }).forUpdate().first();
  if (!p || p.stock <= 0) throw new Error("OUT_OF_STOCK");
  await trx("products")
    .where({ id: 1 })
    .update({ stock: trx.raw("stock - 1") });
});
```

### 4) SKIP LOCKED로 작업 큐 패턴

```ts
// 대기 없이 잡아갈 수 있는 아이템만 즉시 가져오기
const job = await this.db("jobs")
  .where({ status: "READY" })
  .forUpdate()
  .skipLocked() // SELECT ... FOR UPDATE SKIP LOCKED
  .first();

if (job) {
  // 처리 후 완료/실패 상태 업데이트
}
```

메모: 경쟁 작업이 이미 같은 행을 잠갔다면 SKIP LOCKED가 그 행을 건너뛰어 워커들이 병렬로 진행 가능.

### 5) 낙관적 락(버전 컬럼)으로 Lost Update 방지

```ts
// 테이블에 version INT 컬럼 추가 가정
const updated = await this.db("products")
  .where({ id: 1, version: prevVersion })
  .update({ stock: newStock, version: this.db.raw("?? + 1", ["version"]) });

if (updated === 0) {
  // 다른 트랜잭션이 선반영 => 재시도 or 에러 처리
}
```

### 6) 어드바이저리 락으로 전역 임계영역 만들기

```ts
await this.db.transaction(async (trx) => {
  const key = 42; // 64bit 키 구성 권장 (예: appId, entityId 조합)
  const { rows } = await trx.raw("SELECT pg_try_advisory_lock(?) AS locked", [
    key,
  ]);
  if (!rows?.[0]?.locked) throw new Error("LOCK_BUSY");
  try {
    // 전역 단일 실행 보장 구간
  } finally {
    await trx.raw("SELECT pg_advisory_unlock(?)", [key]);
  }
});
```

메모: 행 잠금이 아닌 논리적 락. 잘못 쓰면 교착/영구 점유 가능성 있으니 반드시 unlock 보장.

### 7) NOWAIT로 즉시 실패

```ts
// 바로 획득 실패시키고 싶을 때 (애플리케이션 레벨에서 폴백 처리)
await this.db.transaction(async (trx) => {
  try {
    await trx.raw("SELECT * FROM products WHERE id = ? FOR UPDATE NOWAIT", [1]);
  } catch (e: any) {
    if (e?.code === "55P03") {
      // lock_not_available
      // 즉시 폴백
    } else {
      throw e;
    }
  }
});
```

요약 메모:

- 트랜잭션 안에서 가장 먼저 격리수준을 설정하고 쿼리를 시작.
- 직렬화가 필요하면 40001/40P01 재시도를 기본으로 둔다.
- 행 레벨은 forUpdate()/skipLocked()/nowait, 전역 임계영역은 advisory lock.
- Lost Update는 낙관적 락(버전 컬럼)이나 고유/체크 제약으로 보강.
