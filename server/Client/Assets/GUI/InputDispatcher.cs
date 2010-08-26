using System;
using System.Collections;
using UnityEngine;


public class InputDispatcher : MonoBehaviour
{
    private Ray ray;
    void Update()
    {
        if (Input.GetButtonDown("Fire2"))
        {
            ray = Camera.main.ScreenPointToRay(new Vector3(Input.mousePosition.x, Input.mousePosition.y, 0));   
            RaycastHit hit;
            if (Physics.Raycast(ray, out hit))
            {
                if (hit.collider != null)
                {
                    if (hit.collider.GetComponent<NPCMenu>() != null)
                        hit.collider.SendMessage("OnRightClick");
                }
            }
            
        }
        Debug.DrawRay(ray.origin, ray.direction * 10, Color.yellow);
    }
}
