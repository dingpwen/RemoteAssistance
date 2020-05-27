package com.mshare.remote.assistance.friend

import android.content.Context
import android.content.Intent
import android.util.Log
import com.mshare.remote.assistance.Constants
import com.mshare.remote.assistance.util.OkHttpUtil
import com.wen.app.update.ApkUtils
import com.wen.app.update.UpdateVersionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
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

    private var result: String? = null
    //app update
    private var apkVersion: Long = 0
    private var apkType = 0
    private var apkChecksum: String? = null
    private var patchChecksum: String? = null

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

    override fun addUser(context: Context, token: String, category:String, needSaved:Boolean) {
        val map = HashMap<String, String>()
        map[Constants.WS_MSG_TOKEN_SELF] = token
        map[Constants.USER_TOKEN_CATEGORY] = category
        mModel.addUser(map,object:Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.e("wenpd", "IOException", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body!!.string()
                try {
                    val obj = JSONObject(result)
                    val status = obj.getInt("status")
                    if(status == 200 && needSaved) {
                        Constants.saveUserToken(context, token)
                    }
                }catch (e:JSONException) {
                    e.printStackTrace()
                }
            }
        }, 2)
    }

    override fun checkAppVersion(context: Context): Boolean {
        if(!Constants.isWifiConnected(context)){
            return true
        }
        val curVersion = ApkUtils.getVersionCode(context, context.packageName)
        val map = HashMap<String, String>()
        map["project"] = Constants.PROJECT
        result = null
        runBlocking {
            result = GlobalScope.async(Dispatchers.IO) {
                return@async OkHttpUtil.baseSyncGet(Constants.getVersionUrl(), map)
            }.await()
        }
        if(result == null) {
            return true
        }
        try{
            val obj = JSONObject(result as String)
            if(obj.getInt("status") != 200) {
                return true
            }
            apkVersion = obj.getLong("version")
            if(apkVersion > curVersion) {
                apkType = obj.getInt("type")
                apkChecksum = obj.getString("checksum1")
                patchChecksum = obj.getString("checksum2")
                return false
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return true
    }

    override fun startUpdateVersionService(context: Context) {
        val intent = Intent(context, UpdateVersionService::class.java)
        intent.putExtra("type", apkType)
        intent.putExtra("version", apkVersion)
        intent.putExtra("checksum1", apkChecksum)
        intent.putExtra("checksum2", patchChecksum)
        val pendingIntent = (context as FriendListActivity).createPendingResult(Constants.REQUEST_CODE_VERSION, Intent(), 0)
        intent.putExtra("pendingIntent", pendingIntent)
        context.startService(intent)
    }
}