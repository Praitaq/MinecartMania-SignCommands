package com.afforess.minecartmaniasigncommands.sign;

import com.afforess.minecartmaniacore.config.MinecartManiaConfiguration;
import com.afforess.minecartmaniacore.minecart.MinecartManiaMinecart;
import com.afforess.minecartmaniacore.signs.Sign;
import com.afforess.minecartmaniacore.signs.SignAction;
import com.afforess.minecartmaniacore.utils.StringUtils;

public class SetMaxSpeedAction implements SignAction {
    
    protected int percent = -1;
    
    public SetMaxSpeedAction(final Sign sign) {
        
        for (final String line : sign.getLines()) {
            if (line.toLowerCase().contains("max speed")) {
                final String[] split = line.split(":");
                if (split.length != 2) {
                    continue;
                }
                double percent = Double.parseDouble(StringUtils.getNumber(split[1]));
                percent = Math.min(percent, MinecartManiaConfiguration.getMaximumMinecartSpeedPercent());
                this.percent = (int) percent;
                sign.addBrackets();
                break;
            }
        }
    }
    
    public boolean execute(final MinecartManiaMinecart minecart) {
        minecart.minecart.setMaxSpeed((0.4D * percent) / 100);
        return true;
    }
    
    public boolean async() {
        return true;
    }
    
    public boolean valid(final Sign sign) {
        return percent > 0;
    }
    
    public String getName() {
        return "maxspeedsign";
    }
    
    public String getFriendlyName() {
        return "Max Speed Sign";
    }
    
}
