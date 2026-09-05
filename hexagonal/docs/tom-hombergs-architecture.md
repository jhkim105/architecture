# 톰 홈버그(Tom Hombergs)의 실전 헥사고날 아키텍처 가이드

이 문서는 톰 홈버그(Tom Hombergs)의 저서 **《만들면서 배우는 클린 아키텍처 (Get Your Hands Dirty on Clean Architecture)》**에서 제시하는 실무 중심의 헥사고날 아키텍처(Ports and Adapters) 설계 방식과 구현 패턴을 정리합니다.

---

## 1. 개요 및 설계 철학

전통적인 계층형 아키텍처(Layered Architecture)는 데이터베이스 중심의 사고로 인해 영속성 계층이 모든 계층의 기반이 되는 문제가 있습니다.

톰 홈버그 스타일의 헥사고날 아키텍처는 **도메인 로직과 유스케이스를 시스템의 중심**에 두고, 웹(HTTP/REST), 데이터베이스(JPA/SQL), 외부 메시징(Kafka/RabbitMQ) 등 모든 외부 요소를 **포트(Port)와 어댑터(Adapter)**를 통해 완전히 분리합니다.

```
       [ Web / REST ] (Inbound Adapter)
              │
              ▼
   ┌───────────────────────┐
   │ Inbound Port          │ (SendMoneyUseCase, SendMoneyCommand)
   └──────────┬────────────┘
              ▼
   ┌───────────────────────┐
   │ Application Service   │ (SendMoneyService)
   │  - 유스케이스 흐름 제어 │
   └──────────┬────────────┘
              ▼
   ┌───────────────────────┐ ┌───────────────────────┐
   │ Pure Domain Model     │ │ Outbound Port         │ (LoadAccountPort, UpdateAccountStatePort)
   │  - Account, Money     │ └───────────┬───────────┘
   └───────────────────────┘             ▼
                              [ Persistence Adapter ] (Outbound Adapter: JPA/DB)
```

---

## 2. 톰 홈버그 아키텍처의 5가지 핵심 설계 패턴

### ① 유스케이스별 전용 서비스 분리 (Single Responsibility)
하나의 거대한 `AccountService`에 CRUD와 송금/입출금을 모두 몰아넣지 않고, **유스케이스마다 독립된 서비스 클래스(`SendMoneyService`, `GetAccountBalanceService`)**를 만듭니다.

* **효과**:
  * 클래스가 작고 명확해져 유지보수와 테스트가 쉬워집니다.
  * 여러 개발자가 동시에 작업할 때 Git 머지 충돌(Conflict)이 획기적으로 줄어듭니다.
  * 불필요한 의존성 주입을 방지합니다.

```kotlin
@Service
@Transactional
class SendMoneyService(
    private val loadAccountPort: LoadAccountPort,
    private val accountLockPort: AccountLockPort,
    private val updateAccountStatePort: UpdateAccountStatePort
) : SendMoneyUseCase {

    override fun sendMoney(command: SendMoneyCommand): Boolean {
        // 1. 락 획득 -> 2. 엔티티 로드 -> 3. 도메인 로직 수행 -> 4. 상태 갱신 -> 5. 락 해제
        ...
    }
}
```

---

### ② 자기 유효성 검증 입력 모델 (Self-Validating Command)
입력값 검증(Validation)을 웹 컨트롤러나 서비스 계층에 맡기지 않고, **Command 객체 생성 시점에 직접 검증**합니다.

* **효과**: 유스케이스 내부로 진입하는 모든 데이터는 이미 유효성이 검증된 신뢰할 수 있는 상태임이 보장됩니다.

```kotlin
data class SendMoneyCommand(
    val sourceAccountId: AccountId,
    val targetAccountId: AccountId,
    val money: Money
) {
    init {
        require(sourceAccountId != targetAccountId) { "출금 계좌와 입금 계좌는 같을 수 없습니다." }
        require(money.isPositive()) { "송금 금액은 0보다 커야 합니다." }
    }
}
```

---

### ③ 풍부한 값 객체 (Value Objects)
`String`, `Long`, `BigDecimal` 같은 원시 타입을 직접 쓰지 않고, 도메인 의미와 연산 규칙을 내포한 **불변 값 객체(VO)**로 포장합니다.

```kotlin
@JvmInline
value class AccountId(val value: String)

data class Money(val amount: BigDecimal) {
    companion object {
        val ZERO = Money(BigDecimal.ZERO)
        fun of(value: Long) = Money(BigDecimal.valueOf(value))
    }
    
    operator fun plus(other: Money) = Money(this.amount + other.amount)
    operator fun minus(other: Money) = Money(this.amount - other.amount)
    fun isPositive(): Boolean = this.amount > BigDecimal.ZERO
    fun isGreaterThanOrEqualTo(other: Money): Boolean = this.amount >= other.amount
}
```

