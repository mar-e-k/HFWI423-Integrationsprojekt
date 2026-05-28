package de.fhdw.vendix.orchestrator.ui.performance;

import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.time.Instant;

public record PerformanceTestStatus(
        PerformanceTestType type,
        boolean running,
        String state,
        @Nullable Instant startedAt,
        @Nullable Integer exitCode,
        @Nullable Path logFile
) {}
