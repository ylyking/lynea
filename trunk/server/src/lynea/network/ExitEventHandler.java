/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lynea.network;

import it.gotoandplay.smartfoxserver.events.InternalEventObject;
import it.gotoandplay.smartfoxserver.extensions.AbstractExtension;
import lynea.WorldSender;
import lynea.player.Player;

/**
 *
 * @author Olivier
 */
public class ExitEventHandler implements IInternalEventHandler
{
    private WorldSender sender;

    public void setSender(WorldSender sender) {
        this.sender = sender;
    }

    public void onEvent(InternalEventObject ieo)
    {
        // Get the user id
        int uId = Integer.valueOf(ieo.getParam("uid"));

        // Let's remove the player from the list
        Player.connected.remove(uId);

        //TODO: notify the other players in the interest zone that the user has left
    }

}
