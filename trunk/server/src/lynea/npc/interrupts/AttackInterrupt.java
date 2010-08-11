/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lynea.npc.interrupts;

import lynea.PhysicalEntity;

/**
 *
 * @author Olivier
 */
public class AttackInterrupt {

    private PhysicalEntity attacker;
    public AttackInterrupt(PhysicalEntity attacker)
    {
        this.attacker = attacker;
    }
    public PhysicalEntity getAttacker()
    {
        return attacker;
    }

}
