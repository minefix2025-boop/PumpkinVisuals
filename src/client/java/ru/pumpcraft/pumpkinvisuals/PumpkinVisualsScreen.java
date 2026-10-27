package ru.pumpcraft.pumpkinvisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.Click;
import net.minecraft.particle.ParticlesMode;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;

/** Fully custom PumpkinVisuals menu: no vanilla-rendered grey buttons. */
public final class PumpkinVisualsScreen extends Screen {
    private static final int BG=0xF00A0807, SIDE=0xFF14100D, CARD=0xFF211811, CARD_HOVER=0xFF302116;
    private static final int ORANGE=0xFFFF8617, ORANGE_DARK=0xFF7E3D12, WHITE=0xFFFFF4E8, MUTED=0xFFBBA99A;
    private static final int GREEN=0xFF63E69B, RED=0xFFFF6666;
    private enum Tab { HOME, COMBAT, VISUALS, WORLD, PLAYER, SETTINGS }
    private Tab tab=Tab.HOME;
    private int left,top,panelW,panelH,sideW,contentX,contentW,scroll;
    private final List<Row> rows=new ArrayList<>();
    private record Row(String title,String description,String status,Runnable action) {}

    public PumpkinVisualsScreen(){super(Text.literal("PumpkinVisuals"));}
    @Override protected void init(){
        panelW=Math.min(900,Math.max(320,width-24)); panelH=Math.min(540,Math.max(250,height-24));
        left=(width-panelW)/2; top=(height-panelH)/2; sideW=Math.min(174,Math.max(116,panelW/4));
        contentX=left+sideW+18; contentW=panelW-sideW-34; rebuildRows(); rebuildWidgets();
    }
    private static String on(boolean v){return v?"ON":"OFF";}
    private void particles(){var o=MinecraftClient.getInstance().options; PumpkinVisualsClient.reducedParticles=!PumpkinVisualsClient.reducedParticles; o.getParticles().setValue(PumpkinVisualsClient.reducedParticles?ParticlesMode.MINIMAL:ParticlesMode.ALL);}
    private void fullBright(){var o=MinecraftClient.getInstance().options; double g=o.getGamma().getValue(); o.getGamma().setValue(g<5?16.0:1.0);}
    private void bobbing(){var o=MinecraftClient.getInstance().options; o.getBobView().setValue(!o.getBobView().getValue());}
    private void fov(){var o=MinecraftClient.getInstance().options; o.getFov().setValue(o.getFov().getValue()<100?110:90);}
    private void viewDistance(){var o=MinecraftClient.getInstance().options; o.getViewDistance().setValue(o.getViewDistance().getValue()<12?12:8);}
    private void entityDistance(){var o=MinecraftClient.getInstance().options; o.getEntityDistanceScaling().setValue(o.getEntityDistanceScaling().getValue()<1.5?2.0:1.0);}
    private void rebuildRows(){
        rows.clear();
        switch(tab){
            case HOME -> {
                rows.add(toggle("TriggerBot","Attack the entity under your crosshair",()->PumpkinVisualsClient.triggerBotEnabled,v->PumpkinVisualsClient.triggerBotEnabled=v));
                rows.add(toggle("KillAura","Automatically attack nearby targets",()->PumpkinVisualsClient.killAuraEnabled,v->PumpkinVisualsClient.killAuraEnabled=v));
                rows.add(toggle("Ore X-Ray","Outline ores in the loaded area",()->PumpkinVisualsClient.xrayEnabled,v->PumpkinVisualsClient.xrayEnabled=v));
                rows.add(toggle("Player ESP","Outline players in the world",()->PumpkinVisualsClient.playerEspEnabled,v->PumpkinVisualsClient.playerEspEnabled=v));
                rows.add(toggle("Auto Eat","Eat food when hunger is low",()->PumpkinVisualsClient.autoEatEnabled,v->PumpkinVisualsClient.autoEatEnabled=v));
                rows.add(toggle("FullBright","Switch gamma between normal and maximum",()->MinecraftClient.getInstance().options.getGamma().getValue()>5,v->fullBright()));
            }
            case COMBAT -> {
                rows.add(toggle("TriggerBot","Attacks when a living target is under the crosshair",()->PumpkinVisualsClient.triggerBotEnabled,v->PumpkinVisualsClient.triggerBotEnabled=v));
                rows.add(toggle("KillAura","Attacks the nearest living target",()->PumpkinVisualsClient.killAuraEnabled,v->PumpkinVisualsClient.killAuraEnabled=v));
                rows.add(new Row("Attack cooldown","Minecraft attack cooldown is respected","VANILLA",()->{}));
                rows.add(new Row("Reach limit","Current safe client range: 3.2 blocks","3.2",()->{}));
            }
            case VISUALS -> {
                rows.add(toggle("Ore X-Ray","Highlight nearby ore blocks",()->PumpkinVisualsClient.xrayEnabled,v->PumpkinVisualsClient.xrayEnabled=v));
                rows.add(toggle("Player ESP","Orange outlines around players",()->PumpkinVisualsClient.playerEspEnabled,v->PumpkinVisualsClient.playerEspEnabled=v));
                rows.add(toggle("Reduced Particles","Use Minecraft minimal-particles mode",()->PumpkinVisualsClient.reducedParticles,v->particles()));
                rows.add(toggle("FullBright","Gamma: normal / maximum",()->MinecraftClient.getInstance().options.getGamma().getValue()>5,v->fullBright()));
                rows.add(toggle("Smooth Camera","Toggle view bobbing",()->MinecraftClient.getInstance().options.getBobView().getValue(),v->bobbing()));
                rows.add(toggle("Wide FOV","Switch between 90 and 110 FOV",()->MinecraftClient.getInstance().options.getFov().getValue()>=100,v->fov()));
                rows.add(toggle("Long View","Switch render distance between 8 and 12 chunks",()->MinecraftClient.getInstance().options.getViewDistance().getValue()>=12,v->viewDistance()));
                rows.add(toggle("Entity Distance+","Switch entity distance scaling",()->MinecraftClient.getInstance().options.getEntityDistanceScaling().getValue()>=1.5,v->entityDistance()));
            }
            case WORLD -> {
                rows.add(toggle("Ore X-Ray","Scan ores around the player",()->PumpkinVisualsClient.xrayEnabled,v->PumpkinVisualsClient.xrayEnabled=v));
                rows.add(toggle("Player ESP","Highlight players in loaded chunks",()->PumpkinVisualsClient.playerEspEnabled,v->PumpkinVisualsClient.playerEspEnabled=v));
                rows.add(toggle("Reduced Particles","Reduce particle clutter",()->PumpkinVisualsClient.reducedParticles,v->particles()));
                rows.add(toggle("Long View","8 / 12 chunk render distance",()->MinecraftClient.getInstance().options.getViewDistance().getValue()>=12,v->viewDistance()));
                rows.add(new Row("Chinese Cone Hat","Cosmetic headwear renderer is being added","COSMETIC",()->{}));
                rows.add(new Row("World Scan","Ore scan radius is 16 blocks","16 BLOCKS",()->{}));
            }
            case PLAYER -> {
                rows.add(toggle("Auto Eat","Use food when hunger reaches 14",()->PumpkinVisualsClient.autoEatEnabled,v->PumpkinVisualsClient.autoEatEnabled=v));
                rows.add(new Row("Auto Eat hotkey","Press Z to switch Auto Eat","Z",()->{}));
                rows.add(toggle("Smooth Camera","Toggle view bobbing",()->MinecraftClient.getInstance().options.getBobView().getValue(),v->bobbing()));
                rows.add(toggle("Wide FOV","Toggle 90 / 110 FOV",()->MinecraftClient.getInstance().options.getFov().getValue()>=100,v->fov()));
                rows.add(new Row("Player status","Local client player status","LIVE",()->{}));
            }
            case SETTINGS -> {
                rows.add(new Row("Open menu","Press RIGHT SHIFT at any time","RSHIFT",()->{}));
                rows.add(new Row("Auto Eat key","Press Z to toggle Auto Eat","Z",()->{}));
                rows.add(new Row("Client version","Fabric / Minecraft 1.21.6","1.21.6",()->{}));
                rows.add(new Row("Interface","Custom Pumpkin orange theme","CUSTOM",()->{}));
                rows.add(new Row("Reset visual options","Restore common visual values", "RESET",()->{var o=MinecraftClient.getInstance().options;o.getGamma().setValue(1.0);o.getFov().setValue(90);o.getViewDistance().setValue(8);o.getEntityDistanceScaling().setValue(1.0);o.getBobView().setValue(true);}));
            }
        }
    }
    private interface Getter { boolean get(); }
    private interface Setter { void set(boolean value); }
    private Row toggle(String title,String desc,Getter getter,Setter setter){
        return new Row(title,desc,on(getter.get()),()->setter.set(!getter.get()));
    }
    private void rebuildWidgets(){
        clearChildren();
        String[] labels={"HOME","COMBAT","VISUALS","WORLD","PLAYER","SETTINGS"}; Tab[] tabs=Tab.values();
        int ny=top+94, navX=left+10, navW=sideW-20;
        for(int i=0;i<tabs.length;i++){ final Tab t=tabs[i]; final String label=labels[i]; addDrawableChild(new SkinButton(navX,ny+i*34,navW,27,Text.literal(label),()->{tab=t;scroll=0;rebuildRows();rebuildWidgets();},()->tab==t?ORANGE_DARK:CARD));}
        int y0=top+112,rowH=54,bottom=top+panelH-50,btnW=Math.min(94,Math.max(72,contentW/4));
        for(int i=0;i<rows.size();i++){int y=y0+i*61-scroll;if(y<top+105||y+rowH>bottom)continue;Row r=rows.get(i);int bx=left+panelW-btnW-14;
            addDrawableChild(new SkinButton(bx,y+14,btnW,27,Text.literal(r.status().equals("ON")?"● ON":r.status().equals("OFF")?"○ OFF":r.status()),()->{r.action().run();rebuildRows();rebuildWidgets();},()->r.status().equals("ON")?0xFF17442D:r.status().equals("OFF")?0xFF49201A:CARD_HOVER));
        }
        addDrawableChild(new SkinButton(left+panelW-98,top+panelH-34,84,23,Text.literal("CLOSE  ×"),this::close,()->ORANGE_DARK));
    }
    private final class SkinButton extends ClickableWidget {
        private final Runnable action; private final java.util.function.IntSupplier color;
        SkinButton(int x,int y,int w,int h,Text text,Runnable action,java.util.function.IntSupplier color){super(x,y,w,h,text);this.action=action;this.color=color;}
        @Override protected void renderWidget(DrawContext ctx,int mouseX,int mouseY,float delta){
            int c=isHovered()?CARD_HOVER:color.getAsInt();ctx.fill(getX(),getY(),getX()+width,getY()+height,c);
            ctx.fill(getX(),getY(),getX()+3,getY()+height,isFocused()?WHITE:ORANGE);
            ctx.fill(getX()+3,getY()+height-2,getX()+width,getY()+height,isHovered()?ORANGE:ORANGE_DARK);
            int tw=textRenderer.getWidth(getMessage());ctx.drawTextWithShadow(textRenderer,getMessage(),getX()+Math.max(7,(width-tw)/2),getY()+(height-8)/2,isHovered()?WHITE:0xFFE9D9C9);
        }
        @Override public void onClick(Click click){action.run();}
        @Override protected void appendClickableNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder){appendDefaultNarrations(builder);}
    }
    @Override public void render(DrawContext ctx,int mx,int my,float delta){
        ctx.fill(0,0,width,height,0xC9000000);
        ctx.fill(left-1,top-1,left+panelW+1,top+panelH+1,ORANGE);
        ctx.fill(left,top,left+panelW,top+panelH,BG);ctx.fill(left,top,left+sideW,top+panelH,SIDE);
        ctx.fill(left,top,left+panelW,top+4,ORANGE);ctx.fill(left+sideW,top+4,left+sideW+1,top+panelH-1,ORANGE_DARK);
        ctx.drawTextWithShadow(textRenderer,"PUMPKIN",left+14,top+17,ORANGE);ctx.drawTextWithShadow(textRenderer,"VISUALS",left+14,top+32,WHITE);
        ctx.drawTextWithShadow(textRenderer,"CLIENT  /  1.21.6",left+14,top+51,MUTED);ctx.fill(left+10,top+74,left+sideW-10,top+75,ORANGE_DARK);
        ctx.drawTextWithShadow(textRenderer,"PUMPKINVISUALS",contentX,top+17,WHITE);ctx.drawTextWithShadow(textRenderer,"CUSTOM CLIENT INTERFACE  •  BETA",contentX,top+34,MUTED);
        ctx.fill(contentX,top+54,left+panelW-14,top+55,ORANGE_DARK);
        String status=MinecraftClient.getInstance().player==null?"NOT IN WORLD":"CLIENT ACTIVE";
        ctx.fill(contentX,top+64,left+panelW-14,top+88,CARD);ctx.fill(contentX,top+64,contentX+3,top+88,ORANGE);
        ctx.drawTextWithShadow(textRenderer,"◆ STATUS",contentX+10,top+72,ORANGE);ctx.drawTextWithShadow(textRenderer,status,left+panelW-textRenderer.getWidth(status)-23,top+72,GREEN);
        ctx.drawTextWithShadow(textRenderer,tab.name()+" MODULES",contentX,top+94,ORANGE);
        int y0=top+112,rowH=54,bottom=top+panelH-50;ctx.enableScissor(contentX,top+105,left+panelW-12,bottom);
        for(int i=0;i<rows.size();i++){int y=y0+i*61-scroll;if(y+rowH<top+105||y>bottom)continue;Row r=rows.get(i);
            ctx.fill(contentX,y,left+panelW-14,y+rowH,CARD);ctx.fill(contentX,y,contentX+3,y+rowH,ORANGE);
            ctx.drawTextWithShadow(textRenderer,r.title(),contentX+12,y+9,WHITE);
            String desc=trim(r.description(),Math.max(30,contentW-120));ctx.drawTextWithShadow(textRenderer,desc,contentX+12,y+29,MUTED);
        }ctx.disableScissor();
        ctx.fill(contentX,top+panelH-43,left+panelW-14,top+panelH-42,ORANGE_DARK);
        ctx.drawTextWithShadow(textRenderer,"PUMPKINVISUALS  •  CUSTOM UI",contentX,top+panelH-31,ORANGE);
        super.render(ctx,mx,my,delta);
    }
    private String trim(String s,int max){if(textRenderer.getWidth(s)<=max)return s;while(!s.isEmpty()&&textRenderer.getWidth(s+"...")>max)s=s.substring(0,s.length()-1);return s.isEmpty()?"":s+"...";}
    @Override public boolean mouseScrolled(double mx,double my,double hx,double vy){int max=Math.max(0,rows.size()*61-(panelH-175));scroll=Math.max(0,Math.min(max,scroll-(int)(vy*30)));rebuildWidgets();return true;}
    @Override public boolean shouldPause(){return false;}
}