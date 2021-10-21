package medrawd.`is`.awesome.morse

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import medrawd.`is`.awesome.morse.decoder.DataConsumer

class MainActivityViewModel: ViewModel() {
    var recording = MutableLiveData(false)
    lateinit var dataConsumer: DataConsumer
    var decodedTextHolder = MutableLiveData(MessagesList())
}