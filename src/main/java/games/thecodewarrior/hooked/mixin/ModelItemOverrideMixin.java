package games.thecodewarrior.hooked.mixin;

import com.google.gson.JsonElement;
import games.thecodewarrior.hooked.mixinsupport.IModelItemOverridePublicFix;
import net.minecraft.client.render.model.json.ModelItemOverride;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Type;

@Mixin(ModelItemOverride.class)
public class ModelItemOverrideMixin implements IModelItemOverridePublicFix {
	@Shadow
	private boolean matches(@NotNull ItemStack stack, @Nullable World world, @Nullable LivingEntity entity) { return false; }

	@Nullable
	@Override
	public Boolean matchesOverride(@NotNull ItemStack stack, @Nullable World world, @Nullable LivingEntity entity) {
		return null;
	}

	@Override
	public boolean matchesAccess(@NotNull ItemStack stack, @Nullable World world, @Nullable LivingEntity entity) {
		return matches(stack, world, entity);
	}

	@Inject(at = @At("HEAD"), method = "matches", cancellable = true)
	private void matchesHook(ItemStack stack, World world, LivingEntity entity, CallbackInfoReturnable<Boolean> info) {
	    Boolean overrideResult = matchesOverride(stack, world, entity);
	    if(overrideResult != null) {
	    	info.setReturnValue(overrideResult);
		}
	}
}
