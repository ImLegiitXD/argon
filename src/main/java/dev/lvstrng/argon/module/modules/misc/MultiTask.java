package dev.lvstrng.argon.module.modules.misc;

import dev.lvstrng.argon.module.Category;
import dev.lvstrng.argon.module.Module;
import dev.lvstrng.argon.module.setting.BooleanSetting;
import dev.lvstrng.argon.utils.EncryptedString;

public final class MultiTask extends Module {

    private final BooleanSetting attackingEntities = new BooleanSetting(EncryptedString.of("Attacking Entities"), false)
            .setDescription(EncryptedString.of(""));

    public MultiTask() {
        super(EncryptedString.of("Multi Actions"),
                EncryptedString.of("Allows the player to do multi actions like eat while breaking, etc"),
                -1,
                Category.MISC);
        addSettings(attackingEntities);
    }

    public boolean attackingEntities() {
        return isEnabled() && attackingEntities.getValue();
    }
}
