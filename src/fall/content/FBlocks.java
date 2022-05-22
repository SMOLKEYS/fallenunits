package fall.content;

import arc.*;
import arc.util.*;
import mindustry.*;
import fall.world.blocks.*;
import fall.content.*;

public class FBlocks{
    
    public static void load(){
        Vars.content.units().each(u -> {
            new FallenUnitBlock("fallen-" + u.name, u, FFx.smokeRise){{
                health = 40;
            }};
            Log.info("Created new FallenUnitBlock for unit '" + u.name + "'");
        });
    }
}
