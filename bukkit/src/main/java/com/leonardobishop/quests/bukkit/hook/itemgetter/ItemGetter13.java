package com.leonardobishop.quests.bukkit.hook.itemgetter;

import com.leonardobishop.quests.bukkit.BukkitQuestsPlugin;
import com.leonardobishop.quests.bukkit.util.NamespacedKeyUtils;
import com.leonardobishop.quests.bukkit.util.chat.Chat;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Reads the following:
 * <ul>
 *     <li>type (<b>without</b> data support, <b>without</b> namespace support)</li>
 *     <li>name</li>
 *     <li>lore</li>
 *     <li>enchantments (<b>with</b> namespace support)</li>
 *     <li>item flags</li>
 *     <li>unbreakability (<b>with</b> CraftBukkit support)</li>
 *     <li>attribute modifiers</li>
 * </ul>
 * Requires at least API version 1.13.
 */
@SuppressWarnings("DuplicatedCode")
public class ItemGetter13 extends ItemGetter {

    public ItemGetter13(BukkitQuestsPlugin plugin) {
        super(plugin);
    }

    @Override
    public ItemStack getItem(String path, ConfigurationSection config, Filter... excludes) {
        config = config.getConfigurationSection(path);
        if (config == null) {
            return invalidItemStack;
        }

        List<Filter> filters = Arrays.asList(excludes);

        // type (without data)
        String typeString = config.getString("item", config.getString("type"));
        ItemStack item = getItemStack(typeString);
        ItemMeta meta = item.getItemMeta();

        // skull
        if (meta instanceof SkullMeta skullMeta) {
            String ownerName = config.getString("owner-username");
            String ownerUniqueIdString = config.getString("owner-uuid");
            String ownerBase64 = config.getString("owner-base64");

            plugin.getSkullGetter().apply(skullMeta, ownerName, ownerUniqueIdString, ownerBase64);
        }

        // name
        String nameString = config.getString("name");
        if (nameString != null && !filters.contains(Filter.DISPLAY_NAME)) {
            nameString = Chat.legacyColor(nameString);

            meta.setDisplayName(nameString);
        }

        // lore
        List<String> loreStrings = config.getStringList("lore");
        if (!loreStrings.isEmpty() && !filters.contains(Filter.LORE)) {
            loreStrings = Chat.legacyColor(loreStrings);

            meta.setLore(loreStrings);
        }

        // enchantments
        List<String> enchantmentStrings = config.getStringList("enchantments");
        if (!enchantmentStrings.isEmpty() && !filters.contains(Filter.ENCHANTMENTS)) {
            for (String enchantmentString : enchantmentStrings) {
                String[] parts = enchantmentString.split(":");
                if (parts.length == 0) {
                    continue;
                }

                boolean namespaced = parts.length >= 2 && parts[0].startsWith("(") && parts[1].endsWith(")");

                Enchantment enchantment;
                if (namespaced) {
                    String namespacedKeyString = enchantmentString.substring(1, parts[0].length() + parts[1].length());
                    NamespacedKey namespacedKey = NamespacedKeyUtils.fromString(namespacedKeyString);
                    enchantment = Enchantment.getByKey(namespacedKey);
                } else {
                    enchantment = Enchantment.getByName(parts[0]);
                }

                if (enchantment == null) {
                    continue;
                }

                // (namespace:key):level
                // 0          1    2
                // SOME_ENUM_NAME:level
                // 0              1
                int levelIndex = namespaced ? 2 : 1;

                int level = 1;
                if (parts.length >= levelIndex + 1) {
                    try {
                        level = Integer.parseUnsignedInt(parts[levelIndex]);
                    } catch (NumberFormatException ignored) {
                    }
                }

                meta.addEnchant(enchantment, level, true);
            }
        }

        // item flags
        List<String> itemFlagStrings = config.getStringList("itemflags");
        if (!itemFlagStrings.isEmpty() && !filters.contains(Filter.ITEM_FLAGS)) {
            for (String itemFlagString : itemFlagStrings) {
                ItemFlag itemFlag;
                try {
                    itemFlag = ItemFlag.valueOf(itemFlagString);
                } catch (IllegalArgumentException e) {
                    continue;
                }

                meta.addItemFlags(itemFlag);
            }
        }

        // unbreakability
        Boolean unbreakable = (Boolean) config.get("unbreakable");
        if (unbreakable != null && !filters.contains(Filter.UNBREAKABLE)) {
            meta.setUnbreakable(unbreakable);
        }

        // attribute modifiers
        List<Map<?, ?>> attributeModifierMaps = config.getMapList("attributemodifiers");
        if (!attributeModifierMaps.isEmpty() && !filters.contains(Filter.ATTRIBUTE_MODIFIER)) {
            for (Map<?, ?> attributeModifierMap : attributeModifierMaps) {
                // attribute
                String attributeString = (String) attributeModifierMap.get("attribute");
                Attribute attribute;
                try {
                    attribute = Attribute.valueOf(attributeString);
                } catch (IllegalArgumentException e) {
                    continue;
                }

                // modifier (map)
                Map<?, ?> modifierMap = (Map<?, ?>) attributeModifierMap.get("modifier");
                if (modifierMap == null) {
                    continue;
                }

                // modifier unique id
                String modifierUniqueIdString = (String) modifierMap.get("uuid");
                UUID modifierUniqueId;
                try {
                    modifierUniqueId = UUID.fromString(modifierUniqueIdString);
                } catch (IllegalArgumentException e) {
                    modifierUniqueId = null;
                }

                // modifier name
                String modifierName = (String) modifierMap.get("name");
                if (modifierName == null) {
                    continue;
                }

                // modifier amount
                Object modifierAmountObject = modifierMap.get("amount");
                double modifierAmount;
                if (modifierAmountObject instanceof Number modifierAmountNumber) {
                    modifierAmount = modifierAmountNumber.doubleValue();
                } else {
                    continue;
                }

                // modifier operation
                String modifierOperationString = (String) modifierMap.get("operation");
                AttributeModifier.Operation modifierOperation;
                try {
                    modifierOperation = AttributeModifier.Operation.valueOf(modifierOperationString);
                } catch (IllegalArgumentException e) {
                    continue;
                }

                // modifier equipment slot
                String equipmentSlotString = (String) modifierMap.get("equipmentslot");
                EquipmentSlot equipmentSlot;
                try {
                    equipmentSlot = EquipmentSlot.valueOf(equipmentSlotString);
                } catch (IllegalArgumentException e) {
                    equipmentSlot = null;
                }

                // modifier (ctor)
                AttributeModifier modifier;
                if (modifierUniqueId != null) {
                    if (equipmentSlot != null) {
                        modifier = new AttributeModifier(modifierUniqueId, modifierName, modifierAmount, modifierOperation, equipmentSlot);
                    } else {
                        modifier = new AttributeModifier(modifierUniqueId, modifierName, modifierAmount, modifierOperation);
                    }
                } else {
                    modifier = new AttributeModifier(modifierName, modifierAmount, modifierOperation);
                }

                meta.addAttributeModifier(attribute, modifier);
            }
        }

        item.setItemMeta(meta);
        return item;
    }

    @Override
    public ItemStack getItemStack(String typeString) {
        if (typeString == null) {
            return invalidItemStack;
        }

        Material type = Material.getMaterial(typeString);
        if (type == null) {
            return invalidItemStack;
        }

        return new ItemStack(type, 1);
    }

    @Override
    public boolean isValidMaterial(String typeString) {
        return Material.getMaterial(typeString) != null;
    }
}
