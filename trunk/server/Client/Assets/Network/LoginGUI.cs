using UnityEngine;
using System;
using System.Collections;
using SmartFoxClientAPI;
using SmartFoxClientAPI.Data;
using SmartFoxClientAPI.Util;



public class LoginGUI : MonoBehaviour {
	private SmartFoxClient smartFox;
	//private bool shuttingDown = false;

	private string serverIP = "127.0.0.1";
	private string serverPort = "9339";
	public string zone = "lynea";
	public bool debug = true;

	public Texture2D mainTitle;

	private string username = "";
	private string loginErrorMessage = "";

	/************
     * Unity callback methods
     ************/

	void OnApplicationQuit() {
		//shuttingDown = true;
	}

	private bool connectionAttempt = false;

	void Awake() {
		Application.runInBackground = true;

		if ( SmartFox.IsInitialized() ) {
			smartFox = SmartFox.Connection;
		} else {
			try {
				smartFox = new SmartFoxClient(debug);
				smartFox.runInQueueMode = true;
			} catch ( Exception e ) {
				loginErrorMessage = e.ToString();
			}
		}

		// Register callback delegate
		SFSEvent.onConnection += OnConnection;
		SFSEvent.onConnectionLost += OnConnectionLost;
		SFSEvent.onLogin += OnLogin;
		SFSEvent.onRoomListUpdate += OnRoomList;
		SFSEvent.onDebugMessage += OnDebugMessage;

	}
	
	void FixedUpdate() {
		smartFox.ProcessEventQueue();
	}

	void OnGUI() {

		float titleScale = 0.2f;
		GUI.matrix=Matrix4x4.TRS(new Vector3(0, 0, 0), new Quaternion(0, 0, 0, 1), new Vector3(1, 1, 1) * titleScale);
		GUI.Label(new Rect(100, 100, mainTitle.width, mainTitle.height), mainTitle);	
		GUI.matrix=Matrix4x4.TRS(new Vector3(0, 0, 0), new Quaternion(0, 0, 0, 1), new Vector3(1, 1, 1));


		if (!connectionAttempt) {
			GUI.Label(new Rect(10, 116, 100, 100), "IP: ");
			serverIP = GUI.TextField(new Rect(100, 116, 200, 20), serverIP, 25);
			
			GUI.Label(new Rect(10, 136, 100, 100), "Port: ");
			serverPort = GUI.TextField(new Rect(100, 136, 200, 20), serverPort, 25);
			
			if (GUI.Button(new Rect(100, 166, 100, 24), "Connect")  || (Event.current.type == EventType.keyDown && Event.current.character == '\n')) {
				connectionAttempt = true;
				smartFox.Connect(serverIP, Convert.ToInt32(serverPort));
			}
		
		}
		else if (smartFox.IsConnected()) {
			// Login
			GUI.Label(new Rect(10, 116, 100, 100), "Username: ");
			username = GUI.TextField(new Rect(100, 116, 200, 20), username, 25);

			GUI.Label(new Rect(10, 218, 100, 100), loginErrorMessage);

			if ( GUI.Button(new Rect(100, 166, 100, 24), "Login")  || (Event.current.type == EventType.keyDown && Event.current.character == '\n')) {
				smartFox.Login(zone, username, "");
			}

		} else {
			GUI.Label(new Rect(10, 150, 100, 100), "Waiting for connection");
			GUI.Label(new Rect(10, 218, 100, 100), loginErrorMessage);
		}
	}

	/************
	 * Helper methods
	 ************/

	private void UnregisterSFSSceneCallbacks() {
		// This should be called when switching scenes, so callbacks from the backend do not trigger code in this scene
		SFSEvent.onConnection -= OnConnection;
		SFSEvent.onConnectionLost -= OnConnectionLost;
		SFSEvent.onLogin -= OnLogin;
		SFSEvent.onRoomListUpdate -= OnRoomList;
		SFSEvent.onDebugMessage -= OnDebugMessage;
	}

	/************
	 * Callbacks from the SFS API
	 ************/

	void OnConnection(bool success, string error) {
		if ( success ) {
			SmartFox.Connection = smartFox;
		} else {
			loginErrorMessage = error;
		}
	}

	void OnConnectionLost() {
		loginErrorMessage = "Connection lost / no connection to server";
	}

	public void OnDebugMessage(string message) {
		Debug.Log("[SFS DEBUG] " + message);
	}

	public void OnLogin(bool success, string name, string error) {
		if ( success ) {
			// Lets wait for the room list

		} else {
			// Login failed - lets display the error message sent to us
			loginErrorMessage = error;
		}
	}

	void OnRoomList(Hashtable roomList) {
		// When room list is updated we are ready to move on to the island
		UnregisterSFSSceneCallbacks();
		Application.LoadLevel("lynea");
	}
}
