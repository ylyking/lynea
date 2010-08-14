using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using System;

using SmartFoxClientAPI;
using SmartFoxClientAPI.Data;

public class ChatController : MonoBehaviour {
	
	public GUISkin skin;
	
	private Vector2 scrollPosition;
	
	private List<String> messages = new List<String>();
	
	private Rect chatWindow;
	private string userMessage = "";
	
	private bool typingMessage = false;
	private bool sendingMessage = false;
		
		
	void Start() {
		chatWindow = new Rect(Screen.width-290, Screen.height - 250, 280, 240);
	}


	void OnGUI() {
		GUI.skin = skin;
		
		if (EnterPressed()) {
			if (!typingMessage) {
				 typingMessage = true;
			}
			else {
				// Send message
	   	    	if (userMessage.Length > 0) {
					AddMyChatMessage(userMessage);
					userMessage = "";
				}
				typingMessage = false;
			}
			Event.current.Use();	 
		}	
		
		chatWindow = GUI.Window (1, chatWindow, ShowChatWindow, "Chat");
	}
	
	private bool EnterPressed() {
		return (Event.current.type == EventType.keyDown && Event.current.character == '\n');
	}
	
	void ShowChatWindow(int id) {	
		GUI.SetNextControlName("scroll"); 
		scrollPosition = GUILayout.BeginScrollView (scrollPosition);
		foreach(string message in messages) {
			GUILayout.BeginHorizontal();
			GUILayout.Label(message);
			GUILayout.FlexibleSpace();
			GUILayout.EndHorizontal();
			GUILayout.Space(3);
		}
				
	    GUILayout.EndScrollView();
	   	if (!typingMessage) {
	   		 GUI.FocusControl("scroll");
	   	}
	   	
	   	GUI.SetNextControlName("text"); 
	   	userMessage = GUILayout.TextField(userMessage);
	   	if (typingMessage) {
	   		 GUI.FocusControl("text");
		}
		
		GUILayout.Label("Press Enter to type and again to send");
	}
	
	private void AddMyChatMessage(String message) {
		SmartFoxClient client = NetworkController.GetClient();
		String userName = client.myUserName;
		AddChatMessage("<"+userName+"> "+message);
		GameObject localPlayer = GameObject.Find("localPlayer");
		localPlayer.SendMessage("ShowBubble", "<"+userName+">\n"+message);
		SendChatMessage(message);
	}
	
	// This method to be called when remote chat message is received
	void AddChatMessage(String message) {
		messages.Add(message);
		scrollPosition.y = 10000000000; // To scroll down the messages window
	}
		
	// Send the chat message to all other users
	private void SendChatMessage(String message) {
		SmartFoxClient client = NetworkController.GetClient();
		client.SendPublicMessage(message);	
	}
}
