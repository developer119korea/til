# Java main 메서드가 static인 이유

## 궁금했던 점

Java 코드 작성하면서 항상 `public static void main(String[] args)` 이렇게 써왔는데, 왜 `static`이 필요한지 궁금했다.

## 알아본 내용

### JVM 실행 과정

1. `.class` 파일을 메모리에 로드
2. 지정된 클래스에서 main 메서드 찾기
3. main 메서드부터 실행 시작

핵심: **JVM이 객체 생성 전에 main 메서드를 호출해야 함**

### static이 없다면?

```java
public class Test {
    public void main(String[] args) {  // static 없음
        System.out.println("Hello");
    }
}
```

문제:

- main 메서드 호출하려면 Test 객체가 필요
- 근데 객체 생성 코드는 main 안에 있어야 함
- **순환 참조!**

### static이 있으면?

```java
public class Test {
    public static void main(String[] args) {
        // JVM이 Test 클래스 통해 바로 호출 가능
        Test app = new Test();  // 필요시 객체 생성
    }
}
```

- 클래스 로드 시 메모리에 올라감
- 객체 생성 없이 바로 호출 가능

## 테스트해본 내용

```java
public class StaticTest {
    static {
        System.out.println("클래스 로드됨");
    }

    public static void main(String[] args) {
        System.out.println("main 실행");
        new StaticTest();
    }

    public StaticTest() {
        System.out.println("객체 생성됨");
    }
}
```

실행 결과:

```text
클래스 로드됨
main 실행
객체 생성됨
```

순서 확인됨!

## 실무에서 본 패턴

### 스프링 부트

```java
@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
```

### 일반적인 구조

```java
public class MainApp {
    public static void main(String[] args) {
        new MainApp().run(args);
    }

    private void run(String[] args) {
        // 실제 로직
    }
}
```

## 정리

- JVM이 프로그램 시작할 때 객체 없이 메서드 호출해야 함
- static 메서드는 클래스 로드 시점에 메모리에 올라감
- 순환 참조 문제 해결
- 이게 Java 설계의 필연적 결과
