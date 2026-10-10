package com.soof.nuclearcompat;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

/**
 * 核心桥接逻辑：拦截原子核动的辐射效果施加，改为施加 Alex's Caves Up 的辐照效果。
 *
 * 为什么用 {@link MobEffectEvent.Applicable} 而不是 {@link MobEffectEvent.Added}：
 * {@code Added} 不可取消，且事件触发时效果尚未写入实体，无法真正阻止原效果生效；
 * {@code Applicable} 在 {@code LivingEntity#canBeAffected} 阶段触发，可通过
 * {@code setResult(Result.DO_NOT_APPLY)} 在效果生效前彻底拦截，是最干净的替换点。
 */
@EventBusSubscriber(modid = NuclearCompat.MODID)
public final class RadiationCompatHandler {

    private RadiationCompatHandler() {
    }

    /**
     * 拦截 createnuclear:radiation 的施加：
     * 1. 阻止原辐射效果生效；
     * 2. 以相同的等级/时长/氛围/可见性施加 alexscaves:irradiated。
     */
    @SubscribeEvent
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance == null) {
            return;
        }

        LivingEntity entity = event.getEntity();

        // 注意：不能直接 == 比较 Holder。Create Nuclear 传入的是 DeferredHolder，
        // 而 BuiltInRegistries 返回的是 Holder.Reference，两者是不同对象。
        // 用 Holder.is(ResourceLocation) 按注册名比较才可靠。
        boolean isCnRadiation = instance.getEffect().is(NuclearCompat.CN_RADIATION_ID);
        boolean isAcIrradiated = instance.getEffect().is(NuclearCompat.AC_IRRADIATED_ID);

        // 只处理两个模组的辐射/辐照效果，其余效果一律不干预。
        if (!isCnRadiation && !isAcIrradiated) {
            return;
        }

        // 穿戴整套防化服或防辐射服：彻底免疫，不施加任何辐射/辐照效果。
        // 这样防化服也能像防辐射服一样，在携带铀棒、站在富集营火上时不受辐射。
        if (ArmorCompat.hasFullHazmatArmor(entity) || ArmorCompat.hasFullAntiRadiationArmor(entity)) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
            return;
        }

        // 其余情况：把原子核动的辐射效果统一替换为 Alex's Caves Up 的辐照效果。
        if (isCnRadiation && NuclearCompat.AC_IRRADIATED != null) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);

            // CN 的 applyEffects 每个 tick 都会重新施加辐射效果，若每次都刷新 duration，
            // 会导致辐照效果的倒计时永不递减、缓慢扣血（applyEffectTick）永不触发。
            // 因此：仅当实体没有辐照效果、或辐射等级发生变化时才重新施加，
            // 其余情况保持现有效果，让 duration 正常流转。
            MobEffectInstance existing = entity.getEffect(NuclearCompat.AC_IRRADIATED);
            if (existing != null && existing.getAmplifier() == instance.getAmplifier()) {
                return;
            }

            entity.addEffect(new MobEffectInstance(
                    NuclearCompat.AC_IRRADIATED,
                    instance.getDuration(),
                    instance.getAmplifier(),
                    instance.isAmbient(),
                    instance.isVisible()
            ));
        }
    }

    /**
     * 同步清除：当原子核动试图移除自己的辐射效果（辐射源消失或等级变化）时，
     * 一并移除 Alex's Caves Up 的辐照效果，保持与原版「离开辐射区立即解除」一致。
     */
    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (NuclearCompat.CN_RADIATION == null || NuclearCompat.AC_IRRADIATED == null) {
            return;
        }

        if (event.getEffect().is(NuclearCompat.CN_RADIATION_ID)) {
            event.getEntity().removeEffect(NuclearCompat.AC_IRRADIATED);
        }
    }
}
