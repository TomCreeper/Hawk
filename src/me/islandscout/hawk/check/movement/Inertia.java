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

package me.islandscout.hawk.check.movement;

import me.islandscout.hawk.HawkPlayer;
import me.islandscout.hawk.check.MovementCheck;
import me.islandscout.hawk.event.MoveEvent;
import me.islandscout.hawk.util.AdjacentBlocks;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Inertia extends MovementCheck {

    //"Inertia is a property of matter... Bill, Bill, Bill..."

    public Inertia() {
        super("inertia", true, -1, 3, 0.995, 5000, "%player% failed inertia. VL: %vl%", null);
    }

    @Override
    public void check(MoveEvent e) {
        Player p = e.getPlayer();
        HawkPlayer pp = e.getHawkPlayer();
        Vector moveVector = new Vector(e.getTo().getX() - e.getFrom().getX(), 0, e.getTo().getZ() - e.getFrom().getZ());
        Vector prevVector = pp.getVelocity().clone().setY(0);
        double horizSpeedSquared = Math.pow(e.getTo().getX() - e.getFrom().getX(), 2) + Math.pow(e.getTo().getZ() - e.getFrom().getZ(), 2);
        if (horizSpeedSquared > 0.05) {
            double deltaAngle = moveVector.angle(prevVector);
            if (!AdjacentBlocks.blockNearbyIsSolid(e.getTo(), true) && !AdjacentBlocks.blockAdjacentIsSolid(e.getFrom().clone().add(0, -0.3, 0)) &&
                    !AdjacentBlocks.blockNearbyIsSolid(e.getTo().clone().add(0, 1, 0), true) && !p.isFlying() && !p.isInsideVehicle() && !e.isOnGroundReally()) {
                if (deltaAngle > 0.2 && !e.hasAcceptedKnockback()) {
                    punishAndTryRubberband(pp, e, e.getFrom());
                } else {
                    reward(pp);
                }
            }
        }
    }
}
