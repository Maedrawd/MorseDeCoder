package medrawd.`is`.awesome.morse

import java.lang.IllegalStateException

class Message(val recieved: Boolean) {
    var builder = StringBuilder()
    lateinit var message:String

    fun append(text:CharSequence): Message{
        if(!this::message.isInitialized){
            builder.append(text)
            return this
        } else {
            throw IllegalStateException("message already finalized")
        }
    }

    fun finalize(){
        message = builder.toString()
        builder.clear()
    }

    fun getText():String{
        if(this::message.isInitialized){
            return message
        } else {
            return builder.toString()
        }
    }
}