package jhkim105.architecture.ddd.infrastructure

import jhkim105.architecture.ddd.domain.model.Book
import org.springframework.data.jpa.repository.JpaRepository

interface BookRepository: JpaRepository<Book, String>