package jhkim105.tutorials.onion.infra.persistence

import jhkim105.tutorials.onion.domain.model.Account
import jhkim105.tutorials.onion.domain.repository.AccountRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

/**
 * 4. Outer Ring: Infrastructure Layer (Persistence):
 * Domain Core에 정의된 AccountRepository 인터페이스를 Spring Data JPA를 활용해 구체적으로 구현합니다.
 */
@Repository
class AccountRepositoryImpl(
    private val jpaRepository: AccountJpaRepository
) : AccountRepository {

    override fun findById(accountId: String): Account? {
        val entity = jpaRepository.findByIdOrNull(accountId)
        return entity?.let { AccountMapper.toDomain(it) }
    }

    override fun save(account: Account): Account {
        val entity = AccountMapper.toJpaEntity(account)
        val savedEntity = jpaRepository.save(entity)
        return AccountMapper.toDomain(savedEntity)
    }
}
