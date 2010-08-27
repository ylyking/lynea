using UnityEngine;
using System.Collections;
using System;

using SmartFoxClientAPI;
using SmartFoxClientAPI.Data;

public class HeadingReceiver : MonoBehaviour {
	
	public float interceptTimeMultiplier = 4; //TODO: find the best value
	public long maxDurationInterceptTime = 800; //Milliseconds
	
	private bool receiveMode = false;
	private bool hasReceivedHeading = false;
	private bool isIntercepting;
	private bool softAngleVariation;
	
	private Heading course;
	private Heading interceptor;
	private Heading view;
	
	private long interceptTime; //Milliseconds
	
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
		}
		//check if we have already received at least one heading
		if(hasReceivedHeading)
		{
			//figure out where it should render
			if(isIntercepting)
			{
				if (ServerClock.Instance.GetTime() > interceptTime) 
				{
					isIntercepting = false;
				}
				else
				{
					UpdateView(interceptor);
				}
			}
			if(!isIntercepting)
			{
				UpdateCourse();
				UpdateView(course);
			}
			transform.position = view.GetPosition();
			transform.rotation = view.GetRotation();
			SendMessage("PlayAnimationFromSpeed", /*(course.IsAccelerating())?0:*/(view.GetSpeed().magnitude));
		}
	}
	
	private void UpdateView(Heading heading)
	{
		//amount of time since starting this heading
		float elapsed = (float)(ServerClock.Instance.GetTime() - heading.GetTime())/1000.0f;
		if (elapsed <0)
			return;
		//x, z position
		float x = heading.GetPosition().x + heading.GetSpeed().x * elapsed + 0.5f * heading.GetAcceleration().x * Convert.ToSingle(Math.Pow(elapsed, 2));
		float z = heading.GetPosition().z + heading.GetSpeed().z * elapsed + 0.5f * heading.GetAcceleration().z * Convert.ToSingle(Math.Pow(elapsed, 2));

		float angle = heading.GetAngle();
		if(heading == interceptor && softAngleVariation)
			angle = view.GetAngle();
		
		float speed = heading.GetSpeed().magnitude;
		if (heading.IsAccelerating()) {
			int sign  = (Vector3.Dot(heading.GetAcceleration(), heading.GetSpeed())) > 0 ? 1 : -1;
			speed += sign*heading.GetAcceleration().magnitude * elapsed;
		}

		//copy properties to the view heading
		view.InitFromValues(new Vector3(x, 0, z), angle, ServerClock.Instance.GetTime(), speed);
	}
	
	private void UpdateCourse() 
	{		
		//if the course is accelerating and it should be done accelerating
		if (course.IsAccelerating() && ServerClock.Instance.GetTime() >= course.GetTime() + course.GetAccelerationTime()) 
		{
			//update the course to a position of where it would be at the moment acceleration is done
			float x = course.GetPosition().x + course.GetSpeed().x * (float)course.GetAccelerationTime()/1000.0f + 0.5f * course.GetAcceleration().x * Convert.ToSingle(Math.Pow((float)course.GetAccelerationTime()/1000.0f, 2));
			float z = course.GetPosition().z + course.GetSpeed().z * (float)course.GetAccelerationTime()/1000.0f + 0.5f * course.GetAcceleration().z * Convert.ToSingle(Math.Pow((float)course.GetAccelerationTime()/1000.0f, 2));
			course.SetPosition(new Vector3(x, 0, z));
			course.SetTime(course.GetTime() + course.GetAccelerationTime());
			
			//save endSpeed values
			float newSpeed = course.GetEndSpeed().magnitude;
			//reset acceleration values
			course.ResetAcceleration();
			
			//give a new speed (the one is should have after acceleration)
			course.SetSpeed(newSpeed, -1, -1);
			if (newSpeed == 0)
				SendMessage("PlayAnimation", "idle1");
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
			long time = (long) data.GetNumber("t");
			float speed = Convert.ToSingle(data.GetNumber("s")); 
			long accelerationTime = (long) data.GetNumber("at");
			float endSpeed = -1;
			if (accelerationTime != -1)
			{
				endSpeed = Convert.ToSingle(data.GetNumber("es")); 
			}
			//Debug.Log("ReceiveHeading() has received a heading : "+pos+"; a="+angle+", s="+speed+", t="+time+", at="+accelerationTime+", es="+endSpeed);

			course.InitFromValues(pos, angle, time, speed, endSpeed, accelerationTime);
			if(hasReceivedHeading == true)
				CreateInterceptor();
			
			hasReceivedHeading = true;
		}
	}
	
	private void CreateInterceptor()
	{
		//how long ago was this new vector born?
		long age = ServerClock.Instance.GetTime() - course.GetTime();
		age = (age >=0) ? age : 0;
		//Debug.Log("interc: age="+age);
		if(age == 0)
			return;
		
		isIntercepting = true;
	
		//update the interceptor to start on a new course
		interceptor.SetPosition(view);
		interceptor.SetTime(ServerClock.Instance.GetTime());
		
		//how long from now to give the interceptor time to converge on the course
		long scheduled = (long) ((float)age * interceptTimeMultiplier);
	
		scheduled = Math.Min(scheduled, maxDurationInterceptTime);
		

		//in absolute time, this is when the convergence is complete
		long when = ServerClock.Instance.GetTime() + scheduled;
		
		//the x/z position where the two paths intersect
		float targetx= course.GetPosition().x + course.GetSpeed().x * (float)(when - course.GetTime())/1000.0f;
		float targetz = course.GetPosition().z + course.GetSpeed().z * (float)(when - course.GetTime())/1000.0f;
		
		//if the new vector has acceleration
		if (course.IsAccelerating()) {
			//Debug.Log("COURSE IS ACCELERATING");

			//find x/z for when it is done accelerating
			float tx = course.GetPosition().x + course.GetSpeed().x * (float)course.GetAccelerationTime()/1000.0f + 0.5f * course.GetAcceleration().x * Convert.ToSingle(Math.Pow((float)course.GetAccelerationTime()/1000.0f, 2));
			float tz = course.GetPosition().z + course.GetSpeed().z * (float)course.GetAccelerationTime()/1000.0f  + 0.5f * course.GetAcceleration().z * Convert.ToSingle(Math.Pow((float)course.GetAccelerationTime()/1000.0f, 2));
			//Debug.Log("Intercept @ ("+tx+","+tz+")");

			//update the target intersection point to be when the acceleration is done
			targetx = tx;
			targetz = tz;
			
			//how long from now until acceleration is done
			long timeDelta = course.GetTime() +course.GetAccelerationTime() - ServerClock.Instance.GetTime();

			//update the relative time variable saying how long from to to have the intersection complete
			scheduled = timeDelta;
			//Debug.Log("Scheduled in "+timeDelta+" ms");

			//in absolute time, when will the intersection be complete
			when = ServerClock.Instance.GetTime() + scheduled;
		}
		
		//distance between the current interceptor position and where the intersection will take place
		float dis = Convert.ToSingle(Math.Sqrt(Math.Pow(targetx - interceptor.GetPosition().x, 2) + Math.Pow(targetz - interceptor.GetPosition().z, 2)));
				
		//speed that must occur to achieve this
		float speed = dis / ((float)scheduled/1000.0f);
		
		//angle of the interceptor
		float angle = Convert.ToSingle(Math.PI/2 - Math.Atan2(targetz - interceptor.GetPosition().z, targetx - interceptor.GetPosition().x));
		
		float angleVariation1 = AngleDistance(view.GetAngle(), angle);
		float angleVariation2 = AngleDistance(angle, course.GetAngle());
		softAngleVariation = false;
		if(dis < 0.01 || scheduled <= 0)
		{
			//Debug.Log("NO INTERCEPT : dis=" +dis +" scheduled="+scheduled);
			isIntercepting = false;
			return;
		}
		else if(dis < 0.4 && angleVariation1 > Math.PI/8 && angleVariation2 > Math.PI/8)
		{
			softAngleVariation = true;
			//Debug.Log("SOFT INTERCEPT : dis=" +dis +" scheduled="+scheduled+" angleVariation1="+angleVariation1+" angleVariation2="+angleVariation2);
		}
		else
		{
			//Debug.Log("INTERCEP: dis=" +dis +" scheduled="+scheduled + " speed="+(dis / ((float)scheduled/1000.0f)));
		}
		
		//update properties on the interceptor
		interceptor.SetSpeed(speed, -1, -1);
		interceptor.SetAngle(angle);
		
		interceptTime = when;
	}
	
		private float AngleDistance (float a, float b)
		{
			a = Mathf.Repeat(a, (float)(2*Math.PI));
			b = Mathf.Repeat(b, (float)(2*Math.PI));
			
			return Mathf.Abs(b - a);
		}
		
}
