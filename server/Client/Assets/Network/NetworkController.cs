using UnityEngine;
using System.Collections;

using System;
using SmartFoxClientAPI;
using SmartFoxClientAPI.Data;

public class NetworkController : MonoBehaviour {
	
	private static SmartFoxClient smartFoxClient;
	
	public static SmartFoxClient GetClient() {
		return SmartFox.Connection;
	}
	public static String GetExtensionName()
	{
		return "lyneaServer";
	}
	
	#region Events
	
	private bool started = false;
	
	private void SubscribeEvents() {
		SFSEvent.onJoinRoom += OnJoinRoom;
		SFSEvent.onPublicMessage += OnPublicMessage;
		SFSEvent.onExtensionResponse += OnExtensionResponse;
		ServerClock.Instance.OnClockReady += new EventHandler(OnClockReady);
    }
	
	private void UnsubscribeEvents() {
		SFSEvent.onJoinRoom -= OnJoinRoom;
		SFSEvent.onPublicMessage -= OnPublicMessage;
		SFSEvent.onExtensionResponse -= OnExtensionResponse;
		ServerClock.Instance.OnClockReady -= new EventHandler(OnClockReady);
	}
	
	void FixedUpdate() {
		if (started) {
			smartFoxClient.ProcessEventQueue();
		}
	}
	
	#endregion Events
	
	// We start working from here
	void	Start() {
		Application.runInBackground = true; // Let the application be running while the window is not active.
		smartFoxClient = GetClient();
		if (smartFoxClient==null) {
			Application.LoadLevel("login");
			return;
		}	
		SubscribeEvents();
		started = true;
		smartFoxClient.JoinRoom("MainRoom");
	}
	
	// We should unsubscribe all delegates before quitting the application to avoid probleems.
	// Also we should Disconnect from server
	void OnApplicationQuit() {
		ServerClock.Instance.Stop();
		UnsubscribeEvents();
		smartFoxClient.Disconnect();
	}
	
	
	private void OnJoinRoom(Room room) {
		Debug.Log("Connected ! Initiating ServerClock");
		ServerClock.Instance.Init(2000, true);
	}
	
	private void OnClockReady(object sender, EventArgs e)
	{
		SendMessage("SpawnPlayer");
	}
	
	public void OnExtensionResponse(object dataObject, string type) 
	{
		// We only use XML based messages for now, so ignore string and json types
		if ( type == SmartFoxClient.XTMSG_TYPE_XML ) 
		{
			// For XML based communication the data object is a SFSObject
			SFSObject data = (SFSObject)dataObject;
			//First determine the type of this object - what it contains ?
			String _cmd = data.GetString("_cmd");
			//Debug.Log("<@ct="+Time.time+">"+"CMD received : ["+_cmd+"]");
			switch (_cmd) 
			{
				case "h":  // "t" - means transform sync data
					SendHeadingsToGameObjects(data);
					break;
				case "a": // "a" - for animation message received
					Debug.Log("anim received but anim reception is DISABLED");
					//SendAnimationMessagesToGameObjects(data);
					break;
				case "c"://"c" - for clock sync messages
					ServerClock.Instance.OnExtensionResponse(data);
					break;
			}
		}
	}


	
	private void SendHeadingsToGameObjects(SFSObject data) {
		//Debug.Log("Client gonna extract data object");
		SFSObject physicalEntities = data.GetObj("p");
		if(physicalEntities.Size() == 0) Debug.Log("erreur PE.SIZE = "+physicalEntities.Size() );
		for (int i = 0; i < physicalEntities.Size(); i++)
		{
			//Debug.Log("Client gonna extract a Physical Entity");
			SFSObject pdata = physicalEntities.GetObj(Convert.ToString(i));
			//if (pdata != null)
				//Debug.Log("Cient could extract a Physical Entity! gonna extract his uid");
			int userId = (int) pdata.GetNumber("uid");
			GameObject entity = null;
			
			if(userId >= 0)
			{
				//Debug.Log("Client extracted a player heading for player id="+userId);
				//Find player object with such Id
				entity = GameObject.Find("remotePlayer_"+userId);
				if(!entity)
				{
					//create the player as he entered our interest area
					SendMessage("UserEnterInterestArea", userId);
					entity = GameObject.Find("remotePlayer_"+userId);
				}
			}
			else
			{
				//Debug.Log("Client extracted an NPC heading for NPC name=" + pdata.GetString("n"));
				//Find npc object with such name
				entity = GameObject.Find("npc_"+pdata.GetString("n"));
				if(!entity)
				{
					//create the npc as he entered our interest area
					SendMessage("NPCEnterInterestArea", pdata.GetString("n"));
					entity = GameObject.Find("npc_"+pdata.GetString("n"));
				}
			}
			//send the entity his transform
			entity.SendMessage("ReceiveHeading", pdata);
		}
	}
	
	private void SendAnimationMessagesToGameObjects(SFSObject data) {
		SFSObject physicalEntities = data.GetObj("p");
		for (int i = 0; i < physicalEntities.Size(); i++)
		{
			//Debug.Log("Client gonna extract a Physical Entity");
			SFSObject pdata = physicalEntities.GetObj(Convert.ToString(i));
			//if (pdata != null)
				//Debug.Log("Cient could extract a Physical Entity! gonna extract his uid");
			
			int userId = (int) pdata.GetNumber("uid");
			GameObject entity = null;
			
			if(userId >= 0)
			{
				//Debug.Log("Client extracted a player with id="+userId);
				//Find player object with such Id
				entity = GameObject.Find("remotePlayer_"+userId);
				if(!entity)
				{
					//create the player as he entered our interest area
					SendMessage("UserEnterInterestArea", userId);
					entity = GameObject.Find("remotePlayer_"+userId);
				}
			}
			else
			{
				//Debug.Log("Client extracted an NPC named " + pdata.GetString("n"));
				//Find npc object with such name
				entity = GameObject.Find("npc_"+pdata.GetString("n"));
				if(!entity)
				{
					//create the npc as he entered our interest area
					SendMessage("NPCEnterInterestArea", pdata.GetString("n"));
					entity = GameObject.Find("npc_"+pdata.GetString("n"));
				}
			}
			//send the entity his animation message
			entity.SendMessage("PlayAnimation", pdata.GetString("mes"));
		}
	}
	
	public void OnPublicMessage(string message, User fromUser, int roomId)
	{
		int userId = fromUser.GetId();
    	if (userId!=smartFoxClient.myUserId) {  // If it's not myself
    		string mes = "<"+fromUser.GetName()+"> "+message;
    	
			// Send chat message to the Chat Controller			
			SendMessage("AddChatMessage", mes);
			
			//Find user object with such Id
			GameObject user = GameObject.Find("remote_"+userId);
			//If found - send him bubble message
			if (user) {
				user.SendMessage("ShowBubble", "<"+fromUser.GetName()+">\n"+message);
			}
		}
	}
		
}
