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
	
	//TODO: animation["run"].layer = -1;
	anim["walk"].layer = -1;
	anim["idle1"].layer = -2;
	//TODO: anim.SyncLayer(-1);

	// We are in full control here - don't let any other animations play when we start
	anim.Stop();
	anim.Play("idle1");
	
	allowAnimation = true;
}

function Update ()
{
	if(allowAnimation)
	{
		var playerController : ThirdPersonController = GetComponent(ThirdPersonController);
		var currentSpeed = playerController.GetSpeed();

		// Fade in run
		if (currentSpeed > playerController.trotSpeed)
		{
			//TODO: 
			//animation.CrossFade("run");
			//SendMessage("PlayAnimation", "run");
		}
		// Fade in trot
		else if (currentSpeed > playerController.walkSpeed)
		{
			anim["walk"].speed=currentSpeed;
			anim.CrossFade("walk");
			//send animation to the server
			SendMessage("PlayAnimation", "walk");
		}
		//Fade in walk
		else if (currentSpeed > 0.1)
		{
			anim["walk"].speed=currentSpeed;
			anim.CrossFade("walk");
			//send animation to the server
			SendMessage("PlayAnimation", "walk");
		}
		// Fade out animations "walk" and "run"
		else
		{
			anim.Blend("walk", 0.0, 0.3);
			//TODO: animation.Blend("run", 0.0, 0.3);
			//send animation to the server
			SendMessage("PlayAnimation", "idle1");
		}
	}
}

@script AddComponentMenu ("Third Person Player/Third Person Player Animation")