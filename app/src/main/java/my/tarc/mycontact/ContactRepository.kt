package my.tarc.mycontact

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.SharedPreferences
import android.database.sqlite.SQLiteException
import android.widget.Toast
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow

class ContactRepository(private val contactDao: ContactDao){
    //Room execute all queries on a separate thread
    val allContacts: LiveData<List<Contact>> = contactDao.getAllContact()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun add(contact: Contact){
        contactDao.insert(contact)
    }

    suspend fun delete(contact: Contact){
        contactDao.delete(contact)
    }

    suspend fun update(contact: Contact){
        contactDao.update(contact)
    }

    /*suspend fun setProfile(profile: Profile){
        myProfile = profile
    }

    fun getProfile(): Profile{
        //preferences = getPreferences(AppCompatActivity.MODE_PRIVATE)

        return myProfile
    }*/
}