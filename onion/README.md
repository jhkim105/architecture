# Onion Architecture Tutorial

이 모듈은 제프리 팔레르모(Jeffrey Palermo)의 **오니언 아키텍처(The Onion Architecture)** 원칙을 스프링 부트(Spring Boot 3.x)와 코틀린(Kotlin) 환경에서 구현한 예제입니다.

---

## 1. 오니언 아키텍처 핵심 개념

오니언 아키텍처의 핵심 철학은 **"모든 코드 의존성은 외부 인프라 및 UI(Outer Rings)에서 내부 도메인 코어(Inner Core)로만 향해야 한다"**는 것입니다.

```
                  ┌──────────────────────────────────────────────┐
                  │ 4. Outer Ring: Infrastructure & UI           │
                  │   - UI / Web: AccountController              │
                  │   - Infra: AccountRepositoryImpl (JPA)       │
                  │  ┌────────────────────────────────────────┐  │
                  │  │ 3. Application Services Layer          │  │
                  │  │   - AccountService                     │  │
                  │  │   - Application Commands / Queries     │  │
                  │  │  ┌──────────────────────────────────┐  │  │
                  │  │  │ 2. Domain Services Layer         │  │  │
                  │  │  │   - AccountTransferDomainService │  │  │
                  │  │  │   - Repository Interfaces        │  │  │
                  │  │  │  ┌────────────────────────────┐  │  │  │
                  │  │  │  │ 1. Domain Model (Core)     │  │  │  │
                  │  │  │  │   - Account (Entity)       │  │  │  │
                  │  │  │  └────────────────────────────┘  │  │  │
                  │  │  └──────────────────────────────────┘  │  │
                  │  └────────────────────────────────────────┘  │
                  └──────────────────────────────────────────────┘
                         ─── Dependency Direction ───▶ (Inward)
```

### 💡 오니언 아키텍처의 핵심 규칙
1. **도메인 중심(Domain-Centric)**: 애플리케이션의 핵심은 데이터베이스가 아닌 순수한 도메인 모델입니다.
2. **의존성 역전 원칙(DIP)**: 인터페이스(`AccountRepository`)는 내부 도메인 계층에 정의되고, 구현체(`AccountRepositoryImpl`)는 가장 바깥쪽 인프라 계층에 위치합니다.
3. **컴파일 타임 안전성**: 도메인 코어는 외부 프레임워크(Spring Web, JPA Entity 등)에 일절 의존하지 않습니다.

---

## 2. 모듈 아키텍처 및 흐름 다이어그램

```
       [ HTTP Request ]
              │
              ▼
   ┌────────────────────────────────────────────────────────┐
   │ [4. UI Layer] AccountController                        │
   │  - HTTP 요청을 Application Command/Query로 변환         │
   │  - Application Service 호출 후 Web Response DTO 반환   │
   └──────────────────────────┬─────────────────────────────┘
                              │ calls
                              ▼
   ┌────────────────────────────────────────────────────────┐
   │ [3. Application Services Layer] AccountService         │
   │  - 유스케이스 흐름 제어, 트랜잭션 관리(@Transactional)  │
   │  - Domain Repository 호출 및 Domain Service 조율       │
   └──────────────┬───────────────────────────┬─────────────┘
                  │ uses                      │ uses Repository Interface
                  ▼                           ▼
   ┌───────────────────────────────┐ ┌──────────────────────┐
   │ [2. Domain Services Layer]    │ │ [Domain Repository]  │
   │  AccountTransferDomainService │ │  AccountRepository   │
   │  (다중 엔티티 복합 정책 검증) │ └──────────┬───────────┘
   │                               │            │ implements
   │ [1. Domain Model (Core)]      │            ▼
   │  Account (순수 도메인 엔티티) │ ┌──────────────────────┐
   └───────────────────────────────┘ │ [4. Infra Layer]     │
                                     │  AccountRepositoryImpl
                                     │  (Spring Data JPA)   │
                                     └──────────────────────┘
```

---

## 3. 계층별 책임 및 역할

### 1) Domain Model (Inner Core) (`domain/model/`)
* **`Account`**: 계좌 잔액을 `private set`으로 캡슐화하고, 단일 계좌 단위의 입출금(`deposit`, `withdraw`) 규칙을 수행하는 순수 도메인 엔티티입니다.

### 2) Domain Services (`domain/service/`, `domain/repository/`)
* **`AccountTransferDomainService`**: 2개 이상의 엔티티가 협력하는 비즈니스 정책(동일 계좌 이체 방지, 1회 이체 최대 한도 검증)을 순수 비즈니스 로직으로 수행합니다.
* **`AccountRepository`**: 데이터 영속화를 위한 추상 인터페이스를 도메인 계층에 정의하여 외부 인프라에 대한 의존성을 역전시킵니다.

