package com.peers

typealias State = NeuronFSM.State

object SystemConfig {
    const val decayRate: Double = 0.9
    const val growthRate: Double = 1.1
    const val initialWeight: Double = 1.0
    val amplifiers: Map<NeuronFSM.State, Double> = mapOf(
        State.RESTING to 1.0,
        State.DEPOLARIZED to Double.MAX_VALUE,
        State.REFRACTORY to 1.5,
    )
}