안드로이드 네이티브에서 웹뷰로 이벤트 발생시 CustomEvent를 사용하는데 인자를 꼭 "detail"로 넣어 보내야했다.

```kt
// 안드로이드에서 이벤트 발생시
val webView = findViewById<WebView>(R.id.webview);
webView.evaluateJavascript("var event = new CustomEvent(\'native-navigate\', { detail : { destination : \'${destination}\' }}); window.dispatchEvent(event);", null);
```

```js
//리액트에서 이벤트 수신시
useEffect(() => {
    window.addEventListener('native-navigate', (event) => {
      const { destination } = event.detail;
      navigate(`/${destination}`);
    });
  }, []);
```

알아보니까 도큐먼트에 아래와 같이 설명되어있었다.


```js
// CustomEvent 생성
const catFound = new CustomEvent('animalfound', {
  detail: {
    name: 'cat'
  }
});
const dogFound = new CustomEvent('animalfound', {
  detail: {
    name: 'dog'
  }
});

// 적합한 이벤트 수신기 부착
obj.addEventListener('animalfound', (e) => console.log(e.detail.name));

// 이벤트 발송
obj.dispatchEvent(catFound);
obj.dispatchEvent(dogFound);
```