package cz.gastro.controller;

import cz.gastro.model.Availability;
import cz.gastro.model.MealPlan;
import cz.gastro.model.PlanLine;
import cz.gastro.model.Shortage;
import cz.gastro.dto.PlanRequest;
import java.util.List;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import cz.gastro.service.MealPlanningService;

@RestController
@RequestMapping("/api")
public class MealPlanningController {
    private final MealPlanningService mealPlanningService;

    public MealPlanningController(MealPlanningService mealPlanningService) {
        this.mealPlanningService = mealPlanningService;
    }

    @GetMapping("/availability")
    public List<Availability> availability(@RequestParam LocalDate day) {
        return mealPlanningService.availability(day);
    }

    @GetMapping("/plans")
    public List<MealPlan> plans() {
        return mealPlanningService.plans();
    }

    @PostMapping("/plans")
    public long save(@RequestBody PlanRequest request) {
        return mealPlanningService.save(request);
    }

    @GetMapping("/plans/{id}/items")
    public List<PlanLine> items(@PathVariable long id) {
        return mealPlanningService.items(id);
    }

    @GetMapping("/plans/{id}/shopping")
    public List<Shortage> shoppingList(@PathVariable long id) {
        return mealPlanningService.shoppingList(id);
    }

    @PostMapping("/plans/{id}/prepare")
    public void prepare(@PathVariable long id) {
        mealPlanningService.prepare(id);
    }
}
