package app.ui.home

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.data.model.Danger
import app.data.model.LocationHelper
import app.data.repositories.UserRepository
import app.utils.startLoginActivity

class HomeViewModel(
        private val repository: UserRepository

) : ViewModel() {

    private  var liveData: MutableLiveData<ArrayList<Danger>> = repository.getDangers()


    fun  getLiveData(): MutableLiveData<ArrayList<Danger>> {
            return liveData
        }

    fun init(){
         liveData.value = repository.getDangers().value
    }


    val user by lazy {
        repository.currentUser()
    }

    fun logout(view: View){
        repository.logout()
        view.context.startLoginActivity()
    }

    fun setLocation(locat:LocationHelper){
        repository.setLocation(locat)
    }

    fun sendRegistrationToServer() {
        repository.sendRegistrationToServer()
    }


}