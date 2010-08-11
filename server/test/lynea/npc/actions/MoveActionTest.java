/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lynea.npc.actions;

import org.junit.Ignore;
import lynea.player.Player;
import lynea.npc.NPC;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Olivier
 */
public class MoveActionTest {

    public MoveActionTest() {
    }

    static private Player npcOwner;
    static private NPC npc;
    static private ActionMark start;
    static private ActionMark finish;
    static private MoveAction move;

    @BeforeClass
    public static void setUpClass() throws Exception 
    {

    }

    @AfterClass
    public static void tearDownClass() throws Exception 
    {

    }

    @Before
    public void setUp() {
        //Create an NPC that will move from ActionMark 'start' to ActionMark 'finish'
        npc = new NPC();
        npcOwner = new Player(null);
        start = new ActionMark(-2.0,0.0,-1.0, npcOwner, true);
        finish = new ActionMark(3.0,0.0,4.0, npcOwner, true);
        move = new MoveAction("MoveAction", npc, start, finish);
        npc.addAction(move);
    }

    @After
    public void tearDown() {
        npc.removeAction(move);
        move = null;
        npcOwner = null;
        npc = null;
        start = null;
        finish = null;
    }

    /**
     * Test of update method, of class MoveAction.
     */
    @Test
    public void testUpdate()
    {
        System.out.println("*Test MoveAction.update");
        double deltaTime = 0.5;

        //cannot update if not started
        assertFalse(move.update(deltaTime));
        move.start();
        assertTrue(move.update(deltaTime));
        //check the move progress
        assertEquals(npc.getSpeed() * deltaTime / start.distance(finish), move.getProgress(), 0.0001 * move.getProgress());
        //cannot update if paused
        move.pause();
        assertFalse(move.update(deltaTime));
        move.resume();
        assertTrue(move.update(deltaTime));
        //check the move progress
        assertEquals(2 * npc.getSpeed() * deltaTime / start.distance(finish), move.getProgress(), 0.0001 * move.getProgress());
    }

    /**
     * Test of start method, of class MoveAction.
     */
    @Ignore
    @Test
    public void testStart() {

    }

    /**
     * Test of end method, of class MoveAction.
     */

    @Test
    public void testEnd()
    {
       System.out.println("*Test MoveAction.end");
        double deltaTime = 0.5;

        npc.startAction();
        assertFalse(move.isEnded());
        //make the move in 5 steps
        assertTrue(npc.updateAction(deltaTime));
        assertFalse(move.isEnded());
        assertTrue(npc.updateAction(deltaTime));
        assertFalse(move.isEnded());
        assertTrue(npc.updateAction(deltaTime));
        assertFalse(move.isEnded());
        assertTrue(npc.updateAction(deltaTime));
        assertFalse(move.isEnded());
        assertTrue(npc.updateAction(deltaTime));
        //the move is ended
        assertTrue(move.isEnded());
        assertFalse(npc.updateAction(deltaTime));
        

        //check the move progress
        assertEquals(1.0, move.getProgress(), 0.0001 * move.getProgress());
    }

    /**
     * Test of onAttack method, of class MoveAction.
     */

    @Test
    public void testOnAttack()
    {
        System.out.println("*Test MoveAction.onAttack");
        double deltaTime = 0.5;

        npc.startAction();      
        assertTrue(npc.updateAction(deltaTime));
        assertTrue(npc.updateAction(deltaTime));
        //check the move progress
        assertEquals(npc.getSpeed() * 2 * deltaTime / start.distance(finish), move.getProgress(), 0.0001 * move.getProgress());

        Player attacker = new Player(null);
        attacker.attack(npc);
        assertTrue(move.isPaused());
        //update the Attack action
        assertTrue(npc.updateAction(deltaTime));
        //check the move progress (it shouldn't have changed)
        assertEquals(npc.getSpeed() * 2 * deltaTime / start.distance(finish), move.getProgress(), 0.0001 * move.getProgress());

        //***temporary behaviour***
        //update the Attack action
        assertTrue(npc.updateAction(deltaTime));
        //the Attack action should be ended now (temporary behaviour for test purpose)
        assertFalse(move.isPaused());
        //the begin position,current position and progress have been reseted
        start =  new ActionMark(npc.getX(), npc.getY(), npc.getZ(), npc.getOwner(), false);
        //this updates the move again
        assertTrue(npc.updateAction(deltaTime));
        assertEquals(npc.getSpeed() * deltaTime / start.distance(finish), move.getProgress(), 0.0001 * move.getProgress());
    }

}