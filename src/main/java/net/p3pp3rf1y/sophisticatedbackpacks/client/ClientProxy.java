package net.p3pp3rf1y.sophisticatedbackpacks.client;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.inventory.container.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.client.gui.BackpackScreen;
import net.p3pp3rf1y.sophisticatedbackpacks.client.init.ModBlockColors;
import net.p3pp3rf1y.sophisticatedbackpacks.client.init.ModItemColors;
import net.p3pp3rf1y.sophisticatedbackpacks.client.render.BackpackLayerRenderer;
import net.p3pp3rf1y.sophisticatedbackpacks.common.CommonProxy;
import net.p3pp3rf1y.sophisticatedbackpacks.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedbackpacks.network.BackpackOpenMessage;
import net.p3pp3rf1y.sophisticatedbackpacks.network.PacketHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.network.UpgradeToggleMessage;
import net.p3pp3rf1y.sophisticatedbackpacks.util.RecipeHelper;

import java.util.Map;

import static net.minecraftforge.client.settings.KeyConflictContext.GUI;
import static net.p3pp3rf1y.sophisticatedbackpacks.client.gui.TranslationHelper.translKeybind;
import static net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.EVERLASTING_BACKPACK_ITEM_ENTITY;

@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy {
	private static final int KEY_B = 66;
	private static final int KEY_Z = 90;
	private static final int KEY_X = 88;
	private static final int KEY_UNKNOWN = -1;

	private static final String KEYBIND_SOPHISTICATEDBACKPACKS_CATEGORY = "keybind.sophisticatedbackpacks.category";

	public static final KeyBinding BACKPACK_OPEN_KEYBIND = new KeyBinding(translKeybind("open_backpack"),
			BackpackKeyConflictContext.INSTANCE, InputMappings.Type.KEYSYM.getOrMakeInput(KEY_B), KEYBIND_SOPHISTICATEDBACKPACKS_CATEGORY);

	public static final KeyBinding BACKPACK_TOGGLE_UPGRADE_1 = new KeyBinding(translKeybind("toggle_upgrade_1"),
			KeyConflictContext.UNIVERSAL, KeyModifier.ALT, InputMappings.Type.KEYSYM.getOrMakeInput(KEY_Z), KEYBIND_SOPHISTICATEDBACKPACKS_CATEGORY);
	public static final KeyBinding BACKPACK_TOGGLE_UPGRADE_2 = new KeyBinding(translKeybind("toggle_upgrade_2"),
			KeyConflictContext.UNIVERSAL, KeyModifier.ALT, InputMappings.Type.KEYSYM.getOrMakeInput(KEY_X), KEYBIND_SOPHISTICATEDBACKPACKS_CATEGORY);
	public static final KeyBinding BACKPACK_TOGGLE_UPGRADE_3 = new KeyBinding(translKeybind("toggle_upgrade_3"),
			KeyConflictContext.UNIVERSAL, InputMappings.Type.KEYSYM.getOrMakeInput(KEY_UNKNOWN), KEYBIND_SOPHISTICATEDBACKPACKS_CATEGORY);
	public static final KeyBinding BACKPACK_TOGGLE_UPGRADE_4 = new KeyBinding(translKeybind("toggle_upgrade_4"),
			KeyConflictContext.UNIVERSAL, InputMappings.Type.KEYSYM.getOrMakeInput(KEY_UNKNOWN), KEYBIND_SOPHISTICATEDBACKPACKS_CATEGORY);
	public static final KeyBinding BACKPACK_TOGGLE_UPGRADE_5 = new KeyBinding(translKeybind("toggle_upgrade_5"),
			KeyConflictContext.UNIVERSAL, InputMappings.Type.KEYSYM.getOrMakeInput(KEY_UNKNOWN), KEYBIND_SOPHISTICATEDBACKPACKS_CATEGORY);

	private static final Map<Integer, KeyBinding> UPGRADE_SLOT_TOGGLE_KEYBINDS = ImmutableMap.of(
			0, BACKPACK_TOGGLE_UPGRADE_1,
			1, BACKPACK_TOGGLE_UPGRADE_2,
			2, BACKPACK_TOGGLE_UPGRADE_3,
			3, BACKPACK_TOGGLE_UPGRADE_4,
			4, BACKPACK_TOGGLE_UPGRADE_5
	);

	public static void handleKeyInputEvent(TickEvent.ClientTickEvent event) {
		if (BACKPACK_OPEN_KEYBIND.isPressed()) {
			sendBackpackOpenMessage();
		} else {
			for (Map.Entry<Integer, KeyBinding> slotKeybind : UPGRADE_SLOT_TOGGLE_KEYBINDS.entrySet()) {
				if (slotKeybind.getValue().isPressed()) {
					PacketHandler.sendToServer(new UpgradeToggleMessage(slotKeybind.getKey()));
				}
			}
		}
	}

	private static void sendBackpackOpenMessage() {
		if (!GUI.isActive()) {
			PacketHandler.sendToServer(new BackpackOpenMessage());
		} else {
			BackpackScreen backpackScreen = (BackpackScreen) Minecraft.getInstance().currentScreen;

			if (backpackScreen != null) {
				Slot slot = backpackScreen.getSlotUnderMouse();
				if (slot != null && slot.getStack().getItem() instanceof BackpackItem) {
					PacketHandler.sendToServer(new BackpackOpenMessage(slot.slotNumber));
				}
			}
		}
	}

	@Override
	public void registerHandlers() {
		super.registerHandlers();
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		modBus.addListener(this::loadComplete);
		modBus.addListener(this::clientSetup);
		IEventBus eventBus = MinecraftForge.EVENT_BUS;
		eventBus.addListener(ClientProxy::handleKeyInputEvent);
		eventBus.addListener(ClientProxy::onPlayerJoinServer);
	}

	private void loadComplete(FMLLoadCompleteEvent event) {
		event.enqueueWork(() -> {
			ModItemColors.init();
			ModBlockColors.init();
			registerBackpackLayer();
		});
	}

	private void clientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			ClientRegistry.registerKeyBinding(BACKPACK_OPEN_KEYBIND);
			UPGRADE_SLOT_TOGGLE_KEYBINDS.forEach((slot, keybind) -> ClientRegistry.registerKeyBinding(keybind));
		});
		RenderTypeLookup.setRenderLayer(ModBlocks.BACKPACK.get(), RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(ModBlocks.IRON_BACKPACK.get(), RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(ModBlocks.GOLD_BACKPACK.get(), RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(ModBlocks.DIAMOND_BACKPACK.get(), RenderType.getCutout());
		RenderingRegistry.registerEntityRenderingHandler(EVERLASTING_BACKPACK_ITEM_ENTITY.get(), renderManager -> new ItemRenderer(renderManager, Minecraft.getInstance().getItemRenderer()));
	}

	private void registerBackpackLayer() {
		Map<String, PlayerRenderer> skinMap = Minecraft.getInstance().getRenderManager().getSkinMap();
		PlayerRenderer render = skinMap.get("default");
		render.addLayer(new BackpackLayerRenderer(render));
		render = skinMap.get("slim");
		render.addLayer(new BackpackLayerRenderer(render));
	}

	private static void onPlayerJoinServer(ClientPlayerNetworkEvent.LoggedInEvent evt) {
		//noinspection ConstantConditions - by the time player is joining the world is not null
		RecipeHelper.setWorld(Minecraft.getInstance().world);
	}

	private static class BackpackKeyConflictContext implements IKeyConflictContext {
		public static final BackpackKeyConflictContext INSTANCE = new BackpackKeyConflictContext();

		@Override
		public boolean isActive() {
			return !GUI.isActive() || Minecraft.getInstance().currentScreen instanceof BackpackScreen;
		}

		@Override
		public boolean conflicts(IKeyConflictContext other) {
			return this == other;
		}
	}

}
