package com.volmit.adapt.api.notification;

import com.volmit.adapt.Adapt;
import com.volmit.adapt.api.skill.Skill;
import com.volmit.adapt.api.tick.TickedObject;
import com.volmit.adapt.util.C;
import com.volmit.adapt.util.Form;
import com.volmit.adapt.util.KList;
import com.volmit.adapt.util.KMap;
import com.volmit.adapt.util.M;
import org.bukkit.entity.Player;

public class Notifier extends TickedObject
{
    private int busyTicks;
    private int delayTicks;
    private final KList<Notification> queue;
    private final Player target;
    private KMap<String, Long> lastSkills;
    private KMap<String, Double> lastSkillValues;
    private long lastInstance;

    public Notifier(Player target)
    {
        super("notifications", target.getUniqueId().toString() + "-notify", 97);
        queue = new KList<>();
        lastSkills = new KMap<>();
        lastSkillValues = new KMap<>();
        this.target = target;
        lastInstance = 0;
    }

    public void notifyXP(String line, double value)
    {
        if(!lastSkills.containsKey(line))
        {
            lastSkillValues.put(line, 0d);
        }

        lastSkills.put(line, M.ms());
        lastSkillValues.put(line, lastSkillValues.get(line) + value);
        lastInstance = M.ms();

        if(isBusy())
        {
            return;
        }

        StringBuilder sb = new StringBuilder();

        for(String i : lastSkills.keySet())
        {
            Skill sk = getServer().getSkillRegistry().getSkill(i);
            sb.append((i.equals(line) ? sk.getDisplayName() : sk.getShortName()) + C.RESET + C.GRAY + " +" + C.WHITE + (line.equals(i) ? C.UNDERLINE : "") + Form.f(lastSkillValues.get(i).intValue()) + C.RESET + C.GRAY + "XP ");
        }

        Adapt.actionbar(target, sb.toString());
    }

    public void queue(Notification... f)
    {
        queue.add(f);
    }

    public boolean isBusy()
    {
        return busyTicks > 1 || queue.isNotEmpty();
    }

    @Override
    public void onTick() {
        for(String i : lastSkills.k())
        {
            if(M.ms() - lastSkills.get(i) > 5000 || (lastInstance > 3100 && M.ms() - lastSkills.get(i) > 3100))
            {
                lastSkills.remove(i);
                lastSkillValues.remove(i);
            }
        }

        if(busyTicks > 6)
        {
            busyTicks = 6;
        }

        if(busyTicks-- > 0)
        {
            return;
        }

        if(busyTicks < 0)
        {
            busyTicks = 0;
        }

        delayTicks--;
        if(delayTicks > 0)
        {
            return;
        }

        if(delayTicks < 0)
        {
            delayTicks = 0;
        }

        Notification n = queue.pop();

        if(n == null)
        {
            return;
        }

        delayTicks += (n.getTotalDuration()/50D) + 1;
        n.play(target);
    }
}
