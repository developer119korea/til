# 해시 알고리즘 심화 이해

## 🤔 왜 정리하게 되었나?

equals()와 hashCode()를 공부하다 보니, 해시가 정확히 어떤 원리로 작동하는지, 그리고 왜 이런 방식을 선택하게 되었는지 더 깊이 알고 싶어졌다.
단순히 "성능이 좋다"는 것을 넘어서, 해시 테이블이 어떻게 O(1) 성능을 달성하는지, 그리고 역사적으로 어떻게 발전해왔는지 정리해보았다.

## 🏛️ hashCode를 사용하게 된 근본적인 이유

### 문제 상황: 데이터를 어떻게 빠르게 찾을 것인가?

```java
// 상황: 100만 명의 회원 정보에서 특정 회원을 찾아야 한다
class MemberDatabase {
    private List<Member> members = new ArrayList<>();

    // 방법 1: 순차 탐색 - O(n)
    public Member findByNameLinear(String name) {
        for (Member member : members) {
            if (member.getName().equals(name)) {
                return member;
            }
        }
        return null; // 100만 번 비교할 수도 있음!
    }

    // 방법 2: 이진 탐색 - O(log n) - 하지만 정렬이 필요
    public Member findByNameBinary(String name) {
        // 정렬된 상태여야 함
        Collections.sort(members, Comparator.comparing(Member::getName));
        // 이진 탐색 구현...
        return null; // 20번 정도 비교로 충분
    }
}
```

**문제점:**

- 순차 탐색: 너무 느림 (최악의 경우 100만 번 비교)
- 이진 탐색: 빠르지만 정렬 유지가 어려움 (삽입/삭제 시 정렬 깨짐)

### 해시의 등장: O(1) 평균 시간 복잡도

```java
// 해시 테이블의 기본 아이디어
public class SimpleHashMap<K, V> {
    private static final int DEFAULT_SIZE = 16;
    private Entry<K, V>[] buckets;

    @SuppressWarnings("unchecked")
    public SimpleHashMap() {
        buckets = new Entry[DEFAULT_SIZE];
    }

    public V get(K key) {
        // 핵심: 해시 함수로 바로 위치 계산!
        int index = getHashIndex(key);

        Entry<K, V> entry = buckets[index];
        while (entry != null) {
            if (entry.key.equals(key)) {
                return entry.value; // 대부분의 경우 1-2번 비교로 끝!
            }
            entry = entry.next;
        }
        return null;
    }

    private int getHashIndex(K key) {
        return Math.abs(key.hashCode()) % buckets.length;
    }

    // 링크드 리스트로 충돌 처리
    private static class Entry<K, V> {
        K key;
        V value;
        Entry<K, V> next;

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
}
```

## 🚀 해시의 혁신적인 아이디어

### 1. 직접 주소 계산

```java
// 전통적인 방법: "찾기"
String name = "김철수";
for (int i = 0; i < members.length; i++) {  // 하나씩 확인
    if (members[i].getName().equals(name)) {
        return members[i];
    }
}

// 해시 방법: "계산"
String name = "김철수";
int index = name.hashCode() % buckets.length;  // 바로 위치 계산!
return buckets[index];  // 바로 접근
```

### 2. 실제 성능 차이 체험

```java
public class HashPerformanceDemo {
    public static void main(String[] args) {
        // 100만 개 데이터 준비
        List<String> data = new ArrayList<>();
        Map<String, String> hashMap = new HashMap<>();

        for (int i = 0; i < 1_000_000; i++) {
            String key = "user" + i;
            data.add(key);
            hashMap.put(key, "value" + i);
        }

        String searchKey = "user999999"; // 마지막 데이터

        // 선형 탐색 시간 측정
        long start = System.nanoTime();
        for (String item : data) {
            if (item.equals(searchKey)) {
                break;
            }
        }
        long linearTime = System.nanoTime() - start;

        // 해시 탐색 시간 측정
        start = System.nanoTime();
        hashMap.get(searchKey);
        long hashTime = System.nanoTime() - start;

        System.out.println("선형 탐색: " + linearTime + " ns");
        System.out.println("해시 탐색: " + hashTime + " ns");
        System.out.println("성능 차이: " + (linearTime / hashTime) + "배");

        // 결과 예시:
        // 선형 탐색: 15,234,567 ns
        // 해시 탐색: 123 ns
        // 성능 차이: 123,866배!
    }
}
```

