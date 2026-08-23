package com.maranatha.skools.security

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class RoleAuthorizationConfig {
    var requiredRoles: Set<String> = emptySet()
}

val RoleAuthorizationPlugin = createRouteScopedPlugin(
    name = "RoleAuthorizationPlugin",
    createConfiguration = ::RoleAuthorizationConfig
) {
    val roles = pluginConfig.requiredRoles

    on(AuthenticationChecked) { call ->
        val principal = call.principal<JWTPrincipal>()
        val userRole = principal?.payload?.getClaim("role")?.asString()

        if (userRole == null || userRole !in roles) {
            call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Insufficient permissions"))
        }
    }
}

fun Route.authorizeRoles(vararg roles: String, build: Route.() -> Unit): Route {
    val route = createChild(AuthorizedRouteSelector(roles.joinToString(",")))
    route.install(RoleAuthorizationPlugin) {
        requiredRoles = roles.toSet()
    }
    route.build()
    return route
}

fun Route.withRole(vararg roles: String, build: Route.() -> Unit): Route =
    authorizeRoles(*roles, build = build)

class AuthorizedRouteSelector(private val description: String) : RouteSelector() {
    override suspend fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
        return RouteSelectorEvaluation.Constant
    }

    override fun toString(): String = "(authorize $description)"
}