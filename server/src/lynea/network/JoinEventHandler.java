/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lynea.network;

import it.gotoandplay.smartfoxserver.data.User;
import it.gotoandplay.smartfoxserver.events.InternalEventObject;
import it.gotoandplay.smartfoxserver.extensions.AbstractExtension;
import it.gotoandplay.smartfoxserver.lib.ActionscriptObject;
import java.util.LinkedList;
import lynea.player.Player;

/**
 *
 * @author Olivier
 */
public class JoinEventHandler implements IInternalEventHandler{
    private WorldSender sender;

    public void setSender(WorldSender sender) {
        this.sender = sender;
    }

    public void onEvent(InternalEventObject ieo)
    {
            //get the new user
            User u = (User) ieo.getObject("user");
            System.out.println("DEBUG: new user has joined : "+u.getName());

            Player.hasConnected(u);

            //create a player for the new user
            //Player player = new Player(u);
            //add the players to our list of players
            //we use the userId number as the key
            // Player.connected.put(uId,player);

            //this is not needed actually :
            //send the new user the info about the other players in his interest zone
            //sender.sendPlayerTransformsToReceiver(player, true);
            //this is not needed actually :
            //also send the new user the current animations of the near players
            //sender.sendPlayerActionsToReceiver(player, true);
            //this is not needed actually :
            //send the other player in the interest zone the info about the new player
            //sender.sendTransformOfSender(player);
    }

}
