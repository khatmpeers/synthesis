package com.peers

import com.peers.NeurotransmitterIDs.ACETYLCHOLINE
import com.peers.NeurotransmitterIDs.DOPAMINE
import com.peers.NeurotransmitterIDs.GLUTAMATE
import com.peers.NeurotransmitterIDs.GABA
import com.peers.NeurotransmitterIDs.SEROTONIN

typealias ID = Int // in case we want to change this to smth more complex later

open class Neuron(
    val incomingSignals: MutableList<Signal> = mutableListOf(),
    val fsm: NeuronFSM = NeuronFSM(),
    val weights: MutableMap<ID, Double> = mutableMapOf(),
    var threshold: Double = 1.0,
    var connections: MutableList<Neuron> = mutableListOf(),
    val neurotransmitters: MutableSet<Neurotransmitter> = mutableSetOf(),
    val id: ID,
) {
    open fun logSignal(signal: Signal) {
        this.incomingSignals.add(signal)
    }

    open fun outgoingSignal(): Signal {
        return Signal(
            source = this.id,
            strength = 1.0
        )
    }

    fun toCartesian() = Pair(this.weights.size, this.connections.size)

    open fun linkTo(other: Neuron): Neuron {
        this.connections.add(other)
        return other
    }

    private fun condenseSignals() = if (this.incomingSignals.isNotEmpty()) this.incomingSignals.map { "${it.source}: ${it.strength()}" }.reduce { acc, s -> acc + s } else ""

    override fun toString(): String {
        return "(${this.id}:\n\tcurrentState: ${this.fsm.getCurrentState()}\n\tincomingSignals: (${this.condenseSignals()}))\n\tweights: ${this.weights}\n\tconnections: ${this.connections.joinToString("\n\t")}"
    }

    fun computeIncomingSignal(): Double =
        this.incomingSignals.sumOf { signal ->
            var weight = this.weights[signal.source] ?: 0.0
            if (weight == 0.0) {
                this.weights[signal.source] = SystemConfig.initialWeight
                weight = 1.0
            }
            weight * signal.strength()
        }

    open fun applyNeurotransmitter(nt: Neurotransmitter) {
        if (this.neurotransmitters.any { it.id == id }) {
            this.neurotransmitters.find { it.id == id }?.revitalize()
        } else {
            this.neurotransmitters.add(nt)
        }
    }

    open fun tick() {

        val incomingSignal = this.computeIncomingSignal()

        val profile = ModulationProfile(
            growthRate = SystemConfig.growthRate,
            decayRate = SystemConfig.decayRate,
            firingThreshold = this.threshold,
            effectiveSignal = this.threshold * SystemConfig.amplifiers[this.fsm.getCurrentState()]!!
        )

        this.neurotransmitters.forEach { nt ->
            nt.applyEffects(profile)
        }

        var fired = false

        if (incomingSignal >= profile.effectiveSignal) {
            // update weights
            this.weights.forEach { (id, _) ->
                if (this.incomingSignals.any { it.source == id }) {
                    this.weights[id] = this.weights[id]!! * profile.growthRate
                } else {
                    this.weights[id] = this.weights[id]!! * profile.decayRate
                }
            }
            this.fsm.advance()

            // send outgoing signal to connections
            this.connections.forEach { it.logSignal(this.outgoingSignal()) }
            fired = true
        } else {
            this.weights.forEach { (id, _) ->
                this.weights[id] = this.weights[id]!! * profile.decayRate
            }
        }

        println("condition for ${this.id} is ${this.fsm.getCurrentState() != State.RESTING}, state is ${this.fsm.getCurrentState()}")
        if (this.fsm.getCurrentState() != State.RESTING && !fired) {
            println("advancing Neuron #${this.id}")
            this.fsm.advance()
        }

        // clear incoming signals
        this.incomingSignals.clear()

        this.neurotransmitters.removeIf { !it.isAlive() }
    }
}

open class GatedNeuron(
    val allowedNeurotransmitters: List<ID>,
    id: ID
) : Neuron(id = id) {
    override fun applyNeurotransmitter(nt: Neurotransmitter) {
        if (this.allowedNeurotransmitters.any { it == nt.id }) {
            if (this.neurotransmitters.any { it.id == id }) {
                this.neurotransmitters.find { it.id == id }?.revitalize()
            } else {
                this.neurotransmitters.add(nt)
            }
        }
    }
}

