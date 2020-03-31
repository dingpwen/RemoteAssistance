package com.mshare.remote.assistance.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.mshare.remote.assistance.Constants
import java.util.regex.Matcher
import java.util.regex.Pattern

import com.mshare.remote.assistance.R
import com.mshare.remote.assistance.ui.login.data.Result
import com.mshare.remote.assistance.ui.login.data.User
import com.mshare.remote.assistance.ui.user.DetailActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var viewModel: LoginViewModel
    private lateinit var codePart: LinearLayoutCompat
    private lateinit var numText: EditText
    private lateinit var pwdText: EditText
    private lateinit var codeText: EditText
    private lateinit var loginBtn: Button
    private lateinit var chgText: TextView
    private var mCurType = 1

    companion object{
        const val REG_PHONE = "^1[3-9]\\d{9}\$"
        //const val REG_PHONE = "^((13[0-9])|(15[^4])|(18[0-9])|(17[0-8])|(147,145))\\d{8}$"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initView()
        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)
        viewModel.loginMode.observe(this, Observer {
            changeView(it == 1)
        })
        viewModel.loginResult.observe(this, Observer{
            if(it.failType == 0) {
                loginSuccess(it.result as Result.Success<User>)
            } else {
                loginFail(it.failType)
            }
        })
    }

    private fun initView() {
        codePart = findViewById(R.id.code_part)
        numText = findViewById(R.id.number)
        pwdText = findViewById(R.id.password)
        codeText = findViewById(R.id.code)
        loginBtn = findViewById(R.id.action_login)
        chgText = findViewById(R.id.action_change)
        chgText.setOnClickListener {
            mCurType = if(mCurType == 1) 2 else 1
            viewModel.loginMode.value = mCurType
        }
        loginBtn.setOnClickListener {
            tryLogin()
        }
    }

    private fun changeView(isLogin:Boolean){
        if(isLogin) {
            codePart.visibility = View.GONE
            chgText.text = getString(R.string.to_logon)
            loginBtn.text = getString(R.string.login_button_login)
        } else {
            codePart.visibility = View.VISIBLE
            chgText.text = getString(R.string.to_login)
            loginBtn.text = getString(R.string.login_button_logon)
        }
    }

    private fun tryLogin() {
        val number = numText.text.toString()
        val password = pwdText.text.toString()
        if(mCurType == 1) {
            if(checkNumber(number) && checkPassword(password)){
                viewModel.login(Constants.getUserToken(this), number, password)
            }
        } else {
            val code = codeText.text.toString()
            if(checkCode(code) && checkNumber(number) && checkPassword(password)){
                viewModel.logon(Constants.getUserToken(this), number, password)
            }
        }
    }

    private fun checkNumber(number:String):Boolean{
        if(number == "") {
            Toast.makeText(this, R.string.number_is_empty, Toast.LENGTH_LONG).show()
            return false
        }
        if(number.length == 11) {
            val p: Pattern = Pattern.compile(REG_PHONE)
            val m: Matcher = p.matcher(number)
            if(m.matches()) {
                return true
            }
        }
        Toast.makeText(this, R.string.number_is_invalid, Toast.LENGTH_LONG).show()
        return false
    }

    private fun checkPassword(password:String):Boolean{
        if(password == "") {
            Toast.makeText(this, R.string.pwd_is_empty, Toast.LENGTH_LONG).show()
            return false
        }
        if(password.length < Constants.PWD_MIN_LENGTH || password.length > Constants.PWD_MAX_LENGTH) {
            Toast.makeText(this, R.string.pwd_is_invalid, Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun checkCode(code:String):Boolean{
        if(code == "") {
            Toast.makeText(this, R.string.code_is_empty, Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun loginSuccess(result:Result.Success<User>) {
        val user = result.data
        Constants.saveUserToken(this, user.user_token)
        Constants.setLoginStatus(this, Constants.USER_LOGIN_STATUS_ON)
        if(mCurType == 2) {
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("for_edit", true)
            intent.putExtra("from_logon", true)
            intent.putExtra("token", user.user_token)
            intent.putExtra("name", user.name)
            intent.putExtra("image", user.imageUrl)
            startActivity(intent)
        }
        finish()
    }

    private fun loginFail(failType:Int) {
        if(failType == Constants.ERROR_TYPE_NET){
            Toast.makeText(this, R.string.error_msg_net, Toast.LENGTH_LONG).show()
        } else if(failType == Constants.ERROR_TYPE_JSON) {
            Toast.makeText(this, R.string.error_msg_json, Toast.LENGTH_LONG).show()
        } else {
            if(mCurType == 1) {
                Toast.makeText(this, R.string.fail_to_login, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, R.string.fail_to_logon, Toast.LENGTH_LONG).show()
            }
        }
    }
}
