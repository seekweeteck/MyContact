package my.tarc.mycontact

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import my.tarc.mycontact.databinding.FragmentAboutBinding


class AboutFragment : Fragment() {
    private var _binding: FragmentAboutBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAboutBinding.inflate(inflater, container, false)

        setHasOptionsMenu(true)

        binding.textViewURL.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.tarc.edu.my"))
            if(intent.resolveActivity(context?.packageManager!!) != null){
                startActivity(intent)
            }
        }

        binding.buttonPhone.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:60341450123"))
            startActivity(intent)
        }

        binding.buttonEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_EMAIL, "admin@tarc.edu.my")
                putExtra(Intent.EXTRA_SUBJECT, "Email subject")
                putExtra(Intent.EXTRA_TEXT, "Email message text")
            }
            startActivity(intent)
        }

        binding.buttonMap.setOnClickListener {
            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:3.216128, 101.729015?z=14"))
            if(mapIntent.resolveActivity(context?.packageManager!!)!= null){
                startActivity(mapIntent)
            }
        }

        binding.buttonAboutApp.setOnClickListener {
            val intent = Intent(context, SecondActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_profile).isVisible = false
        menu.findItem(R.id.action_about).isVisible = false
        menu.findItem(R.id.action_settings).isVisible = false
    }
}