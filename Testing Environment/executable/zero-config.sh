#!/bin/sh

# Consensus persistent state

cp empty-configs/consensus_persistent_state.json node-1/Config/consensus_persistent_state.json
cp empty-configs/consensus_persistent_state.json node-2/Config/consensus_persistent_state.json
cp empty-configs/consensus_persistent_state.json node-3/Config/consensus_persistent_state.json
cp empty-configs/consensus_persistent_state.json node-4/Config/consensus_persistent_state.json
cp empty-configs/consensus_persistent_state.json node-5/Config/consensus_persistent_state.json

# Log

cp empty-configs/log node-1/Log/log
cp empty-configs/log node-2/Log/log
cp empty-configs/log node-3/Log/log
cp empty-configs/log node-4/Log/log
cp empty-configs/log node-5/Log/log