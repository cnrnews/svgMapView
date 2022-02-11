package com.wangyi.svg;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import androidx.annotation.Nullable;
import androidx.core.graphics.PathParser;

/**
 * @Author lihl
 * @Date 2022/2/11 8:57
 * @Email 1601796593@qq.com
 */
public class MapView extends View {

    private Context context;
    private Paint paint;

    private List<ProviceItem> itemList;
    private ProviceItem select;// 代表选中的区域

    private RectF totalRect;
    private float scale = 1.0f;// 缩放比例


    private int[] colorArray = new int[]{0xFF239BD7, 0xFF30A9E5, 0xFF80CBF1, 0xFFFFFFFF};
    public MapView(Context context) {
        super(context);
    }

    public MapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * 完成 SVG 读取绘制
     * @param context
     */
    private void init(Context context){
        this.context = context;
        paint = new Paint();
        paint.setAntiAlias(true);
        itemList = new ArrayList<>();
        localThread.start();
    }

    private Thread localThread = new Thread(){
        @Override
        public void run() {
            // 读取 SVG 封装成 ProviceItem 集合
            try {
                InputStream inputStream =
                        context.getResources().openRawResource(R.raw.china);

                // 获取 DocumentBuilderFactory 实例
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

                // 从 DocumentBuilderFactory 中获取 DocumentBuilder 实例
                DocumentBuilder builder = factory.newDocumentBuilder();

                // 解析数据流，得到 Document 实例
                Document document = builder.parse(inputStream);

                Element rootElement = document.getDocumentElement();

                // 开始解析SVG
                NodeList notdeList = rootElement.getElementsByTagName("path");
                float left = -1;
                float top = -1;
                float right = -1;
                float bottom = -1;

                // 防止多线程并发出错
                List<ProviceItem> list = new ArrayList<>();
                for (int i = 0; i < notdeList.getLength(); i++) {

                    // 解析 path
                    Element element = (Element) notdeList.item(i);
                    String pathData = element.getAttribute("android:pathData");
                    Path path =  PathParser.createPathFromPathData(pathData);

                    // 添加 ProviceItem 到集合
                    ProviceItem proviceItem = new ProviceItem(path);
                    proviceItem.setDrawColor(colorArray[i%4]);

                    RectF rectF = new RectF();
                    path.computeBounds(rectF,true);
                    left = left == -1? rectF.left : Math.min(left,rectF.left);
                    right = right == -1? rectF.right : Math.max(right,rectF.right);
                    top = top == -1? rectF.top : Math.min(top,rectF.top);
                    bottom = bottom == -1? rectF.bottom : Math.max(bottom,rectF.bottom);

                    list.add(proviceItem);
                }
                itemList = list;
                // 获取整个SVG 区域
                totalRect = new RectF(left,top,right,bottom);
                // 触发绘制
                Handler handler =new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        requestLayout();
                        invalidate();
                    }
                });
//                postInvalidate();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 计算缩放比例
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (totalRect!=null){
            double mapWidth = totalRect.width();
            scale = (float) (width / mapWidth);
        }
        Log.i("TAG","onMeasure >> "+scale);

        setMeasuredDimension(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height , MeasureSpec.EXACTLY));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (itemList!=null)
        {
            canvas.save();
            // 缩放画布
            canvas.scale(scale,scale);
            for (ProviceItem item : itemList) {
                if (item!=select){
                    // 绘制未选中的区域
                    item.drawItem(canvas,paint,false);
                }else{
                    // 绘制选中的区域
                    select.drawItem(canvas,paint,true);
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 触摸点坐标：除以缩放比例
        handleTouch(event.getX() / scale ,event.getY() / scale);
        return super.onTouchEvent(event);
    }

    private void handleTouch(float x, float y) {
        if (itemList == null)return;
        ProviceItem selectItem = null;
        for (ProviceItem item : itemList) {
            // 是否点击在某个区域
            if (item.isTouch(x,y)){
                selectItem = item;
            }
        }
        // 如果有选中，则触发绘制
        if (selectItem!=null){
            select = selectItem;
            postInvalidate();
        }
    }
}
