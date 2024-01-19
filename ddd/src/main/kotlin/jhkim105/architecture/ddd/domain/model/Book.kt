package jhkim105.architecture.ddd.domain.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator


@Entity
@Table(name = "bt_book")
class Book(
  @Id
  @UuidGenerator
  @Column(length = 50)
  var id: String? = null,

  @Column(length = 200, nullable = false)
  var title: String,

  @Column(nullable = false)
  var available: Boolean = true,

  @Column(nullable = false)
  var reserved: Boolean = false,

  ) {

  @ManyToOne
  var user: User? = null

  fun reserve(user: User) {
    if (available && !reserved) {
      reserved = true
      user.addReservedBook(this)
    } else {
      throw IllegalStateException("Book cannot be reserved.")
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Book) return false

    if (id != other.id) return false

    return true
  }

  override fun hashCode(): Int {
    return id?.hashCode() ?: 0
  }


}