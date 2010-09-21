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

    private float speed;
    private boolean stopping = false;
    private boolean hasBeenAttacked = false;
    private float stopDistance;

    public MoveAction(String name, NPC npc, ActionMark end)
    {
        super(name, npc);
        this.end = end;
        this.current = (ActionMark) begin.clone();
        this.current.setVisible(false);

        attachListener(this);
    }

    @Override
    public boolean update(int deltaTime)
    {
        if(!super.update(deltaTime))
            return false;

        //TODO: smooth rotation
        
        //TODO: path finding
        
        npc.updateHeading(deltaTime);
        current.setPosition(npc);

        if(!stopping && (current.distance(end) <= stopDistance))
        {
            if(current.distance(end)>0)
            {
                //calculate the real stop time (<= NPC.maxStopTime) needed to arrive at the destination with zero speed
                long realStopTime = Math.round(current.distance(end)*2/speed * 1000);
                npc.stop(realStopTime);
            }
            stopping = true;
        }

        if(stopping && !npc.isAccelerating())
        {
            end();
        }
        return true;
    }

    @Override
    public void start()
    {
        super.start();

        System.out.println(getName()+".start()");
        npc.setAnimation("walk");
        speed = npc.getSpeed();
        begin = new ActionMark(npc.getX(), npc.getY(), npc.getZ(), npc.getOwner(), false);
        setNPCInitialHeading();

        float stopTimeInSec = ((float)NPC.maxStopTime)/1000.0f;
        float stopAcceleration = (0 - speed)/ stopTimeInSec;
        this.stopDistance = speed * stopTimeInSec + stopAcceleration/2.0f* (float)Math.pow(stopTimeInSec, 2);
    }

    @Override
    protected void end()
    {
        System.out.println(getName()+".end()");
        //fix the position which could not be 100% correct as we use non infinitesimal time steps (worldUpdatePeriod)
        npc.setPosition(this.end);
        npc.setAnimation("idle1");
        super.end();
    }
    
    public float getProgress()
    {
        return begin.distance(current)/begin.distance(end);
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
            setNPCInitialHeading();
            stopping = false;
            speed = npc.getSpeed();         
            hasBeenAttacked = false;
        }
    }

    private void setNPCInitialHeading()
    {
        //alpha is the angle between the current facing direction and the global z axis
        float alpha = (float) (Math.PI/2 - Math.atan2(end.getZ()-begin.getZ(), end.getX()-begin.getX()));
        npc.setAngle(alpha);
        npc.setSpeed(NPC.walkSpeed);//this automatically sets the "walk" anim so no need for this line:
        //npc.setAnimation("walk");
        npc.setHeadingUpdateTime();
    }
}
