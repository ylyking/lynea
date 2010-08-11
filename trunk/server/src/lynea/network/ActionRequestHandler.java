/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lynea.network;

import it.gotoandplay.smartfoxserver.data.User;
import it.gotoandplay.smartfoxserver.extensions.AbstractExtension;
import it.gotoandplay.smartfoxserver.lib.ActionscriptObject;
import lynea.player.Player;

/**
 *
 * @author Olivier
 */
public class ActionRequestHandler implements IRequestHandler
{
    private AbstractExtension extension;

    public void setExtension(AbstractExtension ext) {
        this.extension = ext;
    }

    public void onRequest(ActionscriptObject ao, User user, int fromRoom)
    {
        int uid = user.getUserId();
        extension.trace("DEBUG: r anim from uid="+String.valueOf(uid));
        String mes = (String) ao.get("mes");

        //nothing to validate at the moment
        Player.connected.get(uid).setAnimation(mes);
        /*
        ActionscriptObject res = new ActionscriptObject();
        res.put("_cmd", "a");
        res.put("mes", mes);
        res.putNumber("uid", uid);

        //TODO: interest management for animation sending
        LinkedList<SocketChannel> recipientList = Player.getAllUserChannelsButOne(user);
        // Send response
        extension.sendResponse(res, -1, null, recipientList);
        */
    }

}
