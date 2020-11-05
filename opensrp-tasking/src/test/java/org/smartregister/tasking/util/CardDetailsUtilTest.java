package org.smartregister.tasking.util;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.util.ReflectionHelpers;
import org.smartregister.tasking.BaseUnitTest;
import org.smartregister.tasking.R;
import org.smartregister.tasking.TaskingLibrary;
import org.smartregister.tasking.model.CardDetails;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.smartregister.tasking.util.CardDetailsUtil.formatCardDetails;
import static org.smartregister.tasking.util.CardDetailsUtil.getTranslatedBusinessStatus;
import static org.smartregister.tasking.util.CardDetailsUtil.getTranslatedIRSVerificationStatus;
import static org.smartregister.tasking.util.Constants.BusinessStatus.NOT_VISITED;

/**
 * Created by Vincent Karuri on 25/04/2019
 */
public class CardDetailsUtilTest extends BaseUnitTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private TaskingLibraryConfiguration taskingLibraryConfiguration;

    @Before
    public void setUp() throws Exception {
        taskingLibraryConfiguration = Mockito.spy(TaskingLibrary.getInstance().getTaskingLibraryConfiguration());
        ReflectionHelpers.setField(TaskingLibrary.getInstance(), "taskingLibraryConfiguration", taskingLibraryConfiguration);
    }

    @Test
    public void testCardDetailsSetsDefaultStatusColorForNullBusinessStatus() {
        CardDetails cardDetails = new CardDetails(null);
        assertEquals(cardDetails.getStatusColor().intValue(), R.color.task_not_done);
        formatCardDetails(cardDetails);
        assertEquals(cardDetails.getStatusColor().intValue(), R.color.task_not_done);
    }

    @Test
    public void testBusinessStatusIsTranslatedCorrectly(){
        getTranslatedBusinessStatus(NOT_VISITED);
        verify(taskingLibraryConfiguration).getTranslatedBusinessStatus(NOT_VISITED);
    }

    @Test
    public void testIRSVerificationStatusITranslatedCorrectly(){
        getTranslatedIRSVerificationStatus(Constants.IRSVerificationStatus.SPRAYED);

        verify(taskingLibraryConfiguration).getTranslatedIRSVerificationStatus(Constants.IRSVerificationStatus.SPRAYED);
    }

}