---

### ④ 세분화된 아웃바운드 포트 (Interface Segregation Principle)
`AccountRepository`와 같은 범용 인터페이스 대신, 각 서비스가 실제로 필요로 하는 기능만 포트로 분리합니다.

```kotlin
// 계좌 조회 전용 포트
interface LoadAccountPort {
    fun loadAccount(accountId: AccountId): Account?
}

// 상태 갱신 전용 포트 (Save 대신 의도를 드러냄)
interface UpdateAccountStatePort {
    fun updateActivities(account: Account)
}

// 동시성 제어 포트
interface AccountLockPort {
    fun lockAccount(accountId: AccountId)
    fun releaseAccount(accountId: AccountId)
}
```

---

### ⑤ 풍부한 도메인 모델 (Rich Domain Model)
비즈니스 로직(잔액 계산, 출금 가능 여부 검증 등)은 서비스가 아닌 **도메인 엔티티(`Account`) 내부에서 직접 완결**합니다. 애플리케이션 서비스는 엔티티들의 상호작용과 흐름만 조율합니다.

```kotlin
class Account(
    val id: AccountId,
    private val baselineBalance: Money,
    private val activityWindow: ActivityWindow
) {
    fun calculateBalance(): Money = baselineBalance + activityWindow.calculateBalance()

    fun withdraw(money: Money, targetAccountId: AccountId): Boolean {
        if (!mayWithdraw(money)) return false
        
        val withdrawal = Activity(
            ownerAccountId = this.id,
            sourceAccountId = this.id,
            targetAccountId = targetAccountId,
            money = money
        )
        this.activityWindow.addActivity(withdrawal)
        return true
    }

    private fun mayWithdraw(money: Money): Boolean {
        return calculateBalance().minus(money).isGreaterThanOrEqualTo(Money.ZERO)
    }
}
```

---

## 3. 패키지 구성 (Package by Feature + Architecture)

도메인 단위(예: `account`)를 최상위 패키지로 삼고, 그 하위에 아키텍처 계층을 구성합니다:

```text
jhkim105/tutorials/hexagonal/account/
├── adapter/
│   ├── in/web/                                 # [Inbound Web Adapter]
│   │   ├── SendMoneyController.kt
│   │   └── SendMoneyRequest.kt
│   └── out/persistence/                         # [Outbound Persistence Adapter]
│       ├── AccountJpaEntity.kt
│       ├── AccountJpaRepository.kt
│       ├── AccountMapper.kt
│       └── AccountPersistenceAdapter.kt         # LoadAccountPort, UpdateAccountStatePort 구현체
├── application/
│   ├── port/
│   │   ├── in/                                  # [Inbound Ports]
│   │   │   ├── SendMoneyUseCase.kt
│   │   │   ├── SendMoneyCommand.kt
│   │   │   └── GetAccountBalanceQuery.kt
│   │   └── out/                                 # [Outbound Ports]
│   │       ├── LoadAccountPort.kt
│   │       ├── UpdateAccountStatePort.kt
│   │       └── AccountLockPort.kt
│   └── service/                                 # [Application Services]
│       ├── SendMoneyService.kt
│       └── GetAccountBalanceService.kt
└── domain/                                      # [Pure Domain]
    ├── Account.kt
    ├── AccountId.kt
    ├── Money.kt
    └── ActivityWindow.kt
```

---

## 4. 매핑 전략 (Mapping Strategies)

톰 홈버그는 유스케이스의 복잡도에 따라 다음과 같은 매핑 전략을 선택할 것을 권장합니다:

1. **매핑하지 않기 전략 (No Mapping)**:
   * 모든 계층(Web, Application, Persistence)이 동일한 도메인 모델을 공유. CRUD에 적합하나 계층 간 결합 발생.
2. **양방향 매핑 전략 (Two-Way Mapping - 본 프로젝트 스타일)**:
   * 각 계층(Web DTO, Domain Model, JPA Entity)이 전용 모델을 갖고 Mapper를 통해 상호 변환.
   * 계층 간 완벽한 독립성과 도메인 순수성을 확보할 수 있는 표준 전략.
3. **완전 매핑 전략 (Full Mapping)**:
   * 각 유스케이스마다 전용 Command/Response DTO를 정의하여 매핑. 대규모 시스템에서 가장 안전한 분리 제공.

---

## 5. 참고 문헌
* Tom Hombergs, *Get Your Hands Dirty on Clean Architecture* (Packt Publishing)
* [BuckPal 예제 저장소 (GitHub)](https://github.com/thombergs/buckpal)
