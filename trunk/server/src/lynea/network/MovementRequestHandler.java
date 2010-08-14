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
            float x = (float) ao.getNumber("x");
            float y = (float) ao.getNumber("y");
            float z = (float) ao.getNumber("z");
            float alpha = (float) ao.getNumber("a");
            long t = (long) ao.getNumber("t");
            float speed = (float) ao.getNumber("s");//no yet used
            
            long accelerationTime = (long) ao.getNumber("at");//no yet used
            float endSpeed;//no yet used
            if (accelerationTime > 0)
                endSpeed = (float) ao.getNumber("es");
            else
            {
                endSpeed = -1;
                accelerationTime = -1;
            }

            //no validating for the moment
            //TODO: validate received transform
            int uid = user.getUserId();
            Player.connected.get(uid).setPosition(x, y, z);
            Player.connected.get(uid).setAngle(alpha);
            Player.connected.get(uid).setHeadingUpdateTime(t);

            float aa = ((float)Math.round(alpha * 1000))/1000;
            float xx = ((float)Math.round(x * 1000))/1000;
            float yy = ((float)Math.round(y * 1000))/1000;
            float zz = ((float)Math.round(z * 1000))/1000;
            float ss = ((float)Math.round(speed * 1000))/1000;
            System.out.println("SAV pos=("+xx+","+yy+","+zz+") alpha=("+aa+") s=("+ss+")");
    }

}
