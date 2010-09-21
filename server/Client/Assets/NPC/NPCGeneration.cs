using System.Collections;
using UnityEngine;

public class NPCGeneration : MonoBehaviour
{
	// Use this for initialization
	public IEnumerator ReceiveConfiguration (string character, string config) 
    {
        // Allow the CharacterElementDatabase to be downloaded.
        while (!CharacterGenerator.ReadyToUse) yield return 0;

        // Configure a CharacterGenerator according to the configuration strings.
        CharacterGenerator generator = CharacterGenerator.CreateWithConfig(character + "|" + config);

        // Give the particle system some time to grow.
        //yield WaitForSeconds(0);

        // Wait for the assets to be downloaded.
        while (!generator.ConfigReady) yield return 0;

        // Create the character.
        GameObject go = generator.Generate();

        // Attach the character to this prefab so we can move it around.
        go.transform.parent = transform;
        go.transform.localPosition = Vector3.zero;
        go.transform.localRotation = Quaternion.identity;

        // Stop emitting particles, as the character now exists.
        GetComponentInChildren<ParticleEmitter>().emit = false;

        //notify the Component handling animation that the character has been created
        SendMessage("InitAnimation");
	}
}
	

