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
    private static WorldSender instance = null;

    private WorldSender(AbstractExtension ext)
    {
        this.extension = ext;
    }

    public static WorldSender getInstance(AbstractExtension ext)
    {
        if (instance == null)
            instance = new WorldSender(ext);
        return instance;
    }
    public static WorldSender getInstance()
    {
        return instance;
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
            if(receiver.canReceive())
                sendPlayerHeadingsToReceiver(receiver, false);
        }
    }
    private void sendAllPlayerActions()
    {
        for(Player receiver : Player.connected.values())
        {
            if(receiver.canReceive())
                sendPlayerActionsToReceiver(receiver, false);
        }
    }
    private void sendAllNPCHeadings()
    {
        for(Player receiver : Player.connected.values())
        {
            if(receiver.canReceive())
                sendNPCHeadingsToReceiver(receiver);
        }

    }
    private void sendAllNPCActions()
    {
        for(Player receiver : Player.connected.values())
        {
            if(receiver.canReceive())
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
                double s = (double) p.getSpeed();
                double at = (double) p.getAccelerationTime();
                double es = (double) p.getEndSpeed();

                ActionscriptObject nearPlayerAO = new ActionscriptObject();

                nearPlayerAO.putNumber("x", x);
                nearPlayerAO.putNumber("y", y);
                nearPlayerAO.putNumber("z", z);
                nearPlayerAO.putNumber("a", alpha);
                nearPlayerAO.putNumber("t", t);
                nearPlayerAO.putNumber("s", s);

                nearPlayerAO.putNumber("at", at); //at == -1 if not accelerating
                if(p.isAccelerating())
                {
                    nearPlayerAO.putNumber("es", es);
                }
 

                nearPlayerAO.putNumber("uid",p.getUser().getUserId());
                nearPlayerAO.put("n", p.getName());
                arrPlayers.put(String.valueOf(i), nearPlayerAO);
                i++;
                String sent = "Send PC h "
                +"pos=("+(double)((int)(100*x))/100+","
                +(double)((int)(100*y))/100+","
                +(double)((int)(100*z))/100+") "
                + "a="+(double)((int)(100*alpha))/100+" "
                + "s="+(double)((int)(100*s))/100+" "
                + "es="+(double)((int)(100*es))/100+" "
                + "at="+(double)((int)(100*at))/100+" "
                + "t="+(double)((int)(100*t))/100+" "
                + "to "+receiver.getUser().getName();
                System.out.println(sent);
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
            double s = (double) npc.getSpeed();
            double at = (double) npc.getAccelerationTime();
            double es = (double) npc.getEndSpeed();

            ActionscriptObject nearNPCAO = new ActionscriptObject();

            nearNPCAO.putNumber("x", x);
            nearNPCAO.putNumber("y", y);
            nearNPCAO.putNumber("z", z);
            nearNPCAO.putNumber("a", alpha);
            nearNPCAO.putNumber("t", t);
            nearNPCAO.putNumber("s", s);

            nearNPCAO.putNumber("at", at); //at == -1 if not accelerating
            if(npc.isAccelerating())
            {
                nearNPCAO.putNumber("es", es);
            }

            //mark the actionscriptobject as an npc
            nearNPCAO.putNumber("uid",-1);
            nearNPCAO.put("n", npc.getName());
            arrNPCs.put(String.valueOf(i), nearNPCAO);
            i++;
            String sent = "Send NPC h "
                    +"pos=("+(double)((int)(100*x))/100+","
                    +(double)((int)(100*y))/100+","
                    +(double)((int)(100*z))/100+") "
                    + "a="+(double)((int)(100*alpha))/100+" "
                    + "s="+(double)((int)(100*s))/100+" "
                    + "es="+(double)((int)(100*es))/100+" "
                    + "at="+(double)((int)(100*at))/100+" "
                    + "t="+(double)((int)(100*t))/100+" "
                    + "to "+receiver.getUser().getName();
            System.out.println(sent);
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
    
    /*
     * send the heading of a player to all the players in his interest zone
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
        newPlayerAO.putNumber("s", sender.getSpeed());
        newPlayerAO.putNumber("at", sender.getAccelerationTime()); //at == -1 if not accelerating
        if(sender.isAccelerating())
        {
            newPlayerAO.putNumber("es", sender.getEndSpeed());
        }
        newPlayerAO.putNumber("uid", sender.getUser().getUserId());
        newPlayerAO.put("n", sender.getName());
        arrPlayers.put("0", newPlayerAO);
        res.put("p",arrPlayers);

        String sent = "[imm] Send PC h "
        +"pos=("+(double)((int)(100*sender.getX()))/100+","
        +(double)((int)(100*sender.getY()))/100+","
        +(double)((int)(100*sender.getZ()))/100+") "
        + "a="+(double)((int)(100*sender.getAngle()))/100+" "
        + "s="+(double)((int)(100*sender.getSpeed()))/100+" "
        + "es="+(double)((int)(100*sender.getEndSpeed()))/100+" "
        + "at="+(double)((int)(100*sender.getAccelerationTime()))/100+" "
        + "t="+(double)((int)(100*sender.getHeadingUpdateTime()))/100+" "
        + "to ALL";
        System.out.println(sent);

        LinkedList nearPlayersLL = new LinkedList();
        //TODO: interest management
        for(Player p : Player.connected.values())
        {
            if(sender.headingHasChanged(p))
                nearPlayersLL.add(p.getUser().getChannel());
        }
        extension.sendResponse(res, -1, null, nearPlayersLL);
    }
    /*
     * send the heading of an npc to all the players in his interest zone
     */
    synchronized public void sendHeadingOfSender(NPC sender)
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
        newPlayerAO.putNumber("s", sender.getSpeed());
        newPlayerAO.putNumber("at", sender.getAccelerationTime()); //at == -1 if not accelerating
        if(sender.isAccelerating())
        {
            newPlayerAO.putNumber("es", sender.getEndSpeed());
        }
        newPlayerAO.putNumber("uid", -1);
        newPlayerAO.put("n", sender.getName());
        arrPlayers.put("0", newPlayerAO);
        res.put("p",arrPlayers);

        String sent = "[imm] Send NPC h "
        +"pos=("+(double)((int)(100*sender.getX()))/100+","
        +(double)((int)(100*sender.getY()))/100+","
        +(double)((int)(100*sender.getZ()))/100+") "
        + "a="+(double)((int)(100*sender.getAngle()))/100+" "
        + "s="+(double)((int)(100*sender.getSpeed()))/100+" "
        + "es="+(double)((int)(100*sender.getEndSpeed()))/100+" "
        + "at="+(double)((int)(100*sender.getAccelerationTime()))/100+" "
        + "t="+(double)((int)(100*sender.getHeadingUpdateTime()))/100+" "
        + "to ALL";
        System.out.println(sent);

        LinkedList nearPlayersLL = new LinkedList();
        //TODO: interest management
        for(Player p : Player.connected.values())
        {
            if(sender.headingHasChanged(p))
                nearPlayersLL.add(p.getUser().getChannel());
        }
        extension.sendResponse(res, -1, null, nearPlayersLL);
    }
    public void trace(String str)
    {
        extension.trace(str);
    }


}
