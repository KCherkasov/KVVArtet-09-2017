package gamemechanics.aliveentities;

import gamemechanics.components.affectors.AffectorCategories;
import gamemechanics.components.properties.Property;
import gamemechanics.components.properties.PropertyCategories;
import gamemechanics.flyweights.CharacterClass;
import gamemechanics.flyweights.CharacterRace;
import gamemechanics.globals.Constants;
import gamemechanics.globals.DigitsPairIndices;
import gamemechanics.interfaces.AliveEntity;
import gamemechanics.interfaces.Bag;
import gamemechanics.interfaces.DecisionMaker;
import gamemechanics.interfaces.Effect;
import gamemechanics.items.containers.CharacterDoll;

import java.util.*;

public abstract class AbstractAliveEntity implements AliveEntity {
    private final String name;
    private final String description;

    private final Map<Integer, Property> properties;
    private final List<Bag> bags;
    private final List<Effect> effects = new ArrayList<>();

    private static class AbstractAliveEntityModel {
        public String name;
        public String description;
        public Map<Integer, Property> properties;
        public List<Bag> bags; // for monsters bags will contain generated loot

        private AbstractAliveEntityModel(String name, String description, Map<Integer, Property> properties, List<Bag> bags) {
            this.name = name;
            this.description = description;
            this.properties = properties;
            this.bags = bags;
        }
    }

    public static class NPCModel extends AbstractAliveEntityModel {
        public List<DecisionMaker> phases;

        public NPCModel(String name, String description, Map<Integer, Property> properties,
                        List<Bag> bags, List<DecisionMaker> phases) {
            super(name, description, properties, bags);
            this.phases = phases;
        }
    }

    public static class UserCharacterModel extends AbstractAliveEntityModel {
        public CharacterClass characterClass;
        public CharacterRace characterRace;
        public CharacterDoll equipment;
        public Map<Integer, Map<Integer, Integer>> perkRanks;

        public UserCharacterModel(String name, String description, Map<Integer, Property> properties,
                                   List<Bag> bags, CharacterClass characterClass,
                                  CharacterRace characterRace, CharacterDoll equipment,
                                  Map<Integer, Map<Integer, Integer>> perkRanks) {
            super(name, description, properties, bags);
            this.characterClass = characterClass;
            this.characterRace = characterRace;
            this.equipment = equipment;
            this.perkRanks = perkRanks;
        }
    }

    public AbstractAliveEntity(NPCModel model) {
        name = model.name;
        description = model.description;
        properties = model.properties;
        bags = model.bags;
    }

