package com.example.mmplayer;

import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.valueproviders.ConstantFloat;


public class DirectSoundInstance extends AbstractSoundInstance {

    private final WeighedSoundEvents events;

    public DirectSoundInstance(Identifier fileId, boolean stream) {
        super(fileId, SoundSource.MUSIC, SoundInstance.createUnseededRandom());
        this.attenuation = SoundInstance.Attenuation.NONE;
        this.relative = true;
        Sound sound = new Sound(fileId, ConstantFloat.of(1.0f), ConstantFloat.of(1.0f), 1,
                Sound.Type.FILE, stream, false, 16);
        this.events = new WeighedSoundEvents(fileId, null);
        this.events.addSound(sound);
        this.sound = sound;
    }

    @Override
    public WeighedSoundEvents resolve(SoundManager manager) {
        return this.events;
    }
}
