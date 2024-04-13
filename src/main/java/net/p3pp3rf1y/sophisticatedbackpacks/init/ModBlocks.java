package net.p3pp3rf1y.sophisticatedbackpacks.init;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.p3pp3rf1y.sophisticatedbackpacks.SophisticatedBackpacks;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlock;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlockEntity;

public class ModBlocks {
	private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, SophisticatedBackpacks.MOD_ID);
	private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, SophisticatedBackpacks.MOD_ID);

	private ModBlocks() {}

	public static final RegistryObject<BackpackBlock> BACKPACK = BLOCKS.register("backpack", BackpackBlock::new);
	public static final RegistryObject<BackpackBlock> COPPER_BACKPACK = BLOCKS.register("copper_backpack", BackpackBlock::new);
	public static final RegistryObject<BackpackBlock> IRON_BACKPACK = BLOCKS.register("iron_backpack", BackpackBlock::new);
	public static final RegistryObject<BackpackBlock> GOLD_BACKPACK = BLOCKS.register("gold_backpack", BackpackBlock::new);
	public static final RegistryObject<BackpackBlock> DIAMOND_BACKPACK = BLOCKS.register("diamond_backpack", BackpackBlock::new);
	public static final RegistryObject<BackpackBlock> NETHERITE_BACKPACK = BLOCKS.register("netherite_backpack", () -> new BackpackBlock(1200));

	@SuppressWarnings("ConstantConditions") //no datafixer type needed
	public static final RegistryObject<BlockEntityType<BackpackBlockEntity>> BACKPACK_TILE_TYPE = BLOCK_ENTITY_TYPES.register("backpack", () ->
			BlockEntityType.Builder.of(BackpackBlockEntity::new, BACKPACK.get(), COPPER_BACKPACK.get(), IRON_BACKPACK.get(), GOLD_BACKPACK.get(), DIAMOND_BACKPACK.get(), NETHERITE_BACKPACK.get())
					.build(null));

	public static void registerHandlers(IEventBus modBus) {
		BLOCKS.register(modBus);
		BLOCK_ENTITY_TYPES.register(modBus);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, BackpackBlock::playerInteract);
	}
}
