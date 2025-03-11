# HLS (HTTP Live Streaming) 학습 프로젝트

이 프로젝트는 HTTP Live Streaming (HLS) 기술을 학습하고 실습하기 위한 예제 프로젝트입니다.

## 소개

HLS는 Apple에서 개발한 HTTP 기반의 적응형 스트리밍 프로토콜입니다. 비디오 콘텐츠를 작은 조각으로 나누어 HTTP를 통해 전송하며, 네트워크 상태에 따라 자동으로 품질을 조절할 수 있습니다.

## 사전 요구사항

- FFmpeg `brew install ffmpeg`
- 웹 서버
- 비디오 파일 (예: MP4 형식) `/public/example.mp4`

## FFmpeg를 사용한 HLS 변환

아래 명령어를 사용하여 비디오 파일을 HLS 형식으로 변환할 수 있습니다:

```sh
ffmpeg -i example.mp4 -codec:v libx264 -codec:a aac -strict -2 -hls_time 10 -hls_list_size 0 -f hls output.m3u8
``
```

### 명령어 설명

- `-i example.mp4`: 입력 비디오 파일
- `-codec:v libx264`: H.264 비디오 코덱 사용
- `-codec:a aac`: AAC 오디오 코덱 사용
- `-hls_time 10`: 각 세그먼트의 길이를 10초로 설정
- `-hls_list_size 0`: 모든 세그먼트를 플레이리스트에 유지
- `-f hls`: HLS 포맷으로 출력
- `output.m3u8`: 출력 플레이리스트 파일

## 프로젝트 구조

```sh
├── index.html # 비디오 플레이어 페이지
├── index.js # JavaScript 코드
├── package.json # 프로젝트 설정
└── public/ # 공개 자원 디렉토리
```

## 사용 방법

1. 비디오 파일을 HLS 형식으로 변환
2. 웹 서버 실행

```sh
npm i
npm run start
```

3. 브라우저에서 index.html 열기
