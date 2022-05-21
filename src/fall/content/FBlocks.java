package fall.content;

import arc.*;
import mindustry.*;
import fall.world.blocks.*;

public class FBlocks{
    
    public static void load(){
        Vars.content.units().each(u -> {
            if(u.region == Core.atlas.error) return;
            
            new FallenUnitBlock(unit){{
                health = 40;
            }};
        });
    }
}