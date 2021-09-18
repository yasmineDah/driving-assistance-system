package app.ui.auth

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import app.utils.startHomeActivity
import kotlinx.android.synthetic.main.activity_login.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import org.tensorflow.lite.examples.detection.DetectorActivity
import app.R
import app.databinding.ActivityLoginBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging


class LoginActivity : AppCompatActivity(), AuthListener, KodeinAware {

    override val kodein by kodein()
    private val factory : AuthViewModelFactory by instance()

    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityLoginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        viewModel = ViewModelProviders.of(this, factory).get(AuthViewModel::class.java)
        binding.viewmodel = viewModel

        viewModel.authListener = this
        viewModel.initFirebase()


//        binding.map.setOnClickListener {
//
//            var intent: Intent? = null
//            try {
//                intent = Intent(this,
//                        Class.forName("org.tensorflow.lite.examples.detection.DetectorActivity"))
//                startActivity(intent)
//            } catch (e: ClassNotFoundException) {
//                e.printStackTrace()
//            }
//            val intent = Intent(this, DetectorActivity::class.java)
//            intent.addCategory(Intent.CATEGORY_LAUNCHER)
//            startActivity(intent)
       }


    override fun onStarted() {
        progressbar.visibility = View.VISIBLE
    }

    override fun onSuccess() {
        progressbar.visibility = View.GONE
        startHomeActivity()
    }

    override fun onFailure(message: String) {
        progressbar.visibility = View.GONE
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        viewModel.user?.let {
            startHomeActivity()
        }
    }
}
