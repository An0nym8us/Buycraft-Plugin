package net.buycraft.tasks;

import java.util.ArrayList;

import net.buycraft.Plugin;
import net.buycraft.util.Chat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PackageCheckerTask extends Thread
{
	private Plugin plugin;
	
	private Boolean manualExecution;
	
	public PackageCheckerTask(Boolean manualExecution)
	{
		this.plugin = Plugin.getInstance();
		this.manualExecution = manualExecution;
	}
	
	public void run()
	{
		try
		{
			if(plugin.isAuthenticated(null))
			{
				if(plugin.getSettings().getBoolean("commandChecker") || manualExecution)
				{
					if(plugin.getServer().getOnlinePlayers().length > 0 || manualExecution)
					{
						JSONObject apiResponse = plugin.getApi().commandsGetAction();
			            
						if(apiResponse != null && apiResponse.getInt("code") == 0)
						{
							JSONObject apiPayload = apiResponse.getJSONObject("payload");
							JSONArray commandsPayload = apiPayload.getJSONArray("commands");
							
							ArrayList<String> executedCommands = new ArrayList<String>();
							    
						    if(commandsPayload.length() > 0)
							{
								for (int i = 0; i < commandsPayload.length(); i++) 
								{
									JSONObject row = commandsPayload.getJSONObject(i);
									
									String username = row.getString("ign");
									Boolean requireOnline = row.getBoolean("requireOnline");
									String command = row.getJSONArray("commands").getString(0);
									
									Player currentPlayer = plugin.getServer().getPlayer(username);
			
									if(currentPlayer != null || requireOnline == false)
									{
										if(executedCommands.contains(username) == false)
										{
											executedCommands.add(username);
										}
										
										Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new CommandExecuteTask(command, username), 60L);
									}
								}
							}
						    
						    if(executedCommands.size() > 0)
						    {
						    	for(String username : executedCommands)
							    {
							    	Player player = plugin.getServer().getPlayer(username);
							    	
							    	if(player != null)
							    	{
							    		player.sendMessage(Chat.header());
							    		player.sendMessage(Chat.seperator());
							    		player.sendMessage(Chat.seperator() + ChatColor.GREEN + plugin.getLanguage().getString("commandsExecuted"));
							    		player.sendMessage(Chat.seperator());
							    		player.sendMessage(Chat.footer());
							    	}
							    }
						    	
						    	plugin.getApi().commandsDeleteAction(new JSONArray(executedCommands.toArray()).toString());
						    }
						    
						    plugin.getLogger().info("Package checker successfully executed.");
						}
						else
						{
							plugin.getLogger().severe("No response/invalid key during package check.");
						}
					}
				}
			}
		}
		catch(JSONException e)
		{
			plugin.getLogger().severe("JSON parsing error in package checker.");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
