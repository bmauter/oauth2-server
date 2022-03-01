package com.mauter.oauth2server

import org.hibernate.annotations.GenericGenerator
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.MappedSuperclass

@Entity
open class User (
    @Column(length = 20, nullable = false)
    var username: String,

    @Column(length = 60, nullable = false)
    var password: String,

    @Column(length = 50, nullable = false)
    var firstName: String,

    @Column(length = 50, nullable = false)
    var lastName: String,

    @Column(length = 150, nullable = false)
    var email: String,
) : UUIDKeyedObject(null)

@Entity
open class App (
    @Column(length = 100, nullable = false)
    var name: String,

    @Column(length = 100, nullable = false)
    var clientId: String,

    @Column(length = 100, nullable = false)
    var clientSecret: String,

    @Column(length = 1000, nullable = false)
    var redirectUri: String
) : UUIDKeyedObject(null)

@Entity
open class AuthCode (
    @Column(length = 2000, nullable = false)
    var code: String,

    @Column(length = 100, nullable = false)
    var clientId: String,

    @Column(length = 1000, nullable = false)
    var redirectUri: String,

    @Column(nullable = false)
    var expiration: Instant,

    @ManyToOne
    @JoinColumn(name = "userId")
    var user: User
) : UUIDKeyedObject(null)

@MappedSuperclass
open class UUIDKeyedObject (
    @Id
    @GeneratedValue(generator="UUID")
    @GenericGenerator(name="UUID", strategy="org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "CHAR(36)")
    var id: String? = null
)