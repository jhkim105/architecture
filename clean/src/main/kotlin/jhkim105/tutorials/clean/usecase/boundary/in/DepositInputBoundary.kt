package jhkim105.tutorials.clean.usecase.boundary.`in`

import jhkim105.tutorials.clean.domain.entity.Account
import jhkim105.tutorials.clean.usecase.model.DepositRequestModel

interface DepositInputBoundary {
    fun deposit(request: DepositRequestModel): Account
}
