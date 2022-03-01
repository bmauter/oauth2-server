package com.mauter.oauth2server

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import java.io.UnsupportedEncodingException
import java.net.URI
import java.net.URLEncoder
import java.security.GeneralSecurityException
import java.security.cert.Certificate
import java.security.cert.CertificateEncodingException
import java.time.Instant
import java.util.Base64
import java.util.UUID


@Controller
class AuthenticationController(
    val appRepository: AppRepository,
    val userRepository: UserRepository,
    val authCodeRepository: AuthCodeRepository,
    val crypter: Crypter,
    val tokenFactory: TokenFactory,
    val certificate: Certificate
) {
    private val log: Logger = LoggerFactory.getLogger(AuthenticationController::class.java)

    @GetMapping("/login/oauth/authorize")
    fun showLogin(
        @RequestParam("response_type") responseType: String,
        @RequestParam("client_id") clientId: String,
        @RequestParam("redirect_uri") redirectUri: String,
        @RequestParam(name = "scope", required = false) scope: String?,
        @RequestParam(name = "state", required = false) state: String?
    ): ModelAndView {
        log.debug("showLogin: responseType={}, clientId={}, redirectUri={}, scope={}, state={}",
            responseType, clientId, redirectUri, scope, state)

        if ("code" != responseType) {
            throw BadInputException("Invalid response type.")
        }
        if (clientId.isEmpty()) {
            throw BadInputException("Invalid client.")
        }
        if (redirectUri.isEmpty()) {
            throw BadInputException("Invalid redirect URI.")
        }

        val app = appRepository.findByClientIdAndRedirectUri(clientId, redirectUri)
            ?: throw BadInputException("Unknown client.")

        val mav = ModelAndView()
        mav.viewName = "login"
        mav.addObject("client_id", clientId)
        mav.addObject("scope", scope)
        mav.addObject("state", state)
        mav.addObject("redirect_uri", redirectUri)
        mav.addObject("app", app)
        return mav
    }


    @PostMapping("/login/oauth/authorize")
    @Throws(GeneralSecurityException::class, UnsupportedEncodingException::class)
    fun login(
        @RequestParam("client_id") clientId: String,
        @RequestParam("redirect_uri") redirectUri: String,
        @RequestParam("username") username: String,
        @RequestParam("password") password: String,
        @RequestParam(name = "scope", required = false) scope: String?,
        @RequestParam(name = "state", required = false) state: String?
    ): ResponseEntity<Void> {
        log.debug("login: clientId={}, redirectUri={}, username={}, password={}, scope={}, state={}",
            clientId, redirectUri, username, password, scope, state)

        if (clientId.isEmpty()) {
            throw BadInputException("Invalid client.")
        }
        if (redirectUri.isEmpty()) {
            throw BadInputException("Invalid redirect URI.")
        }
        if (username.isEmpty()) {
            throw UnauthorizedException("Invalid credentials.")
        }
        if (password.isEmpty()) {
            throw UnauthorizedException("Invalid credentials.")
        }

        val app = appRepository.findByClientIdAndRedirectUri(clientId, redirectUri)
            ?: throw BadInputException("Invalid client.")

        val user = userRepository.findByUsername(username)
            ?: throw UnauthorizedException("Invalid credentials.")
        if (password != user.password) {
            throw UnauthorizedException("Invalid credentials.")
        }

        // at this point, the client is okay and the user is okay.  time to
        // make an authorization code and redirect them back.
        val code = UUID.randomUUID().toString().replace("-", "")

        authCodeRepository.save(AuthCode(
            code = code,
            clientId = clientId,
            redirectUri = redirectUri,
            expiration = Instant.now().plusSeconds(300),
            user = user
        ))

        val url = StringBuilder(redirectUri)
        url.append("?code=").append(URLEncoder.encode(code, "UTF-8"))
        if (state != null) {
            url.append("&state=").append(URLEncoder.encode(state, "UTF-8"))
        }
        return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create(url.toString()))
            .build()
    }


    @PostMapping("/login/oauth/token")
    @Throws(GeneralSecurityException::class, JsonProcessingException::class)
    fun getToken(
        @RequestParam("grant_type") grantType: String,
        @RequestParam("code") code: String,
        @RequestParam("client_id") clientId: String,
        @RequestParam("client_secret") clientSecret: String,
        @RequestParam("redirect_uri") redirectUri: String
    ): ResponseEntity<String> {
        log.debug( "getToken: grantType={}, code={}, clientId={}, clientSecret={}, redirectUri={}",
            grantType, code, clientId, clientSecret, redirectUri)

        if (grantType.isEmpty()) {
            throw BadInputException("Invalid grant type.")
        }
        if (clientId.isEmpty()) {
            throw BadInputException("Invalid client.")
        }
        if (clientSecret.isEmpty()) {
            throw BadInputException("Invalid client.")
        }
        if (redirectUri.isEmpty()) {
            throw BadInputException("Invalid redirect URI.")
        }

        val app = appRepository.findByClientIdAndRedirectUri(clientId, redirectUri)
            ?: throw BadInputException("Invalid client.")

        val authCode = authCodeRepository.findByCodeAndClientIdAndRedirectUri(code, clientId, redirectUri)
            ?: throw BadInputException("Invalid authorization code.")
        if (authCode.expiration.isBefore(Instant.now())) {
            throw BadInputException("Invalid authorization code.")
        }

        // delete the authcode since it should never be used again
        authCodeRepository.delete(authCode)
        val accessToken = tokenFactory.generate(
            mapOf(
                Pair("username", authCode.user.username),
                Pair("email", authCode.user.email),
                Pair("firstName", authCode.user.firstName),
                Pair("lastName", authCode.user.lastName)
            )
        )
        val mapper = ObjectMapper()
        val json = mapper.writer().writeValueAsString(
            mapOf(
                Pair("access_token", accessToken),
                Pair("token_type", TOKEN_TYPE),
                Pair("expires_in", TOKEN_EXPIRES_IN)
            )
        )
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/json"))
            .body(json)
    }


    @GetMapping("/login/oauth/cert")
    @Throws(CertificateEncodingException::class)
    fun getPemCert(): ResponseEntity<String> {
        val beginCert = "-----BEGIN CERTIFICATE-----"
        val endCert = "-----END CERTIFICATE-----"
        val lineSeparator = System.getProperty("line.separator")
        val encoder = Base64.getMimeEncoder(64, lineSeparator.toByteArray())
        val rawCrtText: ByteArray = certificate.encoded
        val encodedCertText = String(encoder.encode(rawCrtText))
        val pretty = "${beginCert}${lineSeparator}${encodedCertText}${lineSeparator}${endCert}"
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/pem-certificate-chain"))
            .header(HttpHeaders.CONTENT_DISPOSITION, "filename=\"cert.pem\"")
            .body(pretty)
    }
}