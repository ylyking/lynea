using UnityEngine;
using System.Collections;

using System;

public class HeadingSender : MonoBehaviour {

	public float sendingPeriod = 0.1f;
	private float timeSinceLastSending = 0.0f;
	
	private bool sendMode = false;
	private Heading lastState; // Last received and last sent heading state
	
	// Use this for initialization
	void Start () {
		lastState = new Heading();
	}
	
	// We call it on local player to start sending his transform
	public void StartSending() {
		sendMode = true;
	}
	
	public void StopSending(){
		sendMode = false;
	}
	
	void Update() 
	{ 
		if (sendMode) 
		{
			//Try to send heading
			timeSinceLastSending += Time.deltaTime;
			if (timeSinceLastSending >= sendingPeriod) 
			{
				SendHeading();
				timeSinceLastSending = 0;
			}
		}
	}
	
	
	void SendHeading() 
	{
		Heading current = new Heading();
		
		Vector3 currPos = transform.position;
		float currAngle = Convert.ToSingle(2*Math.Atan2(transform.rotation.y, transform.rotation.w));
		ThirdPersonController playerController = GetComponent<ThirdPersonController>();
		float currSpeed = playerController.GetSpeed();
		long currTime = ServerClock.Instance.GetTime();
		
		if (playerController.IsAccelerating())
		{
			float currEndSpeed = playerController.GetEndSpeed();
			long currAccelerationTime = playerController.GetAccelerationTime();
			current.InitFromValues(currPos, currAngle, currTime, currSpeed, currEndSpeed, currAccelerationTime);
		}
		else
		{
			current.InitFromValues(currPos, currAngle, currTime, currSpeed);
		}
		
	
		bool headingChanged = !current.IsFutureOf(lastState);
		
		if(headingChanged)
		{
			Debug.Log("last: "+lastState);
			Debug.Log("curr: "+current);
			lastState = current;
			lastState.Send();
		}
	}
	
	
}
