package jhkim105.tutorials.hexagonal.adapter.out.persistence

import jhkim105.tutorials.hexagonal.application.domain.model.Account
import jhkim105.tutorials.hexagonal.application.port.out.LoadAccountPort
import jhkim105.tutorials.hexagonal.application.port.out.SaveAccountPort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

/**
 * 헥사고날 아키텍처의 영속성 아웃바운드 어댑터(Outbound Adapter)
 * LoadAccountPort와 SaveAccountPort를 구현하여 도메인/유스케이스 계층에 영속성 기능을 제공합니다.
 */
@Component
class AccountPersistenceAdapter(
    private val jpaRepository: AccountJpaRepository
) : LoadAccountPort, SaveAccountPort {

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
