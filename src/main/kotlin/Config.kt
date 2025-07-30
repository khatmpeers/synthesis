package com.peers

typealias State = NeuronFSM.State

object SystemConfig {
    const val decayRate: Double = 0.95
    const val growthRate: Double = 1.5
    const val initialWeight: Double = 1.0
    val amplifiers: Map<NeuronFSM.State, Double> = mapOf(
        State.RESTING to 1.0,
        State.DEPOLARIZED to Double.MAX_VALUE,
        State.REFRACTORY to 1.5,
    )
}

object SensoryConfig {
    const val depletionRate: Double = 0.1
    const val recoveryRate: Double = 0.05
}