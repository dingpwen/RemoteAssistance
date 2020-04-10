package com.mshare.remote.assistance.friend
import android.content.Context
import okhttp3.Callback

interface Contact {
    interface IView{
        fun setData(friendList:MutableList<FriendInfo>)
        fun onError(type:Int)
    }

    interface IModel{
        fun loadData(params:Map<*, *>, callback:Callback)
        fun addOrRemoveFriend(params:Map<*, *>, callback:Callback, type:Int)
        fun addUser(params:Map<*, *>, callback:Callback, type:Int)
    }

    interface IPresenter{
        fun attachView(view: IView)
        fun detachView()
        fun loadData(token:String)
        fun addOrRemoveFriend(token:String, friendToken:String, type:Int)
        fun addUser(context: Context, token:String, category:String, needSaved:Boolean)
    }
}