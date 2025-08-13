# 해시코드가 배열 인덱스로 변환되는 과정

## 🤔 왜 정리하게 되었나?

해시 테이블을 공부하다 보니 가장 궁금했던 것이 "해시코드가 어떻게 배열 인덱스로 변환되는 것인가?"라는 의문이었다.
특히 다음 코드에서 어떻게 해시코드가 배열의 인덱스가 되는지 명확하게 이해하고 싶었다.

```java
// 해시 방법: "계산"
String name = "김철수";
int index = name.hashCode() % buckets.length;  // 바로 위치 계산!
return buckets[index];  // 바로 접근
```

## 📚 해시코드의 인덱스 변환 과정

### 1. 변환 과정 (3단계)

```java
public class HashToIndexConversion {
    public static void main(String[] args) {
        String key = "김철수";
        int arraySize = 16; // 해시 테이블 배열 크기

        // 1단계: hashCode() 계산
        int hashCode = key.hashCode();
        System.out.println("1. hashCode(): " + hashCode);

        // 2단계: 음수 처리 (해시코드는 음수일 수 있음)
        int absHashCode = Math.abs(hashCode);
        System.out.println("2. Math.abs(): " + absHashCode);

        // 3단계: 배열 크기에 맞게 조정 (모듈러 연산)
        int index = absHashCode % arraySize;
        System.out.println("3. % " + arraySize + ": " + index);

        System.out.println("최종 인덱스: " + index);
        System.out.println("→ buckets[" + index + "]에 저장 또는 검색");
    }
}
```

**출력 예시:**

```text
1. hashCode(): 54620233
2. Math.abs(): 54620233
3. % 16: 9
최종 인덱스: 9
→ buckets[9]에 저장 또는 검색
```

### 2. 다양한 문자열의 인덱스 변환 예시

```java
public class MultipleKeysDemo {
    public static void main(String[] args) {
        String[] keys = {"김철수", "이영희", "박민수", "최지영",
                         "홍길동", "유재석", "강호동", "신동엽"};
        int arraySize = 16;

        System.out.println("키 → 해시코드 → 인덱스 변환 예시");
        System.out.println("===========================");

        for (String key : keys) {
            int hashCode = key.hashCode();
            int index = Math.abs(hashCode) % arraySize;

            System.out.printf("%-6s → %10d → %2d\n", key, hashCode, index);
        }
    }
}
```

**출력 예시:**

```text
키 → 해시코드 → 인덱스 변환 예시
===========================
김철수  →   54620233 →  9
이영희  →   44150089 →  9
박민수  →   47311998 → 14
최지영  →   49302670 → 14
홍길동  →   54740929 →  1
유재석  →   44270785 →  1
강호동  →   43433368 →  8
신동엽  →   46843717 →  5
```

## 🔍 해시코드 변환의 핵심 개념

### 1. 모듈러 연산의 역할

모듈러 연산(`%`)은 **큰 수를 작은 범위로 매핑**하는 데 사용된다.

```java
public class ModuloExplanation {
    public static void main(String[] args) {
        int arraySize = 16;

        System.out.println("모듈러 연산 " + arraySize + "의 결과 패턴:");
        for (int i = 0; i < 32; i++) {
            System.out.println(i + " % " + arraySize + " = " + (i % arraySize));
        }

        System.out.println("\n큰 숫자도 같은 패턴 적용:");
        System.out.println("100 % " + arraySize + " = " + (100 % arraySize));
        System.out.println("116 % " + arraySize + " = " + (116 % arraySize));
        System.out.println("1000000 % " + arraySize + " = " + (1000000 % arraySize));
    }
}
```

**출력 예시:**

```text
모듈러 연산 16의 결과 패턴:
0 % 16 = 0
1 % 16 = 1
...
15 % 16 = 15
16 % 16 = 0
17 % 16 = 1
...
31 % 16 = 15

큰 숫자도 같은 패턴 적용:
100 % 16 = 4
116 % 16 = 4
1000000 % 16 = 0
```

이처럼 모듈러 연산은 어떤 수가 오더라도 **0 ~ (arraySize-1) 범위의 값**으로 매핑해준다.

### 2. 충돌(Collision) 발생

위 예시에서 "김철수"와 "이영희"는 서로 다른 해시코드를 가지지만, 같은 인덱스(9)로 매핑된다. 이것이 **해시 충돌**이다.

```java
public class HashCollisionDemo {
    public static void main(String[] args) {
        String key1 = "김철수";
        String key2 = "이영희";
        int arraySize = 16;

        int hashCode1 = key1.hashCode();
        int hashCode2 = key2.hashCode();

        int index1 = Math.abs(hashCode1) % arraySize;
        int index2 = Math.abs(hashCode2) % arraySize;

        System.out.println(key1 + "의 해시코드: " + hashCode1 + " → 인덱스: " + index1);
        System.out.println(key2 + "의 해시코드: " + hashCode2 + " → 인덱스: " + index2);

        if (index1 == index2) {
            System.out.println("충돌 발생! 두 키가 같은 인덱스에 매핑됨");
        }
    }
}
```

