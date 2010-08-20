using UnityEngine;

using System.Collections;
using System.Timers;
using System;

using SmartFoxClientAPI;
using SmartFoxClientAPI.Data;


public class ServerClock
{

	static private bool clockReady = false;
	
	//each latency data point
	static private ArrayList deltas;
	
	//max number of deltas to keep track of
	static private int maxDeltas =20;
	
	//the best computed offset to getTimer based on the information we have
	static private long syncTimeDelta; 
	
	//true if there is a request out
	static private bool responsePending;
	
	//time we sent the request
	static private long timeRequestSent;
	
	//determined latency value
	static private int latency;
	
	//of the data set used, this is the biggest variation from the latency and the furthest value
	static private int latencyError;
	
	//how long to wait between pings
	static private int backgroundWaitTime;
	
	//timer used to make pings happen
	static private Timer backgroundTimer;
	
	//true if we are in the initial flurry of pings
	static private bool bursting;
	
	static private bool lockedInServerTime;

	
	/**
	 * Starts the process of determining the server clock time
	 * @param	How long to wait in between pings. If -1, then no pings are sent after the time is determined
	 * @param	If true, the pings are sent as fast as possible until the initial latency is found, then it slows
	 */
	 public static void Init(int pingDelay, bool burst)
	{
		if (pingDelay > 0) 
		{
			backgroundTimer = new Timer();
			backgroundTimer.Interval = pingDelay;
			backgroundTimer.Elapsed += new ElapsedEventHandler(OnTimer);
			backgroundTimer.Start();
		}
		
		deltas = new ArrayList();
		
		lockedInServerTime = false;
		responsePending = false;
		
		bursting = burst;
		RequestServerTime();
	}
	
	
	/**
	 * Stop gathering data
	 */
	 public static void Stop()
	{
		if (backgroundTimer != null) 
		{
			backgroundTimer.Stop();
			backgroundTimer = null;
		}
	}
	
	 private static void OnTimer(object source, ElapsedEventArgs e)
	{
		if (!responsePending && !bursting) 
		{
			RequestServerTime();
		}
	}
	
	//private static int numSend = 0;
	//private static int numReceive = 0;
	 private static void RequestServerTime()
	{
		if (!responsePending) 
		{
			SmartFoxClient client = NetworkController.GetClient();
			string extensionName = NetworkController.GetExtensionName();
	
			Hashtable data = new Hashtable();
			//add the "clock is not yet ready" message => this is a "get server time" request
			data.Add("r", false);
			//send the request. 
			client.SendXtMessage(extensionName, "c", data);			
			
			responsePending = true;
			timeRequestSent = (long) (Time.time*1000);
			
			//Debug.Log("sent request n°"+(++numSend));
		}
	}
	
	 public static void OnExtensionResponse(SFSObject data)
	{
		//Debug.Log("received request n°"+(++numReceive));
		
		responsePending = false;
		
		long serverTimeStamp = (long) data.GetNumber("t");
		AddTimeDelta(timeRequestSent, (long) (Time.time*1000), serverTimeStamp);
		
		if (bursting) 
		{
			if (deltas.Count == maxDeltas) 
			{
				bursting = false;
				clockReady = true;
				SendServerClockReady();
			}
			RequestServerTime();
		}
	}
	
	
	/**
	 * Gets the current server time as best approximated by the algorithm used.
	 * @return The server time (in milliseconds).
	 */
	public  static  long GetTime()
	{
		if(!IsClockReady())
		{
			return -1;
		}
		long now = (long) (Time.time*1000);
		return now + syncTimeDelta;
	}
	
	//public static IEnumerator WaitClockReady()
	//{
	//	while(!IsClockReady())
	//	{
	//		yield return 0;
	//	}
	//}
	
	/**
	 * Adds information to this class so it can properly converge on a more precise idea of the actual server time.
	 * @param	Time the client sent the request (in ms)
	 * @param	Time the client received the response (in ms)
	 * @param	Time the server sent the response (in ms)
	 */
	private static void AddTimeDelta(long clientSendTime, long clientReceiveTime, long serverTime)
	{
		//Debug.Log("AddTimeDelta()");
		//guess the latency
		int latency = (int) (clientReceiveTime - clientSendTime) / 2;
		
		long clientServerDelta = serverTime - clientReceiveTime;
		long timeSyncDelta = clientServerDelta + latency;
		TimeDelta delta = new TimeDelta(latency, timeSyncDelta);
		deltas.Add(delta);
		
		if (deltas.Count > maxDeltas) 
		{
			deltas.RemoveAt(0);
		}
		Recalculate();
	}
	
	/**
	 * Recalculates the best timeSyncDelta based on the most recent information
	 */
	private static void Recalculate()
	{
		//Debug.Log("Recalculate()");
		//grab a copy of the deltas array
		Array tmp_deltas = deltas.ToArray(typeof(TimeDelta));
		
		//sort them lowest to highest
		Array.Sort(tmp_deltas, new TimeDeltaComparer());
		
		//find the median value
		int medianLatency= DetermineMedian(tmp_deltas);
		
		//get rid of any latencies that fall outside a threshold
		PruneOutliers(tmp_deltas, medianLatency, 1.5f);
		latency = DetermineAverageLatency(tmp_deltas);
		
		if (!lockedInServerTime) 
		{
			//average the remaining time deltas
			syncTimeDelta = DetermineAverage(tmp_deltas);
			
			lockedInServerTime = deltas.Count == maxDeltas;
		}
	}
	
