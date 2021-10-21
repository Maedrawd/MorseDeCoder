package medrawd.`is`.awesome.morse.decoder

import medrawd.`is`.awesome.multimon.AudioBufferProcessor
import medrawd.`is`.awesome.multimon.AudioInShort
import medrawd.`is`.awesome.multimon.DemodConfig
import medrawd.`is`.awesome.multimon.PacketCallback

class DataConsumer {

    private lateinit var audioBufferProcessor: AudioBufferProcessor

    fun start(maxValueListener: AudioInShort.MaxValueListener, decodedTextListener: DecodedTextListener){
        audioBufferProcessor = AudioBufferProcessor(object : PacketCallback {
            override fun received(packet: ByteArray) {
            }

            override fun received(sign: Char) {
                decodedTextListener.onNewText(sign)
            }
        }, maxValueListener, DemodConfig.Demod.MORSE)
        audioBufferProcessor.start()
        //audioBufferProcessor.startRecording()
    }

    fun stop(){
        audioBufferProcessor.stopRecording();
    }

    interface DecodedTextListener {
        fun onNewText(letter: Char)
    }
}