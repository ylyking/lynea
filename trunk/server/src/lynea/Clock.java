/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lynea;

import java.sql.Timestamp;

/**
 *
 * @author Olivier
 */
public class Clock
{
    private static Timestamp timestamp = null;

    public static void initClock()
    {
        timestamp = new Timestamp(0);
    }
    public static long getTime()
    {
        if (timestamp == null)
            initClock();
        return timestamp.getTime();
    }

}
