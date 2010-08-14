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
	 * As a result, InitAnimationState MAY have been called before InitAnimation, which would result in Playing an animation other than the default "idle1"
	 */
	public void InitAnimation ()
	{
		anim = GetComponentInChildren(typeof(Animation)) as Animation;
		anim.wrapMode = WrapMode.Loop;
		anim.Play(lastState);
	}
	/*
	 * this function is called by PlayAnimation() if InitAnimation() has not yet been called (and thus anim has not yet been initialised)
	 *
	 */
	private void InitAnimationState(string initState)
	{
		lastState = initState;
		/*//if InitAnimation() has already been called
		if (anim != null)
		{
			//stop playing the default animation state played from InitAnimation()
			anim.Stop();
			anim.Play(lastState);
		}*/
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
			if (anim != null)
			{
				//temporary !! write a better way to set remote player speed !!
				if (message == "walk")
				{
					anim["walk"].speed = npcWalkSpeed;
				}	
				
				anim.CrossFade(message);	
			}
			else
				//store the animation message so that it will be able to be played when InitAnimation() will be called
				InitAnimationState(message);
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
