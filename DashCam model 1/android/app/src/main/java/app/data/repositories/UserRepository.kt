package app.data.repositories

import android.location.Location
import androidx.lifecycle.MutableLiveData
import app.data.firebase.FirebaseSource
import app.data.model.Danger
import app.data.model.LocationHelper

class UserRepository (
        private val firebase: FirebaseSource
){
    fun login(email: String, password: String) = firebase.login(email, password)

    fun register(username: String, email: String, password: String) = firebase.register(username, email, password)

    fun currentUser() = firebase.currentUser()

    fun logout() = firebase.logout()


    fun getDangers()= firebase.retrieveDanger()

    fun setLocation(locat: LocationHelper) = firebase.setLocation(locat)
    fun initFirebase() {
        firebase.initApp()
    }

    fun sendRegistrationToServer() {
        firebase.sendRegistrationToServer()

    }

}