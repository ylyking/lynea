/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lynea;

import it.gotoandplay.smartfoxserver.extensions.AbstractExtension;
import it.gotoandplay.smartfoxserver.lib.ActionscriptObject;
import java.util.LinkedList;
import lynea.npc.NPC;
import lynea.player.Player;

/**
 *
 * @author Olivier
 */
public class WorldSender
{
    private AbstractExtension extension;
    
    public WorldSender(AbstractExtension ext)
    {
        this.extension = ext;
    }

    public void sendWorldState()
    {
        sendAllPlayerActions();
        sendAllPlayerHeadings();
        sendAllNPCActions();
        sendAllNPCHeadings();

    }
    
    private void sendAllPlayerHeadings()
    {
        for(Player receiver : Player.connected.values())
        {
            //if(!receiver.canReceive())
            //    continue;
            sendPlayerHeadingsToReceiver(receiver, false);
        }
    }
    private void sendAllPlayerActions()
    {
        for(Player receiver : Player.connected.values())
        {
            //if(!receiver.canReceive())
            //    continue;
            sendPlayerActionsToReceiver(receiver, false);
        }
    }
    private void sendAllNPCHeadings()
    {
        for(Player receiver : Player.connected.values())
        {
            sendNPCHeadingsToReceiver(receiver);
        }

    }
    private void sendAllNPCActions()
    {
        for(Player receiver : Player.connected.values())
        {
            sendNPCActionsToReceiver(receiver);
        }
        
    }

    /*
     * send the receiver the transforms of all the players in his interest zone
     */
    synchronized public void sendPlayerHeadingsToReceiver(Player receiver, boolean forceSend)
    {
        //TODO: interest management for player info sending
        ActionscriptObject res = new ActionscriptObject();
        res.put("_cmd","h");
        ActionscriptObject arrPlayers = new ActionscriptObject();
        int i=0;
        for(Player p : Player.connected.values())
        {
            if(p.getUser().getUserId() != receiver.getUser().getUserId())
            {
                if(!forceSend && !p.headingHasChanged(receiver))
                    continue;
                double x = (double) p.getX();
                double y = (double) p.getY();
                double z = (double) p.getZ();
                double alpha = (double) p.getAngle();
                double t = (double) p.getHeadingUpdateTime();
                double s = (double) p.getSpeedForCurrentAnimation();
                //extension.trace("sending pos=("+x+","+y+","+z+") alph=("+alpha+")");
                ActionscriptObject nearPlayerAO = new ActionscriptObject();

                nearPlayerAO.putNumber("x", x);
                nearPlayerAO.putNumber("y", y);
                nearPlayerAO.putNumber("z", z);
                nearPlayerAO.putNumber("a", alpha);
                nearPlayerAO.putNumber("t", t);
                nearPlayerAO.putNumber("s", s);

                nearPlayerAO.putNumber("uid",p.getUser().getUserId());
                nearPlayerAO.put("n", p.getName());
                arrPlayers.put(String.valueOf(i), nearPlayerAO);
                i++;
                //extension.trace("DEBUG: put heading of user"+String.valueOf(p.getUser().getUserId()));
            }
        }
        res.put("p",arrPlayers);

        // Send response to user
        LinkedList newPlayerLL = null;
        if (i > 0)
        {
            newPlayerLL = new LinkedList();
            newPlayerLL.add(receiver.getUser().getChannel());
            extension.sendResponse(res, -1, null, newPlayerLL);
            //extension.trace("DEBUG: heading(s) sent to user : "+String.valueOf(receiver.getUser().getUserId()));
        }
    }
    /*
     * send the receiver the actions of all the players in his interest zone
     */
    synchronized public void sendPlayerActionsToReceiver(Player receiver, boolean forceSend)
    {
        //TODO: interest management for player info sending
        ActionscriptObject res = new ActionscriptObject();
        res.put("_cmd","a");
        ActionscriptObject arrPlayers = new ActionscriptObject();
        int i=0;
        for(Player p : Player.connected.values())
        {
            if(p.getUser().getUserId() != receiver.getUser().getUserId())
            {
                if(!forceSend && !p.animationHasChanged(receiver))
                    continue;
                ActionscriptObject nearPlayerAO = new ActionscriptObject();
                nearPlayerAO.put("mes", p.getAnimation());
                nearPlayerAO.putNumber("uid",p.getUser().getUserId());
                nearPlayerAO.put("n",p.getName());
                arrPlayers.put(String.valueOf(i), nearPlayerAO);
                i++;
                //extension.trace("DEBUG: put anim=["+p.getAnimation()+"] for user="+String.valueOf(p.getUser().getUserId()));
            }
        }
        res.put("p",arrPlayers);

        // Send response to user
        LinkedList newPlayerLL = null;
        if (i > 0)
        {
            newPlayerLL = new LinkedList();
            newPlayerLL.add(receiver.getUser().getChannel());
            extension.sendResponse(res, -1, null, newPlayerLL);
            //extension.trace("DEBUG: anim(s) sent to user : "+String.valueOf(receiver.getUser().getUserId()));
        }
    }
    /*
     * send the transform of a player to all the players in his interest zone
     */
    synchronized public void sendHeadingOfSender(Player sender)
    {
        ActionscriptObject res = new ActionscriptObject();
        res.put("_cmd", "h");
        ActionscriptObject arrPlayers = new ActionscriptObject();
        ActionscriptObject newPlayerAO = new ActionscriptObject();
        newPlayerAO.putNumber("x", sender.getX());
        newPlayerAO.putNumber("y", sender.getY());
        newPlayerAO.putNumber("z", sender.getZ());
        newPlayerAO.putNumber("a", sender.getAngle());
        newPlayerAO.putNumber("t", sender.getHeadingUpdateTime());
        newPlayerAO.putNumber("s", sender.getSpeedForCurrentAnimation());
        newPlayerAO.putNumber("uid", sender.getUser().getUserId());
        newPlayerAO.put("n", sender.getName());
        arrPlayers.put("0", newPlayerAO);
        res.put("p",arrPlayers);

        //TODO: interest management
        LinkedList nearPlayersLL = Player.getAllUserChannelsButOne(sender.getUser());
        extension.sendResponse(res, -1, null, nearPlayersLL);
    }
    
