using UnityEngine;
using System.Collections;
using System;

using SmartFoxClientAPI;
using SmartFoxClientAPI.Data;

public class NPCActionManager : MonoBehaviour
{
    private bool actionEditorOpened = false;
    private Rect rctWindow;
    private ActionList serverActionHierarchy;
    private ActionList clientActionHierarchy;
    private ArrayList serverActionLibrary;
    private ArrayList clientActionLibrary;

    private bool hasReceivedActionList = false;

    // Use this for initialization
    void Start()
    {
        rctWindow = new Rect(20, 20, 400, 400);
        test();
    }

    private void test()
    {
        //test
        SFSObject monparam = new SFSObject();
        monparam.Put("n", "monparam");
        monparam.Put("t", "b");
        monparam.PutBool("v", false);
        SFSObject monparam2 = new SFSObject();
        monparam2.Put("n", "monparam2");
        monparam2.Put("t", "b");
        monparam2.PutBool("v", true);
        SFSObject monparam3 = new SFSObject();
        monparam3.Put("n", "monparam3");
        monparam3.Put("t", "mi");
        monparam3.Put("v", "5");
        monparam3.PutNumber("m", 1);
        SFSObject monaction = new SFSObject();
        monaction.Put("n", "monaction");
        monaction.Put("0", monparam);
        monaction.Put("1", monparam2);
        monaction.Put("2", monparam3);

        SFSObject monparam4 = new SFSObject();
        monparam4.Put("n", "monparam4");
        monparam4.Put("t", "b");
        monparam4.PutBool("v", false);
        SFSObject monparam5 = new SFSObject();
        monparam5.Put("n", "monparam5");
        monparam5.Put("t", "mi");
        monparam5.Put("v", "16");
        monparam5.PutNumber("m", -6);
        SFSObject monaction2 = new SFSObject();
        monaction2.Put("n", "monaction2");
        monaction2.Put("0", monparam4);
        monaction2.Put("1", monparam5);

        SFSObject monparam6 = new SFSObject();
        monparam6.Put("n", "monparam6");
        monparam6.Put("t", "b");
        monparam6.PutBool("v", true);
        SFSObject monparam7 = new SFSObject();
        monparam7.Put("n", "monparam7");
        monparam7.Put("t", "mi");
        monparam7.Put("v", "55");
        monparam7.PutNumber("m", -7);
        SFSObject monparam8 = new SFSObject();
        monparam8.Put("n", "monparam8");
        monparam8.Put("t", "b");
        monparam8.PutBool("v", false);
        SFSObject monparam9 = new SFSObject();
        monparam9.Put("n", "monparam9");
        monparam9.Put("t", "mi");
        monparam9.Put("v", "300");
        monparam9.PutNumber("m", 0);
        SFSObject monaction3 = new SFSObject();
        monaction3.Put("n", "monaction3");
        monaction3.Put("0", monparam6);
        monaction3.Put("1", monparam7);
        monaction3.Put("2", monparam8);
        monaction3.Put("3", monparam9);

        SFSObject monactionhierarchy = new SFSObject();
        monactionhierarchy.Put("0", monaction);
        monactionhierarchy.Put("2", monaction2);
        monactionhierarchy.Put("1", monaction3);

        SFSObject monactionlibrary = new SFSObject();
        SFSObject maliste = new SFSObject();
        maliste.Put("h", monactionhierarchy);
        maliste.Put("l", monactionlibrary);

        ReceiveActionList(maliste);
    }

    // Update is called once per frame
    void Update()
    {

    }

    public void OpenActionEditor()
    {
        actionEditorOpened = true;
    }


    public void ReceiveActionList(SFSObject list)
    {
        SFSObject hierarchy = list.GetObj("h");
        SetServerActionHierarchy(hierarchy);
        clientActionHierarchy = (ActionList)serverActionHierarchy.Clone();

        SFSObject library = list.GetObj("l");
        SetServerActionLibrary(library);

        hasReceivedActionList = true;
    }

    private void SetServerActionHierarchy(SFSObject hierarchy)
    {
        serverActionHierarchy = new ActionList();

        for (int a = 0; a < hierarchy.Size(); a++)
        {
            SFSObject actionObject = hierarchy.GetObj(Convert.ToString(a));
            Action action = new Action();
            action.name = actionObject.GetString("n");
            int numParam = actionObject.Size();
            for (int i = 0; i < numParam - 1; i++)
            {             
                SFSObject param = actionObject.GetObj(Convert.ToString(i));
                string paramName = param.GetString("n");
                string paramType = param.GetString("t");
                switch (paramType)
                {
                    case "b" ://boolean
                        bool boolValue = param.GetBool("v");
                        action.AddBoolParam(paramName, boolValue);
                        break;
                    case "maq"://maximum quantity (integer > 0 or "MAX")
                        //
                        break;
                    case "miq"://minimum quantity (integer > 0 or "MIN")
                        //
                        break;
                    case "n"://integer with min and max values
                        //
                        break;
                    case "mi"://minored integer : integer with min value or "INF"
                        string value = param.GetString("v");
                        int minValue = (int) param.GetNumber("m");
                        action.AddMinoredIntegerParam(paramName, value, minValue);
                        break;
                    case "ii"://inventory item
                        //
                        break;
                    case "it"://item type
                        //
                        break;
                    case "am"://actionMark (an absolute position or the position of a physical entity marked by the player or his faction)
                        bool playerIsMarkOwner = param.GetBool("o");
                        int actionMarkID = (int)param.GetNumber("n");
                        action.AddActionMark(paramName, playerIsMarkOwner, actionMarkID);
                        break;
                    case "c"://character (player or npc)
                        //
                        break;
                    case "s"://string message
                        //
                        break;

                }
            }

            serverActionHierarchy.Add(action);
        }
    }

