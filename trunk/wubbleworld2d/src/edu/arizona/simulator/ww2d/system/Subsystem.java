package edu.arizona.simulator.ww2d.system;

import org.newdawn.slick.Graphics;

import edu.arizona.simulator.ww2d.utils.enums.SubsystemType;

public interface Subsystem {

    /**
     * Returns a unique id for this system to distinguish
     * it from the other subsystems.
     * @return
     */
    public SubsystemType getId();

    /**
     * Called every update cycle.  Order is predetermined
     * for some of the managers, but the mixin managers will
     * be called in the order that they are added.
     * @param eps
     */
    public void update(int eps);

    /**
     * Called every render cycle.  Most of the time there will 
     * probably be nothing to render, but occasionally you
     * may want to render
     * @param g
     */
    public void render(Graphics g);

    /**
     * Called when we are shutting everything down so that 
     * you can clean up everything that is outstanding.
     */
    public void finish();
}
