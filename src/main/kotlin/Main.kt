package com.peers

import java.util.*

fun main() {
    val inputNeuron = SensoryNeuron(id = 0)
    val interNeuron = Neuron(id = 1)
    val outputNeuron = MotorNeuron(id = 3, SubscriberInterface { println("AAA") })

    val secondaryInputNeuron = SensoryNeuron(id = 4)

    val allNeurons = listOf(inputNeuron, secondaryInputNeuron, interNeuron, outputNeuron)

    inputNeuron
        .linkTo(interNeuron)
        .linkTo(outputNeuron)

    secondaryInputNeuron
        .linkTo(interNeuron)


    val r = Random()

    val queue: MutableList<Neuron> = mutableListOf(inputNeuron, secondaryInputNeuron)

    var tickCount = 0

    while (tickCount < 1000) {
        val triggeringNeuron = if (r.nextInt() % 2 == 0) inputNeuron else secondaryInputNeuron
        if (tickCount % 5 == 0) {
            triggeringNeuron.fire(2.0)
        }
        if(queue.isEmpty()) queue.add(triggeringNeuron)
        val currentNeuron = queue.removeAt(0)
        currentNeuron.connections.forEach { queue.add(it) }
        currentNeuron.tick()
        tickCount++
    }
    allNeurons.forEach { println(it) }
}
