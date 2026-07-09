package rpg.effect.model;

/**
 * A reusable particle+sound "演出" bundle (SOW: EffectModule) that skills, monster/boss
 * abilities, and orelia-world's CutSceneModule can all trigger by id instead of each
 * hard-coding their own {@code Particle}/{@code Sound} calls.
 */
public final class EffectData {

    private final String id;
    private final String particle;
    private final int particleCount;
    private final double spreadX;
    private final double spreadY;
    private final double spreadZ;
    private final String sound;
    private final float soundVolume;
    private final float soundPitch;

    public EffectData(String id, String particle, int particleCount, double spreadX, double spreadY, double spreadZ,
                       String sound, float soundVolume, float soundPitch) {
        this.id = id;
        this.particle = particle;
        this.particleCount = particleCount;
        this.spreadX = spreadX;
        this.spreadY = spreadY;
        this.spreadZ = spreadZ;
        this.sound = sound;
        this.soundVolume = soundVolume;
        this.soundPitch = soundPitch;
    }

    public String getId() {
        return id;
    }

    public String getParticle() {
        return particle;
    }

    public int getParticleCount() {
        return particleCount;
    }

    public double getSpreadX() {
        return spreadX;
    }

    public double getSpreadY() {
        return spreadY;
    }

    public double getSpreadZ() {
        return spreadZ;
    }

    public String getSound() {
        return sound;
    }

    public float getSoundVolume() {
        return soundVolume;
    }

    public float getSoundPitch() {
        return soundPitch;
    }
}
