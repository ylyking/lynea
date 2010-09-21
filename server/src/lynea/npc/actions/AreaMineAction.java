/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lynea.npc.actions;

import lynea.npc.NPC;

/**
 * repeatedly mine deposits in a cell around a given ActionMark
 * @author Olivier
 */
public class AreaMineAction extends ActionElement
{

    ActionMark miningPosition;
    ActionElement currentSubAction;
    Deposit currentDeposit;
    double quantityToMine;
    double npcDepositQuantity;
    double minedQuantity = 0;
    Deposit.Type depositType;
    
    public AreaMineAction(String name, NPC npc, ActionMark miningPosition, double quantityToMine, Deposit.Type type)
    {
        super(name, npc);
        this.miningPosition = miningPosition;
        this.quantityToMine = quantityToMine;
        this.depositType = type;
    }

    @Override
    public void start()
    {
        System.out.println(getName()+".start()");
        super.start();

        npcDepositQuantity = npc.getInventory().getDeposit(depositType);
        
        Deposit [] deposits = miningPosition.getNearbyDeposits(depositType);
        currentDeposit = deposits[0];
        ActionMark depositPosition = new ActionMark(currentDeposit.getX(), currentDeposit.getY(), currentDeposit.getZ(), npc.getOwner(), false);
        currentSubAction = new MoveAction(getName()+".move", npc, depositPosition);
        currentSubAction.setParent(this);
        currentSubAction.start();
    }

    @Override
    protected void end()
    {
        System.out.println(getName()+".end()");
        npc.setAnimation("idle1");
        super.end();
    }

    @Override
    public boolean update(int deltaTime)
    {
        if(!super.update(deltaTime))
            return false;

        currentSubAction.update(deltaTime);

        return true;
    }

    @Override
    public void childActionEnded(Action child)
    {
        //If this action is paused then the childAction is the alternativeAction,
        //so we resume this action. This is done in the superclass.
        super.childActionEnded(child);

        if(!isPaused())
        {
            //we check whether the child action that has just ended is a move action
            if(currentSubAction.getName().equals(getName()+".move"))
            {
                //the "move to deposit" action has ended. we trigger the mine action            
                currentSubAction = new MineAction(getName()+".mine", npc, currentDeposit);
                currentSubAction.setParent(this);
                currentSubAction.start();
            }
            //we check whether the child action that has just ended is a mine action
            else if(currentSubAction.getName().equals(getName()+".mine"))
            {
                double previouslyMinedQuantity = npcDepositQuantity;
                npcDepositQuantity = npc.getInventory().getDeposit(depositType);
                minedQuantity += npcDepositQuantity - previouslyMinedQuantity;

                //let's determine why the mine action has ended
                //3 possibilities here :
                //1) the currently mined deposit has turned empty
                //2) the npc cannot carry any more deposit
                //3) the mined quantity has reached the quantityToMine limit
                //in case 1), another deposit in the area must be mined;
                //in case 2) and 3), the AreaMineAction must be ended
                if(currentDeposit.isEmpty())
                {
                    currentDeposit = null;

                    if((minedQuantity < quantityToMine) && npc.getInventory().canAddDeposit(depositType, 1))
                    {
                        //let's move to another deposit in the area
                        Deposit [] deposits = miningPosition.getNearbyDeposits(depositType);
                        currentDeposit = deposits[0];
                        ActionMark depositPosition = new ActionMark(currentDeposit.getX(), currentDeposit.getY(), currentDeposit.getZ(), npc.getOwner(), false);
                        currentSubAction = new MoveAction(getName()+".move", npc, depositPosition);
                        currentSubAction.setParent(this);
                        currentSubAction.start();
                    }
                }
            }
        }
    }

}
