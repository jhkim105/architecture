# Software Architecture Patterns with Spring Boot & Kotlin

이 저장소는 동일한 은행 계좌(Account) 도메인 시나리오를 바탕으로 **4가지 대표적인 소프트웨어 아키텍처 패턴**을 스프링 부트(Spring Boot 3.4.x)와 코틀린(Kotlin)으로 구현하고 비교·분석하는 멀티 모듈 프로젝트입니다.

---

## 🏛️ 아키텍처 모듈 구성

| 모듈 | 아키텍처 패턴 | 창안자 / 기원 | 핵심 개념 |
| :--- | :--- | :--- | :--- |
| [`spring`](spring/README.md) | **Spring Layered Architecture** | 전형적인 스프링 3계층 | Controller ➡️ Service ➡️ Repository (도메인과 영속성 결합) |
| [`hexagonal`](hexagonal/README.md) | **Hexagonal Architecture** | Alistair Cockburn (2005) | Ports and Adapters (Inbound/Outbound Port 분리) |
| [`clean`](clean/README.md) | **Clean Architecture** | Robert C. Martin (Uncle Bob, 2012) | Entities, Use Cases (Interactors, Input Boundaries, Gateways) |
| [`onion`](onion/README.md) | **Onion Architecture** | Jeffrey Palermo (2008) | Domain Model ➡️ Domain Services ➡️ Application Services ➡️ Outer Rings |

---

## 📊 4대 아키텍처 한눈에 비교하기

```
[ Traditional Spring Layered ]        [ Hexagonal Architecture ]
   Controller                            Inbound Adapters (Web/CLI)
       │                                            │
       ▼                                            ▼
    Service                              Inbound Ports (*UseCase)
       │                                            │
       ▼                                            ▼
   Repository ──▶ Account (@Entity)      Application Core (Domain/Service)
                                                    │
                                                    ▼
                                         Outbound Ports (Load/Save)
                                                    │
                                                    ▼
                                         Outbound Adapters (JPA/DB)

[ Clean Architecture ]                [ Onion Architecture ]
   Controllers (Interface Adapters)      Outer Ring (UI & Infrastructure)
       │                                            │
       ▼                                            ▼
   Input Boundaries (Use Cases)          Application Services Layer
       │                                            │
       ▼                                            ▼
   Interactors (Use Case Implementations)Domain Services & Repositories
       │                                            │
       ▼                                            ▼
   Entities & Domain Services            Domain Model (Inner Core)
```

### 🔍 계층 및 네이밍 대응 표

| 구분 | Spring Layered (`spring`) | Hexagonal (`hexagonal`) | Clean Architecture (`clean`) | Onion Architecture (`onion`) |
| :--- | :--- | :--- | :--- | :--- |
| **Domain Model** | `model.Account` (`@Entity`) | `domain.model.Account` (순수 객체) | `domain.entity.Account` (순수 객체) | `domain.model.Account` (순수 객체) |
| **Domain Service** | *(별도 분리 없음)* | `domain.service.AccountTransferDomainService` | `domain.service.AccountTransferDomainService` | `domain.service.AccountTransferDomainService` |
| **Inbound 진입점** | `service.AccountService` | `port.in.*UseCase` (`CreateAccountUseCase` 등) | `usecase.boundary.in.*InputBoundary` | `application.service.AccountService` |
| **유스케이스 구현체** | `service.impl.AccountServiceImpl` | `application.service.AccountService` | `usecase.interactor.AccountInteractor` | `application.service.AccountService` |
| **Outbound 인터페이스**| `repository.AccountRepository` | `port.out.LoadAccountPort`, `SaveAccountPort` | `usecase.gateway.LoadAccountGateway`, `SaveAccountGateway` | `domain.repository.AccountRepository` |
| **데이터 전달 모델** | Web DTO | `*Command`, `*Query`, Web DTO | `*RequestModel`, `*ResponseModel`, Web DTO | `*Command`, `*Query`, Web DTO |
| **영속성 어댑터** | *(JPA Repository 직접 사용)* | `adapter.out.persistence.AccountPersistenceAdapter` | `adapter.out.persistence.AccountGatewayImpl` | `infra.persistence.AccountRepositoryImpl` |

---

## ⚖️ 트레이드오프 및 아키텍처 선택 가이드

| 관점 | Spring Layered | Hexagonal / Clean / Onion |
| :--- | :--- | :--- |
| **초기 생산성** | 🚀 **매우 빠름** (보일러플레이트 최소화) | ⚖️ 보통 (Port/Gateway/Mapper 계층 분리 코드 필요) |
| **도메인 순수성** | ❌ JPA `@Entity`와 도메인이 결합됨 |  순수 Kotlin 엔티티로 외부 프레임워크와 완전 격리 |
| **단위 테스트 용이성** | ⚠️ Repository Mocking 및 JPA 의존성 필요 |  외부 Mock 없이 순수 Kotlin 도메인 단위 테스트 가능 |
| **비즈니스 복잡도** | 단순 CRUD 또는 빠른 프로토타이핑 | 복잡한 도메인 룰, 장기 유지보수 및 MSA 전환 고려 시 |
| **인프라 교체 유연성** | ❌ DB/프레임워크 변경 시 도메인 전체 영향 |  영속성 기술(JPA ➡️ NoSQL 등) 교체 시 도메인 영향 없음 |

---

## 🛠️ 기술 스택 및 공통 인프라

* **Language**: Kotlin 1.9.25 (Java 21 Toolchain)
* **Framework**: Spring Boot 3.4.3
* **Build Tool**: Gradle 8.10.1 (Gradle Version Catalog: [`gradle/libs.versions.toml`](gradle/libs.versions.toml))
* **Database**: H2 (In-Memory for Test) / MariaDB
* **Logging**: `kotlin-logging` (`io.github.oshai:kotlin-logging-jvm`)
* **Testing**:
  * **Kotest 5.9.1** (`BehaviorSpec` BDD Style)
  * **MockK 1.13.16**
  * Spring Boot Test (`@WebMvcTest`, `@DataJpaTest`, `@SpringBootTest`)

---

## 🚀 빌드 및 테스트 실행

### 전체 모듈 빌드 및 테스트
```bash
./gradlew clean test
```

### 특정 모듈만 실행/테스트
```bash
# Hexagonal 모듈 테스트
./gradlew :hexagonal:test

# Clean 모듈 테스트
./gradlew :clean:test

# Spring 모듈 실행
./gradlew :spring:bootRun
```

---

## 📚 각 모듈별 상세 가이드

* 📖 [**Spring Layered Architecture Guide**](spring/README.md)
* 📖 [**Hexagonal Architecture Guide**](hexagonal/README.md)
* 📖 [**Clean Architecture Guide**](clean/README.md)
* 📖 [**Onion Architecture Guide**](onion/README.md)
