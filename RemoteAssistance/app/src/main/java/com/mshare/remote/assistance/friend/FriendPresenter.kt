package com.mshare.remote.assistance.friend

import android.content.Context
import android.util.Log
import com.mshare.remote.assistance.Constants
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.lang.ref.WeakReference

class FriendPresenter:Contact.IPresenter {
    private lateinit var mViewRef: WeakReference<Contact.IView>
    private lateinit var mModel:Contact.IModel
    override fun attachView(view: Contact.IView) {
        mViewRef = WeakReference(view)
        mModel = FriendModel()
    }

    override fun detachView() {
        mViewRef.clear()
    }

    override fun loadData(token:String) {
        val map = HashMap<String, String>()
        map[Constants.WS_MSG_TOKEN_SELF] = token
        val friendList:MutableList<FriendInfo> = ArrayList()
        mModel.loadData(map, object:Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.e("wenpd", "onFailure:", e)
                mViewRef.get()?.onError(Constants.ERROR_TYPE_NET)
                mViewRef.get()?.setData(friendList)
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body!!.string()
                try {
                    val obj = JSONObject(result)
                    val status = obj.getInt("status")
                    if(status == 200) {
                        val friends = obj.getJSONArray("friends")
                        for(i in 0 until friends.length()) {
                            val friend:JSONObject = friends.get(i) as JSONObject
                            val name = friend.getString("name")
                            val userToken = friend.getString("user_token")
                            val imgUrl = friend.getString("imageUrl")
                            friendList.add(FriendInfo(userToken, name, imgUrl))
                        }
                    }
                } catch (e:JSONException) {
                    mViewRef.get()?.onError(Constants.ERROR_TYPE_JSON)
                    e.printStackTrace()
                } finally {
                    mViewRef.get()?.setData(friendList)
                }
            }
        })

    }

    override fun addOrRemoveFriend(token: String, friendToken: String, type:Int) {
        val map = HashMap<String, String>()
        map[Constants.WS_MSG_TOKEN_SELF] = token
        map[Constants.WS_MSG_TOKEN_GOAL] = friendToken
        mModel.addOrRemoveFriend(map, object:Callback{
            override fun onFailure(call: Call, e: IOException) {
                mViewRef.get()?.onError(Constants.ERROR_TYPE_NET)
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body!!.string()
                try {
                    val obj = JSONObject(result)
                    val status = obj.getInt("status")
                    if(status == 200) {
                        loadData(token)
                    } else {
                        if(type == 1) {
                            mViewRef.get()?.onError(Constants.ERROR_TYPE_ADD)
                        } else {
                            mViewRef.get()?.onError(Constants.ERROR_TYPE_DEL)
                        }
                    }
                }catch (e:JSONException) {
                    mViewRef.get()?.onError(Constants.ERROR_TYPE_JSON)
                    e.printStackTrace()
                }
            }

        }, type)
    }

    override fun addUser(context: Context, token: String) {
        val map = HashMap<String, String>()
        map[Constants.WS_MSG_TOKEN_SELF] = token
        map[Constants.USER_TOKEN_CATEGORY] = "2"
        mModel.addUser(map,object:Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.e("wenpd", "IOException", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body!!.string()
                try {
                    val obj = JSONObject(result)
                    val status = obj.getInt("status")
                    if(status == 200) {
                        Constants.saveUserToken(context, token)
                    }
                }catch (e:JSONException) {
                    e.printStackTrace()
                }
            }
        }, 2)
    }
}