	/**
	 * Determines the average timeSyncDelta based on values within the acceptable range
	 * @param	Array of Time_deltas to be used
	 * @return Average timeSyncDelta
	 */
	private static long DetermineAverage(Array arr)
	{
		double total = 0;
		for (int i= 0; i < arr.Length;++i) 
		{
			TimeDelta td= (TimeDelta) arr.GetValue(i);
			total += td.GetTimeSyncDelta();
		}
		return (long) Math.Round(total / arr.Length);
	}
	
	private static int DetermineAverageLatency(Array arr)
	{
		float total = 0;
		for (int i= 0; i < arr.Length;++i) 
		{
			TimeDelta td= (TimeDelta) arr.GetValue(i);
			total += td.GetLatency();
		}
		
		int lat = (int) Math.Round(total / arr.Length);
		
		latencyError = Math.Abs(((TimeDelta)arr.GetValue(arr.Length- 1)).GetLatency() - lat);
		
		return lat;
	}
	
	/**
	 * Removes the values that are more than 1.5 X the median. The idea is that if it is outside 1.5 X the median then it was probably a TCP retransmit and so it should be ignored.
	 * @param	Array of Time_deltas to prune
	 * @param	Median value
	 * @param	Threshold multiplier of median value
	 */
	private static void PruneOutliers(Array arr, int median, float threshold)
	{
		int maxValue = (int) (median * threshold);
		for (int i = arr.Length - 1; i >= 0;--i ) 
		{
			TimeDelta td = (TimeDelta) arr.GetValue(i);
			if (td.GetLatency() > maxValue) 
			{
				//remove element i from arr
				Array copy = Array.CreateInstance(typeof(TimeDelta),arr.Length - 1);
				if (i>0)
					Array.Copy(arr, 0, copy, 0, i);
				if (i<arr.Length -1)
					Array.Copy(arr, i+1, copy, i, arr.Length - i);
				arr = copy;
			} 
			else 
			{
				//we can break out of the loop because they are already sorted in order, if we find one that isn't too high then we are done
				break;
			}
		}
	}
	
	/**
	 * Determines the median latency value.
	 * @param	Array of Time_deltas to use.
	 * @return Median value.
	 */
	private static int DetermineMedian(Array arr) 
	{
		int ind;
		if (arr.Length % 2 == 0) 
		{//even
			ind = arr.Length/ 2 - 1;
			return (((TimeDelta)arr.GetValue(ind)).GetLatency() + ((TimeDelta)arr.GetValue(ind + 1)).GetLatency()) / 2;
		} 
		else 
		{//odd
			ind = (int) Math.Floor((float)arr.Length / 2);
			return ((TimeDelta)arr.GetValue(ind)).GetLatency();
		}
	}
	

	
	public static int GetLatency() { return latency; }
	
	public static int GetLatencyError() { return latencyError; }
	
	public static int GetMaxDeltas() { return maxDeltas; }
	
	public static void SetMaxDeltas(int value){
		maxDeltas = value;
	}
	
	public static bool IsClockReady() { return clockReady;}
	
	
	public class TimeDeltaComparer : IComparer
	{
		/**
		 * Function used by Array.sort to sort an array from lowest to highest based on latency values.
		 * @param	TimeDelta
		 * @param	TimeDelta
		 * @return -1, 0, or 1
		 */
		int IComparer.Compare(System.Object ao, System.Object bo)
		{
			TimeDelta a = (TimeDelta) ao;
			TimeDelta b = (TimeDelta) bo;
			if (a.GetLatency() < b.GetLatency()) {
				return -1;
			} else if (a.GetLatency() > b.GetLatency()) {
				return 1;
			} else {
				return 0;
			}
		}
	}
		
	/**
	 * This class is used to store one round trip of information used in an attempt to sync the server and client clocks.
	 */
	public class TimeDelta 
	{
		private int latency;
		private long timeSyncDelta;
		
		/**
		 * Creates a new instance of the TimeDelta class.
		 * @param	Latency value (1/2 the round trip time)
		 * @param	TimeSyncDelta
		 */
		public TimeDelta(int latency, long timeSyncDelta) 
		{
			this.latency = latency;
			this.timeSyncDelta = timeSyncDelta;
		}
		
		public int GetLatency(){ return this.latency; }
		
		public long GetTimeSyncDelta(){ return this.timeSyncDelta; }
	}
	
	public static void SendServerClockReady()
	{
		Debug.Log("Sending ClockReady !!");
		foreach(TimeDelta td in deltas)
		{
			Debug.Log("latency="+td.GetLatency()+ " timeSyncDelta="+td.GetTimeSyncDelta());
		}
		Debug.Log("this.syncTimeDelta=" + syncTimeDelta);
		SmartFoxClient client = NetworkController.GetClient();
		string extensionName = NetworkController.GetExtensionName();
	
		Hashtable data = new Hashtable();
		//add the "clock is ready" message => this is not a "get server time" request
		data.Add("r", true);
		//send the request. 
		client.SendXtMessage(extensionName, "c", data);					
	}
	
}
