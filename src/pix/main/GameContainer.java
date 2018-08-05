package pix.main;

import pix.util.Logger;

public class GameContainer implements Runnable {
	
	private Thread thread;
	private Window window;
	private Renderer renderer;
	private Input input;
	private PixGame game;
	
	private boolean running = false;
	private final double UPDATE_CAP = 1.0/60.0;
	private int width = 320, height = 240;
	private float scale = 1f;
	private String title = "Pix Engine v0.0.1";
	
	private Logger logger = new Logger("pix-engine"); // Allow for the Pix Engine to log things.
	
	public GameContainer(PixGame game) {
		this.game = game;
		logger.log("Engine initialized");
	}
	
	public void start() {
		logger.log("Engine starting");
		window = new Window(this); // Create the window and show it.
		renderer = new Renderer(this);
		input = new Input(this);
		
		logger.log("Components instantiated");
		logger.log("Starting thread");
		
		thread = new Thread(this);
		thread.setName("pix-engine-thread"); // Specify the thread for debugging
		thread.setDaemon(false); // Keep JVM alive while thread is running.
		thread.run(); // Execute as main thread. Runnable calls the run() method.
	}
	
	public void stop() {
		running = false;
		
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		logger.log("PixEngine finished. (exit code 0)");
		System.exit(0);
	}
	
	@Override
	public void run() {
		running = true;
		
		boolean render = false;
		
		double firstTime = 0;
		double lastTime = System.nanoTime() / 1000000000.0;
		double passedTime = 0;
		double delta = 0;
		
		double frameTime = 0;
		int frames = 0;
		int fps = 0;
		
		game.init(this);
		
		while(running) {
			render = false;
			
			firstTime = System.nanoTime() / 1000000000.0;
			passedTime = firstTime - lastTime;
			lastTime = firstTime;
			
			delta += passedTime;
			frameTime += passedTime;
			
			while(delta >= UPDATE_CAP) {
				delta -= UPDATE_CAP;
				
				render = true;
				
				game.update(this, (float)UPDATE_CAP);
				
				input.update();
				
				if(frameTime >= 1.0) {
					frameTime = 0;
					fps = frames;
					frames = 0;
					
					logger.log("FPS: " + fps);
				}
			}
			
			if(render) {
				renderer.clear();
				game.render(this, renderer);
				
				//renderer.drawText("FPS: " + fps, 10, 10, 0xFF00FFFF);
				
				renderer.process();
				
				window.update();
				frames++;
			} else {
				try {
					Thread.sleep(3);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		stop();
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setWindowSizeUsingAlgorithm(int width) {
		this.width = width;
		this.height = width * 3 / 4;
	}

	public Window getWindow() {
		return window;
	}

	public Input getInput() {
		return input;
	}

}
