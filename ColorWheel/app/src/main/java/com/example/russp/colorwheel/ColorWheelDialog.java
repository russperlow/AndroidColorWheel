package com.example.russp.colorwheel;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

/**
 * Created by russp on 8/2/2017.
 */

public class ColorWheelDialog extends Dialog {

    public interface OnColorChangedListener{
        void colorChanged(int color);
    }

    private OnColorChangedListener listener;
    private int initialColor;

    public ColorWheelDialog(Context context, OnColorChangedListener listener, int initialColor){
        super(context);
        this.listener = listener;
        this.initialColor = initialColor;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        OnColorChangedListener colorChangedListener = new OnColorChangedListener() {
            @Override
            public void colorChanged(int color) {
                listener.colorChanged(color);
                dismiss();
            }
        };
        setContentView(new ColorWheelView(getContext(), colorChangedListener, initialColor));
        setTitle("Choose a Tag Color");
    }

    private static class ColorWheelView extends View
    {
        static Context context;
        static int radius = 360;
        static Paint selectedColor;
        static int selectedColorInt;
        private OnColorChangedListener listener;

        ColorWheelView(Context context, OnColorChangedListener listener, int color){
            super(context);
            this.listener = listener;
            selectedColor = new Paint(Paint.ANTI_ALIAS_FLAG);
            selectedColor.setColor(0);
        }

        @Override
        protected void onDraw(Canvas canvas){
            canvas.translate(radius, radius);
            drawCircle(canvas);
            selectedColor.setColor(selectedColorInt);
            canvas.drawCircle(canvas.getWidth() - radius - 100, canvas.getHeight() - radius - 100, 75, selectedColor);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event){
            float x = event.getX() - radius;
            float y = event.getY() - radius;
            float distance = getDistance(x, y);

            int hue = getAngle(x, y);
            selectedColor.setStyle(Paint.Style.FILL);
            selectedColor.setStrokeWidth(5);
            selectedColor.setColor(Color.BLACK);
//            selectedColorInt = getColor(hue, distance / radius, 1);
            invalidate();
            return true;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMesaureSpect){
            setMeasuredDimension((int)(radius * 2.5), (int)(radius * 2.5));
        }

        public void drawCircle(Canvas canvas) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

            for (int x = -radius; x < radius; x++) {
                for (int y = -radius; y < radius; y++) {
                    // The distance from the center
                    float distance = getDistance(x, y);

                    // Skip points outside of the circle
                    if(distance > radius)
                        continue;

                    int hue = getAngle(x, y);
                    double saturation = distance / radius;

                    paint.setColor(getColor(hue, (float)saturation, 1));
                    paint.setStrokeWidth(20);
                    paint.setStyle(Paint.Style.FILL);

//                    canvas.drawLine(0, 0, x, y, paint);
                    canvas.drawPoint(x, y, paint);

                }
            }
        }

        /**
         * Get the length of the vector (x, y)
         *
         * @param x how far in the x direction it goes
         * @param y how far in the y direction it goes
         *
         * @return the distance the vector goes
         */
        public float getDistance(float x, float y){
            return (float)Math.sqrt((x * x) + (y * y));
        }

        /**
         * Get the angle of a given point from (0, 0)
         *
         * @param x coordinate
         * @param y coordinate
         *
         * @return angle of a given point in degrees
         */
        public int getAngle(float x, float y){
            double angle = Math.atan2(y, x);
            return (int)(((angle + Math.PI) / (2 * Math.PI)) * 360);
        }

        /**
         * Get the HSV color (formulas from Wikipedia HSL and HSV)
         * and convert to rgb then to int
         *
         * @param hue        the angle of this colors location
         * @param saturation distance from the center
         * @param value      always 1 for now
         * @return the decimal value of a color
         */
        public int getColor(int hue, float saturation, float value) {
            // Calculate the values to assign
            float chroma = value * saturation;

            // Get hue prime
            hue = Math.abs(hue / 60);
            float x = chroma * (1 - Math.abs((hue % 2) - 1));

            // Set all to 0 (black) by default
            float r1 = 0;
            float g1 = 0;
            float b1 = 0;

            if (hue >= 0 && hue <= 1) {
                r1 = chroma;
                g1 = x;
                b1 = 0;
            } else if (hue >= 1 && hue <= 2) {
                r1 = x;
                g1 = chroma;
                b1 = 0;
            } else if (hue >= 2 && hue <= 3) {
                r1 = 0;
                g1 = chroma;
                b1 = x;
            } else if (hue >= 3 && hue <= 4) {
                r1 = 0;
                g1 = x;
                b1 = chroma;
            } else if (hue >= 4 && hue <= 5) {
                r1 = x;
                g1 = 0;
                b1 = chroma;
            } else if (hue >= 5 && hue <= 6) {
                r1 = chroma;
                g1 = 0;
                b1 = x;
            }

            float m = value - chroma;

            // Calculate RGB values and convert from decimals
            int red = (int) (r1 + m) * 255;
            int green = (int) (g1 + m) * 255;
            int blue = (int) (b1 + m) * 255;

            return Color.argb(1, red, green, blue);
        }
    }
}
