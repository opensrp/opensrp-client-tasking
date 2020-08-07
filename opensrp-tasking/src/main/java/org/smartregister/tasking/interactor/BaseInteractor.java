package org.smartregister.tasking.interactor;

import android.content.Context;

import androidx.annotation.VisibleForTesting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.mapbox.geojson.Feature;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.domain.Client;
import org.smartregister.domain.Location;
import org.smartregister.domain.LocationProperty;
import org.smartregister.domain.Obs;
import org.smartregister.domain.Task;
import org.smartregister.domain.db.EventClient;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.repository.StructureRepository;
import org.smartregister.repository.TaskRepository;
import org.smartregister.sync.ClientProcessorForJava;
import org.smartregister.tasking.BuildConfig;
import org.smartregister.tasking.TaskingLibrary;
import org.smartregister.tasking.contract.BaseContract;
import org.smartregister.tasking.contract.BaseContract.BasePresenter;
import org.smartregister.tasking.util.Constants;
import org.smartregister.tasking.util.Constants.Properties;
import org.smartregister.tasking.util.Utils;
import org.smartregister.util.AppExecutors;
import org.smartregister.util.DateTimeTypeConverter;
import org.smartregister.util.JsonFormUtils;
import org.smartregister.util.PropertiesConverter;
import org.smartregister.view.activity.DrishtiApplication;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import timber.log.Timber;

import static com.cocoahero.android.geojson.Geometry.JSON_COORDINATES;
import static org.smartregister.tasking.util.Constants.DETAILS;
import static org.smartregister.tasking.util.Constants.JsonForm.FAMILY_MEMBER;
import static org.smartregister.tasking.util.Constants.JsonForm.LOCATION_COMPONENT_ACTIVE;
import static org.smartregister.tasking.util.Constants.JsonForm.PHYSICAL_TYPE;
import static org.smartregister.tasking.util.Constants.JsonForm.STRUCTURE_NAME;
import static org.smartregister.tasking.util.Constants.JsonForm.STRUCTURE_TYPE;
import static org.smartregister.tasking.util.Constants.METADATA;
import static org.smartregister.tasking.util.Constants.REGISTER_STRUCTURE_EVENT;
import static org.smartregister.tasking.util.Constants.STRUCTURE;
import static org.smartregister.util.JsonFormUtils.ENTITY_ID;
import static org.smartregister.util.JsonFormUtils.getString;


/**
 * Created by samuelgithengi on 3/25/19.
 */
public class BaseInteractor implements BaseContract.BaseInteractor {