    private void SetServerActionLibrary(SFSObject library)
    {

    }

    void OnGUI()
    {
        if(actionEditorOpened)
            rctWindow = GUI.Window(0, rctWindow, ActionEditor, "NPC Action Editor");
    }

    private Vector2 hierarchyScroll = new Vector2(0, 0);
    private Vector2 libraryScroll = new Vector2(0, 0);
    private Vector2 inspectorScroll = new Vector2(0, 0);
    void ActionEditor(int windowID)
    {
        //GUI params:
        int totalWidth = (int)rctWindow.width;
        int totalHeight = (int)rctWindow.height;
        int windowTitleHeight = 15;
        int boxTitleHeight = 20;
        int internalSpace = 5;
        int externalSpace = 7;
        int hierarchyWidth = 150;
        int hierarchyHeight = 200;
        int saveWidth = 50;
        int saveHeight = 20;

        //GUI computations:
        int hierarchyX = externalSpace;
        int hierarchyY = externalSpace + windowTitleHeight;
        int libraryX = externalSpace;
        int libraryY = hierarchyY + hierarchyHeight + internalSpace;
        int libraryWidth = hierarchyWidth;
        int libraryHeight = totalHeight - windowTitleHeight - 2 * externalSpace - hierarchyHeight - internalSpace;
        int inspectorX = externalSpace + hierarchyWidth + internalSpace;
        int inspectorY = externalSpace + windowTitleHeight;
        int inspectorWidth = totalWidth - 2 * externalSpace - internalSpace - hierarchyWidth;
        int inspectorHeight = totalHeight - windowTitleHeight - 2 * externalSpace - internalSpace - saveHeight;
        int saveX = totalWidth - saveWidth - externalSpace;
        int saveY = totalHeight - externalSpace - saveHeight;
        int cancelWidth = saveWidth;
        int cancelHeight = saveHeight;
        int cancelX = saveX - cancelWidth - internalSpace;
        int cancelY = saveY;

        //Action Hierarchy
        Rect hierarchyRect = new Rect(hierarchyX, hierarchyY, hierarchyWidth, hierarchyHeight);
        GUI.Box(hierarchyRect, "Action Hierarchy");
        GUILayout.BeginArea(new Rect(hierarchyRect.x, hierarchyRect.y + boxTitleHeight, hierarchyRect.width, hierarchyRect.height - boxTitleHeight));
        hierarchyScroll = GUILayout.BeginScrollView(hierarchyScroll, GUILayout.Width(hierarchyRect.width), GUILayout.Height(hierarchyRect.height));
        if (hasReceivedActionList)
        {
            clientActionHierarchy.DisplayInHierarchy();
        }
        GUILayout.EndScrollView();
        GUILayout.EndArea();

        //Action Library
        Rect libraryRect = new Rect(libraryX, libraryY, libraryWidth, libraryHeight);
        GUI.Box(libraryRect, "Action Library");
        GUILayout.BeginArea(new Rect(libraryRect.x, libraryRect.y + boxTitleHeight, libraryRect.width, libraryRect.height - boxTitleHeight));
        inspectorScroll = GUILayout.BeginScrollView(inspectorScroll, GUILayout.Width(libraryRect.width), GUILayout.Height(libraryRect.height)); 
        GUILayout.BeginVertical();
        //temp
        //GUILayout.Label("focus: " + GUI.GetNameOfFocusedControl());
        GUILayout.EndVertical();
        GUILayout.EndScrollView();
        GUILayout.EndArea();

        //Action Inspector
        Rect inspectorRect = new Rect(inspectorX, inspectorY, inspectorWidth, inspectorHeight);
        GUI.Box(inspectorRect, "Parameters of " + clientActionHierarchy.GetCurrentAction().name);
        GUILayout.BeginArea(new Rect(inspectorRect.x, inspectorRect.y + boxTitleHeight, inspectorRect.width, inspectorRect.height - boxTitleHeight));
        inspectorScroll = GUILayout.BeginScrollView(inspectorScroll, GUILayout.Width(inspectorRect.width), GUILayout.Height(inspectorRect.height)); 
        if (hasReceivedActionList)
        {
            clientActionHierarchy.GetCurrentAction().DisplayInspectorContent();
        }
        GUILayout.EndScrollView();
        GUI.Button(new Rect(inspectorRect.width - 140 - internalSpace, inspectorRect.height - boxTitleHeight - saveHeight - internalSpace, 140, saveHeight), "Save Action in Library");
        GUILayout.EndArea();

        //Save or Cancel
        Rect saveRect = new Rect(saveX, saveY, saveWidth, saveHeight);
        if (GUI.Button(saveRect, "Save"))
        {
            GameObject networkController = GameObject.Find("NetworkController");
            networkController.SendMessage("SendActionList", clientActionHierarchy);
            actionEditorOpened = false;
        }
        Rect cancelRect = new Rect(cancelX, cancelY, cancelWidth, cancelHeight);
        if (GUI.Button(cancelRect, "Cancel"))
        {
            actionEditorOpened = false;
        }


        GUI.DragWindow();
    }



}