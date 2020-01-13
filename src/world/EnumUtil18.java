package de.stylextv.gs.world;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class EnumUtil18 extends EnumUtil {
	
	@SuppressWarnings("deprecation")
	@Override
	public ItemFrame spawnItemFrame(int id, World world, Location loc, BlockFace dir, MapRenderer renderer) {
		ItemFrame frame=(ItemFrame) world.spawnEntity(loc, EntityType.ITEM_FRAME);
		frame.setFacingDirection(dir);
		
		try {
			MapView view=(MapView) Bukkit.class.getMethods()[5].invoke(Bukkit.class, (short)id);
			
			view.getRenderers().clear();
			for(MapRenderer r:view.getRenderers()) view.removeRenderer(r);
			view.addRenderer(renderer);
			ItemStack item = new ItemStack(Material.MAP, 1, (short)id);
			
			frame.setItem(item);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException ex) {ex.printStackTrace();}
		return frame;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public ItemFrame spawnItemFrame(World world, Location loc, BlockFace dir, MapRenderer renderer) {
		ItemFrame frame=(ItemFrame) world.spawnEntity(loc, EntityType.ITEM_FRAME);
		frame.setFacingDirection(dir);
		
		MapView view = Bukkit.createMap(world);
		short id=0;
		try {
			id=(short) view.getClass().getMethods()[0].invoke(view);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException ex) {}
		view.getRenderers().clear();
		for(MapRenderer r:view.getRenderers()) view.removeRenderer(r);
		view.addRenderer(renderer);
		ItemStack item = new ItemStack(Material.MAP, 1, id);
		
		frame.setItem(item);
		return frame;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public MapView getMapView(ItemStack item) {
		if(item==null) return null;
		try {
			return (MapView) Bukkit.class.getMethods()[5].invoke(Bukkit.class, item.getDurability());
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException ex) {}
		return null;
	}
	
	@Override
	public int getMapId(MapView view) {
		try {
			short id=(short) view.getClass().getMethods()[0].invoke(view);
			return id;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException ex) {}
		return 0;
	}
	
}
