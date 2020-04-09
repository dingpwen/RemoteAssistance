package com.mshare.remote.assistance.friend

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.zxing.activity.CaptureActivity
import com.mshare.remote.assistance.Constants
import com.mshare.remote.assistance.QrcodeActivity
import kotlinx.android.synthetic.main.activity_friend_list.*
import com.mshare.remote.assistance.R
import com.mshare.remote.assistance.SettingsActivity
import java.lang.ref.WeakReference

class FriendListActivity : AppCompatActivity(), Contact.IView {
    private lateinit var mPresenter:Contact.IPresenter
    private lateinit var mAdaper:GridAdapter
    private lateinit var mEmptyView: TextView
    private val mHandler = MainHadler(this@FriendListActivity)

    companion object{
        private const val REQUEST_CODE_SCAN = 0x123
        private const val ERROR_MSG = 1018
        private class MainHadler(activity:FriendListActivity):Handler() {
            private val mActivity = WeakReference(activity)
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when(msg.what) {
                    ERROR_MSG -> {
                        val errorMsg = msg.obj as String
                        mActivity.get()?.showErrorMsg(errorMsg)
                    }
                    else -> {
                        return
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_list)
        setSupportActionBar(toolbar)

        mPresenter = FriendPresenter()
        mPresenter.attachView(this)

        initListView()
        var token = Constants.getUserToken(this, false)
        if(token == "") {
            token = Constants.getUserToken(this, true)
            mPresenter.addUser(this, token)
            mEmptyView.setText(R.string.friend_list_empty)
            mEmptyView.visibility = View.VISIBLE
        } else {
            mPresenter.loadData(token)
        }

        fab.setOnClickListener {
            scanForAdd()
        }
    }

    private fun initListView() {
        mAdaper = GridAdapter(this)
        findViewById<RecyclerView>(R.id.friend_list).let{
            it.layoutManager = GridLayoutManager(this, 2)
            it.setHasFixedSize(false)
            it.adapter = mAdaper
        }
        mEmptyView = findViewById(R.id.empty_view)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.detachView()
    }

    override fun setData(friendList: MutableList<FriendInfo>) = runOnUiThread{
        mAdaper.setData(friendList)
        if(friendList.size == 0) {
            mEmptyView.setText(R.string.friend_list_empty)
            mEmptyView.visibility = View.VISIBLE
        } else {
            mEmptyView.visibility = View.GONE
        }
    }

    override fun onError(type: Int) {
        val errorMsg:String = when(type) {
            Constants.ERROR_TYPE_NET -> getString(R.string.error_msg_net)
            Constants.ERROR_TYPE_JSON -> getString(R.string.error_msg_json)
            Constants.ERROR_TYPE_ADD -> getString(R.string.error_msg_add)
            Constants.ERROR_TYPE_DEL -> getString(R.string.error_msg_del)
            Constants.ERROR_INVALID_TOKEN -> getString(R.string.error_invalid_code)
            else -> {
                return
            }
        }
        val msg = Message.obtain()
        msg.what = ERROR_MSG
        msg.obj = errorMsg
        mHandler.sendMessage(msg)
    }

    private fun showErrorMsg(errorMsg: String) {
        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
    }

    private fun scanForAdd() {
        val intent = Intent(this, CaptureActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_SCAN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CODE_SCAN) {
            if(resultCode == 0xA1) {
                val token = data?.extras?.getString ("qr_scan_result")?:return
                if(Constants.checkUserToken(token)) {
                    mPresenter.addOrRemoveFriend(Constants.getUserToken(this), token, 1)
                } else {
                    onError(Constants.ERROR_INVALID_TOKEN)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.friend_list_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_qrcode -> showMyQrcode()
            R.id.action_settings -> gotoSettings()
            else -> {}
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showMyQrcode() {
        startActivity( Intent(this, QrcodeActivity::class.java))
    }

    private fun gotoSettings() {
        startActivity( Intent(this, SettingsActivity::class.java))
    }
}
