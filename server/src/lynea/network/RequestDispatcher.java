package lynea.network;

import it.gotoandplay.smartfoxserver.data.User;
import it.gotoandplay.smartfoxserver.data.Zone;
import it.gotoandplay.smartfoxserver.events.InternalEventObject;
import it.gotoandplay.smartfoxserver.extensions.AbstractExtension;
import it.gotoandplay.smartfoxserver.extensions.ExtensionHelper;
import it.gotoandplay.smartfoxserver.lib.ActionscriptObject;
import java.util.HashMap;
import lynea.WorldSender;
import lynea.WorldUpdater;
import lynea.npc.NPC;
import lynea.player.Player;

/**
 *
 * @author Olivier
 */
public class RequestDispatcher extends AbstractExtension{

    private ExtensionHelper helper;
    private Zone currentZone;
    private HashMap<String, String> requestHandlers;
    private HashMap<String, String> internalEventHandlers;

    private WorldSender worldSender;
    private WorldUpdater worldUpdater;

    
    /**
     * Initialize the extension.<br>
     */
    @Override
    public void init()
    {
            helper = ExtensionHelper.instance();
            currentZone = helper.getZone(this.getOwnerZone());
            initInvocationTable();
            trace("Lynea Extension is initialized");
            Player.init();
            NPC.init();
            worldSender = new WorldSender(this);
            worldUpdater = new WorldUpdater(worldSender);
            worldUpdater.start();
    }


    private void initInvocationTable()
    {
        requestHandlers = new HashMap<String, String>();
        requestHandlers.put("h", "MovementRequestHandler");
        requestHandlers.put("a", "ActionRequestHandler");

        internalEventHandlers = new HashMap<String, String>();
        internalEventHandlers.put("userJoin", "JoinEventHandler");
        internalEventHandlers.put("userExit", "ExitEventHandler");
        internalEventHandlers.put("userLost", "ExitEventHandler");
    }

    /**
     * Destroy the extension
     */
    @Override
    public void destroy()
    {
            trace("Lynea Extension is shutting down");
    }

    /**
    * Handle client requests sent in XML format.
    * The AS objects sent by the client are serialized to an ActionscriptObject
    *
    * @param ao 		the ActionscriptObject with the serialized data coming from the client
    * @param cmd 		the cmd name invoked by the client
    * @param fromRoom 	the id of the room where the user is in
    * @param user 		the User who sent the message
    */
    public void handleRequest(String cmd, ActionscriptObject ao, User user, int fromRoom)
    {
        String handlerClassName = requestHandlers.get(cmd);

	if (handlerClassName != null)
	{
		try
		{
			// Obtain the class object
			Class handlerClass = Class.forName(this.getClass().getPackage().getName()+"."+handlerClassName);

			// Create a new instance from its class
			IRequestHandler handler = (IRequestHandler) handlerClass.newInstance();

			// Pass a reference of the extension
			handler.setExtension(this);

			// Handle the request
			handler.onRequest(ao, user, fromRoom);
		}
		catch(ClassNotFoundException issue)
		{
			trace("Class was not found:" + handlerClassName);
		}
		catch(InstantiationException issue)
		{
			trace("Could not instantiate class:" + handlerClassName + ", Exception: " + issue);
		}
                catch(IllegalAccessException issue)
		{
			trace("Could not instantiate class:" + handlerClassName + ", Exception: " + issue);
		}
	}
	else
		throw new UnsupportedOperationException("Unknow request id: " + cmd);
      
    }

    public void handleRequest(String cmd, String[] strings, User user, int fromRoom) {
        throw new UnsupportedOperationException("Error: this version of handleRequest (String, String[], User, int)  has not been implemented");
    }

    public void handleInternalEvent(InternalEventObject ieo) 
    {
        String evtName = ieo.getEventName();
        String handlerClassName = internalEventHandlers.get(evtName);

        if (handlerClassName != null)
        {
                try
                {
                        // Obtain the class object
                        Class handlerClass = Class.forName(this.getClass().getPackage().getName()+"."+handlerClassName);

                        // Create a new instance from its class
                        IInternalEventHandler handler = (IInternalEventHandler) handlerClass.newInstance();

                        // Pass a reference of the extension
                        handler.setSender(worldSender);

                        // Handle the request
                        handler.onEvent(ieo);
                }
                catch(ClassNotFoundException issue)
                {
                        trace("Class was not found:" + handlerClassName);
                }
                catch(InstantiationException issue)
                {
                        trace("Could not instantiate class:" + handlerClassName + ", Exception: " + issue);
                }
                catch(IllegalAccessException issue)
                {
                        trace("Could not instantiate class:" + handlerClassName + ", Exception: " + issue);
                }
        }

    }



}
