package jhkim105.tutorials.clean.adapter.out.persistence

import jhkim105.tutorials.clean.domain.entity.Account
import jhkim105.tutorials.clean.usecase.gateway.AccountGateway
import jhkim105.tutorials.clean.usecase.gateway.LoadAccountGateway
import jhkim105.tutorials.clean.usecase.gateway.SaveAccountGateway
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

/**
 * Interface Adapter (Persistence Gateway Implementation):
 * Use Case 계층에서 정의한 Gateway 인터페이스(LoadAccountGateway, SaveAccountGateway)를
 * Spring Data JPA와 연동하여 구체적으로 구현합니다.
 */
@Repository
class AccountGatewayImpl(
    private val jpaRepository: AccountJpaRepository
) : AccountGateway, LoadAccountGateway, SaveAccountGateway {

    override fun loadAccount(accountId: String): Account? {
        val entity = jpaRepository.findByIdOrNull(accountId)
        return entity?.let { AccountMapper.toDomain(it) }
    }

    override fun saveAccount(account: Account): Account {
        val entity = AccountMapper.toJpaEntity(account)
        val savedEntity = jpaRepository.save(entity)
        return AccountMapper.toDomain(savedEntity)
    }
}
