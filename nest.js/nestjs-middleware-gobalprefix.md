# TIL: NestJS 미들웨어와 Global Prefix의 함정

## 🔍 문제 상황

Firebase App Check 미들웨어를 구현하여 모든 API 요청에 토큰 검증을 적용하려고 했다. 미들웨어 코드는 정상적으로 작성했고, `app.module.ts`에서 글로벌하게 적용했지만, 실제 API 엔드포인트 요청이 App Check 검증을 우회하는 문제가 발생했다.

### 클라이언트 요청

```javascript
// 클라이언트에서 이렇게 요청
const url = `${API_BASE_URL}/api/v1/users${queryParams}`;
fetch(url, { ... });
```

### 서버 설정

```typescript
// main.ts
app.setGlobalPrefix('api/v1');

// app.module.ts
consumer.apply(AppCheckMiddleware).forRoutes('*');
```

### 예상했던 동작

- `/api/v1/users` 요청 → App Check 미들웨어 실행 → 토큰 검증

### 실제 발생한 문제

```
[AppCheckMiddleware] 요청 경로: /          👈 이상함!
[AppCheckMiddleware] 원본 URL: /api/v1/users/profile/xxx
[AppCheckMiddleware] Health check 엔드포인트 - 통과  👈 잘못 통과됨!
```

## 🤔 원인 분석

### NestJS의 미들웨어 실행 순서 문제

1. **Express 미들웨어 스택**에서 커스텀 미들웨어가 먼저 실행됨
2. **Global Prefix 적용**이 나중에 일어남
3. 미들웨어에서 `req.path`는 Global Prefix가 제거된 후의 경로를 받음

```typescript
// 실제 요청: /api/v1/users/profile/xxx
console.log(req.path); // "/"          ← Global Prefix 제거됨
console.log(req.originalUrl); // "/api/v1/users/profile/xxx"  ← 원본 URL
```

### 왜 이런 일이 발생하는가?

NestJS는 내부적으로 Express의 라우터를 사용하는데:

- Global Prefix 설정은 별도의 Express 라우터를 생성
- 미들웨어는 상위 라우터에서 실행
- 하위 라우터(Global Prefix가 적용된)로 전달될 때 경로가 변경됨

## 🚨 근본 원인: Global Prefix 설계 문제

### 현재 설정의 문제점

```typescript
// main.ts
app.setGlobalPrefix('api/v1'); // 👈 이것이 문제!
```

### 왜 문제인가?

1. **미들웨어 실행 순서**

   ```
   요청: /api/v1/users
   ↓
   Express 미들웨어 (AppCheck) 실행
   ↓
   Global Prefix 처리 (/api/v1 제거)
   ↓
   실제 라우터: /users
   ```

2. **미들웨어에서는 원본 경로를 봐야 함**
   - `req.path`: `/users` (Global Prefix 제거됨)
   - `req.originalUrl`: `/api/v1/users` (원본 유지)

## 💡 더 나은 해결책들

### 1. Global Prefix 제거 (권장)

```typescript
// main.ts - Global Prefix 제거
// app.setGlobalPrefix('api/v1'); // 삭제

// 각 컨트롤러에서 개별 설정
@Controller('api/v1/users')
export class UsersController { ... }

@Controller('api/v1/admins')
export class AdminsController { ... }
```

**장점:**

- 미들웨어가 정확한 경로를 받음
- 경로 처리가 명확해짐
- 디버깅이 쉬워짐

### 2. 라우터 모듈 구조화

```typescript
// api-v1.module.ts
@Module({
  imports: [UsersModule, AdminsModule],
})
export class ApiV1Module {}

// app.module.ts
@Module({
  imports: [
    RouterModule.register([
      {
        path: 'api/v1',
        module: ApiV1Module,
      },
    ]),
    ApiV1Module,
  ],
})
export class AppModule {}
```

### 3. Guard 방식으로 변경

```typescript
// app-check.guard.ts
@Injectable()
export class AppCheckGuard implements CanActivate {
  async canActivate(context: ExecutionContext): Promise<boolean> {
    const request = context.switchToHttp().getRequest();
    const appCheckToken = request.header('X-Firebase-AppCheck');

    if (!appCheckToken) {
      throw new UnauthorizedException('App Check token missing');
    }

    try {
      await admin.appCheck().verifyToken(appCheckToken);
      return true;
    } catch (error) {
      throw new UnauthorizedException('Invalid App Check token');
    }
  }
}

// main.ts
app.useGlobalGuards(new AppCheckGuard());
```

**장점:**

- NestJS의 실행 컨텍스트 내에서 동작
- Global Prefix 영향 없음
- Exception Filter와 자연스럽게 연동

## 🔧 최종 구현

```typescript
@Injectable()
export class AppCheckMiddleware implements NestMiddleware {
  async use(req: Request, res: Response, next: NextFunction) {
    console.log('=== AppCheckMiddleware 실행 ===');
    console.log(`원본 URL: ${req.originalUrl}`);
    console.log(`처리된 경로: ${req.path}`);

    // originalUrl을 기준으로 예외 처리
    const originalPath = req.originalUrl.split('?')[0];

    if (
      originalPath === '/health' ||
      originalPath === '/api-docs' ||
      originalPath.startsWith('/api-docs/') ||
      originalPath === '/'
    ) {
      return next();
    }

    const appCheckToken = req.header('X-Firebase-AppCheck');

    if (!appCheckToken) {
      throw new HttpException(
        'App Check token missing',
        HttpStatus.UNAUTHORIZED,
      );
    }

    try {
      await admin.appCheck().verifyToken(appCheckToken);
      next();
    } catch (error) {
      throw new HttpException(
        `Invalid App Check token: ${error.message}`,
        HttpStatus.UNAUTHORIZED,
      );
    }
  }
}
```

## 📝 수정된 핵심 교훈

1. **Global Prefix는 신중하게 사용하라**
   - 미들웨어와의 상호작용 문제 발생 가능
   - 명시적인 컨트롤러 경로가 더 명확함

2. **NestJS의 실행 순서 이해**
   - Express 미들웨어 → Global Prefix 처리 → NestJS 라우터
   - Guard는 NestJS 컨텍스트 내에서 실행됨

3. **프레임워크의 제약사항 인지**
   - 편의 기능이 항상 최선은 아님
   - 명시적인 구현이 디버깅에 유리함

## 🔗 관련 링크

- [NestJS Middleware 공식 문서](https://docs.nestjs.com/middleware)
- [Express Request 객체 문서](https://expressjs.com/en/api.html#req)
- [Firebase App Check 문서](https://firebase.google.com/docs/app-check)
