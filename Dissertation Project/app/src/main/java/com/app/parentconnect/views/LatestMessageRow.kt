package com.app.parentconnect.views

import com.app.parentconnect.R
import com.app.parentconnect.models.ChatMessage
import com.app.parentconnect.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessageRow(val chatMessage: ChatMessage): Item<GroupieViewHolder>(){

    var chatPartnerUser: User? = null
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.messagetextview_latest_text_view.text = chatMessage.text

        val chatPartnerId: String
        if (chatMessage.fromId == FirebaseAuth.getInstance().uid){
            chatPartnerId = chatMessage.toId
        } else {
            chatPartnerId = chatMessage.fromId
        }

        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                chatPartnerUser = p0.getValue(User::class.java)
                viewHolder.itemView.username_message_row.text = chatPartnerUser?.username

                val targetImageView = viewHolder.itemView.imageView_latest_message
                Picasso.get().load(chatPartnerUser?.profileImageUrl).into(targetImageView)
            }
            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }
    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }
}