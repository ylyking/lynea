using UnityEngine;
using System;


public class ContextMenu
{
    static int contextMenuHash = "ContextMenu".GetHashCode();


    public static bool List(Rect position, ref bool showList, ref int listEntry, GUIContent[] listContent,
                             GUIStyle listStyle)
    {
        return List(position, ref showList, ref listEntry, listContent, "box", listStyle);
    }

    public static bool List(Rect position, ref bool showList, ref int listEntry, GUIContent[] listContent,
                             GUIStyle boxStyle, GUIStyle listStyle)
    {
        bool done = false;
        if (showList)
        {

            Rect listRect = new Rect(position.x, position.y, position.width, listStyle.CalcHeight(listContent[0], 1.0f) * listContent.Length);
            int controlID = GUIUtility.GetControlID(contextMenuHash, FocusType.Passive);
            switch (Event.current.GetTypeForControl(controlID))
            {
                case EventType.MouseUp:
                    listEntry = getListEntry(Event.current.mousePosition, listRect, listContent.Length);
                    Event.current.Use();
                    done = true;
                    showList = false;
                    break;
            }

            GUI.Box(listRect, "", boxStyle);
            //Use a selection grid for display purpose only (not for selection functionality)
            GUI.SelectionGrid(listRect, 0, listContent, 1, listStyle);
        }

        return done;
    }

    private static int getListEntry(Vector2 mousePosition, Rect listRect, int listLength)
    {
        //choice canceled
        if (!listRect.Contains(mousePosition))
            return -1;

        //A choice has been made
        int controlHeight = (int)listRect.height / listLength;
        int Dy = (int)Math.Abs(mousePosition.y - listRect.y);
        float n = (float)Dy / (float)controlHeight;
        int entry = (int)n;
        return entry;
    }
}