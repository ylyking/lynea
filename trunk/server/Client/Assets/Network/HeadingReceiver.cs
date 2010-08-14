using UnityEngine;
using System.Collections;
using System;

using SmartFoxClientAPI;
using SmartFoxClientAPI.Data;

public class HeadingReceiver : MonoBehaviour {
	
	public float interceptTimeMultiplier = 7;
	public float maxDurationInterceptTime = 800; //Milliseconds
	
	private bool receiveMode = false;
	private bool hasReceivedHeading = false;
	private bool isIntercepting;
	
	private Heading course;
	private Heading interceptor;
	private Heading view;
	
	private float interceptTime;
	
	// Use this for initialization
	void Awake () {
		course = new Heading();
		interceptor = new Heading();
		view = new Heading();
	}
	
	float t =0;
	// Update is called once per frame
	void Update () 
	{
		t+=Time.deltaTime;
		if (t>0.5f)
		{
			t=0;
			//Debug.Log(course);
		}
		//check if we have already received at least one heading
		if(hasReceivedHeading)
		{
			//figure out where it should render
			if(isIntercepting)
			{
				if (ServerClock.GetTime() > interceptTime) 
				{
					isIntercepting = false;
				}
				//Debug.Log("->UpdateView(interceptor)");
				UpdateView(interceptor);
			}
			else
			{
				UpdateCourse();
				//Debug.Log("->UpdateView(course)");
				UpdateView(course);
			}
			transform.position = view.GetPosition();
			transform.rotation = view.GetRotation();
		}
	}
	
	private void UpdateView(Heading heading)
	//Vector3 position = Vector3.Lerp(heading.GetPosition(), course.GetPosition(), elapsed/);
	//transform.rotation = Quaternion.Slerp(interpolateFrom.rotation, interpolateTo.rotation, t);
	{
		//amount of time since starting this heading
		float elapsed = (float)(ServerClock.GetTime() - heading.GetTime())/1000.0f;
		
		//x, z position
		float x = heading.GetPosition().x + heading.GetSpeed().x * elapsed + 0.5f * heading.GetAcceleration().x * Convert.ToSingle(Math.Pow(elapsed, 2));
		float z = heading.GetPosition().z + heading.GetSpeed().z * elapsed + 0.5f * heading.GetAcceleration().z * Convert.ToSingle(Math.Pow(elapsed, 2));
		//Debug.Log("???="+0.5f * heading.GetAcceleration().x * Convert.ToSingle(Math.Pow(elapsed, 2))+","+0.5f * heading.GetAcceleration().z * Convert.ToSingle(Math.Pow(elapsed, 2)));
		//value for easing rotation
		//TODO: replace with a better rotating algorithm
		//float k = .25f;
		//double angDiff = (double) (heading.GetAngle() - view.GetAngle());
		//if (angDiff> Math.PI) { angDiff = angDiff - 2*Math.PI; } 
		//else if (angDiff < -Math.PI) { angDiff = 2*Math.PI +angDiff; }
		//float angMov = (float) angDiff * k;
		//float angle = view.GetAngle() + angMov;
		float angle = heading.GetAngle();
		
		float speed = heading.GetSpeed().magnitude;
		if (heading.IsAccelerating()) {
			speed += heading.GetAcceleration().magnitude * elapsed;
		}
		
		//Debug.Log("UV: view pos: ["+"("+view.GetPosition().x+","+view.GetPosition().z+")->"+"("+x+","+z+")"+ "]" + " --- "+heading);
		//Debug.Log("UV: view speed: ["+view.GetSpeed().magnitude+"->"+speed + "]" + " --- "+heading);
		//Debug.Log("UV: view angle: ["+view.GetAngle()+"->"+angle + "]" + " --- "+heading);
		//copy properties to the view heading
		view.InitFromValues(new Vector3(x, 0, z), angle, ServerClock.GetTime(), speed);
	}
	
	private void UpdateCourse() 
	{
		//if the course is accelerating and it should be done accelerating
		if (course.IsAccelerating() && ServerClock.GetTime() >= course.GetTime() + course.GetAccelerationTime()) 
		{
			//update the course to a position of where it would be at the moment acceleration is done
			float x = course.GetPosition().x + course.GetSpeed().x * course.GetAccelerationTime() + 0.5f * course.GetAcceleration().x * Convert.ToSingle(Math.Pow(course.GetAccelerationTime(), 2));
			float z = course.GetPosition().z + course.GetSpeed().z * course.GetAccelerationTime() + 0.5f * course.GetAcceleration().z * Convert.ToSingle(Math.Pow(course.GetAccelerationTime(), 2));
			course.SetPosition(new Vector3(x, 0, z));
			course.SetTime(course.GetTime() + course.GetAccelerationTime());
			
			//reset acceleration values
			course.ResetAcceleration();
			
			//give a new speed (the one is should have after acceleration)
			course.SetSpeed(course.GetEndSpeed().magnitude, -1, -1);
			
		}
	}
	
