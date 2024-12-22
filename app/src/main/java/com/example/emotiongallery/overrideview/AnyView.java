package com.example.emotiongallery.overrideview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.InvocationTargetException;

public class AnyView extends View {

    private boolean canDraw = false;
    private String drawMethod;
    private final Paint paint = new Paint();
    private final Path path = new Path();

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (!canDraw) return;
        float x = getWidth();
        float y = getHeight();
        try {
            ViewDraw.draw(canvas, path, paint, x, y, drawMethod);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void setIconByName(String drawMethod) {
        this.drawMethod = drawMethod;
        canDraw = true;
        invalidate();
    }

    public AnyView(Context context) {
        super(context);
    }

    public AnyView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AnyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AnyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
