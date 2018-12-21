package com.mercadopago.android.px.internal.datasource;

import com.mercadopago.android.px.model.CustomSearchItem;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationSolverTest {

    private static final String ACCOUNT_MONEY_SAMPLE_ID = "account_money";
    private static final String CARD_SAMPLE_ID = "1234";

    private static final String HASH_SAMPLE_ACCOUNT_MONEY_CONFIGURATION = "HASH_ACCOUNT_MONEY_CONFIGURATION";
    private static final String HASH_SAMPLE_SAVED_CARD_CONFIGURATION = "HASH_SAVED_CARD_CONFIGURATION";
    private static final String HASH_SAMPLE_GENERAL_CONFIGURATION = "HASH_GENERAL_CONFIGURATION";

    private ConfigurationSolverImpl discountConfigurationSolver;
    private List<CustomSearchItem> customSearchItems;

    @Mock private CustomSearchItem accountMoneyCustomSearchItem;
    @Mock private CustomSearchItem cardCustomSearchItem;

    @Before
    public void setUp() {
        customSearchItems = new ArrayList<>();
        customSearchItems.add(accountMoneyCustomSearchItem);
        customSearchItems.add(cardCustomSearchItem);

        discountConfigurationSolver =
            new ConfigurationSolverImpl(HASH_SAMPLE_GENERAL_CONFIGURATION, customSearchItems);

        when(accountMoneyCustomSearchItem.getId()).thenReturn(ACCOUNT_MONEY_SAMPLE_ID);
        when(accountMoneyCustomSearchItem.getSelectedAmountConfiguration())
            .thenReturn(HASH_SAMPLE_ACCOUNT_MONEY_CONFIGURATION);

        when(cardCustomSearchItem.getId()).thenReturn(CARD_SAMPLE_ID);
        when(cardCustomSearchItem.getSelectedAmountConfiguration()).thenReturn(HASH_SAMPLE_SAVED_CARD_CONFIGURATION);
    }

    @Test
    public void whenHasConfigurationByAccountMoneyIdThenReturnAccountMoneyConfigurationHash() {
        assertEquals(HASH_SAMPLE_ACCOUNT_MONEY_CONFIGURATION,
            discountConfigurationSolver.getConfigurationHashFor(ACCOUNT_MONEY_SAMPLE_ID));
    }

    @Test
    public void whenHasConfigurationByCardIdIdThenReturnCardConfigurationHash() {
        assertEquals(HASH_SAMPLE_SAVED_CARD_CONFIGURATION,
            discountConfigurationSolver.getConfigurationHashFor(CARD_SAMPLE_ID));
    }

    @Test
    public void whenIdIsNullThenReturnGeneralConfiguration() {
        assertEquals(HASH_SAMPLE_GENERAL_CONFIGURATION, discountConfigurationSolver.getGenericConfigurationHash());
    }

    @Test
    public void whenIdIsNullAndHasNotGeneralConfigurationThenReturnEmptyHash() {
        discountConfigurationSolver = new ConfigurationSolverImpl("", customSearchItems);
        assertEquals("", discountConfigurationSolver.getGenericConfigurationHash());
    }

    @Test
    public void whenHasNotConfigurationByIdThenReturnEmptyConfiguration() {
        assertEquals("", discountConfigurationSolver.getConfigurationHashFor("5678"));
    }
}
