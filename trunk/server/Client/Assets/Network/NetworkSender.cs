using UnityEngine;
using System.Collections;
using System;

using SmartFoxClientAPI;
using SmartFoxClientAPI.Data;

public class NetworkSender : MonoBehaviour {

	// Use this for initialization
	void Start () 
    {
	
	}
	
	// Update is called once per frame
	void Update () 
    {
	
	}

    public void SendActionList(ActionList clientList)
    {
    }

    public void SendHeading(Heading heading)
    {
        Hashtable data = new Hashtable();
        data.Add("x", heading.GetPosition().x);
        data.Add("y", heading.GetPosition().y);
        data.Add("z", heading.GetPosition().z);
        data.Add("a", heading.GetAngle());
        data.Add("t", Convert.ToDouble(heading.GetTime()));
        data.Add("s", heading.GetSpeed().magnitude);
        if (heading.IsAccelerating())
        {
            data.Add("at", Convert.ToDouble(heading.GetAccelerationTime()));
            data.Add("es", heading.GetEndSpeed().magnitude);
        }
        else
        {
            data.Add("at", -1);
        }

        //send heading to server
        SmartFoxClient client = NetworkController.GetClient();
        string extensionName = NetworkController.GetExtensionName();
        client.SendXtMessage(extensionName, "h", data);		
    }
}
