package maru;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockStairs;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.network.protocol.AddEntityPacket;
import cn.nukkit.network.protocol.MoveEntityPacket;
import cn.nukkit.network.protocol.PlayerActionPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.RemoveEntityPacket;
import cn.nukkit.network.protocol.SetEntityLinkPacket;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

public class PmChair extends PluginBase implements Listener {
	
	private Map<String, Long> onChair = new HashMap<>();
	private Map<String, Long> doubleTap = new HashMap<>();
	private Map<String, Long> tagblock = new HashMap<>();
	private Map<String, Object> messages;
	
	public static final int m_version = 1;
	
	@Override
	public void onEnable(){
		this.getServer().getPluginManager().registerEvents(this, this);
		this.loadMessage();
	}
	
	public void loadMessage(){
		this.saveResource("messages.yml");
		this.messages = new Config(new File(this.getDataFolder(), "messages.yml"), Config.YAML).getAll();
		this.updateMessage();
	}
	
	public void updateMessage(){
		if((int) this.messages.get("m_version") < m_version) {
			this.saveResource("messages.yml", true);
			new Config(new File(this.getDataFolder(), "messages.yml"), Config.YAML).getAll().forEach((k, v) -> this.messages.put(k, (String) v));
		}
	}
	
	public String get(String m){
		return (String) this.messages.get(this.messages.get("default-language") + "-" + m);
	}
	
	@EventHandler
	public void onTouch(PlayerInteractEvent event){
		Player player = event.getPlayer();
		if(player.isSneaking() || event.getAction() != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK){
			return;
		}
		
		String name = player.getName().toLowerCase();
		Block block = event.getBlock();
		
		if(! this.onChair.containsKey(name)){
			if(block instanceof BlockStairs){
				if(! this.doubleTap.containsKey(name)){
					this.doubleTap.put(name, System.currentTimeMillis());
					player.sendPopup(TextFormat.RED + this.get("touch-popup"));
					return;
				}
				if(System.currentTimeMillis() - this.doubleTap.get(name) < 500){
					
					AddEntityPacket AddTagblockPacket = new AddEntityPacket();
					long eid = Entity.entityCount++;
					this.tagblock.put(name, eid);
					AddTagblockPacket.entityRuntimeId = eid;
					AddTagblockPacket.entityUniqueId = eid;
					AddTagblockPacket.speedX = 0;
					AddTagblockPacket.speedY = 0;
					AddTagblockPacket.speedZ = 0;
					AddTagblockPacket.pitch = 0;
					AddTagblockPacket.yaw = 0;
					AddTagblockPacket.x = (float) (block.getX() + 0.5);
					AddTagblockPacket.y = (float) (block.getY() + 0.3);
					AddTagblockPacket.z = (float) (block.getZ() + 0.5);
					AddTagblockPacket.type = 15;
					
					long flags = 0;
					flags |= 1 << Entity.DATA_FLAG_CAN_SHOW_NAMETAG;
					flags |= 1 << Entity.DATA_FLAG_INVISIBLE;
					flags |= 1 << Entity.DATA_FLAG_NO_AI;
					flags |= 1 << Entity.DATA_FLAG_ALWAYS_SHOW_NAMETAG;
					
					AddTagblockPacket.metadata = new EntityMetadata()
							.putLong(Entity.DATA_FLAGS, flags)
							.putShort(Entity.DATA_AIR, 400)
							.putShort(Entity.DATA_MAX_AIR, 400)
							.putString(Entity.DATA_NAMETAG, TextFormat.AQUA + this.get("tagblock-message"))
							.putLong(Entity.DATA_LEAD_HOLDER_EID, -1)
							.putFloat(Entity.DATA_SCALE, 0.0001f);
					
					MoveEntityPacket moveTagblockPacket = new MoveEntityPacket();
					moveTagblockPacket.eid = eid;
					moveTagblockPacket.x = (float) (block.getX() + 0.5);
					moveTagblockPacket.y = (float) (block.getY() + 0.7);
					moveTagblockPacket.z = (float) (block.getZ() + 0.5);
					
					int[] faces = new int[]{90, 270, 180, 0, 90, 270, 180, 0};
					
					AddEntityPacket addEntityPacket = new AddEntityPacket();
					eid = Entity.entityCount++;
					this.onChair.put(name, eid);
					addEntityPacket.entityRuntimeId = eid;
					addEntityPacket.entityUniqueId = eid;
					addEntityPacket.speedX = 0;
					addEntityPacket.speedY = 0;
					addEntityPacket.speedZ = 0;
					addEntityPacket.pitch = 0;
					addEntityPacket.yaw = faces[event.getBlock().getDamage()];
					addEntityPacket.x = (float) (block.getX() + 0.5);
					addEntityPacket.y = (float) (block.getY() + 1.6);
					addEntityPacket.z = (float) (block.getZ() + 0.5);
					addEntityPacket.type = 15;
					
					flags = 0;
					//flags |= 1 << Entity.DATA_FLAG_CAN_SHOW_NAMETAG;
					flags |= 1 << Entity.DATA_FLAG_INVISIBLE;
					flags |= 1 << Entity.DATA_FLAG_NO_AI;
					//flags |= 1 << Entity.DATA_FLAG_ALWAYS_SHOW_NAMETAG;
					
					addEntityPacket.metadata = new EntityMetadata()
							.putLong(Entity.DATA_FLAGS, flags)
							.putShort(Entity.DATA_AIR, 400)
							.putShort(Entity.DATA_MAX_AIR, 400)
							.putLong(Entity.DATA_LEAD_HOLDER_EID, -1);
							//.putFloat(Entity.DATA_SCALE, 0.0001f);
					
					MoveEntityPacket moveEntityPacket = new MoveEntityPacket();
					moveEntityPacket.eid = eid;
					moveEntityPacket.x = (float) (block.getX() + 0.5);
					moveEntityPacket.y = (float) (block.getY() + 1.6);
					moveEntityPacket.z = (float) (block.getZ() + 0.5);
					moveEntityPacket.yaw = faces[event.getBlock().getDamage()];
					moveEntityPacket.headYaw = faces[event.getBlock().getDamage()];
					moveEntityPacket.pitch = 0;
					
					SetEntityLinkPacket setEntityLinkPacket = new SetEntityLinkPacket();
					setEntityLinkPacket.rider = eid;
					setEntityLinkPacket.riding = player.getId();
					setEntityLinkPacket.type = SetEntityLinkPacket.TYPE_RIDE;
					
					this.getServer().getOnlinePlayers().values().forEach((target) -> {
						target.dataPacket(addEntityPacket);
						target.dataPacket(moveEntityPacket);
						
						target.dataPacket(AddTagblockPacket);
						target.dataPacket(moveTagblockPacket);
						if(target != player){
							target.dataPacket(setEntityLinkPacket);
						}
					});
					
					setEntityLinkPacket.riding = 0;
					player.dataPacket(setEntityLinkPacket);
					this.doubleTap.remove(name);
				}else{
					this.doubleTap.put(name, System.currentTimeMillis());
					player.sendPopup(TextFormat.RED + this.get("touch-popup"));
					return;
				}
			}
		}else{
			RemoveEntityPacket removeEntityPacket = new RemoveEntityPacket();
			removeEntityPacket.eid = this.onChair.remove(name);
			RemoveEntityPacket removeTagblockPacket = new RemoveEntityPacket();
			removeTagblockPacket.eid = this.tagblock.remove(name);
			this.getServer().getOnlinePlayers().values().forEach((p) -> {
				p.dataPacket(removeEntityPacket);
				p.dataPacket(removeTagblockPacket);
			});
		}
	}

