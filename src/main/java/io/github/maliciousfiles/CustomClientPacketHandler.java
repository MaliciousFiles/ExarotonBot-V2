package io.github.maliciousfiles;

import io.netty.buffer.Unpooled;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;

import java.util.UUID;

@SuppressWarnings("NullableProblems")
public class CustomClientPacketHandler implements ClientGamePacketListener {

    private final Connection connection;

    public CustomClientPacketHandler(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void handleAddEntity(ClientboundAddEntityPacket clientboundAddEntityPacket) {}

    @Override
    public void handleAddExperienceOrb(ClientboundAddExperienceOrbPacket clientboundAddExperienceOrbPacket) {}

    @Override
    public void handleAddVibrationSignal(ClientboundAddVibrationSignalPacket clientboundAddVibrationSignalPacket) {}

    @Override
    public void handleAddMob(ClientboundAddMobPacket clientboundAddMobPacket) {}

    @Override
    public void handleAddObjective(ClientboundSetObjectivePacket clientboundSetObjectivePacket) {}

    @Override
    public void handleAddPainting(ClientboundAddPaintingPacket clientboundAddPaintingPacket) {}

    @Override
    public void handleAddPlayer(ClientboundAddPlayerPacket clientboundAddPlayerPacket) {}

    @Override
    public void handleAnimate(ClientboundAnimatePacket clientboundAnimatePacket) {}

    @Override
    public void handleAwardStats(ClientboundAwardStatsPacket clientboundAwardStatsPacket) {}

    @Override
    public void handleAddOrRemoveRecipes(ClientboundRecipePacket clientboundRecipePacket) {}

    @Override
    public void handleBlockDestruction(ClientboundBlockDestructionPacket clientboundBlockDestructionPacket) {}

    @Override
    public void handleOpenSignEditor(ClientboundOpenSignEditorPacket clientboundOpenSignEditorPacket) {}

    @Override
    public void handleBlockEntityData(ClientboundBlockEntityDataPacket clientboundBlockEntityDataPacket) {}

    @Override
    public void handleBlockEvent(ClientboundBlockEventPacket clientboundBlockEventPacket) {}

    @Override
    public void handleBlockUpdate(ClientboundBlockUpdatePacket clientboundBlockUpdatePacket) {}

    @Override
    public void handleChat(ClientboundChatPacket clientboundChatPacket) {
        String message = clientboundChatPacket.getMessage().getString();

        if (message.toLowerCase().contains(ExarotonBot.NAME.toLowerCase()) || (clientboundChatPacket.getMessage().getStyle().isItalic() && message.contains(" whispers to you: "))) {
            ExarotonBot.notify("Chat Message", message);
        }
    }

    @Override
    public void handleChunkBlocksUpdate(ClientboundSectionBlocksUpdatePacket clientboundSectionBlocksUpdatePacket) {}

    @Override
    public void handleMapItemData(ClientboundMapItemDataPacket clientboundMapItemDataPacket) {}

    @Override
    public void handleContainerClose(ClientboundContainerClosePacket clientboundContainerClosePacket) {}

    @Override
    public void handleContainerContent(ClientboundContainerSetContentPacket clientboundContainerSetContentPacket) {}

    @Override
    public void handleHorseScreenOpen(ClientboundHorseScreenOpenPacket clientboundHorseScreenOpenPacket) {}

    @Override
    public void handleContainerSetData(ClientboundContainerSetDataPacket clientboundContainerSetDataPacket) {}

    @Override
    public void handleContainerSetSlot(ClientboundContainerSetSlotPacket clientboundContainerSetSlotPacket) {}

    public void handleCustomPayload(ClientboundCustomPayloadPacket clientboundCustomPayloadPacket) {}

    @Override
    public void handleDisconnect(ClientboundDisconnectPacket clientboundDisconnectPacket) {
        System.out.println("handleDisconnect: "+clientboundDisconnectPacket.getReason().getString());
        ExarotonBot.disconnect(clientboundDisconnectPacket.getReason().getString());
    }

    @Override
    public void handleEntityEvent(ClientboundEntityEventPacket clientboundEntityEventPacket) {}

    @Override
    public void handleEntityLinkPacket(ClientboundSetEntityLinkPacket clientboundSetEntityLinkPacket) {}

    @Override
    public void handleSetEntityPassengersPacket(ClientboundSetPassengersPacket clientboundSetPassengersPacket) {}

    @Override
    public void handleExplosion(ClientboundExplodePacket clientboundExplodePacket) {}

    @Override
    public void handleGameEvent(ClientboundGameEventPacket clientboundGameEventPacket) {}

    @Override
    public void handleKeepAlive(ClientboundKeepAlivePacket clientboundKeepAlivePacket) {
        connection.send(new ServerboundKeepAlivePacket(clientboundKeepAlivePacket.getId()));
    }

    @Override
    public void handleLevelChunkWithLight(ClientboundLevelChunkWithLightPacket clientboundLevelChunkWithLightPacket) {}

    @Override
    public void handleForgetLevelChunk(ClientboundForgetLevelChunkPacket clientboundForgetLevelChunkPacket) {}

    @Override
    public void handleLevelEvent(ClientboundLevelEventPacket clientboundLevelEventPacket) {}

    @Override
    public void handleLogin(ClientboundLoginPacket clientboundLoginPacket) {
        connection.send(new ServerboundClientInformationPacket("en_us", 12, ChatVisiblity.FULL, true, 255, HumanoidArm.RIGHT, false, true));
        connection.send(new ServerboundCustomPayloadPacket(ServerboundCustomPayloadPacket.BRAND, (new FriendlyByteBuf(Unpooled.buffer())).writeUtf(ClientBrandRetriever.getClientModName())));
        System.out.println("logged in");
    }

    @Override
    public void handleMoveEntity(ClientboundMoveEntityPacket clientboundMoveEntityPacket) {}

    @Override
    public void handleMovePlayer(ClientboundPlayerPositionPacket clientboundPlayerPositionPacket) {}

    @Override
    public void handleParticleEvent(ClientboundLevelParticlesPacket clientboundLevelParticlesPacket) {}

    @Override
    public void handlePing(ClientboundPingPacket clientboundPingPacket) {
        connection.send(new ServerboundPongPacket(clientboundPingPacket.getId()));
    }

    @Override
    public void handlePlayerAbilities(ClientboundPlayerAbilitiesPacket clientboundPlayerAbilitiesPacket) {}

    @Override
    public void handlePlayerInfo(ClientboundPlayerInfoPacket clientboundPlayerInfoPacket) {
        if (clientboundPlayerInfoPacket.getAction().equals(ClientboundPlayerInfoPacket.Action.ADD_PLAYER)) {
            clientboundPlayerInfoPacket.getEntries().forEach(e -> {
                ExarotonBot.PLAYER_NAMES.put(e.getProfile().getId(), e.getProfile().getName());
                if (!UUID.fromString(ExarotonBot.UUID).equals(e.getProfile().getId())) {
                    connection.send(new ServerboundChatPacket("/msg " + e.getProfile().getName() + " " + ExarotonBot.MESSAGE));
                    System.out.println("player joined");
                    ExarotonBot.notify("Player Joined", e.getProfile().getName()+" joined the game.");
                }
            });
        } else if (clientboundPlayerInfoPacket.getAction().equals(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER)) {
            clientboundPlayerInfoPacket.getEntries().forEach(e -> ExarotonBot.notify("Player Left", ExarotonBot.PLAYER_NAMES.get(e.getProfile().getId())+" left the game."));
        }
    }

    @Override
    public void handleRemoveEntities(ClientboundRemoveEntitiesPacket clientboundRemoveEntitiesPacket) {}

    @Override
    public void handleRemoveMobEffect(ClientboundRemoveMobEffectPacket clientboundRemoveMobEffectPacket) {}

    @Override
    public void handleRespawn(ClientboundRespawnPacket clientboundRespawnPacket) {}

    @Override
    public void handleRotateMob(ClientboundRotateHeadPacket clientboundRotateHeadPacket) {}

    @Override
    public void handleSetCarriedItem(ClientboundSetCarriedItemPacket clientboundSetCarriedItemPacket) {}

    @Override
    public void handleSetDisplayObjective(ClientboundSetDisplayObjectivePacket clientboundSetDisplayObjectivePacket) {}

    @Override
    public void handleSetEntityData(ClientboundSetEntityDataPacket clientboundSetEntityDataPacket) {}

    @Override
    public void handleSetEntityMotion(ClientboundSetEntityMotionPacket clientboundSetEntityMotionPacket) {}

    @Override
    public void handleSetEquipment(ClientboundSetEquipmentPacket clientboundSetEquipmentPacket) {}

    private int levels = 0;

    @Override
    public void handleSetExperience(ClientboundSetExperiencePacket clientboundSetExperiencePacket) {
        if (levels != clientboundSetExperiencePacket.getExperienceLevel()) {
            levels = clientboundSetExperiencePacket.getExperienceLevel();
            System.out.println("You now have " + levels + " level(s)!");
            ExarotonBot.notify("Levels", "You now have "+levels+" level(s)");
        }
    }

    private float health = 0;

    @Override
    public void handleSetHealth(ClientboundSetHealthPacket clientboundSetHealthPacket) {
        if (clientboundSetHealthPacket.getHealth() != health) {
            if (clientboundSetHealthPacket.getHealth() < health) {
                System.out.println("DAMAGED!");
                connection.send(new ServerboundChatPacket("Whoever hurt me: that's very rude >:("));
                ExarotonBot.disconnect("Took damage!");
                System.exit(0);
            }
            health = clientboundSetHealthPacket.getHealth();
        }
    }

    @Override
    public void handleSetPlayerTeamPacket(ClientboundSetPlayerTeamPacket clientboundSetPlayerTeamPacket) {}

    @Override
    public void handleSetScore(ClientboundSetScorePacket clientboundSetScorePacket) {}

    @Override
    public void handleSetSpawn(ClientboundSetDefaultSpawnPositionPacket clientboundSetDefaultSpawnPositionPacket) {}

    @Override
    public void handleSetTime(ClientboundSetTimePacket clientboundSetTimePacket) {}

    @Override
    public void handleSoundEvent(ClientboundSoundPacket clientboundSoundPacket) {}

    @Override
    public void handleSoundEntityEvent(ClientboundSoundEntityPacket clientboundSoundEntityPacket) {}

    @Override
    public void handleCustomSoundEvent(ClientboundCustomSoundPacket clientboundCustomSoundPacket) {}

    @Override
    public void handleTakeItemEntity(ClientboundTakeItemEntityPacket clientboundTakeItemEntityPacket) {}

    @Override
    public void handleTeleportEntity(ClientboundTeleportEntityPacket clientboundTeleportEntityPacket) {}

    @Override
    public void handleUpdateAttributes(ClientboundUpdateAttributesPacket clientboundUpdateAttributesPacket) {}

    @Override
    public void handleUpdateMobEffect(ClientboundUpdateMobEffectPacket clientboundUpdateMobEffectPacket) {}

    @Override
    public void handleUpdateTags(ClientboundUpdateTagsPacket clientboundUpdateTagsPacket) {}

    @Override
    public void handlePlayerCombatEnd(ClientboundPlayerCombatEndPacket clientboundPlayerCombatEndPacket) {}

    @Override
    public void handlePlayerCombatEnter(ClientboundPlayerCombatEnterPacket clientboundPlayerCombatEnterPacket) {}

    @Override
    public void handlePlayerCombatKill(ClientboundPlayerCombatKillPacket clientboundPlayerCombatKillPacket) {}

    @Override
    public void handleChangeDifficulty(ClientboundChangeDifficultyPacket clientboundChangeDifficultyPacket) {}

    @Override
    public void handleSetCamera(ClientboundSetCameraPacket clientboundSetCameraPacket) {}

    @Override
    public void handleInitializeBorder(ClientboundInitializeBorderPacket clientboundInitializeBorderPacket) {}

    @Override
    public void handleSetBorderLerpSize(ClientboundSetBorderLerpSizePacket clientboundSetBorderLerpSizePacket) {}

    @Override
    public void handleSetBorderSize(ClientboundSetBorderSizePacket clientboundSetBorderSizePacket) {}

    @Override
    public void handleSetBorderWarningDelay(ClientboundSetBorderWarningDelayPacket clientboundSetBorderWarningDelayPacket) {}

    @Override
    public void handleSetBorderWarningDistance(ClientboundSetBorderWarningDistancePacket clientboundSetBorderWarningDistancePacket) {}

    @Override
    public void handleSetBorderCenter(ClientboundSetBorderCenterPacket clientboundSetBorderCenterPacket) {}

    @Override
    public void handleTabListCustomisation(ClientboundTabListPacket clientboundTabListPacket) {}

    @Override
    public void handleResourcePack(ClientboundResourcePackPacket clientboundResourcePackPacket) {}

    @Override
    public void handleBossUpdate(ClientboundBossEventPacket clientboundBossEventPacket) {}

    @Override
    public void handleItemCooldown(ClientboundCooldownPacket clientboundCooldownPacket) {}

    @Override
    public void handleMoveVehicle(ClientboundMoveVehiclePacket clientboundMoveVehiclePacket) {}

    @Override
    public void handleUpdateAdvancementsPacket(ClientboundUpdateAdvancementsPacket clientboundUpdateAdvancementsPacket) {}

    @Override
    public void handleSelectAdvancementsTab(ClientboundSelectAdvancementsTabPacket clientboundSelectAdvancementsTabPacket) {}

    @Override
    public void handlePlaceRecipe(ClientboundPlaceGhostRecipePacket clientboundPlaceGhostRecipePacket) {}

    @Override
    public void handleCommands(ClientboundCommandsPacket clientboundCommandsPacket) {}

    @Override
    public void handleStopSoundEvent(ClientboundStopSoundPacket clientboundStopSoundPacket) {}

    @Override
    public void handleCommandSuggestions(ClientboundCommandSuggestionsPacket clientboundCommandSuggestionsPacket) {}

    @Override
    public void handleUpdateRecipes(ClientboundUpdateRecipesPacket clientboundUpdateRecipesPacket) {}

    @Override
    public void handleLookAt(ClientboundPlayerLookAtPacket clientboundPlayerLookAtPacket) {}

    @Override
    public void handleTagQueryPacket(ClientboundTagQueryPacket clientboundTagQueryPacket) {}

    @Override
    public void handleLightUpdatePacket(ClientboundLightUpdatePacket clientboundLightUpdatePacket) {}

    @Override
    public void handleOpenBook(ClientboundOpenBookPacket clientboundOpenBookPacket) {}

    @Override
    public void handleOpenScreen(ClientboundOpenScreenPacket clientboundOpenScreenPacket) {}

    @Override
    public void handleMerchantOffers(ClientboundMerchantOffersPacket clientboundMerchantOffersPacket) {}

    @Override
    public void handleSetChunkCacheRadius(ClientboundSetChunkCacheRadiusPacket clientboundSetChunkCacheRadiusPacket) {}

    @Override
    public void handleSetSimulationDistance(ClientboundSetSimulationDistancePacket clientboundSetSimulationDistancePacket) {}

    @Override
    public void handleSetChunkCacheCenter(ClientboundSetChunkCacheCenterPacket clientboundSetChunkCacheCenterPacket) {}

    @Override
    public void handleBlockBreakAck(ClientboundBlockBreakAckPacket clientboundBlockBreakAckPacket) {}

    @Override
    public void setActionBarText(ClientboundSetActionBarTextPacket clientboundSetActionBarTextPacket) {}

    @Override
    public void setSubtitleText(ClientboundSetSubtitleTextPacket clientboundSetSubtitleTextPacket) {}

    @Override
    public void setTitleText(ClientboundSetTitleTextPacket clientboundSetTitleTextPacket) {}

    @Override
    public void setTitlesAnimation(ClientboundSetTitlesAnimationPacket clientboundSetTitlesAnimationPacket) {}

    @Override
    public void handleTitlesClear(ClientboundClearTitlesPacket clientboundClearTitlesPacket) {}

    @Override
    public void onDisconnect(Component component) {
        System.out.println("onDisconnect: "+component.getString());
        ExarotonBot.disconnect(component.getString());
    }

    @Override
    public Connection getConnection() {
        return connection;
    }
}
