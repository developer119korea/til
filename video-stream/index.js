const express = require("express");
const app = express();

app.use(express.static(__dirname)); // 현재 디렉토리를 정적 파일 제공

app.listen(8000, () => {
  console.log("Server running at http://localhost:8000");
});
