using UnityEngine;
using System.Collections;

public class BubblePopup : MonoBehaviour {

	public GUISkin skin;
	public float showTime = 4.0f;  // We display each bubble for 4 seconds.
	
	private string str = "";   // Striing to display
	private float bubbleTime =0.0f;    // Time counter   
	
	void OnGUI() {

		//if there is something to say
		if (str!="") {		
			GUI.skin = skin;
			GUIContent content = new GUIContent(str);
			Vector2 textSize = skin.GetStyle("Box").CalcSize(content);
			Vector3 bubblePos = transform.position + new Vector3(0.0f, 3.0f, 0.0f);  //We ajust world Y position here to make chat bubble near the head of the model
			Vector3 screenPos = Camera.main.WorldToScreenPoint(bubblePos);
					
			// We render our text only if it's in the screen view port	
			if (screenPos.x >= 0 && screenPos.x <= Screen.width && screenPos.y >= 0 && screenPos.y <= Screen.height && screenPos.z >= 0 ) {
				Vector2 pos = GUIUtility.ScreenToGUIPoint(new Vector2(screenPos.x, Screen.height - screenPos.y));
				GUI.Box(new Rect(pos.x, pos.y, textSize.x + 5, textSize.y + 20), content);  // Add our offset (5, 20) to size here
			}
		}
	}
	
	void Update() {
		// Here we count the time to display the message
		if (str!="") {
			bubbleTime += Time.deltaTime;
			if (bubbleTime > showTime) {
				bubbleTime = 0;
				str = "";
			}
		}
	}
	
	// Function to be called if we want to show new bubble
	void ShowBubble(string str) {
		this.str = str;
	}
}
