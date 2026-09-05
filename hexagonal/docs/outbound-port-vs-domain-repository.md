# Outbound Port (`application.port.out`) vs Domain Repository (`domain.repository`) 비교

영속성 인터페이스를 **애플리케이션 계층(`application.port.out`)**에 둘 것인가, 아니면 **도메인 계층(`domain.repository`)**에 둘 것인가는 헥사고날 아키텍처와 DDD(도메인 주도 설계) / 온디언(Onion) 아키텍처 간의 대표적인 설계 차이점입니다.

---

## 1. 구조 비교 다이어그램

```
[ 헥사고날 (톰 홈버그 스타일) ]               [ DDD / 온디언 (Onion) 스타일 ]

      Application 계층                            Application 계층
┌───────────────────────────────┐           ┌───────────────────────────────┐
│ • port.in (SendMoneyUseCase)  │           │ • service (AccountService)    │
│ • port.out (LoadAccountPort)  │ ◀ Port    └───────────────┬───────────────┘
│ • service (SendMoneyService)  │                           │ calls
└───────────────┬───────────────┘                           ▼
                │ uses                                 Domain 계층
                ▼                           ┌───────────────────────────────┐
           Domain 계층                      │ • model (Account)             │
┌───────────────────────────────┐           │ • repository                  │
│ • model (Account)             │           │   (AccountRepository) ◀ Repos │
│ (외부 I/O 존재 자체를 모름)   │           └───────────────────────────────┘
└───────────────────────────────┘
```

---

## 2. 각 방식의 상세 분석

### 🅰️ `application.port.out` (Application 계층에 배치)
> **철학**: *"포트는 애플리케이션 코어가 외부 세계(어댑터)와 소통하기 위해 정의하는 계약(Contract)이다."*

* **주요 아키텍처**: 헥사고날 아키텍처(Ports & Adapters), 톰 홈버그 스타일
* **핵심 특징**:
  * 포트는 유스케이스(Application Service)의 요구사항에 맞춰 작게 쪼개어 정의됩니다 (`LoadAccountPort`, `SaveAccountPort`, `UpdateAccountStatePort`).
  * 도메인 계층은 데이터 영속화나 조회가 어떻게 일어나는지 전혀 알 필요가 없으며, 순수한 비즈니스 규칙과 상태 계산에만 집중합니다.

#### 장점
1. **도메인의 절대적인 순수성 보장**: 도메인 패키지 내부에 영속성 관련 인터페이스가 아예 없으므로, 데이터베이스나 I/O의 존재가 도메인을 오염시킬 위험이 원천 차단됩니다.
2. **인터페이스 분리 원칙(ISP) 극대화**: 거대한 `AccountRepository` 대신 각 유스케이스가 필요한 최소한의 포트만 의존하게 만듭니다 (예: 송금 서비스는 `UpdateAccountStatePort`에만 의존).
3. **외부 시스템 연동 포트와의 일관성**: DB 저장소뿐만 아니라 외부 결제 API, SMS 발송, Kafka 메시지 발행 등 모든 Outbound 상호작용이 `application.port.out` 하나의 위치에 일관되게 모입니다.

#### 단점
* **도메인 서비스(Domain Service)에서의 DB 접근 제약**: 도메인 서비스가 비즈니스 규칙 검증을 위해 DB 조회가 필요한 경우, 도메인 서비스 내부에서 직접 포트를 주입받을 수 없습니다. (애플리케이션 서비스가 먼저 조회하여 파라미터로 넘겨주어야 함)

---

### 🅱️ `domain.repository` (Domain 계층에 배치)
> **철학**: *"Repository는 도메인 애그리게이트(Aggregate)의 생명주기를 관리하는 도메인 개념의 일부이다."*

* **주요 아키텍처**: 에릭 에반스의 DDD(Domain-Driven Design), 온디언 아키텍처(Onion Architecture)
* **핵심 특징**:
  * Repository는 단순한 DB 접근자가 아니라, 도메인 객체들의 메모리 컬렉션(Collection)을 흉내 내는 도메인 빌딩 블록으로 취급됩니다.
  * `AccountRepository` 인터페이스가 도메인 계층에 위치하여 도메인 모델과 강하게 결합됩니다.

#### 장점
1. **DDD(Domain-Driven Design) 표준 패턴과의 일치**: DDD에서 정의하는 4대 빌딩 블록(Entity, Value Object, Domain Service, **Repository**) 구조를 그대로 따릅니다.
2. **도메인 서비스의 자율성**: 복잡한 도메인 정책을 수행하는 `DomainService`가 필요 시 도메인 계층 내의 `AccountRepository`를 직접 참조할 수 있습니다.
3. **도메인 중심의 컬렉션 추상화**: 비즈니스 관점에서 계좌 집합을 조회하고 다루는 개념이 도메인 계층 내에 완결됩니다.

#### 단점
* **도메인 계층의 오염 가능성**: 페이징, 정렬, 복잡한 검색 조건, 프로젝션 등 데이터베이스 기술 중심의 쿼리 메서드가 도메인 레포지토리 인터페이스에 침투하기 쉽습니다.
* **비영속성 외부 포트와의 배치 불일치**: DB용 `AccountRepository`는 `domain`에 있고, SMS 발송/외부 API 포트는 `application`에 위치하여 외부 연동 인터페이스의 위치가 분산됩니다.

---

## 3. 항목별 비교 요약

| 비교 항목 | `application.port.out` (Hexagonal) | `domain.repository` (DDD / Onion) |
| :--- | :--- | :--- |
| **인터페이스 정의 주체** | **유스케이스(Application)** 관점의 필요 | **애그리게이트(Domain)** 관점의 생명주기 |
| **인터페이스 크기** | **작고 세분화됨** (`LoadAccountPort`, `SaveAccountPort`) | **비교적 큼** (`findById`, `save`, `delete` 등 통합) |
| **도메인 순수성** | ⭐️⭐️⭐️⭐️⭐️ (완전한 무의존) | ⭐️⭐️⭐️ (영속성 쿼리 개념 침투 가능) |
| **외부 시스템 일관성** | ⭐️⭐️⭐️⭐️⭐️ (DB, API, 메시징 모두 동일 위치) | ⭐️⭐️ (DB는 도메인, 외부 API는 애플리케이션) |
| **도메인 서비스 편의성**| ⭐️⭐️ (서비스가 조회 후 파라미터로 전달 필요) | ⭐️⭐️⭐️⭐️ (도메인 서비스에서 직접 조회 가능) |

---

## 4. 실무 권장 가이드라인

### 💡 `application.port.out`을 추천하는 경우
* **헥사고날 아키텍처(Ports & Adapters)** 원칙을 엄격하게 적용하고자 할 때
* 결제 Gateway, SMS 발송, 이벤트 브로커 등 **다양한 외부 시스템 연동이 많은 MSA 환경**일 때
* 유스케이스 단위로 클래스를 잘게 분리하고 인터페이스 분리 원칙(ISP)을 철저히 지키고자 할 때

### 💡 `domain.repository`를 추천하는 경우
* **에릭 에반스의 DDD** 또는 **온디언 아키텍처** 패턴을 표준으로 채택한 팀일 때
* 도메인 서비스(Domain Service)가 풍부하고, 도메인 로직 내에서 엔티티 컬렉션 조회가 빈번할 때
