# Hexagonal Architecture (Ports and Adapters) Tutorial

이 모듈은 **헥사고날 아키텍처(Hexagonal Architecture / Ports and Adapters)** 패턴을 스프링 부트(Spring Boot 3.x)와 코틀린(Kotlin) 환경에서 올바르게 설계하고 구현하는 가이드를 제공합니다.

---

## 1. 아키텍처 핵심 개념 및 다이어그램

헥사고날 아키텍처의 핵심 목표는 **도메인과 비즈니스 로직(Application Core)을 외부의 세부사항(Web, DB, 외부 프레임워크 등)으로부터 완전히 격리**시키는 것입니다.

```
       [ HTTP Request ]
              │
              ▼
   ┌────────────────────────────────────────────────────────┐
   │ [Inbound Adapter] AccountController                    │
   │  (Web REST API, DTO Mapping, Validation)               │
   └──────────────────────────┬─────────────────────────────┘
                              │ calls Inbound Port
                              ▼
   ┌────────────────────────────────────────────────────────┐
   │ [Inbound Port]                                         │
   │  - CreateAccountUseCase (CreateAccountCommand)         │
   │  - DepositUseCase       (DepositCommand)               │
   │  - WithdrawUseCase      (WithdrawCommand)              │
   │  - TransferUseCase      (TransferCommand)              │
   │  - GetAccountUseCase    (GetAccountQuery)              │
   └──────────────────────────┬─────────────────────────────┘
                              │ implements
                              ▼
   ┌────────────────────────────────────────────────────────┐
   │ [Application Service] AccountService                   │
   │  - 유스케이스 흐름 제어, 트랜잭션 관리(@Transactional)  │
   │  - Outbound Port 호출 및 Domain Service 조율           │
   └──────────────┬───────────────────────────┬─────────────┘
                  │ uses                      │ uses Outbound Port
                  ▼                           ▼
   ┌───────────────────────────────┐ ┌──────────────────────┐
   │ [Domain Service]              │ │ [Outbound Port]      │
   │  AccountTransferDomainService │ │  - LoadAccountPort   │
   │  (다중 엔티티 복합 정책 검증) │ │  - SaveAccountPort   │
   │                               │ └──────────┬───────────┘
   │ [Domain Model]                │            │ implements
   │  Account (단일 엔티티 캡슐화) │            ▼
   └───────────────────────────────┘ ┌──────────────────────┐
                                     │ [Outbound Adapter]   │
                                     │  AccountRepositoryImpl
                                     │  (JPA / DB 연동)     │
                                     └──────────────────────┘
```

---

## 2. Inbound Port 네이밍과 CQRS 대칭성 검토

일부 클린 아키텍처 자료에서는 상태 변경 포트를 `*UseCase`, 조회 포트를 `*Query`로 명명하는 사례가 있습니다.

### ❓ `UseCase` vs `Query` 조합의 대칭성 모순
* **`UseCase`**: 시스템이 액터에게 제공하는 "기능/목적의 단위"를 나타내는 **아키텍처/기능적 개념**입니다.
* **`Query`**: CQS/CQRS에서 Command(명령)의 반대편에 있는 **메시지/역할 패턴 개념**입니다.
* 따라서 `UseCase`와 `Query`를 같은 레벨에서 대치시키는 것은 **개념적 범주가 일치하지 않는 비대칭(Asymmetry)**입니다.

### 🎯 일관성 있는 네이밍 전략 비교

| 전략 | Inbound Port 인터페이스 | 입력 파라미터 DTO | 설명 및 특징 |
| :--- | :--- | :--- | :--- |
| **A. UseCase 일관성 (본 프로젝트 채택)** | `*UseCase`<br>(예: `TransferUseCase`, `GetAccountUseCase`) | `*Command` / `*Query`<br>(예: `TransferCommand`, `GetAccountQuery`) | 모든 Inbound Port의 Suffix를 `*UseCase`로 통일하여 명명 일관성을 유지하고, CQS 분리는 DTO 레벨에서 명확히 표현 |
| **B. 순수 CQRS 대칭** | `*CommandHandler` / `*QueryHandler`<br>(또는 `*CommandPort` / `*QueryPort`) | `*Command` / `*Query` | CQS/CQRS의 대칭성은 완벽하나, 헥사고날의 'Port' 개념과 메시지 핸들러 패턴이 혼재될 수 있음 |

> **본 모듈의 결정**:
> 모든 인바운드 진입점은 사용자에게 비즈니스 가치를 전달하는 유스케이스이므로 포트명은 **`*UseCase`로 일관되게 통일**하고, 상태 변경과 조회의 구분은 파라미터 DTO인 **`*Command`와 `*Query`**로 명확히 분리합니다.

---

## 3. 도메인 서비스(Domain Service) vs 애플리케이션 서비스(Application Service)

DDD(도메인 주도 설계) 및 헥사고날 아키텍처에서 두 서비스는 완전히 다른 책임과 경계를 가집니다.

| 구분 | 도메인 서비스 (`domain/service`) | 애플리케이션 서비스 (`application/service`) |
| :--- | :--- | :--- |
| **주요 역할** | **순수 비즈니스 규칙 및 도메인 정책 실행** | **유스케이스 흐름 제어 및 구성요소 조율 (Orchestration)** |
| **다루는 대상** | 2개 이상의 도메인 엔티티(Aggregate) 간의 상호작용 | 영속성 포트, 도메인 엔티티, 도메인 서비스 |
| **기술/인프라 의존성** | **전혀 없음** (순수 Kotlin 객체, DB/포트 접근 금지) | Outbound Port(`LoadAccountPort`, `SaveAccountPort`) 주입 및 호출 |
| **트랜잭션/보안** | 트랜잭션 개념 없음 (순수 연산만 수행) | `@Transactional`, 보안 검증, 트랜잭션 원자성 보장 |

