package com.uzair.scopedstorage

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL


const val PICK_PDF_FILE = 2

const val IMAGE_URL = "https://storage.googleapis.com/kalaamtime/files/VxDd9PtRK5QQtbt.jpg"
const val VIDEO_URL = "https://storage.googleapis.com/kalaamtime/files/rZagbyRkyn5js5n.mp4"
const val AUDIO_URL = "https://storage.googleapis.com/kalaamtime/files/mmif5A9PZntFkyh.wav"
const val DOCUMENT_URL = "https://storage.googleapis.com/kalaamtime/files/9K3SPCJ55yB4Hkv.pdf"


class MainActivity : AppCompatActivity() {

    private val httpClient by lazy { OkHttpClient() }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnPDF = findViewById<Button>(R.id.btnPDF)
        val btnDownloadImage = findViewById<Button>(R.id.downloadImage)
        val btnDownloadVideo = findViewById<Button>(R.id.downloadVideo)
        val btnDownloadAudio = findViewById<Button>(R.id.downloadAudio)
        val btnDownloadDocument = findViewById<Button>(R.id.downloadDocument)

        btnPDF.setOnClickListener {
            openImages()
        }

        btnDownloadImage.setOnClickListener {
            storeImage()
        }

        btnDownloadVideo.setOnClickListener {
            storeVideo()
        }

        btnDownloadAudio.setOnClickListener {
            storeAudio()
        }

