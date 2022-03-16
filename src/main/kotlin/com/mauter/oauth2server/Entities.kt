package com.mauter.oauth2server

import java.time.Instant
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.MappedSuperclass
import javax.persistence.Version

@Entity
class User(
    @Column(length = 20, nullable = false)
    val username: String,

    @Column(length = 60, nullable = false)
    val password: String,

    @Column(length = 50, nullable = false)
    val firstName: String,

    @Column(length = 50, nullable = false)
    val lastName: String,

    @Column(length = 150, nullable = false)
    val email: String,

    id: UUID = UUID.randomUUID(),
    version: Long? = null

) : AbstractBaseEntity(id, version) {

    fun copy(username: String?, password: String?, firstName: String?, lastName: String?, email: String?, id: UUID? = null, version: Long? = null): User {
        return User(
            username = username ?: this.username,
            password = password ?: this.password,
            firstName = firstName ?: this.firstName,
            lastName = lastName ?: this.lastName,
            email = email ?: this.email,
            id = id ?: this.id,
            version = version ?: this.version
        )
    }
}

@Entity
class App(
    @Column(length = 100, nullable = false)
    val name: String,

    @Column(length = 100, nullable = false)
    val clientId: String,

    @Column(length = 100, nullable = false)
    val clientSecret: String,

    @Column(length = 1000, nullable = false)
    val redirectUri: String,

    id: UUID = UUID.randomUUID(),
    version: Long? = null

) : AbstractBaseEntity(id, version)

@Entity
class AuthCode(
    @Column(length = 2000, nullable = false)
    val code: String,

    @Column(length = 100, nullable = false)
    val clientId: String,

    @Column(length = 1000, nullable = false)
    val redirectUri: String,

    @Column(nullable = false)
    val expiration: Instant,

    @ManyToOne
    @JoinColumn(name = "userId")
    val user: User,

    id: UUID = UUID.randomUUID(),
    version: Long? = null

) : AbstractBaseEntity(id, version)

@MappedSuperclass
abstract class AbstractBaseEntity(
    @Id
    @Column(name = "id", length = 16, unique = true, nullable = false)
    val id: UUID = UUID.randomUUID(),

    @Version
    val version: Long? = null

) {
    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?) = when {
        this === other -> true
        other == null -> false
        other !is AbstractBaseEntity -> false
        else -> id == other.id
    }
}
