package de.stylextv.gs.command;

import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import de.stylextv.gs.main.Main;
import de.stylextv.gs.main.Vars;
import de.stylextv.gs.permission.PermissionUtil;
import de.stylextv.gs.player.CodeParser;
import de.stylextv.gs.player.Order;
import de.stylextv.gs.player.PlayerManager;
import de.stylextv.gs.world.WorldUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class CommandHandler {
	
	private static CopyOnWriteArrayList<String> oldFiles=null;
	
	public static void create() {
		if(oldFiles==null) {
			String[] files=WorldUtil.getCustomImagesFolder().list();
			if(files!=null) {
				oldFiles=new CopyOnWriteArrayList<String>();
				for(String s:files) oldFiles.add(s);
			}
		}
	}
	
	public static boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player p=(Player) sender;
			boolean hasPermList=PermissionUtil.hasListPermission(p);
			boolean hasPermCreate=PermissionUtil.hasCreatePermission(p);
			boolean hasPermRemove=PermissionUtil.hasRemovePermission(p);
			if(args.length>=1) {
				String sub=args[0];
				if(sub.equalsIgnoreCase("listfiles")) {
					if(hasPermList) {
						if(args.length<=2) {
							int page=0;
							if(args.length==2) try {
								page=Integer.valueOf(args[1])-1;
							} catch(NumberFormatException ex) {
								p.sendMessage(Vars.PREFIX+"�7Please enter a valid �cpage number�7.");
								return false;
							}
							String[] files=WorldUtil.getCustomImagesFolder().list();
							int length=0;
							if(files!=null) length=files.length;
							int pages=length/14 + (length%14!=0 ? 1 : 0);
							
							if(length==0) {
								p.sendMessage("�9>�m--------------------�6  Files  �9�m---------------------�9<");
								p.sendMessage("");
								p.sendMessage("    �7Sorry, but your folder seems to be �cempty�7.");
								for(int i=0; i<14; i++) p.sendMessage("");
								sendPageArrows(p, page, pages);
								p.sendMessage("");
								p.sendMessage("�9>�m------------------------------------------------�9<");
							} else {
								if(oldFiles==null) {
									oldFiles=new CopyOnWriteArrayList<String>();
									for(String s:files) oldFiles.add(s);
								} else for(String s:oldFiles) {
									boolean remove=true;
									for(String check:files) {
										if(check.equalsIgnoreCase(s)) {
											remove=false;
											break;
										}
									}
									if(remove) oldFiles.remove(s);
								}
								
								if(page<0) page=0;
								else if(page>=pages) page=pages-1;
								int j=page*14+13;
								if(j>=length) j=length-1;
								p.sendMessage("�9>�m--------------------�6  Files  �9�m---------------------�9<");
								p.sendMessage("");
								p.sendMessage("    �7Page "+(page+1)+":");
								for(int i=0; i<14; i++) {
									int index=i+page*14;
									if(index<=j) {
										String name=files[index];
										boolean isNew=!oldFiles.contains(name);
										if(isNew) {
											oldFiles.add(name);
										}
										sendFile(p, name, isNew);
									} else p.sendMessage("");
								}
								sendPageArrows(p, page, pages);
								p.sendMessage("");
								p.sendMessage("�9>�m------------------------------------------------�9<");
							}
						} else p.sendMessage(Vars.PREFIX+"�7Please use �8\"�7/gs listfiles �c[page]�8\"�7.");
					} else sendNoPermission(p);
				} else if(sub.equalsIgnoreCase("remove")) {
					if(hasPermRemove) {
						if(args.length==1) {
							PlayerManager.toggleRemovingPhase(p);
						} else p.sendMessage(Vars.PREFIX+"�7Please use �8\"�7/gs �cremove�8\"�7.");
					} else sendNoPermission(p);
				} else if(hasPermList||hasPermCreate||hasPermRemove) {
					if(args.length==1) {
						if(sub.equalsIgnoreCase("create")) {
							if(hasPermCreate) sendCreateSuggestion(p);
							else sendNoPermission(p);
						} else if(sub.equalsIgnoreCase("help")) {
							sendHelp(p);
						} else if(sub.equalsIgnoreCase("info")) {
							sendInfo(p);
						} else if(sub.equalsIgnoreCase("cancel")) {
							if(hasPermCreate) PlayerManager.cancelPlacingPhase(p);
							else sendNoPermission(p);
						} else sendHelpSuggestion(p);
					} else {
						if(args.length>1&&sub.equalsIgnoreCase("create")) {
							if(hasPermCreate) {
								if(args.length>=2) {
									new BukkitRunnable() {
										@Override
										public void run() {
											String code="";
											for(int i=1; i<args.length; i++) {
												if(i!=1) code=code+" ";
												code=code+args[i];
											}
											Order order=CodeParser.parseCode(code);
											if(order!=null) {
												if(order.getError()!=null) {
													p.sendMessage(Vars.PREFIX+"�7The following value could not be parsed: �c"+order.getError());
												} else {
													PlayerManager.startPlacingPhase(p, order);
												}
											} else p.sendMessage(Vars.PREFIX+"�7The �ccode�7 you provided could not be parsed.");
											
										}
									}.runTaskAsynchronously(Main.getPlugin());
									
									return false;
								} else sendCreateSuggestion(p);
							} else sendNoPermission(p);
						} else sendHelpSuggestion(p);
					}
				} else {
					sendInfo(p);
				}
			} else {
				if(hasPermList||hasPermCreate||hasPermRemove) sendHelpSuggestion(p);
				else sendInfo(p);
			}
		} else sender.sendMessage(Vars.PREFIX_CONSOLE+"�7This command is for �cplayers�r only.");
		return false;
	}
	private static void sendFile(Player p, String file, boolean isNew) {
		TextComponent comp=new TextComponent("        ");
		String displayName=file;
		int j=isNew ? 6 : 0;
		if(displayName.length()>36-j) displayName=displayName.substring(0, 33-j)+"...";
		comp.addExtra(
				createClickableComponent("�7- "+displayName, "�7Click here to get a �ecommand�7 for this file.", "/gs create {bg-img:"+file+"}", ClickEvent.Action.SUGGEST_COMMAND)
		);
		if(isNew) comp.addExtra(new TextComponent(" �d[�lNEW�d]"));
		p.spigot().sendMessage(comp);
	}
	private static void sendPageArrows(Player p, int page, int pages) {
		TextComponent comp=new TextComponent("    ");
		if(page>0) comp.addExtra(getPageArrow(page-1, true));
		else comp.addExtra(getPageArrow(true));
		TextComponent line=new TextComponent(" �8| ");
		comp.addExtra(line);
		if(page<pages-1) comp.addExtra(getPageArrow(page+1, false));
		else comp.addExtra(getPageArrow(false));
		comp.addExtra(line);
		comp.addExtra(
				createClickableComponent("�e[�lREFRESH�e]", "�7Click here to refresh the �ecurrent�7 page.", "/gs listfiles "+(page+1), ClickEvent.Action.RUN_COMMAND)
		);
		p.spigot().sendMessage(comp);
	}
	private static TextComponent getPageArrow(int page, boolean dir) {
	    return createClickableComponent(dir ? "�6�l<--" : "�6�l-->", dir ? "�7Click here to view the �eprevious�7 page." : "�7Click here to view the �enext�7 page.", "/gs listfiles "+(page+1), ClickEvent.Action.RUN_COMMAND);
	}
	private static TextComponent getPageArrow(boolean dir) {
		TextComponent comp=new TextComponent(dir ? "�8�l<--" : "�8�l-->");
		return comp;
	}
	private static void sendHelpSuggestion(Player p) {
		p.sendMessage(Vars.PREFIX+"�7Please enter �8\"�7/gs �ehelp�8\"�7 to get a list of commands.");
	}
	private static void sendCreateSuggestion(Player p) {
		p.sendMessage(Vars.PREFIX+"�7Please use �8\"�7/gs create �c<code>�8\"�7.");
	}
	private static void sendNoPermission(Player p) {
		p.sendMessage(Vars.PREFIX+"�7You don't have the right �cpermission�7 to do that.");
	}
	private static void sendHelp(Player p) {
		p.sendMessage("�a>�m---------------------�6  Help  �a�m---------------------�a<");
		p.sendMessage("");
		p.sendMessage("    �7/gs �ecreate �7<code>: Lets you create and place a");
		p.sendMessage("    �7 new sign");
		p.sendMessage("    �8 (Requires the permission: gsigns.create)");
		p.sendMessage("");
		p.sendMessage("    �7/gs �eremove�7: Lets you remove a sign.");
		p.sendMessage("    �8 (Requires the permission: gsigns.remove)");
		p.sendMessage("");
		p.sendMessage("    �7/gs �ecancel�7: Cancels the current placement process");
		p.sendMessage("    �8 (Requires the permission: gsigns.create)");
		p.sendMessage("");
		p.sendMessage("    �7/gs �elistfiles�7: Lists the files in your image folder");
		p.sendMessage("    �8 (Requires the permission: gsigns.list)");
		p.sendMessage("");
		p.sendMessage("    �7/gs �ehelp�7: Shows this command list");
		p.sendMessage("    �8 (Requires any of the plugins permissions)");
		p.sendMessage("");
		p.sendMessage("    �7/gs �einfo�7: Shows general information about this plugin");
		p.sendMessage("");
		p.sendMessage("�a>�m------------------------------------------------�a<");
	}
	private static void sendInfo(Player p) {
		p.sendMessage("�5>�m------------------�6  Information  �5�m------------------�5<");
		p.sendMessage("");
		p.sendMessage("    �7This server uses the free plugin �bG-Signs�7.");
		p.sendMessage("");
		p.sendMessage("    �7Author: �e"+Vars.AUTHOR);
		p.sendMessage("");
		p.sendMessage("    �7Version: �6"+Vars.VERSION);
		p.sendMessage("    �8 (Please visit our spigot site for updates)");
		p.sendMessage("");
		p.sendMessage("�5>�m------------------------------------------------�5<");
	}
	
	private static TextComponent createClickableComponent(String baseText, String hoverText, String clickText, ClickEvent.Action clickAction) {
		if(WorldUtil.getMcVersion()==WorldUtil.MCVERSION_1_8) {
			TextComponent comp=new TextComponent(baseText);
			comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
			comp.setClickEvent(new ClickEvent(clickAction, clickText));
			return comp;
		} else {
			TextComponent comp=new TextComponent(baseText);
			comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(new TextComponent(hoverText)).create()));
			comp.setClickEvent(new ClickEvent(clickAction, clickText));
			return comp;
		}
	}
	
}
