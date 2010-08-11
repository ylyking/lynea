package lynea;

import it.gotoandplay.smartfoxserver.extensions.AbstractExtension;

import lynea.npc.NPC;


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Olivier
 */
public class WorldUpdater implements Runnable
{
    private Thread thread;
    private double worldUpdatePeriod = 0.01;
    private double sendWorldStatePeriod = 0.2;
    private WorldSender sender;

    public WorldUpdater(WorldSender sender)
    {
        this.sender = sender;
        thread = new Thread(this, "WorldUpdateThread");
    }

    public void start()
    {
        thread.start();
    }
    public void run()
    {
        while(true)
        {
            long timeBeforeUpdate = System.currentTimeMillis();
            sender.sendWorldState();
            for (int worldIteration = 0; worldIteration < sendWorldStatePeriod/worldUpdatePeriod; worldIteration++)
            {
                NPC.updateAll(worldUpdatePeriod);
            }
            long timeAfterUpdate = System.currentTimeMillis();
            if (timeAfterUpdate - timeBeforeUpdate <= (long) (sendWorldStatePeriod*1000))
            {
                try
                {
                    Thread.sleep((long) (sendWorldStatePeriod*1000) - timeAfterUpdate + timeBeforeUpdate);

                }
                catch(InterruptedException e)
                {
                    System.out.println("WorldUpdateThread interrompu!!");
                }
            }
            
        }
    }



}
