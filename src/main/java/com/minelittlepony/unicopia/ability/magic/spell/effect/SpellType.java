package com.minelittlepony.unicopia.ability.magic.spell.effect;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Affine;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.ChangelingFeedingSpell;
import com.minelittlepony.unicopia.ability.magic.spell.DispersableDisguiseSpell;
import com.minelittlepony.unicopia.ability.magic.spell.RainboomAbilitySpell;
import com.minelittlepony.unicopia.ability.magic.spell.PlaceableSpell;
import com.minelittlepony.unicopia.ability.magic.spell.RageAbilitySpell;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.ThrowableSpell;
import com.minelittlepony.unicopia.ability.magic.spell.TimeControlAbilitySpell;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.item.GemstoneItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.util.RegistryUtils;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.command.ServerCommandSource;

public final class SpellType<T extends Spell> implements Affine, SpellPredicate<T> {
    public static final Identifier EMPTY_ID = Unicopia.id("none");
    public static final SpellType<?> EMPTY_KEY = new SpellType<>(EMPTY_ID, Affinity.NEUTRAL, 0xFFFFFF, false, GemstoneItem.Shape.ROUND, SpellTraits.EMPTY, t -> null);

    public static final Registry<SpellType<?>> REGISTRY = RegistryUtils.createSimple(Unicopia.id("spells"));
    public static final RegistryKey<? extends Registry<SpellType<?>>> REGISTRY_KEY = REGISTRY.getKey();

    private static final DynamicCommandExceptionType UNKNOWN_SPELL_TYPE_EXCEPTION = new DynamicCommandExceptionType(id -> Text.translatable("spell_type.unknown", id));

    public static final SpellType<PlaceableSpell> PLACED_SPELL = register("placed", Affinity.NEUTRAL, 0, false, GemstoneItem.Shape.DONUT, SpellTraits.EMPTY, PlaceableSpell::new);
    public static final SpellType<ThrowableSpell> THROWN_SPELL = register("thrown", Affinity.NEUTRAL, 0, false, GemstoneItem.Shape.DONUT, SpellTraits.EMPTY, ThrowableSpell::new);

    public static final SpellType<DispersableDisguiseSpell> CHANGELING_DISGUISE = register("disguise", Affinity.BAD, 0x19E48E, false, GemstoneItem.Shape.ARROW, SpellTraits.EMPTY, DispersableDisguiseSpell::new);
    public static final SpellType<ChangelingFeedingSpell> FEED = register("feed", Affinity.BAD, 0xBDBDF9, false, GemstoneItem.Shape.ARROW, SpellTraits.EMPTY, ChangelingFeedingSpell::new);
    public static final SpellType<RainboomAbilitySpell> RAINBOOM = register("rainboom", Affinity.GOOD, 0xBDBDF9, false, GemstoneItem.Shape.ROCKET, SpellTraits.EMPTY, RainboomAbilitySpell::new);
    public static final SpellType<RageAbilitySpell> RAGE = register("rage", Affinity.GOOD, 0xBDBDF9, false, GemstoneItem.Shape.FLAME, SpellTraits.EMPTY, RageAbilitySpell::new);
    public static final SpellType<TimeControlAbilitySpell> TIME_CONTROL = register("time_control", Affinity.GOOD, 0xBDBDF9, false, GemstoneItem.Shape.STAR, SpellTraits.EMPTY, TimeControlAbilitySpell::new);

