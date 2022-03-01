package com.mauter.oauth2server

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import java.time.Instant

@DataJpaTest
class RepositoriesTests @Autowired constructor(
    val entityManager: TestEntityManager,
    val userRepository: UserRepository,
    val appRepository: AppRepository,
    val authCodeRepository: AuthCodeRepository) {

    @Test
    fun `When findByUsername then return User`() {
        val expected = User("theuser", "thepassword", "the", "user", "theuser@example.com")
        entityManager.persist(expected)
        entityManager.flush()

        val actual = userRepository.findByUsername(expected.username)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `When findByClientId then return App`() {
        val expected = App("theapp", "clientId", "clientSecret", redirectUri="http://example.com/app")
        entityManager.persist(expected)
        entityManager.flush()

        val actual = appRepository.findByClientId(expected.clientId)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `When findByClientIdAndRedirectUri then return App`() {
        val expected = App("theapp", "clientId", "clientSecret", redirectUri="http://example.com/app")
        entityManager.persist(expected)
        entityManager.flush()

        val actual = appRepository.findByClientIdAndRedirectUri(expected.clientId, expected.redirectUri)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `When findByCodeAndClientIdAndRedirectUri then return AuthCode`() {
        val user = User("username", "password", "first", "last", "firstlast@example.com")
        entityManager.persist(user)

        val expected = AuthCode("code", "clientId", "http://example.com/app", Instant.now(), user)
        entityManager.persist(expected)
        entityManager.flush()

        val actual = authCodeRepository.findByCodeAndClientIdAndRedirectUri(expected.code, expected.clientId, expected.redirectUri)
        assertThat(actual).isEqualTo(expected)
    }

}