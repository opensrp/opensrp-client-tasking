package org.smartregister.tasking.util;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.smartregister.domain.Client;
import org.smartregister.domain.Location;
import org.smartregister.domain.Task;
import org.smartregister.tasking.interactor.BaseInteractor;
import org.smartregister.tasking.model.StructureDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.smartregister.tasking.util.TaskingConstants.BusinessStatus.NOT_ELIGIBLE;
import static org.smartregister.tasking.util.TaskingConstants.Properties.TASK_BUSINESS_STATUS;
import static org.smartregister.tasking.util.TaskingConstants.Properties.TASK_IDENTIFIER;
import static org.smartregister.tasking.util.TaskingConstants.Properties.TASK_STATUS;

/**
 * Created by samuelgithengi on 1/7/19.
 */
public class GeoJsonUtils {

    private static final String MDA_DISPENSE_TASK_COUNT = "mda_dispense_task_count";

    public String getGeoJsonFromStructuresAndTasks(List<Location> structures, Map<String, Set<Task>> tasks,
                                                   String indexCase, Map<String, StructureDetails> structureNames) {
        for (Location structure : structures) {
            Set<Task> taskSet = tasks.get(structure.getId());
            HashMap<String, String> taskProperties = new HashMap<>();

            StringBuilder interventionList = new StringBuilder();

            Map<String, Integer> mdaStatusMap = new HashMap<>();
            mdaStatusMap.put(TaskingConstants.BusinessStatus.FULLY_RECEIVED, 0);
            mdaStatusMap.put(TaskingConstants.BusinessStatus.NONE_RECEIVED, 0);
            mdaStatusMap.put(NOT_ELIGIBLE, 0);
            mdaStatusMap.put(MDA_DISPENSE_TASK_COUNT, 0);
            StateWrapper state = new StateWrapper();
            if (taskSet == null)
                continue;
            for (Task task : taskSet) {
                calculateState(task, state, mdaStatusMap);

                taskProperties = new HashMap<>();
                taskProperties.put(TASK_IDENTIFIER, task.getIdentifier());

//                if (BuildConfig.BUILD_COUNTRY == Country.ZAMBIA && PARTIALLY_SPRAYED.equals(task.getBusinessStatus())) { // Set here for non residential structures
//                    taskProperties.put(TASK_BUSINESS_STATUS, SPRAYED);
//                } else {
//                    taskProperties.put(TASK_BUSINESS_STATUS, task.getBusinessStatus());
//                }
                taskProperties.put(TASK_BUSINESS_STATUS, task.getBusinessStatus());

                taskProperties.put(TaskingConstants.Properties.FEATURE_SELECT_TASK_BUSINESS_STATUS, task.getBusinessStatus()); // used to determine action to take when a feature is selected
                taskProperties.put(TASK_STATUS, task.getStatus().name());
                taskProperties.put(TaskingConstants.Properties.TASK_CODE, task.getCode());

                if (indexCase != null && structure.getId().equals(indexCase)) {
                    taskProperties.put(TaskingConstants.GeoJSON.IS_INDEX_CASE, Boolean.TRUE.toString());
                } else {
                    taskProperties.put(TaskingConstants.GeoJSON.IS_INDEX_CASE, Boolean.FALSE.toString());
                }

                taskProperties.put(TaskingConstants.Properties.LOCATION_UUID, structure.getProperties().getUid());
                taskProperties.put(TaskingConstants.Properties.LOCATION_VERSION, structure.getProperties().getVersion() + "");
                taskProperties.put(TaskingConstants.Properties.LOCATION_TYPE, structure.getProperties().getType());
                interventionList.append(task.getCode());
                interventionList.append("~");

            }

            populateBusinessStatus(taskProperties, mdaStatusMap, state);

            taskProperties.put(TaskingConstants.Properties.TASK_CODE_LIST, interventionList.toString());
            if (structureNames.get(structure.getId()) != null) {
                taskProperties.put(Constants.DatabaseKeys.STRUCTURE_NAME, structureNames.get(structure.getId()).getStructureName());
                taskProperties.put(TaskingConstants.Properties.FAMILY_MEMBER_NAMES, structureNames.get(structure.getId()).getFamilyMembersNames());
            }
            structure.getProperties().setCustomProperties(taskProperties);

        }
        return BaseInteractor.gson.toJson(structures);
    }

