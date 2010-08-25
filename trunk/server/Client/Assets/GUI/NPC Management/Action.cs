using System;
using System.Collections;

using UnityEngine;

public class Action : ICloneable
{
    public string name;
    public bool isCurrent = false;
    public Hashtable parameters;
    public ActionList parent;
    public Action()
    {
        parameters = new Hashtable();
    }
    public void DisplayInspectorContent()
    {
        GUILayout.BeginVertical();    
        foreach (DictionaryEntry param in parameters)
        {
            string paramName = (string) param.Key;
            Hashtable paramInfo = (Hashtable) param.Value;
            string paramType = (string) paramInfo["type"];
            switch (paramType)
            {
                case "boolean":
                    DisplayBooleanChoice(paramName, (bool) paramInfo["value"]);
                    break;
            }
        }
        GUILayout.EndVertical();
    }

    private void DisplayBooleanChoice(string paramName, bool value)
    {
        bool newValue = GUILayout.Toggle(value, " "+paramName);
        Hashtable paramInfo = (Hashtable) parameters[paramName];
        paramInfo["value"] = newValue;
    }

    public void AddBoolParam(string paramName, bool paramValue)
    {
        Hashtable param = new Hashtable();
        param.Add("type", "boolean");
        param.Add("value", paramValue);
        parameters.Add(paramName, param);
    }

    public void AddActionMark(string paramName, bool playerIsMarkOwner, int actionMarkID)
    {
        Hashtable param = new Hashtable();
        param.Add("type", "ActionMark");
        param.Add("owner", playerIsMarkOwner ? "player" : "faction");
        param.Add("ID", actionMarkID);
        parameters.Add(paramName, param);
    }

    public object Clone()
    {
        Action action = new Action();
        action.name = this.name;
        action.parameters = new Hashtable();
        foreach (DictionaryEntry param in this.parameters)
        {
            string paramName = (string) param.Key;
            string newParamName = (string) paramName.Clone();
            Hashtable paramInfo = (Hashtable)param.Value;
            Hashtable newParamInfo = (Hashtable) paramInfo.Clone();
            action.parameters.Add(newParamName, newParamInfo);
        }
        return action;
    }

    public void DisplayInHierarchy()
    {
        string star = isCurrent?" *":"";
        bool isPressed = GUILayout.Button(name+star);
        if (isPressed)
        {
            parent.SetCurrentAction(this);
        }
    }
}
