package lynea.player;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



import it.gotoandplay.smartfoxserver.data.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import lynea.AssetOwner;
import lynea.PhysicalEntity;
import lynea.npc.NPC;
import lynea.npc.interrupts.AttackInterrupt;
import lynea.npc.interrupts.AttackListener;

/**
 *
 * @author Olivier
 */
public class Player extends PhysicalEntity implements AssetOwner
{
    static public HashMap<Integer, Player> connected = new HashMap<Integer, Player>();
    static public HashMap<String, Player> all = new HashMap<String, Player>();

    private List<NPC> workers;
    private User user;

    public static float walkSpeed = 2.0f;
    private boolean canReceiveHeadings = false;


    
    /**
     * retreive all Players from database and create the corresponding Players objects
     */
    static public void init()
    {
        all = new HashMap<String, Player>();
        all.put("oli", new Player(null));
    }

    public static void hasConnected(User u)
    {
        String name = u.getName();
        Player connectedPlayer = all.get(name);
        if(connectedPlayer == null)
        {
            //temporary : add non existing player to the "all" Player HashMap
            connectedPlayer = new Player(null);
            all.put(name, connectedPlayer);
            System.out.println("Unknown player connected. Successfully added to player list.");
        }
        connectedPlayer.setUser(u);
        connected.put(u.getUserId(),connectedPlayer);
    }


    static public LinkedList getAllUserChannels()
    {
        LinkedList ll = new LinkedList();
        for (Player player : connected.values())
        {
            User user=player.getUser();
                if (user != null)
                    ll.add(user.getChannel());
        }
        return ll;
    }

    static public LinkedList getAllUserChannelsButOne(User u)
    {
        LinkedList ll = new LinkedList();
        for (Player player : connected.values())
        {
            User user=player.getUser();
            if (user != null && user.getUserId() != u.getUserId())
                ll.add(user.getChannel());
        }
        return ll;
    }



    public Player(User user)
    {
        this.user = user;
        this.animation = "idle1";
        this.workers = new ArrayList<NPC>();
    }

    public void addNPC(NPC npc)
    {
        workers.add(npc);
    }

    public User getUser()
    {
        return this.user;
    }
    public void setUser(User u)
    {
        this.user = u;
    }

    public void attack(PhysicalEntity target)
    {
        if (target.getClass() == NPC.class)
        {    
            NPC npc = (NPC) target;
            List<AttackListener> attackListeners = npc.getListeners(AttackListener.class);
            if(attackListeners != null)
            {
                AttackInterrupt attackInterrupt = new AttackInterrupt(this);
                for(AttackListener listener : attackListeners)
                    listener.onAttack(attackInterrupt);
            }
        }
        else if (target.getClass() == Player.class)
        {
            System.out.println("No pvp atm");
        }
        else
        {
            System.out.println("Player cannot attack this target atm");
        }
    }
    
    @Override
    synchronized public void setAnimation(String animation)
    {
        super.setAnimation(animation);
        if (animation.equals("idle1"))
            setSpeed(0);
        else if(animation.equals("walk"))
            setSpeed(Player.walkSpeed);
    }

    public void setCanReceive(boolean b) {
        canReceiveHeadings = b;
    }
    public boolean canReceive()
    {
        return canReceiveHeadings;
    }


}