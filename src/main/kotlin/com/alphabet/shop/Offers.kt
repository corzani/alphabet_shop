package com.alphabet.shop

// Considering our specs, this kind of offer is not needed. It was the very first case for tests so I left it.
fun buyOneGetOneFree(quantity: Int, price: Price): Price = ((quantity / 2) + (quantity % 2)) * price

fun groupDiscountOffer(take: Int, pay: Int) =
    fun(quantity: Int, price: Price): Price = (((quantity / take) * pay) + (quantity % take)) * price

fun multiPricedOffer(groupQuantity: Int, groupCost: Price) =
    fun(quantity: Int, price: Price): Price =
        ((quantity / groupQuantity) * groupCost) + (quantity % groupQuantity) * price

fun noOffer(quantity: Int, price: Price): Price = price * quantity

/*
Those functions should filter the checkout item quantity in order to let other offers work on
the remaining products.
If we have a meal deal on A and B, and we buy 2 A and 1 B, I might want to apply a possible offer over the
remaining A that is not an aggregate offer
 */
fun mealDeal(cost: Price, vararg products: String) = fun(checkout: Checkout): AggregateOfferResult {
    val possibleDeals = products.minOfOrNull { checkout.quantities.getOrDefault(it, 0) } ?: 0
    val totalCost = cost * possibleDeals

    val filteredQuantities = when (possibleDeals) {
        0 -> checkout.quantities
        else -> products.fold(checkout.quantities) { acc, value ->
            acc + (value to checkout.quantities.getOrDefault(value, 0) - possibleDeals)
        }
    }

    return AggregateOfferResult(checkout.copy(quantities = filteredQuantities), totalCost)
}