### 💡 실무 구현 비교 (`hexagonal` 모듈 기준)

#### 1) 도메인 서비스 (`AccountTransferDomainService`)
외부 I/O나 DB 조회가 전혀 개입하지 않으며, 순수한 두 계좌 엔티티와 이체 정책(동일 계좌 불가, 1회 한도 등)만을 다룹니다.
```kotlin
@Component
class AccountTransferDomainService {
    companion object {
        val MAX_TRANSFER_AMOUNT: BigDecimal = BigDecimal("10000000") // 1회 최대 1천만원
    }

    fun transfer(fromAccount: Account, toAccount: Account, amount: BigDecimal) {
        require(fromAccount.id != toAccount.id) { "Cannot transfer to the same account" }
        require(amount > BigDecimal.ZERO) { "Transfer amount must be positive" }
        require(amount <= MAX_TRANSFER_AMOUNT) { "Transfer amount exceeds maximum limit" }

        fromAccount.withdraw(amount)
        toAccount.deposit(amount)
    }
}
```

#### 2) 애플리케이션 서비스 (`AccountService`)
DB에서 엔티티를 로드하고, 도메인 서비스를 호출하여 비즈니스 로직을 수행한 뒤, 결과를 영속화하고 트랜잭션을 마칩니다.
```kotlin
@Service
@Transactional
class AccountService(
    private val loadAccountPort: LoadAccountPort,
    private val saveAccountPort: SaveAccountPort,
    private val accountTransferDomainService: AccountTransferDomainService
) : TransferUseCase {

    override fun transfer(command: TransferCommand): Account {
        // 1. Outbound Port를 통한 엔티티 로드
        val fromAccount = loadAccountPort.loadAccount(command.fromAccountId)
            ?: throw NoSuchElementException("Account not found")
        val toAccount = loadAccountPort.loadAccount(command.toAccountId)
            ?: throw NoSuchElementException("Account not found")

        // 2. 도메인 서비스에 비즈니스 규칙 위임
        accountTransferDomainService.transfer(fromAccount, toAccount, command.amount)

        // 3. Outbound Port를 통한 상태 영속화
        saveAccountPort.saveAccount(fromAccount)
        return saveAccountPort.saveAccount(toAccount)
    }
}
```

---

## 4. 디렉토리 및 패키지 구조

```
src/main/kotlin/jhkim105/tutorials/hexagonal/
├── adapter/
│   ├── in/rest/                      # Inbound Web Adapter
│   │   ├── AccountController.kt
│   │   └── dto/AccountDtos.kt        # Web Request/Response DTOs
│   └── out/persistence/              # Outbound Persistence Adapter
│       ├── AccountJpaEntity.kt
│       ├── AccountJpaRepository.kt
│       ├── AccountMapper.kt
│       └── AccountRepositoryImpl.kt
└── application/
    ├── domain/                       # Core Domain
    │   ├── model/
    │   │   └── Account.kt            # 순수 도메인 엔티티 (잔액 캡슐화)
    │   └── service/
    │       └── AccountTransferDomainService.kt # 도메인 서비스
    ├── port/
    │   ├── in/                       # Inbound Ports (Use Cases & Commands/Queries)
    │   │   ├── CreateAccountUseCase.kt
    │   │   ├── DepositUseCase.kt
    │   │   ├── WithdrawUseCase.kt
    │   │   ├── TransferUseCase.kt
    │   │   └── GetAccountUseCase.kt
    │   └── out/                      # Outbound Ports (Driven Ports)
    │       ├── LoadAccountPort.kt
    │       ├── SaveAccountPort.kt
    │       └── AccountRepository.kt
    └── service/
        └── AccountService.kt         # Application Use Case Service
```

---

## 5. 테스트 전략 (Kotest BDD)

이 프로젝트는 **Kotest `BehaviorSpec`** 스타일의 BDD(Behavior-Driven Development) 테스트를 사용하여 비즈니스 시나리오를 명확하게 검증합니다:

* **도메인 단위 테스트**: [`AccountTest.kt`](file:///Users/jihwankim/workspace/architecture/hexagonal/src/test/kotlin/jhkim105/tutorials/hexagonal/application/domain/model/AccountTest.kt)
* **도메인 서비스 단위 테스트**: [`AccountTransferDomainServiceTest.kt`](file:///Users/jihwankim/workspace/architecture/hexagonal/src/test/kotlin/jhkim105/tutorials/hexagonal/application/domain/service/AccountTransferDomainServiceTest.kt)
* **애플리케이션 서비스 Mock 테스트**: [`AccountServiceTest.kt`](file:///Users/jihwankim/workspace/architecture/hexagonal/src/test/kotlin/jhkim105/tutorials/hexagonal/application/service/AccountServiceTest.kt)
* **Web Controller 슬라이스 테스트**: [`AccountControllerTest.kt`](file:///Users/jihwankim/workspace/architecture/hexagonal/src/test/kotlin/jhkim105/tutorials/hexagonal/adapter/in/rest/AccountControllerTest.kt)
* **통합 테스트**: [`AccountControllerIntegrationTest.kt`](file:///Users/jihwankim/workspace/architecture/hexagonal/src/test/kotlin/jhkim105/tutorials/hexagonal/adapter/in/rest/AccountControllerIntegrationTest.kt)

---

## 6. References
- [Alistair Cockburn - Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Hexagonal Architecture Budapest Presentation (PDF)](https://alistaircockburn.com/Hexagonal%20Budapest%2023-05-18.pdf)
- [Reflectoring - Hexagonal Architecture with Spring Boot](https://reflectoring.io/spring-hexagonal/)
