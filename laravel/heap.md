# Laravel에서 힙 메모리 부족 문제 해결하기

## 문제 상황

연말 정산 작업을 위해 한 해 동안 발생한 모든 거래 데이터를 조회하다가 힙 메모리 부족 문제가 발생했다. 약 100만 건의 거래 데이터를 `Transaction::all()`로 한 번에 불러오려고 했더니 PHP Fatal Error가 발생하며 서버가 다운되었다.

```php
// ❌ 문제가 발생한 코드
$transactions = Transaction::whereYear('created_at', 2024)->get(); // 100만 건 조회
$totalAmount = 0;
foreach ($transactions as $transaction) {
    $totalAmount += $transaction->amount;
}
```

이런 대용량 데이터베이스 조회 시 발생하는 힙 메모리 부족 문제의 해결 방법을 정리해보자.

## 힙 메모리 부족의 주요 원인

### 1. 대용량 데이터 처리

```php
// ❌ 문제가 되는 코드
$users = User::all(); // 수십만 건의 데이터를 한 번에 로드
foreach ($users as $user) {
    // 처리 로직
}
```

### 2. 무한 루프 또는 재귀

```php
// ❌ 메모리 누수 발생
public function recursiveFunction($data) {
    $result = [];
    // 종료 조건이 없거나 잘못된 재귀
    return $this->recursiveFunction($data);
}
```

### 3. 큰 배열이나 객체의 누적

```php
// ❌ 메모리에 계속 누적
$bigArray = [];
for ($i = 0; $i < 1000000; $i++) {
    $bigArray[] = new SomeObject();
}
```

## Laravel에서의 해결 방법

### 1. 청크(Chunk) 사용하여 배치 처리

```php
// ✅ 메모리 효율적인 정산 처리
$totalAmount = 0;
Transaction::whereYear('created_at', 2024)
    ->chunk(1000, function ($transactions) use (&$totalAmount) {
        foreach ($transactions as $transaction) {
            $totalAmount += $transaction->amount;
            // 정산 로직 처리
            $this->processTransactionForSettlement($transaction);
        }
    });
```

### 2. 커서 페이지네이션 활용

```php
// ✅ 대용량 거래 데이터 스트리밍 처리
$totalAmount = 0;
foreach (Transaction::whereYear('created_at', 2024)->cursor() as $transaction) {
    $totalAmount += $transaction->amount;
    // CSV 파일로 내보내기
    $this->writeToSettlementCsv($transaction);
}
```

### 3. Laravel Queue로 비동기 처리

```php
// ✅ 정산 작업을 Queue Job으로 분할
class ProcessSettlementJob implements ShouldQueue
{
    private $year;
    private $offset;
    private $limit;

    public function __construct($year, $offset = 0, $limit = 1000)
    {
        $this->year = $year;
        $this->offset = $offset;
        $this->limit = $limit;
    }

    public function handle()
    {
        $transactions = Transaction::whereYear('created_at', $this->year)
            ->offset($this->offset)
            ->limit($this->limit)
            ->get();

        foreach ($transactions as $transaction) {
            $this->processTransactionForSettlement($transaction);
        }

        // 다음 배치가 있으면 새로운 Job 등록
        $nextOffset = $this->offset + $this->limit;
        if (Transaction::whereYear('created_at', $this->year)->offset($nextOffset)->exists()) {
            ProcessSettlementJob::dispatch($this->year, $nextOffset, $this->limit);
        }
    }
}

// 정산 작업 시작
ProcessSettlementJob::dispatch(2024);
```

### 4. DB 집계 함수 활용

```php
// ✅ 데이터베이스 레벨에서 집계 처리
$totalAmount = Transaction::whereYear('created_at', 2024)->sum('amount');
$transactionCount = Transaction::whereYear('created_at', 2024)->count();
$avgAmount = Transaction::whereYear('created_at', 2024)->avg('amount');

// 월별 정산 데이터
$monthlySettlement = Transaction::whereYear('created_at', 2024)
    ->selectRaw('MONTH(created_at) as month, SUM(amount) as total, COUNT(*) as count')
    ->groupBy('month')
    ->get();
```

### 5. 메모리 제한 설정 및 모니터링

