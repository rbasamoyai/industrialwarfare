package rbasamoyai.industrialwarfare.core.init;

import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entityai.formation.UnitFormationType;
import rbasamoyai.industrialwarfare.common.entityai.formation.formations.LineFormation;

public class UnitFormationTypeInit {

	public static final DeferredRegister<UnitFormationType<?>> UNIT_FORMATION_TYPES = DeferredRegister.create(UnitFormationType.CLASS_GENERIC, IndustrialWarfare.MOD_ID);
	
	public static final RegistryObject<UnitFormationType<LineFormation>> LINE = UNIT_FORMATION_TYPES.register("line",
			() -> new UnitFormationType<>(level -> new LineFormation(level, -1, 0, 0)));
	
}
