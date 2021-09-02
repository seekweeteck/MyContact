package my.tarc.mycontact

import android.content.SharedPreferences
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow

class ContactRepository(private val contactDao: ContactDao){
    //Room execute all queries on a separate thread
    val allContacts: LiveData<List<Contact>> = contactDao.getAllContact()
   /* private var myProfile: Profile = Profile()
    private lateinit var  preferences: SharedPreferences*/


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun add(contact: Contact){
        contactDao.insert(contact)
    }

    suspend fun delete(contact: Contact){
        contactDao.delete(contact)
    }

    /*suspend fun setProfile(profile: Profile){
        myProfile = profile
    }

    fun getProfile(): Profile{
        //preferences = getPreferences(AppCompatActivity.MODE_PRIVATE)

        return myProfile
    }*/
}