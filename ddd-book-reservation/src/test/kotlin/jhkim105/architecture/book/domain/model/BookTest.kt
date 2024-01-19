package jhkim105.architecture.ddd.domain.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse

class BookTest {

    @Test
    fun `Should reserve a book for a user`() {
        val book = Book("1", "Clean Code")
        val user = User("user1", "John Doe")

        book.reserve(user)

        assertTrue(user.reservedBooks?.size == 1)
    }

}