package my.tarc.mycontact

import android.R.attr
import android.app.Activity.RESULT_OK
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import my.tarc.mycontact.databinding.FragmentProfileBinding
import android.R.attr.bitmap
import android.os.Environment
import android.util.Log
import android.graphics.drawable.BitmapDrawable
import android.os.Environment.getExternalStorageDirectory
import java.io.*
import java.lang.StringBuilder


/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val contactViewModel: ContactViewModel by activityViewModels()

    lateinit var filepath: Uri

    @RequiresApi(Build.VERSION_CODES.P)
    val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == RESULT_OK) {
                filepath = result.data?.data!!

                var bitmap: Bitmap

                val source: ImageDecoder.Source = ImageDecoder.createSource(
                    context?.applicationContext!!.contentResolver, filepath
                )
                bitmap = ImageDecoder.decodeBitmap(source)

                binding.imageViewPicture.setImageBitmap(bitmap)
                binding.textViewPath.text = filepath.path.toString()

                //Get file path from Uri
                contactViewModel.profile.pic = filepath.path.toString()
                saveImage()
            }
        }

    fun ContentResolver.getFileName(fileUri: Uri): String {

        var name = ""
        val returnCursor = this.query(fileUri, null, null, null, null)
        if (returnCursor != null) {
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            name = returnCursor.getString(nameIndex)
            returnCursor.close()
        }

        return name
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        //Allow fragment to respond to menu item selection
        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.progressBar.visibility = View.GONE

        //Read profile data
        binding.editTextTextPersonName2.setText(contactViewModel.profile.name)
        binding.editTextPhone2.setText(contactViewModel.profile.phone)
        if (!contactViewModel.profile.pic.isNullOrEmpty()) {
            try {
                val dir = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
                val file = File(dir, contactViewModel.profile.phone + ".jpg")

                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                binding.imageViewPicture.setImageBitmap(bitmap)

            } catch (ex: IOException) {
                Log.d("Profile Picture", ex.message.toString())
            }

            binding.textViewPath.text = contactViewModel.profile.pic
        } else {
            binding.imageViewPicture.setImageResource(R.drawable.profile)
        }

        binding.imageViewPicture.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.setType("image/*")
            getContent.launch(intent)
        }

        binding.buttonSaveProfile.setOnClickListener {
            contactViewModel.profile.name = binding.editTextTextPersonName2.text.toString()
            contactViewModel.profile.phone = binding.editTextPhone2.text.toString()

            val dir = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
            contactViewModel.profile.pic = File(dir, contactViewModel.profile.phone + "jpg").absolutePath

            uploadPic()
            //downloadPic()
            savePreference(contactViewModel.profile)
        }
    }

    private fun downloadPic() {
        val myStorage = Firebase.storage("gs://my-contact-89b38.appspot.com")
        val myStorageRef = myStorage.reference
        val myProfileRef = myStorageRef.child("images")
        val myProfileImageRef = myProfileRef.child(contactViewModel.profile.phone.toString())

        val localFile = File.createTempFile("images", "jpg")

        myProfileImageRef.getFile(localFile)
            .addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                binding.imageViewPicture.setImageBitmap(bitmap)
                contactViewModel.profile.pic = localFile.absolutePath.toUri().toString()
                Toast.makeText(context, "Picture downloaded", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
            .addOnProgressListener {

            }
    }

    fun savePreference(profile: Profile) {
        var preferences: SharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE)!!
        with(preferences.edit()) {
            putString("name", profile.name)
            putString("phone", profile.phone)
            putString("pic", profile.pic)
            apply()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        //Make Toolbar invisible
        menu.setGroupVisible(R.id.group_action, false)
        menu.setGroupVisible(R.id.group_db, false)
    }

    private fun saveImage() {
        val dir = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

        if (!dir.exists()) {
            dir.mkdir()
        }
        val drawable = binding.imageViewPicture.drawable as BitmapDrawable
        val bitmap = drawable.bitmap
        val file = File(dir, contactViewModel.profile.phone + ".jpg")

        val outputStream: OutputStream

        try {
            outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            Toast.makeText(context, "Successfuly Saved", Toast.LENGTH_SHORT).show()
            outputStream.flush()
            outputStream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun uploadPic() {
        if (filepath != null) {

            binding.progressBar.visibility = View.VISIBLE

            val myStorage = Firebase.storage("gs://my-contact-89b38.appspot.com")
            val myStorageRef = myStorage.reference
            val myProfileRef = myStorageRef.child("images")
            val myProfileImageRef = myProfileRef.child(contactViewModel.profile.phone.toString())

            myProfileImageRef.putFile(filepath)
                .addOnSuccessListener { p0 ->
                    binding.progressBar.visibility = View.GONE

                    Toast.makeText(context, "File Uploaded", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener { p0 ->
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, p0.message, Toast.LENGTH_LONG).show()
                }
                .addOnProgressListener { p0 ->
                    var progress: Double = (100.0 * p0.bytesTransferred) / p0.totalByteCount
                    binding.progressBar.progress = progress.toInt()
                }
        }
    }

}