package cz.gastro.dto;

import java.time.LocalDate;
import java.util.List;
import cz.gastro.model.PlanLine;

public record PlanRequest(
        LocalDate day,
        List<PlanLine> items
) {}
