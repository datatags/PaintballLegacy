package de.blablubbabc.paintball.extras.weapons;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import de.blablubbabc.paintball.Paintball;

public class NoGravityHandler {
	private Map<Entity, NoGravityState> entities = new HashMap<Entity, NoGravityState>();
	private int taskID = -1;

	public NoGravityHandler() {
		
	}
	
	public void addEntity(Entity entity, Vector velocity, int duration) {
		entities.put(entity, new NoGravityState(velocity, duration));
		// start task if needed:
		if (taskID == -1) {
			run();
		}
	}

	public void removeEntity(Entity entity) {
		entities.remove(entity);
	}

	public boolean containsEntity(Entity entity) {
		return entity == null || entities.get(entity) == null;
	}

	public void run() {
		taskID = Paintball.instance.getServer().getScheduler().runTaskTimer(Paintball.instance, new Runnable() {
			
			@Override
			public void run() {
				if(!entities.isEmpty()) {
					Iterator<Entry<Entity, NoGravityState>> iterator = entities.entrySet().iterator();
					while (iterator.hasNext()) {
						Entry<Entity, NoGravityState> entry = iterator.next();
						Entity entity = entry.getKey();
						NoGravityState state = entry.getValue();
						int duration = state.getDuration();
						
						if (!entity.isValid() || duration <= 0) {
							iterator.remove();
							// stop task if no longer needed:
							if (entities.isEmpty()) {
								Paintball.instance.getServer().getScheduler().cancelTask(taskID);
								taskID = -1;
							}
						} else {
							entity.setVelocity(state.getVelocity());
							state.setDuration(--duration);
						}
					}
				}
			}
		}, 20L, 1L).getTaskId();
	}
	
	private class NoGravityState {
		private final Vector velocity;
		private int duration;
		
		private NoGravityState(Vector velocity, int duration) {
			this.velocity = velocity;
			this.duration = duration;
		}
		
		private Vector getVelocity() {
			return velocity;
		}
		
		private int getDuration() {
			return duration;
		}
		
		private void setDuration(int duration) {
			this.duration = duration;
		}
	}
}
