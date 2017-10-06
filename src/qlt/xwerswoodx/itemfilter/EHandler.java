package qlt.xwerswoodx.itemfilter;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EHandler implements Listener {
	main plugin;
	public EHandler(main ins) {
		plugin = ins;
	}
	
	@EventHandler
	public void itemPickUp_Ground(EntityPickupItemEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if (player.hasPermission("ItemFilter.use")) {
				if (plugin.getConfig().contains(player.getUniqueId().toString())) {
//					event.getItem();
					if (plugin.checkItem(player, event.getItem().getItemStack())) {
						if (plugin.getConfig().getInt("mode." + player.getUniqueId().toString()) == 1)
							event.setCancelled(true);
					} else {
						if (plugin.getConfig().getInt("mode." + player.getUniqueId().toString()) == 2)
							event.setCancelled(true);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent event) {
		if (event.getInventory().getTitle().equals("ItemFilter")) {
			if (event.getWhoClicked() instanceof Player) {
				Player player = (Player) event.getWhoClicked();
				event.setCancelled(true);

				if ((event.getCurrentItem() == null) || event.getCurrentItem().getType().toString().equals("AIR"))
					return;
				
				if (event.getClickedInventory().getHolder() != null) {
					if (event.getClickedInventory().getHolder().equals(player)) {
						if ((event.getCurrentItem() != null) || event.getCurrentItem().getType().toString().equals("AIR")) {
							plugin.setItem(player, event.getCurrentItem());
						}
					}
				} else {
					if ((event.getCurrentItem() != null) || event.getCurrentItem().getType().toString().equals("AIR")) {
						if (event.getSlot() == 53) {
							if (plugin.getConfig().getInt("mode." + player.getUniqueId().toString()) == 1)
								plugin.getConfig().set("mode." + player.getUniqueId().toString(), 2);
							else
								plugin.getConfig().set("mode." + player.getUniqueId().toString(), 1);
							plugin.saveConfig();
							ItemStack item = new ItemStack(Material.PAPER, 1);
							ItemMeta meta = item.getItemMeta();
							meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getMode(player)));
							item.setItemMeta(meta);
							plugin.setInfo(player, item);
							player.getOpenInventory().setItem(53, item);
//							player.closeInventory();
//							plugin.openGUI(player);
						} else {
							plugin.delItem((Player) event.getWhoClicked(), event.getCurrentItem(), event.getSlot());
						}
					}
				}
			}
		}
	}
}
