package com.mercadopago.android.px.internal.features.guessing_card;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.adapters.IdentificationTypesAdapter;
import com.mercadopago.android.px.internal.callbacks.PaymentMethodSelectionCallback;
import com.mercadopago.android.px.internal.callbacks.card.CardExpiryDateEditTextCallback;
import com.mercadopago.android.px.internal.callbacks.card.CardIdentificationNumberEditTextCallback;
import com.mercadopago.android.px.internal.callbacks.card.CardNumberEditTextCallback;
import com.mercadopago.android.px.internal.callbacks.card.CardSecurityCodeEditTextCallback;
import com.mercadopago.android.px.internal.callbacks.card.CardholderNameEditTextCallback;
import com.mercadopago.android.px.internal.controllers.PaymentMethodGuessingController;
import com.mercadopago.android.px.internal.di.CardAssociationSession;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.features.Constants;
import com.mercadopago.android.px.internal.features.MercadoPagoBaseActivity;
import com.mercadopago.android.px.internal.features.ReviewPaymentMethodsActivity;
import com.mercadopago.android.px.internal.features.card.CardExpiryDateTextWatcher;
import com.mercadopago.android.px.internal.features.card.CardIdentificationNumberTextWatcher;
import com.mercadopago.android.px.internal.features.card.CardNumberTextWatcher;
import com.mercadopago.android.px.internal.features.card.CardSecurityCodeTextWatcher;
import com.mercadopago.android.px.internal.features.card.CardholderNameTextWatcher;
import com.mercadopago.android.px.internal.features.guessing_card.card_association_result.CardAssociationResultErrorActivity;
import com.mercadopago.android.px.internal.features.guessing_card.card_association_result.CardAssociationResultSuccessActivity;
import com.mercadopago.android.px.internal.features.providers.GuessingCardProviderImpl;
import com.mercadopago.android.px.internal.features.uicontrollers.card.CardRepresentationModes;
import com.mercadopago.android.px.internal.features.uicontrollers.card.CardView;
import com.mercadopago.android.px.internal.features.uicontrollers.card.IdentificationCardView;
import com.mercadopago.android.px.internal.util.ErrorUtil;
import com.mercadopago.android.px.internal.util.JsonUtil;
import com.mercadopago.android.px.internal.util.MPAnimationUtils;
import com.mercadopago.android.px.internal.util.MPCardMaskUtil;
import com.mercadopago.android.px.internal.util.ScaleUtil;
import com.mercadopago.android.px.internal.util.ViewUtils;
import com.mercadopago.android.px.internal.view.MPEditText;
import com.mercadopago.android.px.internal.view.MPTextView;
import com.mercadopago.android.px.model.CardInfo;
import com.mercadopago.android.px.model.IdentificationType;
import com.mercadopago.android.px.model.Issuer;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.PaymentType;
import com.mercadopago.android.px.model.PaymentTypes;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.CardTokenException;
import com.mercadopago.android.px.model.exceptions.ExceptionHandler;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.tracking.internal.events.FrictionEventTracker;
import com.mercadopago.android.px.tracking.internal.views.GuessingRootViewTracker;
import java.util.List;

import static com.mercadopago.android.px.internal.features.Constants.RESULT_SILENT_ERROR;

