package my.tarc.mycontact

import android.app.Activity.RESULT_OK
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
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
import android.os.Environment
import android.util.Log
import android.graphics.drawable.BitmapDrawable
import android.view.*
import com.google.firebase.database.ktx.database
import java.io.*


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
            }
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
        //Enable Option Menu
        setHasOptionsMenu(true)

        binding.progressBar.visibility = View.GONE

        //Read profile data
        binding.editTextTextPersonName2.setText(contactViewModel.profile.name)
        binding.editTextPhone2.setText(contactViewModel.profile.phone)

        if (!contactViewModel.profile.pic.isNullOrEmpty()) {
            try {
                val dir = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
                val file = File(dir, contactViewModel.profile.phone + ".jpg")

                filepath = file.absolutePath.toUri()
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                binding.imageViewPicture.setImageBitmap(bitmap)

            } catch (ex: IOException) {
                Log.d("Profile Picture", ex.message.toString())
            }
            //binding.textViewPath.text = contactViewModel.profile.pic
        } else {
            binding.imageViewPicture.setImageResource(R.drawable.profile)
        }

        binding.imageViewPicture.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.setType("image/*")
            getContent.launch(intent)
        }

        binding.buttonSaveProfile.setOnClickListener {
            //Validate input data
            if(binding.editTextTextPersonName2.text.isEmpty()){
                binding.editTextTextPersonName2.error = getString(R.string.error_value_required)
                return@setOnClickListener
            }

            if(binding.editTextPhone2.text.isEmpty()){
                binding.editTextPhone2.error = getString(R.string.error_value_required)
                return@setOnClickListener
            }

            contactViewModel.profile.name = binding.editTextTextPersonName2.text.toString()
            contactViewModel.profile.phone = binding.editTextPhone2.text.toString()

            //Save profile picture to the app local storage
            saveImage(context?.applicationContext!!, contactViewModel.profile.phone + ".jpg")

            //Update profile picture file path to local storage
            val dir = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
            contactViewModel.profile.pic = File(dir, contactViewModel.profile.phone + ".jpg").absolutePath

            //Save profile data to shared preference
            contactViewModel.savePreference(context?.applicationContext!!)

            //Save profile data to Firebase Realtime Database
            updateProfile(contactViewModel.profile)

            //Upload profile picture to Firebase Storage
            uploadPic(contactViewModel.profile)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        //Make Toolbar invisible
        menu.setGroupVisible(R.id.group_delete, false)
        menu.setGroupVisible(R.id.group_action, false)
        menu.setGroupVisible(R.id.group_db, false)
    }

    //Save a copy of profile picture to the app folder
    private fun saveImage(context: Context, fileName: String) {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

        if (!dir.exists()) {
            dir.mkdir()
        }
        val drawable = binding.imageViewPicture.drawable as BitmapDrawable
        val bitmap = drawable.bitmap
        val file = File(dir, fileName)

        val outputStream: OutputStream

        try {
            outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
            Toast.makeText(context, "Picture Saved", Toast.LENGTH_SHORT).show()
            outputStream.flush()
            outputStream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

    //Save profile settings to Firebase Real Database
    fun updateProfile(profile: Profile) {
        val firebaseDatabase = Firebase.database
        val myRef = firebaseDatabase.getReference("profile")

        myRef.child(profile.phone.toString()).child(ContactViewModel.PROFILE_NAME).setValue(profile.name)
        myRef.child(profile.phone.toString()).child(ContactViewModel.PROFILE_PHONE).setValue(profile.phone)
        //myRef.child(profile.phone.toString()).child(ContactViewModel.PROFILE_PIC).setValue(profile.pic)
    }

    //Upload profile picture to Firebase Storage
    private fun uploadPic(profile: Profile) {
        val myStorage = Firebase.storage("gs://my-contact-89b38.appspot.com")
        val myProfileImageRef = myStorage.reference.child("images").child(profile.phone.toString())

        binding.progressBar.visibility = View.VISIBLE

        val dir = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        val file = File(dir, contactViewModel.profile.phone + ".jpg").absoluteFile
        val filepath = file.toUri()

        myProfileImageRef.putFile(filepath)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "File Uploaded", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
            }
            .addOnProgressListener {
                val progress: Double = (100.0 * it.bytesTransferred) / it.totalByteCount
                binding.progressBar.progress = progress.toInt()
            }
    }

    private fun downloadPic() {
        val myStorage = Firebase.storage("gs://my-contact-89b38.appspot.com")
        val myProfileImageRef = myStorage.reference.child("images").child(contactViewModel.profile.phone.toString())

        val localFile = File.createTempFile("images", "jpg")
        binding.progressBar.visibility = View.VISIBLE

        myProfileImageRef.getFile(localFile)
            .addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                binding.imageViewPicture.setImageBitmap(bitmap)
                contactViewModel.profile.pic = localFile.absolutePath.toUri().toString()
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Picture downloaded", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
            .addOnProgressListener {
                val progress: Double = (100.0 * it.bytesTransferred) / it.totalByteCount
                binding.progressBar.progress = progress.toInt()
            }
    }
}