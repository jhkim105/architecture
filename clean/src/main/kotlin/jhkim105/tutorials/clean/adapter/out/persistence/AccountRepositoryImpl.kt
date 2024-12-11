package jhkim105.tutorials.clean.adapter.out.persistence

import jhkim105.tutorials.clean.application.entity.Account
import jhkim105.tutorials.clean.application.gateway.AccountRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class AccountRepositoryImpl(private val jpaRepository: AccountJpaRepository) : AccountRepository {
    override fun findById(accountId: String): Account? {
        return jpaRepository.findByIdOrNull(accountId)?.let { AccountMapper.toDomain(it)}
    }

    override fun save(account: Account): Account {
        val accountJpaEntity = AccountMapper.toJpaEntity(account)
        val savedJpaEntity = jpaRepository.save(accountJpaEntity)
        return AccountMapper.toDomain(savedJpaEntity)
    }

}