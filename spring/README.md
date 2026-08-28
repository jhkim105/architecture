# Spring Layered Architecture

* **기본 구성**: Controller, Service, Repository
* **핵심 특징**: **Domain과 Repository(Persistence)를 분리하지 않음** (단일 JPA `@Entity`를 도메인 객체로 사용)

---

## 1. 아키텍처 다이어그램 및 구조

전통적인 스프링 3계층(Layered Architecture)은 상위 계층이 하위 계층에 직접 단방향으로 의존하는 데이터베이스 중심의 아키텍처입니다:

```
   ┌────────────────────────────────────────────────────────┐
   │ [1. Presentation Layer] AccountController              │
   │  - HTTP Request 수신 및 Web DTO 매핑                    │
   └──────────────────────────┬─────────────────────────────┘
                              │ calls directly
                              ▼
   ┌────────────────────────────────────────────────────────┐
   │ [2. Business / Service Layer] AccountServiceImpl       │
   │  - 비즈니스 로직 및 트랜잭션(@Transactional) 관리      │
   │  - AccountRepository(Spring Data JPA) 직접 호출         │
   └──────────────┬───────────────────────────┬─────────────┘
                  │ uses                      │ calls
                  ▼                           ▼
   ┌───────────────────────────────┐ ┌──────────────────────┐
   │ [Domain Model 겸 JPA Entity]  │ │ [3. Persistence Layer│
   │  Account (@Entity)            │ │  AccountRepository   │
   │  - 비즈니스 메서드와 DB 테이블│ │  (JpaRepository)     │
   │    매핑이 결합된 구조         │ └──────────────────────┘
   └───────────────────────────────┘
```

---

## 2. 단점 및 한계 분석

### ⚠️ 단점 1: Service 단위테스트 작성이 어렵다 (Repository mocking이 어려움)
1. **Spring Data JPA 의존성**:
   * 서비스가 순수 도메인 인터페이스가 아니라 Spring Data JPA 인터페이스(`JpaRepository<Account, String>`)에 직접 의존합니다.
   * 단위 테스트를 작성할 때 `findByIdOrNull`, `save`, `flush` 등 JPA 리포지토리의 복잡한 동작을 일일이 Mocking해야 합니다.
2. **영속성 컨텍스트(Dirty Checking) 모킹의 한계**:
   * 트랜잭션 내에서 엔티티의 상태만 변경하고 명시적 `save()`를 호출하지 않는 경우(Dirty Checking), 실제 환경에서는 DB에 반영되지만 Mocking 기반 단위 테스트에서는 변경 여부를 검증하기가 매우 까다롭습니다.

### ⚠️ 단점 2: 도메인(업무 규칙)을 구현할 때 Persistence를 신경써야 함
1. **프레임워크 침투**:
   * 순수 비즈니스 엔티티여야 할 `Account`에 `@Entity`, `@Id`, `@Table`, `@Column` 등 JPA 어노테이션이 강하게 결합됩니다.
2. **기술적 제약 사항의 도메인 오염**:
   * JPA 스펙상 필요한 기본 생성자(`no-arg`), 프록시 생성을 위한 open 키워드, 지연 로딩(`FetchType.LAZY`), `LazyInitializationException`, `N+1 문제` 등 데이터베이스 기술의 제약 조건이 도메인 비즈니스 로직 작성 시 항상 고려되어야 합니다.

---

## 3. 장점 및 도입 고려 시점

| 관점 | Layered Architecture (Spring) | Hexagonal / Clean / Onion |
| :--- | :--- | :--- |
| **초기 개발 속도** | 🚀 **매우 빠름** (보일러플레이트 최소화) | 보통 (Port/Gateway/Mapper 등 추가 코드 필요) |
| **학습 곡선** | 🟢 **매우 낮음** (스프링 기본 3계층 표준) | 🟡 보통~높음 (DDD 및 아키텍처 원칙 이해 필요) |
| **도메인 복잡도** | 단순 CRUD 또는 비즈니스 규칙이 적은 도메인 | 복잡한 도메인 규칙 및 장기적 유지보수가 중요한 도메인 |
| **DB / 프레임워크 격리**| ❌ 격리되지 않음 (Spring/JPA에 종속) |  완전 격리 (순수 POJO/POKO 유지) |

---

## 4. 디렉토리 및 패키지 구조

```
spring/src/main/kotlin/jhkim105/tutorials/spring/
├── controller/                                # Presentation Layer
│   ├── AccountController.kt                   # REST Controller
│   └── dto/
│       └── AccountDtos.kt                     # Web Request/Response DTOs
├── model/                                     # Domain 겸 JPA Entity
│   └── Account.kt                             # @Entity 어노테이션이 결합된 도메인 모델
├── repository/                                # Persistence Layer
│   └── AccountRepository.kt                   # Spring Data JPA JpaRepository
└── service/                                   # Business Service Layer
    ├── AccountService.kt                      # Service Interface
    └── impl/
        └── AccountServiceImpl.kt              # Service Implementation (@Transactional)
```

---

## 5. 테스트 전략

* **Service 단위 테스트**: [`AccountServiceTest.kt`](file:///Users/jihwankim/workspace/architecture/spring/src/test/kotlin/jhkim105/tutorials/spring/service/AccountServiceTest.kt) (Kotest `BehaviorSpec` + MockK 기반 Repository 모킹)
* **Web Controller 슬라이스 테스트**: [`AccountControllerTest.kt`](file:///Users/jihwankim/workspace/architecture/spring/src/test/kotlin/jhkim105/tutorials/spring/controller/AccountControllerTest.kt) (@WebMvcTest)
* **JPA Repository 슬라이스 테스트**: [`AccountRepositoryTest.kt`](file:///Users/jihwankim/workspace/architecture/spring/src/test/kotlin/jhkim105/tutorials/spring/repository/AccountRepositoryTest.kt) (@DataJpaTest)
* **전체 통합 테스트**: [`AccountControllerIntegrationTest.kt`](file:///Users/jihwankim/workspace/architecture/spring/src/test/kotlin/jhkim105/tutorials/spring/controller/AccountControllerIntegrationTest.kt) (@SpringBootTest)
