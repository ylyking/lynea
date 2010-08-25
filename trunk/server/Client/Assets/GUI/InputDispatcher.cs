using System;
using System.Collections;
using UnityEngine;


public class InputDispatcher : MonoBehaviour
{
    void Update()
    {
        if (Input.GetButtonDown("Fire2"))
        {
            Ray ray = Camera.main.ScreenPointToRay(new Vector3(Input.mousePosition.x, Input.mousePosition.y, 0));
            //Debug.DrawRay(ray.origin, ray.direction * 10, Color.yellow);
            RaycastHit hit;
            if (Physics.Raycast(ray, out hit))
            {
                if (hit.collider != null)
                {
                    if (hit.collider.GetComponent<NPCActionManager>() != null)
                        hit.collider.SendMessage("OnRightClick");
                }
            }
            
        }
    }
}
