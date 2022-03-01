package com.mauter.oauth2server

import org.springframework.data.repository.CrudRepository
import java.util.*

interface UserRepository : CrudRepository<User, UUID> {
    fun findByUsername(username: String): User?
}

interface AppRepository : CrudRepository<App, UUID> {
    fun findByClientId(clientId: String?): App?
    fun findByClientIdAndRedirectUri(clientId: String?, redirectUri: String?): App?
}

interface AuthCodeRepository : CrudRepository<AuthCode, UUID> {
    fun findByCodeAndClientIdAndRedirectUri(code: String?, clientId: String?, redirectUri: String?): AuthCode?
}