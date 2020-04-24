package com.mshare.remote.assistance.ui.login

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mshare.remote.assistance.Constants
import com.mshare.remote.assistance.ui.login.data.LoginResult
import com.mshare.remote.assistance.ui.login.data.Result
import com.mshare.remote.assistance.ui.login.data.User
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception

class LoginViewModel: ViewModel() {
    var loginMode: MutableLiveData<Int> = MutableLiveData()
    var loginResult: MutableLiveData<LoginResult> = MutableLiveData()
    val loginModel = LoginModel()

    init{
        loginMode.value = 1
    }

    fun login(token:String, number:String, password:String) {
        val map = HashMap<String, String>()
        map[Constants.WS_MSG_TOKEN_SELF] = token
        map["number"] = number
        map["password"] = Constants.encodeString(password)
        loginModel.login(map, object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                setResultError(Constants.ERROR_TYPE_NET, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body!!.string()
                try {
                    val obj = JSONObject(result)
                    val status = obj.getInt("status")
                    if(status == 200) {
                        val user = obj.getJSONObject("user")
                        val name = user.getString("name")
                        val num = user.getString("number")
                        val userToken = user.getString("user_token")
                        val imgUrl = user.getString("imageUrl")
                        setResultSuccess(User(userToken, name, num, imgUrl))
                    } else {
                        setResultError(Constants.ERROR_TYPE_COMM, Exception("login fail"))
                    }
                }catch (e: JSONException) {
                    setResultError(Constants.ERROR_TYPE_JSON, e)
                }
            }
        })
    }

    fun logon(token:String, number:String, password:String) {
        val map = HashMap<String, String>()
        map[Constants.WS_MSG_TOKEN_SELF] = token
        map["number"] = number
        map["password"] = Constants.encodeString(password)
        map["sn"] = ""
        map["category"] = "2"
        loginModel.logon(map, object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                setResultError(Constants.ERROR_TYPE_NET, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body!!.string()
                try {
                    val obj = JSONObject(result)
                    val status = obj.getInt("status")
                    if(status == 200) {
                        val user = obj.getJSONObject("user")
                        val name = user.getString("name")
                        val num = user.getString("number")
                        val userToken = user.getString("user_token")
                        val imgUrl = user.getString("imageUrl")
                        setResultSuccess(User(userToken, name, num, imgUrl))
                    } else {
                        setResultError(Constants.ERROR_TYPE_COMM, Exception("register fail"))
                    }
                }catch (e: JSONException) {
                    setResultError(Constants.ERROR_TYPE_JSON, e)
                }
            }
        })
    }

    private fun setResultSuccess(user:User) {
        Handler(Looper.getMainLooper()).post {
            loginResult.value = LoginResult(Result.Success(user), 0)
        }
    }

    private fun setResultError(type: Int, e:Exception) {
        Handler(Looper.getMainLooper()).post {
            loginResult.value = LoginResult(Result.Error(e), type)
        }
    }
}