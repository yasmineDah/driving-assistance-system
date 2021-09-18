package app.ui.home

import android.view.View
import androidx.lifecycle.ViewModel
import app.utils.startLoginActivity
import mainApp.app.data.repositories.UserRepository

class HomeViewModel(
        private val repository: UserRepository
) : ViewModel() {

    val user by lazy {
        repository.currentUser()
    }

    fun logout(view: View){
        repository.logout()
        view.context.startLoginActivity()
    }
}