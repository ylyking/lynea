/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lynea;

import lynea.npc.actions.Deposit;
import lynea.npc.actions.Deposit.Type;
/**
 *
 * @author Olivier
 */
public class Inventory
{
    double [] deposits;
    double volume;
    double freeVolume;
    public Inventory(double volume)
    {
        this.volume = volume;
        freeVolume = this.volume;
        deposits = new double[Deposit.Type.values().length];
    }
    public void addDeposit(Deposit.Type type, double quantity)
    {
        deposits[type.ordinal()] += quantity;
        freeVolume -= quantity * type.getVolumePerUnit();
    }

    public double getDeposit(Deposit.Type type)
    {
        return deposits[type.ordinal()];
    }

    public boolean canAddDeposit(Deposit.Type type, double quantity)
    {
        return ((freeVolume - quantity * type.getVolumePerUnit()) >= 0);
    }

}
