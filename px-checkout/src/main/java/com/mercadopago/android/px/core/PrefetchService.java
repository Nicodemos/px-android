package com.mercadopago.android.px.core;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.core.Settings;
import com.mercadopago.android.px.internal.datasource.PluginInitializationAsync;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.repository.PluginInitTask;
import com.mercadopago.android.px.internal.repository.PluginRepository;
import com.mercadopago.android.px.internal.services.CheckoutService;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.model.PaymentMethodSearch;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.services.Callback;

/* default */ class PrefetchService {

    private final Handler mainHandler;

    /* default */ final Session session;
    /* default */ final CheckoutLazyInit checkoutLazyInitCallback;
    /* default */ final MercadoPagoCheckout checkout;
    private Thread currentFetch;

    /* default */ PrefetchService(final MercadoPagoCheckout checkout, final Session session,
        final CheckoutLazyInit checkoutLazyInitCallback) {
        session.init(checkout);
        this.checkout = checkout;
        this.session = session;
        this.checkoutLazyInitCallback = checkoutLazyInitCallback;
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void prefetch() {
        final PaymentSettingRepository paymentSettings =
            session.getConfigurationModule().getPaymentSettings();

        currentFetch = new Thread(new Runnable() {
            @Override
            public void run() {

                final String checkoutPreferenceId =
                    paymentSettings.getCheckoutPreferenceId();
                if (!TextUtil.isEmpty(checkoutPreferenceId)) {
                    fetchPreference();
                } else {
                    fetchGroups();
                }
            }
        });
        currentFetch.start();
    }

    /* default */ void fetchPreference() {
        //TODO ADD PREFERENCE SERVICE.
        final PaymentSettingRepository paymentSettings =
            session.getConfigurationModule().getPaymentSettings();

        session.getRetrofitClient().create(CheckoutService.class)
            .getPreference(Settings.servicesVersion, paymentSettings.getCheckoutPreferenceId(),
                paymentSettings.getPublicKey())
            .execute(
                new Callback<CheckoutPreference>() {
                    @Override
                    public void success(final CheckoutPreference checkoutPreference) {
                        paymentSettings.configure(checkoutPreference);
                        fetchGroups();
                    }

                    @Override
                    public void failure(final ApiException apiException) {
                        //TODO Track
                        postError();
                    }
                });
    }

    /* default */ void initPlugins() {
        final PluginRepository pluginRepository = session.getPluginRepository();
        final PluginInitTask initTask = pluginRepository.getInitTask(true);
        initTask.init(new PluginInitializationAsync.DataInitializationCallbacks() {
            @Override
            public void onDataInitialized() {
                pluginRepository.initialized();
                postSuccess();
            }

            @Override
            public void onFailure(@NonNull final Exception e) {
                pluginRepository.initialized();
                postSuccess();
            }
        });
    }

    /* default */ void fetchGroups() {
        session.getGroupsRepository().getGroups().execute(new Callback<PaymentMethodSearch>() {
            @Override
            public void success(final PaymentMethodSearch paymentMethodSearch) {
                initPlugins();
            }

            @Override
            public void failure(final ApiException apiException) {
                //TODO Track
                postError();
            }
        });
    }

    /* default */ void postSuccess() {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                checkout.prefetch = true;
                checkoutLazyInitCallback.success(checkout);
            }
        });
    }

    /* default */ void postError() {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                checkoutLazyInitCallback.fail();
            }
        });
    }

    public void cancel() {
        if (currentFetch != null && currentFetch.isAlive() && !currentFetch.isInterrupted()) {
            currentFetch.interrupt();
        }
    }
}
