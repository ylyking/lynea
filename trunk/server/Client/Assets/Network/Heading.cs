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
	public void InitFromValues(Vector3 position, float angle, long time, float speed, float endSpeed, long accelerationTime)
	{
		SetPosition(position);
		SetAngle(angle);
		SetTime(time);
		SetSpeed(speed, endSpeed, accelerationTime);
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
			this.acceleration = (this.endSpeed - this.speed)/((float)this.accelerationTime/1000.0f);
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
			this.acceleration = (endSpeed - speed)/((float)this.accelerationTime/1000.0f);
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
		if (endSpeed >=0)
			return new Vector3(Convert.ToSingle(endSpeed * Math.Sin(angle)), 0, Convert.ToSingle(endSpeed * Math.Cos(angle)));
		return Vector3.zero;//if endSpeed == -1
	}
	// Send heading to server
	public void Send() {
        GameObject networkController = GameObject.Find("NetworkController");
        networkController.SendMessage("SendHeading", this);	
	}
	

	override public string ToString()
	{
		string ret = "";
		ret+="Heading pos=("+position.x+","+position.y+","+position.z+") ";
		ret+="angle="+angle+" ";
		ret+="speed="+speed+" ";
		ret+="acc="+acceleration+" ";
		ret+="at="+accelerationTime+" ";
		ret+="es="+endSpeed+" ";
		ret+="time="+time+" ";
		return ret;
	}
	
	public bool IsFutureOf(Heading old)
	{
		if(old == null)
			return false;
		//time elapsed between this heading and the old one
		float elapsed = (float)(GetTime() - old.GetTime())/1000.0f;
		float elapsed2 = 0;
		if(old.IsAccelerating())
		{
			//time elasped while accelerating from old.speed to old.endSpeed
			elapsed = Math.Min(elapsed, (float)old.GetAccelerationTime()/1000.0f);
			//time elapsed while moving at constant speed with magnitude equal to endSpeed
			elapsed2 = Math.Max(elapsed - (float)old.GetAccelerationTime()/1000.0f, 0);
		}
		
		//compute the expected future state of the old heading
		Vector3 expectedPosition = old.GetPosition() + old.GetSpeed() * elapsed + 0.5f * old.GetAcceleration() * Convert.ToSingle(Math.Pow(elapsed, 2));
		expectedPosition += (elapsed2 * old.GetEndSpeed()); 
		Vector3 expectedSpeed = old.GetSpeed()+ old.GetAcceleration() * elapsed;
		float expectedAngle = old.GetAngle();
		
		float dp= Vector3.Distance(expectedPosition, position);
		float ds = Vector3.Distance(expectedSpeed, GetSpeed());
		float da =  AngleDistance(expectedAngle, angle);
		
		if(dp <  0.05 && ds < 0.05 && da < 0.05)
			return true;
		/*Debug.Log("NOT FUTURE");
		Debug.Log("PAST->EXP. FUTURE: -- pos:"+old.GetPosition()+"->"+expectedPosition+", s:"+old.GetSpeed()+"->"+expectedSpeed+", a:"+old.GetAngle()+"->"+expectedAngle+";old is accel?"+old.IsAccelerating());
		Debug.Log("REAL FUTURE: -- pos:"+"  "+"->"+position+", s:"+"  "+"->"+GetSpeed()+", a:"+"  "+"->"+GetAngle()+", accel?"+IsAccelerating());
		Debug.Log("|---> :dpos,ds,da= "+dp+", "+ds+", "+da+".");
		Debug.Log("expectedSpeed = "+expectedSpeed+" = s_0+a*t_a = "+old.GetSpeed()+"+"+old.GetAcceleration()+"*"+elapsed);
		Debug.Log("accelTime = "+old.GetAccelerationTime()+", s= "+ GetSpeed()+", ds= "+ds);
		*/
		return false;
	}
	
	private float AngleDistance (float a, float b)
	{
		a = Mathf.Repeat(a, (float)(2*Math.PI));
		b = Mathf.Repeat(b, (float)(2*Math.PI));
		
		return Mathf.Abs(b - a);
	}
	
}
