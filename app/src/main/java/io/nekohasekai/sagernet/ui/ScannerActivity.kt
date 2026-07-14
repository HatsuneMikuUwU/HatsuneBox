package io.nekohasekai.sagernet.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutManager
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.king.camera.scan.CameraScan
import com.king.camera.scan.DefaultCameraScan
import com.king.zxing.analyze.QRCodeAnalyzer
import com.king.zxing.util.CodeUtils
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.database.ProfileManager
import io.nekohasekai.sagernet.databinding.LayoutScannerBinding
import io.nekohasekai.sagernet.group.RawUpdater
import io.nekohasekai.sagernet.ktx.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class ScannerActivity : ThemedActivity() {

    lateinit var binding: LayoutScannerBinding
    lateinit var cameraScan: CameraScan<String>

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraScan.startCamera()
        } else {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= 25) getSystemService<ShortcutManager>()!!.reportShortcutUsed("scan")
        binding = LayoutScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_navigation_close)
        }

        initCameraScan()
        startCamera()
        binding.ivFlashlight.setOnClickListener { toggleTorchState() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.scanner_menu, menu)
        return true
    }

    val importCodeFile = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) {
        runOnDefaultDispatcher {
            try {
                it.forEachTry { uri ->
                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.decodeBitmap(
                            ImageDecoder.createSource(
                                contentResolver, uri
                            )
                        ) { decoder, _, _ ->
                            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                            decoder.isMutableRequired = true
                        }
                    } else {
                        @Suppress("DEPRECATION") MediaStore.Images.Media.getBitmap(
                            contentResolver, uri
                        )
                    }
                    val resultText = CodeUtils.parseQRCode(bitmap)
                    onMainDispatcher {
                        onScanResultCallback(resultText, true)
                    }
                }
                finish()
            } catch (e: Exception) {
                Logs.w(e)
                onMainDispatcher {
                    Toast.makeText(app, e.readableMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.action_import_file) {
            startFilesForResult(importCodeFile, "image/*")
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    var finished = AtomicBoolean(false)
    var importedN = AtomicInteger(0)

    fun onScanResultCallback(text: String?, multi: Boolean): Boolean {
        if (!multi && finished.getAndSet(true)) return true
        if (!multi) finish()
        runOnDefaultDispatcher {
            try {
                val actualText = text ?: throw Exception("QR code not found")
                val results = RawUpdater.parseRaw(actualText)
                if (!results.isNullOrEmpty()) {
                    val currentGroupId = DataStore.selectedGroupForImport()
                    if (DataStore.selectedGroup != currentGroupId) {
                        DataStore.selectedGroup = currentGroupId
                    }

                    for (profile in results) {
                        ProfileManager.createProfile(currentGroupId, profile)
                        importedN.addAndGet(1)
                    }
                } else {
                    onMainDispatcher {
                        Toast.makeText(app, R.string.action_import_err, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: SubscriptionFoundException) {
                startActivity(Intent(this@ScannerActivity, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    data = e.link.toUri()
                })
            } catch (e: Throwable) {
                Logs.w(e)
                onMainDispatcher {
                    var errText = getString(R.string.action_import_err)
                    errText += "\n" + e.readableMessage
                    Toast.makeText(app, errText, Toast.LENGTH_SHORT).show()
                }
            }
        }
        return true
    }

    fun initCameraScan() {
        cameraScan = DefaultCameraScan(this, binding.previewView, binding.viewfinderView)
        cameraScan.setAnalyzer(QRCodeAnalyzer())
        cameraScan.setOnScanResultCallback { result ->
            onScanResultCallback(result.result, false)
            true
        }
    }

    fun startCamera() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            cameraScan.startCamera()
        } else {
            Logs.d("Requesting camera permission")
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun releaseCamera() {
        cameraScan.release()
    }

    protected fun toggleTorchState() {
        val isTorch = cameraScan.isTorchEnabled
        cameraScan.enableTorch(!isTorch)
        binding.ivFlashlight.isSelected = !isTorch
    }

    override fun onDestroy() {
        releaseCamera()
        super.onDestroy()
        if (importedN.get() > 0) {
            var text = getString(R.string.action_import_msg)
            text += "\n" + importedN.get() + " profile(s)"
            Toast.makeText(app, text, Toast.LENGTH_LONG).show()
        }
    }
}
