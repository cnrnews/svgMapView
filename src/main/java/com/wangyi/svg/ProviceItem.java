package com.wangyi.svg;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;

/**
 * @Author lihl
 * @Date 2022/2/11 9:03
 * @Email 1601796593@qq.com
 *
 *  表示 SVG 上的一块区域
 */
public class ProviceItem {

    // 区域路径
    private Path path;

    // 区块背景着色
    private int drawColor;

    public ProviceItem(Path path) {
        this.path = path;
    }

    public void setDrawColor(int drawColor) {
        this.drawColor = drawColor;
    }

    /**
     * 给指定区域着色
     * @param canvas
     * @param paint
     * @param isSelect 是否选中
     */
    public void drawItem(Canvas canvas, Paint paint, boolean isSelect) {
        if (isSelect){
            // 给选中区域着色
            paint.clearShadowLayer();
            paint.setStrokeWidth(1);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(drawColor);
            canvas.drawPath(path,paint);

            // 绘制边框
            paint.setStyle(Paint.Style.STROKE);
            int strokeColor = 0xFFD0E8F4;
            paint.setColor(strokeColor);
            canvas.drawPath(path,paint);
        }else{
            // 给选中区域着色
            paint.setStrokeWidth(2);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.BLACK);
            paint.setShadowLayer(8,0,0,0xffffff);
            canvas.drawPath(path,paint);

            // 绘制边框
            paint.clearShadowLayer();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(drawColor);
            paint.setStrokeWidth(2);
            canvas.drawPath(path,paint);
        }
    }

    /**
     * 是否在点击区域
     * @param x
     * @param y
     * @return
     */
    public boolean isTouch(float x, float y) {
        RectF rectF = new RectF();
        path.computeBounds(rectF,true);

        Region region = new Region();
        region.setPath(path,new Region((int)rectF.left,(int)rectF.top,(int)rectF.right,(int)rectF.bottom));

        return region.contains((int)x,(int)y);
    }
}
