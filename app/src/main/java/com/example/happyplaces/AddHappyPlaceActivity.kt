package com.example.happyplaces

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Binder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.example.happyplaces.databinding.ActivityAddHappyPlaceBinding
import com.example.happyplaces.databinding.ActivityAddHappyPlaceBinding.inflate
import com.example.happyplaces.databinding.ActivityMainBinding.inflate
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityAddHappyPlaceBinding
    private var cal=Calendar.getInstance()
    private lateinit var dateSetListner: DatePickerDialog.OnDateSetListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityAddHappyPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener{
            onBackPressed()
        }
        dateSetListner= DatePickerDialog.OnDateSetListener{
            view, year, month, dayOfMonth ->  cal.set(Calendar.YEAR,year)
            cal.set(Calendar.MONTH,month)
            cal.set(Calendar.DAY_OF_MONTH,dayOfMonth)
            updateDateInView()
        }
        binding.etDate.setOnClickListener(this)
        binding.tvAddImage.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.et_date->{
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
                        1-> Toast.makeText(this@AddHappyPlaceActivity,"Camera section TODO",Toast.LENGTH_SHORT).show()
                    }
                }.show()

            }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== Activity.RESULT_OK){
            if(requestCode== GALLERY){
                if(data!=null){
                    val contentURI=data.data
                    try{
                        val selectedImageBitmap=MediaStore.Images.Media.getBitmap(this.contentResolver,contentURI)
                        binding.ivPlaceImage.setImageBitmap(selectedImageBitmap)
                    }catch(e:IOException){
                        e.printStackTrace()
                        MotionToast.createColorToast(this@AddHappyPlaceActivity,"Error","Failed to load image from gallery",MotionToastStyle.ERROR,MotionToast.GRAVITY_BOTTOM,MotionToast.SHORT_DURATION, ResourcesCompat.getFont(this,www.sanju.motiontoast.R.font.helvetica_regular))
                    }
                }
            }
        }
    }

    private fun choosePhotoFromGallery() {
        Dexter.withContext(this@AddHappyPlaceActivity).withPermissions(
            android.Manifest.permission.READ_EXTERNAL_STORAGE
            , android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object :MultiplePermissionsListener{
            override fun  onPermissionsChecked(report:MultiplePermissionsReport?) {
                if(report!!.areAllPermissionsGranted()){
                    val galleryIntent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent,GALLERY)

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
    companion object{
        private const val GALLERY=1
    }
}