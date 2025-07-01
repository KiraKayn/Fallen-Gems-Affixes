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