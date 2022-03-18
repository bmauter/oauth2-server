package com.mauter.oauth2server

class UserEntityTests : AbstractEntityTest({

//    "equals adheres to Java and JPA contracts" {
//        val user = User("brian", "52ar320", "Brian", "Mauter", "brianmauter@gmail.com")
//        assertEqualityConsistency(User::class.java, user)
//    }
}) {

    fun asdf() {
        assertEqualityConsistency(User::class.java, User("brian", "52ar320", "Brian", "Mauter", "brianmauter@gmail.com"))
    }
}
