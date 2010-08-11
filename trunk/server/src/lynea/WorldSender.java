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
        sendAllPlayerTransforms();
        sendAllNPCActions();
        sendAllNPCTransforms();

    }
    
    private void sendAllPlayerTransforms()
    {
        for(Player receiver : Player.connected.values())
        {
            //if(!receiver.canReceive())
            //    continue;
            sendPlayerTransformsToReceiver(receiver, false);
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
    private void sendAllNPCTransforms()
    {
        for(Player receiver : Player.connected.values())
        {
            sendNPCTransformsToReceiver(receiver);
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
    synchronized public void sendPlayerTransformsToReceiver(Player receiver, boolean forceSend)
    {
        //TODO: interest management for player info sending
        ActionscriptObject res = new ActionscriptObject();
        res.put("_cmd","t");
        ActionscriptObject arrPlayers = new ActionscriptObject();
        int i=0;
        for(Player p : Player.connected.values())
        {
            if(p.getUser().getUserId() != receiver.getUser().getUserId())
            {
                if(!forceSend && !p.transformHasChanged(receiver))
                    continue;
                double x = p.getX();
                double y = p.getY();
                double z = p.getZ();
                //double rx = p.getRX();
                double ry = p.getRY();
                //double rz = p.getRZ();
                double w = p.getW();
                //extension.trace("sending pos=("+x+","+y+","+z+") rot=("+rx+","+ry+","+rz+","+w+")");
                ActionscriptObject nearPlayerAO = new ActionscriptObject();

                nearPlayerAO.putNumber("x", x);
                nearPlayerAO.putNumber("y", y);
                nearPlayerAO.putNumber("z", z);
                //nearPlayerAO.putNumber("rx", rx);
                nearPlayerAO.putNumber("ry", ry);
                //nearPlayerAO.putNumber("rz", rz);
                nearPlayerAO.putNumber("w", w);
                nearPlayerAO.putNumber("uid",p.getUser().getUserId());
                nearPlayerAO.put("n", p.getName());
                arrPlayers.put(String.valueOf(i), nearPlayerAO);
                i++;
                //extension.trace("DEBUG: put transform of user"+String.valueOf(p.getUser().getUserId()));
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
            //extension.trace("DEBUG: transform(s) sent to user : "+String.valueOf(receiver.getUser().getUserId()));
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
    synchronized public void sendTransformOfSender(Player sender)
    {
        ActionscriptObject res = new ActionscriptObject();
        res.put("_cmd", "t");
        ActionscriptObject arrPlayers = new ActionscriptObject();
        ActionscriptObject newPlayerAO = new ActionscriptObject();
        newPlayerAO.putNumber("x", sender.getX());
        newPlayerAO.putNumber("y", sender.getY());
        newPlayerAO.putNumber("z", sender.getZ());
        //newPlayerAO.putNumber("rx", sender.getRX());
        newPlayerAO.putNumber("ry", sender.getRY());
        //newPlayerAO.putNumber("rz", sender.getRZ());
        newPlayerAO.putNumber("w", sender.getW());
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
    private void sendNPCTransformsToReceiver(Player receiver)
    {
        //TODO: interest management for npc info sending
        ActionscriptObject res = new ActionscriptObject();
        res.put("_cmd","t");
        ActionscriptObject arrNPCs = new ActionscriptObject();
        int i=0;
        for(NPC npc : NPC.all)
        {
            if(!npc.transformHasChanged(receiver))
                continue;
            double x = npc.getX();
            double y = npc.getY();
            double z = npc.getZ();
            double ry = npc.getRY();
            double w = npc.getW();
            ActionscriptObject nearNPCAO = new ActionscriptObject();

            nearNPCAO.putNumber("x", x);
            nearNPCAO.putNumber("y", y);
            nearNPCAO.putNumber("z", z);
            nearNPCAO.putNumber("ry", ry);
            nearNPCAO.putNumber("w", w);
            //mark the actionscriptobject as an npc
            nearNPCAO.putNumber("uid",-1);
            nearNPCAO.put("n", npc.getName());
            arrNPCs.put(String.valueOf(i), nearNPCAO);
            i++;
            //System.out.println("DEBUG: put NPC tsf p=("+(double)((int)(100*x))/100+","+(double)((int)(100*y))/100+","+(double)((int)(100*z))/100+") r=(,"+(double)((int)(100*ry))/100+",,"+(double)((int)(100*w))/100+") for u="+String.valueOf(receiver.getUser().getUserId()));

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
