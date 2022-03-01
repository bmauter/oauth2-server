package com.mauter.oauth2server

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwsHeader
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.SigningKeyResolverAdapter
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.impl.DefaultJwsHeader
import java.math.BigInteger
import java.security.Key
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.interfaces.RSAKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.Date
import java.util.function.Function

const val TOKEN_TYPE = "JWT"
const val TOKEN_EXPIRES_IN = 10 * 24 * 60 * 60 * 1000 // 10 days

class TokenFactory(val privateKey: PrivateKey? = null, vararg publicKeys: PublicKey) {
    val parser: JwtParser

    init {
        val resolver = object : SigningKeyResolverAdapter() {
            val publicKeyMap = publicKeys
                .filterIsInstance<RSAPublicKey>()
                .associateBy { keyId(it) }

            override fun resolveSigningKey(header: JwsHeader<*>?, claims: Claims?): Key {
                return publicKeyMap[header?.keyId] ?: throw UnsupportedJwtException("Unable to resolve signing key for kid ${header?.keyId}.")
            }
        }

        parser = Jwts.parser()
            .setAllowedClockSkewSeconds(300)
            .setSigningKeyResolver(resolver)
    }


    fun getSubjectFromToken(token: String) = getClaimFromToken(token, Claims.SUBJECT)
    fun getIssuedAtDateFromToken(token: String) = getClaimFromToken(token, Claims.ISSUED_AT)
    fun getExpirationDateFromToken(token: String) = getClaimFromToken(token, Claims.EXPIRATION)

    fun getClaimFromToken(token: String?, key: String?): String? = parse(token).get(key, String::class.java)
    fun <T> getClaimFromToken(token: String?, claimsResolver: Function<Claims, T>): T = claimsResolver.apply(parse(token))

    fun parse(token: String?): Claims = parser.parseClaimsJwt(token).body

    fun generate(claims: Map<String, Any?>): String = generate(Jwts.claims(claims))

    fun generate(claims: Claims): String {
        if (privateKey == null) {
            throw UnsupportedOperationException("This application is not configured to generate JSON web tokens.")
        }

        val header = DefaultJwsHeader()
        if (privateKey is RSAPrivateKey) header.keyId = keyId(privateKey)

        return Jwts.builder()
            .setHeader(header)
            .setIssuer("Brian Auth")
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + TOKEN_EXPIRES_IN))
            .setClaims(claims)
            .signWith(SignatureAlgorithm.RS256, privateKey)
            .compact()
    }


    fun keyId(key: RSAKey): String {
        val md = MessageDigest.getInstance("SHA-1")
        val messageDigest = md.digest(key.modulus.toByteArray())
        val no = BigInteger(1, messageDigest)
        return no.toString(16)
    }

}