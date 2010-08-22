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
        
        String mes = (String) ao.get("mes");
        System.out.println("Received "+mes+" from uid="+String.valueOf(uid));
        //nothing to validate at the moment
        Player.connected.get(uid).setAnimation(mes);
  
    }

}
