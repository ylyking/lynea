/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lynea.npc.actions;

import lynea.npc.NPC;
import lynea.npc.actions.ActionElement;

/**
 *
 * @author Olivier
 */
public class TimedActionElement extends ActionElement
{
    private double timeleft;

    public TimedActionElement(String name, NPC npc)
    {
        super(name, npc);
    }
   public TimedActionElement(String name, NPC npc, double totalTime)
    {
        super(name, npc);
        if (totalTime > 0 )
            this.timeleft = totalTime;
    }

    @Override
    public boolean update(double deltaTime)
    {
        if (!super.update(deltaTime))
            return false;

        timeleft -= deltaTime;
        if (timeleft <= 0)
        {
            end();
        }

        return true;
    }

    protected void setTimeLeft(double timeleft)
    {
        if((!isStarted() || isPaused()) && timeleft > 0)
            this.timeleft = timeleft;
    }

    public double getTimeLeft()
    {
        return timeleft;
    }



}
