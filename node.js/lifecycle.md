# Node.js Lifecycle 개념 정리 (TIL)

Node.js 프로세스는 시작 → 이벤트 루프 반복 → 종료의 사이클을 가진다. 아래는 라이프사이클 이해에 꼭 필요한 최소 개념 정리다.

## 1) 부팅과 초기 실행

- V8, libuv 초기화 → 전역 객체(process, global) 구성 → 메인 스크립트 로딩/동기 실행
- 동기 코드가 끝난 뒤, 등록된 비동기 작업들을 이벤트 루프가 처리하기 시작

## 2) 이벤트 루프(major phases)

이벤트 루프는 반복적으로 아래 단계를 순회한다(간단화).

1. timers: setTimeout/setInterval 콜백 실행(만기된 타이머)
2. pending callbacks: 일부 시스템 콜백 처리
3. poll: I/O 이벤트 수집 및 콜백 실행, 대기/즉시 처리 결정
4. check: setImmediate 콜백 실행
5. close callbacks: 'close' 이벤트 콜백 실행

참고: idle/prepare는 내부 용도로 생략.

## 3) 마이크로태스크 큐(process.nextTick / Promises)

- 각 단계 사이(턴 끝)와 콜백 실행 직후 마이크로태스크가 비워질 때까지 실행된다.
- Node.js에서 실행 순서 우선순위
  - process.nextTick 콜백 → Promise microtask(then/catch/finally)
  - 그 다음에 다음 이벤트 루프 단계로 진행

## 4) 자주 혼동하는 타이밍 요약

- setTimeout(fn, 0): timers 단계에서 실행(정확히는 최소 지연 후 만기 시)
- setImmediate(fn): check 단계에서 실행
- I/O 콜백 내부에서는 보통 setImmediate가 setTimeout(0)보다 먼저 실행됨
- process.nextTick(fn): 현재 턴 종료 전에 반드시 실행(주의: 과도 사용 시 굶주림 발생 가능)

## 5) 프로세스 종료 조건과 종료 이벤트

- 종료 조건: 이벤트 루프에 더 이상 실행할 작업(타이머/핸들/요청)이 없을 때 또는 process.exit(code) 호출 시
- beforeExit: 이벤트 루프가 비어 종료 직전에 발생(추가 비동기 작업 등록 시 종료 지연 가능)
- exit: 프로세스가 완전히 종료되기 직전 동기적으로 발생(비동기 작업 불가)

간단 예시:

```js
process.on("beforeExit", (code) => {
  // 마지막으로 할 일 등록 가능(비동기 등록 시 종료가 지연될 수 있음)
});
process.on("exit", (code) => {
  // 동기만 가능, 로그 남기기 정도
});
```

## 6) 신호(Signal) 처리와 우아한 종료(요약)

- SIGINT(Ctrl+C), SIGTERM 수신 시 서버 핸들/DB 연결을 정리하고 종료하도록 훅을 둔다.

```js
process.on("SIGINT", () => shutdown());
process.on("SIGTERM", () => shutdown());
function shutdown() {
  // 서버.close, 연결 해제 등 정리 후 process.exit(0)
}
```

## 7) 핵심 포인트 한 줄 요약

- “동기 실행 → 비동기 등록 → 이벤트 루프 단계별 처리 → 마이크로태스크 소거 → 종료 조건 만족 시 프로세스 종료”
- nextTick > Promise microtask > 다음 루프 단계 순서로 기억
- setTimeout(0) vs setImmediate 타이밍 차이를 I/O 컨텍스트 기준으로 이해

## 참고

- [Event Loop](https://nodejs.org/en/docs/guides/event-loop-timers-and-nexttick/)
- [Process](https://nodejs.org/api/process.html)
