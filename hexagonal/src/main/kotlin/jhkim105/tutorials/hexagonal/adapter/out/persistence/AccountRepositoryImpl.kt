package jhkim105.tutorials.hexagonal.adapter.out.persistence

import jhkim105.tutorials.hexagonal.application.domain.model.Account
import jhkim105.tutorials.hexagonal.application.port.out.AccountRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class AccountRepositoryImpl(private val jpaRepository: AccountJpaRepository) : AccountRepository {
    override fun findById(accountId: String): Account? {
        val accountJpaEntity = jpaRepository.findByIdOrNull(accountId)
        return accountJpaEntity?.let {AccountMapper.toDomain(it)}
    }

    override fun save(account: Account): Account {
        val accountJpaEntity = AccountMapper.toJpaEntity(account)
        val savedJpaEntity = jpaRepository.save(accountJpaEntity)
        return AccountMapper.toDomain(savedJpaEntity)
    }

}