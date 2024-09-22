package jhkim105.tutorials.adapter.out.persistence

import jhkim105.tutorials.application.domain.model.Account

object AccountMapper {
    fun toDomain(jpaEntity: AccountJpaEntity): Account {
        return Account(
            id = jpaEntity.id,
            balance = jpaEntity.balance
        )
    }

    fun toJpaEntity(domain: Account): AccountJpaEntity {
        return AccountJpaEntity(
            id = domain.id,
            balance = domain.balance
        )
    }
}