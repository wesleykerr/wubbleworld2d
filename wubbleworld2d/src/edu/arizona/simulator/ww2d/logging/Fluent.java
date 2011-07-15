package edu.arizona.simulator.ww2d.logging;

import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.spaces.Space;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class Fluent {
    private static Logger logger = Logger.getLogger( Fluent.class );

    protected StateDatabase _db;
	
    private boolean _nonChanging;
    private Object  _defaultValue;
    
	protected String _name;
	protected int    _nameId;

	protected String _entityKey;
	protected int    _entityId;
	
	protected Object _value;
	
	protected long    _lastUpdate;
	protected int     _dbRecordId;
	
	public Fluent(StateDatabase db, String name, String entities, boolean nonChanging) {
		this(db, name, entities, nonChanging, null);
	}

	public Fluent(StateDatabase db, String name, String entities, boolean nonChanging, Object value) { 
		this(db, name, entities, nonChanging, value, "unknown");
	}
	
	public Fluent(StateDatabase db, String name, String entities, boolean nonChanging, Object value, Object defaultValue) { 
		_db = db;
		_nonChanging = nonChanging;
		_defaultValue = defaultValue;
		
		_name = name;
		_entityKey = entities;

		_nameId = _db.findOrAddFluent(_name);
		_entityId = _db.findOrAddEntityKey(_entityKey);

		Space systemSpace = Blackboard.inst().getSpace("system");
		_lastUpdate = systemSpace.get(Variable.logicalTime).get(Long.class);
		
		// we were unknown until this point
		if (_lastUpdate > 1) { 
			_value = _defaultValue;
			open(0);
			close();
		}
		_value = value;
		
		
		if (value != null) 
			open();
	}
	
	/**
	 * returns the currently stored value in value...but what the hell does
	 * this mean if the object is closed?
	 * @return
	 */
	public Object getValue() { 
		return _value;
	}
	
	/**
	 * Change the value to "unknown" when we don't
	 * record any change.  
	 */
	public void postUpdate() {
		Space systemSpace = Blackboard.inst().getSpace("system");
		long time = systemSpace.get(Variable.logicalTime).get(Long.class);
		if (!_nonChanging && _lastUpdate < time && !_defaultValue.equals(_value)) {
			close();
			_value = _defaultValue;
			open();
		}
	}
	
	private void open(long time) { 
		try {
			_dbRecordId = _db.getNextRowId(_name);
			PreparedStatement ps = _db.getInsertPS(_name);
			ps.setInt(1, _dbRecordId);
			ps.setInt(2, _nameId);
			ps.setInt(3, _entityId);
			ps.setString(4, _value.toString());
			ps.setLong(5, time);
			
			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * allow users to open a database record for this fluent
	 * and if it fails it must mean we are missing the 
	 * correct database table, so we will create that
	 * table.
	 */
	public void open() {
		Space systemSpace = Blackboard.inst().getSpace("system");
		long time = systemSpace.get(Variable.logicalTime).get(Long.class);
		open(time);
	}
	
	public void close() {
		try {
			Space systemSpace = Blackboard.inst().getSpace("system");
			long time = systemSpace.get(Variable.logicalTime).get(Long.class);
			PreparedStatement ps = _db.getUpdatePS(_name);
			ps.setLong(1, time);
			ps.setInt(2, _dbRecordId);
			ps.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * default update for objects.  Note that we have updated
	 * ourselves, but if that value is different than a previous
	 * value, we close this fluent and open a new one.
	 * 
	 * If the value has changed we return true.
	 * @param val
	 * @return has this fluent changed.
	 */
	public void update(Object val) {
		Space systemSpace = Blackboard.inst().getSpace("system");
		_lastUpdate = systemSpace.get(Variable.logicalTime).get(Long.class);
		
		boolean isFloat = _value instanceof Float;
		boolean isNewFloat = val instanceof Float;
		
		if (_value == null) { 
			changed(false, val);
		} else if (isFloat && isNewFloat) { 
			float orig = (Float) _value;
			float newV = (Float) val;
			
			float delta = Math.abs(orig - newV);
			if (delta > 0.001)
				changed(true, val);
		} else if (!_value.equals(val)) {
			changed(true, val);
		}
	}
	
	private void changed(boolean close, Object val) { 
		if (close)
			close();
		_value = val;
		open();
	}	
}
