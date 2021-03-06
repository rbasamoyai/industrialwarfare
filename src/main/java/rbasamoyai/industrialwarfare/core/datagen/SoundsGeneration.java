package rbasamoyai.industrialwarfare.core.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SoundDefinition;
import net.minecraftforge.common.data.SoundDefinition.Sound;
import net.minecraftforge.common.data.SoundDefinitionsProvider;
import rbasamoyai.industrialwarfare.IndustrialWarfare;

public class SoundsGeneration extends SoundDefinitionsProvider {
	
	protected SoundsGeneration(DataGenerator generator, ExistingFileHelper helper) {
		super(generator, IndustrialWarfare.MOD_ID, helper);
	}

	@Override
	public void registerSounds() {
		add(loc("item.rifle.cycle_end"), SoundDefinition.definition()
				.with(Sound.sound(loc("firearms/rifle/cycle_end1"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(0.9f)
						.attenuationDistance(4))
				.with(Sound.sound(loc("firearms/rifle/cycle_end1"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(1.0f)
						.attenuationDistance(4))
				.with(Sound.sound(loc("firearms/rifle/cycle_end2"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(0.9f)
						.attenuationDistance(4))
				.with(Sound.sound(loc("firearms/rifle/cycle_end2"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(1.0f)
						.attenuationDistance(4)));

		add(loc("item.rifle.cycle_start"), SoundDefinition.definition()
				.with(Sound.sound(loc("firearms/rifle/cycle_start1"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(0.9f)
						.attenuationDistance(4))
				.with(Sound.sound(loc("firearms/rifle/cycle_start1"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(1.0f)
						.attenuationDistance(4))
				.with(Sound.sound(loc("firearms/rifle/cycle_start2"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(0.9f)
						.attenuationDistance(4))
				.with(Sound.sound(loc("firearms/rifle/cycle_start2"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(1.0f)
						.attenuationDistance(4)));

		add(loc("item.rifle.fired"), SoundDefinition.definition()
				.subtitle("subtitle.industrialwarfare.item.rifle.fired")
				.with(Sound.sound(loc("firearms/rifle/fired"), SoundDefinition.SoundType.SOUND)
						.volume(1.5f)
						.pitch(0.8f)
						.attenuationDistance(64))
				.with(Sound.sound(loc("firearms/rifle/fired"), SoundDefinition.SoundType.SOUND)
						.volume(1.5f)
						.pitch(0.9f)
						.attenuationDistance(64))
				.with(Sound.sound(loc("firearms/rifle/fired"), SoundDefinition.SoundType.SOUND)
						.volume(1.5f)
						.pitch(1.0f)
						.attenuationDistance(64)));
		
		add(loc("item.revolver.fired"), SoundDefinition.definition()
				.subtitle("subtitle.industrialwarfare.item.revolver.fired")
				.with(Sound.sound(loc("firearms/revolver/fired"), SoundDefinition.SoundType.SOUND)
						.volume(1.5f)
						.pitch(0.8f)
						.attenuationDistance(64))
				.with(Sound.sound(loc("firearms/revolver/fired"), SoundDefinition.SoundType.SOUND)
						.volume(1.5f)
						.pitch(0.9f)
						.attenuationDistance(64))
				.with(Sound.sound(loc("firearms/revolver/fired"), SoundDefinition.SoundType.SOUND)
						.volume(1.5f)
						.pitch(1.0f)
						.attenuationDistance(64)));
		
		add(loc("item.heavy_rifle.fired"), SoundDefinition.definition()
				.subtitle("subtitle.industrialwarfare.item.rifle.fired")
				.with(Sound.sound(loc("firearms/heavy_rifle/fired"), SoundDefinition.SoundType.SOUND)
						.volume(1.5f)
						.pitch(0.8f)
						.attenuationDistance(80))
				.with(Sound.sound(loc("firearms/heavy_rifle/fired"), SoundDefinition.SoundType.SOUND)
						.volume(1.5f)
						.pitch(0.9f)
						.attenuationDistance(80))
				.with(Sound.sound(loc("firearms/heavy_rifle/fired"), SoundDefinition.SoundType.SOUND)
						.volume(1.5f)
						.pitch(1.0f)
						.attenuationDistance(80)));
		
		add(loc("item.sniper_rifle.fired"), SoundDefinition.definition()
				.subtitle("subtitle.industrialwarfare.item.rifle.fired")
				.with(Sound.sound(loc("firearms/sniper_rifle/fired"), SoundDefinition.SoundType.SOUND)
						.volume(1.5f)
						.pitch(0.8f)
						.attenuationDistance(80))
				.with(Sound.sound(loc("firearms/sniper_rifle/fired"), SoundDefinition.SoundType.SOUND)
						.volume(1.5f)
						.pitch(0.9f)
						.attenuationDistance(80))
				.with(Sound.sound(loc("firearms/sniper_rifle/fired"), SoundDefinition.SoundType.SOUND)
						.volume(1.5f)
						.pitch(1.0f)
						.attenuationDistance(80)));

		add(loc("item.firearms.hammer_click"), SoundDefinition.definition()
				.with(Sound.sound(loc("firearms/hammer_click"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(0.9f)
						.attenuationDistance(4))
				.with(Sound.sound(loc("firearms/hammer_click"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(0.95f)
						.attenuationDistance(4))
				.with(Sound.sound(loc("firearms/hammer_click"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(1.0f)
						.attenuationDistance(4)));
		
		add(loc("item.firearms.lever_open"), SoundDefinition.definition()
				.with(Sound.sound(loc("firearms/lever_action_open"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(0.9f)
						.attenuationDistance(4))
				.with(Sound.sound(loc("firearms/lever_action_open"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(0.95f)
						.attenuationDistance(4))
				.with(Sound.sound(loc("firearms/lever_action_open"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(1.0f)
						.attenuationDistance(4)));
		
		add(loc("item.firearms.lever_close"), SoundDefinition.definition()
				.with(Sound.sound(loc("firearms/lever_action_close"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(0.9f)
						.attenuationDistance(4))
				.with(Sound.sound(loc("firearms/lever_action_close"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(0.95f)
						.attenuationDistance(4))
				.with(Sound.sound(loc("firearms/lever_action_close"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(1.0f)
						.attenuationDistance(4)));

		add(loc("item.firearms.insert_ammo"), SoundDefinition.definition()
				.with(Sound.sound(innerLoc("minecraft:random/click"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(0.97f)
						.attenuationDistance(4))
				.with(Sound.sound(innerLoc("minecraft:random/click"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(1.0f)
						.attenuationDistance(4))
				.with(Sound.sound(innerLoc("minecraft:random/click"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(1.03f)
						.attenuationDistance(4)));
		
		add(loc("item.firearms.breathe_on_match"), SoundDefinition.definition()
				.with(Sound.sound(innerLoc("minecraft:random/breath"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(1.5f)
						.attenuationDistance(4))
				.with(Sound.sound(innerLoc("minecraft:random/breath"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(1.75f)
						.attenuationDistance(4))
				.with(Sound.sound(innerLoc("minecraft:random/breath"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(2.0f)
						.attenuationDistance(4)));
		
		add(loc("item.firearms.extinguish_match"), SoundDefinition.definition()
				.with(Sound.sound(innerLoc("minecraft:dig/cloth1"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.attenuationDistance(4))
				.with(Sound.sound(innerLoc("minecraft:dig/cloth2"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.attenuationDistance(4))
				.with(Sound.sound(innerLoc("minecraft:dig/cloth3"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.attenuationDistance(4))
				.with(Sound.sound(innerLoc("minecraft:dig/cloth4"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.attenuationDistance(4)));
		
		add(loc("item.firearms.spit_cartridge_tab"), SoundDefinition.definition()
				.with(Sound.sound(innerLoc("minecraft:mob/llama/spit1"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(1.0f)
						.attenuationDistance(4))
				.with(Sound.sound(innerLoc("minecraft:mob/llama/spit2"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(1.0f)
						.attenuationDistance(4)));
		
		add(loc("item.firearms.ram_charge"), SoundDefinition.definition()
				.with(Sound.sound(innerLoc("minecraft:dig/stone2"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(2.0f)
						.attenuationDistance(4)));
		
		add(loc("item.match_coil.cut"), SoundDefinition.definition()
				.with(Sound.sound(innerLoc("minecraft:mob/sheep/shear"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(1.0f)
						.attenuationDistance(4)));
	}
	
	private ResourceLocation loc(String id) {
		return innerLoc(IndustrialWarfare.MOD_ID + ":" + id);
	}
	
	private ResourceLocation innerLoc(String id) {
		return new ResourceLocation(id);
	}
	
	@Override
	public String getName() {
		return "Industrial Warfare sound definitions";
	}
	
}
