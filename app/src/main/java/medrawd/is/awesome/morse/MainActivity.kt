package medrawd.`is`.awesome.morse

import android.Manifest
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import medrawd.`is`.awesome.CheckPermissionResult
import medrawd.`is`.awesome.PermissionHandler
import medrawd.`is`.awesome.TAG
import medrawd.`is`.awesome.morse.databinding.ActivityMainBinding
import medrawd.`is`.awesome.morse.decoder.DataConsumer
import medrawd.`is`.awesome.morse.encoder.MorseEncoder
import medrawd.`is`.awesome.multimon.AudioInShort
import androidx.recyclerview.widget.LinearLayoutManager


class MainActivity : AppCompatActivity(), AudioInShort.MaxValueListener,
    DataConsumer.DecodedTextListener {

    private lateinit var binding: ActivityMainBinding
    private var dialog: AlertDialog? = null
    val morseEncoder = MorseEncoder()


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

        binding.content.recyclerViewMessages.layoutManager = LinearLayoutManager(this)
        binding.content.recyclerViewMessages.adapter = MessagesRecyclerViewAdapter(this)
        viewModel.decodedTextHolder.observe(this, Observer { list ->
            Log.d(TAG, "observer called on decodedTextHolder")
            (binding.content.recyclerViewMessages.adapter as MessagesRecyclerViewAdapter).messages.merge(list)// TODO move to adapter and add change type detection
            (binding.content.recyclerViewMessages.adapter as MessagesRecyclerViewAdapter).notifyDataSetChanged()//TODO optimize as above
        })

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

        binding.content.pBar.max = Short.MAX_VALUE.toInt()
        binding.content.sendButton.setOnClickListener {
            val message = binding.content.messageEditText.text
            morseEncoder.post(message)
            binding.content.messageEditText.setText("")
            viewModel.decodedTextHolder.value?.appendSent(message)
            viewModel.decodedTextHolder.value = viewModel.decodedTextHolder.value
        }
    }

    override fun onStop() {
        dialog?.dismiss()
        dialog = null
        super.onStop();
    }

    override fun onDestroy() {
        val viewModel: MainActivityViewModel by viewModels()
        if(viewModel.recording.value == true){
            stopDataConsumer()
        }
        super.onDestroy()
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
        val viewModel: MainActivityViewModel by viewModels()

        viewModel.decodedTextHolder.value?.appendReceived(""+letter)
        viewModel.decodedTextHolder.postValue(viewModel.decodedTextHolder.value)
    }
}