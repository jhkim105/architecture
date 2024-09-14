package jhkim105.architecture.ddd.presentation


import jhkim105.architecture.ddd.application.BookService
import jhkim105.architecture.ddd.application.dto.ReserveParam
import jhkim105.architecture.ddd.domain.model.Book
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController


@RestController
class BookController(
  private val bookService: BookService
) {

  @GetMapping("/{bookId}")
  fun getBook(@PathVariable bookId: String): Book? {
    return bookService.get(bookId)
  }

  @PostMapping("/reserve")
  fun reserveBook(@RequestBody reserveRequest: ReserveRequest): ReservedResponse {
    val bookId = reserveRequest.bookId
    val userId = reserveRequest.userId

    val result = bookService.reserve(ReserveParam(userId, bookId))
    return ReservedResponse(bookId, userId, result.reservedBooks)
  }
}

data class ReserveRequest(val bookId: String, val userId: String)
data class ReservedResponse(val bookId: String, val userId: String, val reservedBooks: MutableList<Book>)