    public String getGeoJsonFromClientLocationsAndTasks(List<Location> structures, Map<String, Set<Task>> personTasks, HashMap<String, Client> locationToClientEntityIdMap) {
        ArrayList<Location> locationsWithTasks = new ArrayList<>();

        HashSet<Task.TaskStatus> invalidTaskStatus = new HashSet<>();
        invalidTaskStatus.add(Task.TaskStatus.ARCHIVED);
        invalidTaskStatus.add(Task.TaskStatus.COMPLETED);
        invalidTaskStatus.add(Task.TaskStatus.FAILED);

        for (Location structure : structures) {
            Client client = locationToClientEntityIdMap.get(structure.getProperties().getUid());

            if (client == null || TextUtils.isEmpty(client.getBaseEntityId())) {
                continue;
            }

            Set<Task> taskSet = personTasks.get(client.getBaseEntityId());

            if (taskSet == null) {
                continue;
            }

            Map<String, String> locationCustomProperties = structure.getProperties().getCustomProperties();
            boolean isLocationValid = false;

            // TODO: Ask PM what happens when we have multiple tasks on one family
            for (Task task : taskSet) {

                // TODO: Move this to the SQLite query to reduce the performance hit here
                // Ignore tasks which are completed
                if (invalidTaskStatus.contains(task.getStatus())) {
                    continue;
                }

                locationCustomProperties.put(TASK_IDENTIFIER, task.getIdentifier());
                locationCustomProperties.put(TASK_BUSINESS_STATUS, task.getBusinessStatus());
                locationCustomProperties.put(TaskingConstants.Properties.FEATURE_SELECT_TASK_BUSINESS_STATUS, task.getBusinessStatus()); // used to determine action to take when a feature is selected
                locationCustomProperties.put(TASK_STATUS, task.getStatus().name());
                locationCustomProperties.put(TaskingConstants.Properties.TASK_CODE, task.getCode());
                locationCustomProperties.put(TaskingConstants.Properties.TASK_PRIORITY, String.valueOf(task.getPriority()));

                locationCustomProperties.put(TaskingConstants.Properties.LOCATION_UUID, structure.getProperties().getUid());
                locationCustomProperties.put(TaskingConstants.Properties.LOCATION_VERSION, structure.getProperties().getVersion() + "");
                locationCustomProperties.put(TaskingConstants.Properties.LOCATION_TYPE, structure.getProperties().getType());

                String clientName = client.getFirstName() + " " + client.getLastName();
                locationCustomProperties.put(Constants.DatabaseKeys.STRUCTURE_NAME, clientName);
                locationCustomProperties.put(TaskingConstants.Properties.FAMILY_MEMBER_NAMES, clientName);
                isLocationValid = true;
            }

            structure.getProperties().setCustomProperties(locationCustomProperties);

            if (isLocationValid) {
                locationsWithTasks.add(structure);
            }
        }

        return BaseInteractor.gson.toJson(locationsWithTasks);
    }

    public void calculateState(Task task, StateWrapper state, @NonNull Map<String, Integer> mdaStatusMap) {
//        if (Utils.isResidentialStructure(task.getCode())) {
//            switch (task.getCode()) {
//                case TaskingConstants.Intervention.REGISTER_FAMILY:
//                    state.familyRegTaskExists = true;
//                    state.familyRegistered = COMPLETE.equals(task.getBusinessStatus());
//                    state.ineligibleForFamReg = NOT_ELIGIBLE.equals((task.getBusinessStatus()));
//                    break;
//                case BEDNET_DISTRIBUTION:
//                    state.bednetDistributed = COMPLETE.equals(task.getBusinessStatus()) || NOT_ELIGIBLE.equals(task.getBusinessStatus());
//                    break;
//                case BLOOD_SCREENING:
//                    if (!state.bloodScreeningDone) {
//                        state.bloodScreeningDone = COMPLETE.equals(task.getBusinessStatus()) || NOT_ELIGIBLE.equals(task.getBusinessStatus());
//                    }
//                    state.bloodScreeningExists = true;
//                    break;
//                case CASE_CONFIRMATION:
//                    state.caseConfirmed = COMPLETE.equals(task.getBusinessStatus());
//                    break;
//                case MDA_ADHERENCE:
//                    state.mdaAdhered = COMPLETE.equals(task.getBusinessStatus()) || NOT_ELIGIBLE.equals(task.getBusinessStatus());
//                    break;
//                case MDA_DISPENSE:
//                    populateMDAStatus(task, mdaStatusMap);
//                    break;
//                default:
//                    break;
//            }
//        }
    }


