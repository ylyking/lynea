var character : String;
var config : String;

function Start()
{
	// Allow the CharacterElementDatabase to be downloaded.
	while (!CharacterGenerator.ReadyToUse) yield;

	// Configure a CharacterGenerator according to the configuration strings.
	if (character == "")
		generator = CharacterGenerator.CreateWithRandomConfig();
	else if (config == "")
		generator = CharacterGenerator.CreateWithRandomConfig(character);
	else
		generator = CharacterGenerator.CreateWithConfig(character + "|" + config);

	// Give the particle system some time to grow.
	//yield WaitForSeconds(0);

	// Wait for the assets to be downloaded.
	while (!generator.ConfigReady) yield;

	// Create the character.
	go = generator.Generate();

	// Attach the character to this prefab so we can move it around.
	go.transform.parent = transform;
	go.transform.localPosition = Vector3.zero;
	go.transform.localRotation = Quaternion.identity;

	// Stop emitting particles, as the character now exists.
	GetComponentInChildren(ParticleEmitter).emit = false;

	//notify the Component handling animation that the character has been created
	SendMessage("InitAnimation");
}