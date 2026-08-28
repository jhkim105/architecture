# Clean Architecture Tutorial

이 모듈은 로버트 C. 마틴(Robert C. Martin / Uncle Bob)의 **클린 아키텍처(The Clean Architecture)** 원칙을 스프링 부트(Spring Boot 3.x)와 코틀린(Kotlin) 환경에서 정석대로 구현한 예제입니다.

---

## 1. 클린 아키텍처 핵심 개념

클린 아키텍처의 가장 중요한 규칙은 **의존성 규칙(The Dependency Rule)**입니다:
> **"소스 코드의 의존성은 반드시 바깥쪽 원에서 안쪽 원(고수준 정책/도메인)으로만 향해야 한다."**

내부 계층(도메인 엔티티, 유스케이스)은 외부 계층(웹 컨트롤러, DB, UI, 프레임워크)의 존재를 전혀 알지 못해야 하며, 프레임워크나 외부 데이터베이스는 교체 가능한 세부사항(Detail)으로 격리됩니다.

```
                  ┌──────────────────────────────────────────────┐
                  │ 4. Frameworks & Drivers (Spring Boot, JPA)   │
                  │  ┌────────────────────────────────────────┐  │
                  │  │ 3. Interface Adapters (Web, Gateway)   │  │
                  │  │  ┌──────────────────────────────────┐  │  │
                  │  │  │ 2. Application Use Cases         │  │  │
                  │  │  │  ┌────────────────────────────┐  │  │  │
                  │  │  │  │ 1. Enterprise Entities     │  │  │  │
                  │  │  │  │    (Account, DomainService)│  │  │  │
                  │  │  │  └────────────────────────────┘  │  │  │
                  │  │  │   (Interactors, Boundaries)   │  │  │
                  │  │  └──────────────────────────────────┘  │  │
                  │  │   (Controllers, Presenters, Gateways)  │  │
                  │  └────────────────────────────────────────┘  │
                  └──────────────────────────────────────────────┘
                         ─── Dependency Direction ───▶ (Inward)
```

---

## 2. 모듈 아키텍처 및 구성 요소

```
       [ HTTP Request ]
              │
              ▼
   ┌────────────────────────────────────────────────────────┐
   │ [Interface Adapter: Controller] AccountController      │
   │  - HTTP 파라미터를 Use Case RequestModel로 변환         │
   │  - Input Boundary 호출 후 Web Response DTO 반환        │
   └──────────────────────────┬─────────────────────────────┘
                              │ calls Input Boundary
                              ▼
   ┌────────────────────────────────────────────────────────┐
   │ [Application Use Case: Input Boundary]                 │
   │  - CreateAccountInputBoundary (CreateAccountRequestModel)│
   │  - DepositInputBoundary       (DepositRequestModel)    │
   │  - WithdrawInputBoundary      (WithdrawRequestModel)   │
   │  - TransferInputBoundary      (TransferRequestModel)   │
   │  - GetAccountInputBoundary    (GetAccountRequestModel) │
   └──────────────────────────┬─────────────────────────────┘
                              │ implements
                              ▼
   ┌────────────────────────────────────────────────────────┐
   │ [Application Use Case: Interactor] AccountInteractor   │
   │  - 비즈니스 유스케이스 흐름 제어, 트랜잭션 경계 관리   │
   │  - Gateway 및 Domain Service 조율 (Orchestration)      │
   └──────────────┬───────────────────────────┬─────────────┘
                  │ uses                      │ uses Data Gateway
                  ▼                           ▼
   ┌───────────────────────────────┐ ┌──────────────────────┐
   │ [Domain: Domain Service]      │ │ [Use Case: Gateway]  │
   │  AccountTransferDomainService │ │  - LoadAccountGateway│
   │  (복합 엔티티 비즈니스 정책)  │ │  - SaveAccountGateway│
   │                               │ └──────────┬───────────┘
   │ [Domain: Entity]              │            │ implements
   │  Account (핵심 도메인 규칙)   │            ▼
   └───────────────────────────────┘ ┌──────────────────────┐
                                     │ [Adapter: Persistence]
                                     │  AccountGatewayImpl  │
                                     │  (Spring Data JPA)   │
                                     └──────────────────────┘
```

---

## 3. 계층별 책임 및 역할

### 1) Enterprise Business Rules: Entities (`domain/`)
* **`Account`**: 계좌의 잔액을 캡슐화하고 단일 계좌 단위의 입금(`deposit`), 출금(`withdraw`) 검증 규칙을 수행하는 순수 Kotlin 도메인 엔티티입니다.
* **`AccountTransferDomainService`**: 여러 계좌(Aggregate) 간의 상호작용 및 비즈니스 정책(동일 계좌 이체 불가, 1회 이체 한도 검증)을 순수 비즈니스 로직으로 수행합니다.

