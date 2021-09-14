package my.tarc.mycontact

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import my.tarc.mycontact.databinding.FragmentFirstBinding
import java.util.*

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment(), ContactAdapter.CellClickListener {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val myViewModel: ContactViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Enable Option Menu
        setHasOptionsMenu(true)

        binding.fab.setOnClickListener {
            myViewModel.editMode = false
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        val adapter = ContactAdapter(this)

        myViewModel.contactList.observe(viewLifecycleOwner,
            Observer {
                if(it.isEmpty()){
                    val calendar = Calendar.getInstance()
                    binding.textViewCount.text = "No record ${calendar.get(Calendar.HOUR)}"
                    //Toast.makeText(context, "No record found", Toast.LENGTH_SHORT).show()
                }else{
                    binding.textViewCount.isVisible = false
                    adapter.setContact(it)
                }
            })

        binding.listViewContact.adapter = adapter
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.setGroupVisible(R.id.group_delete, false)
        menu.findItem(R.id.action_profile).isVisible = true
        menu.findItem(R.id.action_settings).isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_sync -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d("First Fragment", "onDestroy")
    }

    override fun onCellClickListener(data: Contact, mode: Int) {
        if(mode == 1){
            myViewModel.editMode = true
            myViewModel.selectedContact = data
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }else if(mode == 2){
            val builder = AlertDialog.Builder(context)
            builder.apply {
                setMessage("Delete contact ${data.phone} ?")
                setPositiveButton("Delete", DialogInterface.OnClickListener { dialog, which ->
                    myViewModel.deleteContact(data)
                    Toast.makeText(context, "Contact deleted", Toast.LENGTH_SHORT).show()
                })
                setNegativeButton("Cancel", null)
                show()
            }
        }
    }

}