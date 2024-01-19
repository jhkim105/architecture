package jhkim105.architecture.ddd.domain.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator


@Entity
@Table(name = "bt_user")
class User(
  @Id
  @UuidGenerator
  @Column(length = 50)
  var id: String? = null,

  @Column(length = 100, nullable = false)
  var username: String,

  @OneToMany(mappedBy = "user")
  var reservedBooks: MutableList<Book>? = null,
) {

  fun addReservedBook(book: Book) {
    reservedBooks?.add(book) ?: run {
      reservedBooks = mutableListOf(book)
    }
  }


  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is User) return false

    if (id != other.id) return false

    return true
  }

  override fun hashCode(): Int {
    return id?.hashCode() ?: 0
  }



}