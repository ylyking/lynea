/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lynea.npc.actions;

import lynea.PhysicalEntity;
import lynea.npc.NPC;

/**
 *
 * @author Olivier
 */
public class Deposit extends PhysicalEntity
{

    private void delete()
    {
        //delete deposit in the db
        throw new UnsupportedOperationException("delete Deposit : Not yet implemented");
    }
    public enum Type
    {
        STONE(0.001, 0.01),
        SILVER(0.01, 0.02);

        private double volumePerUnit;
        private double massPerUnit;
        Type(double volumePerUnit, double massPerUnit)
        {
            this.volumePerUnit = volumePerUnit;
            this.massPerUnit = massPerUnit;
        }
        public double getVolumePerUnit()
        {
            return volumePerUnit;
        }
        public double getMassPerUnit()
        {
            return massPerUnit;
        }
        
    };
    private double quantity;
    private Type type;
    public void mine(double quantity, NPC npc)
    {
        double minedQuantity = Math.min(quantity, this.quantity);
        this.quantity -= minedQuantity;
        npc.getInventory().addDeposit(type, minedQuantity);
        if (this.quantity ==  0)
            delete();
    }
    public Type getType() { return type; }
    public boolean isEmpty()
    {
        return (this.quantity == 0);
    }
}