### 3) Application Services (`application/`)
* **`AccountService`**: 비즈니스 유스케이스 흐름을 관장하고 트랜잭션(`@Transactional`)을 제어하며, 도메인 레포지토리와 도메인 서비스를 조율(Orchestration)합니다.
* **Application DTOs (`application/dto/`)**: `CreateAccountCommand`, `TransferCommand`, `DepositCommand`, `WithdrawCommand`, `GetAccountQuery` 등 애플리케이션 진입 파라미터를 캡슐화합니다.

### 4) Outer Ring: UI & Infrastructure (`ui/`, `infra/`)
* **UI Layer (`ui/`)**:
  * `AccountController`: 클라이언트 요청을 받아 Jakarta Validation(`@Valid`)을 수행하고, `AccountResponse` DTO로 응답합니다.
* **Infrastructure Layer (`infra/persistence/`)**:
  * `AccountRepositoryImpl`: 도메인의 `AccountRepository`를 Spring Data JPA(`AccountJpaRepository`)를 활용해 구체적으로 구현합니다.
  * `AccountMapper`: 도메인 엔티티(`Account`)와 JPA 엔티티(`AccountJpaEntity`) 간의 변환을 담당합니다.

---

## 4. 디렉토리 및 패키지 구조

```
onion/src/main/kotlin/jhkim105/tutorials/onion/
├── domain/                                    # Inner Core
│   ├── model/
│   │   └── Account.kt                         # 1. Domain Model (엔티티)
│   ├── service/
│   │   └── AccountTransferDomainService.kt    # 2. Domain Services (비즈니스 정책)
│   └── repository/
│       └── AccountRepository.kt               # 도메인 데이터 접근 인터페이스
├── application/                               # 3. Application Services
│   ├── dto/
│   │   └── AccountCommands.kt                 # Application Commands & Queries
│   └── service/
│       └── AccountService.kt                  # 유스케이스 조율 서비스
├── ui/                                        # 4. Outer Ring: UI
│   ├── AccountController.kt
│   └── dto/
│       └── AccountWebDtos.kt                  # Web Request/Response DTOs
└── infra/                                     # 4. Outer Ring: Infrastructure
    └── persistence/
        ├── AccountJpaEntity.kt
        ├── AccountJpaRepository.kt
        ├── AccountMapper.kt
        └── AccountRepositoryImpl.kt
```

---

## 5. 테스트 전략 (Kotest BDD)

이 프로젝트는 **Kotest `BehaviorSpec`** 기반의 Given-When-Then BDD 스타일로 계층별 단위 및 통합 테스트를 수행합니다:

* **도메인 엔티티 단위 테스트**: [`AccountTest.kt`](file:///Users/jihwankim/workspace/architecture/onion/src/test/kotlin/jhkim105/tutorials/onion/domain/model/AccountTest.kt)
* **도메인 서비스 단위 테스트**: [`AccountTransferDomainServiceTest.kt`](file:///Users/jihwankim/workspace/architecture/onion/src/test/kotlin/jhkim105/tutorials/onion/domain/service/AccountTransferDomainServiceTest.kt)
* **애플리케이션 서비스 Mock 테스트**: [`AccountServiceTest.kt`](file:///Users/jihwankim/workspace/architecture/onion/src/test/kotlin/jhkim105/tutorials/onion/application/service/AccountServiceTest.kt)
* **Web Controller 슬라이스 테스트**: [`AccountControllerTest.kt`](file:///Users/jihwankim/workspace/architecture/onion/src/test/kotlin/jhkim105/tutorials/onion/ui/AccountControllerTest.kt)
* **영속성 인프라 테스트**: [`AccountRepositoryImplTest.kt`](file:///Users/jihwankim/workspace/architecture/onion/src/test/kotlin/jhkim105/tutorials/onion/infra/AccountRepositoryImplTest.kt)
* **전체 통합 테스트**: [`AccountControllerIntegrationTest.kt`](file:///Users/jihwankim/workspace/architecture/onion/src/test/kotlin/jhkim105/tutorials/onion/ui/AccountControllerIntegrationTest.kt)

---

## 6. References
- [Jeffrey Palermo - The Onion Architecture : part 1](https://jeffreypalermo.com/2008/07/the-onion-architecture-part-1/)
- [Jeffrey Palermo - The Onion Architecture : part 2](https://jeffreypalermo.com/2008/07/the-onion-architecture-part-2/)
- [Jeffrey Palermo - The Onion Architecture : part 3](https://jeffreypalermo.com/2008/08/the-onion-architecture-part-3/)