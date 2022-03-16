package com.mauter.oauth2server

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserController(val userService: UserService) {

    @GetMapping
    fun list(): Iterable<User> = userService.list()

    @GetMapping("/{username}")
    fun get(@PathVariable username: String) = userService.get(username)

    @PostMapping
    fun add(user: User) = userService.add(user)

    @PutMapping("/{username}")
    fun update(@PathVariable username: String, user: User) = userService.update(user)
}
