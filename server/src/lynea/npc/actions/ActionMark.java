/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lynea.npc.actions;

import lynea.AssetOwner;
import lynea.PhysicalEntity;

/**
 *
 * @author Olivier
 */
public class ActionMark extends PhysicalEntity implements Cloneable
{
    public AssetOwner markOwner;

    private boolean isVisible = true;

    public ActionMark(float x, float y, float z, AssetOwner markOwner, boolean isVisible)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.markOwner = markOwner;
        this.isVisible = isVisible;
    }

    @Override
    public Object clone()
    {
        try
        {
          ActionMark cloned = (ActionMark)super.clone();
          return cloned;
        }
        catch(CloneNotSupportedException e)
        {
          System.out.println(e);
          return null;
        }
    }

    public void setVisible(boolean status)
    {
        isVisible = status;
    }
}