## 🔬 해시 함수의 핵심 아이디어

### 1. 균등 분포 (Uniform Distribution)

```java
// 나쁜 해시 함수 - 모든 데이터가 한 곳에 몰림
public int badHash(String str) {
    return 42; // 모든 문자열이 같은 해시값
}

// 좋은 해시 함수 - 데이터가 고르게 분산
public int goodHash(String str) {
    int hash = 0;
    for (int i = 0; i < str.length(); i++) {
        hash = 31 * hash + str.charAt(i); // 각 문자가 영향을 미침
    }
    return hash;
}

// 분산도 테스트
public void testDistribution() {
    Map<Integer, Integer> bucketCount = new HashMap<>();
    int bucketSize = 1000;

    for (int i = 0; i < 100000; i++) {
        String key = "user" + i;
        int bucket = Math.abs(key.hashCode()) % bucketSize;
        bucketCount.put(bucket, bucketCount.getOrDefault(bucket, 0) + 1);
    }

    // 각 버킷의 데이터 개수 확인
    int min = Collections.min(bucketCount.values());
    int max = Collections.max(bucketCount.values());
    System.out.println("최소: " + min + ", 최대: " + max + ", 편차: " + (max - min));
    // 좋은 해시 함수라면 편차가 작아야 함
}
```

### 2. 결정성 (Deterministic)

```java
String name = "김철수";
int hash1 = name.hashCode();
int hash2 = name.hashCode();

assert hash1 == hash2; // 항상 같은 입력은 같은 출력

// 하지만 JVM 재시작 시에는 달라질 수 있음 (Java 8+의 보안 기능)
// 같은 실행 중에는 항상 동일함
```

### 3. 빠른 계산

```java
// 복잡한 계산은 피해야 함 - 나쁜 예시
public int slowHash(String str) {
    // 느린 계산 - 피해야 할 패턴
    int hash = 0;
    for (int i = 0; i < 1000000; i++) { // 의미 없는 반복
        hash += str.length() * i;
    }
    return hash;
}

// 빠른 계산 - 좋은 예시
public int fastHash(String str) {
    if (str == null) return 0;
    return str.length() * 31 + str.charAt(0);
}

// String의 실제 hashCode - 효율적인 구현
public int stringHashCode(String str) {
    int h = 0;
    for (int i = 0; i < str.length(); i++) {
        h = 31 * h + str.charAt(i);
    }
    return h;
}
```

## 📚 역사적 배경: 해시 테이블의 발전

### 1960년대 - 해시 테이블 개념 등장

```java
// 초기 아이디어: Division Method
int hash(int key, int tableSize) {
    return key % tableSize; // 나머지 연산으로 인덱스 계산
}

// 문제점: 테이블 크기가 특정 값이면 분포가 나빠짐
// 예: 테이블 크기가 10이면 끝자리 0인 키들만 버킷 0에 몰림
```

### 1970년대 - 충돌 처리 기법 발전

#### 체이닝 (Chaining)