    public static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter())
            .registerTypeAdapter(LocationProperty.class, new PropertiesConverter()).create();

    private TaskingLibrary taskingLibrary;

    protected TaskRepository taskRepository;

    protected StructureRepository structureRepository;

    protected BasePresenter presenterCallBack;

    protected String operationalAreaId;

    protected AppExecutors appExecutors;

    protected AllSharedPreferences sharedPreferences;

    protected EventClientRepository eventClientRepository;

    protected ClientProcessorForJava clientProcessor;

    //private TaskUtils taskUtils;

    private SQLiteDatabase database;

    private CommonRepository commonRepository;

    //private PreferencesUtil prefsUtil;

    public BaseInteractor(BaseContract.BasePresenter presenterCallBack) {
        taskingLibrary = TaskingLibrary.getInstance();
        this.presenterCallBack = presenterCallBack;
        appExecutors = taskingLibrary.getAppExecutors();
        taskRepository = taskingLibrary.getTaskRepository();
        structureRepository = taskingLibrary.getStructureRepository();
        eventClientRepository = taskingLibrary.getEventClientRepository();
        clientProcessor = ClientProcessorForJava.getInstance(DrishtiApplication.getInstance());
        sharedPreferences = taskingLibrary.getAllSharedPreferences();
        //taskUtils = TaskUtils.getInstance();
        database = taskingLibrary.getRepository().getReadableDatabase();
        //prefsUtil = PreferencesUtil.getInstance();
    }

    @VisibleForTesting
    public BaseInteractor(BaseContract.BasePresenter presenterCallBack, CommonRepository commonRepository) {
        this(presenterCallBack);
        this.commonRepository = commonRepository;
    }

    @Override
    public void saveJsonForm(String json) {
        /*try {
            JSONObject jsonForm = new JSONObject(json);
            String encounterType = jsonForm.getString(ENCOUNTER_TYPE);
            boolean refreshMapOnEventSaved = true;
            switch (encounterType) {
                case REGISTER_STRUCTURE_EVENT:
                    saveRegisterStructureForm(jsonForm);
                    break;
                case EventType.MDA_DISPENSE:
                case BLOOD_SCREENING_EVENT:
                case EventType.MDA_ADHERENCE:
                    saveMemberForm(jsonForm, encounterType, BLOOD_SCREENING);
                    break;

                case CASE_CONFIRMATION_EVENT:
                    saveCaseConfirmation(jsonForm, encounterType);
                    break;
                default:
                    saveLocationInterventionForm(jsonForm);
                    if (!encounterType.equals(BEDNET_DISTRIBUTION_EVENT) && !encounterType.equals(EventType.IRS_VERIFICATION)) {
                        refreshMapOnEventSaved = false;
                    }
                    break;
            }
            taskingLibrary.setRefreshMapOnEventSaved(refreshMapOnEventSaved);
        } catch (Exception e) {
            Timber.e(e, "Error saving Json Form data");
        }*/
        TaskingLibrary.getInstance().getTaskingLibraryConfiguration().saveJsonForm(json);
    }

    private org.smartregister.domain.Event saveEvent(JSONObject jsonForm, String encounterType, String bindType) throws JSONException {
        String entityId = getString(jsonForm, ENTITY_ID);
        JSONArray fields = JsonFormUtils.fields(jsonForm);
        JSONObject metadata = JsonFormUtils.getJSONObject(jsonForm, METADATA);
        Event event = JsonFormUtils.createEvent(fields, metadata, Utils.getFormTag(), entityId, encounterType, bindType);
        JSONObject eventJson = new JSONObject(gson.toJson(event));
        eventJson.put(DETAILS, JsonFormUtils.getJSONObject(jsonForm, DETAILS));
        eventClientRepository.addEvent(entityId, eventJson);
        return gson.fromJson(eventJson.toString(), org.smartregister.domain.Event.class);
    }

    private void saveLocationInterventionForm(JSONObject jsonForm) {
        /*String encounterType = null;
        String interventionType = null;
        try {
            encounterType = jsonForm.getString(ENCOUNTER_TYPE);
            if (encounterType.equals(SPRAY_EVENT)) {
                interventionType = IRS;
            } else if (encounterType.equals(MOSQUITO_COLLECTION_EVENT)) {
                interventionType = MOSQUITO_COLLECTION;
            } else if (encounterType.equals(LARVAL_DIPPING_EVENT)) {
                interventionType = LARVAL_DIPPING;
            } else if (encounterType.equals(BEDNET_DISTRIBUTION_EVENT)) {
                interventionType = BEDNET_DISTRIBUTION;
            } else if (encounterType.equals(BEHAVIOUR_CHANGE_COMMUNICATION)) {
                interventionType = BCC;
            } else if (encounterType.equals(EventType.PAOT_EVENT)) {
                interventionType = PAOT;
            } else if (encounterType.equals(EventType.MDA_DISPENSE)) {
                interventionType = Intervention.MDA_DISPENSE;
            } else if (encounterType.equals(EventType.MDA_ADHERENCE)) {
                interventionType = Intervention.MDA_ADHERENCE;
            } else if (encounterType.equals(EventType.IRS_VERIFICATION)) {
                interventionType = Intervention.IRS_VERIFICATION;
            }
        } catch (JSONException e) {
            Timber.e(e);
        }

        final String finalInterventionType = interventionType;
        final String finalEncounterType = encounterType;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    org.smartregister.domain.Event event = saveEvent(jsonForm, finalEncounterType, STRUCTURE);
                    clientProcessor.processClient(Collections.singletonList(new EventClient(event, null)), true);
                    appExecutors.mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            String businessStatus = clientProcessor.calculateBusinessStatus(event);
                            String taskID = event.getDetails().get(Properties.TASK_IDENTIFIER);
                            presenterCallBack.onFormSaved(event.getBaseEntityId(), taskID, Task.TaskStatus.COMPLETED, businessStatus, finalInterventionType);
                        }
                    });
                } catch (JSONException e) {
                    Timber.e(e, "Error saving saving Form ");
                    presenterCallBack.onFormSaveFailure(finalEncounterType);
                }
            }
        };

        appExecutors.diskIO().execute(runnable); */
        TaskingLibrary.getInstance().getTaskingLibraryConfiguration().saveLocationInterventionForm(jsonForm);
    }

    private void saveRegisterStructureForm(JSONObject jsonForm) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    jsonForm.put(ENTITY_ID, UUID.randomUUID().toString());
                    JSONObject eventDetails = new JSONObject();
                    eventDetails.put(Properties.APP_VERSION_NAME, BuildConfig.VERSION_NAME);
                    eventDetails.put(Properties.LOCATION_PARENT, operationalAreaId);
                    String planIdentifier = TaskingLibrary.getInstance().getTaskingLibraryConfiguration().getCurrentPlanId();// PreferencesUtil.getInstance().getCurrentPlanId();
                    eventDetails.put(Properties.PLAN_IDENTIFIER, planIdentifier);
                    jsonForm.put(DETAILS, eventDetails);
                    org.smartregister.domain.Event event = saveEvent(jsonForm, REGISTER_STRUCTURE_EVENT, STRUCTURE);
                    com.cocoahero.android.geojson.Feature feature = new com.cocoahero.android.geojson.Feature(new JSONObject(event.findObs(null, false, "structure").getValue().toString()));
                    Date now = new Date();
                    Location structure = new Location();
                    structure.setId(event.getBaseEntityId());
                    structure.setType(feature.getType());
                    org.smartregister.domain.Geometry geometry = new org.smartregister.domain.Geometry();
                    geometry.setType(org.smartregister.domain.Geometry.GeometryType.valueOf(feature.getGeometry().getType().toUpperCase()));
                    JsonArray coordinates = new JsonArray();
                    JSONArray featureCoordinates = feature.getGeometry().toJSON().getJSONArray(JSON_COORDINATES);
                    coordinates.add(Double.parseDouble(featureCoordinates.get(0).toString()));
                    coordinates.add(Double.parseDouble(featureCoordinates.get(1).toString()));
                    geometry.setCoordinates(coordinates);
                    structure.setGeometry(geometry);
                    LocationProperty properties = new LocationProperty();
                    String structureType = event.findObs(null, false, STRUCTURE_TYPE).getValue().toString();
                    properties.setType(structureType);
                    properties.setEffectiveStartDate(now);
                    properties.setParentId(operationalAreaId);
                    properties.setStatus(LocationProperty.PropertyStatus.PENDING_REVIEW);
                    properties.setUid(UUID.randomUUID().toString());
                    Obs structureNameObs = event.findObs(null, false, STRUCTURE_NAME);
                    if (structureNameObs != null && structureNameObs.getValue() != null) {
                        properties.setName(structureNameObs.getValue().toString());
                    }
                    Obs physicalTypeObs = event.findObs(null, false, PHYSICAL_TYPE);
                    if (physicalTypeObs != null && physicalTypeObs.getValue() != null) {
                        Map<String, String> customProperties = new HashMap<>();
                        customProperties.put(PHYSICAL_TYPE, physicalTypeObs.getValue().toString());
                        properties.setCustomProperties(customProperties);
                    }
                    structure.setProperties(properties);
                    structure.setSyncStatus(BaseRepository.TYPE_Created);
                    structureRepository.addOrUpdate(structure);
                    taskingLibrary.getTaskingLibraryConfiguration().setSynced(false);
                    Context applicationContext = DrishtiApplication.getInstance().getApplicationContext();
                    clientProcessor.processClient(Collections.singletonList(new EventClient(event, null)), true);
                    Task task = TaskingLibrary.getInstance().getTaskingLibraryConfiguration().generateTaskFromStructureType(applicationContext, structure.getId(), structureType);
                    /*Task task = null;
                    if (Constants.StructureType.RESIDENTIAL.equals(structureType) && Utils.isFocusInvestigationOrMDA()) {
                        task = taskUtils.generateRegisterFamilyTask(applicationContext, structure.getId());
                    } else {
                        if (Constants.StructureType.RESIDENTIAL.equals(structureType)) {
                            task = taskUtils.generateTask(applicationContext, structure.getId(), structure.getId(),
                                    Constants.BusinessStatus.NOT_VISITED, Constants.Intervention.IRS, R.string.irs_task_description);
                        } else if (Constants.StructureType.MOSQUITO_COLLECTION_POINT.equals(structureType)) {
                            task = taskUtils.generateTask(applicationContext, structure.getId(), structure.getId(),
                                    Constants.BusinessStatus.NOT_VISITED, Constants.Intervention.MOSQUITO_COLLECTION, R.string.mosquito_collection_task_description);
                        } else if (Constants.StructureType.LARVAL_BREEDING_SITE.equals(structureType)) {
                            task = taskUtils.generateTask(applicationContext, structure.getId(), structure.getId(),
                                    Constants.BusinessStatus.NOT_VISITED, Constants.Intervention.LARVAL_DIPPING, R.string.larval_dipping_task_description);
                        } else if (Constants.StructureType.POTENTIAL_AREA_OF_TRANSMISSION.equals(structureType)) {
                            task = taskUtils.generateTask(applicationContext, structure.getId(), structure.getId(),
                                    Constants.BusinessStatus.NOT_VISITED, Constants.Intervention.PAOT, R.string.poat_task_description);
                        }
                    }*/
                    //TODO add a way of fetching generated task
                    Task finalTask = task;
                    appExecutors.mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            Map<String, String> taskProperties = new HashMap<>();
                            if (finalTask != null) {

                                taskProperties.put(Properties.TASK_IDENTIFIER, finalTask.getIdentifier());
                                taskProperties.put(Properties.TASK_BUSINESS_STATUS, finalTask.getBusinessStatus());
                                taskProperties.put(Properties.TASK_STATUS, finalTask.getStatus().name());
                                taskProperties.put(Properties.TASK_CODE, finalTask.getCode());
                            }
                            taskProperties.put(Properties.LOCATION_UUID, structure.getProperties().getUid());
                            taskProperties.put(Properties.LOCATION_VERSION, structure.getProperties().getVersion() + "");
                            taskProperties.put(Properties.LOCATION_TYPE, structure.getProperties().getType());
                            structure.getProperties().setCustomProperties(taskProperties);


                            Obs myLocationActiveObs = event.findObs(null, false, LOCATION_COMPONENT_ACTIVE);

                            boolean myLocationActive = myLocationActiveObs != null && Boolean.valueOf(myLocationActiveObs.getValue().toString());
                            taskingLibrary.getTaskingLibraryConfiguration().setMyLocationComponentEnabled(myLocationActive);


                            Obs zoomObs = event.findObs(null, false, Constants.ObsKey.ZOOM_LEVEL);
                            double zoomLevel = Double.parseDouble(zoomObs.getValue().toString());

                            presenterCallBack.onStructureAdded(Feature.fromJson(gson.toJson(structure)), featureCoordinates, zoomLevel);
                        }
                    });
                } catch (Exception e) {
                    Timber.e(e, "Error saving new Structure");
                    presenterCallBack.onFormSaveFailure(REGISTER_STRUCTURE_EVENT);
                }
            }
        };

        appExecutors.diskIO().execute(runnable);
    }

    private void saveMemberForm(JSONObject jsonForm, String eventType, String intervention) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    org.smartregister.domain.Event event = saveEvent(jsonForm, eventType, FAMILY_MEMBER);
                    Client client = eventClientRepository.fetchClientByBaseEntityId(event.getBaseEntityId());
                    clientProcessor.processClient(Collections.singletonList(new EventClient(event, client)), true);
                    appExecutors.mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            String businessStatus = TaskingLibrary.getInstance().getTaskingLibraryConfiguration().calculateBusinessStatus(event);
                            String taskID = event.getDetails().get(Properties.TASK_IDENTIFIER);
                            presenterCallBack.onFormSaved(event.getBaseEntityId(), taskID, Task.TaskStatus.COMPLETED, businessStatus, intervention);
                        }
                    });
                } catch (Exception e) {
                    Timber.e("Error saving member event form");
                }
            }
        };
        appExecutors.diskIO().execute(runnable);
    }

    private void saveCaseConfirmation(JSONObject jsonForm, String eventType) {
        /*appExecutors.diskIO().execute(() -> {
            try {
                String baseEntityId = JsonFormUtils.getFieldValue(JsonFormUtils.fields(jsonForm), Constants.JsonForm.FAMILY_MEMBER);
                jsonForm.put(ENTITY_ID, baseEntityId);
                org.smartregister.domain.Event event = saveEvent(jsonForm, eventType, CASE_CONFIRMATION);
                Client client = eventClientRepository.fetchClientByBaseEntityId(event.getBaseEntityId());
                String taskID = event.getDetails().get(Properties.TASK_IDENTIFIER);
                String businessStatus = clientProcessor.calculateBusinessStatus(event);
                Task task = taskRepository.getTaskByIdentifier(taskID);
                task.setForEntity(baseEntityId);
                task.setBusinessStatus(businessStatus);
                task.setStatus(Task.TaskStatus.COMPLETED);
                task.setSyncStatus(BaseRepository.TYPE_Created);
                taskRepository.addOrUpdate(task);
                Set<Task> removedTasks = new HashSet<>();
                for (Task bloodScreeningTask : taskRepository.getTasksByEntityAndCode(prefsUtil.getCurrentPlanId(),
                        Utils.getOperationalAreaLocation(prefsUtil.getCurrentOperationalArea()).getId(), baseEntityId, BLOOD_SCREENING)) {
                    bloodScreeningTask.setStatus(Task.TaskStatus.CANCELLED);
                    bloodScreeningTask.setSyncStatus(BaseRepository.TYPE_Created);
                    taskRepository.addOrUpdate(bloodScreeningTask);
                    removedTasks.add(bloodScreeningTask);
                }
                taskingLibrary.setSynced(false);
                clientProcessor.processClient(Collections.singletonList(new EventClient(event, client)), true);
                appExecutors.mainThread().execute(() -> {
                    ((StructureTasksContract.Presenter) presenterCallBack).onIndexConfirmationFormSaved(taskID, Task.TaskStatus.COMPLETED, businessStatus, removedTasks);
                });
            } catch (Exception e) {
                Timber.e("Error saving case confirmation data");
            }
        });*/
        TaskingLibrary.getInstance().getTaskingLibraryConfiguration().saveCaseConfirmation(jsonForm, eventType);
    }

    protected String getMemberTasksSelect(String mainCondition, String[] memberColumns) {
        SmartRegisterQueryBuilder queryBuilder = new SmartRegisterQueryBuilder();
        queryBuilder.selectInitiateMainTable(Constants.DatabaseKeys.STRUCTURES_TABLE, memberColumns, Constants.DatabaseKeys.ID);
        queryBuilder.customJoin(String.format(" JOIN %s ON %s.%s = %s.%s ",
                Constants.TABLE_NAME.FAMILY_MEMBER, Constants.TABLE_NAME.FAMILY_MEMBER, Constants.DatabaseKeys.STRUCTURE_ID, Constants.DatabaseKeys.STRUCTURES_TABLE, Constants.DatabaseKeys.ID));
        queryBuilder.customJoin(String.format(" JOIN %s ON %s.%s = %s.%s ",
                Constants.Tables.TASK_TABLE, Constants.Tables.TASK_TABLE, Constants.DatabaseKeys.FOR, Constants.TABLE_NAME.FAMILY_MEMBER, Constants.DatabaseKeys.BASE_ENTITY_ID));
        return queryBuilder.mainCondition(mainCondition);
    }

    public void fetchFamilyDetails(String structureId) {
        appExecutors.diskIO().execute(() -> {
            Cursor cursor = null;
            CommonPersonObjectClient family = null;
            try {
                cursor = database.rawQuery(String.format("SELECT %s FROM %S WHERE %s = ? AND %s IS NULL",
                        Constants.IntentKey.BASE_ENTITY_ID, Constants.TABLE_NAME.FAMILY, Constants.DatabaseKeys.STRUCTURE_ID, Constants.DbKey.DATE_REMOVED), new String[]{structureId});
                if (cursor.moveToNext()) {
                    String baseEntityId = cursor.getString(0);
                    setCommonRepository();
                    final CommonPersonObject personObject = commonRepository.findByBaseEntityId(baseEntityId);
                    family = new CommonPersonObjectClient(personObject.getCaseId(),
                            personObject.getDetails(), "");
                    family.setColumnmaps(personObject.getColumnmaps());
                }
            } catch (Exception e) {
                Timber.e(e);
            } finally {
                if (cursor != null)
                    cursor.close();
            }

            CommonPersonObjectClient finalFamily = family;
            appExecutors.mainThread().execute(() -> {
                presenterCallBack.onFamilyFound(finalFamily);
            });
        });
    }

    public SQLiteDatabase getDatabase() {
        return database;
    }

    public void setCommonRepository() {
        if (commonRepository == null) {
            commonRepository = CoreLibrary.getInstance().context().commonrepository(TaskingLibrary.getInstance().getTaskingLibraryConfiguration().familyRegisterTableName());
        }
    }
}
