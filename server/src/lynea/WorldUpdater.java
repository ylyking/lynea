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
    private static WorldUpdater instance = null;
    private Thread thread;
    private int worldUpdatePeriod = 10; //in milliseconds
    private int sendWorldStatePeriod = 100; //in milliseconds
    private WorldSender sender;
    private long simulationTime;

    private WorldUpdater(WorldSender sender)
    {
        if (sender != null)
        {
            System.out.println("Creating WorldUpdater WITH ability to send world states to players");
            this.sender = sender;
        }
        else
        {
            System.out.println("Creating WorldUpdater WITHOUT ability to send world states to players [TEST MODE]");

        }
        thread = new Thread(this, "WorldUpdateThread");
    }
    private WorldUpdater()
    {
        this(null);
    }
    public long getSimulationTime()
    {
        return simulationTime;
    }


    public static WorldUpdater getInstance(WorldSender sender)
    {
        if (instance == null)
            instance = new WorldUpdater(sender);
        return instance;
    }
    public static WorldUpdater getInstance()
    {
        if (instance == null)
            instance = new WorldUpdater();
        return instance;
    }

    public void start()
    {
        thread.start();
    }
    public void run()
    {
        while(true)
        {
            long timeBeforeUpdate = Clock.getTime();
            simulationTime = timeBeforeUpdate;
            if(sender != null)
                sender.sendWorldState();
            for (int worldIteration = 0; worldIteration < sendWorldStatePeriod/worldUpdatePeriod; worldIteration++)
            {
                simulationTime += worldUpdatePeriod;
                NPC.updateAll(worldUpdatePeriod);
            }
            long timeAfterUpdate = Clock.getTime();
            if (timeAfterUpdate - timeBeforeUpdate <= (long) sendWorldStatePeriod)
            {
                try
                {
                    Thread.sleep((long) sendWorldStatePeriod - timeAfterUpdate + timeBeforeUpdate);
                }
                catch(InterruptedException e)
                {
                    System.out.println("WorldUpdateThread interrompu!!");
                }
            }
            
        }
    }

    public void addSimulationTime(long time)
    {
        simulationTime += time;
    }



}
