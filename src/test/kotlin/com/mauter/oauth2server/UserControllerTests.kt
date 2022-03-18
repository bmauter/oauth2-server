package com.mauter.oauth2server

import com.fasterxml.jackson.module.kotlin.readValue
import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import java.util.UUID

@WebMvcTest(UserController::class)
class UserControllerTests() : AbstractControllerTest() {

    @MockkBean
    private lateinit var service: UserService

    init {
        "GET /users should return all users" {
            val brian = User("brian", "52ar320", "Brian", "Mauter", "brianmauter@gmail.com", UUID.randomUUID())
            val janie = User("janie", "52ar320", "Janie", "Mauter", "janiemauter@gmail.com", UUID.randomUUID())
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
                .andReturn().response.contentAsString

            val actual = mapper.readValue<List<User>>(result)
            actual shouldContainExactly expected
        }

        "GET /users/{username} should return one user" {
            val brian = User("brian", "52ar320", "Brian", "Mauter", "brianmauter@gmail.com", UUID.randomUUID())

            every { service.get("brian") } returns brian

            val result = mockMvc.get("/users/brian") {
                accept(MediaType.APPLICATION_JSON)
            }
                .andExpect {
                    status { isOk() }
                    content {
                        contentType(MediaType.APPLICATION_JSON)
                    }
                }
                .andReturn().response.contentAsString

            val actual = mapper.readValue<User>(result)
            actual shouldBe brian

            println("is 5 chars long? ${result hasLength 5}")
        }

        "GET /users/bogus should 404" {
            every { service.get("bogus") } throws NotFoundException("User not found.")

            mockMvc.get("/users/bogus") {
                accept(MediaType.APPLICATION_JSON)
            }
                .andExpect {
                    status { isNotFound() }
                }
        }
    }
}
