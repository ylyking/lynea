/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lynea.npc;



import java.util.ArrayList;
import java.util.List;
import lynea.AssetOwner;
import lynea.npc.actions.ActionGroup;
import lynea.npc.actions.Action;
import lynea.PhysicalEntity;
import lynea.WorldUpdater;
import lynea.npc.actions.ActionMark;
import lynea.npc.actions.MoveAction;
import lynea.npc.actions.WaitAction;
import lynea.npc.interrupts.InterruptListener;
import lynea.player.Player;
/**
 *
 * @author Olivier
 */
public class NPC extends PhysicalEntity
{
    public static float walkSpeed = 1.8f;
    public static final int maxStopTime = 300; //in milliseconds

    private ActionGroup rootActionGroup;
    static private long maxID = 0;
    private long id;
    private AssetOwner owner = null;
    public static List<NPC> all;

    /**
     * retreive all NPC from database, create the corresponding NPC objects and
     * resume each action list from its last saved state
     */
    public static void init()
    {
        all = new ArrayList<NPC>();
        NPC testNPC = new NPC(Player.all.get("oli"));
        testNPC.setPosition(-5.0f, 0.0f, 5.0f);
        all.add(testNPC);

        WaitAction wait0 = new WaitAction("Wait0", testNPC, 7000);
        testNPC.addAction(wait0);
        ActionMark finish1 = new ActionMark(5.0f,0.0f,5.0f, Player.all.get("oli"), true);
        MoveAction move1 = new MoveAction("Move1", testNPC, finish1);
        testNPC.addAction(move1);
        WaitAction wait1 = new WaitAction("Wait1", testNPC, 3000);
        testNPC.addAction(wait1);
        ActionMark finish2 = new ActionMark(-4.0f,0.0f,-5.0f, Player.all.get("oli"), true);
        MoveAction move2 = new MoveAction("Move2", testNPC, finish2);
        testNPC.addAction(move2);
        WaitAction wait2= new WaitAction("Wait2", testNPC, 4000);
        testNPC.addAction(wait2);
        ActionMark finish3 = new ActionMark(-4.0f,0.0f,6.0f, Player.all.get("oli"), true);
        MoveAction move3 = new MoveAction("Move3", testNPC, finish3);
        testNPC.addAction(move3);
        ActionMark finish4 = new ActionMark(0.0f,0.0f,0.0f, Player.all.get("oli"), true);
        MoveAction move4 = new MoveAction("Move4", testNPC, finish4);
        testNPC.addAction(move4);
        WaitAction wait4= new WaitAction("Wait4", testNPC, 2000);
        testNPC.addAction(wait4);

        testNPC.startAction();
    }
    public static void updateAll(int deltaTime)
    {
        if(all == null)
            return;
        for(NPC npc : all)
        {
            npc.updateAction(deltaTime);
        }
    }
    

    public NPC()
    {
        id = maxID++;
        setName("npc_"+String.valueOf(id));
        rootActionGroup = new ActionGroup("npc_"+String.valueOf(id)+".root", this);
    }
    public NPC(AssetOwner owner)
    {
        this();
        this.owner = owner;
    }
    public boolean addAction(Action action)
    {
        action.setParent(rootActionGroup);
        return rootActionGroup.add(action);    
    }
    public boolean removeAction(Action action)
    {
        return rootActionGroup.remove(action);
    }


    public <T extends InterruptListener> List<T> getListeners(Class<T> interruptListenerClass)
    {
       
       List<T> interruptListeners = new ArrayList<T>();
       rootActionGroup.addListenersToList(interruptListeners, interruptListenerClass); //works recursively
       if (interruptListeners.isEmpty())
       {
           interruptListeners = null;
       }
       return interruptListeners;
    }

    public boolean updateAction(int deltaTime)
    {      
        return rootActionGroup.update(deltaTime);
    }
    public void startAction()
    {
        rootActionGroup.start();
    }

    public AssetOwner getOwner()
    {
        return this.owner;
    }

    @Override
    synchronized public void setAnimation(String animation)
    {
        if(!animation.equals(getAnimation()))
        {
            super.setAnimation(animation);
            if (animation.equals("idle1"))
                super.setSpeed(0.0f);
            else if(animation.equals("walk"))
                super.setSpeed(NPC.walkSpeed);
        }
    }
    @Override
    synchronized public void setSpeed(float speed)
    {     
        if(speed != getSpeed())
        {
            super.setSpeed(speed);
            if(speed == 0.0f)
                super.setAnimation("idle1");
            else if (speed > 0.0f && speed <= NPC.walkSpeed)
                super.setAnimation("walk");
        }
    }

    @Override
    synchronized public void updateHeading(int deltaTime)
    {
        super.updateHeading(deltaTime);
        if (getSpeed() == 0.0f)
            super.setAnimation("idle1");
        else if (getSpeed() > 0.0f && getSpeed() <= NPC.walkSpeed)
            super.setAnimation("walk");
    }


    public void stop(long stopTime)
    {
        //if no stopTime is provided, use the default NPC.maxStopTime
        stopTime = (stopTime>0) ? stopTime : NPC.maxStopTime;
        System.out.println("<"+WorldUpdater.getInstance().getSimulationTime()+"> stopping in "+stopTime+" ms");
        setAccelerationTime(stopTime);
        setEndSpeed(0);
    }

    @Override
    synchronized public boolean animationHasChanged(Player animationReceiver)
    {
        //by using the npc speed, the client is able to determine if the current animation of the npc is "idle1" or "walk".
        //So, we don't need to send these messages. As a result, we will return false if one of these two animations has been set
        if(!animation.equals("idle1") && !animation.equals("walk"))
        {
            return super.animationHasChanged(animationReceiver);
        }
        return false;
    }

    /*
     * mined quantity per unit of time [kg/sec]
     */
    public double getMiningSpeed()
    {
        throw new UnsupportedOperationException("getMiningSpeed : Not yet implemented");
    }



}
