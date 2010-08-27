using System;
using System.Collections;

using UnityEngine;

public class Action : ICloneable
{
    public string name;
    public bool isCurrent = false;
    public ActionList parent;

    private SortedList parameters;
    private Hashtable viewedParameters;
    
    public Action()
    {
        parameters = new SortedList();
        viewedParameters = new Hashtable();
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
                    DisplayBoolean(paramName);
                    break;
                case "minored integer":
                    DisplayMinoredInteger(paramName);
                    break;
            }
        }
        GUILayout.EndVertical();
    }

    internal void DisplayBoolean(string paramName)
    {
        Hashtable paramInfo = (Hashtable)parameters[paramName];
        string toggleName = this.name + "." + paramName + ".Toggle";
        GUI.SetNextControlName(toggleName);
        bool newValue = GUILayout.Toggle((bool)paramInfo["value"], " " + paramName);   
        paramInfo["value"] = newValue;
    }

    public void AddBoolParam(string paramName, bool paramValue)
    {
        Hashtable param = new Hashtable();
        param.Add("type", "boolean");
        param.Add("value", paramValue);
        parameters.Add(paramName, param);
    }

    internal void DisplayMinoredInteger(string paramName)
    {
        Hashtable viewedParamInfo = (Hashtable) viewedParameters[paramName];
        Hashtable paramInfo = (Hashtable) parameters[paramName];
       
        GUILayout.BeginHorizontal();
        GUILayout.Label(" "+paramName+": ");
        string textFieldName = this.name + "." + paramName + ".TextField";
        GUI.SetNextControlName(textFieldName);
        string newViewedValue = GUILayout.TextField((string)viewedParamInfo["value"], 5);
        if(newViewedValue != (string) viewedParamInfo["value"])
        {
            viewedParamInfo["value"] = newViewedValue;
            viewedParamInfo["change"] = true;
            //DOES NOT WORK (on action change: erroneous focus):
            //Debug.Log(ServerClock.Instance.GetTime() + "Try 2 Take Focus! " + textFieldName);
            //GUI.FocusControl(textFieldName);
        }
        GUILayout.EndHorizontal();

        bool couldConvert = true;
        string newParamValue = "";
        int intValue = 0;
        try
        {
            intValue = Convert.ToInt32((string)viewedParamInfo["value"]);
        }
        catch (FormatException)
        {
            if (string.Equals((string)viewedParamInfo["value"], "INF", StringComparison.OrdinalIgnoreCase))
                newParamValue = "INF";
            else
                newParamValue = (string)paramInfo["value"];
            couldConvert = false;
        }
        catch (OverflowException)
        {
            newParamValue = "INF";
            couldConvert = false;
        }
        if (couldConvert)
        {
            if (intValue < (int)paramInfo["minimum"])
                paramInfo["value"] = Convert.ToString((int)paramInfo["minimum"]);
            else
                paramInfo["value"] = (string)viewedParamInfo["value"];
        }
        else
            paramInfo["value"] = newParamValue;

        //if (Event.current.type == EventType.keyDown && Event.current.character == '\n' && GUI.GetNameOfFocusedControl().Equals(textFieldName))
        if (Event.current.type == EventType.keyDown && Event.current.character == '\n')
        {
            ApplyChange(paramName);
        }
    }


    public void AddMinoredIntegerParam(string paramName, string paramValue, int minValue)
    {
        Hashtable paramInfo = new Hashtable();
        paramInfo.Add("type", "minored integer");
        paramInfo.Add("value", paramValue);
        paramInfo.Add("minimum", minValue);
        parameters.Add(paramName, paramInfo);
        Hashtable viewedParamInfo = new Hashtable();
        viewedParamInfo.Add("value", paramValue);
        viewedParamInfo.Add("change", false);
        viewedParameters.Add(paramName, viewedParamInfo);
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
        foreach (DictionaryEntry param in this.parameters)
        {
            string paramName = (string) param.Key;
            string newParamName = (string) paramName.Clone();
            Hashtable paramInfo = (Hashtable)param.Value;
            Hashtable newParamInfo = (Hashtable) paramInfo.Clone();
            action.parameters.Add(newParamName, newParamInfo);
        }
        foreach (DictionaryEntry param in this.viewedParameters)
        {
            string paramName = (string)param.Key;
            string newParamName = (string)paramName.Clone();
            Hashtable paramInfo = (Hashtable)param.Value;
            Hashtable newParamInfo = (Hashtable)paramInfo.Clone();
            action.viewedParameters.Add(newParamName, newParamInfo);
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

    public override string ToString()
    {
        return this.name;
    }

    public void SetCurrent(bool isCurrent)
    {
        bool wasCurrent = this.isCurrent;
        this.isCurrent = isCurrent;
        if (!isCurrent && wasCurrent)
            ApplyAllChanges();
    }

    internal void ApplyAllChanges()
    {
        foreach (DictionaryEntry param in this.viewedParameters)
        {
            string paramName = (string)param.Key;
            ApplyChange(paramName);   
        }
    }

    internal void ApplyChange(string paramName)
    {
        Hashtable viewedParamInfo = (Hashtable) (this.viewedParameters[paramName]);
        if (viewedParamInfo.ContainsKey("change") && (bool)viewedParamInfo["change"] == true)
        {
            Hashtable paramInfo = (Hashtable) (this.parameters[paramName]);
            viewedParamInfo["value"] = paramInfo["value"];
            viewedParamInfo["change"] = false;
        }
        GUIUtility.keyboardControl = 0;
    }
}
