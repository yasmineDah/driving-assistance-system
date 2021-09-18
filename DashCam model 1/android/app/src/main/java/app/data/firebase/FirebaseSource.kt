package app.data.firebase


import android.util.Log
import androidx.lifecycle.MutableLiveData
import app.data.model.Danger
import app.data.model.LocationHelper
import app.data.model.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import io.reactivex.Completable
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.*


class FirebaseSource {


    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val databaseRefUser: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference("users")
    }

    private val databaseRefDanger: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference("dangers")
    }

    private val danger: MutableLiveData<ArrayList<Danger>> by lazy {
        MutableLiveData<ArrayList<Danger>>()
    }

    private val arrayList: ArrayList<Danger> by lazy {
        ArrayList<Danger>()
    }


    private lateinit var info: User

    fun login(email: String, password: String) = Completable.create { emitter ->
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (!emitter.isDisposed) {
                if (it.isSuccessful) {

//                    this.currentUser()?.uid?.let { it1 ->
//                        databaseRefUser.child(it1).setValue(info)
//                    }
                    emitter.onComplete()
                } else
                    emitter.onError(it.exception!!)
            }
        }
    }

    fun register(username: String, email: String, password: String) = Completable.create { emitter ->
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {

            if (it.isSuccessful) {
                info = User(username, email)
                this.currentUser()?.uid?.let { it1 ->
                    databaseRefUser.child(it1).setValue(info)
                }
                emitter.onComplete()
            } else
                emitter.onError(it.exception!!)

        }
    }

    fun logout() = firebaseAuth.signOut()

    fun currentUser() = firebaseAuth.currentUser

    fun retrieveDanger(): MutableLiveData<ArrayList<Danger>> {

        if (arrayList.size == 0) {
            loadDangers()
        }
        danger.value = arrayList
        return danger
    }

    private fun loadDangers() {

        databaseRefDanger.addValueEventListener(object : ValueEventListener {


            override fun onDataChange(snapshot: DataSnapshot) {

                val children = snapshot!!.children
                // This returns the correct child count...
                // println("count: "+snapshot.children.count().toString())
                children.forEach {
                    println("voila un danger : "+it.value)
                    it.getValue(Danger::class.java)?.let { it1 -> arrayList.add(it1) }
                }

                danger.value = arrayList
            }

            override fun onCancelled(error: DatabaseError) {
                println(error!!.message)
            }
        })

    }

    fun addDanger(locat: LocationHelper, i: Int) {


        var type = "";
        println("ta localisation est "+locat.getAlt()+" , "+locat.getLong())
        when(i){
            1 -> type = "presence of animals"
            2 -> type = "presence of work"
            3 -> type = "traffic jam"
        }


        var isExist = false

        val query: Query = when (i) {
            1 -> (databaseRefDanger.orderByChild("number").equalTo(1.0))
            2 -> (databaseRefDanger.orderByChild("number").equalTo(2.0))
            else -> (databaseRefDanger.orderByChild("number").equalTo(3.0))
        }

        query.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onCancelled(error: DatabaseError) {
                println(error!!.message)
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val children = snapshot!!.children
                // This returns the correct child count...
                println("count: " + snapshot.children.count().toString())
                children.forEach {
                    println(it.toString())
                    it.getValue(Danger::class.java)?.let { it1 ->

                        println("alt du danger est "+it1.getLocation().getAlt())
                        if (distance(it1.getLocation().getAlt(), locat.getAlt(), it1.getLocation().getLong(), locat.getLong()) < 2)
                        isExist = true
                        return@forEach

                    }

                }

                if(!isExist) {

                    databaseRefDanger.push().setValue(Danger(locat, i, type, Date().time))
                }

            }
        })


    }


    fun distance(lat1: Double,
                 lat2: Double, lon1: Double,
                 lon2: Double): Double {

        // The math module contains a function
        // named toRadians which converts from
        // degrees to radians.
        val lon1 = Math.toRadians(lon1)
        val lon2 = Math.toRadians(lon2)
        val lat1 = Math.toRadians(lat1)
        val lat2 = Math.toRadians(lat2)

        // Haversine formula
        val dlon = lon2 - lon1
        val dlat = lat2 - lat1
        val a = (sin(dlat / 2).pow(2.0)
                + (cos(lat1) * cos(lat2)
                * sin(dlon / 2).pow(2.0)))
        val c = 2 * asin(sqrt(a))

        // Radius of earth in kilometers. Use 3956
        // for miles
        val r = 6371.0

        // calculate the result
        println("la distance est "+c*r)
        return c * r

    }

    fun setLocation(locat: LocationHelper) {
        println("loucif : "+locat)

        this.currentUser()?.uid?.let { it1 ->
            databaseRefUser.child(it1).child("location").setValue(locat)

        }
    }

    fun initApp() {

        FirebaseMessaging.getInstance().isAutoInitEnabled = true

    }


    fun setUserToken(token: String){

        this.currentUser()?.uid?.let { it1 ->
            databaseRefUser.child(it1).child("token").setValue(token)
        }
    }

    fun sendRegistrationToServer() {

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(" token failed", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
//            val msg = getString(R.string.msg_token_fmt, token)
//            Log.d(TAG, msg)
//            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            Log.d("token est :",token)
            setUserToken(token)
        })
    }

}