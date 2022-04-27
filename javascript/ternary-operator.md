# 조건부 연산자 '?'
```js
 let sojuAllowed = (age > 18) ? true : false;
```

# null 병합 연산자 '??'
```js
let realName = null;
let nickName = null;

let message = realName ?? nickName ?? "익명" + "님 반갑습니다"
```

# 옵셔널 체이닝 '?.'
```js
let user = {};
let phoneNumber = user?.number?.phone // undefined

user && user.number && user.number.phone // undefined
```