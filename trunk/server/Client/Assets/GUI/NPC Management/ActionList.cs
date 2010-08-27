using System;
using System.Collections;

using UnityEngine;


public class ActionList : ArrayList, ICloneable
{
    int selected = -1;
    public override object Clone()
    {
        ActionList list = new ActionList();
        foreach(Action action in this)
        {
            list.Add((Action)action.Clone());
        }
        return list;
    }

    public void DisplayInHierarchy()
    {
        GUILayout.BeginVertical();
        
        string[] strArray = new string[this.Count];
        int i=0;
        foreach (Action a in this)
        {
            strArray[i] = a.ToString();
            i++;
        }

        int newSelected = GUILayout.SelectionGrid(selected, strArray, 1);
        SetCurrentAction((Action)this[newSelected]);

        /*
        foreach (Action action in this)
        {
            action.DisplayInHierarchy();
        }
        */
        
        GUILayout.EndVertical();
    }

    public Action GetCurrentAction()
    {
        if(selected >= 0)
        {
            return (Action)this[selected];
        }
        if (this.Count > 0)
        {
            selected = 0;
            ((Action)this[selected]).SetCurrent(true);
            return ((Action)this[selected]);
        }
        return null;
    }

    public void SetCurrentAction(Action action)
    {
        if (selected != IndexOf(action))
        {
            if(selected >= 0)
                ((Action)this[selected]).SetCurrent(false);
            selected = IndexOf(action);
            ((Action)this[selected]).SetCurrent(true);
        }
    }

    public override int Add(object value)
    {
        ((Action)value).parent = this;
        if (this.Count == 0)
        {
            ((Action)value).SetCurrent(true);
            selected = 0;
        }
        return base.Add(value);
    }
}