    public AbstractAliveEntity(UserCharacterModel model) {
        name = model.name;
        description = model.description;
        properties = model.properties;
        bags = model.bags;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Integer getLevel() {
        return getProperty(PropertyCategories.PC_LEVEL);
    }

    @Override
    public Boolean hasProperty(Integer propertyKind) {
        return properties.containsKey(propertyKind);
    }

    @Override
    public Set<Integer> getAvailableProperties(){
        return properties.keySet();
    }

    @Override
    public Integer getProperty(Integer propertyKind, Integer propertyIndex) {
        if (!hasProperty(propertyKind)) {
            return Integer.MIN_VALUE;
        }
        return properties.get(propertyKind).getProperty(propertyIndex);
    }

    @Override
    public Integer getProperty(Integer propertyKind) {
        if (!hasProperty(propertyKind)) {
            return Integer.MIN_VALUE;
        }
        if (propertyKind == PropertyCategories.PC_BASE_DAMAGE) {
            Random random = new Random(System.currentTimeMillis());
            List<Integer> damage = properties.get(propertyKind).getPropertyList();
            return damage.get(DigitsPairIndices.MIN_VALUE_INDEX)
                    + random.nextInt(damage.get(DigitsPairIndices.MAX_VALUE_INDEX)
                    - damage.get(DigitsPairIndices.MIN_VALUE_INDEX));
        }
        return properties.get(propertyKind).getProperty();
    }

    @Override
    public Boolean addProperty(Integer propertyKind, Property property) {
        if (hasProperty(propertyKind)) {
            return false;
        }
        properties.put(propertyKind, property);
        return true;
    }

    @Override
    public Boolean removeProperty(Integer propertyKind) {
        if (!hasProperty(propertyKind)) {
            return false;
        }
        properties.remove(propertyKind);
        return true;
    }

    @Override
    public Boolean setProperty(Integer propertyKind, Integer propertyValue) {
        if (!hasProperty(propertyKind)) {
            return false;
        }
        return properties.get(propertyKind).setSingleProperty(propertyValue);
    }

    @Override
    public Boolean setProperty(Integer propertyKind, Integer propertyIndex, Integer propertyValue) {
        if (!hasProperty(propertyKind)) {
            return false;
        }
        return properties.get(propertyKind).setSingleProperty(propertyIndex, propertyValue);
    }

    @Override
    public Boolean setProperty(Integer propertyKind, List<Integer> propertyValue) {
        if (!hasProperty(propertyKind)) {
            return false;
        }
        return properties.get(propertyKind).setPropertyList(propertyValue);
    }

    @Override
    public Boolean setProperty(Integer propertyKind, Map<Integer, Integer> propertyValue) {
        if (!hasProperty(propertyKind)) {
            return false;
        }
        return properties.get(propertyKind).setPropertyMap(propertyValue);
    }

    @Override
    public Boolean modifyPropertyByPercentage(Integer propertyKind, Float percentage) {
        if (!hasProperty(propertyKind)) {
            return false;
        }
        return properties.get(propertyKind).modifyByPercentage(percentage);
    }

    @Override
    public Boolean modifyPropertyByPercentage(Integer propertyKind, Integer propertyIndex, Float percentage) {
        if (!hasProperty(propertyKind)) {
            return false;
        }
        return properties.get(propertyKind).modifyByPercentage(propertyIndex, percentage);
    }

    @Override
    public Boolean modifyPropertyByAddition(Integer propertyKind, Integer toAdd) {
        if (!hasProperty(propertyKind)) {
            return false;
        }
        return properties.get(propertyKind).modifyByAddition(toAdd);
    }

    @Override
    public Boolean modifyPropertyByAddition(Integer propertyKind, Integer propertyIndex, Integer toAdd) {
        if (!hasProperty(propertyKind)) {
            return false;
        }
        return properties.get(propertyKind).modifyByAddition(propertyIndex, toAdd);
    }

    @Override
    public Boolean isAlive() {
        return getProperty(PropertyCategories.PC_HITPOINTS, DigitsPairIndices.CURRENT_VALUE_INDEX) > 0;
    }

    @Override
    public void affectHitpoints(Integer amount) {
        if (!isAlive()) {
            return;
        }
        if (amount > 0) {
            Integer missingHitpoints = getProperty(PropertyCategories.PC_HITPOINTS, DigitsPairIndices.MAX_VALUE_INDEX)
                    - getProperty(PropertyCategories.PC_HITPOINTS, DigitsPairIndices.CURRENT_VALUE_INDEX);
            if (missingHitpoints < amount) {
                modifyPropertyByAddition(PropertyCategories.PC_HITPOINTS,
                        DigitsPairIndices.CURRENT_VALUE_INDEX, missingHitpoints);
            } else {
                modifyPropertyByAddition(PropertyCategories.PC_HITPOINTS,
                        DigitsPairIndices.CURRENT_VALUE_INDEX, amount);
            }
        } else {
            modifyPropertyByAddition(PropertyCategories.PC_HITPOINTS, DigitsPairIndices.CURRENT_VALUE_INDEX,
                    Math.round(amount.floatValue() * (Constants.PERCENTAGE_CAP_FLOAT - getDamageReduction())));
        }
    }

    @Override
    public Integer getCash() {
        if (hasProperty(PropertyCategories.PC_CASH_AMOUNT)) {
            return getProperty(PropertyCategories.PC_CASH_AMOUNT);
        }
        return 0;
    }

    @Override
    public Boolean addEffect(Effect effect) {
        if (effect == null) {
            return false;
        }
        effects.add(effect);
        return true;
    }

    @Override
    public Boolean removeEffect(Integer effectIndex) {
        if (effectIndex < 0 || effectIndex >= effects.size()) {
            return false;
        }
        effects.remove(effectIndex.intValue());
        return true;
    }

    @Override
    public Boolean removeAllEffects() {
        if (effects.isEmpty()) {
            return false;
        }
        effects.clear();
        return true;
    }

    @Override
    public Bag getBag(Integer bagIndex) {
        if (bagIndex < 0 || bagIndex > bags.size()) {
            return null;
        }
        return bags.get(bagIndex);
    }

    @Override
    public void update() {
        if (!isAlive()) {
            return;
        }
        tickEffects();
        reduceCooldowns();
    }

    @Override
    public Integer getInitiative() {
        return getProperty(PropertyCategories.PC_INITIATIVE);
    }

    @Override
    public Integer getSpeed() {
        return getProperty(PropertyCategories.PC_SPEED);
    }

    private void tickEffects() {
        for (Effect effect : effects) {
            if (effect.isExpired()) {
                effects.remove(effect);
            }
        }
        for (Effect effect : effects) {
            Integer hitpointsAffection = effect.getAffection(AffectorCategories.AC_OVER_TIME_AFFECTOR);
            if (hitpointsAffection == Integer.MIN_VALUE) {
                hitpointsAffection = 0;
            }
            if (hitpointsAffection != 0) {
                affectHitpoints(hitpointsAffection);
            }
        }
    }

    private void reduceCooldowns() {
        if (!hasProperty(PropertyCategories.PC_ABILITIES_COOLDOWN)) {
            return;
        }
        modifyPropertyByAddition(PropertyCategories.PC_ABILITIES_COOLDOWN, -1);
    }

    protected List<Effect> getEffects() {
        return effects;
    }

    protected abstract Float getDamageReduction();
}