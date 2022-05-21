package fall.world.blocks;

import arc.math.*;
import arc.util.io.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import mindustry.graphics.*;

public class FallenUnitBlock extends Block{
    /** The unit drawn on the block. */
    public UnitType unit;
    
    public FallenUnitBlock(String name, UnitType unit){
        super(name);
        this.unit = unit;
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
            Drawf.shadow(unit.region, x, y, rot);
            Drawf.rect(unit.shadowRegion, x, y, rot);
        }
        
        @Override
        public void write(Writes write){
            super.write(write);
            
            write.f(rot);
        }
        
        @Override
        public void read(Reads read){
            super.read(read);
            
            rot = read.f();
        }
    }
}
