/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lynea.npc.interrupts;

/**
 *
 * @author Olivier
 */
public interface AttackListener extends InterruptListener
{
    public void onAttack(AttackInterrupt attackInterrupt);
}
