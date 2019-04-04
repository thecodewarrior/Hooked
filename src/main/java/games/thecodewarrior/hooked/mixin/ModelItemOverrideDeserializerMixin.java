package games.thecodewarrior.hooked.mixin;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import games.thecodewarrior.hooked.mixinsupport.NbtSensitiveModelItemOverride;
import net.minecraft.client.render.model.json.ModelItemOverride;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Type;

@Mixin(ModelItemOverride.Deserializer.class)
public class ModelItemOverrideDeserializerMixin {
	@Inject(at = @At("HEAD"), method = "deserialize")
	private void deserializeAddPredicate(JsonElement json, Type type, JsonDeserializationContext var3, CallbackInfoReturnable<ModelItemOverride> info) {
		if(!json.isJsonObject()) return;
		JsonObject obj = json.getAsJsonObject();
		if(obj.has("nbt") && !obj.has("predicate")) {
		    obj.add("predicate", new JsonObject());
		}
	}

	@Inject(at = @At("TAIL"), method = "deserialize", cancellable = true)
	private void deserializeNBT(JsonElement json, Type type, JsonDeserializationContext var3, CallbackInfoReturnable<ModelItemOverride> info) {
	    if(!json.isJsonObject()) return;
		JsonElement nbt = json.getAsJsonObject().get("nbt");
	    if(nbt != null) {
	    	info.setReturnValue(NbtSensitiveModelItemOverride.deserialize(nbt, info.getReturnValue()));
		}
	}
}
