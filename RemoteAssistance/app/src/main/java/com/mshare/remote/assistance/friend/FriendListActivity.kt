package com.mshare.remote.assistance.friend

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.activity.CaptureActivity
import com.mshare.remote.assistance.Constants
import com.mshare.remote.assistance.QrcodeActivity
import com.mshare.remote.assistance.R
import com.mshare.remote.assistance.SettingsActivity
import com.mshare.remote.assistance.friend.model.FriendEntity
import com.mshare.remote.assistance.util.OkHttpUtil
import com.wen.app.update.ApkUtils
import com.wen.app.update.UpdateVersionService
import kotlinx.android.synthetic.main.activity_friend_list.*
import kotlinx.coroutines.*
import org.json.JSONException
import org.json.JSONObject
import wen.mmvm.arch.Result
import java.lang.ref.WeakReference

class FriendListActivity : AppCompatActivity() {
    private lateinit var mAdaper:GridAdapter
    private lateinit var mEmptyView: TextView
    private val mHandler = MainHandler(this@FriendListActivity)
    private var deleteMode = false
    private lateinit var viewModel: FriendViewModel
    private lateinit var refreshLayout:SwipeRefreshLayout

    companion object{
        private const val REQUEST_CODE_SCAN = 0x123
        private const val ERROR_MSG = 1018
        private val NO_FRIEND = arrayListOf<FriendEntity>()
        private class MainHandler(activity:FriendListActivity):Handler() {
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
        OkHttpUtil.initHttpClientCache(this)

        if(!checkAppVersion(this)){
            showProgress()
            return
        }
        viewModel = ViewModelProviders.of(this).get(FriendViewModel::class.java)

        initListView()
        var token = Constants.getUserToken(this, false)
        if(token == "") {
            token = Constants.getUserToken(this, true)
            Constants.saveUserToken(this, token)
            viewModel.addUser(token, "2").observe(this, Observer{
                if(it is Result.Error){
                    Constants.saveUserToken(this, "")
                }
            })
            mEmptyView.setText(R.string.friend_list_empty)
            mEmptyView.visibility = View.VISIBLE
            refreshLayout.isRefreshing = false
        } else {
            viewModel.getAllFriends().observe(this, Observer {
                if(it is Result.Loading) {
                    showErrorMsg(it.message)
                } else if(it is Result.Success) {
                    setData(it.data)
                    refreshLayout.isRefreshing = false
                }
            })
        }
        viewModel.mErrorType.observe(this,  Observer{
            if(it != 0) {
                onError(it)
            }
        })
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
        mAdaper.setDeleteLister(object:GridAdapter.DeleteLister{
            override fun onDelete(friendToken: String) {
                viewModel.addOrRemoveFriend(Constants.getUserToken(this@FriendListActivity), friendToken, 2)
            }

        })
        mEmptyView = findViewById(R.id.empty_view)
        refreshLayout = findViewById(R.id.refresh)
        refreshLayout.setProgressViewOffset(false, 0,
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 28F, resources.displayMetrics).toInt()
        )

        refreshLayout.setOnRefreshListener {
            mEmptyView.postDelayed({
                viewModel.updateCache()
            }, 2000)
        }
        refreshLayout.setProgressBackgroundColorSchemeResource(android.R.color.white)
        refreshLayout.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_red_light, android.R.color.holo_orange_light,
                android.R.color.holo_green_light)
        refreshLayout.isRefreshing = true
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun setData(friendList: List<FriendEntity>?) = GlobalScope.launch(Dispatchers.Main){
        if(friendList == null) {
            mAdaper.setData(NO_FRIEND)
        } else {
            mAdaper.setData(friendList)
        }
        if(friendList == null || friendList.isEmpty()) {
            mEmptyView.setText(R.string.friend_list_empty)
            mEmptyView.visibility = View.VISIBLE
        } else {
            mEmptyView.visibility = View.GONE
        }
    }

    private fun onError(type: Int) {
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
        Snackbar.make(mEmptyView, errorMsg, Snackbar.LENGTH_LONG).show()
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
                    viewModel.addOrRemoveFriend(Constants.getUserToken(this), token, 1)
                } else {
                    onError(Constants.ERROR_INVALID_TOKEN)
                }
            }
        } else if(Constants.REQUEST_CODE_VERSION == requestCode) {
            val type = data!!.getIntExtra("type", 0)
            if (type == 0) {
                val progress = data.getIntExtra("progress", 0)
                updateProgress(progress)
            } else {
                onUpdateComplete(type)
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
            R.id.action_delete -> {
                gotoDeleteMode()
                item.title = if(deleteMode) getString(R.string.menu_cancel_delete) else getString(R.string.menu_delete)
            }
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

    private fun gotoDeleteMode() {
        deleteMode = !deleteMode
        mAdaper.setDeleteMode(deleteMode)
    }

    /******************** 版本升级进度  */
    private var result:String? = null
    private fun checkAppVersion(context: Context): Boolean {
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
            val apkVersion = obj.getLong("version")
            if(apkVersion > curVersion) {
                val apkType = obj.getInt("type")
                val apkChecksum = obj.getString("checksum1")
                val patchChecksum = obj.getString("checksum2")
                startUpdateVersionService(apkVersion, apkType, apkChecksum, patchChecksum)
                return false
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return true
    }


    private fun startUpdateVersionService(apkVersion: Long, apkType: Int, apkChecksum: String, patchChecksum: String) {
        val intent = Intent(this, UpdateVersionService::class.java)
        intent.putExtra("type", apkType)
        intent.putExtra("version", apkVersion)
        intent.putExtra("checksum1", apkChecksum)
        intent.putExtra("checksum2", patchChecksum)
        val pendingIntent = createPendingResult(Constants.REQUEST_CODE_VERSION, Intent(), 0)
        intent.putExtra("pendingIntent", pendingIntent)
        startService(intent)
    }

    private var mProgress: ProgressDialog? = null

    @Suppress("DEPRECATION")
    private fun showProgress() {
        val progress = ProgressDialog(this)
        progress.setTitle(R.string.update_version_title)
        progress.setIcon(R.mipmap.ic_launcher)
        progress.setMessage(getString(R.string.update_version_content))
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progress.setCancelable(false)
        mProgress = progress
        progress.show()
    }

    private fun updateProgress(progress: Int) {
        mProgress?.progress = progress
    }

    private fun onUpdateComplete(type: Int) {
        if (mProgress!!.isShowing()) {
            mProgress?.dismiss()
            if (type == 1) {
                finish()
            }
        }
    }
}
