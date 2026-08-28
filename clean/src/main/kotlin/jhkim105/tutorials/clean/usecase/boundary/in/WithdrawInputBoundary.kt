package jhkim105.tutorials.clean.usecase.boundary.`in`

import jhkim105.tutorials.clean.domain.entity.Account
import jhkim105.tutorials.clean.usecase.model.WithdrawRequestModel

interface WithdrawInputBoundary {
    fun withdraw(request: WithdrawRequestModel): Account
}
