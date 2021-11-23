package com.volmit.adapt.api.world;

import com.volmit.adapt.Adapt;
import com.volmit.adapt.api.advancement.AdaptAdvancement;
import com.volmit.adapt.api.skill.Skill;
import com.volmit.adapt.util.J;
import com.volmit.adapt.util.KMap;
import eu.endercentral.crazy_advancements.Advancement;
import eu.endercentral.crazy_advancements.AdvancementDisplay;
import eu.endercentral.crazy_advancements.AdvancementVisibility;
import eu.endercentral.crazy_advancements.NameKey;
import eu.endercentral.crazy_advancements.manager.AdvancementManager;
import lombok.Data;
import org.bukkit.Material;

@Data
public class AdvancementHandler
{
    private AdvancementManager manager;
    private AdaptPlayer player;
    private KMap<Skill, AdaptAdvancement> roots;
    private KMap<String, Advancement> real;

    public AdvancementHandler(AdaptPlayer player)
    {
        this.player = player;
        this.manager = new AdvancementManager(player.getPlayer());
        getManager().setAnnounceAdvancementMessages(false);
        roots = new KMap<>();
        real = new KMap<>();
    }

    public void activate()
    {
        J.s(() -> {
            removeAllAdvancements();

            for(Skill i : player.getServer().getSkillRegistry().getSkills())
            {
                AdaptAdvancement aa = i.buildAdvancements();
                roots.put(i, aa);

                for(Advancement j : aa.toAdvancements().reverse())
                {
                    real.put(j.getName().getKey(), j);
                    Adapt.info(j.getName().getKey());
                    try
                    {
                        getManager().addAdvancement(j);
                    }

                    catch(Throwable e)
                    {
                        Adapt.error("Failed to register advancement " + j.getName().getKey());
                        e.printStackTrace();
                    }
                }

                unlockExisting(aa);
            }
        }, 20);
    }

    public void grant(String key, boolean toast)
    {
        getPlayer().getData().ensureGranted(key);
        J.s(() -> getManager().grantAdvancement(player.getPlayer(), real.get(key)), 5);
        Adapt.info("Advancement Granted " + key);

        if(toast)
        {
            real.get(key).displayToast(getPlayer().getPlayer());
        }
    }

    public void grant(String key)
    {
        grant(key, true);
    }

    private void unlockExisting(AdaptAdvancement aa) {
        if(aa.getChildren() != null)
        {
            for(AdaptAdvancement i : aa.getChildren())
            {
                unlockExisting(i);
            }
        }

        if(getPlayer().getData().isGranted(aa.getKey()))
        {
            J.s(() -> grant(aa.getKey(), false), 20);
        }
    }

    public void deactivate()
    {
        removeAllAdvancements();
    }

    public void removeAllAdvancements()
    {
        for(Advancement i : getManager().getAdvancements())
        {
            getManager().removeAdvancement(i);
        }
    }
}
