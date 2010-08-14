
private var anim : Animation = null;
private var allowAnimation : boolean = false;
private var playerController : PlayerController;

function Start ()
{
	playerController = GetComponent(PlayerController);
}

function InitAnimation ()
{
	anim = GetComponentInChildren(Animation);
	
	// By default loop all animations
	anim.wrapMode = WrapMode.Loop;
	
	//animation["run"].layer = -1;
	anim["walk"].layer = -1;
	anim["idle1"].layer = -2;
	//anim.SyncLayer(-1);

	// We are in full control here - don't let any other animations play when we start
	anim.Stop();
	anim.Play("idle1");
	
	allowAnimation = true;
}



function PlayAnimation(message : String)
{
	if(allowAnimation)
	{
		switch(message)
		{
			case "walk":
				var currentSpeed = playerController.GetSpeed();
				anim["walk"].speed=currentSpeed;
				anim.CrossFade("walk");
				break;
			case "idle1":
				anim.Blend("walk", 0.0, 0.3);
				break;
			default:
					Debug.Log("Animation <"+message+"> is not supported for the Player");
		}
	}
}

@script AddComponentMenu ("Third Person Player/Player Animation")