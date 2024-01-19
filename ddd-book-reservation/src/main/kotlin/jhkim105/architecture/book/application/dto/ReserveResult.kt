package jhkim105.architecture.ddd.application.dto

import jhkim105.architecture.ddd.domain.model.Book

data class ReserveResult(
  val userId: String,
  val bookId: String,
  val reservedBooks: MutableList<Book>
)
