# Alphabet Shop

Everything was committed and pushed in one single operation so I am going to explain all the steps because it doesn't make much sense if I simulate all the steps from scratch committing them one by one. Isn't it?

Those were the steps:
* Defining which datatypes were needed for the Checkout -> List of products, quantities, offers
* Writing down some specs via unit test
* Implementing the total cost function using offers (not the aggreate ones like "meal deal")
* Adding the concept of aggregate offer. It's an offer that can reference other products and not just itself. Aggregate offers have priority over all the other offers.
* Adding "Meal Deal" aggregate offer

Repo:
https://github.com/corzani/alphabet_shop
