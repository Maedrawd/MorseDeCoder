package medrawd.`is`.awesome.morsecodedecoder

import android.Manifest
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import medrawd.`is`.awesome.CheckPermissionResult
import medrawd.`is`.awesome.PermissionHandler
import medrawd.`is`.awesome.TAG
import medrawd.`is`.awesome.morsecodedecoder.databinding.ActivityMainBinding
import medrawd.`is`.awesome.multimon.AudioInShort
import java.util.*

class MainActivity : AppCompatActivity(), AudioInShort.MaxValueListener,
    DataConsumer.DecodedTextListener {

    private var lastLetterRecievedAt: Long = 0
    private lateinit var binding: ActivityMainBinding
    private var dialog: AlertDialog? = null


    val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startDataConsumer();
            } else {
                displayPermissionRequestAlert()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: MainActivityViewModel by viewModels()

        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.fab.setOnClickListener { view ->
            if (viewModel.recording.value == true) {
                stopDataConsumer()
            } else {
                startListeningOrGetPermission()
            }
        }

        binding.content.pBar.setMax(Short.MAX_VALUE.toInt())
    }

    override fun onStop() {
        dialog?.dismiss()
        dialog = null
        super.onStop()

    }

    private fun startListeningOrGetPermission() {
        PermissionHandler.checkPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) { result ->
            when (result) {
                CheckPermissionResult.PermissionGranted -> {
                    startDataConsumer()
                }
                CheckPermissionResult.PermissionDisabled -> {
                    requestRecordAudioPermissions()
                }
                CheckPermissionResult.PermissionAsk -> {
                    requestRecordAudioPermissions()
                }
                CheckPermissionResult.PermissionPreviouslyDenied -> {
                    requestRecordAudioPermissions()
                }
            }
        }
    }

    private fun displayNoPermissionAlert() {
        dialog?.dismiss()
        dialog = null
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setPositiveButton(
                android.R.string.ok
            ) { dialog, id ->
            }
        }
        builder.setMessage(getString(R.string.blocked_permission))
        builder.setOnDismissListener { finish() }
        dialog = builder.create()
        dialog?.show()
    }

    private fun displayPermissionRequestAlert() {
        dialog?.dismiss()
        dialog = null
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setPositiveButton(
                android.R.string.ok
            ) { dialog, id ->
            }
        }
        builder.setMessage(getString(R.string.denied_permission))
        builder.setOnDismissListener { finish() }
        dialog = builder.create()
        dialog?.show()
    }

    private fun requestRecordAudioPermissions() {
        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    private fun startDataConsumer() {
        Log.d(TAG, "startDataConsumer: starting")
        val viewModel: MainActivityViewModel by viewModels()
        viewModel.dataConsumer = DataConsumer();
        viewModel.dataConsumer.start(this, this);
        viewModel.recording.value = true
        Log.d(TAG, "startDataConsumer: started")
    }

    private fun stopDataConsumer() {
        Log.d(TAG, "stopDataConsumer: ")
        val viewModel: MainActivityViewModel by viewModels()
        viewModel.dataConsumer.stop()
        viewModel.recording.value = false
        binding.content.pBar.setProgress(0)
    }

    override fun onMaxValueChanged(currmax: Short) {
        val hsvBlue = floatArrayOf(0f, 0f, 0f)
        val hsvRed = floatArrayOf(0f, 0f, 0f)
        Color.colorToHSV(Color.BLUE, hsvBlue)
        Color.colorToHSV(Color.RED, hsvRed)

        val hue: Float = hsvBlue[0] + (hsvRed[0] - hsvBlue[0]) * currmax / binding.content.pBar.max

        val hsvColor = floatArrayOf(hue, 1f, 1f)
        binding.content.pBar.progressTintList = ColorStateList.valueOf(Color.HSVToColor(hsvColor))
        binding.content.pBar.setProgress(currmax.toInt())
    }

    override fun onNewText(letter: Char) {
        val prevLetterRecievedAt = lastLetterRecievedAt
        lastLetterRecievedAt = Date().time
        val addNewLine =
            (prevLetterRecievedAt == 0L || (prevLetterRecievedAt + 1500 < lastLetterRecievedAt))
        val viewModel: MainActivityViewModel by viewModels()
        viewModel.decodedText.postValue((if (null != viewModel.decodedText.value) viewModel.decodedText.value else "") + (if (addNewLine) '\n' else "") + letter)
    }
}