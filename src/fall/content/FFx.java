package fall.content;

import arc.util.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import mindustry.entities.*;
import mindustry.graphics.*;

public class FFx{
    
    public static Effect
    //stolen yet not stolen from betamindy...
    smokeRise = new Effect(120f, e -> {
        Draw.z(Layer.flyingUnit);
        Draw.color(Color.gray);
        Draw.alpha(e.fout());
        Fill.circle(e.x + (e.fin() * 27f), e.y + (e.fin() * 30f), e.fin() * 21f);
    });
}
