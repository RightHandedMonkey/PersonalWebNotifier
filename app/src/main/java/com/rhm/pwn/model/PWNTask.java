package com.rhm.pwn.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by sambo on 10/7/17.
 */
@Entity
public class PWNTask {
    @PrimaryKey(autoGenerate = true)
    int id;
    long minLatency;
    long overrideDeadline;
    String actualExecutionTime;
    String createJobTime;
    String scheduledExecutionMinTime;

    public PWNTask() {}

    public PWNTask(String createTime, String scheduledExecutionMinTime) {
        this.createJobTime = createTime;
        this.scheduledExecutionMinTime = scheduledExecutionMinTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PWNTask pwnTask = (PWNTask) o;

        if (id != pwnTask.id) return false;
        if (minLatency != pwnTask.minLatency) return false;
        if (overrideDeadline != pwnTask.overrideDeadline) return false;
        if (actualExecutionTime != null ? !actualExecutionTime.equals(pwnTask.actualExecutionTime) : pwnTask.actualExecutionTime != null)
            return false;
        return createJobTime.equals(pwnTask.createJobTime);
    }

    @Override
    public String toString() {
//        Long minLatS = minLatency/1000;
//        String minLatHuman = String.format("%d:%d:%02d", minLatS/60/60, minLatS/60, minLatS%60);
//        Long deadlineS = overrideDeadline/1000;
//        String deadlineHuman = String.format("%d:%d:%02d", deadlineS/60/60, deadlineS/60, deadlineS%60);
        String msg = String.format("Id#%d \r\ncreated:%s \n" +
                "sched min:%s \r\nactual:%s", id, createJobTime, scheduledExecutionMinTime, actualExecutionTime);
        return msg;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (int) (minLatency ^ (minLatency >>> 32));
        result = 31 * result + (int) (overrideDeadline ^ (overrideDeadline >>> 32));
        result = 31 * result + (actualExecutionTime != null ? actualExecutionTime.hashCode() : 0);
        result = 31 * result + createJobTime.hashCode();
        return result;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getMinLatency() {
        return minLatency;
    }

    public void setMinLatency(long minLatency) {
        this.minLatency = minLatency;
    }

    public long getOverrideDeadline() {
        return overrideDeadline;
    }

    public void setOverrideDeadline(long overrideDeadline) {
        this.overrideDeadline = overrideDeadline;
    }

    public String getActualExecutionTime() {
        return actualExecutionTime;
    }

    public void setActualExecutionTime(String actualExecutionTime) {
        this.actualExecutionTime = actualExecutionTime;
    }
    public String getCreateJobTime() {
        return createJobTime;
    }

    public void setCreateJobTime(String createJobTime) {
        this.createJobTime = createJobTime;
    }

    public String getScheduledExecutionMinTime() {
        return scheduledExecutionMinTime;
    }

    public void setScheduledExecutionMinTime(String scheduledExecutionMinTime) {
        this.scheduledExecutionMinTime = scheduledExecutionMinTime;
    }
}
