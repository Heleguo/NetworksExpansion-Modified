package com.balugaq.netex.utils;

import com.balugaq.netex.api.enums.MinecraftVersion;
import io.github.sefiraat.networks.Networks;
import lombok.experimental.UtilityClass;
import org.bukkit.Particle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;

@UtilityClass
public class NetworksVersionedParticle {
    public static final Particle DUST;
    public static final Particle EXPLOSION;
    public static final Particle SMOKE;

    static {
        MinecraftVersion version = Networks.getInstance().getMCVersion();
        DUST = version.isAtLeast(MinecraftVersion.MC1_20_5) ? Particle.DUST : getKey("REDSTONE");
        EXPLOSION = version.isAtLeast(MinecraftVersion.MC1_20_5) ? Particle.EXPLOSION : getKey("EXPLOSION_LARGE");
        SMOKE = version.isAtLeast(MinecraftVersion.MC1_20_5) ? Particle.SMOKE : getKey("SMOKE_NORMAL");
    }

    @Nullable
    private static Particle getKey(@Nonnull String key) {
        try {
            Field field = Particle.class.getDeclaredField(key);
            return (Particle) field.get((Object) null);
        } catch (Exception ignored) {
            return null;
        }
    }
}
