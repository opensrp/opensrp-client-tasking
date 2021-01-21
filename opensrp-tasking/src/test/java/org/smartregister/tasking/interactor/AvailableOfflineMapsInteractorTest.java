package org.smartregister.tasking.interactor;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.util.ReflectionHelpers;
import org.smartregister.domain.Location;
import org.smartregister.tasking.BaseUnitTest;
import org.smartregister.tasking.TaskingLibrary;
import org.smartregister.tasking.contract.AvailableOfflineMapsContract;
import org.smartregister.tasking.model.OfflineMapModel;
import org.smartregister.tasking.util.TaskingLibraryConfiguration;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Created by Richard Kareko on 1/24/20.
 */

public class AvailableOfflineMapsInteractorTest extends BaseUnitTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private AvailableOfflineMapsContract.Presenter presenter;

    @Captor
    private ArgumentCaptor<List<String>> locationIdsCaptor;

    @Captor
    private ArgumentCaptor<List<OfflineMapModel>> offlineMapModelListArgumentCaptor;

    private TaskingLibraryConfiguration taskingLibraryConfiguration;

    private AvailableOfflineMapsInteractor interactor;

    private String locationId;

    @Before
    public void setUp() {
        taskingLibraryConfiguration = spy(TaskingLibrary.getInstance().getTaskingLibraryConfiguration());
        ReflectionHelpers.setField(TaskingLibrary.getInstance(), "taskingLibraryConfiguration", taskingLibraryConfiguration);
        interactor = new AvailableOfflineMapsInteractor(presenter);
        locationId = "location_1";
    }

    @Test
    public void testFetchAvailableOAsForMapDownload() {
        List<String> locationIds = Collections.singletonList(locationId);
        List<Location> locations = Collections.singletonList(initLocation());

        doReturn(locations).when(taskingLibraryConfiguration).getLocationsIdsForDownload(anyList());

        interactor.fetchAvailableOAsForMapDownLoad(locationIds);
        verify(taskingLibraryConfiguration, timeout(ASYNC_TIMEOUT)).getLocationsIdsForDownload(locationIdsCaptor.capture());
        verify(presenter, timeout(ASYNC_TIMEOUT)).onFetchAvailableOAsForMapDownLoad(offlineMapModelListArgumentCaptor.capture());
        verifyNoMoreInteractions(presenter);

        assertNotNull(offlineMapModelListArgumentCaptor.getValue());
        assertNotNull(offlineMapModelListArgumentCaptor.getValue().get(0));
        assertNotNull(offlineMapModelListArgumentCaptor.getValue().get(0).getLocation());

        Location location = offlineMapModelListArgumentCaptor.getValue().get(0).getLocation();
        assertEquals(locationId, location.getId());
        assertEquals("Polygon", location.getType());
        assertEquals(locationId, location.getId());

    }

    @Test
    public void testPopulateOfflineMapModelLost() {
        List<Location> locations = Collections.singletonList(initLocation());

        List<OfflineMapModel> offlineMapModels = interactor.populateOfflineMapModelList(locations);
        assertNotNull(offlineMapModels);
        assertNotNull(offlineMapModels.get(0));
        assertNotNull(offlineMapModels.get(0).getLocation());

        Location location = offlineMapModels.get(0).getLocation();
        assertEquals(locationId, location.getId());
        assertEquals("Polygon", location.getType());
        assertEquals(locationId, location.getId());
    }

    private Location initLocation() {
        Location location = new Location();
        location.setType("Polygon");
        location.setId(locationId);
        location.setJurisdiction(true);
        return location;
    }

}