    private void sendNPCActionsToReceiver(Player receiver)
    {
        //TODO: interest management for player info sending
        ActionscriptObject res = new ActionscriptObject();
        res.put("_cmd","a");
        ActionscriptObject arrNPCs = new ActionscriptObject();
        int i=0;
        for(NPC npc : NPC.all)
        {
            if(!npc.animationHasChanged(receiver))
                continue;
            ActionscriptObject nearNPCAO = new ActionscriptObject();
            nearNPCAO.put("mes", npc.getAnimation());
            nearNPCAO.putNumber("uid",-1);
            nearNPCAO.put("n",npc.getName());
            arrNPCs.put(String.valueOf(i), nearNPCAO);
            i++;
            //extension.trace("DEBUG: put NPC anim=["+npc.getAnimation()+"] for user="+String.valueOf(receiver.getUser().getUserId()));

        }
        res.put("p",arrNPCs);

        // Send response to user
        LinkedList newPlayerLL = null;
        if (i > 0)
        {
            newPlayerLL = new LinkedList();
            newPlayerLL.add(receiver.getUser().getChannel());
            extension.sendResponse(res, -1, null, newPlayerLL);
            //extension.trace("DEBUG: NPC anim(s) sent to user : "+String.valueOf(receiver.getUser().getUserId()));
        }
    }
    private void sendNPCHeadingsToReceiver(Player receiver)
    {
        //TODO: interest management for npc info sending
        ActionscriptObject res = new ActionscriptObject();
        res.put("_cmd","h");
        ActionscriptObject arrNPCs = new ActionscriptObject();
        int i=0;
        for(NPC npc : NPC.all)
        {
            if(!npc.headingHasChanged(receiver))
            {
                continue;
            }

            double x = (double) npc.getX();
            double y = (double) npc.getY();
            double z = (double) npc.getZ();
            double alpha = (double) npc.getAngle();
            double t = (double) npc.getHeadingUpdateTime();
            double s = (double) npc.getSpeedForCurrentAnimation();
            
            ActionscriptObject nearNPCAO = new ActionscriptObject();

            nearNPCAO.putNumber("x", x);
            nearNPCAO.putNumber("y", y);
            nearNPCAO.putNumber("z", z);
            nearNPCAO.putNumber("a", alpha);
            nearNPCAO.putNumber("t", t);
             nearNPCAO.putNumber("s", s);

            //mark the actionscriptobject as an npc
            nearNPCAO.putNumber("uid",-1);
            nearNPCAO.put("n", npc.getName());
            arrNPCs.put(String.valueOf(i), nearNPCAO);
            i++;
            System.out.println("DEBUG: sending NPC heading p=("+(double)((int)(100*x))/100+","+(double)((int)(100*y))/100+","+(double)((int)(100*z))/100+") angle="+(double)((int)(100*alpha/Math.PI*180))/100+String.valueOf(receiver.getUser().getUserId()));

        }
        res.put("p",arrNPCs);

        // Send response to user
        LinkedList newPlayerLL = null;
        if (i > 0)
        {
            newPlayerLL = new LinkedList();
            newPlayerLL.add(receiver.getUser().getChannel());
            extension.sendResponse(res, -1, null, newPlayerLL);
            //extension.trace("DEBUG: NPC transform(s) sent to user : "+String.valueOf(receiver.getUser().getUserId()));
        }

    }
    
    public void trace(String str)
    {
        extension.trace(str);
    }

}
