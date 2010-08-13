/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lynea.network;

import it.gotoandplay.smartfoxserver.data.User;
import it.gotoandplay.smartfoxserver.extensions.AbstractExtension;
import it.gotoandplay.smartfoxserver.lib.ActionscriptObject;
import java.util.LinkedList;
import lynea.Clock;

/**
 *
 * @author Olivier
 */
public class ClockRequestHandler implements IRequestHandler
{
    private AbstractExtension extension;

    public void setExtension(AbstractExtension ext) {
        this.extension = ext;
    }

    public void onRequest(ActionscriptObject ao, User user, int fromRoom)
    {
        sendTime(user);
    }
    private void sendTime(User user)
    {
        ActionscriptObject res = new ActionscriptObject();
        res.putNumber("t", Clock.getTime());
        LinkedList userList = new LinkedList();
        userList.add(user.getChannel());
        extension.sendResponse(res, -1, null, userList);
    }

}
