package org.sa.rainbow.configuration.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;


public class FontManager {
	
	
	static private Map<String, Font> s_fontMap; 
	

	protected FontManager() {}
	
	public static Font getFont(String name, int size, int style) {
		String key = name + size + style;
		if (s_fontMap == null) s_fontMap = new HashMap<>();
		Font f = s_fontMap.get(key);
		if (f == null) {
			FontData fd = new FontData(name, size, style);
			f = new Font(Display.getDefault(), fd);
			s_fontMap.put(key, f);
		}
		return f;
	}
}
