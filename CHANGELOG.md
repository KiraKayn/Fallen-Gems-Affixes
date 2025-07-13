# 1.2.2
- Added AdaptiveSpellPowerAffix Class - Rtxyd
- Added Spell Heal and Spell Damage targets, SPELL_DAMAGE_SELF, SPELL_DAMAGE_TARGET, SPELL_HEAL_SELF, SPELL_HEAL_TARGET - Rtxyd
- Added bunch of new affixes for loot categories (Celestial Weapons, Staffs) - Kayn
- Added Spell Power affixes to Celestial Weapons category - Kayn
- Reworked all gems, this includes base apotheosis gems too - Kayn
- Made Runiclib and Additional Attributes not a required dependency - Kayn
- Added config for Celestisynth compat - Kayn
- Improved Staffs category, so now all Staffs work, and not pulled by tag - Kayn
- Added compat to Alshanex's Familiars, T.O Tweaks and Cataclysm Spellbooks - Kayn

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