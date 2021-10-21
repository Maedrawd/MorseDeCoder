package medrawd.`is`.awesome.morse

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import android.content.Context

import android.view.LayoutInflater

class MessagesRecyclerViewAdapter(private val context: Context): RecyclerView.Adapter<MessagesRecyclerViewAdapter.MessageRecyclerViewHolder>(){
    var messages = MessagesList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageRecyclerViewHolder {
        val rootView: View = LayoutInflater.from(context).inflate(R.layout.message_layout, parent, false)
        return MessageRecyclerViewHolder(rootView)
    }

    override fun onBindViewHolder(holder: MessageRecyclerViewHolder, position: Int) {
        holder.messageTextView.text = messages[position].getText()
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    class MessageRecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val messageTextView:TextView = itemView.findViewById(R.id.message_text_view)
    }
}