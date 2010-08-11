public class MoveAction extends ActionElement implements AttackListener
{
    public MoveAction()
    {
        attachListener(this);
    }
    public void onAttack(AttackInterrupt attackInterrupt)
    {
        //...
    }
}

public interface AttackListener extends InterruptListener
{
    public void onAttack(AttackInterrupt attackInterrupt);
}
//******************************************************************************************
public class Creature
{
    public void attack(Target t)
    {
    //...
        if (t.getClass() == NPC.class)
        {
            List<AttackListener> attackListeners = t.getListeners(AttackListener.class);
            if(attackListeners != null && !attackListeners.empty())
            {
                AttackInterrupt attackInterrupt = new AttackInterrupt(this);
                for(AttackListener listener : attackListeners)
                    listener.onAttack(attackInterrupt);
            }
        }
    }
}
//******************************************************************************************

public class NPC
{
    public <T extends InterruptListener> LinkedList<T> getListeners(Class<T> interruptListenerClass)
    {
       List<T> interruptListeners = new LinkedList<T>();
       rootActionGroup.getListeners(interruptListeners); //works recursively
        return interruptListeners;
    }
}

public class Action
{
    List<InterruptListener> interruptListeners;

    public Action()
    {
        interruptListeners = new ArrayList<InterruptListener>();
    }

    public <T extends InterruptListener> void getListeners(LinkedList<T> interruptListeners, Class<T> interruptListenerClass)
    {
        interruptListeners.add(getListener(interruptListenerClass));
        if(this.getClass() == ActionGroup.class)
            currentAction.getListeners(interruptListeners, interruptListenerClass);
    }

    public void attachListener(InterruptListener interruptListener)
    {
        interruptListeners.addLast(interruptListener);
    }

    public <T extends InterruptListener> T getListener(Class <T> interruptListenerClass)
    {
        for(InterruptListener listener : interruptListeners)
        {
            if(listener.getClass() == interruptListenerClass)
                return (T) listener;
        }
    }
}