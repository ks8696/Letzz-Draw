package kunal.project3.letzzdraw

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import yuku.ambilwarna.AmbilWarnaDialog
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {
    private var draw:Draw?=null
    private var num=20
    var tempcolor:Int=Color.BLACK
    private var clearButton:Button?=null
    private var check:Boolean=true
    private var check1:Boolean=true
    private var customProgressDialog:Dialog?=null

    val gallaryLauncher:ActivityResultLauncher<Intent> =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
               result->
                if(result.resultCode== RESULT_OK && result.data!=null){
                val imageBackground:ImageView=findViewById(R.id.backImage)
                imageBackground.setImageURI(result.data?.data)
                }
            }


    val permissionCheck:ActivityResultLauncher<Array<String>> =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
                permissions ->
                permissions.entries.forEach{
                    val isPermission=it.key
                    val isGranted=it.value

                    if(isGranted && isPermission==Manifest.permission.READ_EXTERNAL_STORAGE){
                        val pickIntent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        gallaryLauncher.launch(pickIntent)

                    }
                    else if(isGranted && isPermission==Manifest.permission.WRITE_EXTERNAL_STORAGE){
                        Toast.makeText(this@MainActivity, "File Saved Successfully", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        Toast.makeText(this, "Permission not Granted", Toast.LENGTH_SHORT).show()
                    }
                }
            }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        clearButton=findViewById(R.id.btn1)
       val eraser:ImageButton=findViewById(R.id.btn0)
        draw=findViewById(R.id.drawing)

        val saveButton:ImageButton=findViewById(R.id.share)
        saveButton.setOnClickListener{
            if(isReadGranted()){
                progressDialog()
                lifecycleScope.launch {
                    val frameLay:FrameLayout=findViewById(R.id.frame)
                    saveBitmap(returnBitmap(frameLay))
                }
            }
            else{
                grantPermission(0)
            }
        }

        val sizeButton:ImageButton=findViewById(R.id.btn2)
        sizeButton.setOnClickListener{
            draw?.setBrushColor(Color.BLACK)
            sizeButton.background=ContextCompat.getDrawable(this,R.drawable.img_5)
            eraser.background=ContextCompat.getDrawable(this,R.drawable.img)
        }
        sizeButton.setOnLongClickListener{chooseBrushSizeDialog()}


        val infoButton:ImageButton=findViewById(R.id.info)
        infoButton.setOnClickListener{ infoDialog() }

        val colorButton:ImageButton=findViewById(R.id.btn3)

        colorButton.setOnClickListener{
            if(check1)
            colorChooser()
            else
                grantPermission(1)
        }
        colorButton.setOnLongClickListener {
            if (check1){
                colorButton.background=ContextCompat.getDrawable(this,R.drawable.img_11)
                check1=false
            }else{
                colorButton.background=ContextCompat.getDrawable(this,R.drawable.img_1)
                check1=true
            }
            true
        }

        clearButton!!.setOnClickListener{
            if(check)
            draw?.undo()
            else
                draw?.redo()
        }
        clearButton!!.setOnLongClickListener {undoFun()}




        eraser.setOnLongClickListener {
           val img:ImageView= findViewById(R.id.backImage)
            img.setImageResource(R.drawable.drawing_background)
            draw!!.clear()
            true }
        eraser.setOnClickListener{
            draw?.setBrushColor(Color.WHITE)
            eraser.background=ContextCompat.getDrawable(this,R.drawable.img_6)
            sizeButton.background=ContextCompat.getDrawable(this,R.drawable.img_2)
        }

        draw?.setBrushSize(num.toFloat())


    }
    private fun chooseBrushSizeDialog():Boolean{
        val sizeDialog=Dialog(this)
        sizeDialog.setContentView(R.layout.brush_size)
        sizeDialog.setTitle("Brush Size")
        val text:TextView=sizeDialog.findViewById(R.id.text)
        val slide:SeekBar=sizeDialog.findViewById(R.id.sizeSlide)
        val button:Button=sizeDialog.findViewById(R.id.btn)
        val smallButton:ImageButton=sizeDialog.findViewById(R.id.small_size)
        var num1=0
        slide.setProgress(num)
        text.text=num.toString()

        smallButton.setOnClickListener{
            num1=10
            slide.setProgress(num1)
            text.text=num1.toString()

        }
        val mediumButton:ImageButton=sizeDialog.findViewById(R.id.medium_size)
        mediumButton.setOnClickListener{
            num1=30
            slide.setProgress(num1)
            text.text=num1.toString()
        }
        val largeButton:ImageButton=sizeDialog.findViewById(R.id.large_size)
        largeButton.setOnClickListener{
            num1=50
            slide.setProgress(num1)
            text.text=num1.toString()
        }
        slide.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                text.text=p1.toString()
                num1=p1
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}

        })
        button.setOnClickListener{
            num=num1
            draw?.setBrushSize(num.toFloat())
            sizeDialog.dismiss()
        }
        sizeDialog.show()
        return false
    }
    private fun infoDialog(){
        val info=Dialog(this)
        info.setContentView(R.layout.info)
        info.setTitle("Info Window")
        val back:ImageButton=info.findViewById(R.id.back)
        val insta:ImageButton=info.findViewById(R.id.insta)
        val linked:ImageButton=info.findViewById(R.id.linkedin)
        val git:ImageButton=info.findViewById(R.id.git)
        back.setOnClickListener{
            info.dismiss()
        }
        insta.setOnClickListener{
            val openInsta= Intent(android.content.Intent.ACTION_VIEW)
            openInsta.data=Uri.parse("https://instagram.com/kunalbhardwaj.69?igshid=MzNlNGNkZWQ4Mg==")
            startActivity(openInsta)
        }

        linked.setOnClickListener{
            val openLinked= Intent(android.content.Intent.ACTION_VIEW)
            openLinked.data=Uri.parse("www.linkedin.com/in/kunal-sharma-a1284a219")
            startActivity(openLinked)
        }

        git.setOnClickListener{
            val openGit= Intent(android.content.Intent.ACTION_VIEW)
            openGit.data=Uri.parse("https://github.com/ks8696")
            startActivity(openGit)
        }
info.setCancelable(false)
        info.show()
    }

    private fun colorChooser(){
        var mDefaultColor=tempcolor

        val colorPickerDialogue = AmbilWarnaDialog(this, mDefaultColor,
            object : AmbilWarnaDialog.OnAmbilWarnaListener {
                override fun onCancel(dialog: AmbilWarnaDialog?) {}
                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                    mDefaultColor = color
                    tempcolor=mDefaultColor
                    draw?.setBrushColor(tempcolor)
                }
            }).dialog
        colorPickerDialogue.setCancelable(false)
        colorPickerDialogue.show()
    }

    fun undoFun():Boolean{
        if (check){
            clearButton!!.background=ContextCompat.getDrawable(this,R.drawable.img_4)
            check=false
        }
        else{
            clearButton!!.background=ContextCompat.getDrawable(this,R.drawable.back)
            check=true

        }
return true
    }

    fun isReadGranted():Boolean{
        val result=ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return result==PackageManager.PERMISSION_GRANTED
    }

    fun grantPermission(i:Int){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                show_rationale("Gallary Access","Letzz Draw wants to read your gallary to get the Image for background. You can grant this from app settings")

            }
            else if(i==1){
                permissionCheck.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
            }else{
                permissionCheck.launch(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
            }
    }



    fun show_rationale(title:String,message:String){
        val Alert:AlertDialog.Builder=AlertDialog.Builder(this)
        Alert.setTitle(title).setMessage(message).setNegativeButton("Cancel"){
            dialog,_->dialog.dismiss()
        }.setPositiveButton("OK"){
            dialog,_->openSettings()
            dialog.dismiss()
        }
        Alert.create().show()
    }

    fun openSettings(){
        intent=Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        var uri=Uri.fromParts("package",packageName,null)
        intent.setData(uri)
        startActivity(intent)
    }

    fun returnBitmap(view: View):Bitmap{
        val returnbit=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
        val canvas= Canvas(returnbit)
        val bgDrawable=view.background
        if(bgDrawable!=null){
            bgDrawable.draw(canvas)
        }
        else{
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return returnbit
    }

    private suspend fun saveBitmap(kBitmap:Bitmap?):String{
        var result=""
        withContext(Dispatchers.IO) {
            if (kBitmap != null) {
                try {
                    val bytes = ByteArrayOutputStream()
                    kBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                    val f =
                        File(externalCacheDir?.absoluteFile.toString() + File.separator + "LetzzDraw_" + System.currentTimeMillis() / 1000 + ".jpeg")
                    val fo = FileOutputStream(f)
                    fo.write(bytes.toByteArray())
                    fo.close()
                    result = f.absolutePath
                    runOnUiThread {
                        toCancelTheCustomDialog()
                        if (result.isNotEmpty()) {
                            val snackbar = Snackbar.make(findViewById(R.id.mainLayout), "Image Saved Successfully!!", Snackbar.LENGTH_LONG)
                                .setAction("SHARE") { share(result) }
                            snackbar.setTextColor(Color.parseColor("#12486B"))
                            snackbar.view.setBackgroundColor(Color.parseColor("#CAEDFF"))
                            val view = snackbar.view
                            val params = view.layoutParams as FrameLayout.LayoutParams
                            params.gravity = Gravity.TOP
                            view.layoutParams = params
                            snackbar.show()
                        } else {
                            Toast.makeText(this@MainActivity, "Something went wrong while saving the file", Toast.LENGTH_SHORT).show()
                        }
                    }
                }catch (e:Exception){
                    result=""
                    e.printStackTrace()
                }
            }
        }
        return result
    }

    fun progressDialog(){
        customProgressDialog=Dialog(this@MainActivity)
        customProgressDialog?.setContentView(R.layout.progress)
        customProgressDialog?.getWindow()?.setBackgroundDrawableResource(android.R.color.transparent);
        customProgressDialog?.getWindow()?.setDimAmount(0.toFloat());
        customProgressDialog?.show()
    }
    fun toCancelTheCustomDialog(){
        if (customProgressDialog!=null){
            customProgressDialog?.dismiss()
            customProgressDialog=null
        }
    }
    fun share(result:String){
        MediaScannerConnection.scanFile(this, arrayOf(result),null){
            path,uri->
            val shareIntent=Intent()
            shareIntent.action=Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM,uri)
            shareIntent.type="image/jpeg"
            shareIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            startActivity(Intent.createChooser(shareIntent,"Share"))
        }

    }
}
