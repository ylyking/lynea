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
            double x = ao.getNumber("x");
            double y = ao.getNumber("y");
            double z = ao.getNumber("z");
            //double rx = ao.getNumber("rx");
            double ry = ao.getNumber("ry");
            //double rz = ao.getNumber("rz");
            double w = ao.getNumber("w");

            //no validating for the moment
            //TODO: validate received transform
            int uid = user.getUserId();
            Player.connected.get(uid).setTransform(x, y, z, 0.0, ry, 0.0, w);
            
            //System.out.println("DEBUG: sav pos=("+x+","+y+","+z+") rot=(,"+ry+",,"+w+")");
            //double alpha_ry = Math.asin(ry)*2/Math.PI*180;
            //double alpha_w = Math.acos(w)*2/Math.PI*180;
            double alpha_atan2 = Math.atan2(ry, w)*2/Math.PI*180;
            //alpha_atan2 = (alpha_atan2 > 180)? alpha_atan2 - 360 : alpha_atan2;
            System.out.println("DEBUG: curr angl : "+((double)((int)alpha_atan2*100))/100+" (atan2)");
    }

}
