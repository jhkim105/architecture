package jhkim105.tutorials.clean.usecase.boundary.`in`

import jhkim105.tutorials.clean.domain.entity.Account
import jhkim105.tutorials.clean.usecase.model.CreateAccountRequestModel

interface CreateAccountInputBoundary {
    fun create(request: CreateAccountRequestModel): Account
}
