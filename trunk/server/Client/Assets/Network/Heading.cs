using UnityEngine;
using System.Collections;
using System;

using SmartFoxClientAPI;
using SmartFoxClientAPI.Data;

public class Heading {

	private Vector3 position;
	private Quaternion orientation;
	private float angle;
	private long time;
	private float speed;
	private bool isAccelerating = false;
	private long accelerationTime = -1;
	
	private float acceleration = 0;
	private float endSpeed = -1;
	
	//TODO: better way to handle speed and speed variation
	public float walkSpeed = 1.8f;//warning !!! change this !!!
	public float stopTime = 0.1f;
	
	// Use this for initialization
	void Start () {
		position = new Vector3();
		orientation = new Quaternion();
	}
	
	// Update is called once per frame
	void Update () {
	
	}
	
	public void InitFromValues(Vector3 position, float angle, long time, float speed)
	{
		SetPosition(position);
		SetAngle(angle);
		SetTime(time);
		SetSpeed(speed, -1, -1);
	}
	public void SetPosition(Vector3 pos)
	{
		this.position.x = pos.x;
		this.position.y = pos.y;
		this.position.z = pos.z;
	}
	public void SetPosition(Heading heading)
	{
		Vector3 pos = heading.GetPosition();
		SetPosition(pos);
	}
	public Vector3 GetPosition()
	{
		return this.position;
	}
	public Quaternion GetRotation()
	{
		return this.orientation;
	}
	public void SetTime(long time)
	{
		this.time = time;
	}
	public long GetTime()
	{
		return this.time;
	}
	public void SetAngle(float angle)
	{
		this.angle = angle;
		this.orientation = Quaternion.AngleAxis(Convert.ToSingle(angle/Math.PI*180), new Vector3(0.0f, 1.0f, 0.0f));
	}
	public float GetAngle()
	{
		return this.angle;
	}
	public void SetSpeed(float speed, float endSpeed, long accelerationTime)
	{
		//if speed is zero, enforce a true zero value (an epsilon>0 value would be interpreted as an accelerating heading and as a result would be amplified by the function)
		speed = (speed>0.01)? speed: 0;
		this.speed = speed;
		if(accelerationTime < 0)
		{
			//if (speed < walkSpeed && speed > 0) 
			//{
			//	this.isAccelerating = true;
			//	//assume endSpeed = 0 and use the default stopTime
			//	this.endSpeed = 0;
			//	this.accelerationTime = stopTime * speed / walkSpeed;
			//}
			//else
			//{
				//assume no acceleration
				this.isAccelerating = false;
				this.accelerationTime = -1;
			//}
		}
		else
		{
			this.isAccelerating = true;
			this.endSpeed = (endSpeed >=0) ? endSpeed : 0;
			this.accelerationTime = accelerationTime;
		}
		if(this.isAccelerating)
			this.acceleration = (this.endSpeed - this.speed)/this.accelerationTime;
		else
			this.acceleration = 0;
	}
	
	public bool IsAccelerating()
	{
		return isAccelerating;
	}
	public Vector3 GetAcceleration()
	{
		if (!IsAccelerating())
			return Vector3.zero;
		return new Vector3(Convert.ToSingle(acceleration * Math.Sin(angle)), 0, Convert.ToSingle(acceleration * Math.Cos(angle)));
	}
	public long GetAccelerationTime()
	{
		return this.accelerationTime;
	}
	public void SetAccelerationTime(long time)
	{
		if(time > 0)
		{
			this.accelerationTime = time;
			this.acceleration = (endSpeed - speed)/accelerationTime;
			isAccelerating = true;
		}
	}
	public void ResetAcceleration()
	{
		this.accelerationTime = -1;
		acceleration = 0;
		isAccelerating = false;
		endSpeed = -1;
	}
	public Vector3 GetSpeed()
	{
		return new Vector3(Convert.ToSingle(speed * Math.Sin(angle)), 0, Convert.ToSingle(speed * Math.Cos(angle)));
	}
	public Vector3 GetEndSpeed()
	{
		return new Vector3(Convert.ToSingle(endSpeed * Math.Sin(angle)), 0, Convert.ToSingle(endSpeed * Math.Cos(angle)));
	}
	// Send heading to server
	public void Send() {
		SmartFoxClient client = NetworkController.GetClient();
		string extensionName = NetworkController.GetExtensionName();
		
		Hashtable data = new Hashtable();
		data.Add("x", this.position.x);
		data.Add("y", this.position.y);
		data.Add("z", this.position.z);
		data.Add("a", this.angle);
		data.Add("t", Convert.ToDouble(this.time));
		data.Add("s", this.speed);
		if(isAccelerating)
		{
			data.Add("at", this.accelerationTime);
			data.Add("es", this.endSpeed);
		}
		else
			data.Add("at",-1);
		
		//send transform sync data. 
		client.SendXtMessage(extensionName, "h", data);			
	}
	

	override public string ToString()
	{
		string ret = "";
		ret+="Heading pos=("+position.x+","+position.y+","+position.z+") ";
		ret+="angle="+angle+" ";
		ret+="speed="+speed+" ";
		ret+="time="+time;
		return ret;
	}
	
	public bool IsFutureOf(Heading old)
	{
		//if (!objPos.Equals(lastPos) || objAngle != lastAngle || objSpeed != lastSpeed) 
		if(old == null)
			return false;
		//time elapsed between this heading and the old one
		float elapsed = (float)(GetTime() - old.GetTime())/1000.0f;
		float elapsed2 = 0;
		if(old.IsAccelerating())
		{
			//time elasped while accelerating from old.speed to old.endSpeed
			elapsed = Math.Max(elapsed, old.GetAccelerationTime());
			//time elapsed while moving at constant speed with magnitude equal to endSpeed
			elapsed2 = Math.Max(elapsed - old.GetAccelerationTime(), 0);
		}
		
		//compute the expected future state of the old heading
		Vector3 expectedFuture = old.GetPosition() + old.GetSpeed() * elapsed + 0.5f * old.GetAcceleration() * Convert.ToSingle(Math.Pow(elapsed, 2));
		expectedFuture += (elapsed2 * old.GetEndSpeed()); 
		float expectedSpeed = old.GetSpeed().magnitude + old.GetAcceleration().magnitude * elapsed;
		float expectedAngle = old.GetAngle();
		
		float dp= Vector3.Distance(expectedFuture, position);
		float ds = (float) Math.Abs(expectedSpeed - speed);
		float da = (float) Math.Abs(expectedAngle - angle);
		
		if(dp <  0.01 && ds < 0.01 && da < 0.01)
			return true;
		return false;
	}
}
