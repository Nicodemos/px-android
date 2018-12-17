## VERSION 4.3.1
_21_11_2018_

* FIX - Installments selection in express flow
* FIX - Account money invested in express flow
* FIX - PEC and payer information assets
* FIX - Instructions padding

## VERSION 4.3.0
_31_10_2018_

 * FEATURE - Express checkout.
 * FEATURE - Skip Payer Information.
 * FEATURE - Pec Payment Method.
 * FEATURE - Enabled MLU (Site Uruguay)
 * FEATURE - Interactive instructions in congrats.
 * FEATURE - Configurable titles for Payment Vault Screen.
 * FEATURE - added new tracking listener PXTrackingListener for MeliData compatibility
 ```java
  void setListener(@NonNull final PXTrackingListener listener,
              @NonNull final Map<String, ? extends Object> flowDetail, 
              @Nullable final String flowName)
```

## VERSION 4.2.1
_30_10_2018_

* FIX - Crash NPE when processing payment in background
* FIX - Crash on recovery payment

## VERSION 4.2.0
_25_10_2018_

* FEATURE - dynamic custom dialogs for certain locations.
* FEATURE - dynamic custom views for review and confirm.
* ENHANCEMENT - tracking screen's names and paths unified.
* ENHANCEMENT/FIX - card addition flow now supports installments and ESC.

## VERSION 4.1.3
_01_11_2018_

* FIX - Crash NPE on exploding button

## VERSION 4.1.2
_31_10_2018_

* FIX - Crash NPE when processing payment in background
* FIX - Crash on recovery payment

## VERSION 4.1.0
_04_10_2018_

* FIX - Show app bar when tap back from CVV screen.
* FIX - Tracking of PaymentMethodSearchItem.
* ENHANCEMENT - Standalone Card Association

## VERSION 4.0.6
_08_10_2018_

* FIX - Double congrats one tap.

## VERSION 4.0.5

* FIX - Show app bar when tap back from CVV screen.
* FIX - Tracking of PaymentMethodSearchItem.
* FIX - Payment processor - visual attach bug

## VERSION 4.0.4
_25_09_2018_

* FIX - DefaultPaymentTypeId debit card error, can't look for settings.
* FIX - Colombia currency utils.

## VERSION 4.0.3

_20_09_2018_

* FIX - Payment recovery call for auth.
* FIX - destroy activity behaviour.
* FIX - NPE no decimals for Site Colombia.
* ENHANCEMENT - Color customization detailed documentation.
* ENHANCEMENT - Loading improvements for visual payments (payment processor).

## VERSION 4.0.2

_05_09_2018_

* Fix: one tap with payment recovery
* Fix: payment processor background support
* Fix: added internal payment method change behaviour


## VERSION 4.0.1

_03_09_2018_

* Fix: dynamic id declaration
* Fix: code discount
* Fix: esc with one tap

## VERSION 4.0.0

_30_08_2018_