public class GuessingCardActivity extends MercadoPagoBaseActivity implements GuessingCardActivityView,
    CardExpiryDateEditTextCallback, View.OnTouchListener, View.OnClickListener {

    public static final int REVIEW_PAYMENT_METHODS_REQUEST_CODE = 21;

    public static final String PARAM_INCLUDES_PAYMENT = "includesPayment";
    public static final String PARAM_ACCESS_TOKEN = "accessToken";
    public static final String PARAM_PAYMENT_RECOVERY = "paymentRecovery";

    public static final String CARD_NUMBER_INPUT = "cardNumber";
    public static final String CARDHOLDER_NAME_INPUT = "cardHolderName";
    public static final String CARD_EXPIRYDATE_INPUT = "cardExpiryDate";
    public static final String CARD_SECURITYCODE_INPUT = "cardSecurityCode";
    public static final String CARD_IDENTIFICATION_INPUT = "cardIdentification";
    public static final String CARD_IDENTIFICATION = "identification";

    public static final String ERROR_STATE = "textview_error";
    public static final String NORMAL_STATE = "textview_normal";
    protected static final String EXTRA_ISSUERS = "issuers";

    protected GuessingCardPresenter mPresenter;
    protected MPEditText mCardNumberEditText;
    protected CardView mCardView;
    protected LinearLayout mButtonContainer;
    protected MPEditText mCardHolderNameEditText;
    protected MPEditText mSecurityCodeEditText;
    protected boolean mButtonContainerMustBeShown;
    protected IdentificationCardView mIdentificationCardView;
    protected Spinner mIdentificationTypeSpinner;
    protected MPEditText mIdentificationNumberEditText;
    //View controls
    protected ScrollView mScrollView;
    //ViewMode
    private boolean mLowResActive;
    private Activity mActivity;
    //View Low Res
    private Toolbar mLowResToolbar;
    private MPTextView mLowResTitleToolbar;
    //View Normal
    private Toolbar mNormalToolbar;
    private MPTextView mBankDealsTextView;
    private FrameLayout mCardBackground;
    private FrameLayout mCardViewContainer;
    private FrameLayout mIdentificationCardContainer;
    //Input Views
    private ViewGroup mProgressLayout;
    private LinearLayout mInputContainer;
    private LinearLayout mIdentificationTypeContainer;
    private FrameLayout mNextButton;
    private FrameLayout mBackButton;
    private FrameLayout mBackInactiveButton;
    private MPEditText mCardExpiryDateEditText;
    private LinearLayout mCardNumberInput;
    private LinearLayout mCardholderNameInput;
    private LinearLayout mCardExpiryDateInput;
    private LinearLayout mCardIdentificationInput;
    private LinearLayout mCardSecurityCodeInput;
    private FrameLayout mErrorContainer;
    private FrameLayout mRedErrorContainer;
    private FrameLayout mBlackInfoContainer;
    private MPTextView mInfoTextView;
    private MPTextView mErrorTextView;
    private String mErrorState;
    private TextView mBackInactiveButtonText;
    private Animation mContainerUpAnimation;
    private Animation mContainerDownAnimation;
    //Input Controls
    private String mCurrentEditingEditText;
    private String mCardSideState;
    private boolean mActivityActive;

    /**
     * Starts the guessing card flow with the purpose of storing the card in the users card vault This flows does NOT
     * includes a payment
     *
     * @param callerActivity: the activity that calls this one
     * @param accessToken: user accessToken
     * @param requestCode: the caller request code
     */
    public static void startGuessingCardActivityForStorage(final Activity callerActivity, final String accessToken,
        final int requestCode) {
        final Intent intent = new Intent(callerActivity, GuessingCardActivity.class);
        intent.putExtra(PARAM_ACCESS_TOKEN, accessToken);
        intent.putExtra(GuessingCardActivity.PARAM_INCLUDES_PAYMENT, false);
        callerActivity.startActivityForResult(intent, requestCode);
    }

    /**
     * Starts the guessing card flow with the purpose of performing a payment in the endO
     *
     * @param callerActivity: the activity that calls this one
     * @param paymentRecovery: payment recovery
     */
    public static void startGuessingCardActivityForPayment(final Activity callerActivity,
        final PaymentRecovery paymentRecovery) {
        final Intent intent = new Intent(callerActivity, GuessingCardActivity.class);
        intent.putExtra(PARAM_PAYMENT_RECOVERY, JsonUtil.getInstance().toJson(paymentRecovery));
        intent.putExtra(PARAM_PAYMENT_RECOVERY, JsonUtil.getInstance().toJson(paymentRecovery));
        intent.putExtra(GuessingCardActivity.PARAM_INCLUDES_PAYMENT, true);
        callerActivity.startActivityForResult(intent, Constants.Activities.GUESSING_CARD_FOR_PAYMENT_REQUEST_CODE);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        mActivityActive = true;
        mButtonContainerMustBeShown = true;
        analizeLowRes();
        setContentView();
        setupPresenter();
    }

    @Override
    protected void onResume() {
        mActivityActive = true;
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mActivityActive = false;
        mPresenter.detachView();
        mPresenter.detachResourceProvider();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        mActivityActive = false;
        super.onPause();
    }

    @Override
    protected void onStop() {
        mActivityActive = false;
        super.onStop();
    }

    @Override
    public void setupPresenter() {
        final Intent intent = getIntent();

        final boolean includesPayment = intent.getBooleanExtra(PARAM_INCLUDES_PAYMENT, true);

        if (includesPayment) {
            final PaymentRecovery paymentRecovery =
                JsonUtil.getInstance().fromJson(intent.getStringExtra("paymentRecovery"), PaymentRecovery.class);
            mPresenter =
                GuessingCardPresenter.buildGuessingCardPaymentPresenter(Session.getSession(this), paymentRecovery);
        } else {
            final String accessToken = intent.getStringExtra(PARAM_ACCESS_TOKEN);
            mPresenter = GuessingCardPresenter
                .buildGuessingCardStoragePresenter(CardAssociationSession.getCardAssociationSession(this), accessToken);
        }

        mPresenter.attachResourcesProvider(new GuessingCardProviderImpl(this));
        mPresenter.attachView(this);
        mPresenter.initialize();
    }

    @Override
    public void onValidStart() {
        initializeViews();
        loadViews();
        decorate();
        showInputContainer();
        mErrorState = NORMAL_STATE;
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        mPresenter.onSaveInstanceState(outState, mCardSideState, mLowResActive);
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mPresenter.onRestoreInstanceState(savedInstanceState);
        validatePaymentConfiguration();
    }

    //TODO remove method after session is persisted
    private void validatePaymentConfiguration() {
        final Session session = Session.getSession(this);
        try {
            session.getConfigurationModule().getPaymentSettings().getPaymentConfiguration().getCharges();
            session.getConfigurationModule().getPaymentSettings().getPaymentConfiguration().getPaymentProcessor();
        } catch (Exception e) {
            FrictionEventTracker.with(GuessingRootViewTracker.PATH,
                FrictionEventTracker.Id.SILENT, FrictionEventTracker.Style.SCREEN, e.getStackTrace().toString())
                .track();

            exitCheckout(RESULT_SILENT_ERROR);
        }
    }

    public void exitCheckout(final int resCode) {
        overrideTransitionOut();
        setResult(resCode);
        finish();
    }

    private void analizeLowRes() {
        mLowResActive = ScaleUtil.isLowRes(this);
    }

    private void setContentView() {
        if (mLowResActive) {
            setContentViewLowRes();
        } else {
            setContentViewNormal();
        }
    }

    private void setContentViewLowRes() {
        setContentView(R.layout.px_activity_form_card_lowres);
    }

    private void setContentViewNormal() {
        setContentView(R.layout.px_activity_form_card_normal);
    }

    @Override
    public void showError(final MercadoPagoError error, final String requestOrigin) {
        if (error.isApiException()) {
            showApiException(error.getApiException(), requestOrigin);
        } else {
            ErrorUtil.startErrorActivity(this, error);
        }
    }

    public void showApiException(final ApiException apiException, final String requestOrigin) {
        if (mActivityActive) {
            ErrorUtil.showApiExceptionError(this, apiException, requestOrigin);
        }
    }

    private void initializeViews() {
        if (mLowResActive) {
            mLowResToolbar = findViewById(R.id.mpsdkLowResToolbar);
            mLowResTitleToolbar = findViewById(R.id.mpsdkTitle);
            mLowResToolbar.setVisibility(View.VISIBLE);
        } else {
            mNormalToolbar = findViewById(R.id.mpsdkTransparentToolbar);
            mCardBackground = findViewById(R.id.mpsdkCardBackground);
            mCardViewContainer = findViewById(R.id.mpsdkCardViewContainer);
            mIdentificationCardContainer = findViewById(R.id.mpsdkIdentificationCardContainer);
        }

        mIdentificationTypeContainer = findViewById(R.id.mpsdkCardIdentificationTypeContainer);
        mIdentificationTypeSpinner = findViewById(R.id.mpsdkCardIdentificationType);
        mBankDealsTextView = findViewById(R.id.mpsdkBankDealsText);
        mCardNumberEditText = findViewById(R.id.mpsdkCardNumber);
        mCardHolderNameEditText = findViewById(R.id.mpsdkCardholderName);
        mCardExpiryDateEditText = findViewById(R.id.mpsdkCardExpiryDate);
        mSecurityCodeEditText = findViewById(R.id.mpsdkCardSecurityCode);
        mIdentificationNumberEditText = findViewById(R.id.mpsdkCardIdentificationNumber);
        mInputContainer = findViewById(R.id.mpsdkInputContainer);
        mProgressLayout = findViewById(R.id.mpsdkProgressLayout);
        mNextButton = findViewById(R.id.mpsdkNextButton);
        mBackButton = findViewById(R.id.mpsdkBackButton);
        mBackInactiveButton = findViewById(R.id.mpsdkBackInactiveButton);
        mBackInactiveButtonText = findViewById(R.id.mpsdkBackInactiveButtonText);
        mButtonContainer = findViewById(R.id.mpsdkButtonContainer);
        mCardNumberInput = findViewById(R.id.mpsdkCardNumberInput);
        mCardholderNameInput = findViewById(R.id.mpsdkNameInput);
        mCardExpiryDateInput = findViewById(R.id.mpsdkExpiryDateInput);
        mCardIdentificationInput = findViewById(R.id.mpsdkCardIdentificationInput);
        mCardSecurityCodeInput = findViewById(R.id.mpsdkCardSecurityCodeContainer);
        mErrorContainer = findViewById(R.id.mpsdkErrorContainer);
        mRedErrorContainer = findViewById(R.id.mpsdkRedErrorContainer);
        mBlackInfoContainer = findViewById(R.id.mpsdkBlackInfoContainer);
        mInfoTextView = findViewById(R.id.mpsdkBlackInfoTextView);
        mErrorTextView = findViewById(R.id.mpsdkErrorTextView);
        mScrollView = findViewById(R.id.mpsdkScrollViewContainer);
        mContainerUpAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.px_slide_bottom_up);
        mContainerDownAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.px_slide_bottom_down);

        fullScrollDown();
    }

    @Override
    public void setContainerAnimationListeners() {
        mContainerUpAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(final Animation animation) {
                //Do nothing
            }

            @Override
            public void onAnimationEnd(final Animation animation) {
                if (!mButtonContainerMustBeShown) {
                    mButtonContainer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationRepeat(final Animation animation) {
                //Do nothing
            }
        });
        mContainerDownAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(final Animation animation) {
                mButtonContainer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(final Animation animation) {
                //Do nothing
            }

            @Override
            public void onAnimationRepeat(final Animation animation) {
                //Do nothing
            }
        });
    }

    @Override
    public void showInputContainer() {
        mIdentificationTypeContainer.setVisibility(View.GONE);
        mProgressLayout.setVisibility(View.GONE);
        mInputContainer.setVisibility(View.VISIBLE);
        requestCardNumberFocus();
    }

    private void loadViews() {
        if (mLowResActive) {
            loadLowResViews();
        } else {
            loadNormalViews();
        }
    }

    protected boolean cardViewsActive() {
        return !mLowResActive;
    }

    private void loadLowResViews() {
        loadToolbarArrow(mLowResToolbar);
    }

    private void loadNormalViews() {
        loadToolbarArrow(mNormalToolbar);

        mCardView = new CardView(mActivity);
        mCardView.setSize(CardRepresentationModes.BIG_SIZE);
        mCardView.inflateInParent(mCardViewContainer, true);
        mCardView.initializeControls();
        mCardView.draw(CardView.CARD_SIDE_FRONT);
        mCardSideState = CardView.CARD_SIDE_FRONT;

        mIdentificationCardView = new IdentificationCardView(mActivity);
        mIdentificationCardView.inflateInParent(mIdentificationCardContainer, true);
        mIdentificationCardView.initializeControls();
        mIdentificationCardView.hide();
    }

    private void loadToolbarArrow(final Toolbar toolbar) {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        if (toolbar != null) {
            toolbar.setOnClickListener(this);
        }
    }

    private void decorate() {
        if (mLowResActive) {
            decorateLowRes();
        } else {
            decorateNormal();
        }
    }

    private void decorateLowRes() {
        mBackInactiveButtonText.setTextColor(ContextCompat.getColor(this, R.color.px_warm_grey_with_alpha));
    }

    private void decorateNormal() {
        mBackInactiveButtonText.setTextColor(ContextCompat.getColor(this, R.color.px_warm_grey_with_alpha));
    }

    private String getCardNumberTextTrimmed() {
        return mCardNumberEditText.getText().toString().replaceAll("\\s", "");
    }

    @Override
    public void initializeTitle() {
        if (mLowResActive) {
            final String paymentTypeId = mPresenter.getPaymentTypeId();
            String paymentTypeText = getString(R.string.px_form_card_title);
            if (paymentTypeId != null) {
                if (paymentTypeId.equals(PaymentTypes.CREDIT_CARD)) {
                    paymentTypeText = getString(R.string.px_form_card_title_payment_type,
                        getString(R.string.px_credit_payment_type));
                } else if (paymentTypeId.equals(PaymentTypes.DEBIT_CARD)) {
                    paymentTypeText = getString(R.string.px_form_card_title_payment_type,
                        getString(R.string.px_debit_payment_type));
                } else if (paymentTypeId.equals(PaymentTypes.PREPAID_CARD)) {
                    paymentTypeText = getString(R.string.px_form_card_title_payment_type_prepaid);
                }
            }
            mLowResTitleToolbar.setText(paymentTypeText);
        }
    }

    @Override
    public void showBankDeals() {
        if (mLowResActive) {
            mBankDealsTextView.setText(getString(R.string.px_bank_deals_lowres));
        } else {
            mBankDealsTextView.setText(getString(R.string.px_bank_deals_action));
        }

        mBankDealsTextView.setVisibility(View.VISIBLE);
        mBankDealsTextView.setFocusable(true);
        mBankDealsTextView.setOnClickListener(this);
    }

    @Override
    public void hideBankDeals() {
        mBankDealsTextView.setVisibility(View.GONE);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void setCardNumberListeners(final PaymentMethodGuessingController controller) {
        mCardNumberEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
                return onNextKey(actionId, event);
            }
        });
        mCardNumberEditText.setOnTouchListener(this);
        mCardNumberEditText.addTextChangedListener(new CardNumberTextWatcher(
            controller,
            new PaymentMethodSelectionCallback() {
                @Override
                public void onPaymentMethodListSet(final List<PaymentMethod> paymentMethodList, final String bin) {
                    mPresenter.resolvePaymentMethodListSet(paymentMethodList, bin);
                }

                @Override
                public void onPaymentMethodCleared() {
                    mPresenter.resolvePaymentMethodCleared();
                }
            },
            new CardNumberEditTextCallback() {
                @Override
                public void checkOpenKeyboard() {
                    openKeyboard(mCardNumberEditText);
                }

                @Override
                public void saveCardNumber(final CharSequence string) {
                    mPresenter.saveCardNumber(string.toString());
                    if (cardViewsActive()) {
                        mCardView.drawEditingCardNumber(string.toString());
                    }
                    mPresenter.setCurrentNumberLength(string.length());
                }

                @Override
                public void appendSpace(final CharSequence currentNumber) {
                    if (MPCardMaskUtil.needsMask(currentNumber, mPresenter.getCardNumberLength())) {
                        mCardNumberEditText.append(" ");
                    }
                }

                @Override
                public void deleteChar(final CharSequence s) {
                    if (MPCardMaskUtil.needsMask(s, mPresenter.getCardNumberLength())) {
                        mCardNumberEditText.getText().delete(s.length() - 1, s.length());
                    }
                    mPresenter.setCurrentNumberLength(s.length());
                }

                @Override
                public void changeErrorView() {
                    checkChangeErrorView();
                }

                @Override
                public void toggleLineColorOnError(final boolean toggle) {
                    mCardNumberEditText.toggleLineColorOnError(toggle);
                }
            }));
    }

    @Override
    public void resolvePaymentMethodSet(final PaymentMethod paymentMethod) {
        hideExclusionWithOneElementInfoView();
    }

    @Override
    public void clearSecurityCodeEditText() {
        mSecurityCodeEditText.getText().clear();
    }

    @Override
    public void checkClearCardView() {
        if (cardViewsActive()) {
            mCardView.clearPaymentMethod();
        }
    }

    @Override
    public void eraseDefaultSpace() {
        setEditText(mCardNumberEditText, getCardNumberTextTrimmed());
    }

    private void setEditText(final MPEditText editText, final CharSequence text) {
        editText.setText(text);
        editText.setSelection(editText.getText().length());
    }

    @Override
    public void setPaymentMethod(final PaymentMethod paymentMethod) {
        if (cardViewsActive()) {
            mCardView.setPaymentMethod(paymentMethod);
            mCardView.setCardNumberLength(mPresenter.getCardNumberLength());
            mCardView.setSecurityCodeLength(mPresenter.getSecurityCodeLength());
            mCardView.setSecurityCodeLocation(mPresenter.getSecurityCodeLocation());
            mCardView.updateCardNumberMask(getCardNumberTextTrimmed());
            mCardView.transitionPaymentMethodSet();
        }
    }

    @Override
    public void recoverCardViews(final boolean lowResActive, final String cardNumber, final String cardHolderName,
        final String expiryMonth,
        final String expiryYear, final String identificationNumber, final IdentificationType identificationType) {
        if (mCardView == null) {
            loadViews();
        }
        if (cardViewsActive()) {
            mCardView.drawEditingCardNumber(cardNumber);
            mCardView.drawEditingCardHolderName(cardHolderName);
            mCardView.drawEditingExpiryMonth(expiryMonth);
            mCardView.drawEditingExpiryYear(expiryYear);
            mIdentificationCardView.setIdentificationNumber(identificationNumber);
            mIdentificationCardView.setIdentificationType(identificationType);
            mIdentificationCardView.draw();
            mCardView.updateCardNumberMask(getCardNumberTextTrimmed());
            clearSecurityCodeEditText();
            requestCardNumberFocus();
        }
    }

    @Override
    public void setNextButtonListeners() {
        mNextButton.setOnClickListener(this);
    }

    @Override
    public void setBackButtonListeners() {
        mBackButton.setOnClickListener(this);
    }

    @Override
    public void setErrorContainerListener() {
        mRedErrorContainer.setOnClickListener(this);
    }

    private void startReviewPaymentMethodsActivity(final List<PaymentMethod> supportedPaymentMethods) {
        ReviewPaymentMethodsActivity.start(this, supportedPaymentMethods, REVIEW_PAYMENT_METHODS_REQUEST_CODE);
        overridePendingTransition(R.anim.px_slide_up_activity, R.anim.px_no_change_animation);
    }

    @Override
    public void setCardholderName(final String cardholderName) {
        mCardHolderNameEditText.setText(cardholderName);
        if (cardViewsActive()) {
            mCardView.fillCardholderName(cardholderName);
        }
    }

    @Override
    public void setIdentificationNumber(final String identificationNumber) {
        mIdentificationNumberEditText.setText(identificationNumber);
        if (cardViewsActive()) {
            mIdentificationCardView.setIdentificationNumber(identificationNumber);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void setCardholderNameListeners() {
        mCardHolderNameEditText.setFilters(new InputFilter[] { new InputFilter.AllCaps() });
        mCardHolderNameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
                return onNextKey(actionId, event);
            }
        });
        mCardHolderNameEditText.setOnTouchListener(this);
        mCardHolderNameEditText
            .addTextChangedListener(new CardholderNameTextWatcher(new CardholderNameEditTextCallback() {
                @Override
                public void checkOpenKeyboard() {
                    openKeyboard(mCardHolderNameEditText);
                }

                @Override
                public void saveCardholderName(final CharSequence string) {
                    mPresenter.saveCardholderName(string.toString());
                    if (cardViewsActive()) {
                        mCardView.drawEditingCardHolderName(string.toString());
                    }
                }

                @Override
                public void changeErrorView() {
                    checkChangeErrorView();
                }

                @Override
                public void toggleLineColorOnError(final boolean toggle) {
                    mCardHolderNameEditText.toggleLineColorOnError(toggle);
                }
            }));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void setExpiryDateListeners() {
        mCardExpiryDateEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
                return onNextKey(actionId, event);
            }
        });
        mCardExpiryDateEditText.setOnTouchListener(this);
        mCardExpiryDateEditText.addTextChangedListener(new CardExpiryDateTextWatcher(this));
    }

    @Override
    public void checkOpenKeyboard() {
        openKeyboard(mCardExpiryDateEditText);
    }

    @Override
    public void saveExpiryMonth(final CharSequence string) {
        mPresenter.saveExpiryMonth(string.toString());
        if (cardViewsActive()) {
            mCardView.drawEditingExpiryMonth(string.toString());
        }
    }

    @Override
    public void saveExpiryYear(final CharSequence string) {
        mPresenter.saveExpiryYear(string.toString());
        if (cardViewsActive()) {
            mCardView.drawEditingExpiryYear(string.toString());
        }
    }

    @Override
    public void changeErrorView() {
        checkChangeErrorView();
    }

    @Override
    public void toggleLineColorOnError(final boolean toggle) {
        mCardExpiryDateEditText.toggleLineColorOnError(toggle);
    }

    @Override
    public void appendDivider() {
        mCardExpiryDateEditText.append("/");
    }

    @Override
    public void deleteChar(final CharSequence string) {
        mCardExpiryDateEditText.getText().delete(string.length() - 1, string.length());
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void setSecurityCodeListeners() {
        mSecurityCodeEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
                return onNextKey(actionId, event);
            }
        });
        mSecurityCodeEditText.setOnTouchListener(this);
        mSecurityCodeEditText
            .addTextChangedListener(new CardSecurityCodeTextWatcher(new CardSecurityCodeEditTextCallback() {
                @Override
                public void checkOpenKeyboard() {
                    openKeyboard(mSecurityCodeEditText);
                }

                @Override
                public void saveSecurityCode(final CharSequence string) {
                    mPresenter.saveSecurityCode(string.toString());
                    if (cardViewsActive()) {
                        mCardView.setSecurityCodeLocation(mPresenter.getSecurityCodeLocation());
                        mCardView.drawEditingSecurityCode(string.toString());
                    }
                }

                @Override
                public void changeErrorView() {
                    checkChangeErrorView();
                }

                @Override
                public void toggleLineColorOnError(final boolean toggle) {
                    mSecurityCodeEditText.toggleLineColorOnError(toggle);
                }
            }));
    }

    @Override
    public void setIdentificationTypeListeners() {
        mIdentificationTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> adapterView, final View view, final int i, final long l) {
                mPresenter.saveIdentificationType((IdentificationType) mIdentificationTypeSpinner.getSelectedItem());
            }

            @Override
            public void onNothingSelected(final AdapterView<?> adapterView) {
                //Do something
            }
        });
        mIdentificationTypeSpinner.setOnTouchListener(this);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void setIdentificationNumberListeners() {
        mIdentificationNumberEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
                return onNextKey(actionId, event);
            }
        });
        mIdentificationNumberEditText.setOnTouchListener(this);
        mIdentificationNumberEditText.addTextChangedListener(
            new CardIdentificationNumberTextWatcher(new CardIdentificationNumberEditTextCallback() {
                @Override
                public void checkOpenKeyboard() {
                    openKeyboard(mIdentificationNumberEditText);
                }

                @Override
                public void saveIdentificationNumber(final CharSequence string) {
                    mPresenter.saveIdentificationNumber(string.toString());
                    if (mPresenter.getIdentificationNumberMaxLength() == string.length()) {
                        mPresenter.saveIdentificationNumber(string.toString());
                        mPresenter.validateIdentificationNumber();
                    }
                    if (cardViewsActive()) {
                        mIdentificationCardView.setIdentificationNumber(string.toString());
                        if (showingIdentification()) {
                            mIdentificationCardView.draw();
                        }
                    }
                }

                @Override
                public void changeErrorView() {
                    checkChangeErrorView();
                }

                @Override
                public void toggleLineColorOnError(final boolean toggle) {
                    mIdentificationNumberEditText.toggleLineColorOnError(toggle);
                }
            }));
    }

    @Override
    public void setIdentificationNumberRestrictions(final String type) {
        setInputMaxLength(mIdentificationNumberEditText, mPresenter.getIdentificationNumberMaxLength());
        if ("number".equals(type)) {
            mIdentificationNumberEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        } else {
            mIdentificationNumberEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        }
        if (!mIdentificationNumberEditText.getText().toString().isEmpty()) {
            mPresenter.validateIdentificationNumber();
        }
    }

    @Override
    public void initializeIdentificationTypes(final List<IdentificationType> identificationTypes) {
        mIdentificationTypeSpinner.setAdapter(new IdentificationTypesAdapter(identificationTypes));
        mIdentificationTypeContainer.setVisibility(View.VISIBLE);
        if (cardViewsActive()) {
            mIdentificationCardView.setIdentificationType(identificationTypes.get(0));
        }
    }

    @Override
    public void setSecurityCodeViewLocation(final String location) {
        if (location.equals(CardView.CARD_SIDE_FRONT) && cardViewsActive()) {
            mCardView.hasToShowSecurityCodeInFront(true);
        }
    }

    protected boolean onNextKey(final int actionId, final KeyEvent event) {
        if (isNextKey(actionId, event)) {
            validateCurrentEditText();
            return true;
        }
        return false;
    }

    private void onTouchEditText(final MPEditText editText, final MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            openKeyboard(editText);
        }
    }

    private boolean isNextKey(final int actionId, final KeyEvent event) {
        return actionId == EditorInfo.IME_ACTION_NEXT ||
            (event != null && event.getAction() == KeyEvent.ACTION_DOWN
                && event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
    }

    @Override
    public void setSecurityCodeInputMaxLength(final int length) {
        setInputMaxLength(mSecurityCodeEditText, length);
    }

    @Override
    public void showApiExceptionError(final ApiException exception, final String requestOrigin) {
        ErrorUtil.showApiExceptionError(mActivity, exception, requestOrigin);
    }

    @Override
    public void setCardNumberInputMaxLength(final int length) {
        setInputMaxLength(mCardNumberEditText, length);
    }

    private void setInputMaxLength(final MPEditText text, final int maxLength) {
        final InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(maxLength);
        text.setFilters(fArray);
    }

    @Override
    public void clearCardNumberInputLength() {
        final int maxLength = MPCardMaskUtil.CARD_NUMBER_MAX_LENGTH;
        setInputMaxLength(mCardNumberEditText, maxLength);
    }

    protected void openKeyboard(final MPEditText ediText) {
        ediText.requestFocus();
        final InputMethodManager inputMethodManager =
            (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.showSoftInput(ediText, InputMethodManager.SHOW_IMPLICIT);
        }
        fullScrollDown();
    }

    private void fullScrollDown() {
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                mScrollView.fullScroll(View.FOCUS_DOWN);
            }
        };
        mScrollView.post(r);
        r.run();
    }

    private void requestCardNumberFocus() {
        mPresenter.trackCardNumber();
        disableBackInputButton();
        mCurrentEditingEditText = CARD_NUMBER_INPUT;
        openKeyboard(mCardNumberEditText);
        if (cardViewsActive()) {
            mCardView.drawEditingCardNumber(mPresenter.getCardNumber());
        } else {
            initializeTitle();
        }
    }

    private void requestCardHolderNameFocus() {
        if (!mPresenter.validateCardNumber()) {
            return;
        }
        mPresenter.trackCardHolderName();
        enableBackInputButton();
        mCurrentEditingEditText = CARDHOLDER_NAME_INPUT;
        openKeyboard(mCardHolderNameEditText);
        if (cardViewsActive()) {
            mCardView.drawEditingCardHolderName(mPresenter.getCardholderName());
        }
    }

    private void requestExpiryDateFocus() {
        if (!mPresenter.validateCardName()) {
            return;
        }
        mPresenter.trackCardExpiryDate();
        enableBackInputButton();
        mCurrentEditingEditText = CARD_EXPIRYDATE_INPUT;
        openKeyboard(mCardExpiryDateEditText);
        checkFlipCardToFront();
        if (cardViewsActive()) {
            mCardView.drawEditingExpiryMonth(mPresenter.getExpiryMonth());
            mCardView.drawEditingExpiryYear(mPresenter.getExpiryYear());
        } else {
            initializeTitle();
        }
    }

    private void requestSecurityCodeFocus() {
        if (!mPresenter.validateExpiryDate()) {
            return;
        }
        if (mCurrentEditingEditText.equals(CARD_EXPIRYDATE_INPUT) ||
            mCurrentEditingEditText.equals(CARD_IDENTIFICATION_INPUT) ||
            mCurrentEditingEditText.equals(CARD_SECURITYCODE_INPUT)) {
            mPresenter.trackCardSecurityCode();
            enableBackInputButton();
            mCurrentEditingEditText = CARD_SECURITYCODE_INPUT;
            openKeyboard(mSecurityCodeEditText);
            if (mPresenter.getSecurityCodeLocation().equals(CardView.CARD_SIDE_BACK)) {
                checkFlipCardToBack();
            } else {
                checkFlipCardToFront();
            }
            initializeTitle();
        }
    }

    private void requestIdentificationFocus() {
        if (mPresenter.isSecurityCodeRequired() ? !mPresenter.validateSecurityCode()
            : !mPresenter.validateExpiryDate()) {
            return;
        }
        mPresenter.trackCardIdentification();
        enableBackInputButton();
        mCurrentEditingEditText = CARD_IDENTIFICATION_INPUT;
        openKeyboard(mIdentificationNumberEditText);
        checkTransitionCardToId();
        if (mLowResActive) {
            mLowResTitleToolbar.setText(getResources().getString(R.string.px_form_identification_title));
        }
    }

    private void disableBackInputButton() {
        mBackButton.setVisibility(View.GONE);
        mBackInactiveButton.setVisibility(View.VISIBLE);
    }

    private void enableBackInputButton() {
        mBackButton.setVisibility(View.VISIBLE);
        mBackInactiveButton.setVisibility(View.GONE);
    }

    @Override
    public void hideIdentificationInput() {
        mCardIdentificationInput.setVisibility(View.GONE);
    }

    @Override
    public void hideSecurityCodeInput() {
        mCardSecurityCodeInput.setVisibility(View.GONE);
    }

    @Override
    public void showIdentificationInput() {
        mCardIdentificationInput.setVisibility(View.VISIBLE);
    }

    @Override
    public void setErrorView(final String message) {
        mButtonContainer.setVisibility(View.GONE);
        mErrorContainer.setVisibility(View.VISIBLE);
        mErrorTextView.setText(message);
        setErrorState(ERROR_STATE);
    }

    @Override
    public void setErrorView(final CardTokenException exception) {
        mButtonContainer.setVisibility(View.GONE);
        mErrorContainer.setVisibility(View.VISIBLE);
        final String errorText = ExceptionHandler.getErrorMessage(this, exception);
        mErrorTextView.setText(errorText);
        setErrorState(ERROR_STATE);
    }

    @Override
    public void setInvalidCardMultipleErrorView() {
        mButtonContainerMustBeShown = false;
        mRedErrorContainer.startAnimation(mContainerUpAnimation);
        mRedErrorContainer.setVisibility(View.VISIBLE);
        setErrorState(ERROR_STATE);
        setErrorCardNumber();
    }

    @Override
    public void setInvalidCardOnePaymentMethodErrorView() {
        mBlackInfoContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.px_error_red_pink));
        setErrorState(ERROR_STATE);
        setErrorCardNumber();
    }

    @Override
    public void setExclusionWithOneElementInfoView(final PaymentMethod supportedPaymentMethod,
        final boolean withAnimation) {
        if (withAnimation) {
            mButtonContainerMustBeShown = false;
            mBlackInfoContainer.startAnimation(mContainerUpAnimation);
        }
        mBlackInfoContainer.setVisibility(View.VISIBLE);
        mInfoTextView
            .setText(getResources().getString(R.string.px_exclusion_one_element, supportedPaymentMethod.getName()));
        if (!withAnimation) {
            mButtonContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void restoreBlackInfoContainerView() {
        mBlackInfoContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.ui_meli_black));
    }

    @Override
    public void hideExclusionWithOneElementInfoView() {
        if (mBlackInfoContainer.getVisibility() == View.VISIBLE) {
            mBlackInfoContainer.startAnimation(mContainerDownAnimation);
            mBlackInfoContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void clearErrorView() {
        mButtonContainer.setVisibility(View.VISIBLE);
        mErrorContainer.setVisibility(View.GONE);
        mErrorTextView.setText("");
        setErrorState(NORMAL_STATE);
    }

    @Override
    public void hideRedErrorContainerView(final boolean withAnimation) {
        if (mRedErrorContainer.getVisibility() == View.VISIBLE) {
            if (withAnimation) {
                mRedErrorContainer.startAnimation(mContainerDownAnimation);
            }
            mRedErrorContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void setErrorCardNumber() {
        mCardNumberEditText.toggleLineColorOnError(true);
        mCardNumberEditText.requestFocus();
    }

    @Override
    public void setErrorCardholderName() {
        mCardHolderNameEditText.toggleLineColorOnError(true);
        mCardHolderNameEditText.requestFocus();
    }

    @Override
    public void setErrorExpiryDate() {
        mCardExpiryDateEditText.toggleLineColorOnError(true);
        mCardExpiryDateEditText.requestFocus();
    }

    @Override
    public void setErrorSecurityCode() {
        mSecurityCodeEditText.toggleLineColorOnError(true);
        mSecurityCodeEditText.requestFocus();
    }

    @Override
    public void setErrorIdentificationNumber() {
        ViewUtils.openKeyboard(mIdentificationNumberEditText);
        mIdentificationNumberEditText.toggleLineColorOnError(true);
        mIdentificationNumberEditText.requestFocus();
    }

    @Override
    public void clearErrorIdentificationNumber() {
        mIdentificationNumberEditText.toggleLineColorOnError(false);
    }

    private void setErrorState(final String mErrorState) {
        this.mErrorState = mErrorState;
    }

    protected void checkChangeErrorView() {
        if (ERROR_STATE.equals(mErrorState)) {
            clearErrorView();
        }
    }

    private void validateCurrentEditText() {
        switch (mCurrentEditingEditText) {
        case CARD_NUMBER_INPUT:
            if (mPresenter.validateCardNumber()) {
                mCardNumberInput.setVisibility(View.GONE);
                requestCardHolderNameFocus();
            }
            break;
        case CARDHOLDER_NAME_INPUT:
            if (mPresenter.validateCardName()) {
                mCardholderNameInput.setVisibility(View.GONE);
                requestExpiryDateFocus();
            }
            break;
        case CARD_EXPIRYDATE_INPUT:
            if (mPresenter.validateExpiryDate()) {
                mCardExpiryDateInput.setVisibility(View.GONE);
                if (mPresenter.isSecurityCodeRequired()) {
                    requestSecurityCodeFocus();
                } else if (mPresenter.isIdentificationNumberRequired()) {
                    requestIdentificationFocus();
                } else {
                    mPresenter.checkFinishWithCardToken();
                }
            }
            break;
        case CARD_SECURITYCODE_INPUT:
            if (mPresenter.validateSecurityCode()) {
                mCardSecurityCodeInput.setVisibility(View.GONE);
                if (mPresenter.isIdentificationNumberRequired()) {
                    requestIdentificationFocus();
                } else {
                    mPresenter.checkFinishWithCardToken();
                }
            }
            break;
        case CARD_IDENTIFICATION_INPUT:
            if (mPresenter.validateIdentificationNumber()) {
                mPresenter.checkFinishWithCardToken();
            }
            break;
        default:
            break;
        }
    }

    private void checkIsEmptyOrValid() {
        switch (mCurrentEditingEditText) {
        case CARDHOLDER_NAME_INPUT:
            if (mPresenter.checkIsEmptyOrValidCardholderName()) {
                mCardNumberInput.setVisibility(View.VISIBLE);
                requestCardNumberFocus();
            }
            break;
        case CARD_EXPIRYDATE_INPUT:
            if (mPresenter.checkIsEmptyOrValidExpiryDate()) {
                mCardholderNameInput.setVisibility(View.VISIBLE);
                requestCardHolderNameFocus();
            }
            break;
        case CARD_SECURITYCODE_INPUT:
            if (mPresenter.checkIsEmptyOrValidSecurityCode()) {
                mCardExpiryDateInput.setVisibility(View.VISIBLE);
                requestExpiryDateFocus();
            }
            break;
        case CARD_IDENTIFICATION_INPUT:
            if (mPresenter.checkIsEmptyOrValidIdentificationNumber()) {
                if (mPresenter.isSecurityCodeRequired()) {
                    mCardSecurityCodeInput.setVisibility(View.VISIBLE);
                    requestSecurityCodeFocus();
                } else {
                    mCardExpiryDateInput.setVisibility(View.VISIBLE);
                    requestExpiryDateFocus();
                }
            }
            break;
        default:
        }
    }

    private void checkTransitionCardToId() {
        if (!mPresenter.isIdentificationNumberRequired()) {
            return;
        }
        if (showingFront() || showingBack()) {
            transitionToIdentification();
        }
    }

    private void checkFlipCardToBack() {
        if (showingFront()) {
            flipCardToBack();
        } else if (showingIdentification()) {
            if (cardViewsActive()) {
                MPAnimationUtils.transitionCardDisappear(this, mCardView, mIdentificationCardView);
            }
            mCardSideState = CardView.CARD_SIDE_BACK;
        }
    }

    private void checkFlipCardToFront() {
        if (showingBack() || showingIdentification()) {
            if (showingBack()) {
                flipCardToFrontFromBack();
            } else if (showingIdentification()) {
                if (cardViewsActive()) {
                    MPAnimationUtils.transitionCardDisappear(this, mCardView, mIdentificationCardView);
                }
                mCardSideState = CardView.CARD_SIDE_FRONT;
            }
        }
    }

    private void transitionToIdentification() {
        mCardSideState = CARD_IDENTIFICATION;
        if (cardViewsActive()) {
            MPAnimationUtils.transitionCardAppear(this, mCardView, mIdentificationCardView);
            mIdentificationCardView.draw();
        }
    }

    private void flipCardToBack() {
        mCardSideState = CardView.CARD_SIDE_BACK;
        if (cardViewsActive()) {
            mCardView.flipCardToBack(mPresenter.getPaymentMethod(), mPresenter.getSecurityCodeLength(),
                getWindow(), mCardBackground, mPresenter.getSecurityCode());
        }
    }

    private void flipCardToFrontFromBack() {
        mCardSideState = CardView.CARD_SIDE_FRONT;
        if (cardViewsActive()) {
            mCardView.flipCardToFrontFromBack(getWindow(), mCardBackground, mPresenter.getCardNumber(),
                mPresenter.getCardholderName(), mPresenter.getExpiryMonth(), mPresenter.getExpiryYear(),
                mPresenter.getSecurityCodeFront());
        }
    }

    private void initCardState() {
        if (mCardSideState == null) {
            mCardSideState = CardView.CARD_SIDE_FRONT;
        }
    }

    protected boolean showingIdentification() {
        initCardState();
        return mCardSideState.equals(CARD_IDENTIFICATION);
    }

    private boolean showingBack() {
        initCardState();
        return mCardSideState.equals(CardView.CARD_SIDE_BACK);
    }

    private boolean showingFront() {
        initCardState();
        return mCardSideState.equals(CardView.CARD_SIDE_FRONT);
    }

    @Override
    public void askForPaymentType(final List<PaymentMethod> paymentMethods, final List<PaymentType> paymentTypes,
        final CardInfo cardInfo) {
        new Constants.Activities.PaymentTypesActivityBuilder()
            .setActivity(mActivity)
            .setPaymentMethods(paymentMethods)
            .setPaymentTypes(paymentTypes)
            .setCardInfo(cardInfo)
            .startActivity();
        overridePendingTransition(R.anim.px_slide_right_to_left_in, R.anim.px_slide_right_to_left_out);
    }

    @Override
    public void showFinishCardFlow() {
        ViewUtils.hideKeyboard(this);
        showProgress();
        mPresenter.createToken();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.Activities.PAYMENT_TYPES_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                final Bundle bundle = data.getExtras();
                final String paymentTypeJson = bundle.getString("paymentType");
                if (!TextUtils.isEmpty(paymentTypeJson)) {
                    final PaymentType paymentType =
                        JsonUtil.getInstance().fromJson(paymentTypeJson, PaymentType.class);
                    mPresenter.setSelectedPaymentType(paymentType);
                    showFinishCardFlow();
                }
            } else if (resultCode == RESULT_CANCELED) {
                finish();
            }
        } else if (requestCode == REVIEW_PAYMENT_METHODS_REQUEST_CODE) {
            clearReviewPaymentMethodsMode();
        } else if (requestCode == ErrorUtil.ERROR_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                mPresenter.recoverFromFailure();
            } else {
                setResult(resultCode, data);
                finish();
            }
        } else if (requestCode == Constants.Activities.BANK_DEALS_REQUEST_CODE) {
            setSoftInputMode();
        } else if (requestCode == Constants.Activities.ISSUERS_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                final Long issuerId = data.getExtras().getLong("issuerId");
                ViewUtils.hideKeyboard(this);
                showProgress();
                mPresenter.onIssuerSelected(issuerId);
            }
        }
    }

    private void clearReviewPaymentMethodsMode() {
        mButtonContainerMustBeShown = true;
        clearErrorView();
        hideRedErrorContainerView(false);
        mCardNumberEditText.toggleLineColorOnError(false);
        mCardNumberEditText.getText().clear();
        openKeyboard(mCardNumberEditText);
    }

    /**
     * When guessing card has non - automatic selected issuer then should show issuers screen.
     *
     * @param issuers all issuers
     */
    @Override
    public void finishCardFlow(@NonNull final List<Issuer> issuers) {
        overridePendingTransition(R.anim.px_slide_right_to_left_in, R.anim.px_slide_right_to_left_out);
        final Intent returnIntent = new Intent();
        returnIntent.putExtra(EXTRA_ISSUERS, JsonUtil.getInstance().toJson(issuers));
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void finishCardFlow() {
        overridePendingTransition(R.anim.px_slide_right_to_left_in, R.anim.px_slide_right_to_left_out);
        setResult(Activity.RESULT_OK);
        finish();
    }

    @Override
    public void askForIssuer(final CardInfo cardInfo, final List<Issuer> issuers, final PaymentMethod paymentMethod) {
        new Constants.Activities.IssuersActivityBuilder()
            .setActivity(this)
            .setIssuers(issuers)
            .setCardInfo(cardInfo)
            .setPaymentMethod(paymentMethod)
            .startActivity();
        overridePendingTransition(R.anim.px_slide_right_to_left_in, R.anim.px_slide_right_to_left_out);
    }

    @Override
    public void finishCardStorageFlowWithSuccess() {
        CardAssociationResultSuccessActivity.startCardAssociationResultSuccessActivity(this);
        finish();
        overridePendingTransition(R.anim.px_slide_right_to_left_in, R.anim.px_slide_right_to_left_out);
    }

    @Override
    public void finishCardStorageFlowWithError(final String accessToken) {
        CardAssociationResultErrorActivity.startCardAssociationResultErrorActivity(this, accessToken);
        finish();
        overridePendingTransition(R.anim.px_slide_right_to_left_in, R.anim.px_slide_right_to_left_out);
    }

    @Override
    public void onBackPressed() {
        checkFlipCardToFront();
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void showProgress() {
        mButtonContainer.setVisibility(View.GONE);
        mInputContainer.setVisibility(View.GONE);
        mProgressLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        mButtonContainer.setVisibility(View.VISIBLE);
        mInputContainer.setVisibility(View.VISIBLE);
        mProgressLayout.setVisibility(View.GONE);
    }

    @Override
    public void setSoftInputMode() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        // because method count increase with inline declarations
        // this listener covers all on touch events.
        final int id = v.getId();
        if (id == R.id.mpsdkCardNumber) {
            onTouchEditText(mCardNumberEditText, event);
            return true;
        } else if (id == R.id.mpsdkCardholderName) {
            onTouchEditText(mCardHolderNameEditText, event);
            return true;
        } else if (id == R.id.mpsdkCardExpiryDate) {
            onTouchEditText(mCardExpiryDateEditText, event);
            return true;
        } else if (id == R.id.mpsdkCardSecurityCode) {
            onTouchEditText(mSecurityCodeEditText, event);
            return true;
        } else if (id == R.id.mpsdkCardIdentificationType) {
            if (mCurrentEditingEditText.equals(CARD_SECURITYCODE_INPUT)) {
                return false;
            }
            checkTransitionCardToId();
            openKeyboard(mIdentificationNumberEditText);
            return false;
        } else if (id == R.id.mpsdkCardIdentificationNumber) {
            onTouchEditText(mIdentificationNumberEditText, event);
            return true;
        }

        return false;
    }

    @Override
    public void onClick(final View v) {
        // because method count increase with inline declarations
        // this listener covers all on touch events.
        final int id = v.getId();
        if (id == R.id.mpsdkBankDealsText) {
            new Constants.Activities.BankDealsActivityBuilder()
                .setActivity(mActivity)
                .setBankDeals(mPresenter.getBankDealsList())
                .startActivity();
        } else if (id == R.id.mpsdkNextButton) {
            validateCurrentEditText();
        } else if (id == R.id.mpsdkBackButton && !mCurrentEditingEditText.equals(CARD_NUMBER_INPUT)) {
            checkIsEmptyOrValid();
        } else if (id == R.id.mpsdkRedErrorContainer) {
            final List<PaymentMethod> supportedPaymentMethods = mPresenter.getAllSupportedPaymentMethods();
            if (supportedPaymentMethods != null && !supportedPaymentMethods.isEmpty()) {
                startReviewPaymentMethodsActivity(supportedPaymentMethods);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}

