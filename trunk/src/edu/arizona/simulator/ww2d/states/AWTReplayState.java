package edu.arizona.simulator.ww2d.states;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.StringReader;
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
import org.jbox2d.common.Vec2;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;

import edu.arizona.simulator.ww2d.events.spawn.CreateGameObject;
import edu.arizona.simulator.ww2d.events.spawn.CreatePhysicsObject;
import edu.arizona.simulator.ww2d.events.spawn.RemoveGameObject;
import edu.arizona.simulator.ww2d.events.system.FinishEvent;
import edu.arizona.simulator.ww2d.gui.FengWrapper;
import edu.arizona.simulator.ww2d.logging.StateDatabase;
import edu.arizona.simulator.ww2d.object.component.ShapeVisual;
import edu.arizona.simulator.ww2d.object.component.SpriteVisual;
import edu.arizona.simulator.ww2d.replay.awt.AWTGameObject;
import edu.arizona.simulator.ww2d.replay.awt.component.AWTComponent;
import edu.arizona.simulator.ww2d.replay.awt.component.AWTInternalComponent;
import edu.arizona.simulator.ww2d.replay.awt.component.AWTShapeVisual;
import edu.arizona.simulator.ww2d.replay.awt.component.AWTSpriteVisual;
import edu.arizona.simulator.ww2d.utils.enums.ObjectType;
import edu.arizona.simulator.ww2d.utils.enums.States;

public class AWTReplayState extends BHGameState {
	private static Logger logger = Logger.getLogger(AWTReplayState.class);

	private int _index;
	private List<String> _agents;

	private int _time;
	private long _maxTime;

	private float _scale;

	private StateDatabase _db;
	private List<AWTGameObject> _objects;

	private Vec2 _center;
	private AWTGameObject _following;

	private BufferedImage _offscreen;
	private IMediaWriter _writer;

	public AWTReplayState(FengWrapper feng) {
		super(feng);
	}

	@Override
	public int getID() {
		return States.ReplayState.ordinal();
	}

