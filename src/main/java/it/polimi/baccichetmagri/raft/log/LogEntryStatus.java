package it.polimi.baccichetmagri.raft.log;

/**
 * Characterizes the Log Entry: it can be either snapshotted (no more in the Log), inside the Log or never existed, based on its index
 */
public enum LogEntryStatus {
    SNAPSHOTTED,
    NOT_SNAPSHOTTED,
    NOT_EXISTENT
}
