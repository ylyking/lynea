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
public class WaitAction extends TimedActionElement implements AttackListener
{

    private boolean hasBeenAttacked = false;
    private ActionMark waitPosition;

    public WaitAction(String name, NPC npc, long waitMilliSeconds)
    {
        super(name, npc, waitMilliSeconds);
        waitPosition = new ActionMark(npc.getX(), npc.getY(), npc.getZ(), npc.getOwner(), false);

        attachListener(this);
    }

    @Override
    public boolean update(int deltaTime)
    {
        if(!super.update(deltaTime))
            return false;
        return true;
    }

    @Override
    public void start()
    {
        System.out.println(getName()+".start()");
        //this also sets the speed to zero
        npc.setAnimation("idle1");
        super.start();
    }

    @Override
    protected void end()
    {
        System.out.println(getName()+".end()");
        super.end();
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
            pause();
            ActionMark currentPosition = new ActionMark(npc.getX(), npc.getY(), npc.getZ(), npc.getOwner(), false);
            MoveAction goBackToWaitPosition = new MoveAction("goBackToWaitPosition", npc, currentPosition, waitPosition);
            goBackToWaitPosition.setParent(this);
            goBackToWaitPosition.start();
            alternativeAction = goBackToWaitPosition;
            hasBeenAttacked = false;
        }
        //else: automatically resume the wait action
        //you could use setTimeleft() or end() here to modify the remaining wait time

        //note : another way to code this wait-attack-moveback-waitagain action chain
        //would be to create an actionGroup with both the attack and moveback actions and set this actionGroup as the alternativeAction
        //instead of just the attack action
    }

}
