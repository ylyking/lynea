/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lynea.npc.actions;

import lynea.npc.interrupts.InterruptListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lynea.npc.NPC;

/**
 *
 * @author Olivier
 */
public class ActionGroup extends Action implements Iterable<Action>
{
    private List<Action> actions;
    public Action currentAction = null;

    public ActionGroup(String name, NPC npc)
    {
        super(name, npc);
        actions = new ArrayList();
    }

    public boolean add(Action action)
    {
        action.setParent(this);
        return actions.add(action);
    }

    @Override
    public void childActionEnded(Action child)
    {
        //If this action is paused then the childAction is the alternativeAction,
        //so we resume this action. This is done in the superclass.
        super.childActionEnded(child);
        
        if(!isPaused())
        {
            //we check whether the child action that has just ended is the last action in the action list
            if(actions.indexOf(child) != actions.size( ) - 1)
            {
                currentAction = actions.get(actions.indexOf(child) + 1);
                currentAction.start();
            }
            else
            {
                end();
            }
        }
    }

    @Override
    public boolean update(double deltaTime)
    {
        
        if(!super.update(deltaTime))
        {
            return false;
        }
        currentAction.update(deltaTime);   
        return true;
    }

    @Override
    public void start()
    {
        super.start();
        currentAction = actions.get(0);
        currentAction.start();
    }

    @Override
    protected void end()
    {
        super.end();
    }

    public Iterator<Action> iterator()
    {
        return actions.iterator();
    }

    @Override
    public <T extends InterruptListener> void addListenersToList(List<T> interruptListeners, Class<T> interruptListenerClass)
    {
        super.addListenersToList(interruptListeners, interruptListenerClass);
        currentAction.addListenersToList(interruptListeners, interruptListenerClass);
    }

    public boolean remove(Action action) {
        return actions.remove(action);
    }

}
