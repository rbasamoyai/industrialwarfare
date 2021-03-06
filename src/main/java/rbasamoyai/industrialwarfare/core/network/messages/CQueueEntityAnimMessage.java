package rbasamoyai.industrialwarfare.core.network.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Tuple;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import rbasamoyai.industrialwarfare.core.network.handlers.CQueueEntityAnimHandler;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

public class CQueueEntityAnimMessage {

	private int id;
	private String controller;
	private List<Tuple<String, Boolean>> anim;
	private float speed;
	
	public CQueueEntityAnimMessage() {}
	
	public CQueueEntityAnimMessage(int id, String controller, List<Tuple<String, Boolean>> anim, float speed) {
		this.id = id;
		this.controller = controller;
		this.anim = anim;
		this.speed = speed;
	}
	
	public AnimationBuilder makeAnim() {
		AnimationBuilder builder = new AnimationBuilder();
		for (Tuple<String, Boolean> a : this.anim) {
			builder.addAnimation(a.getA(), a.getB());
		}
		return builder;
	}
	
	public int id() { return this.id; }
	public String controller() { return this.controller; }
	public float speed() { return this.speed; }
	
	public static void encode(CQueueEntityAnimMessage msg, FriendlyByteBuf buf) {
		buf
				.writeVarInt(msg.id)
				.writeUtf(msg.controller)
				.writeFloat(msg.speed);
		
		buf.writeVarInt(msg.anim.size());
		for (Tuple<String, Boolean> a : msg.anim) {
			buf
					.writeUtf(a.getA())
					.writeBoolean(a.getB());
		}
	}
	
	public static CQueueEntityAnimMessage decode(FriendlyByteBuf buf) {
		int id = buf.readVarInt();
		String controllerName = buf.readUtf();
		float speed = buf.readFloat();
		int sz = buf.readVarInt();
		
		List<Tuple<String, Boolean>> anim = new ArrayList<>();
		for (int i = 0; i < sz; ++i) {
			String animName = buf.readUtf();
			boolean shouldLoop = buf.readBoolean();
			anim.add(new Tuple<>(animName, shouldLoop));
		}
		
		return new CQueueEntityAnimMessage(id, controllerName, anim, speed);
	}
	
	public static void handle(CQueueEntityAnimMessage msg, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context ctx = contextSupplier.get();
		ctx.enqueueWork(() -> {
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> CQueueEntityAnimHandler.handle(msg));
		});
		ctx.setPacketHandled(true);
	}
	
}
