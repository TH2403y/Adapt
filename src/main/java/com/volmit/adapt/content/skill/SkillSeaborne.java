package com.volmit.adapt.content.skill;

import com.volmit.adapt.api.skill.SimpleSkill;
import com.volmit.adapt.content.adaptation.SeaborneOxygen;
import com.volmit.adapt.util.C;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;

public class SkillSeaborne extends SimpleSkill {
    public SkillSeaborne() {
        super("seaborne", "\uD83C\uDF0A");
        setColor(C.BLUE);
        setBarColor(BarColor.BLUE);
        setBarStyle(BarStyle.SEGMENTED_6);
        setInterval(2120);
        setIcon(Material.TRIDENT);
        registerAdaptation(new SeaborneOxygen());
    }

    @Override
    public void onTick() {
        for(Player i : Bukkit.getOnlinePlayers())
        {
            if(i.isSwimming() || i.getRemainingAir() < i.getMaximumAir())
            {
                xpSilent(i, 19.7);
            }
        }
    }
}