**출력 예시:**

```text
김철수의 해시코드: 54620233 → 인덱스: 9
이영희의 해시코드: 44150089 → 인덱스: 9
충돌 발생! 두 키가 같은 인덱스에 매핑됨
```

## 🔬 충돌 해결 방법

### 1. 체이닝(Chaining)

같은 인덱스에 여러 항목을 연결 리스트로 저장하는 방법이다.

```java
public class ChainingDemo {
    public static void main(String[] args) {
        // 체이닝 방식의 해시 테이블 생성
        ChainedHashTable<String, String> table = new ChainedHashTable<>(16);

        // 충돌이 발생할 키-값 쌍 저장
        table.put("김철수", "개발자");
        table.put("이영희", "디자이너");

        // 값 조회
        System.out.println("김철수 → " + table.get("김철수"));
        System.out.println("이영희 → " + table.get("이영희"));
    }
}

class ChainedHashTable<K, V> {
    private LinkedList<Entry<K, V>>[] buckets;

    @SuppressWarnings("unchecked")
    public ChainedHashTable(int size) {
        buckets = new LinkedList[size];
        for (int i = 0; i < size; i++) {
            buckets[i] = new LinkedList<>();
        }
    }

    public void put(K key, V value) {
        int index = getIndex(key);

        // 기존 키 확인
        for (Entry<K, V> entry : buckets[index]) {
            if (entry.key.equals(key)) {
                entry.value = value; // 값 업데이트
                return;
            }
        }

        // 새 엔트리 추가
        buckets[index].add(new Entry<>(key, value));
        System.out.println("PUT: " + key + " → 인덱스 " + index + "에 저장");

        if (buckets[index].size() > 1) {
            System.out.println("  충돌 발생! 현재 버킷 크기: " + buckets[index].size());
        }
    }

    public V get(K key) {
        int index = getIndex(key);
        System.out.println("GET: " + key + " → 인덱스 " + index + "에서 검색");

        int steps = 0;
        for (Entry<K, V> entry : buckets[index]) {
            steps++;
            if (entry.key.equals(key)) {
                System.out.println("  찾음! " + steps + "번째 항목");
                return entry.value;
            }
        }

        System.out.println("  찾지 못함");
        return null;
    }

    private int getIndex(K key) {
        return Math.abs(key.hashCode()) % buckets.length;
    }

    static class Entry<K, V> {
        K key;
        V value;

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
}
```

**출력 예시:**

```text
PUT: 김철수 → 인덱스 9에 저장
PUT: 이영희 → 인덱스 9에 저장
  충돌 발생! 현재 버킷 크기: 2
GET: 김철수 → 인덱스 9에서 검색
  찾음! 1번째 항목
김철수 → 개발자
GET: 이영희 → 인덱스 9에서 검색
  찾음! 2번째 항목
이영희 → 디자이너
```

### 2. 개방 주소법(Open Addressing)

충돌 발생 시 다른 빈 슬롯을 찾아 저장하는 방법이다.

```java
public class OpenAddressingDemo {
    public static void main(String[] args) {
        // 개방 주소법 해시 테이블 생성
        OpenAddressHashTable<String, String> table =
            new OpenAddressHashTable<>(16);

        // 충돌이 발생할 키-값 쌍 저장
        table.put("김철수", "개발자");
        table.put("이영희", "디자이너");

        // 값 조회
        System.out.println("김철수 → " + table.get("김철수"));
        System.out.println("이영희 → " + table.get("이영희"));
    }
}

class OpenAddressHashTable<K, V> {
    private Entry<K, V>[] buckets;
    private int size;

    @SuppressWarnings("unchecked")
    public OpenAddressHashTable(int capacity) {
        buckets = new Entry[capacity];
        size = 0;
    }

    public void put(K key, V value) {
        if (size >= buckets.length * 0.75) {
            System.out.println("로드 팩터 초과! 리사이징 필요");
            return;
        }

        int index = getIndex(key);
        int originalIndex = index;
        int step = 0;

        // 선형 탐색(Linear Probing)
        while (buckets[index] != null) {
            // 기존 키 업데이트
            if (buckets[index].key.equals(key)) {
                buckets[index].value = value;
                return;
            }

            // 다음 슬롯 탐색
            index = (index + 1) % buckets.length;
            step++;

            // 전체 배열을 다 돌았다면 종료
            if (index == originalIndex) {
                System.out.println("테이블이 가득 참");
                return;
            }
        }

        // 빈 슬롯에 저장
        buckets[index] = new Entry<>(key, value);
        size++;

        System.out.println("PUT: " + key + " → 원래 인덱스 " +
                          originalIndex + " → 최종 인덱스 " + index);
        if (step > 0) {
            System.out.println("  충돌 발생! " + step + "번 이동");
        }
    }

    public V get(K key) {
        int index = getIndex(key);
        int originalIndex = index;
        int step = 0;

        System.out.println("GET: " + key + " → 시작 인덱스 " + index);

        while (buckets[index] != null) {
            if (buckets[index].key.equals(key)) {
                System.out.println("  찾음! " + step + "번 이동 후");
                return buckets[index].value;
            }

            // 다음 슬롯 확인
            index = (index + 1) % buckets.length;
            step++;

            // 전체 배열을 다 돌았거나 빈 슬롯을 만나면 종료
            if (index == originalIndex || buckets[index] == null) {
                break;
            }
        }

        System.out.println("  찾지 못함");
        return null;
    }

    private int getIndex(K key) {
        return Math.abs(key.hashCode()) % buckets.length;
    }

    static class Entry<K, V> {
        K key;
        V value;

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
}
```

