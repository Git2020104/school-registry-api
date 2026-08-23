package com.maranatha.skools.security

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authorizeRoles(vararg allowedRoles: String, build: Route.() -> Unit): Route {
    val authorizedRoute = createChild(object : RouteSelector() {
        override fun evaluate(context: RoutingResolveContext, index: Int) = RouteSelectorEvaluation.Constant
    })

    authorizedRoute.intercept(ApplicationCallPipeline.Call) {
        val principal = call.principal<JWTPrincipal>()
        val userRole = principal?.payload?.getClaim("role")?.asString()

        if (userRole == null || userRole !in allowedRoles) {
            call.respond(
                HttpStatusCode.Forbidden,
                mapOf("error" to "Access denied: Required role (${allowedRoles.joinToString(", ")}) missing")
            )
            finish()
        }
    }

    authorizedRoute.build()
    return authorizedRoute
}