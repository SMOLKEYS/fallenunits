package fall.world.blocks;

import arc.math.*;
import arc.util.io.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import mindustry.entities.*;
import mindustry.graphics.*;

public class FallenUnitBlock extends Block{
    /** The unit drawn on the block. */
    public UnitType unit;
    /** Idle effect. */
    public Effect effect;
    
    public FallenUnitBlock(String name, UnitType unit, Effect eff){
        super(name);
        this.unit = unit;
        effect = eff;
        update = true;
        solid = true;
        destructible = true;
        hasShadow = false;
        buildVisibility = BuildVisibility.shown;
        category = Category.units;
    }
    
    public class FallenUnitBuild extends Building{
        
        public float rot;
        
        @Override
        public void placed(){
            super.placed();
            
            rot = Mathf.random(360f);
        }
        
        @Override
        public void draw(){
            Draw.z(Layer.groundUnit);
            Draw.rect(unit.region, x, y, rot);
            Draw.color(Color.black);
            Draw.rect(unit.cellRegion, x, y, rot);
            Draw.z(Layer.groundUnit - 0.001f);
            Draw.rect(unit.softShadowRegion, x, y, rot);
            
            if(Mathf.chance(0.01f)){
                effect.at(x + Mathf.range(unit.hitSize), y + Mathf.range(unit.hitSize));
            };
        }
        
        @Override
        public void write(Writes write){
            super.write(write);
            
            write.f(rot);
        }
        
        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            
            rot = read.f();
        }
    }
}
