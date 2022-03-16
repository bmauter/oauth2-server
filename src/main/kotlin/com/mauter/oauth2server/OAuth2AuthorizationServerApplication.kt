package com.mauter.oauth2server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OAuth2AuthorizationServerApplication

fun main(args: Array<String>) {
    runApplication<OAuth2AuthorizationServerApplication>(*args)
}
