package me.islandscout.hawk.modules;

import me.islandscout.hawk.Hawk;
import me.islandscout.hawk.checks.*;
import me.islandscout.hawk.checks.combat.*;
import me.islandscout.hawk.checks.interaction.*;
import me.islandscout.hawk.checks.movement.*;
import me.islandscout.hawk.events.*;
import me.islandscout.hawk.utils.Debug;
import org.bukkit.entity.Player;

import java.util.*;

public class CheckManager {

    //I thought about making a new thread to eliminate some load from the NIO thread, but I'd run into concurrency issues.
    //I'd have to worry about cancelling, packet modding, and concurrent HawkPlayer data (race condition). It's not worth the trouble.

    private final Hawk hawk;

    private final Set<UUID> exemptedPlayers;

    //make these HashSets?
    private final List<Check> checks;
    private final List<BlockDigCheck> blockDigChecks;
    private final List<BlockPlacementCheck> blockPlacementChecks;
    private final List<CustomCheck> customChecks;
    private final List<EntityInteractionCheck> entityInteractionChecks;
    private final List<MovementCheck> movementChecks;

    public CheckManager(Hawk hawk) {
        this.hawk = hawk;
        Check.setHawkReference(hawk);
        exemptedPlayers = new HashSet<>();
        checks = new ArrayList<>();
        blockDigChecks = new ArrayList<>();
        blockPlacementChecks = new ArrayList<>();
        customChecks = new ArrayList<>();
        entityInteractionChecks = new ArrayList<>();
        movementChecks = new ArrayList<>();
    }

    //initialize checks and save any changes in configs to files.
    //can be used to reload checks
    public void loadChecks() {
        unloadChecks();

        new FightHitbox();
        new FightCriticals();
        new Phase();
        new Fly();
        new BlockBreakSpeed();
        new ClockSpeed();
        new Speed();
        new FightSynchronized();
        new Inertia();
        new BlockBreakHitbox();
        new WrongBlock();
        new LiquidExit();
        new GroundSpoof();
        new FightSpeed();
        new FightAccuracy();
        new FightAimbot();
        new FightNoSwing();
        new AntiVelocity();
        new InvalidPitch();
        new FightReachApprox();
        new FightDirectionApprox();
        new BlockInteractHitbox();
        new BlockInteractSpeed();
        //new SpeedRewrite();

        hawk.saveConfigs();
    }

    private void unloadChecks() {
        checks.clear();
        blockDigChecks.clear();
        blockPlacementChecks.clear();
        customChecks.clear();
        entityInteractionChecks.clear();
        movementChecks.clear();
    }

    //iterate through appropriate checks
    void dispatchEvent(Event e) {
        for (CustomCheck check : customChecks) {
            check.checkEvent(e);
        }
        if (e instanceof PositionEvent) {
            for (MovementCheck check : movementChecks)
                check.checkEvent((PositionEvent) e);
        } else if (e instanceof InteractEntityEvent) {
            for (EntityInteractionCheck check : entityInteractionChecks)
                check.checkEvent((InteractEntityEvent) e);
        } else if (e instanceof BlockDigEvent) {
            for (BlockDigCheck check : blockDigChecks)
                check.checkEvent((BlockDigEvent) e);
        } else if (e instanceof BlockPlaceEvent) {
            for (BlockPlacementCheck check : blockPlacementChecks)
                check.checkEvent((BlockPlaceEvent) e);
        }
    }

    public void removeData(Player p) {
        for (Check check : checks)
            check.removeData(p);
    }

    public List<Check> getChecks() {
        return checks;
    }

    public List<BlockDigCheck> getBlockDigChecks() {
        return blockDigChecks;
    }

    public List<BlockPlacementCheck> getBlockPlacementChecks() {
        return blockPlacementChecks;
    }

    public List<CustomCheck> getCustomChecks() {
        return customChecks;
    }

    public List<EntityInteractionCheck> getEntityInteractionChecks() {
        return entityInteractionChecks;
    }

    public List<MovementCheck> getMovementChecks() {
        return movementChecks;
    }

    public Set<UUID> getExemptedPlayers() {
        return exemptedPlayers;
    }
}