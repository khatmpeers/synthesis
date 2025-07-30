package com.peers

import kotlin.concurrent.thread

fun main() {
    val topology = Topology {
        val inputNeuron = SensoryNeuron(id = 0)
        val interNeuron = Neuron(id = 1)
        val outputNeuron = MotorNeuron(id = 3, MotorNeuronInterface { println("AAA") })

        val secondaryInputNeuron = SensoryNeuron(id = 4)

        inputNeuron
            .linkTo(interNeuron)
            .linkTo(outputNeuron)

        secondaryInputNeuron
            .linkTo(interNeuron)

        Pair(
            listOf(inputNeuron, secondaryInputNeuron),
            listOf(outputNeuron)
        )
    }

    val brain = VirtualBrain(topology)

    val brainThread = thread(start = true) {
        brain.run {
            it <= 1000
        }
    }

    val inputInterface = topology.getInterface()
}