**출력 예시:**

```text
PUT: 김철수 → 원래 인덱스 9 → 최종 인덱스 9
PUT: 이영희 → 원래 인덱스 9 → 최종 인덱스 10
  충돌 발생! 1번 이동
GET: 김철수 → 시작 인덱스 9
  찾음! 0번 이동 후
김철수 → 개발자
GET: 이영희 → 시작 인덱스 9
  찾음! 1번 이동 후
이영희 → 디자이너
```

## 💡 해시코드 → 인덱스 변환의 특성

### 1. 균등 분포(Uniform Distribution)

좋은 해시 함수는 키들을 배열 전체에 **균등하게 분포**시켜야 한다.

```java
public class HashDistributionDemo {
    public static void main(String[] args) {
        int arraySize = 16;
        int sampleSize = 100000;
        int[] distribution = new int[arraySize];

        // 랜덤 문자열 생성 및 해시코드 분포 확인
        Random random = new Random();
        for (int i = 0; i < sampleSize; i++) {
            String randomString = "key" + random.nextInt(1000000);
            int index = Math.abs(randomString.hashCode()) % arraySize;
            distribution[index]++;
        }

        System.out.println("인덱스별 항목 수 (총 " + sampleSize + "개):");
        for (int i = 0; i < arraySize; i++) {
            System.out.printf("%2d: %5d (%.2f%%)\n",
                i, distribution[i],
                (distribution[i] * 100.0) / sampleSize);
        }

        // 분포 통계
        int min = Arrays.stream(distribution).min().getAsInt();
        int max = Arrays.stream(distribution).max().getAsInt();
        double avg = Arrays.stream(distribution).average().getAsDouble();

        System.out.println("\n분포 통계:");
        System.out.println("최소값: " + min);
        System.out.println("최대값: " + max);
        System.out.println("평균: " + avg);
        System.out.println("최대/최소 비율: " + (double)max/min);
    }
}
```

**출력 예시:**

```text
인덱스별 항목 수 (총 100000개):
 0:  6253 (6.25%)
 1:  6198 (6.20%)
...
15:  6285 (6.29%)

분포 통계:
최소값: 6152
최대값: 6339
평균: 6250.0
최대/최소 비율: 1.0304
```

### 2. 배열 크기와 성능

배열 크기는 성능에 중요한 영향을 미친다. 특히 **소수(prime number)** 크기의 배열이 충돌을 줄이는 데 효과적이다.

```java
public class ArraySizeDemo {
    public static void main(String[] args) {
        int[] sizes = {16, 17, 32, 31};
        int sampleSize = 10000;

        for (int arraySize : sizes) {
            System.out.println("\n배열 크기 " + arraySize + "에 대한 분포:");
            testDistribution(arraySize, sampleSize);
        }
    }

    private static void testDistribution(int arraySize, int sampleSize) {
        int[] distribution = new int[arraySize];

        // 균일한 테스트 데이터
        for (int i = 0; i < sampleSize; i++) {
            String key = "key" + i;
            int index = Math.abs(key.hashCode()) % arraySize;
            distribution[index]++;
        }

        // 분포 통계
        int min = Arrays.stream(distribution).min().getAsInt();
        int max = Arrays.stream(distribution).max().getAsInt();
        double avg = Arrays.stream(distribution).average().getAsDouble();

        System.out.println("최소/최대/평균: " + min + "/" + max + "/" + avg);
        System.out.println("최대/최소 비율: " + (double)max/min);

        // 빈 버킷과 과밀 버킷 수
        long emptyBuckets = Arrays.stream(distribution).filter(c -> c == 0).count();
        long heavyBuckets = Arrays.stream(distribution).filter(c -> c > avg * 1.5).count();

        System.out.println("빈 버킷 수: " + emptyBuckets);
        System.out.println("과밀 버킷 수: " + heavyBuckets);
    }
}
```

