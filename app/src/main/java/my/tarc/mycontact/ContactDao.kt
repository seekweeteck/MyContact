package my.tarc.mycontact

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contact ORDER BY name ASC")
    fun getAllContact(): LiveData<List<Contact>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(contact: Contact)

    @Delete
    suspend fun delete(contact: Contact)
}