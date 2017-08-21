package me.varunon9.fito;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by varun on 20/8/17.
 */

public class CanvasBoardView extends View {

    private Paint paint;
    private Bitmap bitmap;
    private Canvas canvas;
    private Path path;
    Context context;

    private void init() {
        paint = new Paint();
        path = new Path();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(ContextCompat.getColor(context, R.color.colorAccent));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(2);
    }

    public CanvasBoardView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public CanvasBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public CanvasBoardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLine(0, 0, 20, 20, paint);
        canvas.drawLine(20, 0, 0, 20, paint);
    }

}
