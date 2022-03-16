package com.mauter.oauth2server

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.util.ObjectUtils
import org.springframework.web.filter.CommonsRequestLoggingFilter
import java.io.File
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.Certificate

@Configuration
@ConditionalOnWebApplication
class WebConfig : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http
            .csrf().disable()
            .httpBasic().disable()
            .formLogin().disable()
            .authorizeRequests()
            .antMatchers("/bob").authenticated()
            .anyRequest().permitAll()
    }

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
}
