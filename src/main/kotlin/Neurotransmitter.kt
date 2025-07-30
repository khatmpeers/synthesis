package com.peers

object NeurotransmitterIDs {
    const val GLUTAMATE = 0
    const val GABA = 1
    const val DOPAMINE = 2
    const val SEROTONIN = 3
    const val ACETYLCHOLINE = 4
}

abstract class Neurotransmitter(val id: ID, private val tickLifetime: Int, private var currentLife: Int = tickLifetime) {
    fun isAlive(): Boolean = this.tickLifetime >= 0
    fun applyEffects(profile: ModulationProfile) {
        this.applyEffectsLogic(profile)
        this.currentLife--
    }
    abstract fun applyEffectsLogic(profile: ModulationProfile)
    fun revitalize() {
        this.currentLife = this.tickLifetime
    }
}

class Dud : Neurotransmitter(id = -1, tickLifetime = -1) {
    override fun applyEffectsLogic(profile: ModulationProfile) {}
}

class Glutamate : Neurotransmitter(id = NeurotransmitterIDs.GLUTAMATE, tickLifetime = 2) {
    override fun applyEffectsLogic(profile: ModulationProfile) {
        profile.effectiveSignal *= 1.2
    }
}

class GABA : Neurotransmitter(id = NeurotransmitterIDs.GABA, tickLifetime = 3) {
    override fun applyEffectsLogic(profile: ModulationProfile) {
        profile.effectiveSignal *= 0.6
    }
}

class Dopamine : Neurotransmitter(id = NeurotransmitterIDs.DOPAMINE, tickLifetime = 10) {
    override fun applyEffectsLogic(profile: ModulationProfile) {
        profile.growthRate *= 2.0
        profile.decayRate *= 0.95
    }
}

class Serotonin : Neurotransmitter(id = NeurotransmitterIDs.SEROTONIN, tickLifetime = 15) {
    override fun applyEffectsLogic(profile: ModulationProfile) {
        profile.growthRate *= 0.9
        profile.decayRate *= 0.9
    }
}

class Acetylcholine : Neurotransmitter(id = NeurotransmitterIDs.ACETYLCHOLINE, tickLifetime = 6) {
    override fun applyEffectsLogic(profile: ModulationProfile) {
        profile.firingThreshold *= 0.9
        profile.effectiveSignal *= 1.3
    }
}

class ModulationProfile(
    var growthRate: Double,
    var decayRate: Double,
    var firingThreshold: Double,
    var effectiveSignal: Double
)