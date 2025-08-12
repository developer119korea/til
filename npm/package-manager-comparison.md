# npm vs pnpm vs yarn 비교

패키지 매니저별 핵심 차이점과 특징을 정리한다.

## 전체 비교표

| 특징          | npm               | pnpm                 | yarn (classic) | yarn berry        |
| ------------- | ----------------- | -------------------- | -------------- | ----------------- |
| 설치 방식     | 호이스팅 + 중첩   | 심볼릭 링크 스토어   | 호이스팅       | PnP (Plug'n'Play) |
| 디스크 사용량 | 높음 (중복 저장)  | 낮음 (글로벌 스토어) | 높음           | 낮음 (ZIP)        |
| 설치 속도     | 느림              | 빠름                 | 보통           | 매우 빠름         |
| 모노레포 지원 | 워크스페이스      | 내장 지원            | 워크스페이스   | 강력한 내장 지원  |
| 잠금파일      | package-lock.json | pnpm-lock.yaml       | yarn.lock      | yarn.lock         |

### npm 특징

**장점:**

- Node.js 기본 내장, 별도 설치 불필요
- 가장 널리 사용되어 호환성 문제 적음
- 단순한 구조로 디버깅 용이

**단점:**

- 중복 설치로 디스크 공간 낭비
- 상대적으로 느린 설치 속도
- phantom dependencies 문제 (호이스팅으로 인한 의도치 않은 의존성 접근)

**디렉토리 구조:**

```text
node_modules/
├── package-a/          # 호이스팅됨
├── package-b/
│   └── node_modules/   # 버전 충돌 시 중첩
│       └── package-a@different-version/
```

### pnpm 특징

**장점:**

- 글로벌 스토어 + 심볼릭 링크로 디스크 공간 절약 (최대 90%)
- 빠른 설치 속도 (캐시 재사용)
- 엄격한 의존성 해석으로 phantom dependencies 방지
- 모노레포 지원 우수

**단점:**

- 심볼릭 링크 미지원 환경에서 문제 발생 가능
- 일부 도구와 호환성 이슈
- 상대적으로 적은 생태계

**디렉토리 구조:**

```text
node_modules/
├── .pnpm/                    # 실제 패키지들의 플랫 구조
│   ├── package-a@1.0.0/
│   └── package-b@2.0.0/
├── package-a -> .pnpm/package-a@1.0.0/node_modules/package-a
└── package-b -> .pnpm/package-b@2.0.0/node_modules/package-b
```

**글로벌 스토어:**

```bash
~/.pnpm-store/
└── v3/
    └── files/
        ├── 00/
        └── 01/  # 내용 주소 기반 저장
```

### yarn classic (v1) 특징

**장점:**

- npm보다 빠른 병렬 설치
- 결정적 설치 (yarn.lock)
- 워크스페이스 지원

**단점:**

- npm과 유사한 호이스팅 문제
- 유지보수 중단 (레거시)

### yarn berry (v2+) 특징

**장점:**

- PnP로 node_modules 없이 ZIP 기반 설치
- 매우 빠른 설치 및 실행
- 강력한 모노레포 지원
- 엄격한 의존성 해석

**단점:**

- PnP 호환성 문제 (일부 도구 미지원)
- 학습 곡선 가파름
- 에디터 설정 필요

**PnP 구조:**

```text
.pnp.cjs              # 의존성 해석 로직
.yarn/cache/          # ZIP 파일들
├── package-a-npm-1.0.0.zip
└── package-b-npm-2.0.0.zip
```

### 성능 비교

**설치 속도 (일반적):**

1. yarn berry (PnP) - 가장 빠름
2. pnpm - 빠름 (글로벌 스토어 활용)
3. yarn classic - 보통
4. npm - 가장 느림

**디스크 사용량:**

1. yarn berry - 가장 적음 (압축)
2. pnpm - 적음 (중복 제거)
3. npm/yarn classic - 많음 (중복 저장)

### 모노레포 지원

**pnpm:**

```yaml
# pnpm-workspace.yaml
packages:
  - "packages/*"
  - "apps/*"
```

**yarn:**

```json
// package.json
{
  "workspaces": ["packages/*", "apps/*"]
}
```

**npm (v7+):**

```json
// package.json
{
  "workspaces": ["packages/*", "apps/*"]
}
```

### 선택 기준

**npm 사용:**

- 단순한 프로젝트
- 최대 호환성 필요
- 별도 도구 설치 부담

**pnpm 사용:**

- 디스크 공간 절약 중요
- 모노레포 프로젝트
- 엄격한 의존성 관리 필요

**yarn berry 사용:**

- 최고 성능 필요
- 복잡한 모노레포
- PnP 호환성 문제 해결 가능

**yarn classic 사용:**

- 레거시 호환성 필요 (권장하지 않음)

### 마이그레이션 가이드

**npm → pnpm:**

```bash
# 기존 node_modules 제거
rm -rf node_modules package-lock.json

# pnpm 설치 및 의존성 설치
npm install -g pnpm
pnpm install

# 스크립트는 그대로 사용
pnpm run dev
pnpm test
```

**npm → yarn berry:**

```bash
# yarn berry 설치
npm install -g yarn
yarn set version berry

# 의존성 설치
rm -rf node_modules package-lock.json
yarn install

# PnP 설정 (필요시)
yarn dlx @yarnpkg/sdks vscode
```

### 명령어 비교

| 작업             | npm                   | pnpm               | yarn                  |
| ---------------- | --------------------- | ------------------ | --------------------- |
| 설치             | `npm install`         | `pnpm install`     | `yarn`                |
| 패키지 추가      | `npm install lodash`  | `pnpm add lodash`  | `yarn add lodash`     |
| 개발 의존성 추가 | `npm install -D jest` | `pnpm add -D jest` | `yarn add -D jest`    |
| 글로벌 설치      | `npm install -g pkg`  | `pnpm add -g pkg`  | `yarn global add pkg` |
| 스크립트 실행    | `npm run dev`         | `pnpm run dev`     | `yarn dev`            |
| 의존성 업데이트  | `npm update`          | `pnpm update`      | `yarn upgrade`        |

> 한줄 요약: npm은 호환성, pnpm은 효율성, yarn berry는 성능에 특화되어 있으며, 프로젝트 규모와 요구사항에 따라 선택하면 된다.
