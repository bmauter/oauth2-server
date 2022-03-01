package com.mauter.oauth2server

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.util.ObjectUtils
import org.springframework.web.filter.CommonsRequestLoggingFilter
import java.io.File
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.Certificate
import javax.annotation.PostConstruct


@SpringBootApplication
class OAuth2AuthorizationServerApplication(
	val userRepository: UserRepository,
	val appRepository: AppRepository
) {
	@Bean
	fun tokenFactory(
		@Value("\${token-factory.key-store}") keystore: File,
		@Value("\${token-factory.keystore-password}") keystorePassword: String,
		@Value("\${token-factory.key-alias}") keyAlias: String,
		@Value("\${token-factory.key-password}") keyPassword: String
	): TokenFactory? {
		val ks = KeyStore.getInstance(keystore, keystorePassword.toCharArray())
		var privateKey: PrivateKey? = null
		if (!ObjectUtils.isEmpty(keyAlias) && !ObjectUtils.isEmpty(keyPassword)) {
			privateKey = ks.getKey(keyAlias, keyPassword.toCharArray()) as PrivateKey?
		}
		val publicKey = ks.getCertificate(keyAlias).publicKey
		return TokenFactory(privateKey, publicKey)
	}


	@Bean
	fun certificate(
		@Value("\${token-factory.key-store}") keystore: File,
		@Value("\${token-factory.keystore-password}") keystorePassword: String,
		@Value("\${token-factory.key-alias}") keyAlias: String
	): Certificate? {
		val ks = KeyStore.getInstance(keystore, keystorePassword.toCharArray())
		return ks.getCertificate(keyAlias)
	}

	@Bean
	fun logFilter(): CommonsRequestLoggingFilter {
		val filter = CommonsRequestLoggingFilter()
		filter.setIncludeQueryString(true)
		filter.setIncludePayload(true)
		filter.setMaxPayloadLength(10000)
		filter.setIncludeHeaders(false)
		filter.setAfterMessagePrefix("REQUEST DATA : ")
		return filter
	}


	@PostConstruct
	fun postConstruct() {
		appRepository.save(
			App(
				name = "ASDF Inc Spring Boot",
				clientId = "asdf01",
				clientSecret = "qwerty4life!",
				redirectUri = "https://localhost:9443/login/oauth2/code/brian"
			)
		)

		appRepository.save(
			App(
				name = "PHP Script",
				clientId = "phpscript",
				clientSecret = "phpscript1234",
				redirectUri = "http://localhost:8000/index.php"
			)
		)

		userRepository.save(
			User(
				username = "bmauter",
				password = "52ar320",
				firstName = "Brian",
				lastName = "Mauter",
				email = "brianmauter@gmail.com"
			)
		)
	}
}

fun main(args: Array<String>) {
	runApplication<OAuth2AuthorizationServerApplication>(*args)
}
