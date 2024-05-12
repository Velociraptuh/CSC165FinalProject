package a2;

import java.awt.Color;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.UUID;
import java.util.Vector;
import org.joml.*;
import tage.physics.*;
import tage.physics.JBullet.*;
import tage.shapes.*;

import tage.*;

public class GhostManager
{
	private MyGame game;
	private Vector<GhostAvatar> ghostAvatars = new Vector<GhostAvatar>();

	public GhostManager(VariableFrameRateGame vfrg)
	{	game = (MyGame)vfrg;
	}
	
	public void createGhostAvatar(UUID id, Vector3f position) throws IOException
	{	System.out.println("adding ghost with ID --> " + id);
		AnimatedShape s = game.getGhostShape();
		TextureImage t = game.getGhostTexture();
		GhostAvatar newAvatar = new GhostAvatar(id, s, t, position);
		Matrix4f initialScale = (new Matrix4f()).scaling(1.0f);
		newAvatar.setLocalScale(initialScale);
		ghostAvatars.add(newAvatar);
		
		Light light = new Light();
		light.setType(Light.LightType.SPOTLIGHT);
		light.setLocation(newAvatar.getWorldLocation());
		newAvatar.setLight(light);
		light.setDirection(new Vector3f(0.0f, 0.0f, 0.4f));
		
		//Physics
		/*float[] vals = game.getVals();
		Engine engine = game.getEngine();
		float mass = 5.0f;
		float up[ ] = {0,1,0};
		float radius = 0.75f;
		float height = 2.75f;
		double[ ] tempTransform;
		
		Matrix4f translation = new Matrix4f(newAvatar.getLocalTranslation());
		tempTransform = game.toDoubleArray(translation.get(vals));

		PhysicsObject ghostCapsulePhys = (engine.getSceneGraph()).addPhysicsCylinder(mass, tempTransform, radius, height);
		ghostCapsulePhys.setBounciness(0.45f);
		newAvatar.setPhysicsObject(ghostCapsulePhys);*/
		
		//Animations
	}
	
	public void removeGhostAvatar(UUID id)
	{	GhostAvatar ghostAvatar = findAvatar(id);
		if(ghostAvatar != null)
		{	game.getEngine().getSceneGraph().removeGameObject(ghostAvatar);
			ghostAvatars.remove(ghostAvatar);
		}
		else
		{	System.out.println("tried to remove, but unable to find ghost in list");
		}
	}

	private GhostAvatar findAvatar(UUID id)
	{	GhostAvatar ghostAvatar;
		Iterator<GhostAvatar> it = ghostAvatars.iterator();
		while(it.hasNext())
		{	ghostAvatar = it.next();
			if(ghostAvatar.getID().compareTo(id) == 0)
			{	return ghostAvatar;
			}
		}		
		return null;
	}

	public Vector<GhostAvatar> getGhosts(){
		return ghostAvatars;
	}
	
	public void updateGhostAvatar(UUID id, Vector3f position)
	{	GhostAvatar ghostAvatar = findAvatar(id);
		if (ghostAvatar != null)
		{	
			//if(ghostAvatar.getPosition().x() == position.x() && ghostAvatar.getPosition().y() == position.y() && ghostAvatar.getPosition().z() == position.z()){
				//(ghostAvatar.getShape()).playAnimation();
			//}
			ghostAvatar.setPosition(position);
			//Physics
			//float[] vals = game.getVals();
			//double[] transform = game.toDoubleArray(ghostAvatar.getWorldTranslation().mul(ghostAvatar.getWorldRotation()).get(vals));
			//ghostAvatar.getPhysicsObject().setTransform(transform);
			
			//Light
			ghostAvatar.getLight().setLocation(ghostAvatar.getWorldLocation());
		}
		else
		{	System.out.println("tried to update ghost avatar position, but unable to find ghost in list");
		}
	}

	public void yawGhostAvatar(UUID id, float amount){
		GhostAvatar ghostAvatar = findAvatar(id);
		if(ghostAvatar != null){
			Matrix4f yRot = (new Matrix4f()).rotation((float)(org.joml.Math.toRadians(amount)), 0, 1, 0);
			Matrix4f currRoto = ghostAvatar.getLocalRotation();
			Matrix4f newRoto = yRot.mul(currRoto);
			ghostAvatar.setLocalRotation(newRoto);
			//float[] vals = game.getVals();
			//double[] transform = game.toDoubleArray(ghostAvatar.getWorldTranslation().mul(ghostAvatar.getWorldRotation()).get(vals));
			//ghostAvatar.getPhysicsObject().setTransform(transform);
			
			//Light
			Vector3f tempDir = ghostAvatar.getWorldForwardVector();
			ghostAvatar.getLight().setDirection(tempDir);
		}else{
			System.out.println("Could not update rotation");
		}
	}
	
