package my.tarc.mycontact

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import my.tarc.mycontact.databinding.FragmentSecondBinding
import java.util.*

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val myViewModel: ContactViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)

        //Allow fragment to respond to menu item selection
        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Change view edit attribute based on editMode
        if(myViewModel.editMode){
            binding.editTextTextPersonName.setText(myViewModel.selectedContact.name)
            binding.editTextPhone.setText(myViewModel.selectedContact.phone)
            binding.editTextPhone.isEnabled = !myViewModel.editMode
        }

        binding.buttonSave.setOnClickListener {
            binding.apply {
                if(binding.editTextTextPersonName.text.isBlank()){
                    binding.editTextTextPersonName.error = getString(R.string.error_value_required)
                    return@setOnClickListener
                }
                if(binding.editTextPhone.text.isBlank()){
                    binding.editTextPhone.error = getString(R.string.error_value_required)
                    return@setOnClickListener
                }
                val newContact = Contact(editTextTextPersonName.text.toString(), editTextPhone.text.toString())

                if(myViewModel.editMode){
                    myViewModel.updateContact(newContact)
                }else{
                    myViewModel.addContact(newContact)
                }
            }
            Toast.makeText(context, "Record saved", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }

        binding.buttonCancel.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }

        //Set focus to editText
        binding.editTextTextPersonName.requestFocus()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        //Make Toolbar invisible
        menu.setGroupVisible(R.id.group_delete, true)
        menu.setGroupVisible(R.id.group_action, false)
        menu.setGroupVisible(R.id.group_db, false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.action_delete ->  {
                val builder = AlertDialog.Builder(context)
                builder.apply {
                    setMessage("Delete contact?")
                    setPositiveButton("Delete", DialogInterface.OnClickListener { dialog, which ->
                        myViewModel.deleteContact(myViewModel.selectedContact)
                        Toast.makeText(context, "Contact deleted", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
                    })
                    setNegativeButton("Cancel", null)
                    show()
                }
                true
            }else -> super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}