    public static final SpellType<IceSpell> FROST = register("frost", Affinity.GOOD, 0xEABBFF, true, GemstoneItem.Shape.TRIANGLE, IceSpell.DEFAULT_TRAITS, IceSpell::new);
    public static final SpellType<ChillingBreathSpell> CHILLING_BREATH = register("chilling_breath", Affinity.NEUTRAL, 0xFFEAFF, true, GemstoneItem.Shape.TRIANGLE, ChillingBreathSpell.DEFAULT_TRAITS, ChillingBreathSpell::new);
    public static final SpellType<ScorchSpell> SCORCH = register("scorch", Affinity.BAD, 0xF8EC1F, true, GemstoneItem.Shape.FLAME, ScorchSpell.DEFAULT_TRAITS, ScorchSpell::new);
    public static final SpellType<FireSpell> FLAME = register("flame", Affinity.GOOD, 0xFFBB99, true, GemstoneItem.Shape.FLAME, FireSpell.DEFAULT_TRAITS, FireSpell::new);
    public static final SpellType<InfernoSpell> INFERNAL = register("infernal", Affinity.BAD, 0xFFAA00, true, GemstoneItem.Shape.FLAME, InfernoSpell.DEFAULT_TRAITS, InfernoSpell::new);
    public static final SpellType<ShieldSpell> SHIELD = register("shield", Affinity.NEUTRAL, 0x66CDAA, true, GemstoneItem.Shape.SHIELD, ShieldSpell.DEFAULT_TRAITS, ShieldSpell::new);
    public static final SpellType<AreaProtectionSpell> ARCANE_PROTECTION = register("arcane_protection", Affinity.BAD, 0x99CDAA, true, GemstoneItem.Shape.SHIELD, AreaProtectionSpell.DEFAULT_TRAITS, AreaProtectionSpell::new);
    public static final SpellType<AttractiveSpell> VORTEX = register("vortex", Affinity.NEUTRAL, 0xFFEA88, true, GemstoneItem.Shape.VORTEX, AttractiveSpell.DEFAULT_TRAITS, AttractiveSpell::new);
    public static final SpellType<DarkVortexSpell> DARK_VORTEX = register("dark_vortex", Affinity.BAD, 0xA33333, true, GemstoneItem.Shape.VORTEX, DarkVortexSpell.DEFAULT_TRAITS, DarkVortexSpell::new);
    public static final SpellType<NecromancySpell> NECROMANCY = register("necromancy", Affinity.BAD, 0xFA3A3A, true, GemstoneItem.Shape.SKULL, SpellTraits.EMPTY, NecromancySpell::new);
    public static final SpellType<SiphoningSpell> SIPHONING = register("siphoning", Affinity.NEUTRAL, 0xFFA3AA, true, GemstoneItem.Shape.LAMBDA, SpellTraits.EMPTY, SiphoningSpell::new);
    public static final SpellType<DisperseIllusionSpell> REVEALING = register("reveal", Affinity.GOOD, 0xFFFFAF, true, GemstoneItem.Shape.CROSS, SpellTraits.EMPTY, DisperseIllusionSpell::new);
    public static final SpellType<AwkwardSpell> AWKWARD = register("awkward", Affinity.GOOD, 0x3A59FF, true, GemstoneItem.Shape.ICE, SpellTraits.EMPTY, AwkwardSpell::new);
    public static final SpellType<TransformationSpell> TRANSFORMATION = register("transformation", Affinity.GOOD, 0x19E48E, true, GemstoneItem.Shape.BRUSH, SpellTraits.EMPTY, TransformationSpell::new);
    public static final SpellType<FeatherFallSpell> FEATHER_FALL = register("feather_fall", Affinity.GOOD, 0x00EEFF, true, GemstoneItem.Shape.LAMBDA, FeatherFallSpell.DEFAULT_TRAITS, FeatherFallSpell::new);
    public static final SpellType<CatapultSpell> CATAPULT = register("catapult", Affinity.GOOD, 0x22FF00, true, GemstoneItem.Shape.ROCKET, CatapultSpell.DEFAULT_TRAITS, CatapultSpell::new);
    public static final SpellType<FireBoltSpell> FIRE_BOLT = register("fire_bolt", Affinity.GOOD, 0xFF8811, true, GemstoneItem.Shape.FLAME, FireBoltSpell.DEFAULT_TRAITS, FireBoltSpell::new);
    public static final SpellType<LightSpell> LIGHT = register("light", Affinity.GOOD, 0xEEFFAA, true, GemstoneItem.Shape.STAR, LightSpell.DEFAULT_TRAITS, LightSpell::new);
    public static final SpellType<DisplacementSpell> DISPLACEMENT = register("displacement", Affinity.NEUTRAL, 0x9900FF, true, GemstoneItem.Shape.BRUSH, PortalSpell.DEFAULT_TRAITS, DisplacementSpell::new);
    public static final SpellType<PortalSpell> PORTAL = register("portal", Affinity.GOOD, 0x99FFFF, true, GemstoneItem.Shape.RING, PortalSpell.DEFAULT_TRAITS, PortalSpell::new);
    public static final SpellType<MimicSpell> MIMIC = register("mimic", Affinity.GOOD, 0xFFFF00, true, GemstoneItem.Shape.ARROW, SpellTraits.EMPTY, MimicSpell::new);
    public static final SpellType<MindSwapSpell> MIND_SWAP = register("mind_swap", Affinity.BAD, 0xF9FF99, true, GemstoneItem.Shape.WAVE, SpellTraits.EMPTY, MindSwapSpell::new);
    public static final SpellType<HydrophobicSpell> HYDROPHOBIC = register("hydrophobic", Affinity.NEUTRAL, 0xF999FF, true, GemstoneItem.Shape.ROCKET, SpellTraits.EMPTY, s -> new HydrophobicSpell(s, FluidTags.WATER));
    public static final SpellType<BubbleSpell> BUBBLE = register("bubble", Affinity.NEUTRAL, 0xF999FF, true, GemstoneItem.Shape.DONUT, BubbleSpell.DEFAULT_TRAITS, BubbleSpell::new);
    public static final SpellType<DispellEvilSpell> DISPEL_EVIL = register("dispel_evil", Affinity.GOOD, 0x00FF00, true, GemstoneItem.Shape.CROSS, DispellEvilSpell.DEFAULT_TRAITS, DispellEvilSpell::new);

