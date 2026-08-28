package jhkim105.tutorials.hexagonal.adapter.out.persistence

import jhkim105.tutorials.hexagonal.application.domain.model.Account
import jhkim105.tutorials.hexagonal.application.port.out.AccountRepository
import jhkim105.tutorials.hexagonal.application.port.out.LoadAccountPort
import jhkim105.tutorials.hexagonal.application.port.out.SaveAccountPort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class AccountRepositoryImpl(
    private val jpaRepository: AccountJpaRepository
) : AccountRepository, LoadAccountPort, SaveAccountPort {

    override fun loadAccount(accountId: String): Account? {
        val accountJpaEntity = jpaRepository.findByIdOrNull(accountId)
        return accountJpaEntity?.let { AccountMapper.toDomain(it) }
    }

    override fun saveAccount(account: Account): Account {
        val accountJpaEntity = AccountMapper.toJpaEntity(account)
        val savedJpaEntity = jpaRepository.save(accountJpaEntity)
        return AccountMapper.toDomain(savedJpaEntity)
    }
}