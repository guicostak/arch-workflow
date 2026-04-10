package br.com.arch.workflow;

record StepEntry<I, C, O>(String name, Activity<I, C, O> activity) {}
