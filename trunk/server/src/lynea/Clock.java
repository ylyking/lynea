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
    public static long getTime()
    {
        if (serverstart == null)
            initClock();
        Calendar now = Calendar.getInstance();
        return (now.getTimeInMillis() - serverstart.getTimeInMillis());
    }

}
