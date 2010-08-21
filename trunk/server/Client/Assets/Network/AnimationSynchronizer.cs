using UnityEngine;
using System.Collections;
using System;

using SmartFoxClientAPI;
using SmartFoxClientAPI.Data;

public class AnimationSynchronizer : MonoBehaviour {
	
	private bool sendMode = false;
	private bool receiveMode = false;
	private string lastState = "idle1";
	private Animation anim;	
	private float npcWalkSpeed = 1.8f;
	
	public void Awake ()
	{
	}

	/*
	 * this function is called by CharacterGenerator after having loaded the character, which can be done asynchroneouosly (Unity Pro).
	 * As a result, ChangeAnimationState MAY have been called before InitAnimation, which would result in Playing an animation other than the default "idle1"
	 */
	public void InitAnimation ()
	{
		anim = GetComponentInChildren(typeof(Animation)) as Animation;
		anim.wrapMode = WrapMode.Loop;
		anim.Play(lastState);
		Debug.Log("InitAnimation > "+lastState);
	}
	/*
	 * this function is called by PlayAnimation() 
	 *
	 */
	private void ChangeAnimationState(string newState, float walkSpeed)
	{
		//if InitAnimation() has already been called (and thus anim has already been initialised)
		if (anim != null)
		{
			if (newState == "walk")
			{
				anim["walk"].speed = (walkSpeed >0) ? walkSpeed : npcWalkSpeed;
			}
			if(newState != lastState)
				anim.CrossFade(newState);	
		}
		lastState = newState;
	}

	// We call it on local player to start sending animation messages
	void StartSending() {
		sendMode = true;
		receiveMode = false;		
	}
	
	// We call it on remote player model to start receiving animation messages
	void StartReceiving() {
		sendMode = false;
		receiveMode = true;
	}

	
	void PlayAnimation(string message) 
	{
		if (sendMode) {
			//if the new state differs, send animation message to other clients
			if (lastState!=message) {
				SendAnimationMessage(message);
				//if(message == "idle1")
				//{
				//	HeadingSender ts = GetComponent<HeadingSender>();
				//	ts.StopSending();
				//}
				//else if(lastState == "idle1")
				//{
				//	HeadingSender ts = GetComponent<HeadingSender>();
				//	ts.StartSending();
				//}
				lastState = message;
			}			
		}
		else if (receiveMode) 
		{
			ChangeAnimationState(message, -1);
		}
	}

	void PlayAnimationFromSpeed(float speed)
	{
		//Debug.Log("PlayAnimationFromSpeed > "+newState+" (speed="+walkSpeed+")");
		if (speed > 0.0f)
		{
			ChangeAnimationState("walk", speed);
		}	
		else if (speed == 0.0f)
		{
			ChangeAnimationState("idle1", -1);
		}		
	}
	
	
	public void SendAnimationMessage(string message) {
		SmartFoxClient client = NetworkController.GetClient();
		string extensionName = NetworkController.GetExtensionName();
		Hashtable data = new Hashtable();
		data.Add("mes", message);
		//Send animation message
		client.SendXtMessage(extensionName, "a", data);
	}
	
	/*public float GetSpeed()
	{
		ThirdPersonController playerController = getComponent<ThirdPersonController>()
		if( playerController != null)
			return playerController.GetSpeed();
		
		switch (lastState)
		{
			case "walk":
				return npcWalkSpeed;
			case "idle1":
				return 0;
		}
		return 0;
	}*/
			
}
