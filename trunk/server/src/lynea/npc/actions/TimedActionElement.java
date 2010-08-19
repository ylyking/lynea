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
public class TimedActionElement extends ActionElement
{
    private long timeleft;

    public TimedActionElement(String name, NPC npc)
    {
        super(name, npc);
    }
   public TimedActionElement(String name, NPC npc, long totalTime)
    {
        super(name, npc);
        if (totalTime > 0 )
            this.timeleft = totalTime;
    }

    @Override
    public boolean update(int deltaTime)
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

    protected void setTimeLeft(long timeleft)
    {
        if((!isStarted() || isPaused()) && timeleft > 0)
            this.timeleft = timeleft;
    }

    public long getTimeLeft()
    {
        return timeleft;
    }



}
