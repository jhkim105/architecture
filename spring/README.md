# Spring Layered Architecture
- Controller, Service, Repository
- Domain 과 Repository 를 분리하지 않음


## 단점
- Service 단위테스트 작성이 어렵다 (Repository mocking 이 어려움)
- 도메인(업무 규칙)을 구현할 때 Persistence 를 신경써야 함


