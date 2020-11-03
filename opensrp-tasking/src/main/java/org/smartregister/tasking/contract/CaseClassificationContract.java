package org.smartregister.tasking.contract;

import org.json.JSONObject;

public interface CaseClassificationContract {

    interface View {
        void displayIndexCase(JSONObject indexCase);
    }
}
