# `display: none`과 `visibility: hidden`의 차이점

## 요약

- `display: none`: 요소를 완전히 제거하여 공간을 차지하지 않음
- `visibility: hidden`: 요소를 숨기지만 공간은 그대로 유지

### 상세 차이점

#### 1. 공간 점유 여부

```css
/* display: none - 요소가 차지하는 공간도 제거 */
.hidden-display {
  display: none;
}

/* visibility: hidden - 요소는 숨겨지지만 공간은 유지 */
.hidden-visibility {
  visibility: hidden;
}
```

#### 2. DOM 구조에서의 차이

- **display: none**: DOM에서 완전히 제거되어 레이아웃에 영향 없음
- **visibility: hidden**: DOM에 존재하지만 보이지 않음, 레이아웃은 유지

#### 3. 자식 요소에 미치는 영향

```css
/* display: none - 모든 자식 요소도 함께 숨겨짐 */
.parent-display-none {
  display: none;
}
.parent-display-none .child {
  display: block; /* 무효함 - 부모가 display: none이면 자식도 보이지 않음 */
}

/* visibility: hidden - 자식 요소를 visible로 설정하면 보임 */
.parent-visibility-hidden {
  visibility: hidden;
}
.parent-visibility-hidden .child {
  visibility: visible; /* 유효함 - 자식만 보임 */
}
```

#### 4. 성능 차이점

- **display: none**: 리플로우(reflow)와 리페인트(repaint) 발생
- **visibility: hidden**: 리페인트만 발생 (성능상 더 유리)

#### 5. JavaScript 접근성

```javascript
// display: none인 요소
const hiddenElement = document.querySelector(".hidden-display");
console.log(hiddenElement.offsetWidth); // 0
console.log(hiddenElement.offsetHeight); // 0

// visibility: hidden인 요소
const invisibleElement = document.querySelector(".hidden-visibility");
console.log(invisibleElement.offsetWidth); // 실제 너비값
console.log(invisibleElement.offsetHeight); // 실제 높이값
```

#### 6. 실제 사용 예시

```html
<div class="container">
  <div class="item display-none">Display None</div>
  <div class="item visibility-hidden">Visibility Hidden</div>
  <div class="item">Visible Item</div>
</div>

<style>
  .container {
    border: 1px solid #ccc;
    padding: 10px;
  }

  .item {
    background-color: lightblue;
    padding: 10px;
    margin: 5px 0;
  }

  .display-none {
    display: none; /* 완전히 사라짐, 공간도 없음 */
  }

  .visibility-hidden {
    visibility: hidden; /* 보이지 않지만 공간은 유지 */
  }
</style>
```

### 언제 사용할까?

#### `display: none` 사용 시점

- 요소를 완전히 제거하고 싶을 때
- 반응형 디자인에서 특정 화면 크기에서 요소를 숨길 때
- 모달, 드롭다운 등 토글 기능 구현 시

#### `visibility: hidden` 사용 시점

- 레이아웃을 유지하면서 요소만 숨기고 싶을 때
- 애니메이션 효과와 함께 사용할 때
- 일시적으로 요소를 숨기되 공간은 확보해두고 싶을 때

### 추가 참고사항

- `opacity: 0`도 요소를 투명하게 만들지만, 클릭 이벤트는 여전히 발생
- `visibility: collapse`는 table 요소에서만 사용하며, `display: none`과 유사하게 동작

---
