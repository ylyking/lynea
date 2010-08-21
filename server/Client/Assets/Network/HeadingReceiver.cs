using UnityEngine;
using System.Collections;
using System;

using SmartFoxClientAPI;
using SmartFoxClientAPI.Data;

public class HeadingReceiver : MonoBehaviour {
	
	public float interceptTimeMultiplier = 7;
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
			//Debug.Log(course);
		}
		//check if we have already received at least one heading
		if(hasReceivedHeading)
		{
			//figure out where it should render
			if(isIntercepting)
			{
				//Debug.Log("isIntercepting");
				if (ServerClock.GetTime() > interceptTime) 
				{
					//Debug.Log("finish Intercepting");
					isIntercepting = false;
				}
				//Debug.Log("->UpdateView(interceptor)");
				else
				{
					UpdateView(interceptor);
				}
			}
			if(!isIntercepting)
			{
				//Debug.Log("is not intercepting");
				UpdateCourse();
				//Debug.Log("->UpdateView(course)");
				UpdateView(course);
			}
			transform.position = view.GetPosition();
			transform.rotation = view.GetRotation();
			//Debug.Log("VIEW : x="+transform.position.x +" z="+transform.position.z + " sx="+view.GetSpeed().x+ " sz="+view.GetSpeed().z + " courseIsAcc?" +course.IsAccelerating());
			SendMessage("PlayAnimationFromSpeed", /*(course.IsAccelerating())?0:£*/(view.GetSpeed().magnitude));
		}
	}
	
	private void UpdateView(Heading heading)
	//Vector3 position = Vector3.Lerp(heading.GetPosition(), course.GetPosition(), elapsed/);
	//transform.rotation = Quaternion.Slerp(interpolateFrom.rotation, interpolateTo.rotation, t);
	{
		//amount of time since starting this heading
		float elapsed = (float)(ServerClock.GetTime() - heading.GetTime())/1000.0f;
		if (elapsed <0)
			return;
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
		if(heading == interceptor && softAngleVariation)
			angle = view.GetAngle();
		
		float speed = heading.GetSpeed().magnitude;
		if (heading.IsAccelerating()) {
			int sign  = (Vector3.Dot(heading.GetAcceleration(), heading.GetSpeed())) > 0 ? 1 : -1;
			speed += sign*heading.GetAcceleration().magnitude * elapsed;
		}
		
		//Debug.Log("UV: view pos: ["+"("+view.GetPosition().x+","+view.GetPosition().z+")->"+"("+x+","+z+")"+ "]" + " --- "+heading);
		//Debug.Log("UV: view speed: ["+view.GetSpeed().magnitude+"->"+speed + "]" + " --- "+heading);
		//Debug.Log("UV: view angle: ["+view.GetAngle()+"->"+angle + "]" + " --- "+heading);
		
		//Debug.Log("UpdateView: elapsed [sec]="+elapsed+", view_x="+x);
		//copy properties to the view heading
		view.InitFromValues(new Vector3(x, 0, z), angle, ServerClock.GetTime(), speed);
	}
	
	private void UpdateCourse() 
	{
		//Debug.Log("UpdateCourse()");
		
		//if the course is accelerating and it should be done accelerating
		if (course.IsAccelerating() && ServerClock.GetTime() >= course.GetTime() + course.GetAccelerationTime()) 
		{
			//Debug.Log("the course is accelerating and it should be done accelerating");
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
			//Debug.Log("UpdateCourse:  course_x="+x+", course_sx="+course.GetEndSpeed().magnitude);
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
			//temporary ignore s=0
			//if (speed ==0 && pos.x == 5)
			//	return;
			long accelerationTime = (long) data.GetNumber("at");
			float endSpeed = -1;
			if (accelerationTime != -1)
			{
				endSpeed = Convert.ToSingle(data.GetNumber("es")); 
			}
			Debug.Log("ReceiveHeading() has received a heading : "+pos+"; a="+angle+", s="+speed+", t="+time+", at="+accelerationTime+", es="+endSpeed);

			course.InitFromValues(pos, angle, time, speed, endSpeed, accelerationTime);
			if(hasReceivedHeading == true)
				CreateInterceptor();
			
			hasReceivedHeading = true;
		}
	}
	
	private void CreateInterceptor()
	{
		//how long ago was this new vector born?
		long age = ServerClock.GetTime() - course.GetTime();
		age = (age >=0) ? age : 0;
		Debug.Log("interc: age="+age);
		if(age == 0)
			return;
		
		isIntercepting = true;
	
		//update the interceptor to start on a new course
		interceptor.SetPosition(view);
		interceptor.SetTime(ServerClock.GetTime());
		
		//how long from now to give the interceptor time to converge on the course
		long scheduled = (long) ((float)age * interceptTimeMultiplier);
	
		scheduled = Math.Min(scheduled, maxDurationInterceptTime);
		
		//in absolute time, this is when the convergence is complete
		long when = ServerClock.GetTime() + scheduled;
		
		//the x/z position where the two paths intersect
		float targetx= course.GetPosition().x + course.GetSpeed().x * (float)(when - course.GetTime())/1000.0f;
		float targetz = course.GetPosition().z + course.GetSpeed().z * (float)(when - course.GetTime())/1000.0f;
		
		//if the new vector has acceleration
		if (course.IsAccelerating()) {
			Debug.Log("COURSE IS ACCELERATING");
			//find x/z for when it is done accelerating
			float tx = course.GetPosition().x + course.GetSpeed().x * (float)course.GetAccelerationTime()/1000.0f + 0.5f * course.GetAcceleration().x * Convert.ToSingle(Math.Pow((float)course.GetAccelerationTime()/1000.0f, 2));
			float tz = course.GetPosition().z + course.GetSpeed().z * (float)course.GetAccelerationTime()/1000.0f  + 0.5f * course.GetAcceleration().z * Convert.ToSingle(Math.Pow((float)course.GetAccelerationTime()/1000.0f, 2));
			Debug.Log("Intercept @ ("+tx+","+tz+")");
			//update the target intersection point to be when the acceleration is done
			targetx = tx;
			targetz = tz;
			
			//how long from now until acceleration is done
			long timeDelta = course.GetTime() +course.GetAccelerationTime() - ServerClock.GetTime();

			//update the relative time variable saying how long from to to have the intersection complete
			scheduled = timeDelta;
			Debug.Log("Scheduled in "+timeDelta+" ms");
			//in absolute time, when will the intersection be complete
			when = ServerClock.GetTime() + scheduled;
		}
		
		//distance between the current interceptor position and where the intersection will take place
		float dis = Convert.ToSingle(Math.Sqrt(Math.Pow(targetx - interceptor.GetPosition().x, 2) + Math.Pow(targetz - interceptor.GetPosition().z, 2)));
		//Debug.Log("dis = "+dis+" sqrt[(tx-interceptor.x)² + (tz-interceptor.z)²]= sqrt[("+targetx+"-"+interceptor.GetPosition().x+")² + ("+targetz+"-"+interceptor.GetPosition().z+")²]");


		//speed that must occur to achieve this
		float speed = dis / ((float)scheduled/1000.0f);
		
		//angle of the interceptor
		float angle = Convert.ToSingle(Math.PI/2 - Math.Atan2(targetz - interceptor.GetPosition().z, targetx - interceptor.GetPosition().x));
		
		float angleVariation1 = AngleDistance(view.GetAngle(), angle);
		float angleVariation2 = AngleDistance(angle, course.GetAngle());
		softAngleVariation = false;
		if(dis <0.01 || scheduled <=0)
		{
			Debug.Log("NO INTERCEPT : dis=" +dis +" scheduled="+scheduled);
			isIntercepting = false;
			return;
		}
		else if(dis<0.3 && angleVariation1>Math.PI/8 && angleVariation2>Math.PI/8)
		{
			softAngleVariation = true;
			Debug.Log("SOFT INTERCEPT : dis=" +dis +" scheduled="+scheduled+" angleVariation1="+angleVariation1+" angleVariation2="+angleVariation2);
		}
		else
		{
			Debug.Log("INTERCEP: dis=" +dis +" scheduled="+scheduled + " speed="+(dis / ((float)scheduled/1000.0f)));
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
