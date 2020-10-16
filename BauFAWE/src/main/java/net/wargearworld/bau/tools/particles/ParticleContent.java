package net.wargearworld.bau.tools.particles;

import net.wargearworld.CommandManager.Arguments.EnumArgumentInterface;

public enum ParticleContent implements EnumArgumentInterface<ParticleContent> {
    CLIPBOARD, SELECTION;

    @Override
    public ParticleContent fromString(String s) {
        return ParticleContent.valueOf(s.toUpperCase());
    }

    @Override
    public String getTypeName() {
        return null;
    }

    @Override
    public String[] getPossibleOptions() {
        return new String[0];
    }
}
