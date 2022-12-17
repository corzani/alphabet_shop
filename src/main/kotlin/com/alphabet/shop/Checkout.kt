package com.alphabet.shop

// I would force the usage of BigDecimal for currencies
// but here for simplicity Double is enough
typealias Price = Double
typealias Quantities = Map<String, Int>
typealias Prices = Map<String, Price>
typealias Offer = (quantity: Int, price: Price) -> Price
typealias Offers = Map<String, Offer>

data class AggregateOfferResult(val checkout: Checkout, val price: Price)
typealias AggregateOffer = (Checkout) -> AggregateOfferResult

data class Checkout(
    val quantities: Quantities = emptyMap(),
    val prices: Prices = emptyMap(),
    val offers: Offers = emptyMap(),
    val aggregateOffers: List<AggregateOffer> = emptyList()
)

/*

 */
fun Checkout.computeAggregateOffers(postAggregateFn: (AggregateOfferResult) -> Price): Price =
    aggregateOffers.fold(AggregateOfferResult(this, 0.0)) { acc: AggregateOfferResult, aggregateOffer: AggregateOffer ->
        aggregateOffer(acc.checkout)
            .let { AggregateOfferResult(it.checkout, it.price + acc.price) }
    }.let { postAggregateFn(it) }

fun Checkout.add(vararg products: String): Checkout =
    products.groupingBy { it }
        .eachCount()
        .map { (product, quantity) ->
            product to quantity + quantities.getOrDefault(product, 0)
        }.toMap()
        .let { updatedItems ->
            copy(quantities = quantities + updatedItems)
        }

// To make it simple, something that doesn't exist has cost 0 for now
fun Checkout.itemPriceWithOffer(quantityEntry: Map.Entry<String, Int>): Price =
    offers.getOrDefault(quantityEntry.key, ::noOffer)
        .invoke(
            quantityEntry.value,
            prices.getOrDefault(quantityEntry.key, 0.0)
        )

fun Checkout.totalCost(): Price =
    // Compute Aggregates and filter Checkout
    computeAggregateOffers { (checkout, aggregateTotalPrice) ->
        // Compute normal offers after Aggregates with the remaining items
        checkout.quantities.map(::itemPriceWithOffer).sum() + aggregateTotalPrice
    }