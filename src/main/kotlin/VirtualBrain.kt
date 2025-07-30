package com.peers

import java.util.Random

class VirtualBrain(
    val topology: Topology,
    val activeNeurotransmitters: MutableMap<Neurotransmitter, Int> = mutableMapOf()
) {
    fun run(bounds: (Int) -> Boolean = { true }) {
        val r = Random()

        val queue: MutableList<Neuron> = this.topology.inputs.toMutableList()

        var tickCount = 0

        while (queue.isNotEmpty() && bounds(tickCount)) {
            val currentNeuron = queue.removeAt(0)
            val updatedNTs = activeNeurotransmitters.mapValues { (nt, lt) ->
                currentNeuron.applyNeurotransmitter(nt)
                lt - 1
            }
            activeNeurotransmitters.putAll(updatedNTs)

            currentNeuron.connections.forEach { queue.add(it) }
            currentNeuron.tick()
            this.activeNeurotransmitters.entries.removeIf { it.value <= 0 }

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