/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * For questions related to commercial use licensing, please contact sales@telestax.com.
 *
 */

package org.restcomm.app.utillib.Utils;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.Button;
import android.widget.TextView;

import com.cortxt.app.utillib.R;


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
