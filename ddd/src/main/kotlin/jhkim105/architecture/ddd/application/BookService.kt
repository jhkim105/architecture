package jhkim105.architecture.ddd.application


import jhkim105.architecture.ddd.application.dto.ReserveResult
import jhkim105.architecture.ddd.application.dto.ReserveParam
import jhkim105.architecture.ddd.domain.model.Book
import jhkim105.architecture.ddd.infrastructure.BookRepository
import jhkim105.architecture.ddd.infrastructure.UserRepository
import org.springframework.stereotype.Service

@Service
class BookService(
  private val bookRepository: BookRepository,
  private val userRepository: UserRepository
) {
  fun get(bookId: String): Book {
    return bookRepository.findById(bookId).orElseThrow()
  }

  fun reserve(reserveParam: ReserveParam): ReserveResult {
    val book = bookRepository.findById(reserveParam.bookId).orElseThrow()
    val user = userRepository.findById(reserveParam.userId).orElseThrow()

    book.reserve(user)
    userRepository.save(user)
    return ReserveResult(user.id!!, book.id!!, user.reservedBooks!!)
  }

}