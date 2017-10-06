package qlt.xwerswoodx.itemfilter;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class main extends JavaPlugin {
	EHandler handler = new EHandler(this);
	
	public void onEnable() {
		getServer().getPluginManager().registerEvents(handler, this);
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String argn, String[] args) {
		if (cmd.getName().equalsIgnoreCase("ItemFilter")) {
			if (args.length < 1) {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					if (player.hasPermission("ItemFilter.use") == false) {
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4You don't have a permission for using this command!"));
						return true;
					}
					openGUI(player);
				}
			} else if (args[0].equalsIgnoreCase("reload")) {
				if (!sender.hasPermission("ItemFilter.admin")) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4You don't have permission to use admin commands."));
					return true;
				}
				reloadConfig();
				onDisable();
				onEnable();
				sender.sendMessage("[ItemFilter] Reloaded successfully!");
			}
		}
		return false;
	}
	
	public boolean checkItem(Player player, ItemStack item) {
		if (!getConfig().contains(player.getUniqueId().toString()))
			return false;
		Short data = item.getDurability();
		if (canDamage(item))
			data = 0;
		String path = item.getType().toString() + data;
		if (getConfig().contains(player.getUniqueId().toString() + "." + path))
			return true;
		return false;
	}
	
	public void setItem(Player player, ItemStack item) {
		if (getConfig().contains(player.getUniqueId().toString())) {
			ConfigurationSection items = getConfig().getConfigurationSection(player.getUniqueId().toString());
			Object[] obj = items.getKeys(false).toArray();
			if (obj.length > 52)
				return;
		}
		
		if (checkItem(player, item) == false) {
			short data = item.getDurability();
			if (canDamage(item))
				data = 0;
			getConfig().set(player.getUniqueId() + "." + item.getType().toString() + Short.toString(data), item.getType().toString() + ":" + Short.toString(data));
			saveConfig();
			ConfigurationSection items = getConfig().getConfigurationSection(player.getUniqueId().toString());
			Object[] obj = items.getKeys(false).toArray();
			ItemStack newItem = item.clone();
			newItem.setDurability(data);
			player.getOpenInventory().setItem(obj.length - 1, newItem);
			player.updateInventory();
		}
// Cancel to reopen inventory, because it sets mause cursor to middle.
// So we change this system with auto fix menus.
//		player.closeInventory();
//		openGUI(player);
	}
	
	public void delItem(Player player, ItemStack item, int slot) {
		if (getConfig().contains(player.getUniqueId().toString() + "." + item.getType().toString() + Short.toString(item.getDurability()))) {
			getConfig().set(player.getUniqueId().toString() + "." + item.getType().toString() + Short.toString(item.getDurability()), null);
			saveConfig();
			player.getOpenInventory().setItem(slot, new ItemStack(Material.AIR, 1));
			ConfigurationSection items = getConfig().getConfigurationSection(player.getUniqueId().toString());
			Object[] obj = items.getKeys(false).toArray();
			for (int i = slot; i < obj.length; i++) {
				Material material = Material.getMaterial(getConfig().getString(player.getUniqueId().toString() + "." + obj[i]).toString().split(":")[0]);
				short data = Short.parseShort(getConfig().getString(player.getUniqueId().toString() + "." + obj[i]).split(":")[1]);
				ItemStack it = new ItemStack(material, 1, data);
				player.getOpenInventory().setItem(i, it);
				player.getOpenInventory().setItem(i+1, new ItemStack(Material.AIR, 1));
			}
			player.updateInventory();
		}
// Cancel to reopen inventory, because it sets mause cursor to middle.
// So we change this system with auto fix menus.
//		player.closeInventory();
//		openGUI(player);
	}
	
	public void openGUI(Player player) {
		if (!getConfig().contains("mode." + player.getUniqueId().toString())) {
			getConfig().set("mode." + player.getUniqueId().toString(), 1);
		}
		
		int size = 54;
		Inventory gui = Bukkit.createInventory(null, size, "ItemFilter");
		
		if (getConfig().contains(player.getUniqueId().toString())) {
			ConfigurationSection items = getConfig().getConfigurationSection(player.getUniqueId().toString());
			Object[] obj = items.getKeys(false).toArray();
			
			if (obj.length > 0) {
				for (int i = 0; i < obj.length; i++) {
					Material material = Material.getMaterial(getConfig().getString(player.getUniqueId().toString() + "." + obj[i]).toString().split(":")[0]);
					short data = Short.parseShort(getConfig().getString(player.getUniqueId().toString() + "." + obj[i]).split(":")[1]);
					ItemStack item = new ItemStack(material, 1, data);
					gui.setItem(i, item);
				}
			}
		}
		ItemStack mode = new ItemStack(Material.PAPER, 1);
		ItemMeta meta = mode.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', getMode(player)));
		mode.setItemMeta(meta);
//		addLore(mode, ChatColor.translateAlternateColorCodes('&', getInfo(player)));
		setInfo(player, mode);
		gui.setItem(53, mode);
		player.openInventory(gui);
	}
	
	public String getMode(Player player) {
		if (getConfig().getInt("mode." + player.getUniqueId().toString()) > 1)
			return "&2Pick Up";
		return "&4Drop On";
	}
	
	public void setInfo(Player player, ItemStack item) {
		if (getConfig().getInt("mode." + player.getUniqueId().toString()) > 1) {
			addLore(item, ChatColor.translateAlternateColorCodes('&', "&7This mode means you can pick up items on this list."));
			addLore(item, ChatColor.translateAlternateColorCodes('&', "&6If this list empty, you won't pick up anything."));
		} else {
			addLore(item, ChatColor.translateAlternateColorCodes('&', "&7This mode means you won't pick up items on this list."));
			addLore(item, ChatColor.translateAlternateColorCodes('&', "&6If this list empty, you will pick up everything."));
		}
	}
	
	/*
	 * We just need to check whether it is a weapon, an armor or a tool or not.
	 * Because when we add that things, datas (durability) also saved and
	 * if their's durability are less than full, they seems like different items.
	 */
	public boolean canDamage(ItemStack item) {
		if (isWeapon(item))
			return true;
		else if (isArmor(item))
			return true;
		else if (isTool(item))
			return true;
		return false;
	}
	
	public boolean isWeapon(ItemStack item) {
		String type = item.getType().toString();
		if ((type.endsWith("_AXE")) || (type.endsWith("_SWORD")) || (type.endsWith("_PICKAXE")) || (type.endsWith("_HOE")) || (type.endsWith("_SPADE")) || (type.equals("BOW"))) {
			return true;
		}
		return false;
	}
	
	public boolean isArmor(ItemStack item) {
		String type = item.getType().toString();
		if ((type.endsWith("_HELMET")) || (type.endsWith("_LEGGINGS")) || (type.endsWith("_CHESTPLATE")) || (type.endsWith("_BOOTS"))) {
			return true;
		}
		return false;
	}
	
	public boolean isTool(ItemStack item) {
		String type = item.getType().toString();
		if ((type.equals("SHEARS")) || (type.equals("FISHING_ROD")) || (type.equals("FLINT_AND_STEEL")) || (type.equals("ELYTRA")) || (type.equals("SHIELD")) || (type.equals("CARROT_STICK"))) {
			return true;
		}
		return false;
	}

	public void addLore(ItemStack item, String lore) {
		ItemMeta meta = item.getItemMeta();
		ArrayList<String> loreList = new ArrayList<String>();
		
		if(meta.hasLore()) {
			for(String s : meta.getLore()) {
				loreList.add(s);
			}

		}
		loreList.add(lore);
		meta.setLore(loreList);
		item.setItemMeta(meta);
	}
}
