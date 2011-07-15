package edu.arizona.simulator.ww2d.system;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import edu.arizona.simulator.ww2d.utils.enums.EmotionEnum;

/**
 * This method is responsible for the initial load of the
 * images and keeping them in memory so that you can refer 
 * to the images by a reference and not load multiple copies
 * of the same image.
 * @author wkerr
 *
 */
public class ImageManager {
    private static Logger logger = Logger.getLogger( ImageManager.class );

    private static ImageManager _inst;
    
    private Map<String,BufferedImage> _images;
	private GraphicsConfiguration     _gc;
	
	public ImageManager() { 
		_images = new HashMap<String,BufferedImage>();

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		_gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
		logger.debug("graphics context: " + _gc);
	}
	
	public static ImageManager inst() { 
		if (_inst == null)
			_inst = new ImageManager();
		return _inst;
	}
	
	/**
	 * This method returns the buffered image associated
	 * with the file name.  If it is already loaded then
	 * the original image is returned.  If it is not loaded
	 * then we load the image and return it.
	 * @param fileName
	 * @return
	 */
	public BufferedImage getImage(String fileName) { 
		BufferedImage bi = _images.get(fileName);
		if (bi != null)
			return bi;
		
		try { 
			bi = load(fileName);
			_images.put(fileName, bi);
		} catch (Exception e) { 
			logger.error("error loading image: " + fileName);
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return bi;
	}
	
	/**
	 * Similar to getImage, but in case we want to modify the
	 * image we actuall get a handle to a copy of the image.
	 * @param fileName
	 * @return
	 */
	public BufferedImage getImageCopy(String fileName) { 
		URL url = getClass().getClassLoader().getResource(fileName);
		BufferedImage bi = _images.get(url);
		if (bi == null) { 
			try { 
				logger.debug("fileName: " + fileName);
				bi = load(fileName);
				_images.put(fileName, bi);
			} catch (Exception e) { 
				logger.error("error loading image: " + fileName);
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		}
		
		if (bi == null)
			return null;
		return makeCopy(bi);
	}
	
	private BufferedImage load(String fileName) throws Exception { 
		URL file = getClass().getClassLoader().getResource(fileName);
		BufferedImage im = ImageIO.read(new File(file.getFile()));
		return makeCopy(im);
	}
	
	private BufferedImage makeCopy(BufferedImage im) { 
		int transparency = im.getColorModel().getTransparency();
		BufferedImage copy = _gc.createCompatibleImage(im.getWidth(), im.getHeight(),
													   transparency);
			
		// create a graphics context
		Graphics2D g2d = copy.createGraphics();
		g2d.drawImage(im, 0, 0, null);
		g2d.dispose();
		return copy;
	}
	
	public void finish() { 
		_inst = null;
	}	
}