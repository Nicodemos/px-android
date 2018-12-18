package com.mercadopago.android.px.internal.datasource;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.repository.AmountRepository;
import com.mercadopago.android.px.internal.repository.ChargeRepository;
import com.mercadopago.android.px.internal.repository.DiscountRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.model.Discount;
import java.math.BigDecimal;

public class AmountService implements AmountRepository {

    @NonNull private final PaymentSettingRepository paymentSetting;
    @NonNull private final ChargeRepository chargeRepository;
    @NonNull private final DiscountRepository discountRepository;
    @NonNull private final UserSelectionRepository userSelectionRepository;

    public AmountService(@NonNull final PaymentSettingRepository paymentSetting,
        @NonNull final ChargeRepository chargeRepository,
        @NonNull final DiscountRepository discountRepository,
        @NonNull final UserSelectionRepository userSelectionRepository) {
        this.paymentSetting = paymentSetting;
        this.chargeRepository = chargeRepository;
        this.discountRepository = discountRepository;
        this.userSelectionRepository = userSelectionRepository;
    }

    @Override
    @NonNull
    public BigDecimal getAmountToPay() {
        final BigDecimal amount = amountWithoutPayerCosts();
        final BigDecimal installmentTotalAmount = getInstallmentTotalAmount();
        final int cmp = amount.compareTo(installmentTotalAmount);
        // if amount  > installment amount ; return amount else installmentAmount
        return cmp > 0 ? amount : installmentTotalAmount;
    }

    private BigDecimal amountWithoutPayerCosts() {
        return paymentSetting.getCheckoutPreference()
            .getTotalAmount()
            .add(chargeRepository.getChargeAmount())
            .subtract(getDiscountAmount());
    }

    @Override
    @NonNull
    public BigDecimal getItemsAmount() {
        return paymentSetting.getCheckoutPreference()
            .getTotalAmount();
    }

    @NonNull
    @Override
    public BigDecimal getItemsPlusCharges() {
        final BigDecimal totalAmount = paymentSetting.getCheckoutPreference()
            .getTotalAmount();
        return totalAmount.add(chargeRepository.getChargeAmount());
    }

    @NonNull
    @Override
    public BigDecimal getAppliedCharges() {
        final BigDecimal amountWithoutPayerCosts = amountWithoutPayerCosts();
        final BigDecimal installmentTotalAmount = getInstallmentTotalAmount();
        final int compare = amountWithoutPayerCosts.compareTo(installmentTotalAmount);
        // if amount  > installment amount ; return only charges else charges plus diff
        return compare > 0 ? chargeRepository.getChargeAmount() : installmentTotalAmount
            .subtract(amountWithoutPayerCosts)
            .add(chargeRepository.getChargeAmount());
    }

    @NonNull
    @Override
    public BigDecimal getAmountWithDiscount() {
        return paymentSetting.getCheckoutPreference()
            .getTotalAmount()
            .subtract(getDiscountAmount());
    }

    private BigDecimal getDiscountAmount() {
        final Discount discount = discountRepository.getCurrentConfiguration().getDiscount();
        return discount == null ? BigDecimal.ZERO : discount.getCouponAmount();
    }

    @NonNull
    private BigDecimal getInstallmentTotalAmount() {
        return userSelectionRepository.hasPayerCostSelected() ? userSelectionRepository.getPayerCost()
            .getTotalAmount()
            : BigDecimal.ZERO;
    }
}
