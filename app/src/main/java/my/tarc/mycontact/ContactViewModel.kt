package my.tarc.mycontact

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.os.ProxyFileDescriptorCallback
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch

class ContactViewModel(application: Application) : AndroidViewModel(application) {
    //LiveData gives us updated contacts when they change
    val contactList: LiveData<List<Contact>>
    private val repository: ContactRepository
    var profile: Profile = Profile()

    //To enable editing of contact record
    var selectedContact: Contact    //To hold contact selected by user
    var editMode: Boolean = false   //To differentiate add or edit mode

    init {
        val contactDao = ContactDatabase.getDatabase(application).contactDao()
        repository = ContactRepository(contactDao)
        contactList = repository.allContacts
        profile = getProfile(application)
        selectedContact = Contact("","")
    }

    private fun getProfile(context: Context): Profile {
        val preferences: SharedPreferences =  context.getSharedPreferences(context.packageName,
            Context.MODE_PRIVATE)!!
        val profile = Profile()

        if(preferences.contains(PROFILE_NAME)){
            profile.name = preferences.getString(PROFILE_NAME, null)
        }
        if(preferences.contains(PROFILE_PHONE)){
            profile.phone = preferences.getString(PROFILE_PHONE, null)
        }
        if(preferences.contains(PROFILE_PIC)){
            profile.pic = preferences.getString(PROFILE_PIC, null)
        }
        return profile
    }

    fun addContact(contact: Contact) = viewModelScope.launch {
        repository.add(contact)
    }

    fun updateContact(contact: Contact) = viewModelScope.launch {
        repository.update(contact)
    }

    fun deleteContact(contact: Contact) = viewModelScope.launch {
        repository.delete(contact)
    }

    private fun readProfile() {
        val firebaseDatabase = Firebase.database
        val myRef = firebaseDatabase.getReference("profile").child(profile.phone.toString())

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue<Profile>()
                profile = Profile(value?.name, value?.phone)
                Log.d(ContentValues.TAG, "Value is: $value")
            }

            override fun onCancelled(error: DatabaseError) {
                val error1 = error
                val value = error1
                Log.d(ContentValues.TAG, "Value is: $value")
            }
        })
    }

    //Save profile settings to Shared Preference file
    fun savePreference(context: Context) {
        val preferences: SharedPreferences = context.applicationContext.getSharedPreferences(context.packageName,
            Context.MODE_PRIVATE)!!
        with(preferences.edit()) {
            putString(PROFILE_NAME, profile.name)
            putString(PROFILE_PHONE, profile.phone)
            putString(PROFILE_PIC, profile.pic)
            apply()
        }
    }

    fun uploadContact() {
        if (!profile.name.isNullOrEmpty()) {
            val firebaseDatabase = Firebase.database
            val myRef = firebaseDatabase.getReference("profile")

            for (contact in contactList.value?.iterator()!!) {
                myRef.child(profile.phone!!.toString()).child("contact_list").child(contact.phone)
                    .child(PROFILE_NAME).setValue(contact.name)
                myRef.child(profile.phone!!.toString()).child("contact_list").child(contact.phone)
                    .child(PROFILE_PHONE).setValue(contact.phone)
            }
        }else{
            Log.d(ContentValues.TAG, "Profile is null")
        }
    }

    companion object{
        const val PROFILE_NAME = "name"
        const val PROFILE_PHONE = "phone"
        const val PROFILE_PIC = "pic"
    }
}

