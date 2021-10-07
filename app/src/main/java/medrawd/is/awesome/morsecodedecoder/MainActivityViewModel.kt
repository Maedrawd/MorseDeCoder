package medrawd.`is`.awesome.morsecodedecoder

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel: ViewModel() {
    var decodedText = MutableLiveData("")
    var recording = MutableLiveData(false)
    lateinit var dataConsumer: DataConsumer
}