package jhkim105.tutorials.onion.domain.repository

import jhkim105.tutorials.onion.domain.model.Account

/**
 * Domain Repository Interface:
 * 오니언 아키텍처에서는 데이터 영속성 인터페이스를 도메인 코어 계층에 정의하여 의존성 역전을 적용합니다.
 */
interface AccountRepository {
    fun findById(accountId: String): Account?
    fun save(account: Account): Account
}
