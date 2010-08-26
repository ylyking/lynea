using UnityEngine;
using System.Collections;



public class PopTest : MonoBehaviour {

    private bool showList = false;

    private int listEntry = 0;
    private GUIContent[] list;
    private GUIStyle listStyle;
    private bool picked = false;

	// Use this for initialization
    void Start()
    {
        // Make some content for the popup list
        list = new GUIContent[5];
        list[0] = new GUIContent("Foo");
        list[1] = new GUIContent("Bar");
        list[2] = new GUIContent("Thing1");
        list[3] = new GUIContent("Thing2");
        list[4] = new GUIContent("Thing3");

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
	void Update () {
	
	}

    void OnGUI()
    {
        if (Popup.List(new Rect(50, 100, 100, 20), ref showList, ref listEntry, new GUIContent("Click me!"), list, listStyle))
        {
            picked = true;
        }

        if (picked)
        {
            GUI.Label(new Rect(50, 70, 400, 20), "You picked " + list[listEntry].text + "!");
        }
    }
}
