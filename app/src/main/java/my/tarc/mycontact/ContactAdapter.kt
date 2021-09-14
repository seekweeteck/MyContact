package my.tarc.mycontact

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView


class ContactAdapter(private val cellClickListener: CellClickListener) :
    RecyclerView.Adapter<ContactAdapter.ViewHolder>() {
    //Cached copy of contacts
    private var contactList = emptyList<Contact>()

    interface CellClickListener {
        fun onCellClickListener(data: Contact, mode: Int)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewName: TextView = view.findViewById(R.id.textViewContactName)
        val textViewContact: TextView = view.findViewById(R.id.textViewContact)
        val imageViewEdit: ImageView = view.findViewById(R.id.imageViewEdit)
        val imageViewDelete: ImageView = view.findViewById(R.id.imageViewDelete)
    }

    internal fun setContact(contact: List<Contact>) {
        this.contactList = contact
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //Create a new view, which define the UI of the list item
        val view = LayoutInflater.from(parent.context).inflate(R.layout.record, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //Get element from the dataset at this position and replace the contents of the view with that element
        holder.textViewName.text = contactList[position].name
        holder.textViewContact.text = contactList[position].phone
        holder.imageViewEdit.setOnClickListener {
            val data = contactList[position]
            cellClickListener.onCellClickListener(data, 1)
        }
        holder.imageViewDelete.setOnClickListener {
            val data = contactList[position]
            cellClickListener.onCellClickListener(data, 2)
        }
        /*holder.itemView.setOnClickListener {
            val data = contactList[position]
            cellClickListener.onCellClickListener(data)
        }*/
    }

    override fun getItemCount(): Int {
        return contactList.size
    }
}