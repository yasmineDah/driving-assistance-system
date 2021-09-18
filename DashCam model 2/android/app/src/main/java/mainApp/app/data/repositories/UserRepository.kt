package mainApp.app.data.repositories

import mainApp.app.data.firebase.FirebaseSource

class UserRepository (
        private val firebase: FirebaseSource
){
    fun login(email: String, password: String) = firebase.login(email, password)

    fun register(username: String, email: String, password: String) = firebase.register(username, email, password)

    fun currentUser() = firebase.currentUser()

    fun logout() = firebase.logout()
}