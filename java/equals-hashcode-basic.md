# equals()와 hashCode() 기본 개념 (Java)

## 🤔 왜 정리하게 되었나?

최근 프로젝트에서 컬렉션을 사용하다가 예상과 다른 결과가 나오는 경우를 겪었다.
객체를 Set에 넣었는데 중복이 제거되지 않거나, HashMap에서 같은 키로 값을 찾을 수 없는 상황이 발생했다.
이런 문제들이 모두 equals()와 hashCode()와 관련이 있다는 것을 알게 되어 정리해보았다.

## 📚 학습 내용 정리

### 1. equals()와 hashCode()란?

#### 1.1 equals() 메서드

- Object 클래스에 정의된 메서드
- 두 객체가 논리적으로 같은지 판단
- 기본 구현은 참조 비교 (`==`)

```java
// Object 클래스의 기본 equals 구현
public boolean equals(Object obj) {
    return (this == obj);
}
```

#### 1.2 hashCode() 메서드

- Object 클래스에 정의된 메서드
- 객체의 해시코드 값을 반환
- 해시 기반 컬렉션에서 사용

```java
// Object 클래스의 hashCode (native 메서드)
public native int hashCode();
```

### 2. 왜 함께 오버라이드해야 할까?

#### 2.1 해시 기반 컬렉션의 동작 방식

해시 기반 컬렉션(HashSet, HashMap, HashTable)은 다음과 같이 동작한다:

1. **hashCode()로 버킷 위치 결정**
2. **같은 버킷 내에서 equals()로 실제 동일성 판단**

```java
// HashMap의 get 메서드 동작 방식 (단순화)
public V get(Object key) {
    int hash = key.hashCode();
    int bucketIndex = hash % buckets.length;

    // 해당 버킷에서 equals()로 실제 키 찾기
    for (Entry entry : buckets[bucketIndex]) {
        if (entry.key.equals(key)) {
            return entry.value;
        }
    }
    return null;
}
```

#### 2.2 계약(Contract) 조건

**Java Object 명세서에서 정의한 규칙:**

1. **equals()가 true이면, hashCode()도 같아야 함**
2. **equals()가 false여도, hashCode()는 같을 수 있음** (해시 충돌)
3. **hashCode()가 다르면, equals()는 반드시 false**

```java
// 잘못된 예시 - 계약 위반
class WrongPerson {
    private String name;
    private int age;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        WrongPerson person = (WrongPerson) obj;
        return age == person.age && Objects.equals(name, person.name);
    }

    // hashCode()를 오버라이드하지 않음 - 문제 발생!
}
```

### 3. 문제가 발생하는 실제 사례

#### 3.1 HashSet에서 중복 제거 실패

```java
public class HashSetProblem {
    static class Person {
        private String name;
        private int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;

            Person person = (Person) obj;
            return age == person.age && Objects.equals(name, person.name);
        }

        // hashCode() 오버라이드 안 함

        @Override
        public String toString() {
            return "Person{name='" + name + "', age=" + age + "}";
        }
    }

    public static void main(String[] args) {
        Set<Person> people = new HashSet<>();

        Person person1 = new Person("김철수", 30);
        Person person2 = new Person("김철수", 30);

        people.add(person1);
        people.add(person2);

        System.out.println("Set 크기: " + people.size()); // 2 (예상: 1)
        System.out.println("equals 결과: " + person1.equals(person2)); // true
        System.out.println("hashCode 같음: " + (person1.hashCode() == person2.hashCode())); // false

        // 결과: 같은 내용의 객체가 중복으로 저장됨
    }
}
```

#### 3.2 HashMap에서 값 조회 실패

```java
public class HashMapProblem {
    public static void main(String[] args) {
        Map<Person, String> personMap = new HashMap<>();

        Person key1 = new Person("이영희", 25);
        personMap.put(key1, "개발자");

        Person key2 = new Person("이영희", 25);

        System.out.println("같은 내용인가? " + key1.equals(key2)); // true
        System.out.println("값 조회 결과: " + personMap.get(key2)); // null (예상: "개발자")

        // 이유: hashCode()가 다르므로 다른 버킷에서 찾음
    }
}
```

### 4. 올바른 구현 방법

#### 4.1 수동 구현

```java
public class Person {
    private String name;
    private int age;
    private String email;

    public Person(String name, int age, String email) {
        this.name = name;
        this.age = age;
        this.email = email;
    }

    @Override
    public boolean equals(Object obj) {
        // 1. 같은 참조인지 확인
        if (this == obj) return true;

        // 2. null 체크 및 클래스 타입 확인
        if (obj == null || getClass() != obj.getClass()) return false;

        // 3. 필드별 비교
        Person person = (Person) obj;
        return age == person.age &&
               Objects.equals(name, person.name) &&
               Objects.equals(email, person.email);
    }

    @Override
    public int hashCode() {
        // Objects.hash() 사용 - 권장 방법
        return Objects.hash(name, age, email);
    }
}
```

#### 4.2 IDE 자동 생성 활용

```java
// IntelliJ IDEA에서 자동 생성한 예시
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Person person = (Person) o;
    return age == person.age &&
           Objects.equals(name, person.name) &&
           Objects.equals(email, person.email);
}

@Override
public int hashCode() {
    return Objects.hash(name, age, email);
}
```

