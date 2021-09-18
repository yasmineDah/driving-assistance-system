package mainApp.app.data.firebase


import mainApp.app.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import io.reactivex.Completable


class FirebaseSource {


    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val databaseRef: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference("users")
    }

    private lateinit var info: User

    fun login(email: String, password: String) = Completable.create { emitter ->
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (!emitter.isDisposed) {
                if (it.isSuccessful) {

//                    info = User(email, password)
//                    this.currentUser()?.uid?.let { it1 ->
//                        databaseRef.child(it1).setValue(info)
//                    }
                    emitter.onComplete()
                } else
                    emitter.onError(it.exception!!)
            }
        }
    }

    fun register(username:String, email: String, password: String) = Completable.create { emitter ->
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {

                if (it.isSuccessful) {
                    info = User(username,email)
                    this.currentUser()?.uid?.let { it1 ->
                        databaseRef.child(it1).setValue(info)
                    }
                    emitter.onComplete()
                } else
                    emitter.onError(it.exception!!)

        }
    }

    fun logout() = firebaseAuth.signOut()

    fun currentUser() = firebaseAuth.currentUser

}