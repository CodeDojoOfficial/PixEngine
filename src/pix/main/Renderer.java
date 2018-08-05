package pix.main;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import pix.gfx.Font;
import pix.gfx.Image;
import pix.gfx.ImageTile;
import pix.gfx.Light;

public class Renderer {
	
	private int pW, pH;
	private int[] p;
	
	private int[] lm;
	private int[] lb;
	
	private int ambientColor = 0xFFDDDDDD;
	
	private Font font = Font.STANDARD;
	
	public Renderer(GameContainer gc) {
		pW = gc.getWidth();
		pH = gc.getHeight();
		p = ((DataBufferInt) gc.getWindow().getImage().getRaster().getDataBuffer()).getData();
		lm = new int[p.length];
		lb = new int[p.length];
	}
	
	public void clear() {
		for(int i = 0; i < p.length; i++) {
			p[i] = 0;
			lm[i] = ambientColor;
			lb[i] = 0;
		}
	}
	
	public void process() {
		for(int i = 0; i < p.length; i++) {
			float r = ((lm[i] >> 16) & 0xFF) / 255f;
			float g = ((lm[i] >> 8) & 0xFF) / 255f;
			float b = (lm[i] & 0xFF) / 255f;
			
			p[i] = ((int)(((p[i] >> 16) & 0xFF) * r) << 16 | (int)(((p[i] >> 8) & 0xFF) * g) << 8 | (int)((p[i] & 0xFF) * b));
		}
	}
	
	public void setPixel(int x, int y, int color) {
		int alpha = (color >> 24) & 0xFF;
		
		if((x < 0 || x >= pW || y < 0 || y >= pH) || alpha == 0) {
			return;
		}
		
		if(alpha == 255) {
			p[x + y * pW] = color;
		} else {
			int pixelColor = p[x + y * pW];
			
			int newRed = ((pixelColor >> 16) & 0xFF) - (int)((((pixelColor >> 16) & 0xFF) - ((color >> 16) & 0xFF)) * (alpha / 255f));
			int newGreen = ((pixelColor >> 8) & 0xFF) - (int)((((pixelColor >> 8) & 0xFF) - ((color >> 8) & 0xFF)) * (alpha / 255f));
			int newBlue = (pixelColor & 0xFF) - (int)(((pixelColor & 0xFF) - (color & 0xFF)) * (alpha / 255f));
			
			p[x + y * pW] = (newRed << 16 | newGreen << 8 | newBlue);
		}
	}
	
	public void setLightMap(int x, int y, int value) {
		if(x < 0 || x >= pW || y < 0 || y >= pH) {
			return;
		}
		
		int baseColor = lm[x + y * pW];
		
		int maxRed = Math.max(((baseColor >> 16) & 0xFF), ((value >> 16) & 0xFF));
		int maxGreen = Math.max(((baseColor >> 8) & 0xFF), ((value >> 8) & 0xFF));
		int maxBlue = Math.max(baseColor & 0xFF, value & 0xFF);
		
		lm[x + y * pW] = (maxRed << 16 | maxGreen << 8 | maxBlue);
	}
	
	public void setLightBlock(int x, int y, int value) {
		if(x < 0 || x >= pW || y < 0 || y >= pH) {
			return;
		}
		
		lb[x + y * pW] = value;
	}
	
	public void drawText(String text, int offX, int offY, int color) {
		int offset = 0;
		
		for(int i = 0; i < text.length(); i++) {
			int unicode = text.codePointAt(i);
			
			drawFontedImage(new Image(font.getFontImage().getSource().getSubimage(font.getOffsets()[unicode], 1, font.getWidths()[unicode], font.getFontImage().getH() - 1)), offX + offset, offY, font.getSize() / 9, color);
			
			offset += (font.getWidths()[unicode]) * (font.getSize() / 9);
		}
	}
	
