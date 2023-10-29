package kunal.project3.letzzdraw

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.Toast

class Draw(context:Context, attr:AttributeSet): View(context,attr) {
    private var kDrawPath:CustomPath?=null
    private var kCanvasBitMap:Bitmap?=null
    private var kDrawPaint: Paint?=null
    private var kCanvasPaint:Paint?=null
    private var kBrushSize:Float=0.toFloat()
    private var kcolor= Color.BLACK
    private var canvas: Canvas?=null
    private var kpath=ArrayList<Draw.CustomPath>()
    private var kpathRedo=ArrayList<Draw.CustomPath>()
    init {
        setUpDrawing()
    }
   private fun setUpDrawing(){
       kDrawPaint= Paint()
       kDrawPath=CustomPath(kcolor,kBrushSize)
       kDrawPaint!!.color=kcolor
       kDrawPaint!!.style=Paint.Style.STROKE
       kDrawPaint!!.strokeCap=Paint.Cap.ROUND
       kDrawPaint!!.strokeJoin=Paint.Join.ROUND
       kCanvasPaint=Paint(Paint.ANTI_ALIAS_FLAG)
   }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        kCanvasBitMap=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        canvas=Canvas(kCanvasBitMap!!)
    }
    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        canvas?.drawBitmap(kCanvasBitMap!!,0f,0f,kCanvasPaint)

        for(path in kpath){
            kDrawPaint!!.strokeWidth=path.brushThickness
            kDrawPaint!!.color=path.color
            canvas?.drawPath(path,kDrawPaint!!)
        }


        if(!kDrawPath!!.isEmpty){
            kDrawPaint!!.strokeWidth=kDrawPath!!.brushThickness
            kDrawPaint!!.color=kDrawPath!!.color
            canvas?.drawPath(kDrawPath!!,kDrawPaint!!)
        }
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX=event?.x
        val touchY=event?.y

        when(event?.action){
            MotionEvent.ACTION_DOWN->{
                kDrawPath!!.color=kcolor
                kDrawPath!!.brushThickness=kBrushSize

                kDrawPath!!.reset()
                if (touchX != null) {
                    if (touchY != null) {
                        kDrawPath!!.moveTo(touchX,touchY)
                    }
                }
            }
            MotionEvent.ACTION_MOVE->{
                if (touchX != null) {
                    if (touchY != null) {
                        kDrawPath!!.lineTo(touchX,touchY)
                    }
                }
            }
            MotionEvent.ACTION_UP->{
                kpath.add(kDrawPath!!)
                kDrawPath=CustomPath(kcolor,kBrushSize)
            }

            else-> return false

        }

        invalidate()
        return true
    }

fun undo(){
    if(kpath.isNotEmpty()) {
        kpathRedo.add(kpath.last())
        kpath.removeLast()
        invalidate()

    }
    else{
        Toast.makeText(context, "Canvas cleared buddy", Toast.LENGTH_SHORT).show()
    }
}
    fun redo(){
        if(kpathRedo.isNotEmpty()) {
            kpath.add(kpathRedo.last())
            kpathRedo.removeLast()
            invalidate()

        }
        else{
            Toast.makeText(context, "Nothing to redo", Toast.LENGTH_SHORT).show()
        }
    }




    fun clear(){
        if(kpath.isNotEmpty()) {
            kpath.clear()
            invalidate()
        }
        else{
            Toast.makeText(context, "Canvas cleared buddy", Toast.LENGTH_SHORT).show()
        }
    }


    fun setBrushSize(size:Float){
        kBrushSize=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,size,resources.displayMetrics)
        kDrawPaint!!.strokeWidth=kBrushSize
    }

    fun setBrushColor(color:Int){
        kcolor=color
    }

    inner class CustomPath(var color:Int, var brushThickness:Float) :Path(){

    }

    }
