package jhkim105.tutorials.clean.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface AccountJpaRepository : JpaRepository<AccountJpaEntity, String> {
}