package com.mercadopago.android.px.preferences;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.gson.annotations.SerializedName;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentType;
import com.mercadopago.android.px.model.PaymentTypes;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PaymentPreference implements Serializable {

    @SerializedName("installments")
    private Integer maxInstallments;
    private Integer defaultInstallments;
    private List<PaymentMethod> excludedPaymentMethods;
    private List<PaymentType> excludedPaymentTypes;

    @SerializedName("default_payment_method_id")
    private String defaultPaymentMethodId;

    @SerializedName("default_card_id")
    private String defaultCardId;

    private String defaultPaymentTypeId;

    public void setMaxAcceptedInstallments(final Integer installments) {
        maxInstallments = installments;
    }

    public void setDefaultInstallments(final Integer defaultInstallments) {
        this.defaultInstallments = defaultInstallments;
    }

    public void setExcludedPaymentTypeIds(final List<String> excludedPaymentTypeIds) {
        if (excludedPaymentTypeIds != null) {
            excludedPaymentTypes = new ArrayList<>();
            for (final String paymentTypeId : excludedPaymentTypeIds) {
                final PaymentType excludedPaymentType = new PaymentType(paymentTypeId);
                excludedPaymentTypes.add(excludedPaymentType);
            }
        }
    }

    public void setDefaultPaymentMethodId(final String defaultPaymentMethodId) {
        this.defaultPaymentMethodId = defaultPaymentMethodId;
    }

    public void setDefaultPaymentTypeId(final String defaultPaymentTypeId) {
        this.defaultPaymentTypeId = defaultPaymentTypeId;
    }

    public Integer getMaxInstallments() {
        return maxInstallments;
    }

    @NonNull
    public List<String> getExcludedPaymentMethodIds() {
        if (excludedPaymentMethods != null) {
            final List<String> excludedPaymentMethodIds = new ArrayList<>();
            for (final PaymentMethod paymentMethod : excludedPaymentMethods) {
                excludedPaymentMethodIds.add(paymentMethod.getId());
            }
            return excludedPaymentMethodIds;
        } else {
            return new ArrayList<>();
        }
    }

    public Integer getDefaultInstallments() {
        return defaultInstallments;
    }

    public void setExcludedPaymentMethodIds(final List<String> excludedPaymentMethodIds) {
        if (excludedPaymentMethodIds != null) {
            excludedPaymentMethods = new ArrayList<>();
            for (final String paymentMethodId : excludedPaymentMethodIds) {
                final PaymentMethod excludedPaymentMethod = new PaymentMethod(paymentMethodId);
                excludedPaymentMethods.add(excludedPaymentMethod);
            }
        }
    }

    @NonNull
    public List<String> getExcludedPaymentTypes() {
        if (excludedPaymentTypes != null) {
            final List<String> excludedPaymentTypeIds = new ArrayList<>();
            for (final PaymentType paymentType : excludedPaymentTypes) {
                excludedPaymentTypeIds.add(paymentType.getId());
            }
            return excludedPaymentTypeIds;
        } else {
            return new ArrayList<>();
        }
    }

    @Nullable
    public String getDefaultPaymentMethodId() {
        return defaultPaymentMethodId;
    }

    public String getDefaultPaymentTypeId() {
        return defaultPaymentTypeId;
    }

    public List<PayerCost> getInstallmentsBelowMax(final List<PayerCost> payerCosts) {

        final List<PayerCost> validPayerCosts = new ArrayList<>();

        if (maxInstallments != null) {
            for (final PayerCost currentPayerCost : payerCosts) {
                if (currentPayerCost.getInstallments() <= maxInstallments) {
                    validPayerCosts.add(currentPayerCost);
                }
            }
            return validPayerCosts;
        } else {
            return payerCosts;
        }
    }

    @Nullable
    public PayerCost getDefaultInstallments(final List<PayerCost> payerCosts) {
        PayerCost defaultPayerCost = null;

        for (final PayerCost currentPayerCost : payerCosts) {
            if (currentPayerCost.getInstallments().equals(defaultInstallments)) {
                defaultPayerCost = currentPayerCost;
                break;
            }
        }

        return defaultPayerCost;
    }

    public List<PaymentMethod> getSupportedPaymentMethods(final List<PaymentMethod> paymentMethods) {
        final List<PaymentMethod> supportedPaymentMethods = new ArrayList<>();
        if (paymentMethods != null) {
            for (final PaymentMethod paymentMethod : paymentMethods) {
                if (isPaymentMethodSupported(paymentMethod)) {
                    supportedPaymentMethods.add(paymentMethod);
                }
            }
        }
        return supportedPaymentMethods;
    }

    public boolean isPaymentMethodSupported(final PaymentMethod paymentMethod) {
        boolean isSupported = true;
        if (paymentMethod == null) {
            isSupported = false;
        } else {
            final List<String> excludedPaymentMethodIds = getExcludedPaymentMethodIds();
            final List<String> excludedPaymentTypes = getExcludedPaymentTypes();

            if ((excludedPaymentMethodIds != null && excludedPaymentMethodIds.contains(paymentMethod.getId()))
                || (excludedPaymentTypes != null && excludedPaymentTypes.contains(paymentMethod.getPaymentTypeId()))) {
                isSupported = false;
            }
        }
        return isSupported;
    }

    @Nullable
    public PaymentMethod getDefaultPaymentMethod(final List<PaymentMethod> paymentMethods) {
        PaymentMethod defaultPaymentMethod = null;
        if (defaultPaymentMethodId != null && paymentMethods != null) {
            for (final PaymentMethod pm : paymentMethods) {
                if (pm.getId().equals(defaultPaymentMethodId)) {
                    defaultPaymentMethod = pm;
                    break;
                }
            }
        }
        return defaultPaymentMethod;
    }

    public boolean installmentPreferencesValid() {
        return validDefaultInstallments() && validMaxInstallments();
    }

    public boolean excludedPaymentTypesValid() {
        return excludedPaymentTypes == null
            || excludedPaymentTypes.size() < PaymentTypes.getAllPaymentTypes().size();
    }

    public boolean validDefaultInstallments() {
        return defaultInstallments == null || defaultInstallments > 0;
    }

    public boolean validMaxInstallments() {
        return maxInstallments == null || maxInstallments > 0;
    }

    @SuppressWarnings("unused")
    public List<Card> getValidCards(final List<Card> cards) {
        final List<Card> supportedCards = new ArrayList<>();
        if (cards != null) {
            for (final Card card : cards) {
                if (isPaymentMethodSupported(card.getPaymentMethod())) {
                    supportedCards.add(card);
                }
            }
        }
        return supportedCards;
    }

    public void setDefaultCardId(final String defaultCardId){
        this.defaultCardId = defaultCardId;
    }

    @Nullable
    public String getDefaultCardId() {
        return defaultCardId;
    }
}