	public void playGhostAnimation(UUID id, String name){
		GhostAvatar ghostAvatar = findAvatar(id);
		System.out.println("Ghost Animation Name: "+name);
		if(ghostAvatar != null){
			switch(name){
				case "run":
				System.out.println("Running");
					ghostAvatar.getAnimShape().playAnimation("RUN", 0.45f, AnimatedShape.EndType.LOOP, 0);
					break;
				case "punch": 
					ghostAvatar.getAnimShape().playAnimation("PUNCH", 0.3f, AnimatedShape.EndType.STOP, 0);
					break;
				case "stop":
					ghostAvatar.getAnimShape().stopAnimation();
					break;
			}
			
		}else{
			System.out.println("Could not play animation");
		}
	}
	
	public void resetFromPhysics(){
		GhostAvatar ghostAvatar;
		Iterator<GhostAvatar> it = ghostAvatars.iterator();
		while(it.hasNext()){	
			ghostAvatar = it.next();
			
			Matrix4f identityRotation = new Matrix4f().identity();
			float[] vals = game.getVals();
					
			//Keep ghost rotation but make it upright
			Vector3f eulerAngles = new Vector3f();
			(ghostAvatar.getWorldRotation()).getEulerAnglesXYZ(eulerAngles);
			float y = eulerAngles.y;
			
			//Reset Ghost Rotation
			identityRotation.rotation(y, 0.0f, 1.0f, 0.0f);
			ghostAvatar.setLocalRotation(identityRotation);
			
			//Reset Physics Object
			double[] transform = game.toDoubleArray(ghostAvatar.getWorldTranslation().mul(ghostAvatar.getWorldRotation()).get(vals));
			ghostAvatar.getPhysicsObject().setTransform(transform);
		}		
	}
	
	public void toggleFlashlight(UUID id, String name){
		GhostAvatar ghostAvatar = findAvatar(id);
		System.out.println("Toggling Ghost Light to " + name);
		if(ghostAvatar != null){
			switch(name){
				case "on":
					System.out.println("Ghost Light On" );
					ghostAvatar.getLight().setAmbient(0f, 0f, 0f);
					ghostAvatar.getLight().setDiffuse(0f, 0f, 0f);
					ghostAvatar.getLight().setSpecular(0f, 0f, 0f);
					break;
				case "off": 
					System.out.println("Ghost Light Off" );
					ghostAvatar.getLight().setAmbient(0.3f, 0.3f, 0.3f);
					ghostAvatar.getLight().setDiffuse(0.8f, 0.8f, 0.8f);
					ghostAvatar.getLight().setSpecular(1.0f, 1.0f, 1.0f);
					break;
			}
			
		}else{
			System.out.println("Could not Toggle Lights");
		}
	}
	
