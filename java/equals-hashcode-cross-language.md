# 다른 언어에서의 equals/hashCode 접근 방식

## 🤔 왜 정리하게 되었나?

Java에서 equals()와 hashCode()를 공부하다 보니, 다른 프로그래밍 언어에서는 이런 문제를 어떻게 해결하는지 궁금해졌다.
같은 문제를 다른 언어에서는 어떤 철학과 방법으로 접근하는지 비교해보고 싶어서 정리해보았다.

## 🌍 언어별 접근 방식

### Python - `__eq__`와 `__hash__`

Python은 **특수 메서드(Magic Methods)**를 통해 객체 비교와 해시를 구현한다.

```python
class Person:
    def __init__(self, name, age):
        self.name = name
        self.age = age

    def __eq__(self, other):
        if not isinstance(other, Person):
            return False
        return self.name == other.name and self.age == other.age

    def __hash__(self):
        return hash((self.name, self.age))  # 튜플의 해시 사용

    def __repr__(self):
        return f"Person('{self.name}', {self.age})"

# 사용 예시
person1 = Person("김철수", 30)
person2 = Person("김철수", 30)

# Set에서 중복 제거
people = {person1, person2}
print(len(people))  # 1 (중복 제거됨)

# Dictionary에서 키로 사용
person_dict = {person1: "개발자"}
print(person_dict[person2])  # "개발자" (같은 키로 인식)

# equals와 hash 확인
print(person1 == person2)  # True
print(hash(person1) == hash(person2))  # True
```

#### Python의 dataclass 활용

```python
from dataclasses import dataclass

@dataclass
class Person:
    name: str
    age: int

    # __eq__와 __hash__가 자동 생성됨 (frozen=True 시)

@dataclass(frozen=True)  # 불변 객체로 만들면 해시 가능
class ImmutablePerson:
    name: str
    age: int

# 사용
person1 = ImmutablePerson("김철수", 30)
person2 = ImmutablePerson("김철수", 30)

people_set = {person1, person2}
print(len(people_set))  # 1
```

### C# - `Equals()`와 `GetHashCode()`

C#은 Java와 매우 유사한 접근 방식을 사용한다.

```csharp
public class Person
{
    public string Name { get; set; }
    public int Age { get; set; }

    public override bool Equals(object obj)
    {
        if (obj is Person other)
        {
            return Name == other.Name && Age == other.Age;
        }
        return false;
    }

    public override int GetHashCode()
    {
        return HashCode.Combine(Name, Age);  // .NET Core 2.1+
    }

    // 전통적인 방법
    // public override int GetHashCode()
    // {
    //     unchecked
    //     {
    //         int hash = 17;
    //         hash = hash * 23 + (Name?.GetHashCode() ?? 0);
    //         hash = hash * 23 + Age.GetHashCode();
    //         return hash;
    //     }
    // }
}

// 사용 예시
var person1 = new Person { Name = "김철수", Age = 30 };
var person2 = new Person { Name = "김철수", Age = 30 };

var people = new HashSet<Person> { person1, person2 };
Console.WriteLine(people.Count); // 1

var personDict = new Dictionary<Person, string> { { person1, "개발자" } };
Console.WriteLine(personDict[person2]); // "개발자"
```

#### C#의 Record 타입 (C# 9.0+)

```csharp
// record 키워드로 자동 생성
public record Person(string Name, int Age);

// 사용
var person1 = new Person("김철수", 30);
var person2 = new Person("김철수", 30);

Console.WriteLine(person1 == person2);  // True
Console.WriteLine(person1.Equals(person2));  // True
Console.WriteLine(person1.GetHashCode() == person2.GetHashCode());  // True

var people = new HashSet<Person> { person1, person2 };
Console.WriteLine(people.Count);  // 1
```

### JavaScript - 제한적 지원과 해결책

JavaScript는 기본적으로 **참조 비교**만 지원하므로 특별한 해결책이 필요하다.

```javascript
// JavaScript - 기본 Map/Set 사용 시 문제
const person1 = { name: "김철수", age: 30 };
const person2 = { name: "김철수", age: 30 };

const personMap = new Map();
personMap.set(person1, "개발자");

console.log(personMap.get(person2)); // undefined (다른 객체로 인식)
console.log(person1 === person2); // false

// Set에서도 마찬가지
const peopleSet = new Set([person1, person2]);
console.log(peopleSet.size); // 2 (중복으로 인식)
```

#### 해결 방법 1: 문자열 키 사용

```javascript
const personMap = new Map();
const getKey = (person) => `${person.name}-${person.age}`;

personMap.set(getKey(person1), "개발자");
console.log(personMap.get(getKey(person2))); // "개발자"

// Set도 마찬가지
const keySet = new Set();
keySet.add(getKey(person1));
keySet.add(getKey(person2));
console.log(keySet.size); // 1
```

