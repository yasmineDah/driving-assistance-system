package mainApp.app.ui.auth

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import app.ui.auth.AuthListener
import app.ui.auth.AuthViewModel
import app.ui.auth.AuthViewModelFactory
import app.utils.startHomeActivity
import kotlinx.android.synthetic.main.activity_login.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.tensorflow.lite.examples.detection.DetectorActivity
import org.tensorflow.lite.examples.detection.R
import org.tensorflow.lite.examples.detection.databinding.ActivityLoginBinding
import org.kodein.di.generic.instance as instance1


class LoginActivity : AppCompatActivity(), AuthListener, KodeinAware {

    override val kodein by kodein()
    private val factory : AuthViewModelFactory by this.instance1()

    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityLoginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        viewModel = ViewModelProviders.of(this, factory).get(AuthViewModel::class.java)
        binding.viewmodel = viewModel

        viewModel.authListener = this

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
