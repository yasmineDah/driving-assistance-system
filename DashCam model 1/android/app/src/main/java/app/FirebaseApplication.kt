package app
import android.app.Application
import app.data.firebase.FirebaseSource
import app.data.repositories.UserRepository
import app.ui.auth.AuthViewModelFactory
import app.ui.home.HomeViewModelFactory
import com.google.firebase.FirebaseApp
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton


class FirebaseApplication : Application(), KodeinAware{

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }

    override val kodein = Kodein.lazy {
        import(androidXModule(this@FirebaseApplication))

        bind() from singleton { FirebaseSource()}
        bind() from singleton { UserRepository(instance()) }
        bind() from provider { AuthViewModelFactory(instance()) }
        bind() from provider { HomeViewModelFactory(instance()) }

    }

}