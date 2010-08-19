/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lynea;

import java.util.Calendar;

/**
 *
 * @author Olivier
 */
public class Clock
{
    private static Calendar serverstart = null;

    public static void initClock()
    {
        serverstart = Calendar.getInstance();
    }
    /*
     * This method gives the server REAL time. It should be used by
     * WorldUpdater.run() only. Other classes should use the server SIMULATION
     * time. You can get it by using WorldUpdater.getInstance().getSimulationTime()
     */
    public static long getTime()
    {
        if (serverstart == null)
            initClock();
        Calendar now = Calendar.getInstance();
        return (now.getTimeInMillis() - serverstart.getTimeInMillis());
    }

}
