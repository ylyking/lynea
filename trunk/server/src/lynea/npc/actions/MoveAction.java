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
    private float totalTime;

    private float speed;
    private float progress = 0;
    //private float arrivalProximity = 0.5f;

    private boolean hasBeenAttacked = false;

    public MoveAction(String name, NPC npc, ActionMark begin, ActionMark end)
    {
        super(name, npc);
        this.begin = begin;
        this.end = end;
        this.current = (ActionMark) begin.clone();
        this.current.setVisible(false);

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

        float current_x =  begin.getX() + progress * (end.getX() -  begin.getX());
        float current_y =  begin.getY() + progress * (end.getY() -  begin.getY());
        float current_z =  begin.getZ() + progress * (end.getZ() -  begin.getZ());
        current.setPosition(current_x, current_y, current_z);
        
        npc.updateHeading();
        //if (current.distance(end) < arrivalProximity)
        if (progress >= 1)
            end();

        return true;
    }

    @Override
    public void start()
    {
        super.start();

        System.out.println(getName()+".start()");
        npc.setAnimation("walk");
        speed = npc.getSpeedForCurrentAnimation();
        totalTime = getTotalTime();
        setNPCInitialHeading();
    }

    @Override
    protected void end()
    {
        System.out.println(getName()+".end()");
        npc.setAnimation("idle1");
        npc.setPosition(this.end);
        super.end();
    }

    private float getTotalTime()
    {
       //TODO: path finding
       return begin.distance(end) / speed;
    }
    public float getProgress()
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
            npc.setAnimation("walk");
            speed = npc.getSpeedForCurrentAnimation();
            totalTime = getTotalTime();
            setNPCInitialHeading();
            hasBeenAttacked = false;
        }
    }

    private void setNPCInitialHeading()
    {
        npc.setPosition(begin.getX(), begin.getY(), begin.getZ());
        //alpha is the angle between the current facing direction and the global z axis
        float alpha = (float) (Math.PI/2 - Math.atan2(end.getZ()-begin.getZ(), end.getX()-begin.getX()));
        npc.setAngle(alpha);
        npc.setHeadingUpdateTime();
    }
}