        btnDownloadDocument.setOnClickListener {
            storeDocument()
        }

    }

    private fun storeDocument() {
        saveDocumentFromInternet {
            if (it == "completed") {
                Toast.makeText(this, "PDF Downloaded", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun storeAudio() {
        saveAudioFromInternet {
            if (it == "completed") {
                Toast.makeText(this, "Audio Downloaded", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun storeVideo() {
        Log.e("eeeee", "Start")

        saveVideoFromInternet {
            if (it == "completed") {
                Toast.makeText(this, "Video Downloaded", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun createVideoUri(): Uri? {
        val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        return withContext(Dispatchers.IO) {
            val newImage = ContentValues().apply {
                put(
                    MediaStore.Video.Media.DISPLAY_NAME,
                    "internet-${System.currentTimeMillis()}.mp4"
                )
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_MOVIES + File.separator + "Kalam Time"
                )
            }
            return@withContext contentResolver.insert(imageCollection, newImage)
        }
    }

    private suspend fun createAudioUri(): Uri? {
        val audioCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        return withContext(Dispatchers.IO) {
            val newImage = ContentValues().apply {
                put(
                    MediaStore.Audio.Media.DISPLAY_NAME,
                    "internet-${System.currentTimeMillis()}.wav"
                )
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_RINGTONES + File.separator + "Kalam Time"
                )
            }
            return@withContext contentResolver.insert(audioCollection, newImage)
        }
    }

    private suspend fun createDocumentUri(): Uri? {
        val docCollection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val extension = DOCUMENT_URL.split("/").last().split(".").last()


        return withContext(Dispatchers.IO) {
            val newDoc = ContentValues().apply {
                put(
                    MediaStore.Audio.Media.DISPLAY_NAME,
                    "internet-${System.currentTimeMillis()}.${extension}"
                )
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOCUMENTS + File.separator + "Kalam Time"
                )
            }

            return@withContext contentResolver.insert(docCollection, newDoc)
        }
    }

    private fun saveVideoFromInternet(callback: (String) -> Unit) {
        GlobalScope.launch {
            val videoUri = createVideoUri()
            // We use OkHttp to create HTTP request
            val request = Request.Builder().url(VIDEO_URL).build()
            withContext(Dispatchers.IO) {

                videoUri?.let { destinationUri ->
                    val response = httpClient.newCall(request).execute()
                    Log.e("eeeee", "SendingRequest")

                    response.body?.use { responseBody ->
                        contentResolver.openOutputStream(destinationUri, "w")?.use {
                            responseBody.byteStream().copyTo(it)
                            Log.e("eeeee", "Request Returned ${responseBody.string()}")

                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@MainActivity, "Completed", Toast.LENGTH_LONG)
                                    .show()
                                Log.e("eeeee", "done")
//                                savedStateHandle["currentMediaUri"] = destinationUri
                                callback("Completed")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun saveAudioFromInternet(callback: (String) -> Unit) {
        GlobalScope.launch {
            val audioUri = createAudioUri()
            // We use OkHttp to create HTTP request
            val request = Request.Builder().url(AUDIO_URL).build()
            withContext(Dispatchers.IO) {

                audioUri?.let { destinationUri ->
                    val response = httpClient.newCall(request).execute()
                    Log.e("eeeee", "SendingRequest")

                    response.body?.use { responseBody ->
                        contentResolver.openOutputStream(destinationUri, "w")?.use {
                            responseBody.byteStream().copyTo(it)
                            Log.e("eeeee", "Request Returned ${responseBody.string()}")

                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@MainActivity, "Completed", Toast.LENGTH_LONG)
                                    .show()
                                Log.e("eeeee", "done")
//                                savedStateHandle["currentMediaUri"] = destinationUri
                                callback("Completed")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun saveDocumentFromInternet(callback: (String) -> Unit) {
        GlobalScope.launch {
            val documentUri = createDocumentUri()
            // We use OkHttp to create HTTP request
            val request = Request.Builder().url(DOCUMENT_URL).build()
            withContext(Dispatchers.IO) {

                documentUri?.let { destinationUri ->
                    val response = httpClient.newCall(request).execute()
                    Log.e("eeeee", "SendingRequest")

                    response.body?.use { responseBody ->
                        contentResolver.openOutputStream(destinationUri, "w")?.use {
                            responseBody.byteStream().copyTo(it)
                            Log.e("eeeee", "Request Returned ${responseBody.string()}")

                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@MainActivity, "Completed", Toast.LENGTH_LONG)
                                    .show()
                                Log.e("eeeee", "done")
//                                savedStateHandle["currentMediaUri"] = destinationUri
                                callback("Completed")
                            }
                        }
                    }
                }
            }
        }
    }


    enum class Source {
        CAMERA, INTERNET
    }


    private fun storeImage() {

        GlobalScope.launch {
            val urlBitMap = URL(IMAGE_URL)
            val values = ContentValues()
            val imageBitmap =
                BitmapFactory.decodeStream(urlBitMap.openConnection().getInputStream())
            val resolver = contentResolver
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, "imageName")
            values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            values.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + File.separator + "Kalam Time"
            )
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            val outPutStream = resolver.openOutputStream(uri!!)!!
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outPutStream)
            Log.e("eeeeeee", "Download Completed")
        }

    }


    fun openPdfFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"

            // Optionally, specify a URI for the file that should appear in the
            // system file picker when it loads.
        }

        startActivityForResult(intent, PICK_PDF_FILE)
    }

    fun openWordFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"

            // Optionally, specify a URI for the file that should appear in the
            // system file picker when it loads.
        }

        startActivityForResult(intent, PICK_PDF_FILE)
    }

    private fun openImages() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

            // Optionally, specify a URI for the file that should appear in the
            // system file picker when it loads.
        }

        startActivityForResult(intent, PICK_PDF_FILE)
    }

    private fun requestPermission() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            startActivity(Intent(ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
        }

//        val intent = Intent(Intent.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION ).apply {
//            addCategory(Intent.CATEGORY_OPENABLE)
//            type = "image/*"
//            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
//
//            // Optionally, specify a URI for the file that should appear in the
//            // system file picker when it loads.
//        }

//        startActivityForResult(intent, PICK_PDF_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        getRealPathFromURI(data?.data!!)
    }

    private fun getRealPathFromURI(contentUri: Uri): File {
        val f = File(
            externalCacheDir, "IMG_SHARED.png"
        )
        f.createNewFile()
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, contentUri)
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos)
        val bitmapdata = bos.toByteArray()

//write the bytes in file
        val fos = FileOutputStream(f)
        fos.write(bitmapdata)
        fos.flush()
        fos.close()

        return f
    }

}