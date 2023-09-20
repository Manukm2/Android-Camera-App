package com.example.cameraapp

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.nfc.Tag
import android.nfc.tech.TagTechnology
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.cameraapp.databinding.ActivityMainBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.log

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private var imageCapture : ImageCapture? = null
    private lateinit var outputDirectory : File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        outputDirectory = getOutputDirectory()

        if(allPermissionGranted()){
            Log.d("1", "-------------1-----------------")
            startCamera()
            Log.d("1", "-------------2-----------------")
        }else{
            ActivityCompat.requestPermissions(
                this, Constants.REQUEST_PERMISSIONS,
                Constants.REQUEST_CODE_PERMISSION
            )
        }
        Log.d("1", "-------------3-----------------")
        binding.btnTakePhoto.setOnClickListener{
            Log.d("1", "-------------4-----------------")
            takePhoto()
            Log.d("1", "-------------5-----------------")
        }
    }

    private fun getOutputDirectory() : File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let{ mFile->
            File(mFile, resources.getString(R.string.app_name)).apply{
                mkdirs()
            }
        }
        return if(mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    private fun allPermissionGranted() =
        Constants.REQUEST_PERMISSIONS.all{
             ContextCompat.checkSelfPermission(baseContext,it) ==
                     PackageManager.PERMISSION_GRANTED
        }

    private fun startCamera(){
        Log.d("1", "-------------A-----------------")
        val cameraProviduFuture = ProcessCameraProvider.getInstance(this)

        cameraProviduFuture.addListener({
            Log.d("1", "-------------B-----------------")
            val cameraProvider: ProcessCameraProvider = cameraProviduFuture.get()
            val preview = Preview.Builder().build().also { mPreview->
                    mPreview.setSurfaceProvider(
                        binding.viewFinder.surfaceProvider
                    )
                }
            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,cameraSelector,
                    preview, imageCapture
                )

            }catch (e: Exception){
                Log.d(Constants.TAG,  "StartCamera Falied", e)
            }
        }, ContextCompat.getMainExecutor(this))
        Log.d("1", "-------------c-----------------")
    }

    private fun  takePhoto(){
        val imageCapture = imageCapture ?: return
        val photoFile = File(outputDirectory,
            SimpleDateFormat(Constants.FILE_NAME_FORMAT,Locale.getDefault())
                .format(System.currentTimeMillis()) + ".jpg")

        val outputOption = ImageCapture.OutputFileOptions
            .Builder(photoFile).build()

        imageCapture.takePicture(
            outputOption,ContextCompat.getMainExecutor(this),
            object :ImageCapture.OnImageSavedCallback{
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo Saved"

                    Toast.makeText(
                        this@MainActivity,
                       "$msg $savedUri" ,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(Constants.TAG,"onError ${exception.message}", exception)
                }
            }
            )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == Constants.REQUEST_CODE_PERMISSION){
            if(allPermissionGranted()){
                startCamera()
            }else{
                Toast.makeText(this,
                    "permiision not granted by the user",
                    Toast.LENGTH_SHORT).show()

                finish()
            }
        }
    }


}