# CI/CD 파이프라인 최적화 및 AWS CodeDeploy 구현

## 🎯 배경

기존 수동 배포 프로세스의 한계를 해결하고자 GitHub Actions와 AWS CodeDeploy를 활용한 완전 자동화 파이프라인을 구축. 프로덕션 환경의 무중단 서비스를 위한 배포 전략을 검토하고 최적화 포인트를 정리.

## 📋 구현 세부사항

### 아키텍처 개요

```
GitHub (main branch) → GitHub Actions → S3 Staging → CodeDeploy → EC2 Production
```

### 워크플로우 설정 분석

```yaml
# 전략적 매트릭스 활용으로 Node.js 버전 관리
strategy:
  matrix:
    node-version: [20.11.x] # LTS 버전 고정으로 일관성 확보

# 환경별 Secret 분리로 보안 강화
environment: production
env:
  ENV_FILE: ${{ secrets.ENV_FILE }} # 환경변수 중앙 관리
```

### 빌드 최적화 포인트

**1. 의존성 관리**

```yaml
- name: Install dependencies
  run: npm ci # package-lock.json 기반 deterministic install
```

- `npm install` 대신 `npm ci` 사용으로 빌드 일관성 및 성능 향상
- cache 설정으로 의존성 다운로드 시간 최적화

**2. 자산 패키징 전략**

```yaml
- name: Make zip file
  run: zip -qq -r ./$GITHUB_SHA.zip . -x "node_modules/*"
```

- node_modules 제외로 패키지 크기 최소화
- `$GITHUB_SHA` 활용으로 버전 추적 가능한 아티팩트 생성

**3. 환경 설정**

```bash
echo $ENV_FILE | tr " " "\n" > .env
```

- GitHub Secrets를 활용한 런타임 환경변수 주입
- Space-to-newline 변환으로 다중 환경변수 처리

### AWS 통합 아키텍처

**S3 스테이징 전략**

```yaml
aws s3 cp ./$GITHUB_SHA.zip s3://$S3_BUCKET_NAME/deploy/$PROJECT_NAME/$GITHUB_SHA.zip
```

- 버전별 아티팩트 관리로 롤백 지원
- 지역별 S3 버킷으로 네트워크 레이턴시 최적화

**CodeDeploy 설정**

```yaml
--deployment-config-name CodeDeployDefault.AllAtOnce
```

- AllAtOnce 전략: 빠른 배포 vs 가용성 트레이드오프
- 단일 인스턴스 환경에 적합한 전략 선택

### AppSpec 설정

```yaml
# Blue-Green 배포를 위한 Hook 전략
hooks:
  ApplicationStart:
    - location: scripts/deploy.sh
      timeout: 300
      runas: ubuntu
```

**권한 관리**

```yaml
permissions:
  - object: /home/ubuntu/projects/e-commerce
    mode: 755 # 실행 권한 필수
    owner: ubuntu
```

### 배포 스크립트 최적화

```bash
#!/bin/bash
# NVM 환경 초기화 - 서버 Node.js 버전 일관성 보장
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"

# Process Management with PM2
cd /home/ubuntu/projects/e-commerce
npm install && pm2 restart e-commerce
```

## 🔧 성능 및 안정성 개선

### 1. 캐싱 전략

- **GitHub Actions Cache**: `cache: 'npm'`로 의존성 캐싱
- **Build Artifact**: S3 버전 관리로 이전 빌드 재사용 가능

### 2. 오류 처리 및 모니터링

```yaml
# AWS CLI 명령어 실패 시 워크플로우 중단
set -e # Exit on any command failure
```

### 3. 보안 고려사항

- **IAM Role 최소 권한**: S3, CodeDeploy 특정 권한만 부여
- **Secret Rotation**: 정기적인 AWS Access Key 교체 필요
- **VPC Security Group**: 특정 포트만 허용

## 🚀 고려한 대안 방법들

### 셀프 호스팅 러너 옵션

```yaml
runs-on: self-hosted # 맥북 로컬 환경
```

**장단점:**

- ✅ AWS 비용 절약, 빌드 환경 완전 제어
- ❌ 인프라 관리 부담, 단일 장애점

### Docker 기반 배포

```dockerfile
FROM node:20.11-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
RUN npm run build
CMD ["npm", "start"]
```

### 컨테이너 오케스트레이션 마이그레이션 계획

- **현재**: PM2 프로세스 관리
- **Next**: Docker Compose → ECS/Kubernetes 고려

## 🎯 최적화 기회

### 1. 빌드 성능

- **Multi-stage Docker**: 빌드 레이어 분리로 이미지 크기 최적화
- **Parallel Jobs**: 테스트와 빌드 병렬 실행

### 2. 배포 전략 진화

```yaml
# 향후 Blue-Green 배포 고려
--deployment-config-name CodeDeployDefault.BlueGreenCanary10Percent5Minutes
```

### 3. 모니터링 통합

- **CloudWatch**: 배포 메트릭 수집
- **Slack/Discord**: 배포 알림 통합
- **Health Check**: 배포 후 서비스 상태 검증

## 💡 핵심 인사이트

1. **재현 가능한 빌드**: `npm ci` + package-lock.json으로 재현 가능한 빌드 환경
2. **아티팩트 버전 관리**: `$GITHUB_SHA` 활용으로 추적 가능한 릴리스 관리
3. **환경 격리**: GitHub Environments로 프로덕션 보호
4. **점진적 마이그레이션**: 기존 PM2 → Container 단계적 전환 전략

## 🔄 다음 단계

- [ ] Blue-Green 배포 전략 도입 검토
- [ ] Container 기반 배포로 마이그레이션 계획
- [ ] 통합 테스트 자동화 추가
- [ ] 배포 메트릭 대시보드 구축

---

**아키텍처 결정**: AWS CodeDeploy 선택 이유는 기존 EC2 인프라와의 호환성, 롤백 지원, 단계적 배포 전략 지원 때문. 향후 컨테이너 환경으로 전환 시 ECS Blue/Green 배포 고려 중.
