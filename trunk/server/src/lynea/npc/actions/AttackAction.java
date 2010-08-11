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
public class AttackAction extends ActionElement
{
    public enum StopCondition {DEAD, ESCAPE}
    public enum EscapeCondition {NEVER, LOWHEALTH}
    public float escapeHealth = 0.20f;

    private PhysicalEntity target;
    private StopCondition stopCondition;
    private EscapeCondition escapeCondition;

    public AttackAction(String name, NPC npc, PhysicalEntity target, StopCondition stopCondition, EscapeCondition escapeCondition)
    {
        super(name, npc);
        this.target = target;
        this.stopCondition = stopCondition;
        this.escapeCondition = escapeCondition;
    }
    public AttackAction(String name, NPC npc, PhysicalEntity target)
    {
        this(name, npc, target, StopCondition.DEAD, EscapeCondition.NEVER);
    }
    int counter=0;
    @Override
    public boolean update(double deltaTime)
    {
        System.out.println("fight!");
        if(counter++ == 1)
            end();
        return true;
    }


}