```php
// ✅ PHP 메모리 제한 증가 (정산 작업용)
ini_set('memory_limit', '1G');
ini_set('max_execution_time', 3600); // 1시간

// ✅ 메모리 사용량 모니터링
$startMemory = memory_get_usage(true);
echo "정산 작업 시작 - 메모리 사용량: " . ($startMemory / 1024 / 1024) . " MB\n";

// 정산 로직 실행...

$endMemory = memory_get_usage(true);
echo "정산 작업 완료 - 최종 메모리 사용량: " . ($endMemory / 1024 / 1024) . " MB\n";
echo "최대 메모리 사용량: " . (memory_get_peak_usage(true) / 1024 / 1024) . " MB\n";
```

### 6. 불필요한 관계 로딩 최적화

```php
// ❌ N+1 쿼리 문제로 메모리 과다 사용
$transactions = Transaction::whereYear('created_at', 2024)->get();
foreach ($transactions as $transaction) {
    echo $transaction->user->name; // 각 거래마다 사용자 정보 쿼리
    echo $transaction->product->name; // 각 거래마다 상품 정보 쿼리
}

// ✅ Eager Loading으로 최적화
Transaction::whereYear('created_at', 2024)
    ->with(['user:id,name', 'product:id,name']) // 필요한 컬럼만 로드
    ->chunk(1000, function ($transactions) {
        foreach ($transactions as $transaction) {
            // 정산 리포트 생성
            $this->generateSettlementReport($transaction);
        }
    });
```

### 7. 임시 파일 사용

```php
// ✅ 대용량 정산 데이터를 CSV 파일로 처리
$csvFile = fopen(storage_path('settlement_2024.csv'), 'w');
fputcsv($csvFile, ['거래ID', '사용자', '금액', '날짜']);

Transaction::whereYear('created_at', 2024)
    ->with('user:id,name')
    ->chunk(1000, function ($transactions) use ($csvFile) {
        foreach ($transactions as $transaction) {
            fputcsv($csvFile, [
                $transaction->id,
                $transaction->user->name,
                $transaction->amount,
                $transaction->created_at->format('Y-m-d H:i:s')
            ]);
        }
    });

fclose($csvFile);
```

### 8. 가비지 컬렉션 강제 실행

```php
// ✅ 정산 작업 중 명시적 메모리 해제
Transaction::whereYear('created_at', 2024)
    ->chunk(1000, function ($transactions) {
        foreach ($transactions as $transaction) {
            $this->processTransactionForSettlement($transaction);
        }

        // 청크 처리 후 메모리 해제
        unset($transactions);
        gc_collect_cycles(); // 가비지 컬렉터 실행

        // 메모리 사용량 체크
        $currentMemory = memory_get_usage(true) / 1024 / 1024;
        if ($currentMemory > 500) { // 500MB 초과 시 경고
            Log::warning("정산 처리 중 메모리 사용량 높음: {$currentMemory} MB");
        }
    });
```

## 모니터링 및 디버깅

### Laravel Telescope를 통한 메모리 사용량 추적

```php
// config/telescope.php에서 설정
'watchers' => [
    Watchers\RequestWatcher::class => [
        'enabled' => env('TELESCOPE_REQUEST_WATCHER', true),
        'size_limit' => 64,
    ],
],
```

### 커스텀 미들웨어로 메모리 모니터링

```php
class MemoryMonitorMiddleware
{
    public function handle($request, Closure $next)
    {
        $startMemory = memory_get_usage();

        $response = $next($request);

        $endMemory = memory_get_usage();
        $memoryUsed = ($endMemory - $startMemory) / 1024 / 1024;

        Log::info("Memory used: {$memoryUsed} MB");

        return $response;
    }
}
```

## 프로덕션 환경 설정

### php.ini 최적화

```ini
memory_limit = 512M
max_execution_time = 300
upload_max_filesize = 100M
post_max_size = 100M
```

### Laravel 설정 최적화

```php
// config/database.php
'options' => [
    PDO::MYSQL_ATTR_USE_BUFFERED_QUERY => false, // 메모리 절약
],
```

## 결론

힙 메모리 부족 문제는 다음과 같은 방법들로 해결할 수 있다:

1. **데이터 처리 최적화**: 청크, 커서, 페이지네이션 활용
2. **비동기 처리**: Laravel Queue 사용
3. **메모리 모니터링**: 사용량 추적 및 제한 설정
4. **코드 최적화**: N+1 문제 해결, 불필요한 객체 제거
5. **시스템 설정**: PHP 메모리 제한 조정

특히 Laravel에서는 Eloquent ORM의 다양한 기능(`chunk()`, `cursor()`, `with()` 등)을 활용하면 메모리 효율적인 애플리케이션을 구현할 수 있다.
