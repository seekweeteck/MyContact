package my.tarc.mycontact

import android.util.Log
import androidx.lifecycle.ViewModel

class ContactViewModel: ViewModel() {
    private val contactList = ArrayList<Contact>()

    init {
        Log.d("ViewModel", "Initialize")
    }

    fun addContact(contact: Contact){
        contactList.add(contact)
    }

    fun removeContact(contact: Contact){
        contactList.remove(contact)
    }

    fun getContacts() : ArrayList<Contact>{
        return contactList
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("ViewModel", "Cleared")
    }
}