    public void populateBusinessStatus(HashMap<String, String> taskProperties, Map<String, Integer> mdaStatusMap, StateWrapper state) {
        // The assumption is that a register structure task always exists if the structure has
        // atleast one bednet distribution or blood screening task
//        if (Utils.isResidentialStructure(taskProperties.get(TASK_CODE))) {
//
//            boolean familyRegTaskMissingOrFamilyRegComplete = state.familyRegistered || !state.familyRegTaskExists;
//
//            if (Utils.isFocusInvestigation()) {
//
//                if (familyRegTaskMissingOrFamilyRegComplete &&
//                        state.bednetDistributed && state.bloodScreeningDone) {
//                    taskProperties.put(TASK_BUSINESS_STATUS, COMPLETE);
//                } else if (familyRegTaskMissingOrFamilyRegComplete &&
//                        !state.bednetDistributed && (!state.bloodScreeningDone || (!state.bloodScreeningExists && !state.caseConfirmed))) {
//                    taskProperties.put(TASK_BUSINESS_STATUS, FAMILY_REGISTERED);
//                } else if (state.bednetDistributed && familyRegTaskMissingOrFamilyRegComplete) {
//                    taskProperties.put(TASK_BUSINESS_STATUS, BEDNET_DISTRIBUTED);
//                } else if (state.bloodScreeningDone) {
//                    taskProperties.put(TASK_BUSINESS_STATUS, BLOOD_SCREENING_COMPLETE);
//                } else if (state.ineligibleForFamReg) {
//                    taskProperties.put(TASK_BUSINESS_STATUS, NOT_ELIGIBLE);
//                } else {
//                    taskProperties.put(TASK_BUSINESS_STATUS, NOT_VISITED);
//                }
//
//            } else if (Utils.isMDA()) {
//
//
//                state.fullyReceived = (mdaStatusMap.get(FULLY_RECEIVED).equals(mdaStatusMap.get(MDA_DISPENSE_TASK_COUNT)));
//                state.nonReceived = (mdaStatusMap.get(NONE_RECEIVED).equals(mdaStatusMap.get(MDA_DISPENSE_TASK_COUNT)));
//                state.nonEligible = (mdaStatusMap.get(NOT_ELIGIBLE).equals(mdaStatusMap.get(MDA_DISPENSE_TASK_COUNT)));
//                state.partiallyReceived = (!state.fullyReceived && (mdaStatusMap.get(FULLY_RECEIVED) > 0));
//
//                if (familyRegTaskMissingOrFamilyRegComplete) {
//                    if (state.mdaAdhered) {
//                        taskProperties.put(TASK_BUSINESS_STATUS, ADHERENCE_VISIT_DONE);
//                    } else if (state.fullyReceived) {
//                        taskProperties.put(TASK_BUSINESS_STATUS, FULLY_RECEIVED);
//                    } else if (state.partiallyReceived) {
//                        taskProperties.put(TASK_BUSINESS_STATUS, PARTIALLY_RECEIVED);
//                    } else if (state.nonReceived) {
//                        taskProperties.put(TASK_BUSINESS_STATUS, NONE_RECEIVED);
//                    } else if (state.nonEligible) {
//                        taskProperties.put(TASK_BUSINESS_STATUS, NOT_ELIGIBLE);
//                    } else {
//                        taskProperties.put(TASK_BUSINESS_STATUS, FAMILY_REGISTERED);
//                    }
//                } else {
//                    taskProperties.put(TASK_BUSINESS_STATUS, NOT_VISITED);
//                }
//
//            }
//
//        }
    }

    public static class StateWrapper {
        private boolean familyRegistered = false;
        private boolean bednetDistributed = false;
        private boolean bloodScreeningDone = false;
        private boolean familyRegTaskExists = false;
        private boolean caseConfirmed = false;
        private boolean mdaAdhered = false;
        private boolean fullyReceived;
        private boolean nonReceived;
        private boolean nonEligible;
        private boolean partiallyReceived;
        private boolean bloodScreeningExists = false;
        private boolean ineligibleForFamReg = false;
    }
}
