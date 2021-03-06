package com.elikill58.negativity.spigot.timers;

import java.sql.Timestamp;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import com.elikill58.negativity.spigot.SpigotNegativity;
import com.elikill58.negativity.spigot.SpigotNegativityPlayer;
import com.elikill58.negativity.spigot.SpigotNegativityPlayer.FlyingReason;
import com.elikill58.negativity.spigot.utils.Utils;
import com.elikill58.negativity.universal.Cheat;
import com.elikill58.negativity.universal.ReportType;

@SuppressWarnings("deprecation")
public class TimerAnalyzePacket extends BukkitRunnable {

	@Override
	public void run() {
		for (Player p : Utils.getOnlinePlayers()) {
			SpigotNegativityPlayer np = SpigotNegativityPlayer.getNegativityPlayer(p);
			if (np.SEC_ACTIVE < 2) {
				np.SEC_ACTIVE++;
				return;
			}
			if(!p.isOnline()){
				np.destroy(false);
				return;
			}
			int ping = Utils.getPing(p);
			if (ping == 0)
				ping = 1;
			int flying = np.FLYING - (ping / 6);
			if (flying > 28) {
				if (np.ACTIVE_CHEAT.contains(np.flyingReason.getCheat())) {
					if(p.getItemInHand().getType().equals(Material.BOW))
						np.flyingReason = FlyingReason.BOW;
					int porcent = Utils.parseInPorcent(flying - (ping / 8));
					ReportType type = ReportType.WARNING;
					if (flying > 30)
						type = ReportType.VIOLATION;
					np.addWarn(np.flyingReason.getCheat());
					SpigotNegativity.alertMod(type, p, np.flyingReason.getCheat(), porcent,
							"Flying in one second: " + np.FLYING + ", ping: " + ping + ", max_flying: " + np.MAX_FLYING,
							"Too many packet: " + flying + "\n(Valid packets with low ping: 20)");
					if(np.flyingReason.getCheat().isSetBack()){
						switch(np.flyingReason){
						case BOW:
							break;
						case EAT:
							p.getInventory().addItem(new ItemStack(np.eatMaterial));
							break;
						case POTION:
							@SuppressWarnings("unchecked") List<PotionEffect> po = (List<PotionEffect>) np.POTION_EFFECTS.clone();
							for(PotionEffect pe : po)
								if(!p.hasPotionEffect(pe.getType())){
									p.addPotionEffect(pe);
									np.POTION_EFFECTS.remove(pe);
								}
							break;
						case REGEN:
							break;
						default:
							break;
						}
					}
				}
			}
			Cheat FLY = Cheat.fromString("FLY").get();
			if (np.ACTIVE_CHEAT.contains(FLY)) {
				if (np.FLYING > 4 && (np.POSITION + np.POSITION_LOOK + np.FLYING) < 9) {
					np.NO_PACKET++;
					if (np.NO_PACKET > 4) {
						int reliability = 0;
						ReportType type = ReportType.WARNING;
						if (np.ONLY_KEEP_ALIVE > 10)
							type = ReportType.VIOLATION;
						np.addWarn(FLY);
						SpigotNegativity.alertMod(type, p, FLY, reliability,
								np.ONLY_KEEP_ALIVE + " second of only KeepAlive. Last other: "
										+ np.LAST_OTHER_KEEP_ALIVE + "(" + new Timestamp(np.TIME_OTHER_KEEP_ALIVE)
										+ ", there is: " + (System.currentTimeMillis() - np.TIME_OTHER_KEEP_ALIVE)
										+ "ms)");
					}
				}
			}
			Cheat FORCEFIELD = Cheat.fromString("FORCEFIELD").get();
			if (np.ACTIVE_CHEAT.contains(FORCEFIELD)) {
				if (np.ARM > 14 && np.USE_ENTITY > 20) {
					np.addWarn(FORCEFIELD);
					ReportType type = ReportType.WARNING;
					if (np.getWarn(FORCEFIELD) > 4)
						type = ReportType.VIOLATION;
					SpigotNegativity.alertMod(type, p, FORCEFIELD,
							Utils.parseInPorcent(np.ARM + np.USE_ENTITY + np.getWarn(FORCEFIELD)),
							"ArmAnimation (Attack in one second): " + np.ARM
									+ ", UseEntity (interaction with other entity): " + np.USE_ENTITY + " And warn: "
									+ np.getWarn(FORCEFIELD) + ". Ping: " + ping);
				}
			}
			Cheat BLINK = Cheat.fromString("BLINK").get();
			if (np.ACTIVE_CHEAT.contains(BLINK) && !np.bypassBlink) {
				if (ping < 140) {
					int total = np.ALL - np.KEEP_ALIVE;
					if (total == 0) {
						if(Utils.parseInPorcent(120 - ping) >= BLINK.getReliabilityAlert()) {
							np.addWarn(BLINK);
							boolean last = np.IS_LAST_SEC_BLINK == 2;
							np.IS_LAST_SEC_BLINK++;
							long time_last = System.currentTimeMillis() - np.TIME_OTHER_KEEP_ALIVE;
							if (last) {
								SpigotNegativity.alertMod(ReportType.WARNING, p, BLINK, Utils.parseInPorcent(120 - ping),
										"No packet. Last other than KeepAlive: " + np.LAST_OTHER_KEEP_ALIVE + " there is: "
												+ time_last + "ms . Ping: " + ping + ". Warn: " + np.getWarn(BLINK));
							}
						}
					} else
						np.IS_LAST_SEC_BLINK = 0;
				} else 
					np.IS_LAST_SEC_BLINK = 0;
			}
			Cheat SNEAK = Cheat.fromString("SNEAK").get();
			if(np.ACTIVE_CHEAT.contains(SNEAK)){
				if(ping < 140){
					if(np.ENTITY_ACTION > 35){
						if(np.IS_LAST_SEC_SNEAK){
							np.addWarn(SNEAK);
							SpigotNegativity.alertMod(ReportType.WARNING, p, SNEAK, Utils.parseInPorcent(55 + np.ENTITY_ACTION), "EntityAction packet: " + np.ENTITY_ACTION + " Ping: " + ping + " Warn for Sneak: " + np.getWarn(SNEAK));
							if(SNEAK.isSetBack())
								p.setSneaking(false);
						}
						np.IS_LAST_SEC_SNEAK = true;
					} else np.IS_LAST_SEC_SNEAK = false;
				}
			}
			Cheat EDITED_CLIENT = Cheat.fromString("EDITED_CLIENT").get();
			if(np.ACTIVE_CHEAT.contains(EDITED_CLIENT)) {
				if(ping < EDITED_CLIENT.getMaxAlertPing()){
					int allPos = np.POSITION_LOOK + np.POSITION;
					if(allPos > 40) {
						np.addWarn(EDITED_CLIENT);
						SpigotNegativity.alertMod(allPos > 60 ? ReportType.VIOLATION : ReportType.WARNING, p, EDITED_CLIENT, Utils.parseInPorcent(25 + allPos), "PositionLook packet: " + np.POSITION_LOOK + " Position Packet: " + np.POSITION +  " (=" + allPos + " Ping: " + ping + " Warn for EditedClient: " + np.getWarn(EDITED_CLIENT));
					}
				}
			}
			np.clearPackets();
		}
	}
}