	@Override
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException {
		_objects = new LinkedList<AWTGameObject>();

		float x = (float) container.getWidth() / 2.0f;
		float y = (float) container.getHeight() / 2.0f;

		_center = new Vec2(x, y);
		_offscreen = new BufferedImage(container.getWidth(),
				container.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
	}

	private void isCognitiveAgent(String xml) {
		StringReader stringReader = new StringReader(xml);
		SAXReader reader = new SAXReader(false);
		try {
			Document doc = reader.read(stringReader);
			Element root = doc.getRootElement();
			String name = root.attributeValue("name");
			ObjectType objectType = ObjectType.valueOf(root
					.attributeValue("type"));
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
			Document doc = reader.read(stringReader);
			Element root = doc.getRootElement();
			root.addAttribute("scale", _scale + "");

			String name = root.attributeValue("name");
			int renderPriority = Integer.parseInt(root
					.attributeValue("renderPriority"));
			ObjectType objectType = ObjectType.valueOf(root
					.attributeValue("type"));

			AWTGameObject obj = new AWTGameObject(name, objectType,
					renderPriority);
			logger.debug(" NAMES: " + obj.getName() + " " + _agents.get(_index));
			if (obj.getName().equals(_agents.get(_index))) {
				_following = obj;
				_following.setPriority(200);
				_following.addComponent(new AWTInternalComponent(_following));
				logger.debug("We are choosing to follow: "
						+ _following.getName());
			}

			List components = root.element("components").elements("component");
			for (int i = 0; i < components.size(); ++i) {
				Element comp = (Element) components.get(i);
				String className = comp.attributeValue("className");
				if (ShapeVisual.class.getName().equals(className)) {
					AWTComponent awtComp = new AWTShapeVisual(obj);
					awtComp.fromXML(comp);
					obj.addComponent(awtComp);
				} else if (SpriteVisual.class.getName().equals(className)) {
					AWTComponent awtComp = new AWTSpriteVisual(obj);
					awtComp.fromXML(comp);
					obj.addComponent(awtComp);
				}
			}

			_objects.add(obj);
			Collections.sort(_objects, AWTGameObject.render);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void enter(GameContainer container, StateBasedGame game)
			throws SlickException {
		super.enter(container, game);

		_db = new StateDatabase(StateDatabase.PATH + "state-replay.db", false);
		_scale = Float.parseFloat(_db.getParameter("scale"));
		_maxTime = Long
				.parseLong(_db.queryEvent(FinishEvent.class.getName(), 0));

		_agents = new ArrayList<String>();
		_index = -1;
		for (String params : _db.queryEvents(
				CreatePhysicsObject.class.getName(), 1)) {
			isCognitiveAgent(params);
		}

		newRecording(container, game);
		// layout(_feng.getDisplay());

	}

	/**
	 * Begin a new recording and move forward in the list of agents.
	 * 
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
		_writer = ToolFactory.makeWriter(StateDatabase.PATH + "movie-"
				+ _agents.get(_index) + ".mov");
		_writer.addVideoStream(0, 0, container.getWidth(),
				container.getHeight());

		// IStream stream = _writer.getContainer().getStream(0);
		// IStreamCoder coder = stream.getStreamCoder();
		// // IStreamCoder coder = IStreamCoder.make(Direction.ENCODING);
		// coder.setCodec(ICodec.ID.CODEC_ID_H264);
		// int retval =
		// Configuration.configure("/usr/local/xuggler/share/ffmpeg/libx264-hq.ffpreset",
		// coder);
		// if (retval < 0)
		// throw new
		// RuntimeException("could not configure coder from preset file");
		// _writer.addVideoStream(0, 0, container.getWidth(),
		// container.getHeight());
	}

	/**
	 * Called after we finished a recording in order to clean things up.
	 * 
	 * @param container
	 */
	private void finishRecording(GameContainer container) {
		try {
			_writer.close();
		} catch (Exception e) {
			logger.error("Error closing the damn thing: " + e.getMessage());
			e.printStackTrace();
		}

		// // now transcode it...
		// IMediaReader reader = ToolFactory.makeReader(StateDatabase.PATH +
		// "movie" + _index + ".mp4");
		// reader.addListener(ToolFactory.makeWriter(StateDatabase.PATH +
		// "movie" + _index + ".mov", reader));
		//
		// while (reader.readPacket() == null);
	}

	@Override
	public void leave(GameContainer container, StateBasedGame game)
			throws SlickException {
		super.leave(container, game);

		// Don't forget to remove all of the widgets before moving
		// to the next state. If you forget then the widgets (although not
		// rendered)
		// could actually receive button presses.
		_feng.getDisplay().removeAllWidgets();
	}

	public void finish() {

	}

	@Override
	public void render(GameContainer container, StateBasedGame game,
			Graphics ignore) throws SlickException {
		updateSequential(container, game);

		if (/* _following == null || */_writer == null)
			return;

		Graphics2D g = (Graphics2D) _offscreen.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		g.setColor(Color.green);
		g.fillRect(0, 0, _offscreen.getWidth(), _offscreen.getHeight());

		AffineTransform defaultXForm = new AffineTransform();
		if (_following != null) {
			defaultXForm.translate(-_following.getPosition().x + _center.x,
					-_following.getPosition().y + _center.y);
		} else {
			defaultXForm.translate(_center.x, _center.y);
		}
		g.setTransform(defaultXForm);
		for (AWTGameObject obj : _objects) {
			obj.render(g);
		}

		_writer.encodeVideo(0, _offscreen, _time * 12, TimeUnit.MILLISECONDS);
	}

	private void updateSequential(GameContainer container, StateBasedGame game) {
		for (String params : _db.queryEvents(
				CreatePhysicsObject.class.getName(), _time)) {
			loadObject(params);
		}

		for (String params : _db.queryEvents(CreateGameObject.class.getName(),
				_time)) {
			loadObject(params);
		}

		Set<Integer> removeSet = new HashSet<Integer>();
		for (String params : _db.queryEvents(RemoveGameObject.class.getName(),
				_time)) {
			for (int i = 0; i < _objects.size(); ++i) {
				if (_objects.get(i).getName().equals(params)) {
					removeSet.add(i);
					break;
				}
			}
		}

		List<AWTGameObject> tmp = new LinkedList<AWTGameObject>();
		for (int i = 0; i < _objects.size(); ++i) {
			if (!removeSet.contains(i))
				tmp.add(_objects.get(i));
		}
		Collections.sort(tmp, AWTGameObject.render);
		_objects = tmp;

		for (AWTGameObject obj : _objects) {
			// get the position.
			String queryX = _db.queryFluent("x", obj.getName(), _time);
			if (queryX.equals("unknown")) {
				logger.debug("FUCK ME: " + obj.getName() + " " + _time);
			}
			float x = Float.parseFloat(_db.queryFluent("x", obj.getName(),
					_time));
			float y = Float.parseFloat(_db.queryFluent("y", obj.getName(),
					_time));

			float heading = Float.parseFloat(_db.queryFluent("heading",
					obj.getName(), _time));

			obj.setPosition(new Vec2(x, y));
			obj.setHeading(heading);
		}

		// update the internal state of the agent that we are following
		if (_following != null) {
			try {
				_following.setUserData(
						"energy",
						Float.parseFloat(_db.queryFluent("energy",
								_following.getName(), _time)));
				_following
						.setUserData("energyMax", Float.parseFloat(_db
								.queryFluent("energyMax", _following.getName(),
										_time)));

				_following.setUserData(
						"arousal",
						Float.parseFloat(_db.queryFluent("arousal",
								_following.getName(), _time)));
				_following.setUserData("arousalMax",
						Float.parseFloat(_db.queryFluent("arousalMax",
								_following.getName(), _time)));

				_following.setUserData(
						"valence",
						Float.parseFloat(_db.queryFluent("valence",
								_following.getName(), _time)));
				_following.setUserData("valenceMax",
						Float.parseFloat(_db.queryFluent("valenceMax",
								_following.getName(), _time)));
			} catch (Exception e) {
				logger.debug(_following.getName() + " " + _time);
				e.printStackTrace();
			}
		}

		++_time;
		if (_time >= _maxTime) {
			finishRecording(container);
			newRecording(container, game);
		}
	}

	@Override
	public void update(GameContainer container, StateBasedGame game, int millis)
			throws SlickException {

	}

	@Override
	public void keyPressed(int key, char c) {
	}

	@Override
	public void keyReleased(int key, char c) {
	}
}