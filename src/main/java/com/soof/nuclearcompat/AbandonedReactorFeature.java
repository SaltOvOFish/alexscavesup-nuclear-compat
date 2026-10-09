package com.soof.nuclearcompat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.ArrayList;
import java.util.List;

/**
 * 废弃反应堆结构 feature。
 *
 * 不依赖 Alex's Caves 的 feature 类型注册名（Up 为 {@code alexscaves:toxic_ruins}、
 * Neo 为 {@code alexscaves:underground_ruins}），从而同时兼容两个移植版。
 * 放置逻辑参考 Alex's Caves 的 {@code UndergroundRuinsFeature}。
 */
public class AbandonedReactorFeature extends Feature<AbandonedReactorFeature.Config> {

    /** 结构底部到下方实心方块的最大允许距离，避免在高空悬空处生成。 */
    private static final int MAX_AIR_GAP = 3;

    /** AC 酸液方块注册名。 */
    private static final ResourceLocation ACID_ID = ResourceLocation.fromNamespaceAndPath("alexscaves", "acid");

    public AbandonedReactorFeature(Codec<Config> codec) {
        super(codec);
    }

    public record Config(
            List<ResourceLocation> structures,
            ResourceKey<LootTable> chestLoot,
            int sinkBy
    ) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.listOf().fieldOf("structures").forGetter(Config::structures),
                ResourceKey.codec(Registries.LOOT_TABLE).fieldOf("chest_loot").forGetter(Config::chestLoot),
                Codec.INT.fieldOf("sink_by").forGetter(Config::sinkBy)
        ).apply(instance, Config::new));
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {
        RandomSource random = context.random();
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        Config config = context.config();

        // 从世界最低点向上到地表，寻找「当前格与上方格均可替换」的位置。
        BlockPos pos = origin.atY(level.getMinBuildHeight() + 3);
        List<BlockPos> candidates = new ArrayList<>();
        int surfaceY = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, origin.getX(), origin.getZ()) - 5;
        while (pos.getY() < surfaceY) {
            if (isReplaceableColumn(level, pos, 11) && hasSolidBelow(level, pos, MAX_AIR_GAP)) {
                candidates.add(pos);
            }
            pos = pos.above();
        }
        if (candidates.isEmpty()) {
            return false;
        }
        BlockPos chosen = candidates.get(random.nextInt(candidates.size()));

        Rotation rotation = Rotation.getRandom(random);
        StructureTemplateManager manager = level.getLevel().getServer().getStructureManager();
        StructureTemplate template = manager.getOrCreate(config.structures().get(random.nextInt(config.structures().size())));

        ChunkPos chunkPos = new ChunkPos(chosen);
        BoundingBox boundingBox = new BoundingBox(
                chunkPos.getMinBlockX() - 16, level.getMinBuildHeight(), chunkPos.getMinBlockZ() - 16,
                chunkPos.getMaxBlockX() + 16, level.getMaxBuildHeight(), chunkPos.getMaxBlockZ() + 16);

        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setRotation(rotation)
                .setBoundingBox(boundingBox)
                .setRandom(random)
                .setLiquidSettings(LiquidSettings.IGNORE_WATERLOGGING);

        Vec3i size = template.getSize(rotation);
        BlockPos placePos = chosen.offset(-size.getX() / 2, config.sinkBy(), -size.getZ() / 2);

        if (!template.placeInWorld(level, placePos, placePos, settings, random, 2)) {
            return false;
        }

        // 处理 data 标记：loot_chest 标记下方的容器设置为战利品箱。
        for (StructureTemplate.StructureBlockInfo info : getDataMarkers(template, placePos, rotation)) {
            if (info.nbt() == null) {
                continue;
            }
            String metadata = info.nbt().getString("metadata");
            if ("loot_chest".equals(metadata)) {
                level.setBlock(info.pos(), Blocks.CAVE_AIR.defaultBlockState(), 3);
                if (level.getBlockEntity(info.pos().below()) instanceof RandomizableContainerBlockEntity container) {
                    container.setLootTable(config.chestLoot(), random.nextLong());
                }
            }
        }

        // 在结构底部生成实心基座（结构水平投影外扩 1 格），向下延伸，中心与结构正对。
        Block baseBlock = BuiltInRegistries.BLOCK.get(
                ResourceLocation.fromNamespaceAndPath("alexscaves", "radrock"));
        if (baseBlock == null) {
            baseBlock = Blocks.STONE;
        }
        BlockState baseState = baseBlock.defaultBlockState();
        BoundingBox box = template.getBoundingBox(settings, placePos);
        for (int x = box.minX() - 1; x <= box.maxX() + 1; x++) {
            for (int z = box.minZ() - 1; z <= box.maxZ() + 1; z++) {
                BlockPos below = new BlockPos(x, box.minY() - 1, z);
                while (below.getY() > level.getMinBuildHeight()) {
                    BlockState s = level.getBlockState(below);
                    if (!s.isAir() && !s.canBeReplaced()) {
                        break;
                    }
                    level.setBlock(below, baseState, 3);
                    below = below.below();
                }
            }
        }

        return true;
    }

    /** 检查 pos 上方 height 格是否都可替换（避免结构生成在封闭岩石内）。 */
    private static boolean isReplaceableColumn(WorldGenLevel level, BlockPos pos, int height) {
        for (int i = 0; i < height; i++) {
            BlockPos p = pos.above(i);
            if (p.getY() >= level.getMaxBuildHeight()) {
                return false;
            }
            BlockState s = level.getBlockState(p);
            if (!s.isAir() && !s.canBeReplaced()) {
                return false;
            }
        }
        return true;
    }

    /** 检查 pos 下方 maxGap 格内是否存在实心方块（避免高空悬空）。 */
    private static boolean hasSolidBelow(WorldGenLevel level, BlockPos pos, int maxGap) {
        BlockPos below = pos.below();
        int airGap = 0;
        while (below.getY() > level.getMinBuildHeight()) {
            BlockState s = level.getBlockState(below);
            if (!s.isAir() && !s.canBeReplaced()) {
                return true; // 实心方块（岩石），有支撑
            }
            if (s.isAir() || !isAcid(s)) {
                airGap++; // 空气或其他可替换方块累计悬空距离
                if (airGap > maxGap) {
                    return false;
                }
            }
            // 酸液：不累计悬空距离，继续向下穿透
            below = below.below();
        }
        return true; // 到世界底部
    }

    private static boolean isAcid(BlockState state) {
        return ACID_ID.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    }

    private static List<StructureTemplate.StructureBlockInfo> getDataMarkers(
            StructureTemplate template, BlockPos pos, Rotation rotation) {
        List<StructureTemplate.StructureBlockInfo> result = new ArrayList<>();
        StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation);
        for (StructureTemplate.StructureBlockInfo info : template.filterBlocks(pos, settings, Blocks.STRUCTURE_BLOCK, true)) {
            if (info.nbt() != null && "DATA".equals(info.nbt().getString("mode"))) {
                result.add(info);
            }
        }
        return result;
    }
}
