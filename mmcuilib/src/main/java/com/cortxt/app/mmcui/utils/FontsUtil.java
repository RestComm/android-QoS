package com.cortxt.app.mmcui.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.Button;
import android.widget.TextView;

import com.cortxt.app.mmcui.R;

public class FontsUtil {
	
	public  static void applyFontToTextView(String font,TextView textView,Context context){
		Typeface typeFace=getCustomFont(font, context);
		//Typeface typeFace=Typeface.createFromAsset(context.getAssets(), "fonts/"+fontface + font + ext);
		if (textView != null)
			textView.setTypeface(typeFace);
	}
	public  static void applyFontToButton(String font,Button button,Context context){
		Typeface typeFace=getCustomFont(font, context);
		if (button != null)
			button.setTypeface(typeFace);
	}
	public static Typeface getCustomFont (String font, Context context){
		String customFont = (context.getResources().getString(R.string.CUSTOM_FONT));
		String fontface = "Roboto.ttf";
		if (customFont.length() > 1)
			fontface = customFont;
		String ext = fontface.substring(fontface.indexOf('.'));
		fontface = fontface.substring(0, fontface.indexOf('.'));
		Typeface typeFace=Typeface.createFromAsset(context.getAssets(), "fonts/"+fontface + font + ext);
		return typeFace;
		
	}
}
