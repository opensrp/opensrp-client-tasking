package org.smartregister.tasking.interactor;

import android.content.Context;

import com.mapbox.geojson.Feature;

import net.sqlcipher.Cursor;
import net.sqlcipher.MatrixCursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.reflect.Whitebox;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.domain.Event;
import org.smartregister.domain.Location;
import org.smartregister.domain.Task;
import org.smartregister.domain.db.EventClient;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.repository.StructureRepository;
import org.smartregister.sync.ClientProcessorForJava;
import org.smartregister.tasking.BaseUnitTest;
import org.smartregister.tasking.BuildConfig;
import org.smartregister.tasking.TaskingLibrary;
import org.smartregister.tasking.contract.BaseContract;
import org.smartregister.tasking.util.Constants;
import org.smartregister.tasking.util.PreferencesUtil;
import org.smartregister.tasking.util.TaskingLibraryConfiguration;
import org.smartregister.tasking.util.TestingUtils;
import org.smartregister.tasking.util.Utils;
import org.smartregister.util.AssetHandler;
import org.smartregister.util.Cache;
import org.smartregister.util.JsonFormUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.smartregister.tasking.util.Constants.DETAILS;
import static org.smartregister.tasking.util.Constants.Intervention.PAOT;
import static org.smartregister.tasking.util.Constants.JsonForm.PAOT_STATUS;
import static org.smartregister.tasking.util.Constants.Properties.APP_VERSION_NAME;
import static org.smartregister.tasking.util.Constants.Properties.LOCATION_PARENT;
import static org.smartregister.tasking.util.Constants.Properties.LOCATION_UUID;
import static org.smartregister.tasking.util.Constants.Properties.PLAN_IDENTIFIER;
import static org.smartregister.tasking.util.Constants.Properties.TASK_IDENTIFIER;
import static org.smartregister.tasking.util.Constants.REGISTER_STRUCTURE_EVENT;
import static org.smartregister.util.JsonFormUtils.VALUE;
import static org.smartregister.util.JsonFormUtils.VALUES;

/**
 * Created by samuelgithengi on 5/23/19.
 */
public class BaseInteractorTest extends BaseUnitTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private BaseContract.BasePresenter presenter;

    @Mock
    private SQLiteDatabase database;

    @Mock
    private StructureRepository structureRepository;

    @Mock
    private CommonRepository commonRepository;

    @Mock
    private ClientProcessorForJava clientProcessor;

    @Mock
    private EventClientRepository eventClientRepository;

    @Captor
    private ArgumentCaptor<CommonPersonObjectClient> clientArgumentCaptor;

    @Captor
    private ArgumentCaptor<JSONObject> eventCaptor;

    @Captor
    private ArgumentCaptor<List<EventClient>> eventClientCaptor;


    private BaseInteractor interactor;

    private Context context = RuntimeEnvironment.application;
    private TaskingLibraryConfiguration taskingLibraryConfiguration;

    @Before
    public void setUp() {
        interactor = new BaseInteractor(presenter, commonRepository);
        Whitebox.setInternalState(interactor, "structureRepository", structureRepository);
        Whitebox.setInternalState(interactor, "database", database);
        Whitebox.setInternalState(interactor, "clientProcessor", clientProcessor);
        Whitebox.setInternalState(interactor, "eventClientRepository", eventClientRepository);

        taskingLibraryConfiguration = spy(TaskingLibrary.getInstance().getTaskingLibraryConfiguration());
        ReflectionHelpers.setField(TaskingLibrary.getInstance(), "taskingLibraryConfiguration", taskingLibraryConfiguration);
    }


    @Test
    public void testFetchFamilyDetails() {
        Whitebox.setInternalState(interactor, "commonRepository", commonRepository);
        String structureId = UUID.randomUUID().toString();
        String query = "SELECT base_entity_id FROM EC_FAMILY WHERE structure_id = ? AND date_removed IS NULL";
        when(database.rawQuery(query, new String[]{structureId})).thenReturn(createFamilyCursor());
        CommonPersonObjectClient family = TestingUtils.getCommonPersonObjectClient();
        CommonPersonObject familyObject = new CommonPersonObject(family.getCaseId(),
                null, family.getDetails(), "");
        familyObject.setColumnmaps(family.getColumnmaps());
        when(commonRepository.findByBaseEntityId("69df212c-33a7-4443-a8d5-289e48d90468")).thenReturn(familyObject);
        interactor.fetchFamilyDetails(structureId);
        verify(database,timeout(ASYNC_TIMEOUT)).rawQuery(query, new String[]{structureId});
        verify(presenter, timeout(ASYNC_TIMEOUT)).onFamilyFound(clientArgumentCaptor.capture());
        assertEquals(family.entityId(), clientArgumentCaptor.getValue().entityId());
        assertEquals(family.getColumnmaps(), clientArgumentCaptor.getValue().getColumnmaps());
    }


    @Test
    public void testSaveJsonForm() throws Exception {
        String form = AssetHandler.readFileFromAssetsFolder(org.smartregister.tasking.util.Constants.JsonForm.PAOT_FORM, context);
        JSONObject formObject = new JSONObject(form);
        String structureId = UUID.randomUUID().toString();
        formObject.put("entity_id", structureId);
        JSONObject details = new JSONObject();
        String taskId = UUID.randomUUID().toString();
        details.put(TASK_IDENTIFIER, taskId);
        details.put(LOCATION_UUID, structureId);
        formObject.put(DETAILS, details);
        JsonFormUtils.getFieldJSONObject(JsonFormUtils.fields(formObject), PAOT_STATUS).put(VALUE, "Active");
        JsonFormUtils.getFieldJSONObject(JsonFormUtils.fields(formObject), "lastUpdatedDate").put(VALUE, "19-07-2019");

        String stringJson = formObject.toString();
        interactor.saveJsonForm(stringJson);

        verify(taskingLibraryConfiguration).saveJsonForm(interactor, stringJson);
    }

    @Test
    public void testSaveRegisterStructureForm() throws Exception {
        String form = AssetHandler.readFileFromAssetsFolder(org.smartregister.tasking.util.Constants.JsonForm.ADD_STRUCTURE_FORM, context);
        String planIdentifier = UUID.randomUUID().toString();
        PreferencesUtil.getInstance().setCurrentPlanId(planIdentifier);
        JSONObject formObject = new JSONObject(form);
        String locationId = UUID.randomUUID().toString();
        formObject.put("entity_id", locationId);
        JSONObject details = new JSONObject();
        details.put(LOCATION_PARENT, locationId);
        formObject.put(DETAILS, details);
        Whitebox.setInternalState(interactor, "operationalAreaId", locationId);
        String structureOb = "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[28.35228319086664,-15.421616685545176,0]},\"properties\":null}";
        JsonFormUtils.getFieldJSONObject(JsonFormUtils.fields(formObject), "structure").put(VALUE, structureOb);
        double zoomLevel = 18.2d;
        JsonFormUtils.getFieldJSONObject(JsonFormUtils.fields(formObject), Constants.ObsKey.ZOOM_LEVEL).put(VALUE, zoomLevel);

        String formObjectString = formObject.toString();
        interactor.saveJsonForm(formObjectString);

        verify(taskingLibraryConfiguration).saveJsonForm(interactor, formObjectString);
    }


    private Cursor createFamilyCursor() {
        MatrixCursor cursor = new MatrixCursor(new String[]{
                Constants.IntentKey.BASE_ENTITY_ID
        });
        cursor.addRow(new Object[]{
                "69df212c-33a7-4443-a8d5-289e48d90468"
        });
        return cursor;
    }

}
