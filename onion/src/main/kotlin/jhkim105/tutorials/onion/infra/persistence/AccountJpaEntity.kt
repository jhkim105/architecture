package jhkim105.tutorials.onion.infra.persistence

import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.Id
import java.math.BigDecimal

@Entity
@Table(name = "account")
class AccountJpaEntity(
    @Id
    val id: String,
    var balance: BigDecimal
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AccountJpaEntity) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}