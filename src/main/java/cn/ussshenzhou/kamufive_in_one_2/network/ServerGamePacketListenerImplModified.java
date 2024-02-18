package cn.ussshenzhou.kamufive_in_one_2.network;

import com.google.common.collect.Lists;
import com.google.common.primitives.Floats;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.*;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.*;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.TextFilter;
import net.minecraft.util.Mth;
import net.minecraft.util.SignatureValidator;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.net.SocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author USS_Shenzhou
 */
@ParametersAreNonnullByDefault
public class ServerGamePacketListenerImplModified extends ServerGamePacketListenerImpl {
    public ServerPlayer mainPlayer;

    public ServerGamePacketListenerImplModified(MinecraftServer pServer, Connection pConnection, ServerPlayer pPlayer) {
        super(pServer, pConnection, pPlayer);
    }

    public static ServerGamePacketListenerImplModified from(ServerGamePacketListenerImpl old, ServerPlayer mainPlayer) {
        var thiz = new ServerGamePacketListenerImplModified(old.player.server, old.connection, old.player);
        thiz.mainPlayer = mainPlayer;
        return thiz;
    }

    @Override
    public void tick() {
        if (this.ackBlockChangesUpTo > -1) {
            this.send(new ClientboundBlockChangedAckPacket(this.ackBlockChangesUpTo));
            this.ackBlockChangesUpTo = -1;
        }

        this.resetPosition();
        this.player.xo = this.player.getX();
        this.player.yo = this.player.getY();
        this.player.zo = this.player.getZ();
        this.player.doTick();
        this.player.absMoveTo(this.firstGoodX, this.firstGoodY, this.firstGoodZ, this.player.getYRot(), this.player.getXRot());
        ++this.tickCount;
        this.knownMovePacketCount = this.receivedMovePacketCount;
        if (this.clientIsFloating && !this.player.isSleeping() && !this.player.isPassenger() && !this.player.isDeadOrDying()) {
            if (++this.aboveGroundTickCount > 80) {
                LOGGER.warn("{} was kicked for floating too long!", this.player.getName().getString());
                this.disconnect(Component.translatable("multiplayer.disconnect.flying"));
                return;
            }
        } else {
            this.clientIsFloating = false;
            this.aboveGroundTickCount = 0;
        }

        this.lastVehicle = this.player.getRootVehicle();
        if (this.lastVehicle != this.player && this.lastVehicle.getControllingPassenger() == this.player) {
            this.vehicleFirstGoodX = this.lastVehicle.getX();
            this.vehicleFirstGoodY = this.lastVehicle.getY();
            this.vehicleFirstGoodZ = this.lastVehicle.getZ();
            this.vehicleLastGoodX = this.lastVehicle.getX();
            this.vehicleLastGoodY = this.lastVehicle.getY();
            this.vehicleLastGoodZ = this.lastVehicle.getZ();
            if (this.clientVehicleIsFloating && this.player.getRootVehicle().getControllingPassenger() == this.player) {
                if (++this.aboveGroundVehicleTickCount > 80) {
                    LOGGER.warn("{} was kicked for floating a vehicle too long!", this.player.getName().getString());
                    this.disconnect(Component.translatable("multiplayer.disconnect.flying"));
                    return;
                }
            } else {
                this.clientVehicleIsFloating = false;
                this.aboveGroundVehicleTickCount = 0;
            }
        } else {
            this.lastVehicle = null;
            this.clientVehicleIsFloating = false;
            this.aboveGroundVehicleTickCount = 0;
        }

        this.server.getProfiler().push("keepAlive");
        long i = Util.getMillis();
        if (i - this.keepAliveTime >= 15000L) {
            if (this.keepAlivePending) {
                this.disconnect(Component.translatable("disconnect.timeout"));
            } else {
                this.keepAlivePending = true;
                this.keepAliveTime = i;
                this.keepAliveChallenge = i;
                this.send(new ClientboundKeepAlivePacket(this.keepAliveChallenge));
            }
        }

        this.server.getProfiler().pop();
        if (this.chatSpamTickCount > 0) {
            --this.chatSpamTickCount;
        }

        if (this.dropSpamTickCount > 0) {
            --this.dropSpamTickCount;
        }

        if (this.player.getLastActionTime() > 0L && this.server.getPlayerIdleTimeout() > 0 && Util.getMillis() - this.player.getLastActionTime() > (long)this.server.getPlayerIdleTimeout() * 1000L * 60L) {
            this.disconnect(Component.translatable("multiplayer.disconnect.idling"));
        }

    }