	private void drawFontedImage(Image image, int offX, int offY, double scaleSize, int color) {
		// If scaleSize is 2, the font will have a doubled look.
		int newX = 0;
		int newY = 0;
		int newWidth = new Double(image.getW() * scaleSize).intValue();
		int newHeight = new Double(image.getH() * scaleSize).intValue();
		
		BufferedImage bi = new BufferedImage(newWidth,
				newHeight,
				BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g = (Graphics2D) bi.getGraphics();
		
		g.scale(scaleSize, scaleSize);
		g.drawImage(image.getSource(), 0, 0, null);
		g.dispose();
		
		image = new Image(bi);
		
		// "Don't Render" code
		if(offX < -newWidth) return;
		if(offY < -newHeight) return;
		if(offX >= pW) return;
		if(offY >= pH) return;
		
		// "Clipping" code
		if(offX < 0) newX -= offX;
		if(offY < 0) newY -= offY;
		if(newWidth + offX >= pW) newWidth -= newWidth + (offX - pW);
		if(newHeight + offY >= pH) newHeight -= newWidth + (offY - pH);
		
		for(int y = newY; y < newHeight; y++) {
			for(int x = newX; x < newWidth; x++) {
				if(image.getP()[x + y * image.getW()] == 0xFFFFFFFF)
					setPixel(x + offX, y + offY, color);
			}
		}
	}
	
	public void drawImage(Image image, int offX, int offY) {
		int newX = 0;
		int newY = 0;
		int newWidth = image.getW();
		int newHeight = image.getH();
		
		// "Don't Render" code
		if(offX < -newWidth) return;
		if(offY < -newHeight) return;
		if(offX >= pW) return;
		if(offY >= pH) return;
		
		// "Clipping" code
		if(offX < 0) newX -= offX;
		if(offY < 0) newY -= offY;
		if(newWidth + offX >= pW) newWidth -= newWidth + (offX - pW);
		if(newHeight + offY >= pH) newHeight -= newWidth + (offY - pH);
		
		for(int y = newY; y < newHeight; y++) {
			for(int x = newX; x < newWidth; x++) {
				setPixel(x + offX, y + offY, image.getP()[x + y * image.getW()]);
				setLightBlock(x + offX, y + offY, image.getLightBlock());
			}
		}
	}
	
	public void drawImage(Image image, int offX, int offY, int width, int height) {
		int newX = 0;
		int newY = 0;
		
		BufferedImage bi = new BufferedImage(width,
                height,
                BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g = (Graphics2D) bi.getGraphics();
		g.drawImage(image.getSource(), 0, 0, width, height, null);
		g.dispose();
		
		image = new Image(bi);
		
		// "Don't Render" code
		if(offX < -width) return;
		if(offY < -height) return;
		if(offX >= pW) return;
		if(offY >= pH) return;
		
		// "Clipping" code
		if(offX < 0) newX -= offX;
		if(offY < 0) newY -= offY;
		if(width + offX >= pW) width -= width + offX - pW;
		if(height + offY >= pH) height -= height + offY - pH;
		
		for(int y = newY; y < height; y++) {
			for(int x = newX; x < width; x++) {
				setPixel(x + offX, y + offY, image.getP()[x + y * image.getW()]);
				setLightBlock(x + offX, y + offY, image.getLightBlock());
			}
		}
	}
	
	public void drawImageTile(ImageTile image, int offX, int offY, int tileX, int tileY) {
		int newX = 0;
		int newY = 0;
		int newWidth = image.getTileW();
		int newHeight = image.getTileH();
		
		// "Don't Render" code
		if(offX < -newWidth) return;
		if(offY < -newHeight) return;
		if(offX >= pW) return;
		if(offY >= pH) return;
		
		// "Clipping" code
		if(offX < 0) newX -= offX;
		if(offY < 0) newY -= offY;
		if(newWidth + offX >= pW) newWidth -= newWidth + offX - pW;
		if(newHeight + offY >= pH) newHeight -= newHeight + offY - pH;
		
		for(int y = newY; y < newHeight; y++) {
			for(int x = newX; x < newWidth; x++) {
				setPixel(x + offX, y + offY, image.getP()[(x + tileX * image.getTileW()) + (y + tileY * image.getTileH()) * image.getW()]);
				setLightBlock(x + offX, y + offY, image.getLightBlock());
			}
		}
	}
	
	public void drawImageTile(ImageTile image, int offX, int offY, int width, int height, int tileX, int tileY) {
		BufferedImage bi = new BufferedImage(width,
                height,
                BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g = (Graphics2D) bi.getGraphics();
		g.drawImage(image.getSource(), 0, 0, width, height, null);
		g.dispose();
		
		drawImage(new Image(bi), offX, offY);
	}
	
	public void drawRect(int offX, int offY, int width, int height, int color) {
		for(int y = 0; y < height; y++) {
			setPixel(offX, y + offY, color);
			setPixel(offX + width, y + offY, color);
		}
		
		for(int x = 0; x < width; x++) {
			setPixel(x + offX, offY, color);
			setPixel(x + offX, offY + height, color);
		}
	}
	
	public void drawRect(int offX, int offY, int width, int height, int color, boolean lightBlock) {
		for(int y = 0; y < height; y++) {
			setPixel(offX, y + offY, color);
			setPixel(offX + width, y + offY, color);
			setLightBlock(offX, y + offX, (lightBlock) ? Light.NONE : Light.FULL);
			setLightBlock(offX + width, y + offY, (lightBlock) ? Light.NONE : Light.FULL);
		}
		
		for(int x = 0; x < width; x++) {
			setPixel(x + offX, offY, color);
			setPixel(x + offX, offY + height, color);
			setLightBlock(x + offX, offX, (lightBlock) ? Light.NONE : Light.FULL);
			setLightBlock(x + offX + width, offY, (lightBlock) ? Light.NONE : Light.FULL);
		}
	}
	
	public void drawFilledRect(int offX, int offY, int width, int height, int color) {
		int newX = 0;
		int newY = 0;
		int newWidth = width;
		int newHeight = height;
		
		// "Don't Render" code
		if(offX < -newWidth) return;
		if(offY < -newHeight) return;
		if(offX >= pW) return;
		if(offY >= pH) return;
		
		// "Clipping" code
		if(offX < 0) newX -= offX;
		if(offY < 0) newY -= offY;
		if(newWidth + offX >= pW) newWidth -= newWidth + (offX - pW);
		if(newHeight + offY >= pH) newHeight -= newWidth + (offY - pH);
		
		for(int y = newY; y < newHeight; y++) {
			for(int x = newX; x < newWidth; x++) {
				setPixel(x + offX, y + offY, color);
				
			}
		}
	}
	
	public void drawFilledRect(int offX, int offY, int width, int height, int color, boolean lightBlock) {
		int newX = 0;
		int newY = 0;
		int newWidth = width;
		int newHeight = height;
		
		// "Don't Render" code
		if(offX < -newWidth) return;
		if(offY < -newHeight) return;
		if(offX >= pW) return;
		if(offY >= pH) return;
		
		// "Clipping" code
		if(offX < 0) newX -= offX;
		if(offY < 0) newY -= offY;
		if(newWidth + offX >= pW) newWidth -= newWidth + (offX - pW);
		if(newHeight + offY >= pH) newHeight -= newWidth + (offY - pH);
		
		for(int y = newY; y < newHeight; y++) {
			for(int x = newX; x < newWidth; x++) {
				setPixel(x + offX, y + offY, color);
				setLightBlock(x + offX, y + offY, (lightBlock) ? Light.NONE : Light.FULL);
			}
		}
	}
	
	public void drawLight(Light light, int offX, int offY) {
		for(int i = 0; i <= light.getDiameter(); i++) {
			drawLightLine(light, light.getRadius(), light.getRadius(), i, 0, offX, offY);
			drawLightLine(light, light.getRadius(), light.getRadius(), i, light.getDiameter(), offX, offY);
			drawLightLine(light, light.getRadius(), light.getRadius(), 0, i, offX, offY);
			drawLightLine(light, light.getRadius(), light.getRadius(), light.getDiameter(), i, offX, offY);
		}
	}
	
	private void drawLightLine(Light light, int x0, int y0, int x1, int y1, int offX, int offY) {
		int dx = Math.abs(x1 - x0);
		int dy = Math.abs(y1 - y0);
		
		int sx = x0 < x1 ? 1 : -1;
		int sy = y0 < y1 ? 1 : -1;
		
		int err = dx - dy;
		int e2;
		
		while(true) {
			int screenX = x0 - light.getRadius() + offX;
			int screenY = y0 - light.getRadius() + offY;
			
			if(screenX < 0 || screenX >= pW || screenY < 0 || screenY >= pH)
				return;
			
			if(light.getLightValue(x0, y0) == 0)
				return;
			
			if(lb[screenX + screenY * pW] == Light.NONE)
				return;
			
			setLightMap(screenX, screenY, light.getLightValue(x0, y0));
			
			if(x0 == x1 && y0 == y1)
				break;
			
			e2 = 2 * err;
			
			if(e2 > -1 * dy) {
				err -= dy;
				x0 += sx;
			}
			
			if(e2 < dx) {
				err += dx;
				y0 += sy;
			}
		}
	}
	
	
	public void setFont(Font font) {
		this.font = font;
	}

	public int getAmbientColor() {
		return ambientColor;
	}

	public void setAmbientColor(int ambientColor) {
		this.ambientColor = ambientColor;
	}
	
}
