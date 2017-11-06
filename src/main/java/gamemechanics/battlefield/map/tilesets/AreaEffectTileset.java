package gamemechanics.battlefield.map.tilesets;

import gamemechanics.aliveentities.AliveEntitiesCategories;
import gamemechanics.components.properties.PropertyCategories;
import gamemechanics.interfaces.AliveEntity;
import gamemechanics.interfaces.Effect;
import gamemechanics.interfaces.MapNode;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class AreaEffectTileset extends MapNodeTileset {
    private final MapNode sender;
    private final List<Effect> effects;
    private final Integer healthAffection;
    Set<Integer> affectedCategories;

    AreaEffectTileset(@NotNull MapNode sender, @NotNull MapNode target, Integer shape,
                      Integer size, @NotNull List<Effect> effects, Integer healthAffection,
                      Set<Integer> affectedCategories) {
        super(target, shape, calculateDirection(sender, target), size);
        this.sender = sender;
        this.effects = effects;
        this.healthAffection = healthAffection;
    }

    @Override
    public void applyEffects() {
        for (MapNode tile : getTileset()) {
            if (tile.isOccupied()) {
                applyOnInhabitant(sender.getInhabitant(), tile.getInhabitant());
            }
        }
    }

    private void applyOnInhabitant(AliveEntity sender, AliveEntity target) {
        if (affectedCategories.contains(AliveEntitiesCategories.AE_ENEMY) && !areOnSameSide(sender, target)) {
            for (Effect effect : effects) {
                target.addEffect(effect);
            }
            target.affectHitpoints(healthAffection);
        }
        if (affectedCategories.contains(AliveEntitiesCategories.AE_ALLY) && areOnSameSide(sender, target)) {
            for (Effect effect : effects) {
                target.addEffect(effect);
            }
            target.affectHitpoints(healthAffection);
        }
    }

    private Boolean areOnSameSide(AliveEntity lhs, AliveEntity rhs) {
        return Objects.equals(lhs.getProperty(PropertyCategories.PC_SQUAD_ID),
                rhs.getProperty(PropertyCategories.PC_SQUAD_ID));
    }
}