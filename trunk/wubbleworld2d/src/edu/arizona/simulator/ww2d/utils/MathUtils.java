package edu.arizona.simulator.ww2d.utils;

import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;

import org.apache.log4j.Logger;
import org.jbox2d.collision.Distance;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.common.XForm;

import edu.arizona.simulator.ww2d.object.PhysicsObject;

public class MathUtils {
	private static Logger logger = Logger.getLogger( MathUtils.class );
	
	/** Keep a global random around so that I don't always have to recreate it.	 */
	public static Random random;
	
	public static float PI4 = (float) (Math.PI / 4.0);
	public static float PI34 = (float) (3.0 * Math.PI / 4.0);
	
	static { 
		random = new Random();
	}
	
	/**
	 * Return a random number between -1 and 1 that is 
	 * centered around 0.
	 * @return
	 */
	public static float randomBinomial() { 
		return random.nextFloat() - random.nextFloat();
	}
	
	/**
	 * Take an angle and convert it into a normalized
	 * vector in the direction of the angle.
	 * @param angle
	 * @return
	 */
	public static Vec2 toVec2(float angle) { 
		Vec2 v = new Vec2();
		v.x = (float) Math.cos(angle);
		v.y = (float) Math.sin(angle);
		return v;
	}
	
	public static Vec2 random(Random r, Vec2 min, Vec2 max) {
		float x = r.nextInt((int) (max.x-min.x)) + min.x;
		float y = r.nextInt((int) (max.y-min.y)) + min.y;
		
		return new Vec2(x,y);
	}
	
	/**
	 * Returns the angle between two vectors.
	 * Assumes that the vectors have already been normalized.
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static float angleBetween(Vec2 v1, Vec2 v2) { 
		float d = v1.x*v2.x + v1.y*v2.y;
		return (float) Math.acos(d);
	}
	
	/**
	 * Returns the relative angle between the two vectors.
	 * This may be more useful than angleBetween.
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static float relativeAngle(Vec2 v1, Vec2 v2) { 
		return (float) (Math.atan2(v2.y, v2.x) - Math.atan2(v1.y, v1.x));
	}
	
	/**
	 * Calculate the shortest distance between the 
	 * AnimateEntity and the Entity given.
	 * @param ae
	 * @param e
	 * @return
	 */
	public static float distance(PhysicsObject ae, PhysicsObject e) { 
		Vec2 v1 = new Vec2();
		Vec2 v2 = new Vec2();
		
		return distance(ae, e, v1, v2);
	}
	
	/**
	 * Calculate the shortest distance between the 
	 * AnimateEntity and the Entity given.
	 * @param ae
	 * @param e
	 * @param v1 - the closest point on the first shape of ae
	 * @param v2 - the closest point on the first shape of e
	 * @return
	 */
	public static float distance(PhysicsObject ae, PhysicsObject e, Vec2 v1, Vec2 v2) { 
		Shape s1 = ae.getBody().getShapeList();
		XForm x1 = ae.getBody().getXForm();
		
		Shape s2 = e.getBody().getShapeList();
		XForm x2 = e.getBody().getXForm();
		
		Distance distance = new Distance();
		float d = distance.distance(v1, v2, s1, x1, s2, x2);
		return d;
	}	
	
	/**
	 * make sure that this value is between 0 and 2pi
	 * @param value
	 * @return
	 */
	public static float wrapValue(float value) { 
		float max = 2 * (float) Math.PI;
		while (value > max) {
			value -= max;
		}
		
		while (value < 0) { 
			value += max;
		}
		
		return value;
	}
	
	public static float angleDiff(float actual, float target) { 
		return -1 * (wrapValue(actual + (float) Math.PI - target) - (float) Math.PI);
	}
	
