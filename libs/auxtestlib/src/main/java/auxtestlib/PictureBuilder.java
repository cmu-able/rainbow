package auxtestlib;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;

/**
 * Class that can be used to generate random images.
 */
public final class PictureBuilder {
	/**
	 * Utility class, no constructor.
	 */
	private PictureBuilder() {
		/*
		 * Utility class, no constructor.
		 */
	}

	/**
	 * Generates a totally random picture with the given width and height.
	 * 
	 * @param width the width
	 * @param height the height
	 * 
	 * @return a random picture
	 */
	public static BufferedImage generateFullRandomPicture(int width, int height) {
		if (width <= 0) {
			throw new IllegalArgumentException("width <= 0");
		}

		if (height <= 0) {
			throw new IllegalArgumentException("height <= 0");
		}

		BufferedImage bimg = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				Color c = pickRandomColor();
				bimg.setRGB(x, y, c.getRGB());
			}
		}

		return bimg;
	}

	/**
	 * Generates a random picture by random drawing figures (rectangles and
	 * circles) on a white canvas.
	 * 
	 * @param width the width of the canvas
	 * @param height the height of the canvas
	 * @param figures the number of figures to draw
	 * 
	 * @return the generated picture
	 */
	public static BufferedImage generateSemiRandomPicture(int width,
			int height, int figures) {
		if (width <= 0) {
			throw new IllegalArgumentException("width <= 0");
		}

		if (height <= 0) {
			throw new IllegalArgumentException("height <= 0");
		}

		BufferedImage bimg = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				bimg.setRGB(x, y, Color.WHITE.getRGB());
			}
		}

		Graphics g = bimg.createGraphics();
		for (int i = 0; i < figures; i++) {
			int ftype = RandomGenerator.randInt(2);

			int x = RandomGenerator.randInt(width - 1);
			int y = RandomGenerator.randInt(height - 1);
			int w = RandomGenerator.randInt(width - x);
			int h = RandomGenerator.randInt(height);
			Color c = pickRandomColor();
			g.setColor(c);

			switch (ftype) {
			case 0:
				/*
				 * Rectangle.
				 */
				g.fillRect(x, y, w, h);
				break;
			case 1:
				/*
				 * Circle.
				 */
				g.fillOval(x, y, w, h);
				break;
			default:
				assert false;
			}
		}

		return bimg;
	}

	/**
	 * Generates a random color.
	 * 
	 * @return a random color
	 */
	public static Color pickRandomColor() {
		int r = RandomGenerator.randInt(256);
		int g = RandomGenerator.randInt(256);
		int b = RandomGenerator.randInt(256);

		Color rgbColor = new Color(r, g, b);
		return rgbColor;
	}

	/**
	 * Main method that can be used to try the random picture generator.
	 * 
	 * @param args not used
	 */
	public static void main(String[] args) {
		JFrame f = new JFrame("Picture generator test");
		final int w = 300;
		final int h = 200;
		final BufferedImage[] img = new BufferedImage[1];
		img[0] = generateFullRandomPicture(w, h);
		final JComponent image = new JComponent() {
			/**
			 * Version for serialization.
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void paint(Graphics g) {
				g.drawImage(img[0], 0, 0, w, h, null);
			}
		};

		image.setMinimumSize(new Dimension(w, h));
		image.setMaximumSize(new Dimension(w, h));
		image.setPreferredSize(new Dimension(w, h));

		JButton newFullRandom = new JButton("Full Random");
		newFullRandom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				img[0] = generateFullRandomPicture(w, h);
				image.repaint();
			}
		});

		JButton newSemi3Random = new JButton("Semi 3 Random");
		newSemi3Random.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				img[0] = generateSemiRandomPicture(w, h, 3);
				image.repaint();
			}
		});

		JButton newSemi10Random = new JButton("Semi 10 Random");
		newSemi10Random.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				img[0] = generateSemiRandomPicture(w, h, 10);
				image.repaint();
			}
		});

		JToolBar tb = new JToolBar();
		tb.add(newFullRandom);
		tb.add(newSemi3Random);
		tb.add(newSemi10Random);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(image, BorderLayout.CENTER);
		panel.add(tb, BorderLayout.NORTH);

		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(panel, BorderLayout.CENTER);
		f.pack();
		f.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		f.setVisible(true);
	}
}
