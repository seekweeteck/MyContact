package my.tarc.mycontact

import android.net.Uri
import androidx.room.PrimaryKey
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

data class Profile(var name: String?= null, var phone: String?= null, var pic : String? = null) {

}