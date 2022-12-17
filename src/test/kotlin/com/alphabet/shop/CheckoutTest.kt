package com.alphabet.shop

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertContains
import kotlin.test.assertEquals

class CheckoutTest {

    @Test
    fun `This is a complete example with the data on the given spec`() {

        // Alphabet shop sells letters
        //    A 0.50
        //    B 0.75 (2 for £1.25)
        //    C 0.25 (Buy 3, get one free)
        //    D 1.50 (Buy D and E for £3)
        //    E 2.00 (Buy D and E for £3)

        Checkout(
            quantities = mapOf(
                "A" to 2,
                "B" to 3,
                "C" to 5,
                "D" to 2,
                "E" to 3
            ),
            prices = mapOf(
                "A" to 0.50,
                "B" to 0.75,
                "C" to 0.25,
                "D" to 1.50,
                "E" to 2.00
            ),
            offers = mapOf(
                "B" to multiPricedOffer(2, 1.25),
                "C" to groupDiscountOffer(4, 3),
            ),
            aggregateOffers = listOf(
                mealDeal(3.0, "D", "E")
            )
        ).run {
            assertEquals(
                0.5 * 2.0 + // Total cost for A
                        1.25 + 0.75 + // 2 "B" items discount at 1.25 + 1 "B" item full price
                        0.25 * 4.0 + // Every "C" 3 items 1 is free so over 5 items 1 is free
                        6.0 + 2.0 // 2 "D and E" meal deals are 6 pound, 1 remaining E item is full price (2 pounds)
                , totalCost()
            )
        }

    }

    @Test
    fun `A basket should contain items given in multiple times`() {
        val testBasket = Checkout()
            .add("A", "A", "B")
            .add("B", "B")

        listOf(
            "A" to 2,
            "B" to 3
        ).forEach { (product, quantity) ->
            assertContains(testBasket.quantities, product)
            assertEquals(testBasket.quantities[product], quantity)
        }
        assertEquals(testBasket.quantities.size, 2)
    }

    @Test
    fun `search for possible offers and compute the total price`() {
        /*
        * This test should use mocks and only check if the offers functions are called with the right parameters
        * The offers functions should have been tested somewhere else
        * */

        val aPrice = 25.0
        val bPrice = 60.0

        Checkout(
            quantities = mapOf(
                "A" to 264,
                "B" to 5
            ),
            prices = mapOf("A" to aPrice, "B" to bPrice),
            offers = mapOf(
                "A" to groupDiscountOffer(35, 6), // Only for this test..., you take 35 oranges and pay 6
                "B" to ::buyOneGetOneFree
            )
        ).run {
            assertEquals(aPrice * 61 + bPrice * 3, totalCost())
        }
    }

    @TestFactory
    fun buyOneGetOneFreeTest(): List<DynamicTest> {
        data class TestData(val quantity: Int, val price: Price, val expectedCost: Price)

        return listOf(
            TestData(quantity = 6, price = 10.0, expectedCost = 30.0),
            TestData(quantity = 5, price = 10.0, expectedCost = 30.0)
        ).map { (quantity, price, expectedCost) ->
            DynamicTest.dynamicTest("Buy One Get One Free offer price should be <$expectedCost> when price <$price> and quantity <$quantity>") {
                assertEquals(buyOneGetOneFree(quantity, price), expectedCost)
            }
        }
    }

    @TestFactory
    fun groupDiscountOfferTest(): List<DynamicTest> {
        data class TestData(
            val take: Int,
            val pay: Int,
            val quantity: Int,
            val price: Price,
            val expectedCost: Price
        )

        return listOf(
            TestData(
                take = 3,
                pay = 2,
                quantity = 6,
                price = 10.0,
                expectedCost = 40.0
            ),
            TestData(
                take = 3,
                pay = 2,
                quantity = 7,
                price = 10.0,
                expectedCost = 50.0
            )
        ).map { (take, pay, quantity, price, expectedCost) ->
            DynamicTest.dynamicTest(
                "A Group Discount offer <$take X $pay> price should be <$expectedCost> when price <$price> and quantity <$quantity>"
            ) { assertEquals(groupDiscountOffer(take, pay)(quantity, price), expectedCost) }
        }
    }

    @TestFactory
    fun multiPricedDiscountOfferTest(): List<DynamicTest> {
        data class TestData(
            val groupQuantity: Int,
            val groupCost: Price,
            val quantity: Int,
            val price: Price,
            val expectedCost: Price
        )

        return listOf(
            TestData(
                groupQuantity = 3,
                groupCost = 2.0,
                quantity = 6,
                price = 10.0,
                expectedCost = 4.0
            ),
            TestData(
                groupQuantity = 3,
                groupCost = 2.0,
                quantity = 7,
                price = 10.0,
                expectedCost = 14.0
            )
        ).map { (groupQuantity, groupCost, quantity, price, expectedCost) ->
            DynamicTest.dynamicTest(
                "A MultiPriced Discount offer <take $groupQuantity pay $groupCost> price should be <$expectedCost> when price <$price> and quantity <$quantity>"
            ) { assertEquals(multiPricedOffer(groupQuantity, groupCost)(quantity, price), expectedCost) }
        }
    }
}