package com.example.re_noise3_app

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_main.*
import org.opencv.android.OpenCVLoader
import java.io.File
import java.io.FileOutputStream
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val CAMERA_REQUEST_CODE = 1
        const val CAMERA_PERMISSION_REQUEST_CODE = 2
        const val External_Strage_REQUEST_CODE = 3
    }

    private lateinit var image : Bitmap
    private lateinit var mImageUri : Uri

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

            takePicture()

            //camera_transを起動するための、intentを初期化する
            val intent = Intent(this, transformation::class.java)

            saveFile(createFile(), image)

            //bitmapをUriに変換する
            val uri: Uri = bitmapToUri(image)

            //intentにUriをセットする
            intent.putExtra("uri", uri)

            //camera_viewを起動する
            startActivity(intent)

        }
    }

    private fun takePicture() {
        val filename : String = System.currentTimeMillis().toString()

        val values = ContentValues()

        values.put(MediaStore.Images.Media.TITLE, filename)

        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")


        val mImageUri_kari = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        if (mImageUri_kari != null){
            mImageUri = mImageUri_kari
        }


        val intent : Intent = Intent()

        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE)

        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri)


        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {

                image = MediaStore.Images.Media.getBitmap(this.contentResolver, mImageUri) as Bitmap

            }
        }
    }

    private fun createFile(): File {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        val date = Date()
        println("日付" + date)
        return File(dir, date.toString() + ".png")
    }

    private fun saveFile(f: File, bitmap: Bitmap) {

        var bit: Bitmap = bitmap


        val ops = FileOutputStream(f)

        bit.compress(Bitmap.CompressFormat.PNG, 100, ops)


        ops.close()

        //ギャラリーからもアクセスできるように、画像データとしてAndroidに登録
        val contextValues = ContentValues().apply {
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put("_data", f.absolutePath)
        }

        contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contextValues
        )

    }

    private fun bitmapToUri(bitmap: Bitmap) : Uri {

        //一時ファイル作成用のキャッシュディレクトリを定義する
        val cacheDir : File = this.cacheDir

        //現在日時からファイル名を生成する
        val fileName : String = System.currentTimeMillis().toString() + ".png"

        //空のファイルを作成する
        val file = File(cacheDir, fileName)

        //ファイルにバイトデータを書き込み開始する
        val fileOutputStream : FileOutputStream? = FileOutputStream(file)

        //ファイルにbitmapを書き込む
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)

        //ファイルにバイトデータの書き込みを終了する
        fileOutputStream?.close()

        //ファイルからcontent://スキーマ形式のuriを取得する
        val contentSchemaUri : Uri = FileProvider.getUriForFile(this, "com.hoge.fuga.fileproviders", file)

        return contentSchemaUri
    }
}
