package net.chococraft.common.entity;

import net.chococraft.Chococraft;
import net.chococraft.ChococraftExpectPlatform;
import net.chococraft.common.entity.breeding.BreedingHelper;
import net.chococraft.common.entity.breeding.ChocoboAbilityInfo;
import net.chococraft.common.entity.goal.ChocoboFollowOwnerGoal;
import net.chococraft.common.entity.goal.ChocoboHealInPenGoal;
import net.chococraft.common.entity.properties.ChocoboColor;
import net.chococraft.common.entity.properties.MovementType;
import net.chococraft.common.items.ChocoboSaddleItem;
import net.chococraft.common.items.armor.AbstractChocoDisguiseItem;
import net.chococraft.registry.ModEntities;
import net.chococraft.registry.ModRegistry;
import net.chococraft.registry.ModSounds;
import net.chococraft.utils.RandomHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractChocobo extends TamableAnimal implements HasCustomInventoryScreen {
	private static final ResourceLocation STEP_HEIGHT_ID = ResourceLocation.fromNamespaceAndPath(Chococraft.MOD_ID, "step_height");

	private static final String NBTKEY_CHOCOBO_COLOR = "Color";
	private static final String NBTKEY_CHOCOBO_IS_MALE = "Male";
	private static final String NBTKEY_MOVEMENTTYPE = "MovementType";
	protected static final String NBTKEY_SADDLE_ITEM = "Saddle";
	protected static final String NBTKEY_INVENTORY = "Inventory";
	private static final String NBTKEY_CHOCOBO_GENERATION = "Generation";
	private static final String NBTKEY_ALLOWED_FLIGHT = "AllowedFlight";

	private static final EntityDataAccessor<ChocoboColor> PARAM_COLOR = SynchedEntityData.defineId(AbstractChocobo.class, ChococraftExpectPlatform.getColorSerializer());
	private static final EntityDataAccessor<Boolean> PARAM_IS_MALE = SynchedEntityData.defineId(AbstractChocobo.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> PARAM_FED_GOLD_GYSAHL = SynchedEntityData.defineId(AbstractChocobo.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<MovementType> PARAM_MOVEMENT_TYPE = SynchedEntityData.defineId(AbstractChocobo.class, ChococraftExpectPlatform.getMovementSerializer());
	private static final EntityDataAccessor<ItemStack> PARAM_SADDLE_ITEM = SynchedEntityData.defineId(AbstractChocobo.class, EntityDataSerializers.ITEM_STACK);
	private static final EntityDataAccessor<Boolean> ALLOWED_FLIGHT = SynchedEntityData.defineId(AbstractChocobo.class, EntityDataSerializers.BOOLEAN);

	private final static EntityDataAccessor<Integer> PARAM_GENERATION = SynchedEntityData.defineId(AbstractChocobo.class, EntityDataSerializers.INT);

	private AvoidEntityGoal<Player> chocoboAvoidPlayerGoal;
	private ChocoboHealInPenGoal healInPenGoal;

	private float wingRotation;
	private float destPos;
	private float wingRotDelta;

	public int timeSinceFeatherChance = 0;

	public AbstractChocobo(EntityType<? extends AbstractChocobo> type, Level world) {
		super(type, world);
		timeSinceFeatherChance = 0;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(3, new BreedGoal(this, 1.0D));
		this.goalSelector.addGoal(4, new ChocoboFollowOwnerGoal(this, 1.0D, 5.0F, 5.0F));
		this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 5.0F));
		this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
	}

	private final ChocoboFollowOwnerGoal follow = new ChocoboFollowOwnerGoal(this, 2.0D, 3.0F, 10.0F);

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 20 / 100f).add(Attributes.FLYING_SPEED, 0.0F).add(Attributes.MAX_HEALTH, 30);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(PARAM_COLOR, ChocoboColor.YELLOW);
		builder.define(PARAM_IS_MALE, false);
		builder.define(PARAM_FED_GOLD_GYSAHL, false);
		builder.define(PARAM_MOVEMENT_TYPE, MovementType.WANDER);
		builder.define(PARAM_SADDLE_ITEM, ItemStack.EMPTY);
		builder.define(PARAM_GENERATION, 0);
		builder.define(ALLOWED_FLIGHT, true);
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyIn, MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn) {
		if (levelAccessor.getBiome((new BlockPos(blockPosition().below()))).is(BiomeTags.IS_NETHER)) {
			this.setChocoboColor(ChocoboColor.FLAME);
		}
		this.finalizeChocobo(this);
		return super.finalizeSpawn(levelAccessor, difficultyIn, reason, spawnDataIn);
	}

	private void finalizeChocobo(AbstractChocobo chocobo) {
		chocobo.setMale(random.nextBoolean());
		chocobo.getAttribute(Attributes.MAX_HEALTH).setBaseValue(getChocoboColor().getAbilityInfo().getMaxHP());
		chocobo.setHealth(getMaxHealth());
		chocobo.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(getChocoboColor().getAbilityInfo().getLandSpeed() / 100F);
		chocobo.getAttribute(Attributes.FLYING_SPEED).setBaseValue(getChocoboColor().getAbilityInfo().getAirbornSpeed() / 100F);
		chocobo.setAllowedFlight(ChococraftExpectPlatform.canChocobosFly());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		this.setChocoboColor(ChocoboColor.values()[compound.getByte(NBTKEY_CHOCOBO_COLOR)]);
		this.setMale(compound.getBoolean(NBTKEY_CHOCOBO_IS_MALE));
		this.setMovementType(MovementType.values()[compound.getByte(NBTKEY_MOVEMENTTYPE)]);

		this.setGeneration(compound.getInt(NBTKEY_CHOCOBO_GENERATION));
		if (compound.contains("wornSaddle", 10))
			this.setSaddleType(ItemStack.parseOptional(this.registryAccess(), compound.getCompound("wornSaddle")));

		this.setAllowedFlight(compound.getBoolean(NBTKEY_ALLOWED_FLIGHT));
		this.reassessTameGoals();
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putByte(NBTKEY_CHOCOBO_COLOR, (byte) this.getChocoboColor().ordinal());
		compound.putBoolean(NBTKEY_CHOCOBO_IS_MALE, this.isMale());
		compound.putByte(NBTKEY_MOVEMENTTYPE, (byte) this.getMovementType().ordinal());

		compound.putInt(NBTKEY_CHOCOBO_GENERATION, this.getGeneration());
		if (!getSaddle().isEmpty())
			compound.put("wornSaddle", getSaddle().save(this.registryAccess(), new CompoundTag()));

		compound.putBoolean(NBTKEY_ALLOWED_FLIGHT, this.allowedFlight());
	}

	public ChocoboColor getChocoboColor() {
		return this.entityData.get(PARAM_COLOR);
	}

	public void setChocoboColor(ChocoboColor color) {
		this.entityData.set(PARAM_COLOR, color);
	}

	@Override
	public boolean fireImmune() {
		return getChocoboColor().getAbilityInfo().isImmuneToFire();
	}

	public boolean isMale() {
		return this.entityData.get(PARAM_IS_MALE);
	}

	public void setMale(boolean isMale) {
		this.entityData.set(PARAM_IS_MALE, isMale);
	}

	public boolean isFedGoldGysahl() {
		return this.entityData.get(PARAM_FED_GOLD_GYSAHL);
	}

	public void setFedGoldGysahl(boolean value) {
		this.entityData.set(PARAM_FED_GOLD_GYSAHL, value);
	}

	public MovementType getMovementType() {
		return this.entityData.get(PARAM_MOVEMENT_TYPE);
	}

	public void setMovementType(MovementType type) {
		this.entityData.set(PARAM_MOVEMENT_TYPE, type);
	}

	public boolean isSaddled() {
		return !this.getSaddle().isEmpty();
	}

	public ItemStack getSaddle() {
		return this.entityData.get(PARAM_SADDLE_ITEM);
	}

	protected void setSaddleType(ItemStack saddleStack) {
		ItemStack newStack = saddleStack;
		ItemStack oldStack = getSaddle();
		if (oldStack.getItem() != newStack.getItem()) {
			this.entityData.set(PARAM_SADDLE_ITEM, newStack.copy());
			this.reconfigureInventory(oldStack, newStack);
		}
	}

	public int getGeneration() {
		return this.entityData.get(PARAM_GENERATION);
	}

	public void setGeneration(int value) {
		this.entityData.set(PARAM_GENERATION, value);
	}

	public boolean allowedFlight() {
		return this.entityData.get(ALLOWED_FLIGHT);
	}

	public void setAllowedFlight(boolean value) {
		this.entityData.set(ALLOWED_FLIGHT, value);
	}
	//endregion

	@Override
	protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions entityDimensions, float f) {
		return new Vec3(0.0F, 1.65F, 0.0F);
	}

	@Nullable
	public LivingEntity getControllingPassenger() {
		Entity entity = this.getFirstPassenger();
		if (entity instanceof LivingEntity livingEntity) {
			return this.canBeControlledBy(livingEntity) ? livingEntity : null;
		}
		return null;
	}

	private boolean canBeControlledBy(LivingEntity entity) {
		return this.isTame() && this.isSaddled();
	}

	@Override
	protected boolean updateInWaterStateAndDoFluidPushing() {
		return super.updateInWaterStateAndDoFluidPushing();
	}

	@Override
	public boolean canStandOnFluid(FluidState fluidState) {
		boolean flag = this.getControllingPassenger() != null && this.getControllingPassenger().jumping;
		return fluidState.is(FluidTags.WATER) && this.getAbilityInfo().canWalkOnWater() && !flag;
	}

	@Override
	public void travel(Vec3 travelVector) {
		if (this.isAlive()) {
			LivingEntity livingentity = this.getControllingPassenger();
			if (this.isVehicle() && livingentity != null) {
				this.setYRot(livingentity.getYRot());
				this.yRotO = this.getYRot();
				this.setXRot(livingentity.getXRot() * 0.5F);
				this.setRot(this.getYRot(), this.getXRot());
				this.yBodyRot = this.getYRot();
				this.yHeadRot = this.yBodyRot;
				float strafe = livingentity.xxa * 0.5F;
				float forward = livingentity.zza;
				if (forward <= 0.0F) {
					forward *= 0.25F;
				}

				if (livingentity.jumping && (this.getAbilityInfo().getCanFly() && allowedFlight())) {
					setJumping(true);
					this.jumpFromGround();
					this.hasImpulse = true;
					this.moveRelative(getChocoboColor().getAbilityInfo().getAirbornSpeed() / 100, travelVector);
				} else if (livingentity.jumping && !this.jumping) {
					if (isInWater()) {
						if (this.getAbilityInfo().canWalkOnWater()) {
							if (isUnderWater()) {
								jumpInLiquid(FluidTags.WATER);
							}
						} else {
							this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.01f + random.nextFloat() * 0.09f, 0.0));
						}
					} else {
						if (this.isInWater() && this.getAbilityInfo().canWalkOnWater()) {
							goDownInWater();
						} else if (onGround()) {
							jumpFromGround();
							livingentity.setJumping(false);
							this.setJumping(true);
						}
					}
				}

				if (this.isControlledByLocalInstance()) {
					this.setSpeed((float) this.getAttributeValue(Attributes.MOVEMENT_SPEED));
					super.travel(new Vec3((double) strafe, travelVector.y, (double) forward));
				} else if (livingentity instanceof Player) {
					this.setDeltaMovement(Vec3.ZERO);
				}

				if (this.onGround()) {
					this.setJumping(false);
				}

				this.calculateEntityAnimation(false);
				this.tryCheckInsideBlocks();
			} else {
				super.travel(travelVector);
			}
		}
	}

	protected ChocoboAbilityInfo getAbilityInfo() {
		return getChocoboColor().getAbilityInfo();
	}

	@Override
	protected void positionRider(Entity passenger, MoveFunction moveFunction) {
		super.positionRider(passenger, moveFunction);
		if (passenger instanceof Mob && this.getControllingPassenger() == passenger) {
			this.yBodyRot = ((LivingEntity) passenger).yBodyRot;
		}
	}

	@Nullable
	@Override
	public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
		AbstractChocobo babyChocobo = ModEntities.CHOCOBO.get().create(level);
		babyChocobo.setChocoboColor(BreedingHelper.getColor(this, (AbstractChocobo) partner));
		this.finalizeChocobo(babyChocobo);
		//Reset golden status
		this.setFedGoldGysahl(false);
		((AbstractChocobo) partner).setFedGoldGysahl(false);
		return babyChocobo;
	}

	@Override
	public void spawnChildFromBreeding(ServerLevel level, Animal partner) {
		if (partner instanceof AbstractChocobo target && this.getChocoboColor() == ChocoboColor.PURPLE && target.getChocoboColor() == ChocoboColor.PURPLE) {
			this.spawnAtLocation(new ItemStack(ModRegistry.PURPLE_CHOCOBO_SPAWN_EGG.get()), 0);
			this.setAge(6000);
			target.setAge(6000);
			this.resetLove();
			target.resetLove();
		} else {
			super.spawnChildFromBreeding(level, partner);
		}
	}

	@Override
	public boolean canMate(Animal parent) {
		if (parent == this || !(parent instanceof AbstractChocobo otherChocobo)) return false;
		if (!this.isInLove() || !parent.isInLove()) return false;
		if (ChococraftExpectPlatform.genderless()) return otherChocobo.isMale() != this.isMale();
		else return true;
	}

	public void dropFeather() {
		if (this.getCommandSenderWorld().isClientSide) return;

		if (this.isBaby()) return;

		this.spawnAtLocation(new ItemStack(ModRegistry.CHOCOBO_FEATHER.get(), 1), 0.0F);
	}

	@Override
	protected boolean canRide(Entity entityIn) {
		return !this.getSaddle().isEmpty() && super.canRide(entityIn);
	}

	@Override
	public void aiStep() {
		super.aiStep();

		this.setRot(this.getYRot(), this.getXRot());

		resetFallDistance();

		if (this.timeSinceFeatherChance == 3000) {
			this.timeSinceFeatherChance = 0;

			if ((float) Math.random() < .25) {
				this.dropFeather();
			}
		} else {
			this.timeSinceFeatherChance++;
		}

		if (!this.level().isClientSide) {
			if (isPassenger() && isVehicle()) {
				stopRiding();
			}
		}

		if (this.getCommandSenderWorld().isClientSide) {
			// Wing rotations, control packet, client side
			// Client side
			this.destPos += (float) ((double) (this.onGround() ? -1 : 4) * 0.3D);
			this.destPos = Mth.clamp(destPos, 0f, 1f);

			if (!this.onGround()) this.wingRotDelta = Math.min(wingRotation, 1f);
			this.wingRotDelta *= 0.9F;
			this.wingRotation += this.wingRotDelta * 2.0F;

			if (this.onGround()) {
				this.calculateEntityAnimation(false);
			} else {
				this.walkAnimation.position(0);
				this.walkAnimation.setSpeed(0);
			}
		}
	}

	@Override
	public void tick() {
		super.tick();

		if (isVehicle() && getFirstPassenger() instanceof LivingEntity passenger) {
			getAbilityInfo().getRiderAbilities().forEach(ability -> {
				passenger.addEffect(ability.get());
				this.addEffect(ability.get());
			});
		}
		this.updateStepHeight();
	}

	private void updateStepHeight() {
		AttributeInstance attributeinstance = this.getAttribute(Attributes.STEP_HEIGHT);
		float stepHeight = getAbilityInfo().getStepHeight(true);
		if (attributeinstance != null) {
			attributeinstance.addOrUpdateTransientModifier(
					new AttributeModifier(
							STEP_HEIGHT_ID, stepHeight, AttributeModifier.Operation.ADD_VALUE
					)
			);
		}
	}

	@Override
	public boolean isFood(ItemStack stack) {
		return stack.is(ModRegistry.LOVERLY_GYSAHL_GREEN.get()) || stack.is(ModRegistry.GOLD_GYSAHL.get()) || stack.is(ModRegistry.GYSAHL_CAKE.get());
	}

	public boolean isInteractionItem(ItemStack stack) {
		return isFood(stack) || stack.getItem() instanceof ChocoboSaddleItem || stack.is(ModRegistry.CHOCOBO_WHISTLE.get());
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		ItemStack heldItemStack = player.getItemInHand(hand);
		if (this.isFood(heldItemStack)) {
			boolean fedCake = heldItemStack.getItem() == ModRegistry.GYSAHL_CAKE.get();
			//Allow the Chocobo to breed by feeding Loverly or Gold Gysahl
			if (this.isBaby()) {
				if (fedCake) {
					this.usePlayerItem(player, hand, heldItemStack);
					this.setBaby(false);
					return InteractionResult.sidedSuccess(this.level().isClientSide);
				}
			} else {
				int i = this.getAge();
				if (!this.level().isClientSide && i == 0 && this.canFallInLove() && !fedCake) {
					if (heldItemStack.getItem() == ModRegistry.GOLD_GYSAHL.get()) {
						//If fed a Gold Gysahl set the "fedGoldGysahl" flag to true
						this.setFedGoldGysahl(true);
					}
					this.usePlayerItem(player, hand, heldItemStack);
					this.setInLove(player);
					return InteractionResult.SUCCESS;
				}
			}

			if (this.level().isClientSide) {
				return InteractionResult.CONSUME;
			}
		}

		if (this.level().isClientSide) {
			return InteractionResult.PASS;
		} else {
			if (this.isTame()) {
				//Mount the chocobo
				boolean emptyHand = !ChococraftExpectPlatform.requireEmptyHand() && !isInteractionItem(player.getMainHandItem()) || player.getMainHandItem().isEmpty();
				if (this.isSaddled() && !this.isVehicle() && emptyHand && !player.isShiftKeyDown() && !this.isBaby()) {
					player.startRiding(this);
					return InteractionResult.SUCCESS;
				}

				//Open chocobo's inventory
				if (player.isShiftKeyDown() && !this.isBaby() && emptyHand) {
					this.openCustomInventoryScreen((ServerPlayer) player);
					return InteractionResult.SUCCESS;
				}

				//Switch between the Chocobo following, wandering or staying using the Chocobo Whistle
				if (heldItemStack.is(ModRegistry.CHOCOBO_WHISTLE.get()) && !this.isBaby()) {
					if (isOwnedBy(player)) {
						if (getMovementType() == MovementType.STANDSTILL) {
							this.playSound(ModSounds.WHISTLE_SOUND_FOLLOW.get(), 1.0F, 1.0F);
							this.setNoAi(false);
							this.goalSelector.addGoal(0, this.follow);
							this.setMovementType(MovementType.FOLLOW_OWNER);
							player.displayClientMessage(Component.translatable(Chococraft.MOD_ID + ".entity_chocobo.chocobo_followcmd"), true);
						} else if (this.getMovementType() == MovementType.FOLLOW_OWNER) {
							this.playSound(ModSounds.WHISTLE_SOUND_WANDER.get(), 1.0F, 1.0F);
							this.goalSelector.removeGoal(this.follow);
							this.setMovementType(MovementType.WANDER);
							player.displayClientMessage(Component.translatable(Chococraft.MOD_ID + ".entity_chocobo.chocobo_wandercmd"), true);
						} else if (this.getMovementType() == MovementType.WANDER) {
							this.playSound(ModSounds.WHISTLE_SOUND_STAY.get(), 1.0F, 1.0F);
							this.setNoAi(true);
							this.setMovementType(MovementType.STANDSTILL);
							player.displayClientMessage(Component.translatable(Chococraft.MOD_ID + ".entity_chocobo.chocobo_staycmd"), true);
						}
					} else {
						player.displayClientMessage(Component.translatable(Chococraft.MOD_ID + ".entity_chocobo.not_owner"), true);
					}
					return InteractionResult.SUCCESS;
				}


				//Heal the Chocobo if fed with Gysahl Green after being tamed and not at max health
				if (heldItemStack.is(ModRegistry.GYSAHL_GREEN_ITEM.get())) {
					if (getHealth() != getMaxHealth()) {
						this.usePlayerItem(player, hand, heldItemStack);
						this.heal(5);
						this.gameEvent(GameEvent.EAT, this);
						return InteractionResult.SUCCESS;
					} else {
						player.displayClientMessage(Component.translatable(Chococraft.MOD_ID + ".entity_chocobo.heal_fail"), true);
						return InteractionResult.PASS;
					}
				}

				//Turn Gold Chocobo red or pink depending on the gysahl fed
				if (getChocoboColor() == ChocoboColor.GOLD) {
					if (heldItemStack.getItem() == ModRegistry.RED_GYSAHL.get()) {
						this.usePlayerItem(player, hand, heldItemStack);
						this.setChocoboColor(ChocoboColor.RED);
						return InteractionResult.SUCCESS;
					} else if (heldItemStack.getItem() == ModRegistry.PINK_GYSAHL.get()) {
						this.usePlayerItem(player, hand, heldItemStack);
						this.setChocoboColor(ChocoboColor.PINK);
						return InteractionResult.SUCCESS;
					}
				}

				//Saddle the Chocobo if right-clicked with a saddle
				if (heldItemStack.getItem() instanceof ChocoboSaddleItem && !this.isSaddled() && !this.isBaby()) {
					setSaddled(player, hand, new ItemStack(heldItemStack.getItem()));
					if (!player.getAbilities().instabuild) {
						heldItemStack.shrink(1);
					}
					return InteractionResult.SUCCESS;
				}
			} else {
				//Chance of taming Chocobo if right-clicked with Gysahl Green
				if (heldItemStack.is(ModRegistry.GYSAHL_GREEN_ITEM.get())) {
					this.usePlayerItem(player, hand, heldItemStack);
					if ((float) Math.random() < ChococraftExpectPlatform.getTameChance()) {
						this.setOwnerUUID(player.getUUID());
						this.setTame(true, false);
						if (ChococraftExpectPlatform.nameTamedChocobos()) {
							if (!hasCustomName()) {
								setCustomName(DefaultNames.getRandomName(random, isMale()));
							}
						}
						player.displayClientMessage(Component.translatable(Chococraft.MOD_ID + ".entity_chocobo.tame_success"), true);
					} else {
						player.displayClientMessage(Component.translatable(Chococraft.MOD_ID + ".entity_chocobo.tame_fail"), true);
					}
					return InteractionResult.SUCCESS;
				}
			}
		}
		return InteractionResult.PASS;
	}

	protected abstract void setSaddled(Player player, InteractionHand hand, ItemStack heldItemStack);

	protected abstract void reconfigureInventory(ItemStack oldSaddle, ItemStack newSaddle);

	@Override
	protected void dropFromLootTable(DamageSource damageSourceIn, boolean attackedRecently) {
		super.dropFromLootTable(damageSourceIn, attackedRecently);

		dropInventory();
	}

	protected abstract void dropInventory();

	protected SoundEvent getAmbientSound() {
		return ModSounds.AMBIENT_SOUND.get();
	}

	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return ModSounds.AMBIENT_SOUND.get();
	}

	protected SoundEvent getDeathSound() {
		return ModSounds.AMBIENT_SOUND.get();
	}

	@Override
	protected float getSoundVolume() {
		return 0.6F;
	}

	@Override
	public int getAmbientSoundInterval() {
		return (24 * (int) (Math.random() * ChococraftExpectPlatform.kwehIntervalLimit()));
	}

	@Override
	public float getWalkTargetValue(BlockPos pos, LevelReader levelReader) {
		if (this.level().getBiome((new BlockPos(blockPosition()))).is(BiomeTags.IS_NETHER))
			return 0.0F;

		return super.getWalkTargetValue(pos, levelReader);
	}

	@Override
	public void setTame(boolean tame, boolean applyTamingSideEffects) {
		super.setTame(tame, applyTamingSideEffects);
		this.reassessTameGoals();
	}

	protected void reassessTameGoals() {
		if (chocoboAvoidPlayerGoal == null) {
			chocoboAvoidPlayerGoal = new AvoidEntityGoal<>(this, Player.class, 10.0F, 1.0D, 1.2D, livingEntity -> {
				if (livingEntity instanceof Player player) {
					int chance = 0;
					for (ItemStack stack : player.getInventory().armor) {
						if (stack != null) {
							if (stack.getItem() instanceof AbstractChocoDisguiseItem) chance += 25;
						}
					}

					return !RandomHelper.getChanceResult(getRandom(), chance);
				}
				return false;
			});
		}
		if (healInPenGoal == null) {
			healInPenGoal = new ChocoboHealInPenGoal(this);
		}

		goalSelector.removeGoal(chocoboAvoidPlayerGoal);
		goalSelector.removeGoal(healInPenGoal);

		if (isTame()) {
			goalSelector.addGoal(4, healInPenGoal);
		} else {
			goalSelector.addGoal(5, chocoboAvoidPlayerGoal);
		}
	}

	public static boolean checkChocoboSpawnRules(EntityType<? extends AbstractChocobo> entityType, LevelAccessor levelAccessor,
	                                             MobSpawnType spawnType, BlockPos pos, RandomSource randomSource) {
		if (levelAccessor.getBiome(new BlockPos(pos)).is(BiomeTags.IS_NETHER)) {
			BlockPos blockpos = pos.below();
			return spawnType == MobSpawnType.SPAWNER || levelAccessor.getBlockState(blockpos).isValidSpawn(levelAccessor, blockpos, entityType);
		}

		return levelAccessor.getBlockState(pos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON) && isBrightEnoughToSpawn(levelAccessor, pos);
	}
}