#### 해결 방법 2: 커스텀 클래스

```javascript
class Person {
  constructor(name, age) {
    this.name = name;
    this.age = age;
  }

  equals(other) {
    return this.name === other.name && this.age === other.age;
  }

  hashCode() {
    // 간단한 해시 함수
    return (this.name + this.age).split("").reduce((a, b) => {
      a = (a << 5) - a + b.charCodeAt(0);
      return a & a; // 32bit 정수로 변환
    }, 0);
  }

  toString() {
    return `Person(${this.name}, ${this.age})`;
  }
}

// 커스텀 해시 맵 구현
class HashMapJS {
  constructor() {
    this.buckets = new Array(16);
    for (let i = 0; i < this.buckets.length; i++) {
      this.buckets[i] = [];
    }
  }

  set(key, value) {
    const index = Math.abs(key.hashCode()) % this.buckets.length;
    const bucket = this.buckets[index];

    const existing = bucket.find((item) => item.key.equals(key));
    if (existing) {
      existing.value = value;
    } else {
      bucket.push({ key, value });
    }
  }

  get(key) {
    const index = Math.abs(key.hashCode()) % this.buckets.length;
    const bucket = this.buckets[index];

    const item = bucket.find((item) => item.key.equals(key));
    return item ? item.value : undefined;
  }
}

// 사용
const person1 = new Person("김철수", 30);
const person2 = new Person("김철수", 30);

const customMap = new HashMapJS();
customMap.set(person1, "개발자");
console.log(customMap.get(person2)); // "개발자"
```

### Rust - 트레이트 기반 접근

Rust는 **트레이트(Trait)** 시스템을 통해 equals와 hash 기능을 구현한다.

```rust
use std::collections::HashMap;
use std::hash::{Hash, Hasher};

#[derive(Debug)]
struct Person {
    name: String,
    age: u32,
}

impl PartialEq for Person {
    fn eq(&self, other: &Self) -> bool {
        self.name == other.name && self.age == other.age
    }
}

impl Eq for Person {}

impl Hash for Person {
    fn hash<H: Hasher>(&self, state: &mut H) {
        self.name.hash(state);
        self.age.hash(state);
    }
}

// 더 간단한 방법: derive 매크로 사용
#[derive(Debug, PartialEq, Eq, Hash)]
struct SimplePerson {
    name: String,
    age: u32,
}

fn main() {
    let person1 = Person {
        name: "김철수".to_string(),
        age: 30
    };
    let person2 = Person {
        name: "김철수".to_string(),
        age: 30
    };

    // 동등성 확인
    println!("Equal: {}", person1 == person2); // true

    // HashMap에서 사용
    let mut people = HashMap::new();
    people.insert(person1, "개발자");

    // person2로 조회 가능
    println!("Job: {:?}", people.get(&person2)); // Some("개발자")

    // HashSet 사용
    use std::collections::HashSet;
    let mut people_set = HashSet::new();
    people_set.insert(SimplePerson {
        name: "김철수".to_string(),
        age: 30
    });
    people_set.insert(SimplePerson {
        name: "김철수".to_string(),
        age: 30
    });

    println!("Set size: {}", people_set.len()); // 1
}
```

### Kotlin - Java 호환성 + 편의성

Kotlin은 Java와 완전 호환되면서도 더 편리한 기능을 제공한다.

```kotlin
// data class는 자동으로 equals/hashCode 생성
data class Person(val name: String, val age: Int)

// 사용
val person1 = Person("김철수", 30)
val person2 = Person("김철수", 30)

val people = setOf(person1, person2)
println(people.size) // 1

val personMap = mapOf(person1 to "개발자")
println(personMap[person2]) // "개발자"

// 수동 구현도 가능
class ManualPerson(val name: String, val age: Int) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ManualPerson) return false
        return name == other.name && age == other.age
    }

    override fun hashCode(): Int {
        return name.hashCode() * 31 + age
    }
}

// destructuring과 copy도 지원
val (name, age) = person1
val olderPerson = person1.copy(age = 31)
```

### Swift - 구조체와 프로토콜

Swift는 구조체와 프로토콜을 통해 값 타입의 동등성을 지원한다.

```swift
struct Person: Equatable, Hashable {
    let name: String
    let age: Int

    // Equatable 프로토콜 구현 (자동 생성 가능)
    static func == (lhs: Person, rhs: Person) -> Bool {
        return lhs.name == rhs.name && lhs.age == rhs.age
    }

    // Hashable 프로토콜 구현 (자동 생성 가능)
    func hash(into hasher: inout Hasher) {
        hasher.combine(name)
        hasher.combine(age)
    }
}

// 더 간단하게 - 자동 생성
struct SimplePerson: Equatable, Hashable {
    let name: String
    let age: Int
    // equals와 hash가 자동 생성됨
}

// 사용
let person1 = Person(name: "김철수", age: 30)
let person2 = Person(name: "김철수", age: 30)

// Set에서 중복 제거
let people: Set<Person> = [person1, person2]
print(people.count) // 1

// Dictionary에서 키로 사용
let personDict: [Person: String] = [person1: "개발자"]
print(personDict[person2] ?? "없음") // "개발자"
```