```java
class ChainedHashTable<K, V> {
    private List<Entry<K, V>>[] buckets;

    @SuppressWarnings("unchecked")
    public ChainedHashTable(int size) {
        buckets = new List[size];
        for (int i = 0; i < size; i++) {
            buckets[i] = new ArrayList<>();
        }
    }

    public void put(K key, V value) {
        int index = Math.abs(key.hashCode()) % buckets.length;
        List<Entry<K, V>> bucket = buckets[index];

        // 기존 키 찾기
        for (Entry<K, V> entry : bucket) {
            if (entry.key.equals(key)) {
                entry.value = value;
                return;
            }
        }

        // 새로운 엔트리 추가
        bucket.add(new Entry<>(key, value));
    }

    public V get(K key) {
        int index = Math.abs(key.hashCode()) % buckets.length;
        List<Entry<K, V>> bucket = buckets[index];

        for (Entry<K, V> entry : bucket) {
            if (entry.key.equals(key)) {
                return entry.value;
            }
        }
        return null;
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

#### 개방 주소법 (Open Addressing)

```java
class OpenAddressedHashTable<K, V> {
    private Entry<K, V>[] table;
    private int size;
    private static final Entry DELETED = new Entry(null, null);

    @SuppressWarnings("unchecked")
    public OpenAddressedHashTable(int capacity) {
        table = new Entry[capacity];
        size = 0;
    }

    // 선형 탐사 (Linear Probing)
    public void put(K key, V value) {
        int index = Math.abs(key.hashCode()) % table.length;

        while (table[index] != null && !table[index].key.equals(key)) {
            index = (index + 1) % table.length; // 다음 슬롯으로
        }

        if (table[index] == null || table[index] == DELETED) {
            size++;
        }
        table[index] = new Entry<>(key, value);
    }

