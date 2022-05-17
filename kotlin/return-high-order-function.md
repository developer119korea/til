# 고차 함수의 흐름 제어

## for문
---
```kt
data class Person(val name: String, val age: Int)
val people = listOf(Person("Alice", 29), Person("Bob", 31))

fun lookForAlice(people: List<Person>) {
    for (person in people) {
        if(person.name == "Alice") {
            println("Found")
            // lookForAlice 함수를 반환
            return
        }
    }

}
```
Alice를 찾으면 리턴 된다


## forEach + 람다
---
```kt
fun lookForAlice(people: List<Person>) {
    people.forEach {
        if(it.name == "Alice") {
            println("Found")
            // lookForAlice 함수를 반환
            return
        }
    }

    println("Alice is not found")
}
```
람다 안에서 return을 사용하면 람다로부터만 반환되는 게 아니라 그 람다를 호출하는 함수가 실행을 끝내고 반환됩니다. 즉, 위의 for문 예제과 똑같이 실행됩니다.
코틀린에서는 람다를 받는 함수의 return은 for 루프의 return과 같은 의미를 갖도록 구현했습니다. 이렇게 자신을 둘러싸고 있는 블록보다 더 바깥에 있는 다른 블록을 반환하게 만든 return문을 넌로컬(non-local) return이라 부릅니다.

## 레이블 사용
---
```kt
// 레이블을 통해 로컬 리턴 사용
fun lookForAlice(people: List<Person>) {
    people.forEach label@{ // 람다 식 앞에 레이블을 붙임
        if(it.name == "Alice") return@label // return@label은 앞에서 정의한 레이블을 참조
    }

    // 항상 이 줄은 출력
    println("Alice might be somewhere")
}
```

## 인라인 함수의 이름을 레이블로 사용
---
```kt
fun lookForAlice(people: List<Person>) {
    people.forEach {
        // return@forEach는 람다식으로부터 반환시킵니다.
        if(it.name == "Alice") return@forEach
    }

    println("Alice might be somewhere")
}
```

## 무명 함수 안에서 return
---
```kt
// 무명 함수 안에서 return 사용하기
fun lookForAlice(people: List<Person>) {
    people.forEach(fun (person) { // 람다 식 대신 무명 함수를 사용
        // return은 가장 가까운 함수를 가리키는데 이 위치에서 가장 가까운 함수는 무명 함수
        if(person.name == "Alice") return
        println("${person.name} is not Alice")
    })
}
```
