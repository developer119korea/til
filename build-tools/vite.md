# Vite

## 개요

Vite는 현대적인 프론트엔드 개발을 위한 빠르고 효율적인 빌드 도구이자 개발 서버다. Evan You(Vue.js 창시자)가 개발했으며, ES 모듈을 활용한 빠른 개발 서버와 Rollup 기반의 최적화된 프로덕션 빌드를 제공한다.

## 주요 특징

### 1. 빠른 개발 서버

- **ES 모듈(ESM) 활용**: 브라우저가 모듈을 직접 로드하여 번들링 없이 빠른 코드 변경 반영
- **즉시 시작**: 전체 번들링 과정 없이 개발 서버 실행
- **핫 모듈 리플레이스먼트(HMR)**: 페이지 새로고침 없이 변경된 모듈만 즉시 업데이트

### 2. 효율적인 프로덕션 빌드

- **Rollup 기반**: 코드 최적화와 트리 셰이킹에 강력한 Rollup을 내부적으로 사용
- **최소 번들 크기**: 트리 셰이킹을 통한 최종 번들 크기 최소화
- **다양한 포맷 지원**: CSS, JavaScript, TypeScript, JSX 기본 지원

### 3. 간단한 설정

- **제로 컨피그**: 기본 설정으로 대부분의 프로젝트에서 바로 사용 가능
- **유연한 커스터마이징**: 필요시 `vite.config.js`를 통한 세밀한 설정
- **프레임워크 템플릿**: Vue, React, Svelte 등 다양한 프레임워크 템플릿 제공

### 4. 광범위한 생태계 지원

- **멀티 프레임워크**: Vue.js뿐만 아니라 React, Svelte, Preact 등 지원
- **플러그인 시스템**: 커뮤니티 플러그인과 Webpack 플러그인 호환성

## 장단점

### 장점

- **속도**: ES 모듈 기반의 빠른 시작과 HMR
- **간결함**: 복잡한 설정 없이 강력한 기능 제공
- **확장성**: 플러그인 시스템으로 대규모 프로젝트 지원
- **현대적**: 최신 브라우저와 ES 모듈 적극 활용

### 단점

- **최신 브라우저 의존성**: ES 모듈 사용으로 구형 브라우저(IE) 지원 제한
- **플러그인 생태계**: Webpack 대비 상대적으로 적은 플러그인
- **학습 곡선**: Webpack에서 전환시 ES 모듈 방식 이해 필요

## 비교 도구들

### 1. Webpack

- **특징**: 모든 자원을 번들링하는 모듈 번들러
- **차이점**:
  - Webpack: 전체 번들링 후 제공 vs Vite: ES 모듈 활용
  - 설정 복잡도: Webpack 높음 vs Vite 낮음
  - 브라우저 지원: Webpack 레거시 지원 vs Vite 모던 브라우저

### 2. esbuild

- **특징**: Go로 작성된 초고속 빌드 도구
- **관계**: Vite 내부에서 의존성 사전 번들링에 사용

### 3. Rollup

- **특징**: 트리 셰이킹과 경량 번들 생성에 특화
- **관계**: Vite 프로덕션 빌드의 핵심 엔진

### 4. Parcel

- **특징**: 제로 컨피그 번들러
- **차이점**: Vite가 ES 모듈과 HMR에서 우위

### 5. Turbopack

- **특징**: Vercel의 Webpack 후속작 (Rust 기반)
- **현황**: Next.js와 통합, 아직 초기 단계

## 프로젝트 세팅 비교

### Vite 세팅

```bash
# 프로젝트 생성
npm create vite@latest

# 의존성 설치 및 개발 서버 실행
npm install
npm run dev
```

**특징**:

- 대화형 CLI로 프레임워크/언어 선택
- 몇 분 내 설정 완료
- 제로 컨피그로 바로 사용 가능

### Webpack 세팅

```bash
# 수동 설정
npm init -y
npm install webpack webpack-cli webpack-dev-server --save-dev
# 로더, 플러그인 추가 설치 및 설정 필요
```

**특징**:

- 수동 설정 필요
- 로더와 플러그인 세밀한 구성
- 초보자는 30분 이상 소요

### 설정 파일 비교

#### Vite (`vite.config.js`)

```javascript
import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
  },
  build: {
    outDir: "dist",
  },
});
```

#### Webpack (`webpack.config.js`)

```javascript
const path = require("path");
const HtmlWebpackPlugin = require("html-webpack-plugin");

module.exports = {
  entry: "./src/index.js",
  output: {
    path: path.resolve(__dirname, "dist"),
    filename: "bundle.js",
  },
  module: {
    rules: [
      {
        test: /\.js$/,
        exclude: /node_modules/,
        use: "babel-loader",
      },
      {
        test: /\.css$/,
        use: ["style-loader", "css-loader"],
      },
    ],
  },
  plugins: [
    new HtmlWebpackPlugin({
      template: "./src/index.html",
    }),
  ],
  devServer: {
    port: 3000,
  },
};
```

## 사용 사례

### Vite 적합한 경우

- 현대적인 프론트엔드 프로젝트 (Vue, React, Svelte)
- 빠른 개발 서버와 HMR이 중요한 경우
- 간단한 설정을 선호하는 프로젝트
- 소규모~중규모 SPA

### Webpack 적합한 경우

- 복잡한 빌드 요구사항
- 레거시 브라우저 지원 필요
- 대규모 엔터프라이즈 프로젝트
- 기존 Webpack 기반 프로젝트 유지보수

## 시작하기

### 프로젝트 생성

```bash
# npm
npm create vite@latest

# yarn
yarn create vite

# pnpm
pnpm create vite
```

### 개발 서버 실행

```bash
npm install
npm run dev
```

## 결론

Vite는 빠르고 간편한 개발 경험을 제공하는 현대적인 프론트엔드 개발 도구다. 특히 Vue.js나 React 기반 프로젝트에서 Webpack의 강력한 대안으로 자리잡고 있으며, ES 모듈을 활용한 빠른 개발 서버와 간단한 설정이 주요 강점이다. 프로젝트의 요구사항과 팀의 기술 수준을 고려하여 적절한 빌드 도구를 선택하는 것이 중요하다.
