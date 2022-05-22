package fall.world.blocks;

import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import mindustry.entities.*;
import mindustry.graphics.*;

public class FallenUnit extends Block{
    /** The unit drawn on the block. */
    public UnitType unit;
    /** Idle effect. */
    public Effect effect;
    /** Whether or not the weapons should also be on the unit. */
    public boolean weaponsAttached;
    
    public FallenUnit(UnitType unit, Effect eff, boolean wa){
        super(wa ? "fallen-" + unit.name + "-weapons-att" : "fallen-" + unit.name);
        this.unit = unit;
        effect = eff;
        weaponsAttached = wa;
        update = true;
        solid = true;
        destructible = true;
        hasShadow = false;
        uiIcon = wa ? unit.fullIcon : unit.region;
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
            
            if(weaponsAttached){
                Draw.rect(unit.fullIcon, x, y, rot);
            } else {
                Draw.rect(unit.region, x, y, rot);
            }
            
            if(unit.drawCell){
                Draw.color(Tmp.c1.set(Color.gray).lerp(team.color, Mathf.absin(4f, 1f)));
                Draw.rect(unit.cellRegion, x, y, rot);
            }
            
            Draw.z(Layer.groundUnit - 0.001f);
            Draw.color(Color.black);
            Draw.rect("circle-shadow", x, y, unit.hitSize * 2.3f, unit.hitSize * 2.3f);
            
            //increase smoke effect likelihood for bigger units
            if(Mathf.chance(unit.hitSize / 2000f)){
                effect.at(x + Mathf.range(unit.hitSize / 2f), y + Mathf.range(unit.hitSize / 2f));
            }
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
