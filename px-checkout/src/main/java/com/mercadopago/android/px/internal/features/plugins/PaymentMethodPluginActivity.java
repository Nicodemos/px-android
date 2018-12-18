package com.mercadopago.android.px.internal.features.plugins;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.core.PaymentMethodPlugin;
import com.mercadopago.android.px.internal.di.ConfigurationModule;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.repository.PluginRepository;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentMethodInfo;

public class PaymentMethodPluginActivity extends AppCompatActivity implements
    PaymentMethodPlugin.OnPaymentMethodListener {

    private static final String PLUGIN_FRAGMENT = PaymentMethodPluginActivity.class.getName() + "_fragment";

    public static Intent getIntent(@NonNull final Context context) {
        return new Intent(context, PaymentMethodPluginActivity.class);
    }
    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setId(R.id.px_main_container);
        setContentView(frameLayout,
            new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        final Session session = Session.getSession(this);
        final ConfigurationModule configurationModule = session.getConfigurationModule();
        final UserSelectionRepository userSelectionRepository = configurationModule.getUserSelectionRepository();
        final PaymentMethod paymentMethod = userSelectionRepository.getPaymentMethod();

        if (paymentMethod == null) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        final PluginRepository pluginRepository = session.getPluginRepository();
        final PaymentMethodInfo paymentMethodInfo = pluginRepository
            .getPaymentMethodInfo(paymentMethod.getId());

        final PaymentMethodPlugin plugin = pluginRepository.getPlugin(paymentMethodInfo.getId());

        final PaymentMethodPlugin.CheckoutData checkoutData =
            new PaymentMethodPlugin.CheckoutData(configurationModule.getPaymentSettings().getCheckoutPreference()
                , session.getDiscountRepository().getCurrentConfiguration().getDiscount(),
                configurationModule.getPaymentSettings().getPrivateKey());

        final Fragment fragment = plugin.getFragment(checkoutData, this);

        if (fragment == null) {
            next();
        } else {
            final Bundle fragmentBundle = plugin.getFragmentBundle(checkoutData, this);
            fragment.setArguments(fragmentBundle);
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.px_main_container, fragment, PLUGIN_FRAGMENT)
                .commit();
        }
    }

    @Override
    public void next() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void back() {
        onBackPressed();
    }
}