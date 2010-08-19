/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lynea.npc.actions;

import lynea.WorldUpdater;
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
        //assign an integer value to the walk speed so that the calculations are easier
        NPC.walkSpeed = 2.0f;
    }

    @AfterClass
    public static void tearDownClass() throws Exception 
    {
        NPC.walkSpeed = 1.8f;
    }

    @Before
    public void setUp() {
        //Create an NPC that will move from ActionMark 'start' to ActionMark 'finish'
        npc = new NPC();
        npcOwner = new Player(null);
        start = new ActionMark(-5.0f,0.0f,5.0f, npcOwner, true);
        finish = new ActionMark(5.0f,0.0f,5.0f, npcOwner, true);
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
        int deltaTime = 10;
        float deltaTimeInSec = (float)deltaTime / 1000.0f;

        //cannot update if not started
        WorldUpdater.getInstance().addSimulationTime(deltaTime);
        assertFalse(move.update(deltaTime));
        npc.startAction();
        WorldUpdater.getInstance().addSimulationTime(deltaTime);
        assertTrue(move.update(deltaTime));
        //check the move progress
        assertEquals(npc.getSpeed() * deltaTimeInSec / start.distance(finish), move.getProgress(), 0.0001 * move.getProgress());
        //cannot update if paused
        move.pause();
        WorldUpdater.getInstance().addSimulationTime(deltaTime);
        assertFalse(move.update(deltaTime));
        move.resume();
        WorldUpdater.getInstance().addSimulationTime(deltaTime);
        assertTrue(move.update(deltaTime));   
        //check the move progress
        assertEquals(2 * npc.getSpeed() * deltaTimeInSec / start.distance(finish), move.getProgress(), 0.0001 * move.getProgress());

        while(!npc.isAccelerating())
        {
            WorldUpdater.getInstance().addSimulationTime(deltaTime);
            assertTrue(npc.updateAction(deltaTime));
        }
        while(npc.isAccelerating())
        {
            WorldUpdater.getInstance().addSimulationTime(deltaTime);
            assertTrue(npc.updateAction(deltaTime));
        }
        WorldUpdater.getInstance().addSimulationTime(deltaTime);
        //the move is ended
        assertFalse(npc.updateAction(deltaTime));
        assertTrue(move.isEnded());
        


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


        npc.startAction();
        int deltaTime = Math.round(2000.0f/npc.getSpeed());
        
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
        int deltaTime = 500;
        float deltaTimeInSec = (float)deltaTime / 1000.0f;

        npc.startAction();      
        assertTrue(npc.updateAction(deltaTime));
        assertTrue(npc.updateAction(deltaTime));
        //check the move progress
        assertEquals(npc.getSpeed() * 2 * deltaTimeInSec / start.distance(finish), move.getProgress(), 0.0001 * move.getProgress());

        Player attacker = new Player(null);
        attacker.attack(npc);
        assertTrue(move.isPaused());
        //update the Attack action
        assertTrue(npc.updateAction(deltaTime));
        //check the move progress (it shouldn't have changed)
        assertEquals(npc.getSpeed() * 2 * deltaTimeInSec / start.distance(finish), move.getProgress(), 0.0001 * move.getProgress());

        //***temporary behaviour***
        //update the Attack action
        assertTrue(npc.updateAction(deltaTime));
        //the Attack action should be ended now (temporary behaviour for test purpose)
        assertFalse(move.isPaused());
        //the begin position,current position and progress have been reseted
        start =  new ActionMark(npc.getX(), npc.getY(), npc.getZ(), npc.getOwner(), false);
        //the progress should have been reseted
        assertEquals(0, move.getProgress(), 0);
        //this updates the move again
        assertTrue(npc.updateAction(deltaTime));

        long newTotalTime = Math.round(1000 * start.distance(finish) / npc.getSpeed());
        assertEquals(deltaTimeInSec / ((float)newTotalTime/1000.0f) , move.getProgress(), 0.0001 * move.getProgress());
    }

}