open class TransmitterNeuron(
    private val producibleNeurotransmitters: List<ID>,
    val output: MutableList<Neurotransmitter>,
    id: ID
) : Neuron(id = id) {
    fun emit() {
        this.generateNeurotransmitters().forEach {
            this.output.add(it)
        }
    }

    private fun generateNeurotransmitters() = this.producibleNeurotransmitters.map {
        when (it) {
            DOPAMINE -> Dopamine()
            GLUTAMATE -> Glutamate()
            GABA -> GABA()
            SEROTONIN -> Serotonin()
            ACETYLCHOLINE -> Acetylcholine()
            else -> Dud()
        }
    }
}

class SensoryNeuron(
    id: ID,
    var stamina: Double = 1.0
) : GatedNeuron(listOf(GLUTAMATE, DOPAMINE), id) {
    override fun outgoingSignal(): Signal {
        return Signal(
            source = this.id,
            strength = 1.0
        )
    }

    override fun tick() {
        if (this.stamina <= 0.0) return

        val signalStrength = this.computeIncomingSignal()

        val fatigueFactor = this.stamina.coerceIn(0.0, 1.0)

        val profile = ModulationProfile(
            growthRate = -1.0,
            decayRate = -1.0,
            firingThreshold = SystemConfig.amplifiers[this.fsm.getCurrentState()]!!,
            effectiveSignal = signalStrength * fatigueFactor
        )

        this.neurotransmitters.forEach { nt ->
            nt.applyEffects(profile)
        }

        var fired = false

        if (profile.effectiveSignal >= profile.firingThreshold) {
            println(this.connections)
            this.connections.forEach { it.logSignal(this.outgoingSignal()) }
            this.fsm.advance()
            this.stamina -= SensoryConfig.depletionRate
            fired = true
        }

        println("condition for ${this.id} is ${this.fsm.getCurrentState() != State.RESTING}, state is ${this.fsm.getCurrentState()}")
        if (this.fsm.getCurrentState() != State.RESTING && !fired) {
            println("advancing Neuron #${this.id}")
            this.fsm.advance()
        }
        this.stamina = (this.stamina + SensoryConfig.recoveryRate).coerceAtMost(1.0)

        this.neurotransmitters.removeIf { !it.isAlive() }
    }

    fun fire(signalStrength: Double) {
        this.incomingSignals.add(Signal(source = -1, strength = signalStrength))
    }
}

class MotorNeuron(id: ID, private val subscriber: MotorNeuronInterface) : Neuron(id = id) {

    override fun tick() {
        val incomingSignal = this.computeIncomingSignal()

        val profile = ModulationProfile(
            growthRate = SystemConfig.growthRate,
            decayRate = SystemConfig.decayRate,
            effectiveSignal = incomingSignal,
            firingThreshold = this.threshold * SystemConfig.amplifiers[this.fsm.getCurrentState()]!!
        )

        this.neurotransmitters.forEach { nt ->
            nt.applyEffects(profile)
        }

        var fired = false

        if (profile.effectiveSignal >= profile.firingThreshold) {
            // update weights
            this.weights.forEach { (id, _) ->
                if (this.incomingSignals.any { it.source == id }) {
                    this.weights[id] = this.weights[id]!! * SystemConfig.growthRate
                }
            }

            this.fsm.advance()

            // interface with external thing
            this.subscriber.trigger()
            fired = true
        } else {
            this.weights.forEach { (id, _) ->
                this.weights[id] = this.weights[id]!! * SystemConfig.decayRate
            }
        }

        println("condition for ${this.id} is ${this.fsm.getCurrentState() != State.RESTING}, state is ${this.fsm.getCurrentState()}")
        if (this.fsm.getCurrentState() != State.RESTING && !fired) {
            println("advancing Neuron #${this.id}")
            this.fsm.advance()
        }

        // clear incoming signals
        this.incomingSignals.clear()

        this.neurotransmitters.removeIf { !it.isAlive() }
    }

}

class MotorNeuronInterface(val triggerAction: () -> Unit) {
    fun trigger() {
        triggerAction()
    }
}

class SensoryNeuronInterface(val targets: List<SensoryNeuron> = mutableListOf()) {
    fun fire(signalStrength: Double) {
        this.targets.forEach { it.fire(signalStrength) }
    }
}

/**
 * Eventually create different kinds of signals to mimic neurotransmitters.
 */
class Signal(val source: ID, private val strength: Double = 1.0) {
    fun strength() = strength
}


class NeuronFSM(private var currentState: State = State.RESTING) {
    enum class State {
        RESTING, DEPOLARIZED, REFRACTORY
    }

    fun advance() {
        this.currentState = when (this.currentState) {
            State.RESTING -> State.DEPOLARIZED
            State.DEPOLARIZED -> State.REFRACTORY
            else -> State.RESTING
        }
    }

    fun getCurrentState(): State = this.currentState
}