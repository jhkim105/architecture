package jhkim105.tutorials.onion.infra.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface AccountJpaRepository : JpaRepository<AccountJpaEntity, String> {
}