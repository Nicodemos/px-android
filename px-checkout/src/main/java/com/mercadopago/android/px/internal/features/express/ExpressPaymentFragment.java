package com.mercadopago.android.px.internal.features.express;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import com.mercadolibre.android.ui.widgets.MeliButton;
import com.mercadolibre.android.ui.widgets.MeliSnackbar;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.features.CheckoutActivity;
import com.mercadopago.android.px.internal.features.Constants;
import com.mercadopago.android.px.internal.features.explode.ExplodeDecorator;
import com.mercadopago.android.px.internal.features.explode.ExplodeParams;
import com.mercadopago.android.px.internal.features.explode.ExplodingFragment;
import com.mercadopago.android.px.internal.features.express.animations.FadeAnim;
import com.mercadopago.android.px.internal.features.express.animations.InstallmentsAnimation;
import com.mercadopago.android.px.internal.features.express.animations.SlideAnim;
import com.mercadopago.android.px.internal.features.express.installments.InstallmentsAdapter;
import com.mercadopago.android.px.internal.features.express.slider.PaymentMethodAdapter;
import com.mercadopago.android.px.internal.features.express.slider.PaymentMethodAdapterImpl;
import com.mercadopago.android.px.internal.features.express.slider.PaymentMethodFragmentAdapter;
import com.mercadopago.android.px.internal.features.express.slider.PaymentMethodFragmentAdapterLowRes;
import com.mercadopago.android.px.internal.features.plugins.PaymentProcessorActivity;
import com.mercadopago.android.px.internal.tracker.Tracker;
import com.mercadopago.android.px.internal.util.ApiUtil;
import com.mercadopago.android.px.internal.util.ScaleUtil;
import com.mercadopago.android.px.internal.util.StatusBarDecorator;
import com.mercadopago.android.px.internal.util.VibrationUtils;
import com.mercadopago.android.px.internal.view.AmountDescriptorView;
import com.mercadopago.android.px.internal.view.DiscountDetailDialog;
import com.mercadopago.android.px.internal.view.ElementDescriptorView;
import com.mercadopago.android.px.internal.view.FixedAspectRatioFrameLayout;
import com.mercadopago.android.px.internal.view.PaymentMethodHeaderView;
import com.mercadopago.android.px.internal.view.ScrollingPagerIndicator;
import com.mercadopago.android.px.internal.view.SummaryView;
import com.mercadopago.android.px.internal.view.TitlePager;
import com.mercadopago.android.px.internal.viewmodel.PayerCostSelection;
import com.mercadopago.android.px.internal.viewmodel.drawables.DrawableFragmentItem;
import com.mercadopago.android.px.internal.viewmodel.mappers.ElementDescriptorMapper;
import com.mercadopago.android.px.model.BusinessPayment;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.GenericPayment;
import com.mercadopago.android.px.model.IPayment;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.Site;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.tracking.internal.model.ErrorView;
import com.mercadopago.android.px.tracking.internal.utils.TrackingUtil;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class ExpressPaymentFragment extends Fragment implements ExpressPayment.View, ViewPager.OnPageChangeListener,
    InstallmentsAdapter.ItemListener, SummaryView.OnFitListener, AmountDescriptorView.OnClickListener,
    ExplodingFragment.ExplodingAnimationListener {

    private static final String TAG_EXPLODING_FRAGMENT = "TAG_EXPLODING_FRAGMENT";
    private static final int REQ_CODE_CARD_VAULT = 0x999;
    private static final int REQ_CODE_PAYMENT_PROCESSOR = 0x123;
    private static final float PAGER_NEGATIVE_MARGIN_MULTIPLIER = -1.5f;
    private static final String BUNDLE_STATE_PAYER_COST =
        "com.mercadopago.android.px.internal.features.express.PAYER_COST";

    // Width / Height
    @NonNull private static final Pair<Integer, Integer> ASPECT_RATIO_HIGH_RES = new Pair<>(850, 460);
    // Width / Height
    @NonNull private static final Pair<Integer, Integer> ASPECT_RATIO_LOW_RES = new Pair<>(288, 98);

    private CallBack callback;

    /* default */ ExpressPaymentPresenter presenter;

    private ActionBar actionBar;
    private ElementDescriptorView toolbarElementDescriptor;
    private SummaryView summaryView;
    private View installmentsSelectorSeparator;
    private MeliButton confirmButton;
    private RecyclerView installmentsRecyclerView;
    private ViewPager paymentMethodPager;
    private View pagerAndConfirmButtonContainer;
    private ScrollingPagerIndicator indicator;
    private InstallmentsAnimation installmentsAnimation;
    private FadeAnim fadeAnimation;
    private SlideAnim slideAnim;
    private InstallmentsAdapter installmentsAdapter;
    private FixedAspectRatioFrameLayout aspectRatioContainer;
    private PaymentMethodAdapter paymentMethodAdapter;
    private View recyclerContainer;

    public static Fragment getInstance() {
        return new ExpressPaymentFragment();
    }

    public interface CallBack {

        void onOneTapCanceled();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
        @Nullable final ViewGroup container,
        @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.px_fragment_express_payment, container, false);
    }

    //region Low res config
    @NonNull
    private PaymentMethodFragmentAdapter getAdapter(@NonNull final List<DrawableFragmentItem> items) {
        return ScaleUtil.isLowRes(getContext()) ? new PaymentMethodFragmentAdapterLowRes(getChildFragmentManager(),
            items) : new PaymentMethodFragmentAdapter(getChildFragmentManager(), items);
    }
    // endregion Low res config

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {

        configureViews(view);

        //Order is important - On click and events should be wired AFTER view is attached.
        presenter = createPresenter(view.getContext());
        presenter.attachView(this);

        //Add interaction listeners.
        summaryView.setOnFitListener(this);
        summaryView.setOnAmountDescriptorListener(this);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                presenter.trackConfirmButton(paymentMethodPager.getCurrentItem());
                if (ApiUtil.checkConnection(getContext())) {
                    presenter.confirmPayment(paymentMethodPager.getCurrentItem());
                } else {
                    presenter.manageNoConnection();
                }
            }
        });
        paymentMethodPager.addOnPageChangeListener(this);

        if (savedInstanceState == null) {
            presenter.trackExpressView();
        }
    }

    private void configureViews(@NonNull final View view) {
        toolbarElementDescriptor = view.findViewById(R.id.element_descriptor_toolbar);
        summaryView = view.findViewById(R.id.summary_view);
        pagerAndConfirmButtonContainer = view.findViewById(R.id.container);
        installmentsSelectorSeparator = view.findViewById(R.id.installments_selector_separator);
        aspectRatioContainer = view.findViewById(R.id.aspect_ratio_container);
        paymentMethodPager = view.findViewById(R.id.payment_method_pager);
        indicator = view.findViewById(R.id.indicator);
        installmentsRecyclerView = view.findViewById(R.id.installments_recycler_view);
        confirmButton = view.findViewById(R.id.confirm_button);
        recyclerContainer = view.findViewById(R.id.installments_recycler_container);
        installmentsAnimation = new InstallmentsAnimation(installmentsRecyclerView);
        fadeAnimation = new FadeAnim(view.getContext());
        slideAnim = new SlideAnim();

        configureCardAspectRatio(aspectRatioContainer);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        installmentsRecyclerView.setLayoutManager(linearLayoutManager);
        installmentsRecyclerView
            .addItemDecoration(new DividerItemDecoration(view.getContext(), linearLayoutManager.getOrientation()));

        paymentMethodPager.setPageMargin(
            ((int) (getResources().getDimensionPixelSize(R.dimen.px_m_margin) * PAGER_NEGATIVE_MARGIN_MULTIPLIER)));
        paymentMethodPager.setOffscreenPageLimit(2);

        pagerAndConfirmButtonContainer.getViewTreeObserver()
            .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (pagerAndConfirmButtonContainer.getHeight() > 0) {
                        ViewGroup.LayoutParams params = recyclerContainer.getLayoutParams();
                        params.height = pagerAndConfirmButtonContainer.getHeight();
                        recyclerContainer.setLayoutParams(params);
                        pagerAndConfirmButtonContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });

        final PaymentMethodHeaderView paymentMethodHeaderView = view.findViewById(R.id.installments_header);
        paymentMethodHeaderView.setListener(new PaymentMethodHeaderView.Listener() {
            @Override
            public void onDescriptorViewClicked() {
                presenter.onInstallmentsRowPressed(paymentMethodPager.getCurrentItem());
            }

            @Override
            public void onInstallmentsSelectorCancelClicked() {
                presenter.onInstallmentSelectionCanceled(paymentMethodPager.getCurrentItem());
            }
        });

        final TitlePager titlePager = view.findViewById(R.id.title_pager);
        paymentMethodAdapter = new PaymentMethodAdapterImpl(titlePager, paymentMethodHeaderView,
            confirmButton, summaryView);
        titlePager.setAdapter(paymentMethodAdapter);

        configureToolbar(view);
    }

    private void configureCardAspectRatio(final FixedAspectRatioFrameLayout aspectRatioContainer) {
        if (ScaleUtil.isLowRes(aspectRatioContainer.getContext())) {
            aspectRatioContainer.setAspectRatio(ASPECT_RATIO_LOW_RES.first, ASPECT_RATIO_LOW_RES.second);
        } else {
            aspectRatioContainer.setAspectRatio(ASPECT_RATIO_HIGH_RES.first, ASPECT_RATIO_HIGH_RES.second);
        }
    }

    private ExpressPaymentPresenter createPresenter(@NonNull final Context context) {
        final Session session = Session.getSession(context);
        return new ExpressPaymentPresenter(session.getPaymentRepository(),
            session.getConfigurationModule().getPaymentSettings(),
            session.getDiscountRepository(),
            session.getAmountRepository(),
            session.getGroupsRepository());
    }

    @Override
    public void onViewStateRestored(@Nullable final Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            final PayerCostSelection payerCostSelection = savedInstanceState.getParcelable(BUNDLE_STATE_PAYER_COST);
            presenter.setPayerCostSelection(payerCostSelection);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        outState.putParcelable(BUNDLE_STATE_PAYER_COST, presenter.getPayerCostSelection());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.onViewResumed();
        presenter.updateElementPosition(paymentMethodPager.getCurrentItem());
    }

    @Override
    public void onPause() {
        presenter.onViewPaused();
        super.onPause();
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        if (context instanceof CallBack) {
            callback = (CallBack) context;
        }
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onDetach() {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        callback = null;
        presenter.detachView();
        super.onDetach();
    }

    @Override
    public void configureAdapters(@NonNull final List<DrawableFragmentItem> items, @NonNull final Site site,
        final int selectedPayerCost, @NonNull final PaymentMethodAdapter.Model paymentMethodViewModel) {
        installmentsAdapter = new InstallmentsAdapter(site, new ArrayList<PayerCost>(), selectedPayerCost, this);
        installmentsRecyclerView.setAdapter(installmentsAdapter);
        paymentMethodPager.setAdapter(getAdapter(items));
        // indicator must be after paymentMethodPager adapter is set.
        indicator.attachToPager(paymentMethodPager);
        paymentMethodAdapter.setModels(paymentMethodViewModel);
    }

    private void animateViewPagerDown() {
        slideAnim.slideDown(aspectRatioContainer, 0, pagerAndConfirmButtonContainer.getHeight());
        fadeAnimation.fadeOut(aspectRatioContainer);
        fadeAnimation.fadeOutFast(confirmButton);
        fadeAnimation.fadeOutFast(indicator);
    }

    @Override
    public void cancel() {
        if (callback != null) {
            callback.onOneTapCanceled();
        }
    }

    @Override
    public void showInstallmentsList(final List<PayerCost> payerCostList,
        final int payerCostSelected) {
        animateViewPagerDown();
        installmentsSelectorSeparator.setVisibility(VISIBLE);
        installmentsRecyclerView.setVisibility(VISIBLE);
        installmentsRecyclerView.scrollToPosition(payerCostSelected);
        installmentsAdapter.setPayerCosts(payerCostList);
        installmentsAdapter.setPayerCostSelected(payerCostSelected);
        installmentsAdapter.notifyDataSetChanged();
        installmentsAnimation.expand();
        paymentMethodAdapter.showInstallmentsList();
    }

    @Override
    public void hideInstallmentsSelection() {
        installmentsSelectorSeparator.setVisibility(INVISIBLE);
    }

    @Override
    public void showToolbarElementDescriptor(@NonNull final ElementDescriptorView.Model elementDescriptorModel) {
        toolbarElementDescriptor.update(elementDescriptorModel);
        toolbarElementDescriptor.setVisibility(VISIBLE);
    }

    @Override
    public void collapseInstallmentsSelection() {
        slideAnim.slideUp(aspectRatioContainer, pagerAndConfirmButtonContainer.getHeight(), 0);

        fadeAnimation.fadeInFastest(aspectRatioContainer);
        fadeAnimation.fadeIn(confirmButton);
        fadeAnimation.fadeIn(indicator);

        installmentsAnimation.collapse();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == REQ_CODE_CARD_VAULT && resultCode == RESULT_OK) {
            getActivity().getWindow().getDecorView().post(new Runnable() {
                @Override
                public void run() {
                    presenter.onTokenResolved(paymentMethodPager.getCurrentItem());
                }
            });
        } else if (requestCode == REQ_CODE_CARD_VAULT && resultCode == RESULT_CANCELED) {
            getActivity().getWindow().getDecorView().post(new Runnable() {
                @Override
                public void run() {
                    presenter.trackExpressView();
                }
            });
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void showPaymentProcessor() {
        final Intent intent = PaymentProcessorActivity.getIntent(getContext());
        startActivityForResult(intent, REQ_CODE_PAYMENT_PROCESSOR);
    }

    @Override
    public void showErrorScreen(@NonNull final MercadoPagoError error) {
        if (getActivity() != null) {
            ((CheckoutActivity) getActivity()).presenter.onPaymentError(error);
        }
    }

    @Override
    public void showErrorSnackBar(@NonNull final MercadoPagoError error) {
        Tracker.trackGenericError(TrackingUtil.VIEW_PATH_EXPRESS_CHECKOUT, ErrorView.ErrorType.SNACKBAR, error,
            error.getMessage());
        if (getView() != null && getActivity() != null) {
            MeliSnackbar.make(getView(), error.getMessage(), Snackbar.LENGTH_LONG,
                MeliSnackbar.SnackbarType.ERROR).show();
        }
    }

    @Override
    public void showInstallmentsDescriptionRow(final int paymentMethodIndex, final int payerCostSelected) {
        paymentMethodAdapter.updateData(paymentMethodIndex, payerCostSelected);
    }

    @Override
    public void showPaymentResult(@NonNull final IPayment paymentResult) {
        //TODO refactor
        if (getActivity() != null) {
            //TODO refactor
            if (paymentResult instanceof GenericPayment) {
                ((CheckoutActivity) getActivity()).presenter.onPaymentFinished((GenericPayment) paymentResult);
            } else if (paymentResult instanceof Payment) {
                ((CheckoutActivity) getActivity()).presenter.onPaymentFinished((Payment) paymentResult);
            } else {
                ((CheckoutActivity) getActivity()).presenter.onPaymentFinished((BusinessPayment) paymentResult);
            }
        }
    }

    //TODO refactor
    @Override
    public void onRecoverPaymentEscInvalid(final PaymentRecovery recovery) {
        if (getActivity() != null) {
            ((CheckoutActivity) getActivity()).presenter.onRecoverPaymentEscInvalid(recovery);
        }
    }

    @Override
    public void startPayment() {
        presenter.confirmPayment(paymentMethodPager.getCurrentItem());
    }

    @Override
    public void finishLoading(@NonNull final ExplodeDecorator params) {
        final FragmentManager childFragmentManager = getChildFragmentManager();
        final Fragment fragment = childFragmentManager.findFragmentByTag(TAG_EXPLODING_FRAGMENT);
        if (fragment != null && fragment.isAdded() && fragment.isVisible() && fragment instanceof ExplodingFragment) {
            ((ExplodingFragment) fragment).finishLoading(params);
        } else {
            presenter.hasFinishPaymentAnimation();
        }
    }

    @Override
    public void cancelLoading() {
        showConfirmButton();
        final FragmentManager childFragmentManager = getChildFragmentManager();
        final ExplodingFragment fragment =
            (ExplodingFragment) childFragmentManager.findFragmentByTag(TAG_EXPLODING_FRAGMENT);
        if (fragment != null && fragment.isAdded() && fragment.hasFinished()) {
            childFragmentManager
                .beginTransaction()
                .remove(fragment)
                .commitNowAllowingStateLoss();
            restoreStatusBar();
        }
    }

    private void restoreStatusBar() {
        if (getActivity() != null) {
            new StatusBarDecorator(getActivity().getWindow())
                .setupStatusBarColor(ContextCompat.getColor(getActivity(), R.color.px_colorPrimaryDark));
        }
    }

    private void configureToolbar(final View view) {
        final AppCompatActivity activity = (AppCompatActivity) getActivity();

        if (activity != null) {
            final Toolbar toolbar = view.findViewById(R.id.toolbar);
            activity.setSupportActionBar(toolbar);

            actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayShowTitleEnabled(false);
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowHomeEnabled(true);
                enableToolbarBack();
            }
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    presenter.cancel();
                    trackAbortExpress();
                }
            });
        }
    }

    void trackAbortExpress() {
        Tracker.trackAbortExpress();
    }

    private void showConfirmButton() {
        confirmButton.clearAnimation();
        confirmButton.setVisibility(VISIBLE);
    }

    private void hideConfirmButton() {
        confirmButton.clearAnimation();
        confirmButton.setVisibility(INVISIBLE);
    }

    @Override
    public void enableToolbarBack() {
        actionBar.setHomeButtonEnabled(true);
    }

    @Override
    public void disableToolbarBack() {
        actionBar.setHomeButtonEnabled(false);
    }

    @Override
    public void startLoadingButton(final int paymentTimeout) {
        hideConfirmButton();
        final int[] location = new int[2];
        confirmButton.getLocationOnScreen(location);

        final ExplodeParams explodeParams =
            new ExplodeParams(location[1] - confirmButton.getMeasuredHeight() / 2, confirmButton.getMeasuredHeight(),
                (int) getContext().getResources().getDimension(R.dimen.px_m_margin),
                getContext().getResources().getString(R.string.px_processing_payment_button),
                paymentTimeout);

        final FragmentManager childFragmentManager = getChildFragmentManager();
        final ExplodingFragment explodingFragment = ExplodingFragment.newInstance(explodeParams);
        childFragmentManager.beginTransaction()
            .replace(R.id.exploding_frame, explodingFragment, TAG_EXPLODING_FRAGMENT)
            .commitNowAllowingStateLoss();
        childFragmentManager.executePendingTransactions();
    }

    @Override
    public void showCardFlow(@NonNull final Card card) {
        new Constants.Activities.CardVaultActivityBuilder()
            .setCard(card)
            .startActivity(this, REQ_CODE_CARD_VAULT);
    }

    @Override
    public void onClick(final PayerCost payerCostSelected) {
        presenter.onPayerCostSelected(paymentMethodPager.getCurrentItem(), payerCostSelected);
    }

    @Override
    public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
        paymentMethodAdapter.updatePosition(positionOffset, position);
    }

    @Override
    public void onPageSelected(final int position) {
        Tracker.trackSwipeExpress();
        presenter.onSliderOptionSelected(position);
        VibrationUtils.smallVibration(getContext());
    }

    @Override
    public void onPageScrollStateChanged(final int state) {
        // do nothing.
    }

    @Override
    public void onBigHeaderOverlaps() {
        toolbarElementDescriptor.setVisibility(VISIBLE);
    }

    @Override
    public void onBigHeaderDoesNotOverlaps() {
        toolbarElementDescriptor.setVisibility(View.GONE);
    }

    @Override
    public void onAmountDescriptorClicked() {
        Tracker.trackExpressDiscountView();
        DiscountDetailDialog.showDialog(getFragmentManager());
    }

    @Override
    public void onAnimationFinished() {
        presenter.hasFinishPaymentAnimation();
    }
}
