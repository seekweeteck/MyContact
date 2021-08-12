package my.tarc.mycontact

import android.content.pm.PackageInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import my.tarc.mycontact.databinding.ActivitySecondBinding


class SecondActivity : AppCompatActivity() {
   private lateinit var binding : ActivitySecondBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonClose.setOnClickListener {

            val pInfo: PackageInfo =
                this.getPackageManager().getPackageInfo(this.getPackageName(), 0)
            val version = pInfo.versionName

            binding.textViewVersion.text = version.toString()
            finish()
        }
    }
}