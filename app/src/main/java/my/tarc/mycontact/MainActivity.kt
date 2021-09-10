package my.tarc.mycontact

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import my.tarc.mycontact.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    //private val myViewModel: ContactViewModel by viewModels()
    private lateinit var myViewModel: ContactViewModel

    private lateinit var  preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        myViewModel = ViewModelProvider(this).get(ContactViewModel::class.java)

        myViewModel.contactList.observe(this, Observer {
            Log.d("MainActivity", "Contact List Size:" + it.size)
        })

        /*preferences = getSharedPreferences(applicationContext.packageName, MODE_PRIVATE)

        preferences.apply {
            val name = getString("name", null)
            val phone = getString("phone", null)
            val pic = getString("pic", null)
            myViewModel.profile = Profile(name, phone, pic)
        }*/

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {

            R.id.action_settings -> {
                val navController = findNavController(R.id.nav_host_fragment_content_main)
                navController.navigate(R.id.action_FirstFragment_to_SettingsFragment)
                true
            }
            R.id.action_about ->{
                val navController = findNavController(R.id.nav_host_fragment_content_main)
                navController.navigate(R.id.action_FirstFragment_to_AboutFragment)
                true
            }
            R.id.action_profile ->{
                val navController = findNavController(R.id.nav_host_fragment_content_main)
                navController.navigate(R.id.action_FirstFragment_to_ProfileFragment)
                true
            }
            R.id.action_sync -> {
                myViewModel.uploadContact()
                Toast.makeText(this, "Uploading completed", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun showHideMenu(visible: Boolean){
        binding.toolbar.menu.findItem(R.id.action_settings).isVisible = visible
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

   /* companion object{
        val contactList = ArrayList<Contact>()
        private var index = -1
    }*/
}