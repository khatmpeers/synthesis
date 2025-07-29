package com.peers

import java.util.*

fun main() {
    val inputNeuron = SensoryNeuron(id = 0)
    val interNeuron = Neuron(id = 1)
    val outputNeuron = MotorNeuron(id = 3, SubscriberInterface { println("AAA") })

    val secondaryInputNeuron = SensoryNeuron(id = 4)

    inputNeuron
        .linkTo(interNeuron)
        .linkTo(outputNeuron)

    secondaryInputNeuron
        .linkTo(interNeuron)


    val scanner = Scanner(System.`in`)

    while (true) {
        listOf(inputNeuron, secondaryInputNeuron, outputNeuron, interNeuron).forEach { it.tick() }
        val input = scanner.nextLine()
        println("InterNeuron: ${interNeuron.weights},\nOutputNeuron: ${outputNeuron.weights}")
        println("States:\n\tInput1: ${inputNeuron.fsm.getCurrentState()}\n\tInput2: ${secondaryInputNeuron.fsm.getCurrentState()}\n\tInterNeuron: ${interNeuron.fsm.getCurrentState()}\n\tOutputNeuron: ${outputNeuron.fsm.getCurrentState()}")
        if (input.contains("a")) {
            inputNeuron.fire(1.0)
        } else if (input.contains("b")) {
            secondaryInputNeuron.fire(1.0)
        }
        if (input == "exit") break
    }

    println("InterNeuron: ${interNeuron.weights},\nOutputNeuron: ${outputNeuron.weights}")
}