**출력 예시:**

```text
배열 크기 16에 대한 분포:
최소/최대/평균: 583/652/625.0
최대/최소 비율: 1.1184
빈 버킷 수: 0
과밀 버킷 수: 0

배열 크기 17에 대한 분포:
최소/최대/평균: 567/612/588.2
최대/최소 비율: 1.0793
빈 버킷 수: 0
과밀 버킷 수: 0

...
```

## 🎯 실제 구현에서의 최적화

### 1. Java의 HashMap 구현

Java의 HashMap은 다양한 최적화 기법을 사용한다:

```java
public class HashMapInternals {
    public static void main(String[] args) {
        System.out.println("Java HashMap의 해시코드 처리 과정:");

        String key = "김철수";

        // 1. 원래 해시코드
        int hashCode = key.hashCode();
        System.out.println("1. 원래 해시코드: " + hashCode);

        // 2. 해시코드 스프레딩 (Java 8 이후)
        int spreadHash = hashCode ^ (hashCode >>> 16);
        System.out.println("2. 스프레딩 해시코드: " + spreadHash);

        // 3. 인덱스 계산 (가정: 배열 크기 16)
        int arraySize = 16;
        int index = spreadHash & (arraySize - 1);
        System.out.println("3. 최종 인덱스: " + index);

        // 참고: 비트 연산 대신 모듈러 연산 사용 시
        int modIndex = Math.abs(hashCode) % arraySize;
        System.out.println("(참고) 모듈러 연산 인덱스: " + modIndex);
    }
}
```

**출력 예시:**

```text
Java HashMap의 해시코드 처리 과정:
1. 원래 해시코드: 54620233
2. 스프레딩 해시코드: 54620001
3. 최종 인덱스: 1
(참고) 모듈러 연산 인덱스: 9
```

### 2. 비트 연산을 이용한 최적화

모듈러 연산 대신 비트 AND 연산을 사용하면 더 빠르게 인덱스를 계산할 수 있다.

```java
public class BitOperationDemo {
    public static void main(String[] args) {
        int hashCode = 54620233;

        // 다양한 배열 크기에 대한 비교
        testBitOperation(hashCode, 16);
        testBitOperation(hashCode, 32);
        testBitOperation(hashCode, 64);

        // 주의: 비트 연산은 배열 크기가 2의 제곱일 때만 작동
        System.out.println("\n[주의] 비트 연산은 2의 제곱 크기에서만 유효");
        testBitOperation(hashCode, 17); // 2의 제곱이 아님 - 결과가 달라짐!
        testBitOperation(hashCode, 31); // 2의 제곱이 아님 - 결과가 달라짐!
    }

    private static void testBitOperation(int hashCode, int arraySize) {
        // 모듈러 연산
        int modIndex = Math.abs(hashCode) % arraySize;

        // 비트 연산 (배열 크기가 2의 제곱인 경우만 유효)
        int bitIndex = hashCode & (arraySize - 1);

        System.out.println("배열 크기 " + arraySize + ":");
        System.out.println("  모듈러 연산: " + modIndex);
        System.out.println("  비트 연산: " + bitIndex);
        System.out.println("  일치 여부: " + (modIndex == bitIndex));
    }
}
```

**출력 예시:**

```text
배열 크기 16:
  모듈러 연산: 9
  비트 연산: 9
  일치 여부: true

...

[주의] 비트 연산은 2의 제곱 크기에서만 유효
배열 크기 17:
  모듈러 연산: 12
  비트 연산: 9
  일치 여부: false
```

## 🧠 해시코드 → 인덱스 변환에 대한 깊은 이해

### 기본 원리 정리

1. **해시코드 계산**: 객체의 내용에 기반한 정수값 생성
2. **인덱스 변환**: 해시코드를 배열 범위의 인덱스로 매핑
3. **충돌 처리**: 같은 인덱스에 여러 항목이 매핑될 때 해결

### 핵심 통찰

1. **직접 계산의 혁신**: "찾기"에서 "계산"으로의 패러다임 전환
2. **균형 잡힌 절충**: 메모리 사용량과 검색 속도 사이의 최적화
3. **확장성**: 데이터 크기에 관계없이 평균 O(1) 접근 시간 유지

해시코드가 인덱스로 변환되는 이 메커니즘은 컴퓨터 과학에서 가장 아름다운 아이디어 중 하나다.
거대한 데이터세트에서도 "바로 거기에 있을 겁니다"라고 거의 정확하게 말할 수 있는 기술이기 때문이다.
