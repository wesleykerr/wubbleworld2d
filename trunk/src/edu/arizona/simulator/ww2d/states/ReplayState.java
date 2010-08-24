package edu.arizona.simulator.ww2d.states;

import java.awt.Point;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.fenggui.Container;
import org.fenggui.Display;
import org.fenggui.FengGUI;
import org.fenggui.composite.Window;
import org.fenggui.layout.FlowLayout;
import org.fenggui.layout.RowLayout;
import org.fenggui.theme.XMLTheme;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.util.Spacing;
import org.jbox2d.common.Vec2;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;

import edu.arizona.simulator.ww2d.gui.FengWrapper;
import edu.arizona.simulator.ww2d.logging.StateDatabase;
import edu.arizona.simulator.ww2d.object.GameObject;
import edu.arizona.simulator.ww2d.object.component.Component;
import edu.arizona.simulator.ww2d.object.component.ShapeVisual;
import edu.arizona.simulator.ww2d.object.component.SpriteVisual;
import edu.arizona.simulator.ww2d.object.component.TextVisual;
import edu.arizona.simulator.ww2d.utils.enums.ObjectType;
import edu.arizona.simulator.ww2d.utils.enums.States;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class ReplayState extends BHGameState {
	private static Logger logger = Logger.getLogger( ReplayState.class );

	private int _index;
	private List<String> _agents; 

	private int _time;
	private long _maxTime;

	private float _scale;

	private StateDatabase _db;
	private List<GameObject> _objects;

	private Vec2 _center;
	private GameObject _following;

	private Image _offscreen;
	private IMediaWriter _writer;

	public ReplayState(FengWrapper feng) { 
		super(feng);
	}

	@Override
	public int getID() {
		return States.ReplayState.ordinal();
	}

	@Override
	public void init(GameContainer container, StateBasedGame game) throws SlickException {
		_objects = new LinkedList<GameObject>();

		float x = (float) container.getWidth() / 2.0f;
		float y = (float) container.getHeight() / 2.0f;

		_center = new Vec2(x,y);
		_offscreen = new Image(container.getWidth(), container.getHeight());	
	}

	private void isCognitiveAgent(String xml) { 
		StringReader stringReader = new StringReader(xml);
		SAXReader reader = new SAXReader(false);
		try {
			Document doc = reader.read(stringReader);
			Element root = doc.getRootElement();
			String name = root.attributeValue("name");
			ObjectType objectType = ObjectType.valueOf(root.attributeValue("type"));
			if (objectType == ObjectType.cognitiveAgent)
				_agents.add(name);
		} catch (Exception e) { 
			e.printStackTrace();
		}
	}

	private void loadObject(String xml) { 
		StringReader stringReader = new StringReader(xml);
		SAXReader reader = new SAXReader(false);
		try {
			Set<Class> classSet = new HashSet<Class>();
			classSet.add(ShapeVisual.class);
			classSet.add(SpriteVisual.class);
			classSet.add(TextVisual.class);

			Document doc = reader.read(stringReader);
			Element root = doc.getRootElement();
			root.addAttribute("scale", _scale+"");

			String name = root.attributeValue("name");
			int renderPriority = Integer.parseInt(root.attributeValue("renderPriority"));
			ObjectType objectType = ObjectType.valueOf(root.attributeValue("type"));

			GameObject obj = new GameObject(name, objectType, renderPriority);
			logger.debug(" NAMES: " + obj.getName() + " " + _agents.get(_index));
			if (obj.getName().equals(_agents.get(_index))) {
				_following = obj;
				logger.debug("We are choosing to follow: " + _following.getName());
			}

			_objects.add(obj);
			Collections.sort(_objects, GameObject.render);

			List components = root.element("components").elements("component");
			for (int i = 0; i < components.size(); ++i) {
				Element comp = (Element) components.get(i);
				String className = comp.attributeValue("className");
				Class c = Class.forName(className);
				if (classSet.contains(c)) { 
					try { 
						Constructor<Component> constructor  = c.getConstructor(GameObject.class);
						Component compInstance = constructor.newInstance(obj);
						compInstance.fromXML(comp);
						obj.addComponent(compInstance);
					} catch (Exception exp) { 
						exp.printStackTrace();
					}
				}
			}

		} catch (Exception e) { 
			e.printStackTrace();
		}
	}

	@Override 
	public void enter(GameContainer container, StateBasedGame game) throws SlickException { 
		super.enter(container, game);

		_db = new StateDatabase(StateDatabase.PATH + "state-replay.db", false);
		_scale = Float.parseFloat(_db.getParameter("scale"));
		_maxTime = Long.parseLong(_db.queryEvent("FINISH", 0));

		_agents = new ArrayList<String>();
		_index = -1;
		for (String params : _db.queryEvents("CREATE_PHYSICS_OBJECT", 1)) {
			isCognitiveAgent(params);
		}

		newRecording(container, game);
		//		layout(_feng.getDisplay());

	}

	/**
	 * Begin a new recording and move forward in the list of agents.
	 * @param container
	 */
	private void newRecording(GameContainer container, StateBasedGame game) { 
		_objects.clear();
		_index += 1;
		if (_index >= _agents.size()) {
			_writer = null;
			game.enterState(States.RecordingState.ordinal());
			return;
		}

		logger.debug("Recording: " + _index);
		_time = 1;
		_writer = ToolFactory.makeWriter(StateDatabase.PATH + "movie-" + _agents.get(_index) + ".mp4");
		_writer.addVideoStream(0, 0, container.getWidth(), container.getHeight());
	}

	/**
	 * Called after we finished a recording in order to clean things up.
	 * @param container
	 */
	private void finishRecording(GameContainer container) { 
		_writer.close();

//		// now transcode it...
//		IMediaReader reader = ToolFactory.makeReader(StateDatabase.PATH + "movie" + _index + ".mp4");
//		IMediaWriter = ToolFactory.makeWriter(StateDatabase.PATH + "movie" + _index + ".mov", reader);
//		reader.addListener(ToolFactory.makeWriter(StateDatabase.PATH + "movie" + _index + ".mov", reader));
//
//		while (reader.readPacket() == null);
	}

	@Override
	public void leave(GameContainer container, StateBasedGame game)
	throws SlickException {
		super.leave(container, game);

		// Don't forget to remove all of the widgets before moving
		// to the next state.  If you forget then the widgets (although not rendered)
		// could actually receive button presses.
		_feng.getDisplay().removeAllWidgets();
	}

	public void finish() { 

	}

	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
		updateSequential(container, game);

		g.setAntiAlias(true);
		g.setBackground(Color.black);

		if (_following == null || _writer == null)
			return;

		g.pushTransform();
		g.resetTransform();
		g.translate(-_following.getPosition().x+_center.x, -_following.getPosition().y+_center.y);
		for (GameObject obj : _objects) { 
			obj.render(g);
		}
		g.popTransform();


		g.copyArea(_offscreen, 0, 0);
		_offscreen = _offscreen.getFlippedCopy(false, true);
		//		ImageOut.write(_offscreen, "/tmp/image.png");

		//		BufferedImage img = convertImage(_offscreen);
		//		BufferedImage convertImg = convertToType(img, BufferedImage.TYPE_3BYTE_BGR);
		BufferedImage convertImg = convertImage(_offscreen, BufferedImage.TYPE_3BYTE_BGR);
		_writer.encodeVideo(0, convertImg, _time*12, TimeUnit.MILLISECONDS);
		//		_feng.render(container, game, g);
	}

	private void updateSequential(GameContainer container, StateBasedGame game) { 
		for (String params : _db.queryEvents("CREATE_PHYSICS_OBJECT", _time)) {
			loadObject(params);
		}

		for (String params : _db.queryEvents("CREATE_GAME_OBJECT", _time)) {
			loadObject(params);
		}

		Set<Integer> removeSet = new HashSet<Integer>();
		for (String params : _db.queryEvents("REMOVE_OBJECT_EVENT", _time)) { 
			for (int i = 0; i < _objects.size(); ++i) { 
				if (_objects.get(i).getName().equals(params)) {
					removeSet.add(i);
					break;
				}
			}
		}

		List<GameObject> tmp = new LinkedList<GameObject>();
		for (int i = 0; i < _objects.size(); ++i) { 
			if (!removeSet.contains(i))
				tmp.add(_objects.get(i));
		}
		Collections.sort(tmp, GameObject.render);
		_objects = tmp;

		for (GameObject obj : _objects) { 
			// get the position.
			String queryX = _db.queryFluent("x", obj.getName(), _time);
			if (queryX.equals("unknown")) {
				logger.debug("FUCK ME: " + obj.getName() + " " + _time);
			}
			float x = Float.parseFloat(_db.queryFluent("x", obj.getName(), _time));
			float y = Float.parseFloat(_db.queryFluent("y", obj.getName(), _time));

			float heading = Float.parseFloat(_db.queryFluent("heading", obj.getName(), _time));

			obj.setPosition(new Vec2(x,y));
			obj.setHeading(heading);
		}

		++_time;
		if (_time >= _maxTime) {
			finishRecording(container);
			newRecording(container, game);
		}		
	}

	@Override
	public void update(GameContainer container, StateBasedGame game, int millis) throws SlickException {

	}

	@Override
	public void keyPressed(int key, char c) {
	}

	@Override
	public void keyReleased(int key, char c) { 
	}

	/**
	 * Layout the GUI for this game state.  We definitely need some sliders to 
	 * modify values, etc.
	 */
	private void layout(Display display) {
		try {
			FengGUI.setTheme(new XMLTheme("data/themes/QtCurve/QtCurve.xml"));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IXMLStreamableException e) {
			e.printStackTrace();
		}

		Window f = FengGUI.createWindow(false, false);
		f.setTitle("Parameters");
		f.getContentContainer().setLayoutManager(new RowLayout(false));
		f.getContentContainer().getAppearance().setPadding(new Spacing(10,10));

		f.getContentContainer().addWidget(addParameterSlider(Variable.maxAngularAcceleration, 0, 500));

		Container c = FengGUI.createWidget(Container.class);
		c.setLayoutManager(new FlowLayout(FlowLayout.CENTER));

		f.getContentContainer().addWidget(c);
		f.setSize(400, 200);
		//	    f.pack();
		display.addWidget(f);
	}


	/**
	 * Creates a FengGUI slider and connects it to a global variable with the same name
	 * @param name
	 * 		the name of the global variable.
	 * @param min
	 * 		the minimum value of the global variable.
	 * @param max
	 * 		the maximum value of the global variable.
	 * @return
	 */
	private Container addParameterSlider(final Variable name, final float min, final float max) { 
		//		float startValue = 0;
		//		Object obj = BB.inst().getVariableObject(name);
		//		if (obj != null)
		//			startValue = (Float) obj;
		//		
		//		Container c = FengGUI.createWidget(Container.class);
		//		c.setLayoutManager(new FlowLayout(FlowLayout.CENTER));
		//	    
		//		Label nameLabel = FengGUI.createWidget(Label.class);
		//		nameLabel.getAppearance().setAlignment(Alignment.MIDDLE);
		//		nameLabel.setText(name.toString());
		//		nameLabel.updateMinSize();
		//		c.addWidget(nameLabel);
		//		
		//		double v = (double) (startValue - min) / (double) (max - min);
		//		// Eclipse says it is deprecated, but it still works and you need to call
		//		// it this way for some reason.
		//		Slider slider = FengGUI.createSlider(true);
		//		slider.setValue(v);
		//		slider.setMinSize(75, 25);
		//	    c.addWidget(slider);
		//	    
		//	    final Label valueLabel = FengGUI.createWidget(Label.class);
		//	    valueLabel.setText(startValue + "");
		//		valueLabel.updateMinSize();
		//	    c.addWidget(valueLabel);
		//
		//	    slider.addSliderMovedListener(new ISliderMovedListener() {
		//			public void sliderMoved(SliderMovedEvent evt) {
		//				double range = max - min;
		//				double d = evt.getPosition();
		//				float v = (float) (min + (d*range));
		//				
		//				BB.inst().setVariableObject(name, v);
		//				valueLabel.setText(v + "");
		//			} 
		//	    });
		//
		//	    c.updateMinSize();
		//	    c.pack();
		//		return c;
		return null;
	}		

	private BufferedImage convertImage(Image image, int type) { 
		BufferedImage img = new BufferedImage(image.getWidth(), image.getHeight(), type);
		Color c;
		for (int y = image.getHeight()-1; y >= 0; y--) {
			for (int x = 0; x < image.getWidth(); x++) {
				c = image.getColor(x, y);
				java.awt.Color color = new java.awt.Color(c.r, c.g, c.b, c.a);
				img.setRGB(x, y, color.getRGB());
			}
		}
		return img;
	}

	private BufferedImage convertImage(Image image) {
		boolean hasAlpha = false;

		// conver the image into a byte buffer by reading each pixel in turn
		int len = 4 * image.getWidth() * image.getHeight();
		if (!hasAlpha) {
			len = 3 * image.getWidth() * image.getHeight();
		}

		ByteBuffer out = ByteBuffer.allocate(len);
		Color c;

		for (int y = image.getHeight()-1; y >= 0; y--) {
			for (int x = 0; x < image.getWidth(); x++) {
				c = image.getColor(x, y);

				out.put((byte) (c.r * 255.0f));
				out.put((byte) (c.g * 255.0f));
				out.put((byte) (c.b * 255.0f));
				if (hasAlpha) {
					out.put((byte) (c.a * 255.0f));
				}
			}
		}

		// create a raster of the correct format and fill it with our buffer
		DataBufferByte dataBuffer = new DataBufferByte(out.array(), len);

		PixelInterleavedSampleModel sampleModel;

		ColorModel cm;

		if (hasAlpha) {
			int[] offsets = { 0, 1, 2, 3 };
			sampleModel = new PixelInterleavedSampleModel(
					DataBuffer.TYPE_BYTE, image.getWidth(), image.getHeight(), 4,
					4 * image.getWidth(), offsets);

			cm = new ComponentColorModel(ColorSpace
					.getInstance(ColorSpace.CS_sRGB), new int[] { 8, 8, 8, 8 },
					true, false, ComponentColorModel.TRANSLUCENT,
					DataBuffer.TYPE_BYTE);
		} else {
			int[] offsets = { 0, 1, 2};
			sampleModel = new PixelInterleavedSampleModel(
					DataBuffer.TYPE_BYTE, image.getWidth(), image.getHeight(), 3,
					3 * image.getWidth(), offsets);

			cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
					new int[] {8,8,8,0},
					false,
					false,
					ComponentColorModel.OPAQUE,
					DataBuffer.TYPE_BYTE);
		}
		WritableRaster raster = Raster.createWritableRaster(sampleModel, dataBuffer, new Point(0, 0));

		// finally create the buffered image based on the data from the texture
		// and spit it through to ImageIO
		BufferedImage img = new BufferedImage(cm, raster, false, null);		
		return img;
	}

	/** 
	 * Convert a {@link BufferedImage} of any type, to {@link
	 * BufferedImage} of a specified type.  If the source image is the
	 * same type as the target type, then original image is returned,
	 * otherwise new image of the correct type is created and the content
	 * of the source image is copied into the new image.
	 *
	 * @param sourceImage the image to be converted
	 * @param targetType the desired BufferedImage type 
	 *
	 * @return a BufferedImage of the specifed target type.
	 *
	 * @see BufferedImage
	 */

	public static BufferedImage convertToType(BufferedImage sourceImage, int targetType) {
		BufferedImage image;
		if (sourceImage.getType() == targetType) {
			// if the source image is already the target type, return the source image
			image = sourceImage;
		} else {
			// otherwise create a new image of the target type and draw the new image 
			image = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(),
					targetType);
			image.getGraphics().drawImage(sourceImage, 0, 0, null);
		}
		return image;
	}
}
