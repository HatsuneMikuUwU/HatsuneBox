package io.nekohasekai.sagernet.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.nekohasekai.sagernet.R
import moe.matsuri.nb4a.utils.SendLog

class BlankActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // process crash log
        val title = intent?.getStringExtra("sendLog")
        if (title != null) {
            showCrashDialog(title)
        } else {
            finish()
        }
    }

    private fun showCrashDialog(title: String) {
        // Building the log (logcat dump + neko.log) can take a moment, but this activity
        // has nothing else to show, so it's fine to do it on the main thread here.
        val file = SendLog.buildLogFile(this, title)
        val logText = file.readText()

        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(logText)
            .setPositiveButton(R.string.action_copy) { _, _ ->
                copyToClipboard(logText)
                cleanupAndFinish()
            }
            .setNegativeButton(R.string.abc_shareactionprovider_share_with) { _, _ ->
                SendLog.shareLogFile(this, file)
                cleanupAndFinish()
            }
            .setNeutralButton(android.R.string.cancel) { _, _ ->
                cleanupAndFinish()
            }
            .setOnDismissListener {
                cleanupAndFinish()
            }
            .setCancelable(false)
            .show()
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.app_name), text))
        Toast.makeText(this, R.string.copy_success, Toast.LENGTH_SHORT).show()
    }

    private fun cleanupAndFinish() {
        if (!isFinishing) finish()
    }

}
