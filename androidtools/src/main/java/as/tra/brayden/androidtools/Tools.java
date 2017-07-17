package as.tra.brayden.androidtools;

import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.widget.Button;

/**
 * Created by Brayden on 6/13/2017.
 */

public abstract class Tools {

    public static String formatPhone(String input, String countryCode) {

        // get only numerics
        String number = getInt(input);

        // return invalid number
        if(number.length() < 10 || number.length() > 11)
            return number;

        // remove 1 if exists
        if(number.length() == 11)
            number = number.substring(1);

        String format = "(" + number.substring(0,3) + ") " + number.substring(3,6) + "-" + number.substring(6,10);

        // add country code if exists
        if(countryCode != null)
            format = countryCode + " " + format;

        return format;
    }

    public static String formatPhone(String input) {
        return formatPhone(input, null);
    }

    public static String formatPhoneLongDistance(String input){
        return formatPhone(input, "+1");
    }

    public static String getInt(String input) {
        return input.replaceAll("[^\\d.]", "");
    }


    private static int lighten(byte colorVal) {
        colorVal = (byte)255;
        int add = (255 - colorVal) / 2;
        return (byte)(colorVal + add);
    }

    public static void paintButton(@NonNull Button button, int color) {
        button.getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
    }

    public static void paintButtonText(@NonNull Button button, int buttonColor, int textColor) {
        button.getBackground().setColorFilter(buttonColor, PorterDuff.Mode.MULTIPLY);
        button.setTextColor(textColor);
    }


    public static void paintButtonTextEnabled(@NonNull Button button) {

        button.getBackground().clearColorFilter();
        button.setTextColor(ContextCompat.getColor(button.getContext(), R.color.black));

    }

    public static void paintButtonTextDisabled(@NonNull Button button) {
        paintButtonText(button,
                ContextCompat.getColor(button.getContext(), R.color.colorDisabledButton),
                ContextCompat.getColor(button.getContext(), R.color.colorGreyedOut));
    }


}
