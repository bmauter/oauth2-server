package com.mauter.oauth2server

import org.springframework.context.annotation.Profile
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
@Profile("!test")
class LoadInitialDataComponent(
    val userRepository: UserRepository,
    val appRepository: AppRepository
) {

    @EventListener
    fun loadInitialData(event: ContextRefreshedEvent) {
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
