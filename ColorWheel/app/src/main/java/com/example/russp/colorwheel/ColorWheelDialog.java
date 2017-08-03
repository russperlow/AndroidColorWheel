package com.example.russp.colorwheel;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
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

public class ColorWheelDialog extends DialogFragment {


    static Bitmap bitmap;
    static Context context;

    public ColorWheelDialog(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        final Activity activity = getActivity();
    }

    public void drawCanvas(){
        bitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);

        int radius = 25;
        for(int x = -radius; x < radius; x++){
            for(int y = -radius; y < radius; y++){
                int distance = (int)Math.sqrt(x*x+y*y);
                if(distance < radius){
                    int rowLength = 2 * radius;
                    int adjustX = x + radius;
                    int adjustY = y + radius;
                    int pixelWidth = 4;
                    int index = (adjustX + (adjustY * rowLength)) * pixelWidth;
                    int angle = (int)Math.atan(y/x);
                    bitmap.setPixel(x, y, getColor(angle, distance/radius, 1));
                }
            }
        }
    }

    /**
     * Get the HSV color (formulas from Wikipedia HSL and HSV)
     * and convert to rgb then to int
     *
     * @param hue the angle of this colors location
     * @param saturation distance from the center
     * @param value always 1 for now
     *
     * @return the decimal value of a color
     */
    public int getColor(int hue, float saturation, float value){
        // Calculate the values to assign
        float chroma = value * saturation;
        float x = chroma * (1 - Math.abs((hue % 2) - 1));

        // Get hue prime
        hue = hue / 60;

        // Set all to 0 (black) by default
        float r1 = 0;
        float g1 = 0;
        float b1 = 0;

        if(hue >= 0 && hue <= 1)
        {
            r1 = chroma;
            g1 = x;
            b1 = 0;
        }
        else if(hue >= 1 && hue <= 2){
            r1 = x;
            g1 = chroma;
            b1 = 0;
        }
        else if(hue >= 2 && hue <= 3){
            r1 = 0;
            g1 = chroma;
            b1 = x;
        }
        else if(hue >= 3 && hue <= 4){
            r1 = 0;
            g1 = x;
            b1 = chroma;
        }
        else if(hue >= 4 && hue <= 5){
            r1 = x;
            g1 = 0;
            b1 = chroma;
        }
        else if(hue >= 5 && hue <= 6){
            r1 = chroma;
            g1 = 0;
            b1 = x;
        }

        float m = value - chroma;

        // Calculate RGB values and convert from decimals
        int red = (int)(r1+m) * 255;
        int green = (int)(g1+m) * 255;
        int blue = (int)(b1+m) * 255;

        return (red * 65536) + (green * 256) + blue;
    }

}
