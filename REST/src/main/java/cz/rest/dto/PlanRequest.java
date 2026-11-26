package cz.rest.dto;

import java.time.LocalDate;
import java.util.List;
import cz.rest.model.PlanLine;

public record PlanRequest(
        LocalDate day,
        List<PlanLine> items
) {}
