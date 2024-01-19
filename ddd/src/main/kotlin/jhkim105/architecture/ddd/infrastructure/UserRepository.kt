package jhkim105.architecture.ddd.infrastructure

import jhkim105.architecture.ddd.domain.model.Book
import jhkim105.architecture.ddd.domain.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository: JpaRepository<User, String>