	public void changeModel(UUID id, String name){
		GhostAvatar ghostAvatar = findAvatar(id);
		Engine engine = game.getEngine();
		AnimatedShape s = game.getGhostShape();

		//Physics
		float[] vals = game.getVals();
		float mass = 5.0f;
		float up[ ] = {0,1,0};
		float radius = 0.75f;
		float height = 2.75f;
		double[ ] tempTransform;
		System.out.println("Changing Ghost Model");
		
		if(ghostAvatar != null){
			switch(name){
				case "red":
					System.out.println("Changing Ghost Model Red");
					
					Matrix4f trans = ghostAvatar.getWorldTranslation();
					Vector3f location = ghostAvatar.getWorldLocation();
					Matrix4f rotation = ghostAvatar.getWorldRotation();
					
					(engine.getSceneGraph()).removeGameObject(ghostAvatar);
					(engine.getSceneGraph()).removePhysicsObject(ghostAvatar.getPhysicsObject());
					
					ghostAvatars.remove(ghostAvatar);
					
					TextureImage t = game.getGhostTexture(1);
					GhostAvatar newAvatar = new GhostAvatar(id, s, t, location);
					Matrix4f initialScale = (new Matrix4f()).scaling(1.0f);
					newAvatar.setLocalScale(initialScale);
					newAvatar.setLocalTranslation(trans);
					newAvatar.setLocalRotation(rotation);
					ghostAvatars.add(newAvatar);
					
					Matrix4f translation = new Matrix4f(newAvatar.getLocalTranslation());
					tempTransform = game.toDoubleArray(translation.get(vals));

					PhysicsObject ghostCapsulePhys = (engine.getSceneGraph()).addPhysicsCylinder(mass, tempTransform, radius, height);
					ghostCapsulePhys.setBounciness(0.45f);
					newAvatar.setPhysicsObject(ghostCapsulePhys);
					
					break;
				case "blue": 
					System.out.println("Changing Ghost Model Blue");
					
					 trans = ghostAvatar.getWorldTranslation();
					 location = ghostAvatar.getWorldLocation();
					 rotation = ghostAvatar.getWorldRotation();
					
					(engine.getSceneGraph()).removeGameObject(ghostAvatar);
					(engine.getSceneGraph()).removePhysicsObject(ghostAvatar.getPhysicsObject());
					
					ghostAvatars.remove(ghostAvatar);
					
					 t = game.getGhostTexture(2);
					 newAvatar = new GhostAvatar(id, s, t, location);
					 initialScale = (new Matrix4f()).scaling(1.0f);
					newAvatar.setLocalScale(initialScale);
					newAvatar.setLocalTranslation(trans);
					newAvatar.setLocalRotation(rotation);
					ghostAvatars.add(newAvatar);
					
					 translation = new Matrix4f(newAvatar.getLocalTranslation());
					tempTransform = game.toDoubleArray(translation.get(vals));

					 ghostCapsulePhys = (engine.getSceneGraph()).addPhysicsCylinder(mass, tempTransform, radius, height);
					ghostCapsulePhys.setBounciness(0.45f);
					newAvatar.setPhysicsObject(ghostCapsulePhys);
					break;
				case "green": 
					System.out.println("Changing Ghost Model Green");
					
					 trans = ghostAvatar.getWorldTranslation();
					 location = ghostAvatar.getWorldLocation();
					 rotation = ghostAvatar.getWorldRotation();
					
					(engine.getSceneGraph()).removeGameObject(ghostAvatar);
					(engine.getSceneGraph()).removePhysicsObject(ghostAvatar.getPhysicsObject());
					
					ghostAvatars.remove(ghostAvatar);
					
					 t = game.getGhostTexture(3);
					 newAvatar = new GhostAvatar(id, s, t, location);
					 initialScale = (new Matrix4f()).scaling(1.0f);
					newAvatar.setLocalScale(initialScale);
					newAvatar.setLocalTranslation(trans);
					newAvatar.setLocalRotation(rotation);
					ghostAvatars.add(newAvatar);
					
					 translation = new Matrix4f(newAvatar.getLocalTranslation());
					tempTransform = game.toDoubleArray(translation.get(vals));

					 ghostCapsulePhys = (engine.getSceneGraph()).addPhysicsCylinder(mass, tempTransform, radius, height);
					ghostCapsulePhys.setBounciness(0.45f);
					newAvatar.setPhysicsObject(ghostCapsulePhys);
					break;
				case "tan": 
					System.out.println("Changing Ghost Model Tan");
					
					 trans = ghostAvatar.getWorldTranslation();
					 location = ghostAvatar.getWorldLocation();
					 rotation = ghostAvatar.getWorldRotation();
					
					(engine.getSceneGraph()).removeGameObject(ghostAvatar);
					(engine.getSceneGraph()).removePhysicsObject(ghostAvatar.getPhysicsObject());
					
					ghostAvatars.remove(ghostAvatar);
					
					 t = game.getGhostTexture(4);
					 newAvatar = new GhostAvatar(id, s, t, location);
					 initialScale = (new Matrix4f()).scaling(1.0f);
					newAvatar.setLocalScale(initialScale);
					newAvatar.setLocalTranslation(trans);
					newAvatar.setLocalRotation(rotation);
					ghostAvatars.add(newAvatar);
					
					 translation = new Matrix4f(newAvatar.getLocalTranslation());
					tempTransform = game.toDoubleArray(translation.get(vals));

					 ghostCapsulePhys = (engine.getSceneGraph()).addPhysicsCylinder(mass, tempTransform, radius, height);
					ghostCapsulePhys.setBounciness(0.45f);
					newAvatar.setPhysicsObject(ghostCapsulePhys);
					break;
				case "cyan": 
					System.out.println("Changing Ghost Model Cyan");
					
					 trans = ghostAvatar.getWorldTranslation();
					 location = ghostAvatar.getWorldLocation();
					 rotation = ghostAvatar.getWorldRotation();
					
					(engine.getSceneGraph()).removeGameObject(ghostAvatar);
					(engine.getSceneGraph()).removePhysicsObject(ghostAvatar.getPhysicsObject());
					
					ghostAvatars.remove(ghostAvatar);
					
					 t = game.getGhostTexture(5);
					 newAvatar = new GhostAvatar(id, s, t, location);
					 initialScale = (new Matrix4f()).scaling(1.0f);
					newAvatar.setLocalScale(initialScale);
					newAvatar.setLocalTranslation(trans);
					newAvatar.setLocalRotation(rotation);
					ghostAvatars.add(newAvatar);
					
					 translation = new Matrix4f(newAvatar.getLocalTranslation());
					tempTransform = game.toDoubleArray(translation.get(vals));

					 ghostCapsulePhys = (engine.getSceneGraph()).addPhysicsCylinder(mass, tempTransform, radius, height);
					ghostCapsulePhys.setBounciness(0.45f);
					newAvatar.setPhysicsObject(ghostCapsulePhys);
					break;
			}
			
		}
	}
}