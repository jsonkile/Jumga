# Jumga (Flutterwave Dev Challenge)
Jumga is a simple e-commerce Android app made for the Flutterwave developer challenge. 

![alt text](https://github.com/jsonkile/Jumga/blob/master/Konna%20App.png?raw=true)

The app was written in purely Kotlin. Download installation APK here: https://drive.google.com/file/d/1DEz3YGNBAsImMTwUlhzeV5hVUPh3QNYp/view?usp=sharing

The platform allows merchants to create stores and sell goods, but each merchant must complete a one-time payment of $20 to continue their 'store' setup.
All payments can be done using test tools provided by Flutterwave (which of course handles payments on the app). Please find the test cards and accounts needed here: https://developer.flutterwave.com/docs/test-cards and https://developer.flutterwave.com/docs/test-bank-accounts.
Once the payment of the setup fee is complete, merchants can begin to add to their inventory and expect orders from customers. Sub-accounts for the merchants are also created immediately after payment is made, this is so they can receive payments from customers.

Dispatchers are also assigned to each merchant and store randomly upon creation. Each dispatcher has been created already with existing sub-accounts.

The platform also allows said customers find products from 4 categories (Food, Fashion, Gadgets, and Others), they can simple place orders for a product and make payment directly using Fluttwerwave. Again please use the provided links above to make payments.

On every completed purchase, the value of the order is split among the platform (Konna, the merchant, and the assigned Dispatcher) accordingly. The platform takes 2.5% of the value of the product, while the merchant takes the rest. The platform also receives an additional 20% of the delivery charge.

Payments can be received from the UK, Nigeria, Kenya, and Ghana depending on the billing location of the customer or merchant during setup.


Where I implement payment:
Product page (where customers can view items and make orders) - https://github.com/jsonkile/Jumga/blob/master/app/src/main/java/com/bigheadapps/monkee/ui/activities/ProductActivity.kt
Store Account page (where merchant pay setup fee) - https://github.com/jsonkile/Jumga/blob/master/app/src/main/java/com/bigheadapps/monkee/ui/fragments/store/AccountFragment.kt

Please contact me for further explanation of my code if neccessary.
Cheers 🥂
