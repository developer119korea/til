# PC와 모바일 Drag 처리

- PC브라우저에서는 touch관련 이벤트를 수신하지 못하고, 모바일 브라우저에서는 mouse관련 이벤트를 수신하지 못한다.
- 엘리먼트에 이벤트를 등록할때 공백으로 구분하여 이벤트를 등록하면 여러 이벤트를 한 함수에서 수신할 수 있다.
- 이벤트 타입으로 구분하여 좌표를 얻어올 수 있다.

```js
$(document).ready(function () {
  const gaugeHandle = $("." + parentClassName + " .gauge-handle");
  let isDragging = false;
  let mouseX = 0;

  gaugeHandle.on("mousedown touchstart", function (event) {
    isDragging = true;
    var clientX;
    if (event.type === "mousemove") {
      clientX = event.clientX;
    } else if (event.type === "touchmove") {
      clientX = event.touches[0].clientX;
    }
    mouseX = clientX;
  });

  $(document).on("mousemove touchmove", function (event) {
    if (!isDragging) {
      return;
    }
    var clientX;
    if (event.type === "mousemove") {
      clientX = event.clientX;
    } else if (event.type === "touchmove") {
      clientX = event.touches[0].clientX;
    }

    const deltaX = clientX - mouseX;
    mouseX = clientX;
  });

  $(document).on("mouseup touchend", function () {
    if (!isDragging) {
      return;
    }
    isDragging = false;
  });
});
```
