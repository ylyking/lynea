/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lynea.npc.actions;

import lynea.WorldSender;
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
    public boolean update(int deltaTime)
    {
        if(!super.update(deltaTime))
            return false;

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
                WorldSender sender = WorldSender.getInstance();
                if (sender!= null)
                    sender.sendHeadingOfSender(npc);
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
        setNPCInitialHeading();

        float stopTimeInSec = ((float)NPC.maxStopTime)/1000.0f;
        float stopAcceleration = (0 - speed)/ stopTimeInSec;
        this.stopDistance = speed * stopTimeInSec + stopAcceleration/2.0f* (float)Math.pow(stopTimeInSec, 2);
    }

    @Override
    protected void end()
    {
        System.out.println(getName()+".end()");
        npc.setAnimation("idle1");
        npc.setPosition(this.end);
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
            npc.setAnimation("walk");
            stopping = false;
            speed = npc.getSpeed();
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
