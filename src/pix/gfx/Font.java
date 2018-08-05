package pix.gfx;

public class Font {

	private Image fontImage;
	private int[] offsets = new int[256];
	private int[] widths = new int[256];
	private int size;
	
	public static final Font STANDARD = new Font("font/standard.png", 10);
	
	public Font(String path, int size) {
		this.size = size;
		
		fontImage = new Image(path);
		
		int unicode = 0;
		
		for(int i = 0; i < fontImage.getW(); i++) {
			if(fontImage.getP()[i] == 0xFF0000FF) {
				offsets[unicode] = i;
			}
			
			if(fontImage.getP()[i] == 0xFFFFFF00) {
				widths[unicode] = i - offsets[unicode];
				unicode++;
			}
		}
	}

	public Image getFontImage() {
		return fontImage;
	}

	public int[] getOffsets() {
		return offsets;
	}

	public int[] getWidths() {
		return widths;
	}
	
	public int getSize() {
		return size;
	}
	
}
