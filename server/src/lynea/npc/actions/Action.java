/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lynea.npc.actions;

import lynea.npc.interrupts.InterruptListener;
import java.util.ArrayList;
import java.util.List;
import lynea.npc.NPC;

/**
 *
 * @author Olivier
 */
public abstract class Action
{


    protected String name = "";
    protected NPC npc;
    private boolean isStarted = false;
    private boolean isEnded = false;
    private boolean isPaused = false;
    private List<InterruptListener> interruptListeners;
    protected Action alternativeAction = null;
    private int iterate = 1;
    private boolean loop = false;
    private Action parentAction = null;

    public Action(String name, NPC npc)
    {
        this.name = name;
        this.npc = npc;
        interruptListeners = new ArrayList<InterruptListener>();
    }

    public void start()
    {
        isStarted = true;
        isEnded = false;
        isPaused = false;
    }
    public boolean isStarted()
    {
        return isStarted;
    }

    /*
     * @param : deltaTime (int) : in milliseconds
     */
    public boolean update(int deltaTime)
    {
        if (!isStarted() || isEnded())
        {
            return false;
        }
        if(isPaused())
        {
            if(alternativeAction != null && alternativeAction.isStarted())
                alternativeAction.update(deltaTime); //do execute the alternative Action.update()
            return false; //do not execute the normal Action.update()
        }
        return true;
    }

    protected void end()
    {
        isEnded = true;
        if (parentAction != null)
        {
            parentAction.childActionEnded(this);
        }
    }

    public void childActionEnded(Action childAction)
    {
        //If this action is paused then the childAction is the alternativeAction.
        //So we resume this action.
        if(isPaused())
        {
            alternativeAction = null;
            resume();
        }
    }
    public boolean isEnded()
    {
        return isEnded;
    }

    public void pause()
    {
        isPaused = true;
    }
    public void resume()
    {
        isPaused = false;
    }
    public boolean isPaused()
    {
        return isPaused;
    }
    
    public String getName()
    {
        return name;
    }

    public boolean attachListener(InterruptListener interruptListener)
    {
        return interruptListeners.add(interruptListener);
    }

    public <T extends InterruptListener> void addListenersToList(List<T> interruptListeners, Class<T> interruptListenerClass)
    {
        T listener = getListener(interruptListenerClass);
        if (listener != null)
            interruptListeners.add(listener);
    }

    public <T extends InterruptListener> T getListener(Class <T> interruptListenerClass)
    {
        for(InterruptListener listener : interruptListeners)
        {
            if(interruptListenerClass.isInstance(listener))
                return (T) listener;
        }
        return null;
    }

    public void setParent(Action parent) {
        this.parentAction = parent;
    }





}
