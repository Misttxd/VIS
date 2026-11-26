export interface NamedItem { id: number; name: string }
export interface Ingredient extends NamedItem { categoryId: number; unit: string; minimum: number }
export interface Recipe extends NamedItem { servings: number; instructions: string }
export interface RecipeDetail extends Recipe {
  ingredients: { name: string; quantity: number; unit: string; category: string }[]
}
export interface Delivery { id: number; ingredientId: number; remaining: number; expires: string }
export interface StockLevel { name: string; available: number; unit: string }
export interface StockOverview { low: StockLevel[]; available: StockLevel[]; deliveries: Delivery[] }
export interface Plan { id: number; day: string; prepared: boolean; userId: number }
export interface PlanItem { recipeId: number; portions: number }
export interface Availability extends PlanItem { name: string; useSoon: boolean }
export interface Shortage { name: string; missing: number; unit: string }

export interface User {
  id: number;
  username: string;
  role: string;
}

export interface PageProps {
  user: User;
  ingredients: Ingredient[];
  categories: NamedItem[];
  recipes: Recipe[];
  version: number;
  report: (message: string) => void;
  refresh: () => void;
}