    @Override
    public void resetPosition() {
        this.firstGoodX = this.player.getX();
        this.firstGoodY = this.player.getY();
        this.firstGoodZ = this.player.getZ();
        this.lastGoodX = this.player.getX();
        this.lastGoodY = this.player.getY();
        this.lastGoodZ = this.player.getZ();
    }

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }

    private boolean isSingleplayerOwner() {
        return this.server.isSingleplayerOwner(this.player.getGameProfile());
    }

    /**
     * Disconnect the player with a specified reason
     */
    @Override
    public void disconnect(Component pTextComponent) {
        this.connection.send(new ClientboundDisconnectPacket(pTextComponent), PacketSendListener.thenRun(() -> {
            this.connection.disconnect(pTextComponent);
        }));
        this.connection.setReadOnly();
        this.server.executeBlocking(this.connection::handleDisconnection);
    }

    private <T, R> CompletableFuture<R> filterTextPacket(T pMessage, BiFunction<TextFilter, T, CompletableFuture<R>> pProcessor) {
        return pProcessor.apply(this.player.getTextFilter(), pMessage).thenApply((p_264862_) -> {
            if (!this.isAcceptingMessages()) {
                LOGGER.debug("Ignoring packet due to disconnection");
                throw new CancellationException("disconnected");
            } else {
                return p_264862_;
            }
        });
    }

    private CompletableFuture<FilteredText> filterTextPacket(String pText) {
        return this.filterTextPacket(pText, TextFilter::processStreamMessage);
    }

    private CompletableFuture<List<FilteredText>> filterTextPacket(List<String> pTexts) {
        return this.filterTextPacket(pTexts, TextFilter::processMessageBundle);
    }

    /**
     * Processes player movement input. Includes walking, strafing, jumping, and sneaking. Excludes riding and toggling
     * flying/sprinting.
     */
    @Override
    public void handlePlayerInput(ServerboundPlayerInputPacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        this.player.setPlayerInput(pPacket.getXxa(), pPacket.getZza(), pPacket.isJumping(), pPacket.isShiftKeyDown());
    }

    private static boolean containsInvalidValues(double pX, double pY, double pZ, float pYRot, float pXRot) {
        return Double.isNaN(pX) || Double.isNaN(pY) || Double.isNaN(pZ) || !Floats.isFinite(pXRot) || !Floats.isFinite(pYRot);
    }

    private static double clampHorizontal(double pValue) {
        return Mth.clamp(pValue, -3.0E7D, 3.0E7D);
    }

    private static double clampVertical(double pValue) {
        return Mth.clamp(pValue, -2.0E7D, 2.0E7D);
    }

    @Override
    public void handleMoveVehicle(ServerboundMoveVehiclePacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        if (containsInvalidValues(pPacket.getX(), pPacket.getY(), pPacket.getZ(), pPacket.getYRot(), pPacket.getXRot())) {
            this.disconnect(Component.translatable("multiplayer.disconnect.invalid_vehicle_movement"));
        } else {
            Entity entity = this.player.getRootVehicle();
            if (entity != this.player && entity.getControllingPassenger() == this.player && entity == this.lastVehicle) {
                ServerLevel serverlevel = this.player.serverLevel();
                double d0 = entity.getX();
                double d1 = entity.getY();
                double d2 = entity.getZ();
                double d3 = clampHorizontal(pPacket.getX());
                double d4 = clampVertical(pPacket.getY());
                double d5 = clampHorizontal(pPacket.getZ());
                float f = Mth.wrapDegrees(pPacket.getYRot());
                float f1 = Mth.wrapDegrees(pPacket.getXRot());
                double d6 = d3 - this.vehicleFirstGoodX;
                double d7 = d4 - this.vehicleFirstGoodY;
                double d8 = d5 - this.vehicleFirstGoodZ;
                double d9 = entity.getDeltaMovement().lengthSqr();
                double d10 = d6 * d6 + d7 * d7 + d8 * d8;
                if (d10 - d9 > 100.0D && !this.isSingleplayerOwner()) {
                    LOGGER.warn("{} (vehicle of {}) moved too quickly! {},{},{}", entity.getName().getString(), this.player.getName().getString(), d6, d7, d8);
                    this.connection.send(new ClientboundMoveVehiclePacket(entity));
                    return;
                }

                boolean flag = serverlevel.noCollision(entity, entity.getBoundingBox().deflate(0.0625D));
                d6 = d3 - this.vehicleLastGoodX;
                d7 = d4 - this.vehicleLastGoodY - 1.0E-6D;
                d8 = d5 - this.vehicleLastGoodZ;
                boolean flag1 = entity.verticalCollisionBelow;
                if (entity instanceof LivingEntity) {
                    LivingEntity livingentity = (LivingEntity)entity;
                    if (livingentity.onClimbable()) {
                        livingentity.resetFallDistance();
                    }
                }

                entity.move(MoverType.PLAYER, new Vec3(d6, d7, d8));
                d6 = d3 - entity.getX();
                d7 = d4 - entity.getY();
                if (d7 > -0.5D || d7 < 0.5D) {
                    d7 = 0.0D;
                }

                d8 = d5 - entity.getZ();
                d10 = d6 * d6 + d7 * d7 + d8 * d8;
                boolean flag2 = false;
                if (d10 > 0.0625D) {
                    flag2 = true;
                    LOGGER.warn("{} (vehicle of {}) moved wrongly! {}", entity.getName().getString(), this.player.getName().getString(), Math.sqrt(d10));
                }

                entity.absMoveTo(d3, d4, d5, f, f1);
                this.player.absMoveTo(d3, d4, d5, this.player.getYRot(), this.player.getXRot()); // Forge - Resync player position on vehicle moving
                boolean flag3 = serverlevel.noCollision(entity, entity.getBoundingBox().deflate(0.0625D));
                if (flag && (flag2 || !flag3)) {
                    entity.absMoveTo(d0, d1, d2, f, f1);
                    this.player.absMoveTo(d3, d4, d5, this.player.getYRot(), this.player.getXRot()); // Forge - Resync player position on vehicle moving
                    this.connection.send(new ClientboundMoveVehiclePacket(entity));
                    return;
                }

                this.player.serverLevel().getChunkSource().move(this.player);
                this.player.checkMovementStatistics(this.player.getX() - d0, this.player.getY() - d1, this.player.getZ() - d2);
                this.clientVehicleIsFloating = d7 >= -0.03125D && !flag1 && !this.server.isFlightAllowed() && !entity.isNoGravity() && this.noBlocksAround(entity);
                this.vehicleLastGoodX = entity.getX();
                this.vehicleLastGoodY = entity.getY();
                this.vehicleLastGoodZ = entity.getZ();
            }

        }
    }

    private boolean noBlocksAround(Entity pEntity) {
        return pEntity.level().getBlockStates(pEntity.getBoundingBox().inflate(0.0625D).expandTowards(0.0D, -0.55D, 0.0D)).allMatch(BlockBehaviour.BlockStateBase::isAir);
    }

    @Override
    public void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        if (pPacket.getId() == this.awaitingTeleport) {
            if (this.awaitingPositionFromClient == null) {
                this.disconnect(Component.translatable("multiplayer.disconnect.invalid_player_movement"));
                return;
            }

            this.player.absMoveTo(this.awaitingPositionFromClient.x, this.awaitingPositionFromClient.y, this.awaitingPositionFromClient.z, this.player.getYRot(), this.player.getXRot());
            this.lastGoodX = this.awaitingPositionFromClient.x;
            this.lastGoodY = this.awaitingPositionFromClient.y;
            this.lastGoodZ = this.awaitingPositionFromClient.z;
            if (this.player.isChangingDimension()) {
                this.player.hasChangedDimension();
            }

            this.awaitingPositionFromClient = null;
        }

    }

    @Override
    public void handleRecipeBookSeenRecipePacket(ServerboundRecipeBookSeenRecipePacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        this.server.getRecipeManager().byKey(pPacket.getRecipe()).ifPresent(this.player.getRecipeBook()::removeHighlight);
    }

    @Override
    public void handleRecipeBookChangeSettingsPacket(ServerboundRecipeBookChangeSettingsPacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        this.player.getRecipeBook().setBookSetting(pPacket.getBookType(), pPacket.isOpen(), pPacket.isFiltering());
    }

    @Override
    public void handleSeenAdvancements(ServerboundSeenAdvancementsPacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        if (pPacket.getAction() == ServerboundSeenAdvancementsPacket.Action.OPENED_TAB) {
            ResourceLocation resourcelocation = pPacket.getTab();
            Advancement advancement = this.server.getAdvancements().getAdvancement(resourcelocation);
            if (advancement != null) {
                this.player.getAdvancements().setSelectedTab(advancement);
            }
        }

    }

    /**
     * This method is only called for manual tab-completion (the {@link
     * net.minecraft.commands.synchronization.SuggestionProviders#ASK_SERVER minecraft:ask_server} suggestion provider).
     */
    @Override
    public void handleCustomCommandSuggestions(ServerboundCommandSuggestionPacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        StringReader stringreader = new StringReader(pPacket.getCommand());
        if (stringreader.canRead() && stringreader.peek() == '/') {
            stringreader.skip();
        }

        ParseResults<CommandSourceStack> parseresults = this.server.getCommands().getDispatcher().parse(stringreader, this.player.createCommandSourceStack());
        this.server.getCommands().getDispatcher().getCompletionSuggestions(parseresults).thenAccept((p_238197_) -> {
            this.connection.send(new ClientboundCommandSuggestionsPacket(pPacket.getId(), p_238197_));
        });
    }

    @Override
    public void handleSetCommandBlock(ServerboundSetCommandBlockPacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        if (!this.server.isCommandBlockEnabled()) {
            this.player.sendSystemMessage(Component.translatable("advMode.notEnabled"));
        } else if (!this.player.canUseGameMasterBlocks()) {
            this.player.sendSystemMessage(Component.translatable("advMode.notAllowed"));
        } else {
            BaseCommandBlock basecommandblock = null;
            CommandBlockEntity commandblockentity = null;
            BlockPos blockpos = pPacket.getPos();
            BlockEntity blockentity = this.player.level().getBlockEntity(blockpos);
            if (blockentity instanceof CommandBlockEntity) {
                commandblockentity = (CommandBlockEntity)blockentity;
                basecommandblock = commandblockentity.getCommandBlock();
            }

            String s = pPacket.getCommand();
            boolean flag = pPacket.isTrackOutput();
            if (basecommandblock != null) {
                CommandBlockEntity.Mode commandblockentity$mode = commandblockentity.getMode();
                BlockState blockstate = this.player.level().getBlockState(blockpos);
                Direction direction = blockstate.getValue(CommandBlock.FACING);
                BlockState blockstate1;
                switch (pPacket.getMode()) {
                    case SEQUENCE:
                        blockstate1 = Blocks.CHAIN_COMMAND_BLOCK.defaultBlockState();
                        break;
                    case AUTO:
                        blockstate1 = Blocks.REPEATING_COMMAND_BLOCK.defaultBlockState();
                        break;
                    case REDSTONE:
                    default:
                        blockstate1 = Blocks.COMMAND_BLOCK.defaultBlockState();
                }

                BlockState blockstate2 = blockstate1.setValue(CommandBlock.FACING, direction).setValue(CommandBlock.CONDITIONAL, Boolean.valueOf(pPacket.isConditional()));
                if (blockstate2 != blockstate) {
                    this.player.level().setBlock(blockpos, blockstate2, 2);
                    blockentity.setBlockState(blockstate2);
                    this.player.level().getChunkAt(blockpos).setBlockEntity(blockentity);
                }

                basecommandblock.setCommand(s);
                basecommandblock.setTrackOutput(flag);
                if (!flag) {
                    basecommandblock.setLastOutput(null);
                }

                commandblockentity.setAutomatic(pPacket.isAutomatic());
                if (commandblockentity$mode != pPacket.getMode()) {
                    commandblockentity.onModeSwitch();
                }

                basecommandblock.onUpdated();
                if (!StringUtil.isNullOrEmpty(s)) {
                    this.player.sendSystemMessage(Component.translatable("advMode.setCommand.success", s));
                }
            }

        }
    }

    @Override
    public void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        if (!this.server.isCommandBlockEnabled()) {
            this.player.sendSystemMessage(Component.translatable("advMode.notEnabled"));
        } else if (!this.player.canUseGameMasterBlocks()) {
            this.player.sendSystemMessage(Component.translatable("advMode.notAllowed"));
        } else {
            BaseCommandBlock basecommandblock = pPacket.getCommandBlock(this.player.level());
            if (basecommandblock != null) {
                basecommandblock.setCommand(pPacket.getCommand());
                basecommandblock.setTrackOutput(pPacket.isTrackOutput());
                if (!pPacket.isTrackOutput()) {
                    basecommandblock.setLastOutput(null);
                }

                basecommandblock.onUpdated();
                this.player.sendSystemMessage(Component.translatable("advMode.setCommand.success", pPacket.getCommand()));
            }

        }
    }

    @Override
    public void handlePickItem(ServerboundPickItemPacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        this.player.getInventory().pickSlot(pPacket.getSlot());
        this.player.connection.send(new ClientboundContainerSetSlotPacket(-2, 0, this.player.getInventory().selected, this.player.getInventory().getItem(this.player.getInventory().selected)));
        this.player.connection.send(new ClientboundContainerSetSlotPacket(-2, 0, pPacket.getSlot(), this.player.getInventory().getItem(pPacket.getSlot())));
        this.player.connection.send(new ClientboundSetCarriedItemPacket(this.player.getInventory().selected));
    }

    @Override
    public void handleRenameItem(ServerboundRenameItemPacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        AbstractContainerMenu abstractcontainermenu = this.player.containerMenu;
        if (abstractcontainermenu instanceof AnvilMenu anvilmenu) {
            if (!anvilmenu.stillValid(this.player)) {
                LOGGER.debug("Player {} interacted with invalid menu {}", this.player, anvilmenu);
                return;
            }

            anvilmenu.setItemName(pPacket.getName());
        }

    }

    @Override
    public void handleSetBeaconPacket(ServerboundSetBeaconPacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        AbstractContainerMenu abstractcontainermenu = this.player.containerMenu;
        if (abstractcontainermenu instanceof BeaconMenu beaconmenu) {
            if (!this.player.containerMenu.stillValid(this.player)) {
                LOGGER.debug("Player {} interacted with invalid menu {}", this.player, this.player.containerMenu);
                return;
            }

            beaconmenu.updateEffects(pPacket.getPrimary(), pPacket.getSecondary());
        }

    }

    @Override
    public void handleSetStructureBlock(ServerboundSetStructureBlockPacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        if (this.player.canUseGameMasterBlocks()) {
            BlockPos blockpos = pPacket.getPos();
            BlockState blockstate = this.player.level().getBlockState(blockpos);
            BlockEntity blockentity = this.player.level().getBlockEntity(blockpos);
            if (blockentity instanceof StructureBlockEntity) {
                StructureBlockEntity structureblockentity = (StructureBlockEntity)blockentity;
                structureblockentity.setMode(pPacket.getMode());
                structureblockentity.setStructureName(pPacket.getName());
                structureblockentity.setStructurePos(pPacket.getOffset());
                structureblockentity.setStructureSize(pPacket.getSize());
                structureblockentity.setMirror(pPacket.getMirror());
                structureblockentity.setRotation(pPacket.getRotation());
                structureblockentity.setMetaData(pPacket.getData());
                structureblockentity.setIgnoreEntities(pPacket.isIgnoreEntities());
                structureblockentity.setShowAir(pPacket.isShowAir());
                structureblockentity.setShowBoundingBox(pPacket.isShowBoundingBox());
                structureblockentity.setIntegrity(pPacket.getIntegrity());
                structureblockentity.setSeed(pPacket.getSeed());
                if (structureblockentity.hasStructureName()) {
                    String s = structureblockentity.getStructureName();
                    if (pPacket.getUpdateType() == StructureBlockEntity.UpdateType.SAVE_AREA) {
                        if (structureblockentity.saveStructure()) {
                            this.player.displayClientMessage(Component.translatable("structure_block.save_success", s), false);
                        } else {
                            this.player.displayClientMessage(Component.translatable("structure_block.save_failure", s), false);
                        }
                    } else if (pPacket.getUpdateType() == StructureBlockEntity.UpdateType.LOAD_AREA) {
                        if (!structureblockentity.isStructureLoadable()) {
                            this.player.displayClientMessage(Component.translatable("structure_block.load_not_found", s), false);
                        } else if (structureblockentity.loadStructure(this.player.serverLevel())) {
                            this.player.displayClientMessage(Component.translatable("structure_block.load_success", s), false);
                        } else {
                            this.player.displayClientMessage(Component.translatable("structure_block.load_prepare", s), false);
                        }
                    } else if (pPacket.getUpdateType() == StructureBlockEntity.UpdateType.SCAN_AREA) {
                        if (structureblockentity.detectSize()) {
                            this.player.displayClientMessage(Component.translatable("structure_block.size_success", s), false);
                        } else {
                            this.player.displayClientMessage(Component.translatable("structure_block.size_failure"), false);
                        }
                    }
                } else {
                    this.player.displayClientMessage(Component.translatable("structure_block.invalid_structure_name", pPacket.getName()), false);
                }

                structureblockentity.setChanged();
                this.player.level().sendBlockUpdated(blockpos, blockstate, blockstate, 3);
            }

        }
    }

    @Override
    public void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        if (this.player.canUseGameMasterBlocks()) {
            BlockPos blockpos = pPacket.getPos();
            BlockState blockstate = this.player.level().getBlockState(blockpos);
            BlockEntity blockentity = this.player.level().getBlockEntity(blockpos);
            if (blockentity instanceof JigsawBlockEntity) {
                JigsawBlockEntity jigsawblockentity = (JigsawBlockEntity)blockentity;
                jigsawblockentity.setName(pPacket.getName());
                jigsawblockentity.setTarget(pPacket.getTarget());
                jigsawblockentity.setPool(ResourceKey.create(Registries.TEMPLATE_POOL, pPacket.getPool()));
                jigsawblockentity.setFinalState(pPacket.getFinalState());
                jigsawblockentity.setJoint(pPacket.getJoint());
                jigsawblockentity.setChanged();
                this.player.level().sendBlockUpdated(blockpos, blockstate, blockstate, 3);
            }

        }
    }

    @Override
    public void handleJigsawGenerate(ServerboundJigsawGeneratePacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        if (this.player.canUseGameMasterBlocks()) {
            BlockPos blockpos = pPacket.getPos();
            BlockEntity blockentity = this.player.level().getBlockEntity(blockpos);
            if (blockentity instanceof JigsawBlockEntity) {
                JigsawBlockEntity jigsawblockentity = (JigsawBlockEntity)blockentity;
                jigsawblockentity.generate(this.player.serverLevel(), pPacket.levels(), pPacket.keepJigsaws());
            }

        }
    }

    @Override
    public void handleSelectTrade(ServerboundSelectTradePacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        int i = pPacket.getItem();
        AbstractContainerMenu abstractcontainermenu = this.player.containerMenu;
        if (abstractcontainermenu instanceof MerchantMenu merchantmenu) {
            if (!merchantmenu.stillValid(this.player)) {
                LOGGER.debug("Player {} interacted with invalid menu {}", this.player, merchantmenu);
                return;
            }

            merchantmenu.setSelectionHint(i);
            merchantmenu.tryMoveItems(i);
        }

    }

    @Override
    public void handleEditBook(ServerboundEditBookPacket pPacket) {
        int i = pPacket.getSlot();
        if (Inventory.isHotbarSlot(i) || i == 40) {
            List<String> list = Lists.newArrayList();
            Optional<String> optional = pPacket.getTitle();
            optional.ifPresent(list::add);
            pPacket.getPages().stream().limit(100L).forEach(list::add);
            Consumer<List<FilteredText>> consumer = optional.isPresent() ? (p_238198_) -> {
                this.signBook(p_238198_.get(0), p_238198_.subList(1, p_238198_.size()), i);
            } : (p_143627_) -> {
                this.updateBookContents(p_143627_, i);
            };
            this.filterTextPacket(list).thenAcceptAsync(consumer, this.server);
        }
    }

    private void updateBookContents(List<FilteredText> pPages, int pIndex) {
        ItemStack itemstack = this.player.getInventory().getItem(pIndex);
        if (itemstack.is(Items.WRITABLE_BOOK)) {
            this.updateBookPages(pPages, UnaryOperator.identity(), itemstack);
        }
    }

    private void signBook(FilteredText pTitle, List<FilteredText> pPages, int pIndex) {
        ItemStack itemstack = this.player.getInventory().getItem(pIndex);
        if (itemstack.is(Items.WRITABLE_BOOK)) {
            ItemStack itemstack1 = new ItemStack(Items.WRITTEN_BOOK);
            CompoundTag compoundtag = itemstack.getTag();
            if (compoundtag != null) {
                itemstack1.setTag(compoundtag.copy());
            }

            itemstack1.addTagElement("author", StringTag.valueOf(this.player.getName().getString()));
            if (this.player.isTextFilteringEnabled()) {
                itemstack1.addTagElement("title", StringTag.valueOf(pTitle.filteredOrEmpty()));
            } else {
                itemstack1.addTagElement("filtered_title", StringTag.valueOf(pTitle.filteredOrEmpty()));
                itemstack1.addTagElement("title", StringTag.valueOf(pTitle.raw()));
            }

            this.updateBookPages(pPages, (p_238206_) -> {
                return Component.Serializer.toJson(Component.literal(p_238206_));
            }, itemstack1);
            this.player.getInventory().setItem(pIndex, itemstack1);
        }
    }

    private void updateBookPages(List<FilteredText> pPages, UnaryOperator<String> pUpdater, ItemStack pBook) {
        ListTag listtag = new ListTag();
        if (this.player.isTextFilteringEnabled()) {
            pPages.stream().map((p_238209_) -> {
                return StringTag.valueOf(pUpdater.apply(p_238209_.filteredOrEmpty()));
            }).forEach(listtag::add);
        } else {
            CompoundTag compoundtag = new CompoundTag();
            int i = 0;

            for(int j = pPages.size(); i < j; ++i) {
                FilteredText filteredtext = pPages.get(i);
                String s = filteredtext.raw();
                listtag.add(StringTag.valueOf(pUpdater.apply(s)));
                if (filteredtext.isFiltered()) {
                    compoundtag.putString(String.valueOf(i), pUpdater.apply(filteredtext.filteredOrEmpty()));
                }
            }

            if (!compoundtag.isEmpty()) {
                pBook.addTagElement("filtered_pages", compoundtag);
            }
        }

        pBook.addTagElement("pages", listtag);
    }

    @Override
    public void handleEntityTagQuery(ServerboundEntityTagQuery pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        if (this.player.hasPermissions(2)) {
            Entity entity = this.player.level().getEntity(pPacket.getEntityId());
            if (entity != null) {
                CompoundTag compoundtag = entity.saveWithoutId(new CompoundTag());
                this.player.connection.send(new ClientboundTagQueryPacket(pPacket.getTransactionId(), compoundtag));
            }

        }
    }

    @Override
    public void handleBlockEntityTagQuery(ServerboundBlockEntityTagQuery pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        if (this.player.hasPermissions(2)) {
            BlockEntity blockentity = this.player.level().getBlockEntity(pPacket.getPos());
            CompoundTag compoundtag = blockentity != null ? blockentity.saveWithoutMetadata() : null;
            this.player.connection.send(new ClientboundTagQueryPacket(pPacket.getTransactionId(), compoundtag));
        }
    }

    /**
     * Processes clients perspective on player positioning and/or orientation
     */
    @Override
    public void handleMovePlayer(ServerboundMovePlayerPacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        if (containsInvalidValues(pPacket.getX(0.0D), pPacket.getY(0.0D), pPacket.getZ(0.0D), pPacket.getYRot(0.0F), pPacket.getXRot(0.0F))) {
            this.disconnect(Component.translatable("multiplayer.disconnect.invalid_player_movement"));
        } else {
            ServerLevel serverlevel = this.player.serverLevel();
            if (!this.player.wonGame) {
                if (this.tickCount == 0) {
                    this.resetPosition();
                }

                if (this.awaitingPositionFromClient != null) {
                    if (this.tickCount - this.awaitingTeleportTime > 20) {
                        this.awaitingTeleportTime = this.tickCount;
                        this.teleport(this.awaitingPositionFromClient.x, this.awaitingPositionFromClient.y, this.awaitingPositionFromClient.z, this.player.getYRot(), this.player.getXRot());
                    }

                } else {
                    this.awaitingTeleportTime = this.tickCount;
                    double d0 = clampHorizontal(pPacket.getX(this.player.getX()));
                    double d1 = clampVertical(pPacket.getY(this.player.getY()));
                    double d2 = clampHorizontal(pPacket.getZ(this.player.getZ()));
                    float f = Mth.wrapDegrees(pPacket.getYRot(this.player.getYRot()));
                    float f1 = Mth.wrapDegrees(pPacket.getXRot(this.player.getXRot()));
                    if (this.player.isPassenger()) {
                        this.player.absMoveTo(this.player.getX(), this.player.getY(), this.player.getZ(), f, f1);
                        this.player.serverLevel().getChunkSource().move(this.player);
                    } else {
                        double d3 = this.player.getX();
                        double d4 = this.player.getY();
                        double d5 = this.player.getZ();
                        double d6 = d0 - this.firstGoodX;
                        double d7 = d1 - this.firstGoodY;
                        double d8 = d2 - this.firstGoodZ;
                        double d9 = this.player.getDeltaMovement().lengthSqr();
                        double d10 = d6 * d6 + d7 * d7 + d8 * d8;
                        if (this.player.isSleeping()) {
                            if (d10 > 1.0D) {
                                this.teleport(this.player.getX(), this.player.getY(), this.player.getZ(), f, f1);
                            }

                        } else {
                            ++this.receivedMovePacketCount;
                            int i = this.receivedMovePacketCount - this.knownMovePacketCount;
                            if (i > 5) {
                                LOGGER.debug("{} is sending move packets too frequently ({} packets since last tick)", this.player.getName().getString(), i);
                                i = 1;
                            }

                            if (!this.player.isChangingDimension() && (!this.player.level().getGameRules().getBoolean(GameRules.RULE_DISABLE_ELYTRA_MOVEMENT_CHECK) || !this.player.isFallFlying())) {
                                float f2 = this.player.isFallFlying() ? 300.0F : 100.0F;
                                if (d10 - d9 > (double)(f2 * (float)i) && !this.isSingleplayerOwner()) {
                                    LOGGER.warn("{} moved too quickly! {},{},{}", this.player.getName().getString(), d6, d7, d8);
                                    this.teleport(this.player.getX(), this.player.getY(), this.player.getZ(), this.player.getYRot(), this.player.getXRot());
                                    return;
                                }
                            }

                            AABB aabb = this.player.getBoundingBox();
                            d6 = d0 - this.lastGoodX;
                            d7 = d1 - this.lastGoodY;
                            d8 = d2 - this.lastGoodZ;
                            boolean flag = d7 > 0.0D;
                            if (this.player.onGround() && !pPacket.isOnGround() && flag) {
                                this.player.jumpFromGround();
                            }

                            boolean flag1 = this.player.verticalCollisionBelow;
                            this.player.move(MoverType.PLAYER, new Vec3(d6, d7, d8));
                            d6 = d0 - this.player.getX();
                            d7 = d1 - this.player.getY();
                            if (d7 > -0.5D || d7 < 0.5D) {
                                d7 = 0.0D;
                            }

                            d8 = d2 - this.player.getZ();
                            d10 = d6 * d6 + d7 * d7 + d8 * d8;
                            boolean flag2 = false;
                            if (!this.player.isChangingDimension() && d10 > 0.0625D && !this.player.isSleeping() && !this.player.gameMode.isCreative() && this.player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) {
                                flag2 = true;
                                LOGGER.warn("{} moved wrongly!", this.player.getName().getString());
                            }

                            if (this.player.noPhysics || this.player.isSleeping() || (!flag2 || !serverlevel.noCollision(this.player, aabb)) && !this.isPlayerCollidingWithAnythingNew(serverlevel, aabb, d0, d1, d2)) {
                                this.player.absMoveTo(d0, d1, d2, f, f1);
                                this.clientIsFloating = d7 >= -0.03125D && !flag1 && this.player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR && !this.server.isFlightAllowed() && !this.player.getAbilities().mayfly && !this.player.hasEffect(MobEffects.LEVITATION) && !this.player.isFallFlying() && !this.player.isAutoSpinAttack() && this.noBlocksAround(this.player);
                                this.player.serverLevel().getChunkSource().move(this.player);
                                this.player.doCheckFallDamage(this.player.getX() - d3, this.player.getY() - d4, this.player.getZ() - d5, pPacket.isOnGround());
                                this.player.setOnGroundWithKnownMovement(pPacket.isOnGround(), new Vec3(this.player.getX() - d3, this.player.getY() - d4, this.player.getZ() - d5));
                                if (flag) {
                                    this.player.resetFallDistance();
                                }

                                this.player.checkMovementStatistics(this.player.getX() - d3, this.player.getY() - d4, this.player.getZ() - d5);
                                this.lastGoodX = this.player.getX();
                                this.lastGoodY = this.player.getY();
                                this.lastGoodZ = this.player.getZ();
                            } else {
                                this.teleport(d3, d4, d5, f, f1);
                                this.player.doCheckFallDamage(this.player.getX() - d3, this.player.getY() - d4, this.player.getZ() - d5, pPacket.isOnGround());
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isPlayerCollidingWithAnythingNew(LevelReader pLevel, AABB pBox, double pX, double pY, double pZ) {
        AABB aabb = this.player.getBoundingBox().move(pX - this.player.getX(), pY - this.player.getY(), pZ - this.player.getZ());
        Iterable<VoxelShape> iterable = pLevel.getCollisions(this.player, aabb.deflate(1.0E-5F));
        VoxelShape voxelshape = Shapes.create(pBox.deflate(1.0E-5F));

        for(VoxelShape voxelshape1 : iterable) {
            if (!Shapes.joinIsNotEmpty(voxelshape1, voxelshape, BooleanOp.AND)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void teleport(double pX, double pY, double pZ, float pYaw, float pPitch) {
        this.teleport(pX, pY, pZ, pYaw, pPitch, Collections.emptySet());
    }

    /**
     * Teleports the player position to the (relative) values specified, and syncs to the client
     */
    @Override
    public void teleport(double pX, double pY, double pZ, float pYaw, float pPitch, Set<RelativeMovement> pRelativeSet) {
        double d0 = pRelativeSet.contains(RelativeMovement.X) ? this.player.getX() : 0.0D;
        double d1 = pRelativeSet.contains(RelativeMovement.Y) ? this.player.getY() : 0.0D;
        double d2 = pRelativeSet.contains(RelativeMovement.Z) ? this.player.getZ() : 0.0D;
        float f = pRelativeSet.contains(RelativeMovement.Y_ROT) ? this.player.getYRot() : 0.0F;
        float f1 = pRelativeSet.contains(RelativeMovement.X_ROT) ? this.player.getXRot() : 0.0F;
        this.awaitingPositionFromClient = new Vec3(pX, pY, pZ);
        if (++this.awaitingTeleport == Integer.MAX_VALUE) {
            this.awaitingTeleport = 0;
        }

        this.awaitingTeleportTime = this.tickCount;
        this.player.absMoveTo(pX, pY, pZ, pYaw, pPitch);
        this.player.connection.send(new ClientboundPlayerPositionPacket(pX - d0, pY - d1, pZ - d2, pYaw - f, pPitch - f1, pRelativeSet, this.awaitingTeleport));
    }

    /**
     * Processes the player initiating/stopping digging on a particular spot, as well as a player dropping items
     */
    @Override
    public void handlePlayerAction(ServerboundPlayerActionPacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        BlockPos blockpos = pPacket.getPos();
        this.player.resetLastActionTime();
        ServerboundPlayerActionPacket.Action serverboundplayeractionpacket$action = pPacket.getAction();
        switch (serverboundplayeractionpacket$action) {
            case SWAP_ITEM_WITH_OFFHAND:
                if (!this.player.isSpectator()) {
                    ItemStack itemstack = this.player.getItemInHand(InteractionHand.OFF_HAND);
                    var event = net.minecraftforge.common.ForgeHooks.onLivingSwapHandItems(this.player);
                    if (event.isCanceled()) {
                        return;
                    }
                    this.player.setItemInHand(InteractionHand.OFF_HAND, event.getItemSwappedToOffHand());
                    this.player.setItemInHand(InteractionHand.MAIN_HAND, event.getItemSwappedToMainHand());
                    this.player.stopUsingItem();
                }

                return;
            case DROP_ITEM:
                if (!this.player.isSpectator()) {
                    this.player.drop(false);
                }

                return;
            case DROP_ALL_ITEMS:
                if (!this.player.isSpectator()) {
                    this.player.drop(true);
                }

                return;
            case RELEASE_USE_ITEM:
                this.player.releaseUsingItem();
                return;
            case START_DESTROY_BLOCK:
            case ABORT_DESTROY_BLOCK:
            case STOP_DESTROY_BLOCK:
                this.player.gameMode.handleBlockBreakAction(blockpos, serverboundplayeractionpacket$action, pPacket.getDirection(), this.player.level().getMaxBuildHeight(), pPacket.getSequence());
                this.player.connection.ackBlockChangesUpTo(pPacket.getSequence());
                return;
            default:
                throw new IllegalArgumentException("Invalid player action");
        }
    }

    private static boolean wasBlockPlacementAttempt(ServerPlayer pPlayer, ItemStack pStack) {
        if (pStack.isEmpty()) {
            return false;
        } else {
            Item item = pStack.getItem();
            return (item instanceof BlockItem || item instanceof BucketItem) && !pPlayer.getCooldowns().isOnCooldown(item);
        }
    }

    @Override
    public void handleUseItemOn(ServerboundUseItemOnPacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        this.player.connection.ackBlockChangesUpTo(pPacket.getSequence());
        ServerLevel serverlevel = this.player.serverLevel();
        InteractionHand interactionhand = pPacket.getHand();
        ItemStack itemstack = this.player.getItemInHand(interactionhand);
        if (itemstack.isItemEnabled(serverlevel.enabledFeatures())) {
            BlockHitResult blockhitresult = pPacket.getHitResult();
            Vec3 vec3 = blockhitresult.getLocation();
            BlockPos blockpos = blockhitresult.getBlockPos();
            Vec3 vec31 = Vec3.atCenterOf(blockpos);
            if (this.player.canReach(blockpos, 1.5)) { // Vanilla uses eye-to-center distance < 6, which implies a padding of 1.5
                Vec3 vec32 = vec3.subtract(vec31);
                double d0 = 1.0000001D;
                if (Math.abs(vec32.x()) < 1.0000001D && Math.abs(vec32.y()) < 1.0000001D && Math.abs(vec32.z()) < 1.0000001D) {
                    Direction direction = blockhitresult.getDirection();
                    this.player.resetLastActionTime();
                    int i = this.player.level().getMaxBuildHeight();
                    if (blockpos.getY() < i) {
                        if (this.awaitingPositionFromClient == null && serverlevel.mayInteract(this.player, blockpos)) {
                            InteractionResult interactionresult = this.player.gameMode.useItemOn(this.player, serverlevel, itemstack, interactionhand, blockhitresult);
                            if (direction == Direction.UP && !interactionresult.consumesAction() && blockpos.getY() >= i - 1 && wasBlockPlacementAttempt(this.player, itemstack)) {
                                Component component = Component.translatable("build.tooHigh", i - 1).withStyle(ChatFormatting.RED);
                                this.player.sendSystemMessage(component, true);
                            } else if (interactionresult.shouldSwing()) {
                                this.player.swing(interactionhand, true);
                            }
                        }
                    } else {
                        Component component1 = Component.translatable("build.tooHigh", i - 1).withStyle(ChatFormatting.RED);
                        this.player.sendSystemMessage(component1, true);
                    }

                    this.player.connection.send(new ClientboundBlockUpdatePacket(serverlevel, blockpos));
                    this.player.connection.send(new ClientboundBlockUpdatePacket(serverlevel, blockpos.relative(direction)));
                } else {
                    LOGGER.warn("Rejecting UseItemOnPacket from {}: Location {} too far away from hit block {}.", this.player.getGameProfile().getName(), vec3, blockpos);
                }
            }
        }
    }

    /**
     * Called when a client is using an item while not pointing at a block, but simply using an item
     */
    @Override
    public void handleUseItem(ServerboundUseItemPacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        this.ackBlockChangesUpTo(pPacket.getSequence());
        ServerLevel serverlevel = this.player.serverLevel();
        InteractionHand interactionhand = pPacket.getHand();
        ItemStack itemstack = this.player.getItemInHand(interactionhand);
        this.player.resetLastActionTime();
        if (!itemstack.isEmpty() && itemstack.isItemEnabled(serverlevel.enabledFeatures())) {
            InteractionResult interactionresult = this.player.gameMode.useItem(this.player, serverlevel, itemstack, interactionhand);
            if (interactionresult.shouldSwing()) {
                this.player.swing(interactionhand, true);
            }

        }
    }

    @Override
    public void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        if (this.player.isSpectator()) {
            for(ServerLevel serverlevel : this.server.getAllLevels()) {
                Entity entity = pPacket.getEntity(serverlevel);
                if (entity != null) {
                    this.player.teleportTo(serverlevel, entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
                    return;
                }
            }
        }

    }

    @Override
    public void handleResourcePackResponse(ServerboundResourcePackPacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        if (pPacket.getAction() == ServerboundResourcePackPacket.Action.DECLINED && this.server.isResourcePackRequired()) {
            LOGGER.info("Disconnecting {} due to resource pack rejection", this.player.getName());
            this.disconnect(Component.translatable("multiplayer.requiredTexturePrompt.disconnect"));
        }

    }

    @Override
    public void handlePaddleBoat(ServerboundPaddleBoatPacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        Entity entity = this.player.getControlledVehicle();
        if (entity instanceof Boat boat) {
            boat.setPaddleState(pPacket.getLeft(), pPacket.getRight());
        }

    }

    @Override
    public void handlePong(ServerboundPongPacket pPacket) {
    }

    /**
     * Invoked when disconnecting, the parameter is a ChatComponent describing the reason for termination
     */
    @Override
    public void onDisconnect(Component pReason) {
        this.chatMessageChain.close();
        LOGGER.info("{} lost connection: {}", this.player.getName().getString(), pReason.getString());
        this.server.invalidateStatus();
        this.server.getPlayerList().broadcastSystemMessage(Component.translatable("multiplayer.player.left", this.player.getDisplayName()).withStyle(ChatFormatting.YELLOW), false);
        this.player.disconnect();
        this.server.getPlayerList().remove(this.player);
        this.player.getTextFilter().leave();
        if (this.isSingleplayerOwner()) {
            LOGGER.info("Stopping singleplayer server as player logged out");
            this.server.halt(false);
        }

    }

    @Override
    public void ackBlockChangesUpTo(int pSequence) {
        if (pSequence < 0) {
            throw new IllegalArgumentException("Expected packet sequence nr >= 0");
        } else {
            this.ackBlockChangesUpTo = Math.max(pSequence, this.ackBlockChangesUpTo);
        }
    }

    @Override
    public void send(Packet<?> pPacket) {
        this.send(pPacket, null);
    }

    @Override
    public void send(Packet<?> pPacket, @Nullable PacketSendListener pListener) {
        try {
            this.connection.send(pPacket, pListener);
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Sending packet");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Packet being sent");
            crashreportcategory.setDetail("Packet class", () -> {
                return pPacket.getClass().getCanonicalName();
            });
            throw new ReportedException(crashreport);
        }
    }

    /**
     * Updates which quickbar slot is selected
     */
    @Override
    public void handleSetCarriedItem(ServerboundSetCarriedItemPacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        if (pPacket.getSlot() >= 0 && pPacket.getSlot() < Inventory.getSelectionSize()) {
            if (this.player.getInventory().selected != pPacket.getSlot() && this.player.getUsedItemHand() == InteractionHand.MAIN_HAND) {
                this.player.stopUsingItem();
            }

            this.player.getInventory().selected = pPacket.getSlot();
            this.player.resetLastActionTime();
        } else {
            LOGGER.warn("{} tried to set an invalid carried item", this.player.getName().getString());
        }
    }

    /**
     * Process chat messages (broadcast back to clients) and commands (executes)
     */
    @Override
    public void handleChat(ServerboundChatPacket pPacket) {
        if (isChatMessageIllegal(pPacket.message())) {
            this.disconnect(Component.translatable("multiplayer.disconnect.illegal_characters"));
        } else {
            Optional<LastSeenMessages> optional = this.tryHandleChat(pPacket.message(), pPacket.timeStamp(), pPacket.lastSeenMessages());
            if (optional.isPresent()) {
                this.server.submit(() -> {
                    PlayerChatMessage playerchatmessage;
                    try {
                        playerchatmessage = this.getSignedMessage(pPacket, optional.get());
                    } catch (SignedMessageChain.DecodeException signedmessagechain$decodeexception) {
                        this.handleMessageDecodeFailure(signedmessagechain$decodeexception);
                        return;
                    }

                    CompletableFuture<FilteredText> completablefuture = this.filterTextPacket(playerchatmessage.signedContent());
                    CompletableFuture<Component> completablefuture1 = net.minecraftforge.common.ForgeHooks.getServerChatSubmittedDecorator().decorate(this.player, playerchatmessage.decoratedContent());
                    this.chatMessageChain.append((p_248212_) -> {
                        return CompletableFuture.allOf(completablefuture, completablefuture1).thenAcceptAsync((p_248218_) -> {
                            Component decoratedContent = completablefuture1.join();
                            if (decoratedContent == null) {
                                return; // Forge: ServerChatEvent was canceled if this is null.
                            }
                            PlayerChatMessage playerchatmessage1 = playerchatmessage.withUnsignedContent(decoratedContent).filter(completablefuture.join().mask());
                            this.broadcastChatMessage(playerchatmessage1);
                        }, p_248212_);
                    });
                });
            }

        }
    }

    @Override
    public void handleChatCommand(ServerboundChatCommandPacket pPacket) {
        if (isChatMessageIllegal(pPacket.command())) {
            this.disconnect(Component.translatable("multiplayer.disconnect.illegal_characters"));
        } else {
            Optional<LastSeenMessages> optional = this.tryHandleChat(pPacket.command(), pPacket.timeStamp(), pPacket.lastSeenMessages());
            if (optional.isPresent()) {
                this.server.submit(() -> {
                    this.performChatCommand(pPacket, optional.get());
                    this.detectRateSpam();
                });
            }

        }
    }

    private void performChatCommand(ServerboundChatCommandPacket pPacket, LastSeenMessages pLastSeenMessages) {
        ParseResults<CommandSourceStack> parseresults = this.parseCommand(pPacket.command());

        Map<String, PlayerChatMessage> map;
        try {
            map = this.collectSignedArguments(pPacket, SignableCommand.of(parseresults), pLastSeenMessages);
        } catch (SignedMessageChain.DecodeException signedmessagechain$decodeexception) {
            this.handleMessageDecodeFailure(signedmessagechain$decodeexception);
            return;
        }

        CommandSigningContext commandsigningcontext = new CommandSigningContext.SignedArguments(map);
        parseresults = Commands.mapSource(parseresults, (p_242749_) -> {
            return p_242749_.withSigningContext(commandsigningcontext);
        });
        this.server.getCommands().performCommand(parseresults, pPacket.command());
    }

    private void handleMessageDecodeFailure(SignedMessageChain.DecodeException pException) {
        if (pException.shouldDisconnect()) {
            this.disconnect(pException.getComponent());
        } else {
            this.player.sendSystemMessage(pException.getComponent().copy().withStyle(ChatFormatting.RED));
        }

    }

    private Map<String, PlayerChatMessage> collectSignedArguments(ServerboundChatCommandPacket pPacket, SignableCommand<?> pCommand, LastSeenMessages pLastSeenMessages) throws SignedMessageChain.DecodeException {
        Map<String, PlayerChatMessage> map = new Object2ObjectOpenHashMap<>();

        for(SignableCommand.Argument<?> argument : pCommand.arguments()) {
            MessageSignature messagesignature = pPacket.argumentSignatures().get(argument.name());
            SignedMessageBody signedmessagebody = new SignedMessageBody(argument.value(), pPacket.timeStamp(), pPacket.salt(), pLastSeenMessages);
            map.put(argument.name(), this.signedMessageDecoder.unpack(messagesignature, signedmessagebody));
        }

        return map;
    }

    private ParseResults<CommandSourceStack> parseCommand(String pCommand) {
        CommandDispatcher<CommandSourceStack> commanddispatcher = this.server.getCommands().getDispatcher();
        return commanddispatcher.parse(pCommand, this.player.createCommandSourceStack());
    }

    private Optional<LastSeenMessages> tryHandleChat(String pMessage, Instant pTimestamp, LastSeenMessages.Update pUpdate) {
        if (!this.updateChatOrder(pTimestamp)) {
            LOGGER.warn("{} sent out-of-order chat: '{}'", this.player.getName().getString(), pMessage);
            this.disconnect(Component.translatable("multiplayer.disconnect.out_of_order_chat"));
            return Optional.empty();
        } else {
            Optional<LastSeenMessages> optional = this.unpackAndApplyLastSeen(pUpdate);
            if (this.player.getChatVisibility() == ChatVisiblity.HIDDEN) {
                this.send(new ClientboundSystemChatPacket(Component.translatable("chat.disabled.options").withStyle(ChatFormatting.RED), false));
                return Optional.empty();
            } else {
                this.player.resetLastActionTime();
                return optional;
            }
        }
    }

    private Optional<LastSeenMessages> unpackAndApplyLastSeen(LastSeenMessages.Update pUpdate) {
        synchronized(this.lastSeenMessages) {
            Optional<LastSeenMessages> optional = this.lastSeenMessages.applyUpdate(pUpdate);
            if (optional.isEmpty()) {
                LOGGER.warn("Failed to validate message acknowledgements from {}", this.player.getName().getString());
                this.disconnect(CHAT_VALIDATION_FAILED);
            }

            return optional;
        }
    }

    private boolean updateChatOrder(Instant pTimestamp) {
        Instant instant;
        do {
            instant = this.lastChatTimeStamp.get();
            if (pTimestamp.isBefore(instant)) {
                return false;
            }
        } while(!this.lastChatTimeStamp.compareAndSet(instant, pTimestamp));

        return true;
    }

    private static boolean isChatMessageIllegal(String pMessage) {
        for(int i = 0; i < pMessage.length(); ++i) {
            if (!SharedConstants.isAllowedChatCharacter(pMessage.charAt(i))) {
                return true;
            }
        }

        return false;
    }

    private PlayerChatMessage getSignedMessage(ServerboundChatPacket pPacket, LastSeenMessages pLastSeenMessages) throws SignedMessageChain.DecodeException {
        SignedMessageBody signedmessagebody = new SignedMessageBody(pPacket.message(), pPacket.timeStamp(), pPacket.salt(), pLastSeenMessages);
        return this.signedMessageDecoder.unpack(pPacket.signature(), signedmessagebody);
    }

    private void broadcastChatMessage(PlayerChatMessage pMessage) {
        this.server.getPlayerList().broadcastChatMessage(pMessage, this.player, ChatType.bind(ChatType.CHAT, this.player));
        this.detectRateSpam();
    }

    private void detectRateSpam() {
        this.chatSpamTickCount += 20;
        if (this.chatSpamTickCount > 200 && !this.server.getPlayerList().isOp(this.player.getGameProfile())) {
            this.disconnect(Component.translatable("disconnect.spam"));
        }

    }

    @Override
    public void handleChatAck(ServerboundChatAckPacket pPacket) {
        synchronized(this.lastSeenMessages) {
            if (!this.lastSeenMessages.applyOffset(pPacket.offset())) {
                LOGGER.warn("Failed to validate message acknowledgements from {}", this.player.getName().getString());
                this.disconnect(CHAT_VALIDATION_FAILED);
            }

        }
    }

    @Override
    public void handleAnimate(ServerboundSwingPacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        this.player.resetLastActionTime();
        this.player.swing(pPacket.getHand());
    }

    /**
     * Processes a range of action-types: sneaking, sprinting, waking from sleep, opening the inventory or setting jump
     * height of the horse the player is riding
     */
    @Override
    public void handlePlayerCommand(ServerboundPlayerCommandPacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        this.player.resetLastActionTime();
        switch (pPacket.getAction()) {
            case PRESS_SHIFT_KEY:
                this.player.setShiftKeyDown(true);
                break;
            case RELEASE_SHIFT_KEY:
                this.player.setShiftKeyDown(false);
                break;
            case START_SPRINTING:
                this.player.setSprinting(true);
                break;
            case STOP_SPRINTING:
                this.player.setSprinting(false);
                break;
            case STOP_SLEEPING:
                if (this.player.isSleeping()) {
                    this.player.stopSleepInBed(false, true);
                    this.awaitingPositionFromClient = this.player.position();
                }
                break;
            case START_RIDING_JUMP:
                Entity entity2 = this.player.getControlledVehicle();
                if (entity2 instanceof PlayerRideableJumping playerrideablejumping1) {
                    int i = pPacket.getData();
                    if (playerrideablejumping1.canJump() && i > 0) {
                        playerrideablejumping1.handleStartJump(i);
                    }
                }
                break;
            case STOP_RIDING_JUMP:
                Entity entity1 = this.player.getControlledVehicle();
                if (entity1 instanceof PlayerRideableJumping playerrideablejumping) {
                    playerrideablejumping.handleStopJump();
                }
                break;
            case OPEN_INVENTORY:
                Entity $$2 = this.player.getVehicle();
                if ($$2 instanceof HasCustomInventoryScreen hascustominventoryscreen) {
                    hascustominventoryscreen.openCustomInventoryScreen(this.player);
                }
                break;
            case START_FALL_FLYING:
                if (!this.player.tryToStartFallFlying()) {
                    this.player.stopFallFlying();
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid client command!");
        }

    }

    @Override
    public void addPendingMessage(PlayerChatMessage pMessage) {
        MessageSignature messagesignature = pMessage.signature();
        if (messagesignature != null) {
            this.messageSignatureCache.push(pMessage);
            int i;
            synchronized(this.lastSeenMessages) {
                this.lastSeenMessages.addPending(messagesignature);
                i = this.lastSeenMessages.trackedMessagesCount();
            }

            if (i > 4096) {
                this.disconnect(Component.translatable("multiplayer.disconnect.too_many_pending_chats"));
            }

        }
    }

    @Override
    public void sendPlayerChatMessage(PlayerChatMessage pChatMessage, ChatType.Bound pBoundType) {
        this.send(new ClientboundPlayerChatPacket(pChatMessage.link().sender(), pChatMessage.link().index(), pChatMessage.signature(), pChatMessage.signedBody().pack(this.messageSignatureCache), pChatMessage.unsignedContent(), pChatMessage.filterMask(), pBoundType.toNetwork(this.player.level().registryAccess())));
        this.addPendingMessage(pChatMessage);
    }

    @Override
    public void sendDisguisedChatMessage(Component pMessage, ChatType.Bound pBoundType) {
        this.send(new ClientboundDisguisedChatPacket(pMessage, pBoundType.toNetwork(this.player.level().registryAccess())));
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return this.connection.getRemoteAddress();
    }

    /**
     * Processes left and right clicks on entities
     */
    @Override
    public void handleInteract(ServerboundInteractPacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        final ServerLevel serverlevel = this.player.serverLevel();
        final Entity entity = pPacket.getTarget(serverlevel);
        this.player.resetLastActionTime();
        this.player.setShiftKeyDown(pPacket.isUsingSecondaryAction());
        if (entity != null) {
            if (!serverlevel.getWorldBorder().isWithinBounds(entity.blockPosition())) {
                return;
            }

            AABB aabb = entity.getBoundingBox();
            if (this.player.canReach(entity, 3)) { // Vanilla padding is 3.0 (distSq < 6.0 * 6.0)
                pPacket.dispatch(new ServerboundInteractPacket.Handler() {
                    private void performInteraction(InteractionHand p_143679_, ServerGamePacketListenerImpl.EntityInteraction p_143680_) {
                        ItemStack itemstack = ServerGamePacketListenerImplModified.this.player.getItemInHand(p_143679_);
                        if (itemstack.isItemEnabled(serverlevel.enabledFeatures())) {
                            ItemStack itemstack1 = itemstack.copy();
                            InteractionResult interactionresult = p_143680_.run(ServerGamePacketListenerImplModified.this.player, entity, p_143679_);
                            if (interactionresult.consumesAction()) {
                                CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY.trigger(ServerGamePacketListenerImplModified.this.player, itemstack1, entity);
                                if (interactionresult.shouldSwing()) {
                                    ServerGamePacketListenerImplModified.this.player.swing(p_143679_, true);
                                }
                            }

                        }
                    }

                    @Override
                    public void onInteraction(InteractionHand p_143677_) {
                        this.performInteraction(p_143677_, Player::interactOn);
                    }

                    @Override
                    public void onInteraction(InteractionHand p_143682_, Vec3 p_143683_) {
                        this.performInteraction(p_143682_, (p_143686_, p_143687_, p_143688_) -> {
                            InteractionResult onInteractEntityAtResult = net.minecraftforge.common.ForgeHooks.onInteractEntityAt(player, entity, p_143683_, p_143682_);
                            if (onInteractEntityAtResult != null) {
                                return onInteractEntityAtResult;
                            }
                            return p_143687_.interactAt(p_143686_, p_143683_, p_143688_);
                        });
                    }

                    @Override
                    public void onAttack() {
                        if (!(entity instanceof ItemEntity) && !(entity instanceof ExperienceOrb) && !(entity instanceof AbstractArrow) && entity != ServerGamePacketListenerImplModified.this.player) {
                            ItemStack itemstack = ServerGamePacketListenerImplModified.this.player.getItemInHand(InteractionHand.MAIN_HAND);
                            if (itemstack.isItemEnabled(serverlevel.enabledFeatures())) {
                                ServerGamePacketListenerImplModified.this.player.attack(entity);
                            }
                        } else {
                            ServerGamePacketListenerImplModified.this.disconnect(Component.translatable("multiplayer.disconnect.invalid_entity_attacked"));
                            ServerGamePacketListenerImpl.LOGGER.warn("Player {} tried to attack an invalid entity", ServerGamePacketListenerImplModified.this.player.getName().getString());
                        }
                    }
                });
            }
        }

    }

    /**
     * Processes the client status updates: respawn attempt from player, opening statistics or achievements, or acquiring
     * 'open inventory' achievement
     */
    @Override
    public void handleClientCommand(ServerboundClientCommandPacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        this.player.resetLastActionTime();
        ServerboundClientCommandPacket.Action serverboundclientcommandpacket$action = pPacket.getAction();
        switch (serverboundclientcommandpacket$action) {
            case PERFORM_RESPAWN:
                if (this.player.wonGame) {
                    this.player.wonGame = false;
                    this.player = this.server.getPlayerList().respawn(this.player, true);
                    CriteriaTriggers.CHANGED_DIMENSION.trigger(this.player, Level.END, Level.OVERWORLD);
                } else {
                    if (this.player.getHealth() > 0.0F) {
                        return;
                    }

                    this.player = this.server.getPlayerList().respawn(this.player, false);
                    if (this.server.isHardcore()) {
                        this.player.setGameMode(GameType.SPECTATOR);
                        this.player.level().getGameRules().getRule(GameRules.RULE_SPECTATORSGENERATECHUNKS).set(false, this.server);
                    }
                }
                break;
            case REQUEST_STATS:
                this.player.getStats().sendStats(this.player);
        }

    }

    /**
     * Processes the client closing windows (container)
     */
    @Override
    public void handleContainerClose(ServerboundContainerClosePacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        this.player.doCloseContainer();
    }

    /**
     * Executes a container/inventory slot manipulation as indicated by the packet. Sends the serverside result if they
     * didn't match the indicated result and prevents further manipulation by the player until he confirms that it has
     * the same open container/inventory
     */
    @Override
    public void handleContainerClick(ServerboundContainerClickPacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        this.player.resetLastActionTime();
        if (this.player.containerMenu.containerId == pPacket.getContainerId()) {
            if (this.player.isSpectator()) {
                this.player.containerMenu.sendAllDataToRemote();
            } else if (!this.player.containerMenu.stillValid(this.player)) {
                LOGGER.debug("Player {} interacted with invalid menu {}", this.player, this.player.containerMenu);
            } else {
                int i = pPacket.getSlotNum();
                if (!this.player.containerMenu.isValidSlotIndex(i)) {
                    LOGGER.debug("Player {} clicked invalid slot index: {}, available slots: {}", this.player.getName(), i, this.player.containerMenu.slots.size());
                } else {
                    boolean flag = pPacket.getStateId() != this.player.containerMenu.getStateId();
                    this.player.containerMenu.suppressRemoteUpdates();
                    this.player.containerMenu.clicked(i, pPacket.getButtonNum(), pPacket.getClickType(), this.player);

                    for(Int2ObjectMap.Entry<ItemStack> entry : Int2ObjectMaps.fastIterable(pPacket.getChangedSlots())) {
                        this.player.containerMenu.setRemoteSlotNoCopy(entry.getIntKey(), entry.getValue());
                    }

                    this.player.containerMenu.setRemoteCarried(pPacket.getCarriedItem());
                    this.player.containerMenu.resumeRemoteUpdates();
                    if (flag) {
                        this.player.containerMenu.broadcastFullState();
                    } else {
                        this.player.containerMenu.broadcastChanges();
                    }

                }
            }
        }
    }

    @Override
    public void handlePlaceRecipe(ServerboundPlaceRecipePacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        this.player.resetLastActionTime();
        if (!this.player.isSpectator() && this.player.containerMenu.containerId == pPacket.getContainerId() && this.player.containerMenu instanceof RecipeBookMenu) {
            if (!this.player.containerMenu.stillValid(this.player)) {
                LOGGER.debug("Player {} interacted with invalid menu {}", this.player, this.player.containerMenu);
            } else {
                this.server.getRecipeManager().byKey(pPacket.getRecipe()).ifPresent((p_287379_) -> {
                    ((RecipeBookMenu)this.player.containerMenu).handlePlacement(pPacket.isShiftDown(), p_287379_, this.player);
                });
            }
        }
    }

    /**
     * Enchants the item identified by the packet given some convoluted conditions (matching window, which
     * should/shouldn't be in use?)
     */
    @Override
    public void handleContainerButtonClick(ServerboundContainerButtonClickPacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        this.player.resetLastActionTime();
        if (this.player.containerMenu.containerId == pPacket.getContainerId() && !this.player.isSpectator()) {
            if (!this.player.containerMenu.stillValid(this.player)) {
                LOGGER.debug("Player {} interacted with invalid menu {}", this.player, this.player.containerMenu);
            } else {
                boolean flag = this.player.containerMenu.clickMenuButton(this.player, pPacket.getButtonId());
                if (flag) {
                    this.player.containerMenu.broadcastChanges();
                }

            }
        }
    }

    /**
     * Update the server with an ItemStack in a slot.
     */
    @Override
    public void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        if (this.player.gameMode.isCreative()) {
            boolean flag = pPacket.getSlotNum() < 0;
            ItemStack itemstack = pPacket.getItem();
            if (!itemstack.isItemEnabled(this.player.level().enabledFeatures())) {
                return;
            }

            CompoundTag compoundtag = BlockItem.getBlockEntityData(itemstack);
            if (!itemstack.isEmpty() && compoundtag != null && compoundtag.contains("x") && compoundtag.contains("y") && compoundtag.contains("z")) {
                BlockPos blockpos = BlockEntity.getPosFromTag(compoundtag);
                if (this.player.level().isLoaded(blockpos)) {
                    BlockEntity blockentity = this.player.level().getBlockEntity(blockpos);
                    if (blockentity != null) {
                        blockentity.saveToItem(itemstack);
                    }
                }
            }

            boolean flag1 = pPacket.getSlotNum() >= 1 && pPacket.getSlotNum() <= 45;
            boolean flag2 = itemstack.isEmpty() || itemstack.getDamageValue() >= 0 && itemstack.getCount() <= 64 && !itemstack.isEmpty();
            if (flag1 && flag2) {
                this.player.inventoryMenu.getSlot(pPacket.getSlotNum()).setByPlayer(itemstack);
                this.player.inventoryMenu.broadcastChanges();
            } else if (flag && flag2 && this.dropSpamTickCount < 200) {
                this.dropSpamTickCount += 20;
                this.player.drop(itemstack, true);
            }
        }

    }

    @Override
    public void handleSignUpdate(ServerboundSignUpdatePacket pPacket) {
        List<String> list = Stream.of(pPacket.getLines()).map(ChatFormatting::stripFormatting).collect(Collectors.toList());
        this.filterTextPacket(list).thenAcceptAsync((p_215245_) -> {
            this.updateSignText(pPacket, p_215245_);
        }, this.server);
    }

    private void updateSignText(ServerboundSignUpdatePacket pPacket, List<FilteredText> pFilteredText) {
        this.player.resetLastActionTime();
        ServerLevel serverlevel = this.player.serverLevel();
        BlockPos blockpos = pPacket.getPos();
        if (serverlevel.hasChunkAt(blockpos)) {
            BlockEntity blockentity = serverlevel.getBlockEntity(blockpos);
            if (!(blockentity instanceof SignBlockEntity)) {
                return;
            }

            SignBlockEntity signblockentity = (SignBlockEntity)blockentity;
            signblockentity.updateSignText(this.player, pPacket.isFrontText(), pFilteredText);
        }

    }

    /**
     * Updates a players' ping statistics
     */
    @Override
    public void handleKeepAlive(ServerboundKeepAlivePacket pPacket) {
        if (this.keepAlivePending && pPacket.getId() == this.keepAliveChallenge) {
            int i = (int)(Util.getMillis() - this.keepAliveTime);
            this.player.latency = (this.player.latency * 3 + i) / 4;
            this.keepAlivePending = false;
        } else if (!this.isSingleplayerOwner()) {
            this.disconnect(Component.translatable("disconnect.timeout"));
        }

    }

    /**
     * Processes a player starting/stopping flying
     */
    @Override
    public void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        this.player.getAbilities().flying = pPacket.isFlying() && this.player.getAbilities().mayfly;
    }

    /**
     * Updates serverside copy of client settings: language, render distance, chat visibility, chat colours, difficulty,
     * and whether to show the cape
     */
    @Override
    public void handleClientInformation(ServerboundClientInformationPacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        this.player.updateOptions(pPacket);
    }

    /**
     * Synchronizes serverside and clientside book contents and signing
     */
    @Override
    public void handleCustomPayload(ServerboundCustomPayloadPacket pPacket) {
        net.minecraftforge.network.NetworkHooks.onCustomPayload(pPacket, this.connection);
    }

    @Override
    public void handleChangeDifficulty(ServerboundChangeDifficultyPacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        if (this.player.hasPermissions(2) || this.isSingleplayerOwner()) {
            this.server.setDifficulty(pPacket.getDifficulty(), false);
        }
    }

    @Override
    public void handleLockDifficulty(ServerboundLockDifficultyPacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        if (this.player.hasPermissions(2) || this.isSingleplayerOwner()) {
            this.server.setDifficultyLocked(pPacket.isLocked());
        }
    }

    @Override
    public void handleChatSessionUpdate(ServerboundChatSessionUpdatePacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
        RemoteChatSession.Data remotechatsession$data = pPacket.chatSession();
        ProfilePublicKey.Data profilepublickey$data = this.chatSession != null ? this.chatSession.profilePublicKey().data() : null;
        ProfilePublicKey.Data profilepublickey$data1 = remotechatsession$data.profilePublicKey();
        if (!Objects.equals(profilepublickey$data, profilepublickey$data1)) {
            if (profilepublickey$data != null && profilepublickey$data1.expiresAt().isBefore(profilepublickey$data.expiresAt())) {
                this.disconnect(ProfilePublicKey.EXPIRED_PROFILE_PUBLIC_KEY);
            } else {
                try {
                    SignatureValidator signaturevalidator = this.server.getProfileKeySignatureValidator();
                    if (signaturevalidator == null) {
                        LOGGER.warn("Ignoring chat session from {} due to missing Services public key", this.player.getGameProfile().getName());
                        return;
                    }

                    this.resetPlayerChatState(remotechatsession$data.validate(this.player.getGameProfile(), signaturevalidator, Duration.ZERO));
                } catch (ProfilePublicKey.ValidationException profilepublickey$validationexception) {
                    LOGGER.error("Failed to validate profile key: {}", profilepublickey$validationexception.getMessage());
                    this.disconnect(profilepublickey$validationexception.getComponent());
                }

            }
        }
    }

    private void resetPlayerChatState(RemoteChatSession pChatSession) {
        this.chatSession = pChatSession;
        this.signedMessageDecoder = pChatSession.createMessageDecoder(this.player.getUUID());
        this.chatMessageChain.append((p_253488_) -> {
            this.player.setChatSession(pChatSession);
            this.server.getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.INITIALIZE_CHAT), List.of(this.player)));
            return CompletableFuture.completedFuture(null);
        });
    }
}
