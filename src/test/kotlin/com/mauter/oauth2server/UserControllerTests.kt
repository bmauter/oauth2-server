package com.mauter.oauth2server

import com.fasterxml.jackson.module.kotlin.readValue
import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.collections.shouldContainExactly
import io.mockk.every
import io.mockk.mockk
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import java.util.UUID

@WebMvcTest(UserController::class)
class UserControllerTests() : AbstractControllerTest() {
    private val service = mockk<UserService>()

    init {
        "GET /users should return all users" {
            val brian = User("brian", "52ar320", "Brian", "Mauter", "brianmauter@gmail.com", UUID.randomUUID().toString())
            val janie = User("janie", "52ar320", "Janie", "Mauter", "janiemauter@gmail.com", UUID.randomUUID().toString())
            val expected = listOf<User>(brian, janie)

            every { service.list() } returns expected

            val result = mockMvc.get("/users") {
                accept(MediaType.APPLICATION_JSON)
            }
                .andExpect {
                    status { isOk() }
                    content {
                        contentType(MediaType.APPLICATION_JSON)
                    }
                }
                .andDo { print() }
                .andReturn().response.contentAsString

            val actual = mapper.readValue<List<User>>(result)
            actual shouldContainExactly expected
        }
    }
}