	@EventHandler
	public void onJump(DataPacketReceiveEvent event){
		if(event.getPacket().pid() == ProtocolInfo.PLAYER_ACTION_PACKET){
			PlayerActionPacket packet = (PlayerActionPacket) event.getPacket();
			String name = event.getPlayer().getName().toLowerCase();
			if (packet.action == PlayerActionPacket.ACTION_JUMP && this.onChair.containsKey(name)){
				RemoveEntityPacket removeEntityPacket = new RemoveEntityPacket();
				removeEntityPacket.eid = this.onChair.remove(name);
				RemoveEntityPacket removeTagblockPacket = new RemoveEntityPacket();
				removeTagblockPacket.eid = this.tagblock.remove(name);
				this.getServer().getOnlinePlayers().values().forEach((p) -> {
					p.dataPacket(removeEntityPacket);
					p.dataPacket(removeTagblockPacket);
				});
			}
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event){
		String name = event.getPlayer().getName().toLowerCase();
		if(! this.onChair.containsKey(name)){
			return;
		}
		RemoveEntityPacket removeEntityPacket = new RemoveEntityPacket();
		removeEntityPacket.eid = this.onChair.remove(name);
		RemoveEntityPacket removeTagblockPacket = new RemoveEntityPacket();
		removeTagblockPacket.eid = this.tagblock.remove(name);
		this.getServer().getOnlinePlayers().values().forEach((p) -> {
			p.dataPacket(removeEntityPacket);
			p.dataPacket(removeTagblockPacket);
		});
	}
}
