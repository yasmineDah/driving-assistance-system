package app.ui.home


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import org.tensorflow.lite.examples.detection.R
import org.tensorflow.lite.examples.detection.databinding.ActivityHomeBinding
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.tensorflow.lite.examples.detection.DetectorActivity
import org.kodein.di.generic.instance as instance1

class HomeActivity : AppCompatActivity(), KodeinAware {

    override val kodein by kodein()
    private val factory: HomeViewModelFactory by this.instance1()

    private lateinit var viewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)


        val binding: ActivityHomeBinding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        viewModel = ViewModelProviders.of(this, factory).get(HomeViewModel::class.java)
        binding.viewmodel = viewModel

        binding.videoNav.setOnClickListener {
            // Handler code here.
            val intent = Intent(this,DetectorActivity::class.java)
            startActivity(intent);
        }
    }
}
