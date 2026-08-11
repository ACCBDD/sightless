package com.accbdd.sightless.item;

import com.accbdd.sightless.register.SightlessParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;

public class LidarItem extends Item {
    public LidarItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        double maxDistance = 40.0;
        double spread = 0.07;

        if (level instanceof ServerLevel serverLevel) {
//            double totalDistance = start.distanceTo(impactPos);
//            double stepSize = 0.25;
//
//            for (double d = 0; d < totalDistance; d += stepSize) {
//                double ratio = d / totalDistance;
//                double x = start.x + (impactPos.x - start.x) * ratio;
//                double y = start.y + (impactPos.y - start.y) * ratio;
//                double z = start.z + (impactPos.z - start.z) * ratio;
//
//                serverLevel.sendParticles(
//                        ParticleTypes.FLAME,
//                        x, y, z,
//                        1,
//                        0.0, 0.0, 0.0,
//                        0.0
//                );
//            }
            Vec3 look = player.getLookAngle();
            Vec3 right;
            if (Math.abs(look.y) > 0.99) {
                right = new Vec3(1, 0, 0);
            } else {
                right = look.cross(new Vec3(0, 1, 0)).normalize();
            }
            Vec3 up = right.cross(look).normalize();
            double fuzziness = 1;
            RandomSource random = level.getRandom();

            for (int x = -10; x <= 10; x++) {
                for (int y = -10; y <= 10; y++) {
                    double offsetX = x + (random.nextDouble() - 0.5) * fuzziness;
                    double offsetY = y + (random.nextDouble() - 0.5) * fuzziness;

                    Vec3 rayDir = look.add(right.scale(offsetX * spread))
                            .add(up.scale(offsetY * spread))
                            .normalize();

                    HitResult finalHit = getHit(player, rayDir, maxDistance);

                    if (finalHit.getType() != HitResult.Type.MISS) {
                        Vec3 impactPos = finalHit.getLocation();
                        double distance = player.getEyePosition().distanceTo(impactPos);

                        float ratio = (float) Math.min(distance / maxDistance, 1.0);
                        Vec3 color = new Vec3(1, 0, 0);
                        if (finalHit.getType() == HitResult.Type.BLOCK) {
                            color = new Vec3(
                                    Math.min(1, ratio * 2F),
                                    0.8F - (ratio * 0.8F * 0.5F),
                                    0.8F - ratio * 0.8F);
                        }

                        serverLevel.sendParticles(
                                SightlessParticleTypes.LIDAR_PARTICLE.get(),
                                impactPos.x, impactPos.y, impactPos.z,
                                0,                  // MUST BE 0 to pass exact RGB parameters
                                color.x, color.y, color.z,   // r g b passed as speed
                                1.0
                        );
                    }
                }
            }
        }
        return super.use(level, player, hand);
    }

    private HitResult getHit(Player player, Vec3 angle, double maxDistance) {
        Vec3 start = player.getEyePosition();
        Vec3 targetEnd = start.add(angle.x * maxDistance, angle.y * maxDistance, angle.z * maxDistance);

        ClipContext clipContext = new ClipContext(
                start,
                targetEnd,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        );
        BlockHitResult blockHit = player.level().clip(clipContext);

        Vec3 actualEnd = blockHit.getType() != HitResult.Type.MISS ? blockHit.getLocation() : targetEnd;

        AABB searchBox = player.getBoundingBox().expandTowards(angle.scale(maxDistance)).inflate(1.0);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(player, start, actualEnd, searchBox, entity -> !entity.isSpectator() && entity.isPickable(), maxDistance * maxDistance);

        return entityHit != null ? entityHit : blockHit;
    }
}
