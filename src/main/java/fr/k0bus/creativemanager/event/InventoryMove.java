package fr.k0bus.creativemanager.event;

import fr.k0bus.creativemanager.CreativeManager;
import fr.k0bus.creativemanager.settings.Protections;
import fr.k0bus.k0buslib.utils.Messages;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Inventory move listener.
 */
public class InventoryMove implements Listener {

	private final CreativeManager plugin;
	private final HashMap<UUID, Long> cdtime = new HashMap<>();

	/**
	 * Instantiates a new Inventory move.
	 *
	 * @param instance the instance.
	 */
	public InventoryMove(CreativeManager instance) {
		plugin = instance;
	}

	/**
	 * On inventory click.
	 *
	 * @param e the event.
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInventoryClick(InventoryCreativeEvent e) {
		Player player = (Player) e.getWhoClicked();
		ItemStack itemStack = e.getCurrentItem();
		if (itemStack == null)
			itemStack = e.getCursor();
		else if (itemStack.getType().equals(Material.AIR))
			itemStack = e.getCursor();
		if (e.getClick().equals(ClickType.DROP) || e.getClick().equals(ClickType.CONTROL_DROP) ||
				e.getClick().equals(ClickType.WINDOW_BORDER_LEFT) || e.getClick().equals(ClickType.WINDOW_BORDER_RIGHT) ||
				e.getClick().equals(ClickType.UNKNOWN)) {
			if (plugin.getSettings().getProtection(Protections.DROP) && !player.hasPermission("creativemanager.bypass.drop")) {
				if (plugin.getSettings().getBoolean("send-player-messages"))
					Messages.sendMessage(plugin.getMessageManager(), player, "permission.drop");
				e.setCancelled(true);
			}
			return;
		}
		List<String> blacklist = plugin.getSettings().getGetBL();
		if(blacklist.size() > 0)
			if (blacklist.stream().anyMatch(itemStack.getType().name()::equalsIgnoreCase)) {
				if (!player.hasPermission("creativemanager.bypass.blacklist.get")) {
					if (cdtime.get(player.getUniqueId()) == null || (cdtime.get(player.getUniqueId()) + 1000) <= System.currentTimeMillis()) {
						if (cdtime.get(player.getUniqueId()) != null) {
							cdtime.remove(player.getUniqueId());
						}
						String blget = plugin.getLang().getString("blacklist.get");
						HashMap<String, String> replaceMap = new HashMap<>();
						replaceMap.put("{ITEM}", itemStack.getType().name());
						if (blget != null)
							Messages.sendMessage(plugin.getMessageManager(), player, "blacklist.get", replaceMap);
						cdtime.put(player.getUniqueId(), System.currentTimeMillis());
					}
					e.setCancelled(true);
				}
			}
		if (!player.hasPermission("creativemanager.bypass.lore") && plugin.getSettings().getProtection(Protections.LORE)) {
			e.setCurrentItem(addLore(e.getCurrentItem(), player));
			e.setCursor(addLore(e.getCursor(), player));
		}
	}

	/**
	 * Add lore item stack.
	 *
	 * @param item the item.
	 * @param p    the player.
	 * @return the item stack.
	 */
	private ItemStack addLore(ItemStack item, Player p) {
		if (item == null)
			return null;
		if (p == null)
			return null;
		ItemMeta meta = item.getItemMeta();
		if (meta == null) {
			return item;
		}
		List<?> lore = this.plugin.getSettings().getLore();
		List<String> lore_t = new ArrayList<>();

		if (lore != null) {
			for (Object obj : lore) {
				if (obj instanceof String) {
					String string = (String) obj;
					string = string.replace("{PLAYER}", p.getName())
							.replace("{UUID}", p.getUniqueId().toString())
							.replace("{ITEM}", item.getType().name());
					lore_t.add(ChatColor.translateAlternateColorCodes('&',string));
				}
			}
		}
		meta.setLore(lore_t);
		item.setItemMeta(meta);
		return item;
	}
}
