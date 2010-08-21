/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lynea.network;

import it.gotoandplay.smartfoxserver.events.InternalEventObject;

/**
 *
 * @author Olivier
 */
public interface IInternalEventHandler {
    	public void setSender(WorldSender sender);
	public void onEvent(InternalEventObject ieo);

}
