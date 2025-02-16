package com.sammy.malum.common.item.curiosities.curios.sets.weeping;

import com.google.common.collect.*;
import com.sammy.malum.common.item.*;
import com.sammy.malum.common.item.curiosities.curios.*;
import com.sammy.malum.registry.common.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import top.theillusivec4.curios.api.*;

public class CurioEndlessRing extends MalumCurioItem implements IVoidItem {
    public CurioEndlessRing(Properties builder) {
        super(builder, MalumTrinketType.VOID);
    }

    @Override
    public void addAttributeModifiers(Multimap<Attribute, AttributeModifier> map, SlotContext slotContext, ItemStack stack) {
        addAttributeModifier(map, AttributeRegistry.RESERVE_STAFF_CHARGES.get(), uuid -> new AttributeModifier(uuid,
                "Curio Reserve Staff Charges", 3f, AttributeModifier.Operation.ADDITION));
    }
}