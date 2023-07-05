Cook a set amount of an item.

Since Quests v3.13, `smeltingcertain` and `smelting` have been merged
into one. Both names can be used to refer to this task.

## Options

| Key      | Description                                     | Type                   | Required | Default | Notes                                                                                                                                                                                                                                                                                                                 |
|----------|-------------------------------------------------|------------------------|----------|---------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `amount` | The number of items to smelt.                   | Integer                | Yes      | \-      | \-                                                                                                                                                                                                                                                                                                                    |
| `item`   | The specific item to smelt.                     | Material, or ItemStack | No       | \-      | Accepts standard [item definition](defining_items "wikilink"). Please see [this list](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html) (1.13+) or [this list](https://helpch.at/docs/1.12.2/org/bukkit/Material.html) (1.8-1.12) for material names. If this is not specified, any item will count. |
| `mode`   | The specific mode of smelting.                  | String                 | No       | \-      | One of: `smoker`, `blast_furnace`, `furnace`. If this is not specified, any furnace will count.                                                                                                                                                                                                                       |
| `worlds` | Worlds which should count towards the progress. | List of world names    | No       | \-      | \-                                                                                                                                                                                                                                                                                                                    |

## Examples

Smelt 10 items:

``` yaml
smelting:
  type: "smelting"
  amount: 10                            # amount to smelt
  worlds:                               # (OPTIONAL) restrict to certain worlds
   - "world"
```

Cook 10 steak:

``` yaml
smeltingcertain:
  type: "smeltingcertain"
  amount: 10                            # amount to smelt
  item: STEAK                           # type of item 
  worlds:                               # (OPTIONAL) restrict to certain worlds
   - "world"
```

Cook 10 of a specific item:

``` yaml
smeltingcertain:
  type: "smeltingcertain"
  amount: 10                            # amount to smelt
  item:                                 # SPECIFIC item with name and lore
    name: "&cSpecial Beef"
    type: "RAW_BEEF"
    lore:
     - "&7This is a special type of beef"
  worlds:                               # (OPTIONAL) restrict to certain worlds
   - "world"
```

Cook 10 of [quest item](Defining-items#quest-item "wikilink")
`special_beef`:

``` yaml
smeltingcertain:
  type: "smeltingcertain"
  amount: 10                            # amount to smelt
  item:                                 # type of item 
    quest-item: "special_beef"
  worlds:                               # (OPTIONAL) restrict to certain worlds
   - "world"
```