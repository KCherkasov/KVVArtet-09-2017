package gamemechanics.resources.pcg;

import gamemechanics.interfaces.AliveEntity;
import gamemechanics.interfaces.EquipableItem;
import gamemechanics.resources.pcg.items.ItemBlueprint;
import gamemechanics.resources.pcg.items.ItemsFactory;
import gamemechanics.resources.pcg.npcs.NpcBlueprint;
import gamemechanics.resources.pcg.npcs.NpcsFactory;

import javax.validation.constraints.NotNull;

public interface PcgContentFactory {
    EquipableItem makeItem(@NotNull ItemBlueprint blueprint);

    EquipableItem makeItem(@NotNull Integer level);

    AliveEntity makeNpc(@NotNull NpcBlueprint blueprint);

    AliveEntity makeNpc(@NotNull Integer level);

    ItemsFactory getItemsFactory();

    NpcsFactory getNpcsFactory();
}
