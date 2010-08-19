/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lynea.npc.actions;

import lynea.npc.NPC;

/**
 *
 * @author Olivier
 */
public class ActionElement extends Action
{

    public ActionElement(String name, NPC npc)
    {
        super(name, npc);
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
        super.start();
    }

    @Override
    protected void end()
    {
        super.end();
    }

    @Override
    public void childActionEnded(Action childAction)
    {
        //If this action is paused then the childAction is the alternativeAction,
        //so we resume this action. This is done in the superclass.
        super.childActionEnded(childAction);
    }

    @Override
    public void resume()
    {
        super.resume();
    }
    

}

