package incubator.ui;

import javax.swing.ImageIcon;

/**
 * Class providing icons with circles of several colors.
 */
public class ColorCircles {
	/**
	 * Known circle colors.
	 */
	public enum Color {
		/**
		 * Blue color.
		 */
		BLUE,
		
		/**
		 * Green color.
		 */
		GREEN,
		
		/**
		 * Grey color.
		 */
		GREY,
		
		/**
		 * Orange color.
		 */
		ORANGE,
		
		/**
		 * Red color.
		 */
		RED,
		
		/**
		 * Yellow color.
		 */
		YELLOW
	}
	
	/**
	 * Loads a color icon of a given size.
	 * @param c the color
	 * @param size the size
	 * @return the icon or <code>null</code> if not found
	 */
	public static ImageIcon get_icon(Color c, int size) {
		return IconResourceLoader.loadIcon(ColorCircles.class, "circ-" +
				c.name().toLowerCase() + "-" + size + ".png");
	}
	
	/**
	 * Obtains the attribution text of this icons.
	 * @return the attribution
	 */
	public static String attribution() {
		return "Icons from \"Vista Style Base Software\" by "
				+ "http://www.icons-land.com";
		
	}
}
