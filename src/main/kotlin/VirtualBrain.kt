package com.peers

import java.util.Random

class VirtualBrain(
    val topology: Topology,
    val activeNeurotransmitters: MutableList<Neurotransmitter> = mutableListOf()
) {
    fun run(bounds: (Int) -> Boolean = { true }) {
        val r = Random()

        val queue: MutableList<Neuron> = this.topology.inputs.toMutableList()

        var tickCount = 0

        while (queue.isNotEmpty() && bounds(tickCount)) {
            val currentNeuron = queue.removeAt(0)
            currentNeuron.connections.forEach { queue.add(it) }
            currentNeuron.tick()
            tickCount++
        }
    }
}

class Topology(val inputs: MutableList<SensoryNeuron> = mutableListOf(), val outputs: MutableList<MotorNeuron> = mutableListOf(), generator: () -> Pair<List<SensoryNeuron>, List<MotorNeuron>>) {
    init {
        val params = generator()
        params.first.forEach { this.inputs.add(it) }
        params.second.forEach { this.outputs.add(it) }
    }

    fun getInterface(): SensoryNeuronInterface = SensoryNeuronInterface(this.inputs)
}