package jhkim105.tutorials.clean.usecase.boundary.`in`

import jhkim105.tutorials.clean.domain.entity.Account
import jhkim105.tutorials.clean.usecase.model.GetAccountRequestModel

interface GetAccountInputBoundary {
    fun get(request: GetAccountRequestModel): Account
}
