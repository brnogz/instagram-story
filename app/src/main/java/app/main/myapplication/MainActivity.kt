package app.main.myapplication

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
    }

    private var file: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dispatchTakePictureIntent()

        share.setOnClickListener {
            val file = this.file ?: return@setOnClickListener
            val packageName = applicationContext?.packageName + ".provider"
            val fileUri = FileProvider.getUriForFile(this, packageName, file)

            try {
                val intent = Intent("com.instagram.share.ADD_TO_STORY")
//        val intent = Intent("com.facebook.stories.ADD_TO_STORY")
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                intent.type = "image/png"
//        intent.putExtra("com.facebook.platform.extra.APPLICATION_ID", 0000000000000)
                intent.putExtra("interactive_asset_uri", fileUri)
                intent.putExtra("content_url", "https://translately.com")
                intent.putExtra("top_background_color", "#fe4fa1")
                intent.putExtra("bottom_background_color", "#00d2e1")
                grantUriPermission(
                    "com.instagram.android",
                    fileUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
//        grantUriPermission("com.facebook.katana", fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Log.e(this::class.java.name, e.message.toString(), e)
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val imageBitmap = data?.extras?.get("data") as? Bitmap ?: return
                        preview.setImageBitmap(imageBitmap)

                        val directory = File(cacheDir, "story")
                        if (directory.exists().not())
                            directory.mkdirs()

                        val file = File.createTempFile("story", ".png", directory)
                        file.outputStream().use {
                            imageBitmap.compress(Bitmap.CompressFormat.PNG, 0, it)
                        }

                        this.file = file
                        share.isEnabled = true
                    }
                }
            }
        }
    }
}