### 2) Application Business Rules: Use Cases (`usecase/`)
* **Input Boundaries (`boundary/in/`)**: 외부에서 애플리케이션 유스케이스를 호출하기 위한 진입점 인터페이스입니다 (`CreateAccountInputBoundary`, `TransferInputBoundary` 등).
* **Gateways (`gateway/`)**: 유스케이스가 데이터 영속화를 위해 요구하는 추상 인터페이스입니다 (`LoadAccountGateway`, `SaveAccountGateway`, `AccountGateway`). 의존성 역전 원칙(DIP)을 적용하여 세부 DB 구현에 독립적입니다.
* **Request & Response Models (`model/`)**: 경계 간 데이터를 전달하기 위한 순수 DTO입니다 (`CreateAccountRequestModel`, `TransferRequestModel`, `AccountResponseModel`).
* **Use Case Interactor (`interactor/AccountInteractor`)**: Input Boundary를 구현하며, Gateway를 통해 데이터를 로드하고 도메인 서비스를 호출하여 비즈니스 결과를 영속화하는 유스케이스의 핵심 조정자(Orchestrator)입니다.

### 3) Interface Adapters (`adapter/`)
* **Web Adapter (`adapter/in/web/`)**:
  * `AccountController`: 클라이언트의 HTTP 요청을 받아 `@Valid` 검증을 거친 후 `RequestModel`로 변환하여 Input Boundary를 실행하고, 그 결과를 `AccountResponse` DTO로 매핑하여 반환합니다.
* **Persistence Adapter (`adapter/out/persistence/`)**:
  * `AccountGatewayImpl`: UseCase 계층의 `LoadAccountGateway`, `SaveAccountGateway`를 구현하며, Spring Data JPA Repository 및 JPA Entity를 사용하여 실제 데이터베이스와 통신합니다.
  * `AccountMapper`: 순수 도메인 객체(`Account`)와 JPA 엔티티(`AccountJpaEntity`) 간의 상호 변환을 전담합니다.

---

## 4. 디렉토리 및 패키지 구조

```
src/main/kotlin/jhkim105/tutorials/clean/
├── domain/                                    # 1. Enterprise Business Rules
│   ├── entity/
│   │   └── Account.kt                         # 순수 도메인 엔티티
│   └── service/
│       └── AccountTransferDomainService.kt    # 도메인 서비스 (복합 비즈니스 정책)
├── usecase/                                   # 2. Application Business Rules
│   ├── boundary/in/                           # Input Boundaries
│   │   ├── CreateAccountInputBoundary.kt
│   │   ├── DepositInputBoundary.kt
│   │   ├── WithdrawInputBoundary.kt
│   │   ├── TransferInputBoundary.kt
│   │   └── GetAccountInputBoundary.kt
│   ├── gateway/                               # Data Gateways (Outbound)
│   │   ├── LoadAccountGateway.kt
│   │   ├── SaveAccountGateway.kt
│   │   └── AccountGateway.kt
│   ├── model/                                 # Request / Response Models
│   │   ├── AccountRequestModels.kt
│   │   └── AccountResponseModel.kt
│   └── interactor/                            # Use Case Interactors
│       └── AccountInteractor.kt
└── adapter/                                   # 3. Interface Adapters
    ├── in/web/                                # Web REST Adapter
    │   ├── AccountController.kt
    │   └── dto/AccountWebDtos.kt              # HTTP Request / Response DTOs
    └── out/persistence/                       # Persistence Gateway Adapter
        ├── AccountJpaEntity.kt
        ├── AccountJpaRepository.kt
        ├── AccountMapper.kt
        └── AccountGatewayImpl.kt
```

---

## 5. 테스트 전략 (Kotest BDD)

이 프로젝트는 **Kotest `BehaviorSpec`** 스타일의 BDD(Behavior-Driven Development) 테스트를 사용하여 계층별 관심사를 격리하여 검증합니다:

* **도메인 엔티티 단위 테스트**: [`AccountTest.kt`](file:///Users/jihwankim/workspace/architecture/clean/src/test/kotlin/jhkim105/tutorials/clean/domain/entity/AccountTest.kt)
* **도메인 서비스 단위 테스트**: [`AccountTransferDomainServiceTest.kt`](file:///Users/jihwankim/workspace/architecture/clean/src/test/kotlin/jhkim105/tutorials/clean/domain/service/AccountTransferDomainServiceTest.kt)
* **유스케이스 인터랙터 BDD Mock 테스트**: [`AccountInteractorTest.kt`](file:///Users/jihwankim/workspace/architecture/clean/src/test/kotlin/jhkim105/tutorials/clean/usecase/interactor/AccountInteractorTest.kt)
* **웹 컨트롤러 슬라이스 테스트**: [`AccountControllerTest.kt`](file:///Users/jihwankim/workspace/architecture/clean/src/test/kotlin/jhkim105/tutorials/clean/adapter/in/web/AccountControllerTest.kt)
* **영속성 게이트웨이 JPA 테스트**: [`AccountGatewayImplTest.kt`](file:///Users/jihwankim/workspace/architecture/clean/src/test/kotlin/jhkim105/tutorials/clean/adapter/out/persistence/AccountGatewayImplTest.kt)
* **전체 통합 테스트**: [`AccountControllerIntegrationTest.kt`](file:///Users/jihwankim/workspace/architecture/clean/src/test/kotlin/jhkim105/tutorials/clean/adapter/in/web/AccountControllerIntegrationTest.kt)

---

## 6. References
- [The Clean Code Blog - The Clean Architecture (Uncle Bob)](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Baeldung - Clean Architecture with Spring Boot](https://www.baeldung.com/spring-boot-clean-architecture)