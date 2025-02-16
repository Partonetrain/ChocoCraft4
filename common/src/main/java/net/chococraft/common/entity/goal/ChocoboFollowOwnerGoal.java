package net.chococraft.common.entity.goal;

import net.chococraft.common.entity.AbstractChocobo;
import net.chococraft.common.entity.properties.MovementType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;

public class ChocoboFollowOwnerGoal extends FollowOwnerGoal {
	private final AbstractChocobo chocobo;

	public ChocoboFollowOwnerGoal(AbstractChocobo chocobo, double speedModifier, float startDistance, float stopDistance) {
		super(chocobo, speedModifier, startDistance, stopDistance);
		this.chocobo = chocobo;
	}

	public boolean canUse() {
		LivingEntity livingentity = this.chocobo.getOwner();
		if (livingentity == null) {
			return false;
		} else if (livingentity.isSpectator()) {
			return false;
		} else if (this.chocobo.getMovementType() != MovementType.FOLLOW_OWNER) {
			return false;
		} else if (this.chocobo.distanceToSqr(livingentity) < (double) (this.startDistance * this.startDistance)) {
			return false;
		} else {
			this.owner = livingentity;
			return true;
		}
	}

	public boolean canContinueToUse() {
		if (this.navigation.isDone()) {
			return false;
		} else if (this.chocobo.isOrderedToSit()) {
			return false;
		} else {
			return !(this.chocobo.distanceToSqr(this.owner) <= (double) (this.stopDistance * this.stopDistance)) && this.chocobo.getMovementType() == MovementType.FOLLOW_OWNER;
		}
	}
}
