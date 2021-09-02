package my.tarc.mycontact

import android.app.Application
import android.content.ContentValues
import android.net.Uri
import android.util.Log
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

    init {
        val contactDao = ContactDatabase.getDatabase(application).contactDao()
        repository = ContactRepository(contactDao)
        contactList = repository.allContacts
    }

    fun addContact(contact: Contact) = viewModelScope.launch {
        repository.add(contact)
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

    fun updateProfile(newProfile: Profile) {
        profile = newProfile

        val firebaseDatabase = Firebase.database
        val myRef = firebaseDatabase.getReference("profile")

        myRef.child(newProfile.phone.toString()).child("name").setValue(newProfile.name)
        myRef.child(newProfile.phone.toString()).child("phone").setValue(newProfile.phone)
        myRef.child(newProfile.phone.toString()).child("pic").setValue(newProfile.pic)

        val myStorage = Firebase.storage("gs://my-contact-89b38.appspot.com")
        val myStorageRef = myStorage.reference
        val myProfileRef = myStorageRef.child(profile.pic.toString())
        val myProfileImageRef = myProfileRef.child("profile-images/"+ profile.pic.toString())

        myProfileImageRef.putFile(Uri.parse(profile.pic))
    }

    fun uploadContact() {
        if (!profile.name.isNullOrEmpty()) {
            val firebaseDatabase = Firebase.database
            val myRef = firebaseDatabase.getReference("profile")

            for (contact in contactList.value?.iterator()!!) {
                myRef.child(profile?.phone!!.toString()).child("contact_list").child(contact.phone)
                    .child("name").setValue(contact.name)
                myRef.child(profile?.phone!!.toString()).child("contact_list").child(contact.phone)
                    .child("phone").setValue(contact.phone)
            }
        }else{
            Log.d(ContentValues.TAG, "Profile is null")
        }
    }
}

