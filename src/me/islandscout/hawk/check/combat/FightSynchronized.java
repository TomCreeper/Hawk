/*
 * This file is part of Hawk Anticheat.
 * Copyright (C) 2018 Hawk Development Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.islandscout.hawk.check.combat;

import me.islandscout.hawk.HawkPlayer;
import me.islandscout.hawk.check.EntityInteractionCheck;
import me.islandscout.hawk.check.Cancelless;
import me.islandscout.hawk.event.InteractAction;
import me.islandscout.hawk.event.InteractEntityEvent;
import me.islandscout.hawk.util.ConfigHelper;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * FightSynchronized exploits a flaw in aim-bot/aim-assist cheats
 * by comparing the player's last attack time to their last move
 * time. Although easily bypassed, it catches a significant
 * number of cheaters.
 */
public class FightSynchronized extends EntityInteractionCheck implements Cancelless {

    //This check is deprecated since it flags incorrectly more than it flags correctly.

    private final Map<UUID, Long> attackTimes;
    private final Map<UUID, Integer> samples;
    private final int SAMPLE_SIZE;
    private final int THRESHOLD;

    public FightSynchronized() {
        super("fightsync", true, -1, 2, 0.95, 5000, "%player% may be using killaura (SYNC). VL %vl%", null);
        attackTimes = new HashMap<>();
        samples = new HashMap<>();
        SAMPLE_SIZE = ConfigHelper.getOrSetDefault(10, hawk.getConfig(), "checks.fightsync.samplesize");
        THRESHOLD = ConfigHelper.getOrSetDefault(6, hawk.getConfig(), "checks.fightsync.threshold");
    }

    @Override
    public void check(InteractEntityEvent event) {
        if (event.getInteractAction() != InteractAction.ATTACK)
            return;
        Player attacker = event.getPlayer();
        HawkPlayer pp = event.getHawkPlayer();
        samples.put(attacker.getUniqueId(), samples.getOrDefault(attacker.getUniqueId(), 0) + 1);
        long diff = System.currentTimeMillis() - pp.getLastMoveTime();
        if (diff > 100) {
            diff = 100L;
        }
        attackTimes.put(attacker.getUniqueId(), attackTimes.getOrDefault(attacker.getUniqueId(), 0L) + diff);
        if (samples.get(attacker.getUniqueId()) >= SAMPLE_SIZE) {
            samples.put(attacker.getUniqueId(), 0);
            attackTimes.put(attacker.getUniqueId(), attackTimes.get(attacker.getUniqueId()) / SAMPLE_SIZE);
            if (attackTimes.get(attacker.getUniqueId()) < THRESHOLD) {
                punish(pp, 1, false, event);
            } else
                reward(pp);
        }
    }

    public void removeData(Player p) {
        samples.remove(p.getUniqueId());
        attackTimes.remove(p.getUniqueId());
    }
}
