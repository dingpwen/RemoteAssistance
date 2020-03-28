package com.mshare.remote.assistance.friend

import com.mshare.remote.assistance.R
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mshare.remote.assistance.Constants
import com.mshare.remote.assistance.MainActivity

class GridAdapter(context: Context): RecyclerView.Adapter<GridAdapter.GridViewHolder>() {
    private val mContext = context
    private var mFriendList:MutableList<FriendInfo> = ArrayList()

    fun setData(friendList:MutableList<FriendInfo>) {
        mFriendList = friendList
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val view: View =
            LayoutInflater.from(mContext).inflate(R.layout.grid_friend_item, parent, false)
        return GridViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mFriendList.size
    }

    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
        val friend = mFriendList[position]
        if(friend.name.isEmpty()) {
            holder.title.text = friend.user_token
        } else {
            holder.title.text = friend.name
        }
        if(friend.imageUrl.isEmpty() || friend.imageUrl == "none") {
            //holder.image.setImageResource(R.drawable.default_img)
            val imgUrl = Constants.getImageUrl(mContext, "default.jpg")
            Glide.with(mContext).load(imgUrl).placeholder(R.drawable.default_img).into(holder.image)
        } else {
            val imgUrl = Constants.getImageUrl(mContext, friend.imageUrl)
            Glide.with(mContext).load(imgUrl).placeholder(R.drawable.default_img).into(holder.image)
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(mContext,MainActivity::class.java);
            intent.putExtra("token", friend.user_token)
            mContext.startActivity(intent)
        }
    }

    class GridViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        val title:TextView = itemView.findViewById(R.id.title)
        val image:ImageView = itemView.findViewById(R.id.header_img)
    }
}