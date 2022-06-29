package com.arash.altafi.alertdialogfirebase

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.arash.altafi.alertdialogfirebase.databinding.ActivityMainBinding
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var remoteConfig: FirebaseRemoteConfig? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        binding.apply {
            tvVersion.text = "Current Version Code: ${getVersionCode()}"

            val defaultsRate: HashMap<String, Any> = HashMap()
            defaultsRate["new_version_code"] = getVersionCode().toString()

            remoteConfig = FirebaseRemoteConfig.getInstance()
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(10)
                .build()

            remoteConfig?.setConfigSettingsAsync(configSettings)
            remoteConfig?.setDefaultsAsync(defaultsRate)

            remoteConfig?.fetchAndActivate()?.addOnCompleteListener { task ->
                Log.i("test123321", "task = $task")
                if (task.isSuccessful) {
                    val newVersionCode: String = remoteConfig?.getString("new_version_code").toString()
                    if (newVersionCode.toInt() > getVersionCode()) showTheDialog("com.facebook.lite", newVersionCode)
                } else Log.i("test123321", "mFirebaseRemoteConfig.fetchAndActivate() NOT Successful")
            }
        }
    }

    private fun showTheDialog(appPackageName: String, versionFromRemoteConfig: String) {
        val dialog: AlertDialog = AlertDialog.Builder(this)
            .setTitle("Update")
            .setMessage("This version is absolute, please update to version: $versionFromRemoteConfig")
            .setPositiveButton("UPDATE", null)
            .show()
        dialog.setCancelable(false)
        val positiveButton: Button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener {
            try {
                startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName"))
                )
            } catch (activityNotFoundException: ActivityNotFoundException) {
                startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName"))
                )
            }
        }
    }

    private var pInfo: PackageInfo? = null
    private fun getVersionCode(): Int {
        try {
            pInfo = packageManager.getPackageInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.i("test123321", "NameNotFoundException: " + e.message)
        }
        return pInfo!!.versionCode
    }

}