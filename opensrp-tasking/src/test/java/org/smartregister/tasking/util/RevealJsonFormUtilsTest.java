package org.smartregister.tasking.util;

import android.content.Context;

import com.mapbox.geojson.Feature;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.domain.Geometry;
import org.smartregister.domain.Location;
import org.smartregister.domain.LocationProperty;
import org.smartregister.tasking.BaseUnitTest;
import org.smartregister.tasking.TaskingLibrary;
import org.smartregister.tasking.model.TaskDetails;
import org.smartregister.tasking.util.Constants.JsonForm;
import org.smartregister.util.AssetHandler;
import org.smartregister.util.JsonFormUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.vijay.jsonwizard.constants.JsonFormConstants.TEXT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.smartregister.tasking.util.Constants.LARVAL_DIPPING_EVENT;
import static org.smartregister.tasking.util.Constants.STRUCTURE;
import static org.smartregister.tasking.util.Constants.TASK_RESET_EVENT;
import static org.smartregister.tasking.util.Utils.getPropertyValue;

/**
 * Created by Vincent Karuri on 25/04/2019
 */
public class RevealJsonFormUtilsTest extends BaseUnitTest {

    private RevealJsonFormUtils revealJsonFormUtils;

    private Context context = RuntimeEnvironment.application;
    private TaskingLibraryConfiguration taskingLibraryConfiguration;

    @Before
    public void setUp() {
        revealJsonFormUtils = new RevealJsonFormUtils();
        taskingLibraryConfiguration = Mockito.spy(TaskingLibrary.getInstance().getTaskingLibraryConfiguration());
        ReflectionHelpers.setField(TaskingLibrary.getInstance(), "taskingLibraryConfiguration", taskingLibraryConfiguration);
    }

    @Test
    public void testGetFormNameShouldReturnThailandLarvalDippingFormForLarvalDippingEvent() {
        revealJsonFormUtils.getFormName(LARVAL_DIPPING_EVENT, null);

        Mockito.verify(taskingLibraryConfiguration).getFormName(LARVAL_DIPPING_EVENT, null);
    }

    @Test
    public void testPopulateField() throws JSONException {
        JSONObject form = new JSONObject(AssetHandler.readFileFromAssetsFolder(JsonForm.ADD_STRUCTURE_FORM, context));
        assertEquals("", JsonFormUtils.getFieldJSONObject(JsonFormUtils.fields(form), JsonForm.SELECTED_OPERATIONAL_AREA_NAME).get(TEXT).toString());

        revealJsonFormUtils.populateField(form, Constants.JsonForm.SELECTED_OPERATIONAL_AREA_NAME, "TLV1", TEXT);
        assertEquals("TLV1", JsonFormUtils.getFieldJSONObject(JsonFormUtils.fields(form), JsonForm.SELECTED_OPERATIONAL_AREA_NAME).get(TEXT));
    }

    @Test
    public void testgetFormJSON() throws JSONException {
        TaskDetails task = new TaskDetails("d12202fb-d347-4d7a-8859-fb370304c34c");
        task.setBusinessStatus("Not Visited");
        task.setTaskEntity("c72310fd-9c60-403e-a6f8-e38bf5d6359b");
        task.setStructureId("e5246812-f66c-41d9-8739-464f913b112d");
        task.setTaskCode("Blood Screening");
        task.setTaskStatus("READY");
        /*task.setTaskName("Yogi  Feri, 9");
        task.setTaskAction("Record\n" +"Screening");*/

        Location structure = new Location();
        structure.setId("e5246812-f66c-41d9-8739-464f913b112d");
        structure.setServerVersion(1569490867604L);
        structure.setSyncStatus("Synced");
        structure.setType("Feature");
        structure.setJurisdiction(false);

        Geometry g = new Geometry();
        g.setType(Geometry.GeometryType.MULTI_POLYGON);
        structure.setGeometry(g);

        LocationProperty lp = new LocationProperty();
        lp.setParentId("6fffaf7f-f16f-4713-a1ac-0cf6e2fe7f2a");
        HashMap<String, String> hm = new HashMap<>();
        hm.put("houseNumber", "6533");
        lp.setCustomProperties(hm);
        lp.setStatus(LocationProperty.PropertyStatus.ACTIVE);
        structure.setProperties(lp);

        JSONObject jsonObject = revealJsonFormUtils.getFormJSON(context, JsonForm.BLOOD_SCREENING_FORM, task, structure);
        assertEquals(jsonObject.getJSONObject("details").getString(Constants.Properties.FORM_VERSION), "0.0.1");
    }

    @Test
    public void testGetFormJsonFromFeature() throws JSONException {
        Feature structure = TestingUtils.getStructure();
        String expectedTaskIdentifier = getPropertyValue(structure, Constants.Properties.TASK_IDENTIFIER);
        JSONObject jsonObject = revealJsonFormUtils.getFormJSON(context, JsonForm.SPRAY_FORM, structure, Constants.BusinessStatus.SPRAYED, "John");
        assertNotNull(jsonObject);
        assertEquals(structure.id(), jsonObject.getJSONObject("details").getString(Constants.Properties.LOCATION_ID));
        assertEquals(expectedTaskIdentifier, jsonObject.getJSONObject("details").getString(Constants.Properties.TASK_IDENTIFIER));
    }

    @Test
    public void testCreateEvent() {

        String baseEntityId = UUID.randomUUID().toString();
        String locationId = UUID.randomUUID().toString();
        Map<String, String> details = new HashMap<>();
        String eventType =  TASK_RESET_EVENT;
        String entityType = STRUCTURE;

        Event actualEvent = revealJsonFormUtils.createTaskEvent(baseEntityId, locationId, details, eventType, entityType);

        assertEquals(baseEntityId, actualEvent.getBaseEntityId());
        assertEquals(locationId, actualEvent.getLocationId());
        assertEquals(eventType, actualEvent.getEventType());
        assertEquals(entityType, actualEvent.getEntityType());
        assertEquals(baseEntityId, actualEvent.getBaseEntityId());
    }

}

