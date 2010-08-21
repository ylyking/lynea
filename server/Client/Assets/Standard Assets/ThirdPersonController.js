
// The speed when walking
var walkSpeed = 2.0;
// after trotAfterSeconds of walking we trot with trotSpeed
var trotSpeed = 2.0;
// when pressing "Fire3" button (cmd) we start running
var runSpeed = 6.0;

var speedSmoothing = 10.0;
var rotateSpeed = 500.0;
var trotAfterSeconds = 3.0;

var stopTime:long = 300; //stop time in milliseconds

// The camera doesnt start following the target immediately but waits for a split second to avoid too much waving around.
private var lockCameraTimer = 0.0;

// The current move direction in x-z
private var moveDirection = Vector3.zero;

// The current x-z move speed
private var moveSpeed = 0.0;

// The last collision flags returned from controller.Move
private var collisionFlags : CollisionFlags; 


// Are we moving backwards (This locks the camera to not do a 180 degree spin)
private var movingBack = false;
// Is the user pressing any keys?
private var isMoving = false;
// When did the user start walking (Used for going into trot after a while)
private var walkTimeStart = 0.0;

private var lean = 0.0;
private var slammed = false;

private var isControllable = true;

private var up = Vector3.forward;
private var followCameraComponent;

private var endSpeed:float = -1;
private var accelerationTime:long = -1;
private var isAccelerating:boolean = false;
private var acceleration:float = 0;

function Awake ()
{
	moveDirection = transform.TransformDirection(Vector3.forward);
	followCameraComponent = Camera.main.GetComponent("FollowCamera");
}


function UpdateSmoothedMovementDirection ()
{
	var cameraTransform = Camera.main.transform;
	
	// World Coords of {Forward vector relative to the camera} along the x-z plane	
	//OLD : var forward = cameraTransform.TransformDirection(Vector3.forward);
	//NEW:
	var forward = followCameraComponent.GetUpDirection();
	
	forward.y = 0;
	forward = forward.normalized;

	// Right vector relative to the camera
	// Always orthogonal to the forward vector
	var right = Vector3(forward.z, 0, -forward.x);

	var v = Input.GetAxisRaw("Vertical");
	var h = Input.GetAxisRaw("Horizontal");

	// Are we moving backwards or looking backwards
	if (v < -0.2)
		movingBack = true;
	else
		movingBack = false;
	
	var wasMoving = isMoving;
	isMoving = Mathf.Abs (h) > 0.1 || Mathf.Abs (v) > 0.1;
	

	// Target direction relative to the camera
	var targetDirection = h * right + v * forward;
	

	// Lock camera for short period when transitioning moving & standing still
	lockCameraTimer += Time.deltaTime;
	if (isMoving != wasMoving)
		lockCameraTimer = 0.0;

	// We store speed and direction seperately,
	// so that when the character stands still we still have a valid forward direction
	// moveDirection is always normalized, and we only update it if there is user input.
	if (targetDirection != Vector3.zero)
	{
		// If we are really slow, just snap to the target direction
		if (moveSpeed < walkSpeed * 0.9)
		{
			moveDirection = targetDirection.normalized;
		}
		// Otherwise smoothly turn towards it
		else
		{
			moveDirection = Vector3.RotateTowards(moveDirection, targetDirection, rotateSpeed * Mathf.Deg2Rad * Time.deltaTime, 1000);
			
			moveDirection = moveDirection.normalized;
		}
	}
	
	// Choose target speed
	//* We want to support analog input but make sure you cant walk faster diagonally than just forward or sideways
	var targetSpeed = Mathf.Min(targetDirection.magnitude, 1.0);

	// Pick speed modifier
	if (Input.GetButton ("Fire3"))
	{
		targetSpeed *= runSpeed;
	}
	else if (Time.time - trotAfterSeconds > walkTimeStart)
	{
		targetSpeed *= trotSpeed;
	}
	else
	{
		targetSpeed *= walkSpeed;
	}
	
	if(!isMoving)
	{
		if(wasMoving)
		{
			accelerationTime = stopTime;
			endSpeed = 0;
			acceleration = (endSpeed - moveSpeed) / (accelerationTime/1000.0);
			isAccelerating = true;
			//immediately send heading to server
			SendMessage("SendHeading");
		}
		
		if(isAccelerating && moveSpeed > 0)
		{
			moveSpeed += acceleration * Time.deltaTime;
			accelerationTime -= Mathf.Round(Time.deltaTime*1000);
		}
		else if (isAccelerating)
		{
			acceleration = 0;
			moveSpeed = 0;
			isAccelerating = false;
			endSpeed = -1;
			accelerationTime = -1;
		}
	}
	else
	{
		//OLD: Smooth the speed based on the current target direction
		//var curSmooth = speedSmoothing * Time.deltaTime;
		//moveSpeed = Mathf.Lerp(moveSpeed, targetSpeed, curSmooth);
		//NEW:
		moveSpeed = targetSpeed;
	}
	
	// Reset walk time start when we slow down
	if (moveSpeed < walkSpeed * 0.3)
		walkTimeStart = Time.time;
		
}



function Update() {
	
	if (!isControllable)
	{
		// kill all inputs if not controllable.
		Input.ResetInputAxes();
	}

	UpdateSmoothedMovementDirection();
	
	
	// Calculate actual motion
	var movement = moveDirection * moveSpeed;
	movement *= Time.deltaTime;
	
	// Move the controller
	var controller : CharacterController = GetComponent(CharacterController);
	collisionFlags = controller.Move(movement);
	
	// Set rotation to the move direction	
	transform.rotation = Quaternion.LookRotation(moveDirection);
			
}


function GetSpeed () {
	return moveSpeed;
}

function GetEndSpeed () {
	return endSpeed;
}

function GetAccelerationTime() {
	return accelerationTime;
}

function IsAccelerating() {
	return isAccelerating;
}



function GetDirection () {
	return moveDirection;
}

function IsMovingBackwards () {
	return movingBack;
}

function GetLockCameraTimer () 
{
	return lockCameraTimer;
}

function IsMoving ()  : boolean
{
	return Mathf.Abs(Input.GetAxisRaw("Vertical")) + Mathf.Abs(Input.GetAxisRaw("Horizontal")) > 0.5;
}

function Reset ()
{
	gameObject.tag = "Player";
}
// Require a character controller to be attached to the same game object
@script RequireComponent(CharacterController)
@script AddComponentMenu("Third Person Player/Third Person Controller")