    public V get(K key) {
        int index = Math.abs(key.hashCode()) % table.length;

        while (table[index] != null) {
            if (table[index] != DELETED && table[index].key.equals(key)) {
                return table[index].value;
            }
            index = (index + 1) % table.length;
        }
        return null;
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

### 1990년대 - Java HashMap의 등장

```java
// Java 1.2부터 HashMap 제공
Map<String, Object> map = new HashMap<>(); // O(1) 평균 성능!

// Java 8 이후의 개선: 트리화 (Treeification)
// 한 버킷에 8개 이상 엔트리가 몰리면 Red-Black Tree로 변환
// 최악의 경우 O(log n)으로 개선
```

## 🛡️ 해시가 해결한 근본적인 문제들

### 1. 메모리 vs 시간 트레이드오프

```java
// 옵션 1: 모든 가능한 키에 대해 배열 생성 (메모리 낭비)
class DirectAddressing {
    private String[] values = new String[Integer.MAX_VALUE]; // 21억 개!

    public void put(int key, String value) {
        values[key] = value; // O(1) 하지만 메모리 엄청 낭비
    }

    public String get(int key) {
        return values[key]; // O(1)
    }

    // 문제: 대부분 슬롯이 비어있음 (메모리 낭비)
}

// 옵션 2: 해시 테이블 (메모리와 시간의 균형)
class HashTable {
    private Entry[] buckets = new Entry[1024]; // 적당한 크기

    // 해시 함수로 큰 키 공간을 작은 배열로 매핑
    private int hash(int key) {
        return key % buckets.length;
    }

    // 메모리는 절약하면서 평균 O(1) 성능 유지
}
```

### 2. 동적 크기 조절

```java
// HashMap의 동적 리사이징
public class DynamicHashMap<K, V> {
    private Entry<K, V>[] buckets;
    private int size;
    private int capacity;
    private static final double LOAD_FACTOR = 0.75;

    @SuppressWarnings("unchecked")
    public DynamicHashMap() {
        capacity = 16;
        buckets = new Entry[capacity];
        size = 0;
    }

    public void put(K key, V value) {
        // 로드 팩터 초과 시 배열 크기 2배로 확장
        if (size > capacity * LOAD_FACTOR) {
            resize();
        }

        int index = Math.abs(key.hashCode()) % capacity;
        Entry<K, V> newEntry = new Entry<>(key, value);

        if (buckets[index] == null) {
            buckets[index] = newEntry;
        } else {
            // 체이닝으로 추가
            newEntry.next = buckets[index];
            buckets[index] = newEntry;
        }
        size++;
    }

    private void resize() {
        Entry<K, V>[] oldBuckets = buckets;
        int oldCapacity = capacity;

        capacity *= 2;
        buckets = new Entry[capacity];
        size = 0; // 다시 카운트됨

        // 모든 엔트리를 새로운 배열에 재해싱
        for (int i = 0; i < oldCapacity; i++) {
            Entry<K, V> entry = oldBuckets[i];
            while (entry != null) {
                put(entry.key, entry.value); // 새로운 위치에 재배치
                entry = entry.next;
            }
        }
    }

    static class Entry<K, V> {
        K key;
        V value;
        Entry<K, V> next;

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
}
```

## ⚠️ 해시의 한계와 해결책

### 1. 해시 충돌 (Hash Collision)

```java
// 충돌 예시
public void demonstrateCollisions() {
    // 의도적으로 작은 테이블 크기 사용
    int tableSize = 10;

    String[] keys = {"Aa", "BB"}; // 이 두 문자열은 같은 해시코드를 가짐!

    for (String key : keys) {
        int hash = key.hashCode();
        int index = Math.abs(hash) % tableSize;
        System.out.println(key + " -> hash: " + hash + ", index: " + index);
    }

    // 결과: 두 키가 같은 인덱스에 매핑됨
}
```

#### Java 8의 트리화 개선

```java
// Java 8 이전: 연결 리스트만 사용
// 최악의 경우 O(n) - 모든 데이터가 한 버킷에 몰릴 때

// Java 8 이후: 트리화 (Treeification)
class ImprovedHashMap {
    private static final int TREEIFY_THRESHOLD = 8;
    private static final int UNTREEIFY_THRESHOLD = 6;

    // 한 버킷의 엔트리가 8개 이상이면 Red-Black Tree로 변환
    // 최악의 경우도 O(log n)으로 개선!

    // 6개 이하로 줄어들면 다시 연결 리스트로 변환
    // (트리 유지 비용을 줄이기 위함)
}
```

### 2. 악의적인 입력 (Hash DoS Attack)

```java
// 문제 상황: 의도적으로 같은 해시값을 가지는 키들을 대량 입력
public class HashDosDemo {
    public static void hashDosAttack() {
        Map<String, String> map = new HashMap<>();

        // Java 7 이전에는 이런 공격이 가능했음
        // 같은 해시값을 가지는 문자열들을 대량 생성
        // 모든 엔트리가 한 버킷에 몰려서 O(n) 성능 저하

        long start = System.currentTimeMillis();

        // 의도적인 충돌 문자열들 (예시)
        String[] collisionStrings = generateCollisionStrings(100000);

        for (String str : collisionStrings) {
            map.put(str, "value");
        }

        long time = System.currentTimeMillis() - start;
        System.out.println("충돌 공격 시간: " + time + "ms");
    }

    // 실제로는 복잡한 알고리즘으로 충돌 문자열 생성
    private static String[] generateCollisionStrings(int count) {
        // 단순화된 예시
        return new String[]{"Aa", "BB", "C#"}; // 실제로는 더 많은 충돌 문자열
    }
}

// 해결책: 랜덤 시드 사용 (Java 8+)
class SecureHashMap {
    // HashMap 내부적으로 랜덤 시드를 추가하여
    // 예측 불가능한 해시값 생성

    // alternative_hashing과 hash randomization 기법 사용
    // 공격자가 충돌을 예측하기 어렵게 만듦
}
```

## 🎯 좋은 해시 함수의 특성

### 1. 31이라는 마법의 수

```java
// String의 hashCode에서 31을 사용하는 이유
public int stringHashCode(String str) {
    int h = 0;
    for (int i = 0; i < str.length(); i++) {
        h = 31 * h + str.charAt(i);  // 왜 31일까?
    }
    return h;
}

// 31을 사용하는 이유들:
// 1. 홀수 소수 - 곱셈 시 더 좋은 분포
// 2. 31 * i == (i << 5) - i - 비트 시프트로 최적화 가능
// 3. 충분히 크면서도 오버플로우가 적음
// 4. 경험적으로 좋은 분산도 제공

public void demonstrate31() {
    System.out.println("31 * 10 = " + (31 * 10));
    System.out.println("(10 << 5) - 10 = " + ((10 << 5) - 10)); // 같은 값!

    // JVM이 자동으로 최적화하여 비트 시프트 연산 사용
}
```

### 2. 실제 해시 함수 구현 예시

```java
// 커스텀 객체의 좋은 hashCode 구현
public class GoodHashExample {
    private String name;
    private int age;
    private List<String> hobbies;

    @Override
    public int hashCode() {
        // 방법 1: Objects.hash() 사용 (권장)
        return Objects.hash(name, age, hobbies);
    }

    // 방법 2: 수동 구현 (더 세밀한 제어)
    public int manualHashCode() {
        int result = 17; // 0이 아닌 상수로 시작

        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + age;
        result = 31 * result + (hobbies != null ? hobbies.hashCode() : 0);

        return result;
    }

    // 방법 3: 가중치 적용 (중요도가 다른 필드들)
    public int weightedHashCode() {
        int result = 17;

        result = 31 * result + (name != null ? name.hashCode() * 3 : 0); // name이 더 중요
        result = 31 * result + age * 2; // age는 중간 중요도
        result = 31 * result + (hobbies != null ? hobbies.hashCode() : 0); // 기본 가중치

        return result;
    }
}
```

## 🔍 개인적인 깨달음

### 해시가 해결한 핵심 문제

1. **검색 속도**: O(n)에서 O(1)로 혁신적 개선
2. **메모리 효율성**: 전체 키 공간을 작은 배열로 매핑
3. **동적 확장**: 데이터 크기에 따른 자동 조절
4. **범용성**: 모든 타입의 키에 적용 가능

### 해시 함수의 핵심 철학

- **"큰 공간을 작은 공간으로"**: 무한한 키 공간을 유한한 배열로 매핑
- **"계산으로 위치 찾기"**: 탐색 대신 직접 계산으로 접근
- **"균등 분포"**: 편향되지 않는 공정한 데이터 분배
- **"빠른 계산"**: 해시 계산 자체가 병목이 되어서는 안 됨

### 현대 프로그래밍에서의 의미

해시는 단순히 **성능 최적화 기법**을 넘어서 **현대 프로그래밍의 기초**가 되었다:

- **데이터베이스 인덱싱**: B+ 트리와 함께 핵심 기술
- **캐시 시스템**: Redis, Memcached 등의 핵심
- **분산 시스템의 샤딩**: 일관된 해시로 데이터 분산
- **암호학적 해시**: SHA, MD5 등 보안 기술의 기반
- **블록체인**: 작업증명과 블록 연결의 핵심
- **로드 밸런싱**: 서버 부하 분산
- **CDN**: 콘텐츠 분산 네트워크

### 미래 전망

해시 기술은 계속 발전하고 있다:

- **일관된 해시 (Consistent Hashing)**: 분산 시스템에서 활용
- **Cuckoo Hashing**: 최악의 경우에도 O(1) 보장
- **Robin Hood Hashing**: 분산도 개선
- **HyperLogLog**: 메모리 효율적인 카디널리티 추정

결국 hashCode()는 이 모든 혁신의 시작점이었다.
단순한 메서드 하나가 이렇게 큰 변화를 가져올 수 있다는 것이 놀랍고,
**좋은 추상화가 얼마나 강력한지**를 보여주는 완벽한 예시인 것 같다!

## 📚 참고 자료

- **Introduction to Algorithms (CLRS)** - 해시 테이블 구조와 분석
- **The Art of Computer Programming Vol.3** - Donald Knuth의 해시 함수 분석
- **Effective Java 3rd Edition** - Java hashCode 구현 가이드라인
- **Hash Table Wikipedia** - 해시 테이블의 역사와 발전
- **Java HashMap Source Code** - 실제 구현체 분석

## 🔗 연관 문서

- [Java equals/hashCode 기본](./equals-hashcode-basic.md)
- [다른 언어에서의 equals/hashCode](./equals-hashcode-cross-language.md)
