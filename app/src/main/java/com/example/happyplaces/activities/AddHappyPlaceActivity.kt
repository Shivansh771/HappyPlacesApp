package com.example.happyplaces.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.example.happyplaces.R
import com.example.happyplaces.database.DatabaseHandler
import com.example.happyplaces.databinding.ActivityAddHappyPlaceBinding
import com.example.happyplaces.models.HappyPlaceModel
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityAddHappyPlaceBinding
    private var cal=Calendar.getInstance()
    private lateinit var dateSetListner: DatePickerDialog.OnDateSetListener
    private var saveImageToInternalStorage:Uri?=null
    private var mLatitude:Double=0.0
    private var mLongitude:Double=0.0
    private   var mHappyPlaceDetails:HappyPlaceModel?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityAddHappyPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener{
            onBackPressed()
        }
        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mHappyPlaceDetails= intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS)
        }
        dateSetListner= DatePickerDialog.OnDateSetListener{
            view, year, month, dayOfMonth ->  cal.set(Calendar.YEAR,year)
            cal.set(Calendar.MONTH,month)
            cal.set(Calendar.DAY_OF_MONTH,dayOfMonth)
            updateDateInView()
        }
        updateDateInView()
        if(mHappyPlaceDetails!=null){
            supportActionBar?.title="Edit Happy place"
            binding.etTitle.setText(mHappyPlaceDetails!!.title)
            binding.etDescription.setText(mHappyPlaceDetails!!.description)
            binding.etDate.setText(mHappyPlaceDetails!!.date)
            binding.etLocation.setText(mHappyPlaceDetails!!.location)
            mLatitude=mHappyPlaceDetails!!.latitude
            mLongitude=mHappyPlaceDetails!!.longitude
            saveImageToInternalStorage=Uri.parse(
                mHappyPlaceDetails!!.image
            )
            binding.ivPlaceImage.setImageURI(saveImageToInternalStorage)
            binding.btnSave.text="UPDATE"

        }
        binding.etDate.setOnClickListener(this)
        binding.tvAddImage.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.et_date ->{
                DatePickerDialog(this@AddHappyPlaceActivity,dateSetListner,cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).show()
            }
            R.id.tv_add_image -> {
                val pictureDialog=AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")

                val pictureDialogItems= arrayOf("Select Photo from Gallery","Capture photo from camera")
                pictureDialog.setItems(pictureDialogItems){
                    dialog,which->
                    when(which){
                        0->choosePhotoFromGallery()
                        1-> takePhotoFromCamera()
                    }
                }.show()

            }
            R.id.btn_save->{
                when{
                binding.etTitle.text.isNullOrEmpty()->{
                    MotionToast.createColorToast(this@AddHappyPlaceActivity,"please Enter a title","",MotionToastStyle.INFO,MotionToast.GRAVITY_BOTTOM,MotionToast.SHORT_DURATION,                ResourcesCompat.getFont(this,www.sanju.motiontoast.R.font.helvetica_regular))

                }
                binding.etDescription.text.isNullOrEmpty()->{
                    MotionToast.createColorToast(this@AddHappyPlaceActivity,"Please Enter a description","",MotionToastStyle.INFO,MotionToast.GRAVITY_BOTTOM,MotionToast.SHORT_DURATION,                ResourcesCompat.getFont(this,www.sanju.motiontoast.R.font.helvetica_regular))

                }
                    binding.etLocation.text.isNullOrEmpty()->{
                        MotionToast.createColorToast(this@AddHappyPlaceActivity,"Please Enter a location","",MotionToastStyle.INFO,MotionToast.GRAVITY_BOTTOM,MotionToast.SHORT_DURATION,                ResourcesCompat.getFont(this,www.sanju.motiontoast.R.font.helvetica_regular))

                    }
                    saveImageToInternalStorage==null->{
                        MotionToast.createColorToast(this@AddHappyPlaceActivity,"Please select a image","",MotionToastStyle.INFO,MotionToast.GRAVITY_BOTTOM,MotionToast.SHORT_DURATION,                ResourcesCompat.getFont(this,www.sanju.motiontoast.R.font.helvetica_regular))

                    }
                    else->{
                        val happyPlaceModal=HappyPlaceModel(if(mHappyPlaceDetails==null)0 else mHappyPlaceDetails!!.id,binding.etTitle.text.toString(),saveImageToInternalStorage.toString(),binding.etDescription.text.toString(),
                        binding.etDate.text.toString(),binding.etLocation.text.toString(),mLatitude,mLongitude)
                        val dbHandler=DatabaseHandler(this)
                        if(mHappyPlaceDetails==null){
                        val addHappyPlace=dbHandler.addHappyPlace(happyPlaceModal)
                        if(addHappyPlace>0){
                            setResult(Activity.RESULT_OK)
                            finish()
                        }}else{
                            val updateHappyPlace=dbHandler.UpdateHappyPlace(happyPlaceModal)
                            if(updateHappyPlace>0){
                                setResult(Activity.RESULT_OK)
                                finish()
                        }



                    }
            }
            }
        }}}


    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== Activity.RESULT_OK){
            if(requestCode== GALLERY){
                if(data!=null){
                    val contentURI=data.data
                    try{
                        val selectedImageBitmap=MediaStore.Images.Media.getBitmap(this.contentResolver,contentURI)
                        binding.ivPlaceImage.setImageBitmap(selectedImageBitmap)
                       saveImageToInternalStorage= saveImageToInternalStorage(selectedImageBitmap)
                        Log.e("saved image:","path::$saveImageToInternalStorage")
                    }catch(e:IOException){
                        e.printStackTrace()
                        MotionToast.createColorToast(this@AddHappyPlaceActivity,"Error","Failed to load image from gallery",MotionToastStyle.ERROR,MotionToast.GRAVITY_BOTTOM,MotionToast.SHORT_DURATION, ResourcesCompat.getFont(this,www.sanju.motiontoast.R.font.helvetica_regular))
                    }
                }
            }else if(requestCode== CAMERA){
                val thumbnail:Bitmap=data!!.extras!!.get("data") as Bitmap
                binding.ivPlaceImage.setImageBitmap(thumbnail)
                 saveImageToInternalStorage= saveImageToInternalStorage(thumbnail)
                Log.e("saved image:","path::$saveImageToInternalStorage")

            }
        }
    }
    private fun takePhotoFromCamera(){
        Dexter.withContext(this@AddHappyPlaceActivity).withPermissions(
            android.Manifest.permission.READ_EXTERNAL_STORAGE
            , android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ,android.Manifest.permission.CAMERA
        ).withListener(object :MultiplePermissionsListener{
            override fun  onPermissionsChecked(report:MultiplePermissionsReport?) {
                if(report!!.areAllPermissionsGranted()){
                    val galleryIntent=Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(galleryIntent, CAMERA)

                }
            }
            override fun  onPermissionRationaleShouldBeShown(permissions:MutableList<PermissionRequest>? ,token: PermissionToken? ) {
                token?.continuePermissionRequest()
                showRationalDialogForPermissions()
            }
        }).withErrorListener { Toast.makeText(this@AddHappyPlaceActivity, it.name, Toast.LENGTH_SHORT).show() }.check();

    }

    private fun choosePhotoFromGallery() {
        Dexter.withContext(this@AddHappyPlaceActivity).withPermissions(
            android.Manifest.permission.READ_EXTERNAL_STORAGE
            , android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object :MultiplePermissionsListener{
            override fun  onPermissionsChecked(report:MultiplePermissionsReport?) {
                if(report!!.areAllPermissionsGranted()){
                    val galleryIntent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, GALLERY)

                }
            }
            override fun  onPermissionRationaleShouldBeShown(permissions:MutableList<PermissionRequest>? ,token: PermissionToken? ) {
                token?.continuePermissionRequest()
                showRationalDialogForPermissions()
            }
        }).withErrorListener { Toast.makeText(this@AddHappyPlaceActivity, it.name, Toast.LENGTH_SHORT).show() }.check();
        }

    private fun showRationalDialogForPermissions() {
        androidx.appcompat.app.AlertDialog.Builder(this).setMessage("Please give the permissions for this feature.Please enable it from the application settings").setPositiveButton("Go to Settings"){
            _,_ ->
            try{
                val intent= Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri= Uri.fromParts("package",packageName,null)
                intent.data=uri
                startActivity(intent)
            }catch (e:ActivityNotFoundException){
                e.printStackTrace()
            }
        }.setNegativeButton("Cancel"){dialog,_->
            dialog.dismiss()
        }.show()
    }


    private fun updateDateInView(){
        val myFormat="dd.MM.yyyy"
        val sdf=SimpleDateFormat(myFormat, Locale.getDefault())
        binding.etDate.setText(sdf.format(cal.time).toString())

    }
    private fun saveImageToInternalStorage(bitmap: Bitmap):Uri{
       val wrapper=ContextWrapper(applicationContext)
       var file=wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file= File(file,"${UUID.randomUUID()}.jpg")
        try{
            val stream:OutputStream=FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
            stream.flush()
            stream.close()
        }catch(e:IOException){
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }
    companion object{
        private const val GALLERY=1
        private const val CAMERA=2
        private const val IMAGE_DIRECTORY="HappyPlacesImages"
    }
}