package fall.content;

import arc.*;
import arc.util.*;
import mindustry.*;
import fall.world.blocks.*;
import fall.content.*;

public class FBlocks{
    
    public static void load(){
        Vars.content.units().each(u -> {
            new FallenUnit(u, FFx.smokeRise, false){{
                health = (int)u.health / 3;
            }};
            new FallenUnit(u, FFx.smokeRise, true){{
                health = (int)u.health / 3;
            }};
            Log.info("Created new FallenUnitBlock for unit '" + u.name + "'");
        });
    }
}
