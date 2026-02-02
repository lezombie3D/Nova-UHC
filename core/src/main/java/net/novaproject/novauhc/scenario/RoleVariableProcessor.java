package net.novaproject.novauhc.scenario;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.scenario.role.RoleVariable;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;

import java.lang.reflect.Field;

public class RoleVariableProcessor {

    public static void process(Object role, UHCPlayer player, ScenarioRole scenarioRole) {
        for (Field field : role.getClass().getDeclaredFields()) {
            RoleVariable variable = field.getAnnotation(RoleVariable.class);
            if (variable == null) continue;

            field.setAccessible(true);

            try {
                switch (variable.type()) {

                    case ABILITY -> handleAbility(field, role, player,scenarioRole);

                    case INTEGER, DOUBLE, BOOLEAN -> {

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void handleAbility(Field field, Object role, UHCPlayer player, ScenarioRole scenarioRole)
            throws Exception {

        if (!Ability.class.isAssignableFrom(field.getType())) {
            throw new IllegalStateException(
                    "@RoleVariable(type=ABILITY) doit être Ability");
        }

        Ability base = (Ability) field.get(role);
        System.out.println(base.getName()+" active :" + base.active() + " cooldown : " + base.getCooldown() + "max use : " + base.getMaxUse());
        if (base == null || !base.active()) return;

        Ability instance = base.clone();
        System.out.println(instance.getName()+"nouveaux active :" + instance.active() + " cooldown : " + instance.getCooldown() + "max use : " + instance.getMaxUse());
        scenarioRole.getRoleByUHCPlayer(player).getAbilities().add(instance);
    }
}
