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
public class MovementRequestHandler implements IRequestHandler
{
    private AbstractExtension extension;

    public void setExtension(AbstractExtension ext)
    {
        this.extension = ext;
    }

    public void onRequest(ActionscriptObject ao, User user, int fromRoom)
    {
            //extension.trace("DEBUG: rcvd transform from uid="+String.valueOf(user.getUserId()));
            float x = (float) ao.getNumber("x");
            float y = (float) ao.getNumber("y");
            float z = (float) ao.getNumber("z");
            float alpha = (float) ao.getNumber("a");
            long t = (long) ao.getNumber("t");

            //no validating for the moment
            //TODO: validate received transform
            int uid = user.getUserId();
            Player.connected.get(uid).setPosition(x, y, z);
            Player.connected.get(uid).setAngle(alpha);
            Player.connected.get(uid).setHeadingUpdateTime(t);
            
            //System.out.println("DEBUG: sav pos=("+x+","+y+","+z+") alpha=(,"+((double)((int)alpha/Math.PI*180*100))/100+")");
    }

}
