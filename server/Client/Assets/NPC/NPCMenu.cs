using UnityEngine;
using System.Collections;

public class NPCMenu : MonoBehaviour {

    private int listEntry = 0;
    private GUIContent[] list;
    private GUIStyle listStyle;

    private Rect menuPosition;
    private bool showMenu = false;



    public void OnRightClick()
    {
        menuPosition = new Rect(Input.mousePosition.x, Screen.height - Input.mousePosition.y, 100, 20);
        showMenu = true;
    }



    // Use this for initialization
    void Start()
    {
        // Make some content for the popup list
        list = new GUIContent[3];
        list[0] = new GUIContent("Attack");
        list[1] = new GUIContent("Chain of Actions");
        list[2] = new GUIContent("Inventory");

        // Make a GUIStyle that has a solid white hover/onHover background to indicate highlighted items
        listStyle = new GUIStyle();
        listStyle.normal.textColor = Color.white;
        Texture2D texture = new Texture2D(1, 1);

        // Fill the texture
        texture.SetPixel(0, 0, Color.grey);

        // Apply all SetPixel calls
        texture.Apply();

        listStyle.hover.background = texture;
        listStyle.onHover.background = texture;
        listStyle.padding.left = listStyle.padding.right = 4;
        listStyle.padding.top = listStyle.padding.bottom = 2;

    }

    // Update is called once per frame
    void Update()
    {

    }

    void OnGUI()
    {
        if (ContextMenu.List(menuPosition, ref showMenu, ref listEntry, list, listStyle))
        {
            if (listEntry == 1)
            {
                GetComponent<NPCActionManager>().OpenActionEditor();
            }
        }      
    }

}
