package com.mauter.oauth2server

import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
@Transactional
class UserService(val userRepository: UserRepository) {

    fun list(): Iterable<User> = userRepository.findAll()
    fun get(username: String) = userRepository.findByUsername(username)

    @Transactional
    fun add(user: User) = userRepository.save(user)

    @Transactional
    fun update(user: User): User {
        if (user.id == null) {
            throw BadInputException("Invalid user ID.")
        }

        val current = userRepository.findById(user.id).orElseThrow { NotFoundException("User not found.") }

        var changed = false
        if (current.username != user.username) {
            current.username = user.username
            changed = true
        }
        if (current.firstName != user.firstName) {
            current.firstName = user.firstName
            changed = true
        }
        if (current.lastName != user.lastName) {
            current.lastName = user.lastName
            changed = true
        }
        if (current.email != user.email) {
            current.email = user.email
            changed = true
        }

        return if (changed) userRepository.save(current) else current
    }
}
