var speed = 2.0;
//var jumpSpeed = 8.0;
var gravity = 20.0;

private var moveDirection = Vector3.zero;
private var grounded : boolean = false;

function FixedUpdate() {
	if (grounded) {
		// We are grounded, so recalculate movedirection directly from axes
		moveDirection = new Vector3(Input.GetAxis("Horizontal"), 0, Input.GetAxis("Vertical"));
		if (moveDirection==Vector3.zero) {
			SendMessage("PlayAnimation", "idle1");
		}
		else {
			SendMessage("PlayAnimation", "walk");
		}	 	
			
		moveDirection = transform.TransformDirection(moveDirection);
		moveDirection *= speed;
		
		/*if (Input.GetButton ("Jump")) {
			SendMessage("PlayAnimation", "jump");
			moveDirection.y = jumpSpeed;
		}*/
		
	}
	
	// Apply gravity
	moveDirection.y -= gravity * Time.deltaTime;
		
	// Move the controller
	var controller : CharacterController = GetComponent(CharacterController);
	var flags = controller.Move(moveDirection * Time.deltaTime);
	grounded = (flags & CollisionFlags.CollidedBelow) != 0;
}

function GetSpeed(){
	return speed;
}

@script RequireComponent(CharacterController)