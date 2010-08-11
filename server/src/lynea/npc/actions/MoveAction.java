/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lynea.npc.actions;

import lynea.npc.interrupts.AttackInterrupt;
import lynea.npc.interrupts.AttackListener;
import lynea.npc.NPC;

/**
 *
 * @author Olivier
 */
public class MoveAction extends ActionElement implements AttackListener
{
    private ActionMark begin;
    private ActionMark end;
    private ActionMark current;
    private double totalTime;

    private double speed;
    private double progress = 0;
    private double arrivalProximity = 0.5;

    private boolean hasBeenAttacked = false;

    public MoveAction(String name, NPC npc, ActionMark begin, ActionMark end)
    {
        super(name, npc);
        this.begin = begin;
        this.end = end;
        this.current = (ActionMark) begin.clone();
        this.current.setVisible(false);
        speed = npc.getSpeed();
        totalTime = getTotalTime();

        attachListener(this);
    }

    @Override
    public boolean update(double deltaTime)
    {
        if(!super.update(deltaTime))
            return false;

        //TODO: path finding
        //update movement progress (between 0 and 1)
        progress += deltaTime / totalTime;
        double current_x =  begin.getX() + progress * (end.getX() -  begin.getX());
        double current_y =  begin.getY() + progress * (end.getY() -  begin.getY());
        double current_z =  begin.getZ() + progress * (end.getZ() -  begin.getZ());
        current.setPosition(current_x, current_y, current_z);
        
        //alpha_z is the angle between the current facing direction and the global z axis

        double alpha_z = Math.PI/2 - Math.atan2(end.getZ()-current_z, end.getX()-current_x);

        //convert to (left handed) quaternion
        double ry = Math.sin(alpha_z/2);
        double w = Math.cos(alpha_z/2);
        current.setOrientation(0.0, ry, 0.0, w);

        npc.setTransform(current);
        if (current.distance(end) < arrivalProximity)
            end();

        return true;
    }

    @Override
    public void start()
    {
        System.out.println(getName()+".start() -- NPC>WALK anim");
        npc.setAnimation("walk");
        super.start();
    }

    @Override
    protected void end()
    {
        System.out.println(getName()+".end() -- NPC>idle1 anim");
        npc.setAnimation("idle1");
        super.end();
    }

    private double getTotalTime()
    {
       //TODO: path finding
       return begin.distance(end) / speed;
    }
    public double getProgress()
    {
        return progress;
    }

    @Override
    public void onAttack(AttackInterrupt attackInterrupt)
    {
        pause();
        AttackAction attackAction = new AttackAction(name+".onAttack", 
                npc,
                attackInterrupt.getAttacker(),
                AttackAction.StopCondition.ESCAPE,
                AttackAction.EscapeCondition.NEVER);
        attackAction.setParent(this);
        attackAction.start();
        alternativeAction = attackAction;
        hasBeenAttacked = true;
    }

    @Override
    public void resume()
    {
        super.resume();
        if (hasBeenAttacked)
        {
            begin = new ActionMark(npc.getX(), npc.getY(), npc.getZ(), npc.getOwner(), false);
            current = (ActionMark) begin.clone();
            progress = 0;
            totalTime = getTotalTime();
            hasBeenAttacked = false;
        }
    }

}
