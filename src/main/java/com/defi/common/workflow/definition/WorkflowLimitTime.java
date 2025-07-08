package com.defi.common.workflow.definition;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Represents time constraints configuration for workflows.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowLimitTime {
    private int value;
    private TIME_UNIT unit;
    private boolean skipSaturday;
    private boolean skipSunday;

    public enum TIME_UNIT {
        DAY, MONTH
    }
}
