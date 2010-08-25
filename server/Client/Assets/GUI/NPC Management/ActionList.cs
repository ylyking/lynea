using System;
using System.Collections;

using UnityEngine;


public class ActionList : ArrayList, ICloneable
{
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
        foreach (Action action in this)
        {
            action.DisplayInHierarchy();
        }
        GUILayout.EndVertical();
    }

    public Action GetCurrentAction()
    {
        foreach (Action action in this)
        {
            if (action.isCurrent)
                return action;
        }
        if (this.Count > 0)
        {
            ((Action)this[0]).isCurrent = true;
            return ((Action)this[0]);
        }
        return null;
    }

    public void SetCurrentAction(Action action)
    {
        foreach (Action a in this)
        {
            if (a == action)
                a.isCurrent = true;
            else
                a.isCurrent = false;
        }
    }

    public override int Add(object value)
    {
        ((Action)value).parent = this;
        return base.Add(value);
    }
}

