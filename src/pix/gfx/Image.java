package pix.gfx;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Image {
	
	private int w, h;
	private int[] p;
	private boolean alpha = false;
	
	private int lightBlock = Light.FULL;
	
	private BufferedImage source;
	
	public Image(String path) {
		source = null;
		
		try {
			source = ImageIO.read(Image.class.getClassLoader().getResourceAsStream(path));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			source = null;
		}
		
		if(source != null) {
			w = source.getWidth();
			h = source.getHeight();
			p = source.getRGB(0, 0, w, h, null, 0, w);
		} else {
			w = 1;
			h = 1;
			p = new int[1];
			p[0] = 0xFFFF00FF;
			source = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
			int[] pixels = ((DataBufferInt) source.getRaster().getDataBuffer()).getData();
			pixels[0] = 0xFFFF00FF;
		}
	}
	
	public Image(java.awt.Image image) {
		source = (BufferedImage) image;
		
		if(source != null) {
			w = source.getWidth();
			h = source.getHeight();
			p = source.getRGB(0, 0, w, h, null, 0, w);
			source.flush();
		} else {
			w = 1;
			h = 1;
			p = new int[1];
			p[0] = 0xFFFF00FF;
			source = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
			int[] pixels = source.getRGB(0, 0, w, h, null, 0, w);
			pixels[0] = 0xFFFF00FF;
		}
	}
	
	public int getW() {
		return w;
	}

	public int getH() {
		return h;
	}

	public int[] getP() {
		return p;
	}
	
	public BufferedImage getSource() {
		return source;
	}

	public boolean isAlpha() {
		return alpha;
	}

	public void setAlpha(boolean alpha) {
		this.alpha = alpha;
	}

	public int getLightBlock() {
		return lightBlock;
	}

	public void setLightBlock(int lightBlock) {
		this.lightBlock = lightBlock;
	}
	
}
