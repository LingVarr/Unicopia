package com.minelittlepony.unicopia.entity;

import java.util.List;

import net.fabricmc.fabric.api.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.EndBiome;
import net.minecraft.world.biome.ForestBiome;
import net.minecraft.world.biome.MountainsBiome;
import net.minecraft.world.biome.NetherBiome;
import net.minecraft.world.biome.OceanBiome;
import net.minecraft.world.biome.PlainsBiome;
import net.minecraft.world.biome.RiverBiome;

public interface UEntities {
    EntityType<SpellbookEntity> SPELLBOOK = register("spellbook", FabricEntityTypeBuilder.create(EntityCategory.MISC, SpellbookEntity::new)
            .size(EntityDimensions.changing(0.6F, 0.6F)));
    EntityType<SpellcastEntity> MAGIC_SPELL = register("magic_spell", FabricEntityTypeBuilder.create(EntityCategory.MISC, SpellcastEntity::new)
            .size(EntityDimensions.changing(0.6F, 0.25F)));
    EntityType<CloudEntity> CLOUD = register("cloud", FabricEntityTypeBuilder.create(EntityCategory.CREATURE, CloudEntity::new)
            .size(CloudEntity.BASE_DIMENSIONS));
    EntityType<WildCloudEntity> WILD_CLOUD = register("wild_cloud", FabricEntityTypeBuilder.create(EntityCategory.CREATURE, WildCloudEntity::new)
            .size(CloudEntity.BASE_DIMENSIONS));
    EntityType<RacingCloudEntity> RACING_CLOUD = register("racing_cloud", FabricEntityTypeBuilder.create(EntityCategory.CREATURE, RacingCloudEntity::new)
            .size(CloudEntity.BASE_DIMENSIONS));
    EntityType<ConstructionCloudEntity> CONSTRUCTION_CLOUD = register("construction_cloud", FabricEntityTypeBuilder.create(EntityCategory.CREATURE, ConstructionCloudEntity::new)
            .size(CloudEntity.BASE_DIMENSIONS));

    EntityType<RainbowEntity> RAINBOW = register("rainbow", FabricEntityTypeBuilder.create(EntityCategory.AMBIENT, RainbowEntity::new)
            .size(EntityDimensions.fixed(1, 1)));

    EntityType<CucoonEntity> CUCOON = register("cucoon", FabricEntityTypeBuilder.create(EntityCategory.MISC, CucoonEntity::new)
            .size(EntityDimensions.changing(1.5F, 1.6F)));

    EntityType<ButterflyEntity> BUTTERFLY = register("butterfly", FabricEntityTypeBuilder.create(EntityCategory.AMBIENT, ButterflyEntity::new)
            .size(EntityDimensions.fixed(1, 1)));

    EntityType<ProjectileEntity> THROWN_ITEM = register("thrown_item", FabricEntityTypeBuilder.<ProjectileEntity>create(EntityCategory.MISC, ProjectileEntity::new)
            .trackable(100, 2)
            .size(EntityDimensions.fixed(0.25F, 0.25F)));
    EntityType<SpearEntity> THROWN_SPEAR = register("thrown_spear", FabricEntityTypeBuilder.<SpearEntity>create(EntityCategory.MISC, SpearEntity::new)
            .trackable(100, 2)
            .size(EntityDimensions.fixed(0.6F, 0.6F)));

    static <T extends Entity> EntityType<T> register(String name, FabricEntityTypeBuilder<T> builder) {
        return Registry.register(Registry.ENTITY_TYPE, new Identifier("unicopia", name), builder.build());
    }

    static void bootstrap() {
        Registry.BIOME.forEach(biome -> {
            if (!(biome instanceof NetherBiome || biome instanceof EndBiome)) {
                addifAbsent(biome.getEntitySpawnList(EntityCategory.AMBIENT), biome instanceof OceanBiome ? WildCloudEntity.SPAWN_ENTRY_OCEAN : WildCloudEntity.SPAWN_ENTRY_LAND);
                addifAbsent(biome.getEntitySpawnList(EntityCategory.CREATURE), RainbowEntity.SPAWN_ENTRY);
            }

            if (biome instanceof PlainsBiome
                || biome instanceof RiverBiome
                || biome instanceof MountainsBiome
                || biome instanceof ForestBiome) {
                addifAbsent(biome.getEntitySpawnList(EntityCategory.AMBIENT), ButterflyEntity.SPAWN_ENTRY);
            }
        });
    }

    static <T> void addifAbsent(List<T> entries, T entry) {
        if (!entries.contains(entry)) {
            entries.add(entry);
        }
    }
}
