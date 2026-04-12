package br.com.arch.workflow;

record StepEntry<I, C, O>(String name, FlowItem<I, C, O> flowItem) {}
