# 1.3.0
- Fixed the Celestial Weapons category by separating Ranged and Melee Weapons
- Completely reimplemented the Max Health Damage attribute, so it's sort of balanced now and works as intended
- Deleted the Attributes made for Scguns and Switched the attribute affixes to the mod's attributes
- Fixed the Eldritch Spell Gem not showing up in chests
- Fixed log spam if Iron's Spellbooks is not installed - #7
- Removed the unnecessary CastResult check from SpellCastUtil - thanks Muon
- Add back the deleted fix for the crash caused by FakePlayer and non-vanilla sized inventory - Rtxyd

# 1.2.9
- Completely fixed the Soulbound Affix, items never get deleted, only equips item if it was in the equipment slot OR in offhand slot and only if it's equippable, also works with Corpse mod
- Added Spell Cast affix, that allows you to cast ISS spells on these triggers: spell_damage, spell_heal, hurt, melee_hit and projectile_hit
- Nerfed Hemolith Shard by changing the permanent Assassin effect on it

# 1.2.8
- Improved on the Soulbound affix by making it so armors stay in their slot, and improved on the code overall
- Fixed Permanent Effect config not working
- Added missing translations for Soulbound affix and changed the tooltip a little

# 1.2.7
- Added Soulbound Affix

# 1.2.6
- Fixed Fallen Warrior gem not showing up (sorry)

# 1.2.5
- Fixed Ragnarok Gem not showing up if celestisynth is not installed
- Fixed the "bleeding" mob effect for swords not showing up if celestisynth is not installed
- Added new Codec class (ConditonalLootCategory) for conditional loot categories, example can be seen in the Ragnarok gem file
- Improved the Boss Slayer bonus so it supports invaders (bosses) from Apotheosis
- Added BossUtils class for the change above
- Improved BossSlayerBonus code
- Added missing celestisynth:frostbound|irons_spellbooks:ice to the config

# 1.2.4HF
- Fixed Ragnarok gem having 3 rarities, while it should have all

# 1.2.4
- Added BossSlayerBonus and BossSlayerHandler classes
- Added Ragnarok gem, which has the bonus + damage against bosses, you can define bosses in a tag located at fallen_gems_affixes:tags/entity_types/boss_slayer.json
- Made the Cataclysm gems work when only cataclysm is installed, using the Extra Gem Bonus registry

# 1.2.3
- Fixed untranslated spell on Thunderheart Fragment gem
- Fixed a few Gem Bonuses not showing up
- Made ISS integration optional so no crashes occur

# 1.2.2
- Added AdaptiveSpellPowerAffix Class
- Added Spell Heal and Spell Damage targets, SPELL_DAMAGE_SELF, SPELL_DAMAGE_TARGET, SPELL_HEAL_SELF, SPELL_HEAL_TARGET
- Added bunch of new affixes for loot categories (Celestial Weapons, Staffs)
- Added Spell Power affixes to Celestial Weapons category
- Reworked all gems, this includes base apotheosis gems too
- Made Runiclib and Additional Attributes not a required dependency
- Added config for Celestisynth compat
- Improved Staffs category, so now all Staffs work, and not pulled by tag 
- Added compat to Alshanex's Familiars, T.O Tweaks and Cataclysm Spellbooks 
- The AdaptiveSpellPowerAffix and new targets have been backported from the amazing Apotheosis x Iron's Spellbooks mod, so makes sure to check that out if you are playing 1.21+ :D

# 1.2.1
- Added compat to Apotheotic Additions rarities, requires Esoteric Reforging too
- Added Forge conditions to all staff affixes

# 1.2.0
- Backported Extra Gem Bonus feature from 1.21.1 apotheosis
- Added Permanent Effect Bonus
- Added Attribute Effect Bonus
- Translated Staffs and Heavy Weapons category
- Cleaned up most code so there are no warnings in them

# 1.1.9
- Changed Howling Storm Gem texture, made by Chainsa
- Made Forge 47.4.0 the min version needed
- Nerfed Guardian of Hell Gem
- Added Multi Effect gem bonus type, fallen_gems_affixes:multi_effect, example can be found on the page
- Made RunicLib the new dependency

# 1.1.8
- Added config for SocketHelperMixin, so if you don't want gaps between sockets you can turn it off

# 1.1.7
- Rewrote SocketGemModifier class to use the logic from MobGearGemInjector
- Added SocketHelperMixin to allow sockets to have gaps between them, made by rtxyd 

# 1.1.6
- Fixed the issue where the gear you have would change the gems on it when leaving & reentering the world

# 1.1.5
- Added Gem of the Howling Storm for Scylla boss, reduced the drop chance for Cataclysm gems, epic: 50%, mythic: 25%, ancient: 5%
  Fixed the Innate Blood Gem, Fixed the gun category not working, fixed all the errors that showed up in the log

# 1.1.0
- Added two new classes, SocketGemModifier, which is a global loot modifier that adds gems to affixed loot in chests, and MobGearInjector, which is an event that adds gems to boss gear. Default chance is 30%, can be changed between 0-1, and can be turned off in fallen_gems_affixes.common.toml

# 1.0.1
- Changed version="1.20.1-1.7.0" to version="1.20.1-1.0.0" in mods.toml

# 1.0.0
- Release