	// We call it on remote player to start receiving his transform
	void StartReceiving() {
		receiveMode = true;
	}
	
	//This method is called when receiving remote heading
	// We update course here
	public void ReceiveHeading(SFSObject data) 
	{
		if (receiveMode) 
		{
			Vector3 pos = new Vector3(Convert.ToSingle(data.GetNumber("x")), 
										Convert.ToSingle(data.GetNumber("y")),
										Convert.ToSingle(data.GetNumber("z"))
										);
			float angle = Convert.ToSingle(data.GetNumber("a"));
			long time = (long) Convert.ToSingle(data.GetNumber("t"));
			float speed = Convert.ToSingle(data.GetNumber("s")); 
			Debug.Log("ReceiveHeading() has received a heading : ("+Convert.ToSingle(data.GetNumber("x"))+","+Convert.ToSingle(data.GetNumber("z"))+"); a="+angle+", s="+speed+", t="+time);

			course.InitFromValues(pos, angle, time, speed);
			if(hasReceivedHeading == true)
				CreateInterceptor();
			
			hasReceivedHeading = true;
		}
	}
	
	private void CreateInterceptor()
	{
		//how long ago was this new vector born?
		float age = ServerClock.GetTime() - course.GetTime();
		age = (age >=0) ? age : 0;
		Debug.Log("interc: age="+age);
		if(age == 0)
			return;
		
		isIntercepting = true;
	
		//update the interceptor to start on a new course
		interceptor.SetPosition(view);
		interceptor.SetTime(ServerClock.GetTime());
		
		
		
		//how long from now to give the interceptor time to converge on the course
		float scheduled = age * interceptTimeMultiplier;
	
		scheduled = Math.Min(scheduled, maxDurationInterceptTime);
		
		//in absolute time, this is when the convergence is complete
		float when = ServerClock.GetTime() + scheduled;
		
		//the x/z position where the two paths intersect
		float targetx= course.GetPosition().x + course.GetSpeed().x * (when - course.GetTime());
		float targetz = course.GetPosition().z + course.GetSpeed().z * (when - course.GetTime());
		
		//if the new vector has acceleration
		if (course.IsAccelerating()) {
			Debug.Log("COURSE IS ACCELERATING !!");
			//find x/z for when it is done accelerating
			float tx = course.GetPosition().x + course.GetSpeed().x * course.GetAccelerationTime() + 0.5f * course.GetAcceleration().x * Convert.ToSingle(Math.Pow(course.GetAccelerationTime(), 2));
			float tz = course.GetPosition().z + course.GetSpeed().z* course.GetAccelerationTime() + 0.5f * course.GetAcceleration().z * Convert.ToSingle(Math.Pow(course.GetAccelerationTime(), 2));
			
			//update the target intersection point to be when the acceleration is done
			targetx = tx;
			targetz = tz;
			
			//how long from now until acceleration is done
			float timeDelta = course.GetTime() +course.GetAccelerationTime() - ServerClock.GetTime();
			
			//update the relative time variable saying how long from to to have the intersection complete
			scheduled = timeDelta;
			
			//in absolute time, when will the intersection be complete
			when = ServerClock.GetTime() + scheduled;
		}
		
		//distance between the current interceptor position and where the intersection will take place
		float dis = Convert.ToSingle(Math.Sqrt(Math.Pow(targetx - interceptor.GetPosition().x, 2) + Math.Pow(targetz - interceptor.GetPosition().z, 2)));
		
		//speed that must occur to achieve this
		float speed = dis / scheduled;
		
		//angle of the interseptor
		float angle = Convert.ToSingle(Math.PI/2 - Math.Atan2(targetz - interceptor.GetPosition().z, targetx - interceptor.GetPosition().x));
		
		//update properties on the interceptor
		interceptor.SetSpeed(speed, -1, -1);
		interceptor.SetAngle(angle);
		
		interceptTime = when;
	}
		
	
}
