package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.trinkets.TrinketsDelegate;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Wearable;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

public abstract class WearableItem extends Item implements Wearable {

    public WearableItem(FabricItemSettings settings) {
        super(configureEquipmentSlotSupplier(settings));
        DispenserBlock.registerBehavior(this, DISPENSER_BEHAVIOR);
        TrinketsDelegate.getInstance().registerTrinket(this);
    }

    private static FabricItemSettings configureEquipmentSlotSupplier(FabricItemSettings settings) {
        if (TrinketsDelegate.hasTrinkets()) {
            return settings;
        }
        return settings.equipmentSlot(s -> ((WearableItem)s.getItem()).getPreferredSlot(s));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        return TrinketsDelegate.getInstance().getAvailableTrinketSlots(player, TrinketsDelegate.ALL).stream()
                .findAny()
                .filter(slotId -> TrinketsDelegate.getInstance().equipStack(player, slotId, stack))
                .map(slotId -> TypedActionResult.success(stack, world.isClient()))
                .orElseGet(() -> TypedActionResult.fail(stack));
    }

    @Override
    public SoundEvent getEquipSound() {
        return ArmorMaterials.LEATHER.getEquipSound();
    }

    public EquipmentSlot getPreferredSlot(ItemStack stack) {
        return EquipmentSlot.OFFHAND;
    }

    public static boolean dispenseArmor(BlockPointer pointer, ItemStack armor) {
        return pointer.getWorld().getEntitiesByClass(
                    LivingEntity.class,
                    new Box(pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING))),
                    EntityPredicates.EXCEPT_SPECTATOR.and(new EntityPredicates.Equipable(armor))
                )
                .stream()
                .flatMap(entity -> TrinketsDelegate.getInstance()
                        .getAvailableTrinketSlots(entity, TrinketsDelegate.ALL)
                        .stream()
                        .filter(slotId -> TrinketsDelegate.getInstance().equipStack(entity, slotId, armor)))
                .findFirst()
                .isPresent();
    }

    private static final DispenserBehavior DISPENSER_BEHAVIOR = new ItemDispenserBehavior(){
        @Override
        protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
            return dispenseArmor(pointer, stack) ? stack : super.dispenseSilently(pointer, stack);
        }
    };
}
