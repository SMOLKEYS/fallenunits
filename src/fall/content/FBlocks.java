package fall.content;

import arc.*;
import arc.util.*;
import mindustry.*;
import fall.world.blocks.*;

public class FBlocks{
    
    public static void load(){
        Vars.content.units().each(u -> {
            new FallenUnitBlock("fallen-" + u.name, u){{
                health = 40;
            }};
            Log.info("Created new FallenUnitBlock for unit '" + u.name + "'");
        });
    }
}
