/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lynea.npc.actions;

import lynea.npc.NPC;
import lynea.npc.interrupts.AttackInterrupt;
import lynea.npc.interrupts.AttackListener;

/**
 *
 * @author Olivier
 */
public class MineAction extends ActionElement implements AttackListener
{
    private boolean hasBeenAttacked = false;
    private ActionMark miningPosition;
    private Deposit deposit;
    private double minedQuantityUnderOneUnit = 0;

    public MineAction(String name, NPC npc, Deposit deposit)
    {
        super(name, npc);
        this.deposit = deposit;

        attachListener(this);
    }

    @Override
    public boolean update(int deltaTime)
    {
        if(!super.update(deltaTime))
            return false;

        minedQuantityUnderOneUnit += npc.getMiningSpeed()*(float)deltaTime/1000.0f;

        if(minedQuantityUnderOneUnit >= 1)
        {
            minedQuantityUnderOneUnit = minedQuantityUnderOneUnit - 1;
            deposit.mine(1, npc);
            
            if(!npc.getInventory().canAddDeposit(deposit.getType(), 1))
            {
                end();
            }
        }
      
        return true;
    }

    @Override
    public void start()
    {
        System.out.println(getName()+".start()");
        miningPosition = new ActionMark(npc.getX(), npc.getY(), npc.getZ(), npc.getOwner(), false);
        
        npc.setAnimation("mine");
        super.start();
    }

    @Override
    protected void end()
    {
        System.out.println(getName()+".end()");
        npc.setAnimation("idle1");
        super.end();
    }



    @Override
    public void onAttack(AttackInterrupt attackInterrupt)
    {
        pause();
        AttackAction attackAction = new AttackAction(getName()+".onAttack",
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
            MoveAction goBackToMinePosition = new MoveAction("goBackToMinePosition", npc, miningPosition);
            goBackToMinePosition.setParent(this);
            goBackToMinePosition.start();
            alternativeAction = goBackToMinePosition;
            hasBeenAttacked = false;
        }

 
        //else: automatically resume the mine action
        //you could use end() here to abord the mine action

        //note : another way to code this mine-attack-moveback-mineagain action chain
        //would be to create an actionGroup with both the attack and moveback actions and set this actionGroup as the alternativeAction
        //instead of just the attack action
    }

}
