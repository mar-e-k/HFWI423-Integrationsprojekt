package de.fhdw.vendix.orchestrator.ui.performance;

import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

public record PerformanceTestLaunchResult(
        boolean started,
        String message,
        @Nullable Path logFile
) {}
