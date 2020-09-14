package incubator.ui.bean;

import java.awt.Component;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;

/**
 * Provider that shows an image. The value should be an integer number
 * which is mapped into images using hints. Each hint has the following name:
 * <code>img-rsrc.X</code> where <code>X</code> is the image number. The value
 * for each hint is the resource that is loaded with the image.
 */
public class ImageComponentProvider implements BeanTableComponentProvider {
	/**
	 * Prefix for the image hint.
	 */
	public static final String HINT_PREFIX = "img-rsrc.";
	
	/**
	 * Label used to display the image.
	 */
	private JLabel m_label;

	/**
	 * Maps numbers into images.
	 */
	private Map<Integer, Image> m_images;
	
	/**
	 * Creates a new provider.
	 */
	public ImageComponentProvider() {
		m_label = new JLabel();
		m_label.setOpaque(true);
		m_images = new HashMap<>();
	}
	
	@Override
	public Component getComponentForBean(JTable table, BeanRendererInfo info,
			boolean is_selected, boolean has_focus, int row, int column) {
		m_label.setText("");
		m_label.setIcon(null);
		Image img = null;
		
		if (info.value() != null && info.value() instanceof Integer) {
			img = m_images.get(info.value());

			if (img == null) {
				String rsrc = info.hint(HINT_PREFIX + info.value());
				if (rsrc != null) {
					URL imgRsrc = getClass().getResource(rsrc);
					if (imgRsrc != null) {
						img = Toolkit.getDefaultToolkit().getImage(imgRsrc);
					}
					
				}
				
				if (img != null) {
					m_images.put((Integer) info.value(), img);
				}
			}
		}
		
		if (img != null) {
			m_label.setIcon(new ImageIcon(img));
		} else {
			m_label.setText("## error ##: image not found: " + info.value());
		}
		
		ProviderUtils.set_text_selection_colors(table, m_label, is_selected);
		return m_label;
	}
}
