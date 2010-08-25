using UnityEngine;
using System.Collections;

using System;
using SmartFoxClientAPI;
using SmartFoxClientAPI.Data;

public class PlayerSpawnController : MonoBehaviour {

	public Transform localPlayerObject; //Note: we leave local player as object and do not instantiate it to keep existing Island Demo scripts working.
	public Transform remotePlayerPrefab;
    public Transform NPCPrefab;
	
    void SpawnPlayer() {
		SpawnLocalPlayer();  // Spawn local player object
	}
	
	private void SpawnLocalPlayer() {
		localPlayerObject.transform.position = new Vector3(0, 0, 0);
		localPlayerObject.transform.rotation = new Quaternion(0, 0, 0, 1);
		localPlayerObject.SendMessage("StartSending");  // Start sending our transform to other players
	}
	
	private void SpawnRemotePlayer(int userId) {
		// Just spawn remote player at a very remote point
		UnityEngine.Object remotePlayer = Instantiate(remotePlayerPrefab, new Vector3(-10000, -10000, -10000), new Quaternion(0,0,0,1));
		
		//Give remote player a name like "remote_<id>" to easily find him then
		remotePlayer.name = "remotePlayer_"+userId;
		
		//Start receiving heading synchronization messages
		(remotePlayer as Component).SendMessage("StartReceiving");
	}
	private void SpawnNPC(string name) {
		// Just spawn npc at a very remote point
        UnityEngine.Object npc = Instantiate(NPCPrefab, new Vector3(-10000, -10000, -10000), new Quaternion(0, 0, 0, 1));
		
		//Give npc a name like "npc_<name>" to easily find him then
		npc.name = "npc_"+name;

        //Start receiving heading synchronization messages
		(npc as Component).SendMessage("StartReceiving");
	}
	
	private void UserEnterInterestArea(int userId) {
		//When remote user enters our interest area we spawn his object.
		//Debug.Log("UserEnterInterestArea()");
		SpawnRemotePlayer(userId);
	}
	private void NPCEnterInterestArea(string name)
	{
		//When npc enters our interest area we spawn his object.
		//Debug.Log("NPCEnterInterestArea()");
		SpawnNPC(name);
	}
	
	private void UserLeaveInterestArea(int userId) {
		//Just destroy the corresponding object
		GameObject obj = GameObject.Find("remotePlayer_"+userId);
		if (obj!=null) Destroy(obj);
	}
	private void NPCLeaveInterestArea(string name) {
		//Just destroy the corresponding object
		GameObject obj = GameObject.Find("npc_"+name);
		if (obj!=null) Destroy(obj);
	}
	
}
