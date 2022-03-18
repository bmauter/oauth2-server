package com.mauter.oauth2server

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserService(val userRepository: UserRepository) {

    fun list(): Iterable<User> = userRepository.findAll()
    fun get(username: String) = userRepository.findByUsername(username) ?: throw NotFoundException("User not found.")

    @Transactional
    fun add(user: User) = userRepository.save(user)

    @Transactional
    fun update(username: String, user: User): User {
        val current = userRepository.findByUsername(username) ?: throw NotFoundException("User not found.")

        if (current.id != user.id) {
            throw BadInputException("Invalid username.")
        }

        val changed = current.copy(
            username = user.username,
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email
        )

        return userRepository.save(changed)
    }
}
