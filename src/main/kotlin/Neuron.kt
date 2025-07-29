package com.peers

typealias ID = Int // in case we want to change this to smth more complex later

open class Neuron(
    var incomingSignals: MutableList<Signal> = mutableListOf(),
    val fsm: NeuronFSM = NeuronFSM(),
    val weights: MutableMap<ID, Double> = mutableMapOf(),
    var threshold: Double = 1.0,
    var connections: MutableList<Neuron> = mutableListOf(),
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

    open fun linkTo(other: Neuron): Neuron {
        this.connections.add(other)
        return other
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

    open fun tick() {
        val incomingSignal = this.computeIncomingSignal()

        if (incomingSignal >= this.threshold * SystemConfig.amplifiers[this.fsm.getCurrentState()]!!) {
            // update weights
            this.weights.forEach { (id, _) ->
                if (this.incomingSignals.any { it.source == id }) {
                    this.weights[id] = this.weights[id]!! * SystemConfig.growthRate
                } else {
                    this.weights[id] = this.weights[id]!! * SystemConfig.decayRate
                }
            }
            this.fsm.advance()

            // send outgoing signal to connections
            this.connections.forEach { it.logSignal(this.outgoingSignal()) }
        }

        println("condition for ${this.id} is ${this.fsm.getCurrentState() != State.RESTING}, state is ${this.fsm.getCurrentState()}")
        if (this.fsm.getCurrentState() != State.RESTING) {
            println("advancing Neuron #${this.id}")
            this.fsm.advance()
        }

        // clear incoming signals
        this.incomingSignals.clear()
    }
}

class SensoryNeuron(
    id: ID,
    var stamina: Double = 1.0
) : Neuron(id = id) {
    override fun outgoingSignal(): Signal {
        return Signal(
            source = this.id,
            strength = 1.0
        )
    }

    override fun tick() {
        println("condition for ${this.id} is ${this.fsm.getCurrentState() != State.RESTING}, state is ${this.fsm.getCurrentState()}")
        if (this.fsm.getCurrentState() != State.RESTING) {
            println("advancing Neuron #${this.id}")
            this.fsm.advance()
        }
        this.stamina = (this.stamina + SensoryConfig.recoveryRate).coerceAtMost(1.0)
    }

    fun fire(signalStrength: Double) {
        if (this.stamina <= 0.0) return

        val fatigueFactor = this.stamina.coerceIn(0.0, 1.0)
        val adjustedStrength = signalStrength * fatigueFactor

        if (adjustedStrength >= SystemConfig.amplifiers[this.fsm.getCurrentState()]!!) {
            println(this.connections)
            this.connections.forEach { it.logSignal(this.outgoingSignal()) }
            this.fsm.advance()
            this.stamina -= SensoryConfig.depletionRate
        }
    }
}

class MotorNeuron(id: ID, private val subscriber: SubscriberInterface) : Neuron(id = id) {

    override fun tick() {
        val incomingSignal = this.computeIncomingSignal()

        if (incomingSignal >= this.threshold * SystemConfig.amplifiers[this.fsm.getCurrentState()]!!) {
            // update weights
            this.weights.forEach { (id, _) ->
                if (this.incomingSignals.any { it.source == id }) {
                    this.weights[id] = this.weights[id]!! * SystemConfig.growthRate
                } else {
                    this.weights[id] = this.weights[id]!! * SystemConfig.decayRate
                }
            }

            this.fsm.advance()

            // interface with external thing
            this.subscriber.trigger()
        }

        println("condition for ${this.id} is ${this.fsm.getCurrentState() != State.RESTING}, state is ${this.fsm.getCurrentState()}")
        if (this.fsm.getCurrentState() != State.RESTING) {
            println("advancing Neuron #${this.id}")
            this.fsm.advance()
        }

        // clear incoming signals
        this.incomingSignals.clear()
    }

}

class SubscriberInterface(val triggerAction: () -> Unit) {
    fun trigger() {
        triggerAction()
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