    /**
     * Compute the intersection of two line segments.
     * 
     * @param line1 
     * 			the first line to test
     * @param line2
     * 			the second line to test
     * @return intersect
     *            a Point2D that contains the intersection point or null if 
     *            it doesn't exist.
     */
    public static Point2D intersectLineLine(Line2D line1, Line2D line2) {
    	double a1x = line1.getX1();
    	double a1y = line1.getY1();
    	
    	double a2x = line1.getX2();
    	double a2y = line1.getY2();
    	
    	double b1x = line2.getX1();
    	double b1y = line2.getY1();
    	
    	double b2x = line2.getX2();
    	double b2y = line2.getY2();
    	
    	double ua_t = (b2x - b1x) * (a1y - b1y) - (b2y - b1y) * (a1x - b1x);
    	double ub_t = (a2x - a1x) * (a1y - b1y) - (a2y - a1y) * (a1x - b1x);
    	double u_b = (b2y - b1y) * (a2x - a1x) - (b2x - b1x) * (a2y - a1y);

    	if (u_b != 0) {
    		double ua = ua_t / u_b;
    		double ub = ub_t / u_b;
    		
    		if (0 <= ua && ua <= 1 && 0 <= ub && ub <= 1) {
    			double x = a1x + ua * (a2x - a1x);
    			double y = a1y + ua * (a2y - a1y);
    			return new Point2D.Double(x, y);
    		} else {
    			return null;
    		}
    	} else {
    		return null;
    	}
    }
	
    /**
	 * Example implementation of "Minimum Distance between a Point and a Line" as
	 * described by Paul Bourke on
	 * See http://local.wasp.uwa.edu.au/~pbourke/geometry/pointline/.
     * 
     * Returns the distance of p3 to the segment defined by p1,p2 and 
     * stores the point on the segment inside of the result.
     * @param p1
     *                First point of the segment
     * @param p2
     *                Second point of the segment
     * @param p3
     *                Point to which we want to know the distance of the segment
     *                defined by p1,p2
     *                
     * @param result  
     *                Where we want the closest point stored.
     * @return The distance of p3 to the segment defined by p1,p2
     */
    public static float distanceToSegment(Vec2 p3, Vec2 p1, Vec2 p2, Vec2 result) {
    	Vec2 delta = p2.sub(p1);

    	if (delta.x == 0 && delta.y == 0) 
    		throw new RuntimeException("Line segment has 0 length: " + p1 + " " + p2);

    	float deltaX2 = delta.x*delta.x;
    	float deltaY2 = delta.y*delta.y;

    	float u = ((p3.x - p1.x) * delta.x + (p3.y - p1.y) * delta.y) / (deltaX2 + deltaY2);

    	if (u < 0) {
    		result.x = p1.x;
    		result.y = p1.y;
    	} else if (u > 1) {
    		result.x = p2.x;
    		result.y = p2.y;
    	} else {
    		result.x = p1.x + (u * delta.x);
    		result.y = p1.y + (u * delta.y);
    	}
    	
    	return result.sub(p3).length();
    }	    
    
    public static Collection<Line2D> getLines(FlatteningPathIterator it) {
    	Collection<Line2D> out = new LinkedList<Line2D>();
    	double[] coords = new double[2];
    	double[] lastCoords = new double[2];
    	double[] lastMove = new double[2];
    	while (!it.isDone()) {
    		int type = it.currentSegment(coords);
    		if (type == PathIterator.SEG_LINETO) {
    			out.add(new Line2D.Double(lastCoords[0], lastCoords[1],
    					coords[0], coords[1]));
    		} else if (type == PathIterator.SEG_MOVETO) {
    			lastMove[0] = coords[0];
    			lastMove[1] = coords[1];
    		} else if (type == PathIterator.SEG_CLOSE) {
    			double d = Point2D.distance(lastMove[0], lastMove[1], coords[0], coords[1]);
    			if (d > 0.0001)
    				out.add(new Line2D.Double(lastMove[0], lastMove[1], coords[0],
    						coords[1]));
    		}
    		lastCoords[0] = coords[0];
    		lastCoords[1] = coords[1];
    		it.next();
    	}
    	return out;
    }
}
