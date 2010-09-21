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

    private Rect popupWindowRect;
    private bool popupWindowIsOpened = false;
    private string popupWindowParamName;
    private string popupWindowTitle;
    private GUI.WindowFunction popupWindowFunction;
    
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
                case "action mark":
                    DisplayActionMark(paramName);
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

    public void DisplayActionMark(string paramName)
    {
        Hashtable viewedParamInfo = (Hashtable) viewedParameters[paramName];
        Hashtable paramInfo = (Hashtable)parameters[paramName];

        GUILayout.BeginHorizontal();
        GUILayout.Label(" " + paramName + ": ");

        if (GUILayout.Button(new GUIContent((string)paramInfo["name"])))
        {
            viewedParamInfo["show window"] = true;  
        }
        if ((bool)viewedParamInfo["show window"])
        {
            ShowWindow(new Rect(100, 100, 300, 200), DisplayActionMarkPopupWindow, "Choose Way Point", paramName);
        }
        GUILayout.EndHorizontal();
        
    }
    public void DisplayActionMarkPopupWindow(int windowID)
    {
        Hashtable viewedParamInfo = (Hashtable)viewedParameters[popupWindowParamName];

        bool showOwnerList = (bool)viewedParamInfo["show owner types"];
        int ownerListEntry = (int)viewedParamInfo["owner types entry"];
        GUIContent[] ownerList = (GUIContent[])viewedParamInfo["owner types"];
        Vector2 ownerTypesScroll = (Vector2)viewedParamInfo["owner types scroll"];

        GUI.Label(new Rect(10, 20, 100, 20), "Way Point Owner:");
        DropDownList.List(new Rect(120, 20, 0, 0), ref showOwnerList, ref ownerListEntry, ref ownerTypesScroll, ownerList);
        viewedParamInfo["show owner types"] = showOwnerList;
        viewedParamInfo["owner types entry"] = ownerListEntry;
        viewedParamInfo["owner types scroll"] = ownerTypesScroll;

        bool showMarkList = false;
        int markEntry = -1;
        GUIContent[] markList = null;
        Vector2 markScroll = Vector2.zero;

        bool ownerSelected = false;

        switch (ownerListEntry)
        {
            case 1:
                showMarkList = (bool)viewedParamInfo["show player action marks"];
                markEntry = (int)viewedParamInfo["player action marks entry"];
                markList = (GUIContent[])viewedParamInfo["player action marks"];
                markScroll = (Vector2)viewedParamInfo["player action marks scroll"];
                break;
            case 2:
                showMarkList = (bool)viewedParamInfo["show faction action marks"];
                markEntry = (int)viewedParamInfo["faction action marks entry"];
                markList = (GUIContent[])viewedParamInfo["faction action marks"];
                markScroll = (Vector2)viewedParamInfo["faction action marks scroll"];
                break;
        }

        if (ownerListEntry > 0)
        {
            GUI.Label(new Rect(10, 100, 100, 20), "Way Point:");
            DropDownList.List(new Rect(120, 100, 0, 0), ref showMarkList, ref markEntry, ref markScroll, markList);
        }
    
        switch (ownerListEntry)
        {
            case 1:
                viewedParamInfo["show player action marks"] = showMarkList;
                viewedParamInfo["player action marks entry"] = markEntry;
                viewedParamInfo["player action marks scroll"] = markScroll;
                break;
            case 2:
                viewedParamInfo["show faction action marks"] = showMarkList;
                viewedParamInfo["faction action marks entry"] = markEntry;
                viewedParamInfo["faction action marks scroll"] = markScroll;
                break;
        }


        if (GUI.Button(new Rect(250, 170, 40, 20), "OK"))
        {
            //save the changes
            Hashtable paramInfo = (Hashtable)parameters[popupWindowParamName];
            if(ownerListEntry > 0 && markEntry > 0)
            {
                paramInfo["owner"] = (ownerListEntry==1)?ActionMark.OwnerType.PLAYER:ActionMark.OwnerType.FACTION;
                paramInfo["name"] = markList[markEntry].text;
            }

            //close the window
            viewedParamInfo["show window"] = false;
        }
        
        GUI.DragWindow();

    }
    public void AddActionMark(string paramName, ActionMark.OwnerType ownerType, string actionMarkName)
    {
        Hashtable param = new Hashtable();
        param.Add("type", "action mark");
        param.Add("owner", ownerType);
        param.Add("name", actionMarkName);
        parameters.Add(paramName, param);

        GUIContent [] ownerTypes = new GUIContent[3];
        ownerTypes[0] = new GUIContent("<WayPoint owner>");
        ownerTypes[1] = new GUIContent("me");
        ownerTypes[2] = new GUIContent("my faction");
    
        GameObject invGO = GameObject.Find("Inventory");
        Inventory inventory = invGO.GetComponent<Inventory>();
        ActionMark[] playerAM = inventory.GetPlayerOwnedActionMarks();
        ActionMark[] factionAM = inventory.GetFactionOwnedActionMarks();
        GUIContent[] playerActionMarks = new GUIContent[playerAM.Length + 1];
        playerActionMarks[0] = new GUIContent("<WayPoint>");
        int playerActionMarksEntry = 0;
        for (int i = 1; i <= playerAM.Length; i++)
        {
            playerActionMarks[i] = new GUIContent(playerAM[i - 1].ToString());
            if (actionMarkName.Equals(playerActionMarks[i]))
                playerActionMarksEntry = i;
        }
        GUIContent[] factionActionMarks = new GUIContent[factionAM.Length + 1];
        factionActionMarks[0] = new GUIContent("<WayPoint>");
        int factionActionMarksEntry = 0;
        for (int j = 1; j <= factionAM.Length; j++)
        {
            factionActionMarks[j] = new GUIContent(factionAM[j - 1].ToString());
            if (actionMarkName.Equals(factionActionMarks[j]))
                factionActionMarksEntry = j;
        }

        int ownerTypesEntry;
        switch (ownerType)
        {
            case ActionMark.OwnerType.PLAYER:
                ownerTypesEntry = 1;
                break;
            case ActionMark.OwnerType.FACTION:
                ownerTypesEntry = 2;
                break;
            default:
                ownerTypesEntry = 0;
                break;
        }

        Hashtable viewedParamInfo = new Hashtable();
        viewedParamInfo.Add("show owner types", false);
        viewedParamInfo.Add("owner types", ownerTypes);
        viewedParamInfo.Add("owner types entry", ownerTypesEntry);
        viewedParamInfo.Add("owner types scroll", Vector2.zero);
        viewedParamInfo.Add("show player action marks", false);
        viewedParamInfo.Add("player action marks", playerActionMarks);
        viewedParamInfo.Add("player action marks entry", playerActionMarksEntry);
        viewedParamInfo.Add("player action marks scroll", Vector2.zero);
        viewedParamInfo.Add("show faction action marks", false);
        viewedParamInfo.Add("faction action marks", factionActionMarks);
        viewedParamInfo.Add("faction action marks entry", factionActionMarksEntry);
        viewedParamInfo.Add("faction action marks scroll", Vector2.zero);
        viewedParamInfo.Add("show window", false);
        viewedParameters.Add(paramName, viewedParamInfo);
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

    internal void ShowWindow(Rect rect, GUI.WindowFunction winFun, string title, string paramName)
    {
        if (!popupWindowIsOpened)
        {
            popupWindowIsOpened = true;
            popupWindowParamName = paramName;
            popupWindowTitle = title;
            popupWindowFunction = winFun;
            popupWindowRect = rect;
        }
        popupWindowRect = GUI.Window(6, popupWindowRect, popupWindowFunction, popupWindowTitle);
        GUI.BringWindowToFront(6);
    }
}
