package medrawd.`is`.awesome.morse

import android.util.Log
import medrawd.`is`.awesome.TAG
import java.util.*

class MessagesList: LinkedList<Message>() {
    private var lastLetterRecievedAt: Long = 0

    fun appendReceived(text:CharSequence){
        Log.d(TAG, "appendReceived")
        if(size >0 && last?.recieved == true){
            val prevLetterRecievedAt = lastLetterRecievedAt
            lastLetterRecievedAt = Date().time
            val addNewLine = (prevLetterRecievedAt == 0L || (prevLetterRecievedAt + 1500 < lastLetterRecievedAt))
            if(addNewLine){
                last.append("\n")
            }
            last.append(text)
        } else {
            val message = Message(true)
            message.append(text)
            append(message)
        }
    }
    fun appendSent(text:CharSequence){
        Log.d(TAG, "appendSent")
        if(size >0 && last?.recieved == false){
            last.append("\n").append(text)
        } else {
            val message = Message(false)
            message.append(text)
            append(message)
        }
    }
    private fun append(message:Message){
        Log.d(TAG, "append")
        if(size >0) {
            last?.finalize()
        }
        add(message)
        Log.d(TAG, "append post "+ size)
    }

    fun merge(list: MessagesList?) {
        Log.d(TAG, "merging list with size "+ size + " and list with size "+ list?.size)
        if(size == 0){
            list?.let { addAll(it) }
        } else if(size == list?.size) {
        } else if(size<list?.size!!) {
            for  (i in size-1 until list.size){
                append(list[i])
            }
        }
        Log.d(TAG, "merge post" + size)
    }
}