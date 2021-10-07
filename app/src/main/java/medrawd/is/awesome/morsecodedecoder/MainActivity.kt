package medrawd.`is`.awesome.morsecodedecoder

import android.Manifest
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import medrawd.`is`.awesome.CheckPermissionResult
import medrawd.`is`.awesome.PermissionHandler
import medrawd.`is`.awesome.TAG
import medrawd.`is`.awesome.morsecodedecoder.databinding.ActivityMainBinding
import medrawd.`is`.awesome.multimon.AudioInShort
import java.util.*

class MainActivity : AppCompatActivity(), AudioInShort.MaxValueListener, DataConsumer.DecodedTextListener {

    private var lastLetterRecievedAt: Long = 0
    private lateinit var binding: ActivityMainBinding


    val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            startDataConsumer();
        } else {
            // displayAlert(permissionRequestAlert)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: MainActivityViewModel by viewModels()

        Log.d(TAG, "onCreate: view mode ="+viewModel)
        Log.d(TAG, "onCreate: recording ="+viewModel.recording.value)
        Log.d(TAG, "onCreate: decoded ="+viewModel.decodedText.value)
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.fab.setOnClickListener { view ->
            if(viewModel.recording.value == true){
                stopDataConsumer()
            } else {
                startListeningOrGetPermission()
            }
        }

        binding.content.pBar.setMax(Short.MAX_VALUE.toInt())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()

    }

    private fun startListeningOrGetPermission() {
        PermissionHandler.checkPermission(this,
                Manifest.permission.RECORD_AUDIO) { result ->
            when (result) {
                CheckPermissionResult.PermissionGranted -> {
                    startDataConsumer()
                }
                CheckPermissionResult.PermissionDisabled -> {
                    // displayAlert(noPermissionAlert)
                }
                CheckPermissionResult.PermissionAsk -> {
                    requestRecordAudioPermissions()
                }
                CheckPermissionResult.PermissionPreviouslyDenied -> {
                    // displayAlert(permissionRequestAlert)
                }
            }
        }
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
        val hsvBlue = floatArrayOf(0f,0f,0f)
        val hsvRed = floatArrayOf(0f,0f,0f)
        Color.colorToHSV(Color.BLUE, hsvBlue)
        Color.colorToHSV(Color.RED, hsvRed)

        val hue: Float = hsvBlue[0] + (hsvRed[0] - hsvBlue[0]) * currmax / binding.content.pBar.max

        val hsvColor = floatArrayOf(hue,1f,1f)
        binding.content.pBar.progressTintList = ColorStateList.valueOf(Color.HSVToColor(hsvColor))
        binding.content.pBar.setProgress(currmax.toInt())
    }

    override fun onNewText(letter: Char) {
        val prevLetterRecievedAt = lastLetterRecievedAt
        lastLetterRecievedAt = Date().time
        val addNewLine = (prevLetterRecievedAt == 0L || (prevLetterRecievedAt+1500<lastLetterRecievedAt))
        val viewModel: MainActivityViewModel by viewModels()
        viewModel.decodedText.postValue((if (null != viewModel.decodedText.value)  viewModel.decodedText.value else "") + (if(addNewLine) '\n' else "") +letter)
    }
}