    public static void bootstrap() {}

    private final Identifier id;
    private final Affinity affinity;
    private final int color;
    private final boolean obtainable;
    private final GemstoneItem.Shape shape;

    private final Factory<T> factory;

    @Nullable
    private String translationKey;

    private final CustomisedSpellType<T> traited;
    private final SpellTraits traits;

    private final ItemStack defaultStack;

    private SpellType(Identifier id, Affinity affinity, int color, boolean obtainable, GemstoneItem.Shape shape, SpellTraits traits, Factory<T> factory) {
        this.id = id;
        this.affinity = affinity;
        this.color = color;
        this.obtainable = obtainable;
        this.shape = shape;
        this.factory = factory;
        this.traits = traits;
        traited = new CustomisedSpellType<>(this, traits);
        defaultStack = UItems.GEMSTONE.getDefaultStack(this);
    }

    public boolean isObtainable() {
        return obtainable;
    }

    public Identifier getId() {
        return id;
    }

    public ItemStack getDefualtStack() {
        return defaultStack;
    }

    /**
     * Gets the tint for this spell when applied to a gem.
     */
    public int getColor() {
        return color;
    }

    @Override
    public Affinity getAffinity() {
        return affinity;
    }

    public GemstoneItem.Shape getGemShape() {
        return shape;
    }

    public SpellTraits getTraits() {
        return traits;
    }

    public String getTranslationKey() {
        if (translationKey == null) {
            translationKey = Util.createTranslationKey("spell", getId());
        }
        return translationKey;
    }

    public Text getName() {
        return Text.translatable(getTranslationKey());
    }

    public CustomisedSpellType<T> withTraits() {
        return traited;
    }

    public CustomisedSpellType<T> withTraits(SpellTraits traits) {
        return traits.isEmpty() ? withTraits() : new CustomisedSpellType<>(this, traits);
    }

    public Factory<T> getFactory() {
        return factory;
    }

    @Override
    public boolean test(@Nullable Spell spell) {
        return spell != null && spell.getType() == this;
    }

    public void toNbt(NbtCompound tag) {
        tag.putString("effect_id", getId().toString());
    }

    public boolean isEmpty() {
        return this == EMPTY_KEY;
    }

    @Override
    public String toString() {
        return "SpellType[" + getTranslationKey() + "]";
    }

    public static <T extends Spell> SpellType<T> register(String name, Affinity affinity, int color, boolean obtainable, GemstoneItem.Shape shape, SpellTraits traits, Factory<T> factory) {
        return register(Unicopia.id(name), affinity, color, obtainable, shape, traits, factory);
    }

    public static <T extends Spell> SpellType<T> register(Identifier id, Affinity affinity, int color, boolean obtainable, GemstoneItem.Shape shape, SpellTraits traits, Factory<T> factory) {
        return Registry.register(REGISTRY, id, new SpellType<>(id, affinity, color, obtainable, shape, traits, factory));
    }

    @SuppressWarnings("unchecked")
    public static <T extends Spell> SpellType<T> empty() {
        return (SpellType<T>)EMPTY_KEY;
    }

    public static <T extends Spell> SpellType<T> getKey(NbtCompound tag) {
        return getKey(Identifier.tryParse(tag.getString("effect_id")));
    }

    @SuppressWarnings("unchecked")
    public static <T extends Spell> SpellType<T> getKey(@Nullable Identifier id) {
        return (SpellType<T>)REGISTRY.getOrEmpty(id).orElse(EMPTY_KEY);
    }

    public static SpellType<?> fromArgument(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        Identifier id = context.getArgument(name, RegistryKey.class).getValue();
        return REGISTRY.getOrEmpty(id).orElseThrow(() -> UNKNOWN_SPELL_TYPE_EXCEPTION.create(id));
    }

    public interface Factory<T extends Spell> {
        T create(CustomisedSpellType<T> type);
    }
}
