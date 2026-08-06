package com.cuddly.heartbound.command;

import com.cuddly.heartbound.Heartbound;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class Commands {
   public static void register() {
      Heartbound.LOGGER.info("Registering Commands for Heartbound");
      CommandRegistrationCallback.EVENT.register(GirlsCommand::register);
   }
}
