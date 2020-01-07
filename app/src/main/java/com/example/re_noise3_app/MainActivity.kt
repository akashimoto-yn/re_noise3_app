package com.example.re_noise3_app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import org.opencv.android.OpenCVLoader

class MainActivity : AppCompatActivity() {

    companion object {
        const val CAMERA_REQUEST_CODE = 1
        const val CAMERA_PERMISSION_REQUEST_CODE = 2
        const val External_Strage_REQUEST_CODE = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!OpenCVLoader.initDebug()) {
            //Handle initialization error
        } else {

            //カメラアプリが存在するかをチェック
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).resolveActivity(packageManager)?.let {

                //カメラアプリが存在してたら
                if (checkCameraPermission()) {  //カメラアプリの利用許可があるかチェック　あるとき：カメラを起動　
                }
                else {  //ないとき：カメラの利用許可を取得しに行く　
                    grantCameraPermission()
                }
            } ?: Toast.makeText(this, "カメラを扱うアプリがありません", Toast.LENGTH_LONG).show()  //カメラアプリがそもそもないとき、アプリがないことをお知らせ


            if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            }
            else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    External_Strage_REQUEST_CODE
                )
            }
        }
    }

    private fun checkCameraPermission() =
        PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.CAMERA
        )  //カメラアプリの使用許可があればtrueが返される

    private fun grantCameraPermission() = ActivityCompat.requestPermissions(
        this,
        arrayOf(Manifest.permission.CAMERA),
        CAMERA_PERMISSION_REQUEST_CODE
    )  //onrequestPermiisonsResultで実際に許可がもらえているか確認しに行く

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {  //リクエストコードが一致しているかを確認
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) { //結果が空でない（許可の有無が出ている）かつ結果として許可がでていたらカメラを起動
            }
            else{
                Toast.makeText(this, "カメラ利用許可が取得できていない可能性があります", Toast.LENGTH_LONG).show()  //カメラアプリの利用許可がないことをお知らせ
            }
        }
    }

    override fun onResume() {
        super.onResume()

        camera_btn.setOnClickListener {

            aaa

        }



}
