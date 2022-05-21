package example;

import arc.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;
import fall.content.*;

public class ExampleJavaMod extends Mod{

    public ExampleJavaMod(){
        
    }

    @Override
    public void loadContent(){
        FBlocks.load();
    }

}
