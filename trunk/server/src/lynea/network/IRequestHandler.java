/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lynea.network;

import it.gotoandplay.smartfoxserver.data.User;
import it.gotoandplay.smartfoxserver.extensions.AbstractExtension;
import it.gotoandplay.smartfoxserver.lib.ActionscriptObject;

/**
 *
 * @author Olivier
 */
public interface IRequestHandler
{
	public void setExtension(AbstractExtension ext);
	public void onRequest(ActionscriptObject ao, User user, int fromRoom);
}