package com.mauter.oauth2server

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.spring.SpringExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc

@ActiveProfiles("test")
abstract class AbstractControllerTest(body: StringSpec.() -> Unit = {}) : StringSpec(body = body) {

    @Autowired
    protected lateinit var mockMvc: MockMvc

    protected lateinit var mapper: ObjectMapper

    init {
        extension(SpringExtension)

        mapper = ObjectMapper()
    }
}
