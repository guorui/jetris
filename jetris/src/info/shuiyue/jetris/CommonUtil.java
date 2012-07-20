package info.shuiyue.jetris;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;


public class CommonUtil {
	
	public static final int LEFT = 0;
	public static final int RIGHT = 1;
	public static final int UP = 2;
	public static final int DOWN = 3;
	
	private static final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
	
	public static Image loadImage(String fileName){
		
		InputStream in = classLoader.getResourceAsStream(fileName);
		Image img = null;
		
		try {
			img = ImageIO.read(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return img;
	}
}
