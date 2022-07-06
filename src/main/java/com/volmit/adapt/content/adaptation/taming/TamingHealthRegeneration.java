package com.volmit.adapt.content.adaptation.taming;

import com.volmit.adapt.api.adaptation.SimpleAdaptation;
import com.volmit.adapt.util.C;
import com.volmit.adapt.util.Element;
import com.volmit.adapt.util.Form;
import com.volmit.adapt.util.J;
import com.volmit.adapt.util.KMap;
import com.volmit.adapt.util.M;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import xyz.xenondevs.particle.ParticleEffect;

import java.util.Collection;
import java.util.UUID;

public class TamingHealthRegeneration extends SimpleAdaptation<TamingHealthRegeneration.Config> {
    private final UUID attUUID = UUID.nameUUIDFromBytes("health-boost".getBytes());
    private final String attid = "att-health-boost";
    private final KMap<UUID, Long> lastDamage = new KMap<>();

    public TamingHealthRegeneration() {
        super("tame-health-regeneration");
        registerConfiguration(Config.class);
        setDescription("Increase your tamed animal health.");
        setIcon(Material.GOLDEN_APPLE);
        setBaseCost(getConfig().baseCost);
        setMaxLevel(getConfig().maxLevel);
        setInitialCost(getConfig().initialCost);
        setInterval(1000);
        setCostFactor(getConfig().costFactor);
    }

    @EventHandler
    public void on(EntityDamageByEntityEvent e) {
        if(e.getEntity() instanceof Tameable) {
            lastDamage.put(e.getEntity().getUniqueId(), M.ms());
        }

        if(e.getEntity() instanceof Tameable) {
            lastDamage.put(e.getDamager().getUniqueId(), M.ms());
        }
    }

    @EventHandler
    public void on(EntityDeathEvent e) {
        lastDamage.remove(e.getEntity().getUniqueId());
    }

    @Override
    public void addStats(int level, Element v) {
        v.addLore(C.GREEN + "+ " + Form.f(getRegenSpeed(level), 0) + C.GRAY + " HP/s");
    }

    private double getRegenSpeed(int level) {
        return ((getLevelPercent(level) * (getLevelPercent(level)) * getConfig().regenFactor) + getConfig().regenBase);
    }

    @Override
    public void onTick() {
        for(UUID i : lastDamage.k()) {
            if(M.ms() - lastDamage.get(i) > 10000) {
                lastDamage.remove(i);
            }
        }

        for(World i : Bukkit.getServer().getWorlds()) {
            J.s(() -> {
                Collection<Tameable> gl = i.getEntitiesByClass(Tameable.class);

                J.a(() -> {
                    for(Tameable j : gl) {
                        if(lastDamage.containsKey(j.getUniqueId())) {
                            continue;
                        }

                        double mh = j.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                        if(j.isTamed() && j.getOwner() instanceof Player && j.getHealth() < mh) {
                            Player p = (Player) j.getOwner();
                            int level = getLevel(p);

                            if(level > 0) {
                                J.s(() -> j.setHealth(Math.min(j.getHealth() + getRegenSpeed(level), mh)));
                                ParticleEffect.HEART.display(j.getLocation().clone().add(0, 1, 0), 0.55f, 0.37f, 0.55f, 0.3f, level, null);
                            }
                        }
                    }
                });
            });
        }
    }

    @Override
    public boolean isEnabled() {
        return getConfig().enabled;
    }

    @NoArgsConstructor
    protected static class Config {
        boolean enabled = true;
        int baseCost = 7;
        int maxLevel = 3;
        int initialCost = 8;
        double costFactor = 0.4;
        double regenFactor = 5;
        double regenBase = 1;
    }
}
