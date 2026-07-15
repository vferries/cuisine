export function scaleQuantity(quantity: number, ratio: number): number {
  // Arrondi à 2 décimales, miroir de scaleQuantityText (Android), pour un
  // rendu identique des décimales sur les deux plateformes.
  return Math.round(quantity * ratio * 100) / 100;
}
