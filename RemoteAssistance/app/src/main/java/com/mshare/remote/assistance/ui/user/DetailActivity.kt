package com.mshare.remote.assistance.ui.user

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.mshare.remote.assistance.Constants
import com.mshare.remote.assistance.util.OkHttpUtil
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import com.mshare.remote.assistance.R
import java.io.ByteArrayOutputStream

class DetailActivity : AppCompatActivity() {
    private var mEditMode = false
    private lateinit var mEidtText:EditText
    private lateinit var mNameText:TextView
    private lateinit var mImgView:ImageView
    private lateinit var mToken:String
    private lateinit var mName:String
    private lateinit var mImgUrl:String
    private var bitmap:Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        mEidtText = findViewById(R.id.name_edit)
        mNameText = findViewById(R.id.name)
        mImgView = findViewById(R.id.image)
        mToken = Constants.getUserToken(this)
        mEditMode = intent.getBooleanExtra("for_edit", false)
        val fromLogon = intent.getBooleanExtra("from_logon", false)
        if(fromLogon) {
            mName =intent.getStringExtra("name")!!
            mImgUrl =intent.getStringExtra("image")!!
            setView()
        } else {
            mName = ""
            loadInfo()
        }
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setView() {
        mEidtText.setText(mName)
        mNameText.text = mName
        if(mImgUrl == "" || mImgUrl == "none") {
            val imgUrl = Constants.getImageUrl(this, "default.jpg")
            Glide.with(this).load(imgUrl).placeholder(R.drawable.default_img).into(mImgView)
        } else {
            val imgUrl = Constants.getImageUrl(this, mImgUrl)
            Glide.with(this).load(imgUrl).placeholder(R.drawable.default_img).into(mImgView)
        }
    }

    private fun updateView(eidtMode:Boolean) {
        if(eidtMode) {
            mEidtText.visibility = View.VISIBLE
            mNameText.visibility = View.GONE
            mImgView.setOnClickListener {
                selectImage()
            }
        } else {
            mEidtText.visibility = View.GONE
            mNameText.visibility = View.VISIBLE
            mImgView.setOnClickListener(null)
        }
    }

    private fun loadInfo() {
        val map = HashMap<String, String>()
        map[Constants.WS_MSG_TOKEN_SELF] = mToken
        OkHttpUtil.baseGet(Constants.getUserInfoUrl(), map, callback = object:Callback{
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body!!.string()
                try {
                    val obj = JSONObject(result)
                    val status = obj.getInt("status")
                    if(status == 200) {
                        val user = obj.getJSONObject("user")
                        val name = user.getString("name")
                        val imgUrl = user.getString("imageUrl")
                        onLoad(name, imgUrl)
                    }
                }catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })
    }

    private fun onLoad(name:String, imgUrl:String) {
        Handler(Looper.getMainLooper()).post {
            mName = name
            mImgUrl = imgUrl
            setView()
        }
    }

    private fun selectImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_PICK
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, data?.data!!)
                val listener = ImageDecoder.OnHeaderDecodedListener { decoder, _, _ -> decoder.setTargetSize(Constants.USER_IMG_SIZE,Constants.USER_IMG_SIZE) }
                val drawable = ImageDecoder.decodeDrawable(source, listener)
                bitmap = drawable.toBitmap()
                mImgView.setImageDrawable(drawable)
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, data?.data)
                mImgView.setImageBitmap(bitmap)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.detail_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if(mEditMode) {
            menu?.findItem(R.id.action_edit )?.isVisible = false
            menu?.findItem(R.id.action_done )?.isVisible = true
        } else {
            menu?.findItem(R.id.action_edit )?.isVisible = true
            menu?.findItem(R.id.action_done )?.isVisible = false
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_edit -> gotoEditMode()
            R.id.action_done -> doSave()
            android.R.id.home -> finish()
            else -> {}
        }
        return super.onOptionsItemSelected(item)
    }

    private fun gotoEditMode() {
        mEditMode = true
        updateView(mEditMode)
        invalidateOptionsMenu()
    }

    private fun doSave() {
        if(bitmap != null) {
            val uploadBitmap = bitmap
            Thread {
                val out = ByteArrayOutputStream()
                if(uploadBitmap!!.compress(Bitmap.CompressFormat.JPEG,100, out)){
                    uploadImage(Base64.encodeToString(out.toByteArray(), 0))
                }
            }.start()
        } else {
            doSaveAll("")
        }
    }

    private fun uploadImage(data:String) {
        val map = HashMap<String, String>()
        map[Constants.WS_MSG_TOKEN_SELF] = mToken
        map["image"] = data
        OkHttpUtil.basePost(Constants.getImageUploadUrl(), map, callback = object:Callback{
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body!!.string()
                try {
                    val obj = JSONObject(result)
                    val status = obj.getInt("status")
                    if(status == 200) {
                        val imgUrl = obj.getString("imageUrl")
                        doSaveAll(imgUrl)
                    }
                }catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

        })
    }

    private fun doSaveAll(imgUrl: String) {
        val newName = mEidtText.text.toString()
        if(newName != mName || imgUrl != "") {
            val map = HashMap<String, String>()
            map[Constants.WS_MSG_TOKEN_SELF] = mToken
            map["image"] = imgUrl
            map["name"] = newName
            OkHttpUtil.basePost(Constants.getUserUpdateUrl(), map, callback = object:Callback{
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    val result = response.body!!.string()
                    try {
                        val obj = JSONObject(result)
                        val status = obj.getInt("status")
                        if(status == 200) {
                            onSaveDone()
                        }
                    }catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            })
        } else {
            finish()
        }
    }

    private fun onSaveDone() {
        Handler(Looper.getMainLooper()).post {
            finish()
        }
    }
}