#### 4.3 Lombok 사용

```java
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class Person {
    private String name;
    private int age;
    private String email;

    // equals()와 hashCode()가 자동 생성됨
}
```

### 5. 고급 고려사항

#### 5.1 상속 관계에서의 주의점

```java
// 부모 클래스
class Animal {
    protected String name;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Animal animal = (Animal) obj;
        return Objects.equals(name, animal.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}

// 자식 클래스
class Dog extends Animal {
    private String breed;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false; // 부모 equals 호출

        Dog dog = (Dog) obj;
        return Objects.equals(breed, dog.breed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), breed); // 부모 hashCode 포함
    }
}
```

#### 5.2 불변 객체에서의 구현

```java
public final class ImmutablePerson {
    private final String name;
    private final int age;

    public ImmutablePerson(String name, int age) {
        this.name = name;
        this.age = age;
    }

    // 불변 객체이므로 hashCode를 캐싱할 수 있음
    private volatile int hashCode; // lazy initialization

    @Override
    public int hashCode() {
        int result = hashCode;
        if (result == 0) {
            result = Objects.hash(name, age);
            hashCode = result;
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        ImmutablePerson that = (ImmutablePerson) obj;
        return age == that.age && Objects.equals(name, that.name);
    }
}
```

### 6. 성능 고려사항

#### 6.1 좋은 hashCode() 특성

```java
// 나쁜 hashCode - 모든 객체가 같은 해시코드
@Override
public int hashCode() {
    return 42; // 모든 객체가 같은 버킷에 몰림
}

// 좋은 hashCode - 고르게 분산
@Override
public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + age; // 31은 홀수 소수
    result = 31 * result + (email != null ? email.hashCode() : 0);
    return result;
}
```

#### 6.2 String의 hashCode 구현 참고

```java
// String 클래스의 실제 hashCode 구현
public int hashCode() {
    int h = hash;
    if (h == 0 && value.length > 0) {
        char val[] = value;

        for (int i = 0; i < value.length; i++) {
            h = 31 * h + val[i]; // 31 * 이전값 + 현재문자
        }
        hash = h;
    }
    return h;
}
```

### 7. 실무 적용 팁

#### 7.1 테스트 코드 작성

```java
@Test
public void testEqualsAndHashCode() {
    Person person1 = new Person("김철수", 30, "kim@example.com");
    Person person2 = new Person("김철수", 30, "kim@example.com");
    Person person3 = new Person("이영희", 25, "lee@example.com");

    // equals 테스트
    assertEquals(person1, person2);
    assertNotEquals(person1, person3);

    // hashCode 테스트
    assertEquals(person1.hashCode(), person2.hashCode());
    // person1과 person3의 hashCode는 다를 가능성이 높지만, 같을 수도 있음

    // 컬렉션에서의 동작 테스트
    Set<Person> people = new HashSet<>();
    people.add(person1);
    people.add(person2);

    assertEquals(1, people.size()); // 중복 제거 확인
    assertTrue(people.contains(new Person("김철수", 30, "kim@example.com")));
}
```

#### 7.2 equals() 구현 시 주의사항

```java
@Override
public boolean equals(Object obj) {
    // ❌ 잘못된 패턴들
    if (obj instanceof Person) { // getClass() 대신 instanceof 사용 시 대칭성 위반 가능
        // ...
    }

    Person other = (Person) obj;
    if (this.name.equals(other.name)) { // NPE 가능성
        // ...
    }

    // ✅ 올바른 패턴
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;

    Person person = (Person) obj;
    return age == person.age &&
           Objects.equals(name, person.name); // null-safe 비교
}
```

## 🔍 개인적인 정리

### 핵심 깨달음

- **해시 기반 컬렉션의 성능**은 hashCode() 구현의 품질에 달려있다
- **equals()만 오버라이드하면 안 되는 이유**를 이제 확실히 알겠다
- **계약 조건**을 지키지 않으면 예측할 수 없는 버그가 발생한다
- **IDE나 Lombok**을 활용하면 실수를 줄일 수 있다

### 실무에서 적용할 점

- **항상 함께 구현**: equals()를 오버라이드하면 반드시 hashCode()도 함께
- **불변 필드 사용**: equals/hashCode에 사용되는 필드는 가능한 불변으로
- **테스트 작성**: 컬렉션 동작을 포함한 테스트 케이스 필수
- **성능 고려**: 자주 사용되는 객체라면 hashCode 분산성 고민

### 추가로 공부할 것들

- **EqualsVerifier 라이브러리**: equals/hashCode 구현 검증 도구
- **Apache Commons EqualsBuilder/HashCodeBuilder**: 빌더 패턴으로 구현
- **Record 클래스**: Java 14+에서 자동으로 equals/hashCode 생성

## 📚 참고 자료

- **Effective Java 3rd Edition** - Item 10, 11 (equals/hashCode 구현 가이드)
- **Java Object 클래스 명세서** - equals/hashCode 계약 조건
- **Collections Framework 소스코드** - 해시 기반 컬렉션의 실제 구현

## 🔗 연관 문서

- [다른 언어에서의 equals/hashCode](./equals-hashcode-cross-language.md)
- [해시 알고리즘 심화 이해](./hash-algorithm-deep-dive.md)