## 📊 언어별 비교 정리

| 언어           | equals 메서드 | hashCode 메서드 | 자동 생성     | 기본 철학          |
| -------------- | ------------- | --------------- | ------------- | ------------------ |
| **Java**       | `equals()`    | `hashCode()`    | IDE, Lombok   | 명시적 제어        |
| **Python**     | `__eq__()`    | `__hash__()`    | `@dataclass`  | 간결성             |
| **C#**         | `Equals()`    | `GetHashCode()` | Record(C# 9+) | Java 유사 + 개선   |
| **JavaScript** | 없음          | 없음            | 없음          | 유연성 vs 복잡성   |
| **Rust**       | `PartialEq`   | `Hash`          | `#[derive]`   | 안전성 + 성능      |
| **Kotlin**     | `equals()`    | `hashCode()`    | `data class`  | Java 호환 + 편의성 |
| **Swift**      | `Equatable`   | `Hashable`      | 자동 생성     | 값 타입 중심       |

## 🔍 언어별 특성 분석

### 공통점

1. **해시 기반 자료구조** 사용 시 동일한 문제 발생
2. **equals와 hashCode 일관성** 필요
3. **성능에 직접적 영향**
4. **자동 생성** 기능 제공 추세

### 주요 차이점

#### 1. 자동 생성 지원 수준

- **높음**: Kotlin(data class), Swift(자동), Rust(derive)
- **중간**: Python(dataclass), C#(record)
- **낮음**: Java(IDE 도움), JavaScript(없음)

#### 2. 언어 철학

- **명시적 제어**: Java, C#
- **편의성 우선**: Kotlin, Python
- **안전성 우선**: Rust
- **유연성 우선**: JavaScript

#### 3. 타입 시스템

- **클래스 기반**: Java, C#, Python
- **트레이트/프로토콜**: Rust, Swift
- **하이브리드**: Kotlin
- **프로토타입**: JavaScript

### 실무에서 고려할 점

#### 1. 팀 환경

- **다국어 개발 팀**: 언어별 차이점 공유 필요
- **일관된 패턴**: 프로젝트 내 일관된 구현 방식 채택

#### 2. 마이그레이션

- **Java ↔ Kotlin**: 거의 투명한 호환성
- **Java ↔ C#**: 개념은 동일, 문법 약간 다름
- **다른 언어**: 개념 이해 후 해당 언어 방식으로 재구현

#### 3. 성능 고려사항

- **해시 함수 품질**: 모든 언어에서 공통으로 중요
- **메모리 사용량**: 언어별 메모리 모델 차이
- **GC 영향**: 가비지 컬렉션 있는 언어에서 고려

## 💡 개인적인 깨달음

### 언어 설계 철학의 영향

- **Java**: "명시적이 암묵적보다 낫다" - 제어권은 주지만 실수 가능성
- **Python**: "단순함이 복잡함보다 낫다" - 간결한 문법과 자동화
- **Rust**: "안전성을 컴파일 타임에" - 타입 시스템으로 오류 방지
- **Kotlin**: "Java 호환 + 현대적 편의성" - 기존 생태계 활용

### 공통된 핵심 원리

언어는 달라도 해결하려는 **근본적인 문제는 동일**:

1. 객체 동등성 비교
2. 해시 기반 자료구조에서의 효율적 검색
3. 메모리와 성능의 균형

### 발전 방향

- **자동 생성** 기능이 점점 강화되는 추세
- **불변성(Immutability)** 지원 증가
- **타입 안전성** 강화
- **개발자 편의성** 개선

결국 어떤 언어를 사용하든, **해시 테이블의 기본 원리**와 **equals/hash 일관성**의 중요성은 변하지 않는다는 것을 알 수 있었다!

## 📚 참고 자료

- **Python Data Model** - `__eq__`, `__hash__` 매직 메서드
- **C# Object.Equals Documentation** - Microsoft 공식 가이드
- **Rust Book - Traits** - PartialEq, Eq, Hash 트레이트
- **Kotlin Data Classes** - JetBrains 공식 문서
- **Swift Equatable & Hashable** - Apple 개발자 문서

## 🔗 연관 문서

- [Java equals/hashCode 기본](./equals-hashcode-basic.md)
- [해시 알고리즘 심화 이해](./hash-algorithm-deep-dive.md)
