package com.example.russp.colorwheel;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.Preference;
import android.util.DisplayMetrics;
import android.view.Display;
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
import android.view.WindowManager;
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
    private static final String COLOR_DIALOG_PREF_NAME = "COLOR_DIALOG_PREF_NAME";
    private static final String SAVED_RECENT_COLOR_LENGTH = "SAVED_RECENT_COLOR_LENGTH";
    private static final String SAVED_RECENT_COLOR_RETRIEVE_ = "SAVED_RECENT_COLOR_RETRIEVE_";


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
        static Paint selectedColor;
        static Paint wheelColor;
        static Paint crossHairColor;
        static Paint recentColor;
        static int radius = 360;
        static int selectedColorInt;
        static int[] recentColors;
        static float crossHairX = 0;
        static float crossHairY = 0;
        static float canvasTranslation = radius * 1.375f;
        static float displayR = radius / 4;
        static float displayX = (displayR * 1.25f) - canvasTranslation;
        static float displayY = (displayR * 1.25f) - canvasTranslation;

        private OnColorChangedListener listener;

        ColorWheelView(Context context, OnColorChangedListener listener, int color){
            super(context);
            this.listener = listener;

            selectedColor = new Paint(Paint.ANTI_ALIAS_FLAG);
            wheelColor = new Paint(Paint.ANTI_ALIAS_FLAG);
            crossHairColor = new Paint(Paint.ANTI_ALIAS_FLAG);
            recentColor = new Paint(Paint.ANTI_ALIAS_FLAG);

            selectedColorInt = color;
            selectedColor.setColor(selectedColorInt);
            selectedColor.setTextAlign(Paint.Align.CENTER);
            selectedColor.setTypeface(Typeface.DEFAULT_BOLD);
            selectedColor.setTextSize(50);

            SharedPreferences preferences = context.getSharedPreferences(COLOR_DIALOG_PREF_NAME, Context.MODE_PRIVATE);
            int arrLength = preferences.getInt(SAVED_RECENT_COLOR_LENGTH, 6);
            recentColors = new int[arrLength];

            for(int i = 0; i < arrLength; i++){
                recentColors[i] = preferences.getInt(SAVED_RECENT_COLOR_RETRIEVE_ + i, Color.LTGRAY);
            }

        }

        @Override
        protected void onDraw(Canvas canvas){
            canvas.translate(canvasTranslation, canvasTranslation);

            drawCircle(canvas);
            drawCrossHair(canvas);
            drawRecentColors(canvas);

            selectedColor.setColor(selectedColorInt);
            canvas.drawCircle(displayX, displayY, displayR, selectedColor);

            // Get the opposite color to show the "Save" text easily
            int neg = (0x00FFFFFF - selectedColor.getColor()) | 0xFF000000;
            selectedColor.setColor(neg);
            canvas.drawText("Save", displayX, displayY + (selectedColor.getTextSize() / 4), selectedColor);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event){
            float x = event.getX() - canvasTranslation;
            float y = event.getY() - canvasTranslation;
            float distance = getDistance(x, y);
            float hue = getAngle(x, y);


            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if(distance <= radius) {
                        selectedColor.setStyle(Paint.Style.FILL);
                        selectedColor.setStrokeWidth(5);
                        selectedColorInt = getColor(hue, distance / radius, 1);
                        invalidate();
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(distance <= radius) {
                        selectedColor.setStyle(Paint.Style.FILL);
                        selectedColor.setStrokeWidth(5);
                        selectedColorInt = getColor(hue, distance / radius, 1);
                        invalidate();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    float intersected = (float)Math.sqrt(((x - displayX) * (x - displayX)) + ((y - displayY) * (y - displayY)));
                    if(intersected < displayR){
                        listener.colorChanged(selectedColorInt);
                        invalidate();
                    }
                    else if(distance <= radius){
                        for(int i = recentColors.length - 1; i > 0; i--){
                            recentColors[i] = recentColors[i - 1];
                        }
                        recentColors[0] = selectedColorInt;
                        saveRecentColors();
                    }
                    else{
                        int width = radius / 6;
                        int height = radius / 6;
                        int sqx = -radius;
                        int sqy = radius + height;

                        for(int i = 0; i < recentColors.length; i++){
                            sqx = -radius + (width * 2 * i);
                            if(x >= sqx && x <= sqx + width && y >= sqy && y <= sqy + height){
                                selectedColorInt = recentColors[i];
                                float[] hsv = new float[3];
                                Color.colorToHSV(selectedColorInt, hsv);
                                setCrossHair(hsv);
                                invalidate();
                                return true;
                            }
                        }
                    }
                    break;
            }

            if(distance <= radius) {
                crossHairX = x;
                crossHairY = y;
            }
            return true;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMesaureSpect){
            setMeasuredDimension((int)(radius * 2.75f), (int)(radius * 2.75));
        }

        /**
         * Used to position the cross hair when the set color is given
         * from outside of the circle (recent color or on start of dialog)
         *
         * @param hsv (hue, saturation, value) of the color
         */
        private void setCrossHair(float[] hsv){
            float hue = hsv[0];
            float saturation = hsv[1];
            float distance = saturation * radius;

            // Convert to radians from degrees
            hue = hue * (float)Math.PI / 180f;

            // Make sure to negate the distance because the wheel is rotated 180 degrees
            crossHairX = (float)Math.cos((double)hue) * -distance;
            crossHairY = (float)Math.sin((double)hue) * -distance;

        }

        /**
         * Draws the wheel of colors and the ring around it
         *
         * @param canvas to draw on
         */
        private void drawCircle(Canvas canvas) {
            wheelColor.setStrokeWidth(10);
            wheelColor.setStyle(Paint.Style.FILL_AND_STROKE);

            for (int x = -radius; x < radius; x+=10) {
                for (int y = -radius; y < radius; y+=10) {
                    // The distance from the center
                    float distance = getDistance(x, y);

                    // Skip points outside of the circle
                    if(distance > radius)
                        continue;

                    float hue = getAngle(x, y);
                    double saturation = distance / radius;

                    wheelColor.setColor(getColor(hue, (float)saturation, 1));
                    canvas.drawRect(x, y, x + 10, y + 10, wheelColor);

                }
            }

            // Draw the outer ring that shows the current color
            wheelColor.setStyle(Paint.Style.STROKE);
            wheelColor.setColor(selectedColorInt);
            wheelColor.setStrokeWidth(36);
            canvas.drawCircle(0, 0, radius, wheelColor);
        }

        /**
         * Draws a cross hair at the location of the current color
         *
         * @param canvas to draw on
         */
        private void drawCrossHair(Canvas canvas){
            crossHairColor.setColor(Color.LTGRAY);
            crossHairColor.setStyle(Paint.Style.FILL);
            crossHairColor.setStrokeWidth(5);

            // Draw lines for the cross hair
            canvas.drawLine(crossHairX, crossHairY, crossHairX + 20, crossHairY, crossHairColor);
            canvas.drawLine(crossHairX, crossHairY, crossHairX - 20, crossHairY, crossHairColor);
            canvas.drawLine(crossHairX, crossHairY, crossHairX, crossHairY + 20, crossHairColor);
            canvas.drawLine(crossHairX, crossHairY, crossHairX, crossHairY - 20, crossHairColor);

            // Draw the ring around the cross hair
            crossHairColor.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(crossHairX, crossHairY, 10, crossHairColor);
        }

        /**
         * Draws the most recently used colors
         *
         * @param canvas to draw on
         */
        private void drawRecentColors(Canvas canvas){
            int width = radius / 6;
            int height = radius / 6;
            int x = -radius;
            int y = radius + height;

            for(int i = 0; i < recentColors.length; i++){
                recentColor.setStrokeWidth(10);
                recentColor.setStyle(Paint.Style.FILL);
                recentColor.setColor(recentColors[i]);
                x = -radius + (width * 2 * i);
                canvas.drawRect(new Rect(x, y, x + width, y + height), recentColor);
            }
        }

        /**
         * Saves the recent colors so they can be accessed when opened again
         */
        private void saveRecentColors(){
            SharedPreferences.Editor editor = getContext().getSharedPreferences(COLOR_DIALOG_PREF_NAME, Context.MODE_PRIVATE).edit();
            editor.putInt(SAVED_RECENT_COLOR_LENGTH, recentColors.length);
            for(int i = 0; i < recentColors.length; i++){
                editor.putInt(SAVED_RECENT_COLOR_RETRIEVE_ + i, recentColors[i]);
            }
            editor.apply();
        }

        /**
         * Get the length of the vector (x, y)
         *
         * @param x how far in the x direction it goes
         * @param y how far in the y direction it goes
         *
         * @return the distance the vector goes
         */
        private float getDistance(float x, float y){
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
        private float getAngle(float x, float y){
            double angle = Math.atan2(y, x);
            return (float) (((angle + Math.PI) / (2 * Math.PI)) * 360);
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
        private int getColor(float hue, float saturation, float value) {
            // Calculate the values to assign
            float chroma = value * saturation;

            // Get hue prime
            float hue1 = Math.abs(hue / 60);
            float x = chroma * (1 - Math.abs((hue1 % 2) - 1));

            // Set all to 0 (black) by default
            float r1 = 0;
            float g1 = 0;
            float b1 = 0;

            if (hue1 >= 0 && hue1 <= 1) {
                r1 = chroma;
                g1 = x;
                b1 = 0;
            } else if (hue1 >= 1 && hue1 <= 2) {
                r1 = x;
                g1 = chroma;
                b1 = 0;
            } else if (hue1 >= 2 && hue1 <= 3) {
                r1 = 0;
                g1 = chroma;
                b1 = x;
            } else if (hue1 >= 3 && hue1 <= 4) {
                r1 = 0;
                g1 = x;
                b1 = chroma;
            } else if (hue1 >= 4 && hue1 <= 5) {
                r1 = x;
                g1 = 0;
                b1 = chroma;
            } else if (hue1 >= 5 && hue1 <= 6) {
                r1 = chroma;
                g1 = 0;
                b1 = x;
            }

            float m = value - chroma;
            // Calculate RGB values and convert from decimals
            float red = r1 + m;
            float green = g1 + m;
            float blue = b1 + m;

            return Color.argb(0xFF, (int)(red * 255), (int)(green * 255), (int)(blue * 255));
        }
    }
}
