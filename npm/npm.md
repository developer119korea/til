### NPM 동작 원리 요약

- 레지스트리/메타데이터: npm은 레지스트리에서 패키지 메타데이터(package.json)와 tarball을 가져오고, semver 범위를 해석해 설치 버전을 선택한다. 무결성 해시(SRI sha512)로 검증하고 사용자 캐시(`~/.npm`)에 저장한다.

#### 설치 흐름(npm install)

1. package.json 읽기 → dependencies/dev/optional/peer 구분 수집
2. lockfile 우선 적용: package-lock.json이 있으면 정확한 버전·무결성·의존 트리를 그대로 사용
3. 의존성 그래프 해석: OS/CPU/engines, optional 실패 허용, peer는 상위(호스트)가 만족해야 함
4. 충돌 해결/호이스트(hoist): 가능한 한 상위 `node_modules`로 중복 제거(dedupe), 버전 충돌 시 하위에 중첩 설치
5. 가져오기(fetch)·검증: 캐시 적중 시 캐시 사용, 미적중이면 레지스트리에서 tarball 다운로드 후 integrity 체크
6. 배치·링크: `node_modules`에 배치하고 실행 파일은 `node_modules/.bin`에 심볼릭 링크 생성
7. 스크립트 실행: preinstall → install → postinstall → prepare 순서(프로덕션 설치는 devDependencies 제외). `--ignore-scripts`로 비활성 가능

#### 의존성 종류

- dependencies: 런타임 필요
- devDependencies: 개발/테스트 전용
- peerDependencies: 호스트 앱이 직접 설치해야 하는 버전 요구사항(플러그인/프레임워크 조합에서 사용)
- optionalDependencies: 설치 실패해도 계속 진행
- bundledDependencies: 배포 tarball에 함께 묶어 배포

#### node_modules 구조와 모듈 해석◊

- npm v3+는 평탄화(hoisting)로 중복 버전을 상위로 올려 설치 용량을 줄인다.
- 동일 패키지의 서로 다른 버전이 필요하면 하위 폴더에 중첩 설치한다.
- require/resolve 규칙: 호출 파일 기준으로 가장 가까운 `node_modules`부터 위로 탐색한다.

#### 잠금파일과 재현성

- package-lock.json은 실제 설치된 정확한 버전/무결성/트리를 기록하여 동일한 설치를 보장한다.
- CI/팀에서는 `npm ci`로 lockfile을 정확히 재현(깨끗한 설치, 더 빠름).

#### 워크스페이스

- 루트 package.json의 `workspaces`로 멀티 패키지(monorepo)를 구성. 루트에서 설치 시 워크스페이스 간 로컬 링크와 hoist가 자동 처리된다.

#### 캐시/네트워크

- 내용 주소 기반 캐시(`~/.npm`): 같은 콘텐츠는 한 번만 저장. `--prefer-offline`, `--offline` 등으로 동작 제어.
- `.npmrc`로 registry, 인증 토큰, 프록시, scripts 동작 등을 설정.

#### 보안

- `npm audit`으로 취약점 점검, `npm audit fix`로 자동 수정 시도.

#### 배포(publish) 흐름

1. semver에 따라 버전 증가 → 2) 필요 시 `prepublishOnly`/`prepare` 스크립트 실행 → 3) `npm publish`

- 접근은 `~/.npmrc` 토큰으로 인증. 공개 범위(scope)·태그(dist-tag)로 배포 채널 관리 가능(latest, next 등).

#### npx와 실행 파일

- 패키지의 `bin` 필드는 CLI를 노출하고, 설치 시 `node_modules/.bin`에 링크된다.
- `npx <bin>`은 로컬/캐시 우선으로 해당 바이너리를 실행하거나 임시 설치 후 실행한다.

#### 자주 쓰는 플래그

```bash
# 재현 가능한 빠른 설치(깨끗한 환경 필요)
npm ci

# 스크립트 무시(보안·속도)
npm install --ignore-scripts

# 프로덕션 의존성만
npm install --production

# 캐시 우선/오프라인
npm install --prefer-offline
npm install --offline
```

> 한줄 요약: npm은 package.json과 lockfile을 바탕으로 의존성 트리를 해석·평탄화하고, 캐시/무결성 검증을 거쳐 node_modules에 배치한 뒤 스크립트를 실행한다.
