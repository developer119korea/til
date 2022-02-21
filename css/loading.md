기본 요소를 활용하여 로딩스피너를 만들수 있다

```css
.loading {
  width: 1.5em;
  height: 1.5em;
  border-radius: 50%;
  border: 3px solid makerLightGrey;
  border-top: 3px solid makerPink;
  animation: spin 2s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
```