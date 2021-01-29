package org.smartregister.tasking.sample.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ibm.fhir.model.resource.Observation;
import com.ibm.fhir.model.resource.Patient;
import com.ibm.fhir.model.type.CodeableConcept;
import com.ibm.fhir.model.type.Date;
import com.ibm.fhir.model.type.HumanName;
import com.ibm.fhir.model.type.Identifier;
import com.ibm.fhir.model.type.Reference;
import com.ibm.fhir.model.type.code.ObservationStatus;

import org.smartregister.pathevaluator.PathEvaluatorLibrary;
import org.smartregister.tasking.sample.R;

import static com.ibm.fhir.model.type.String.of;

public class MainActivity extends AppCompatActivity {

    private Patient patient;

    private TextView resultsTextView;

    private PathEvaluatorLibrary pathEvaluatorLibrary;

    private EditText expressionEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PathEvaluatorLibrary.init(null, null, null, null);
        pathEvaluatorLibrary = PathEvaluatorLibrary.getInstance();

        patient = Patient.builder().id("12345").birthDate(Date.of("1990-12-19"))
                .identifier(Identifier.builder().id("1234").value(of("1212313")).build())
                .name(HumanName.builder().family(of("John")).given(of("Doe")).build()).build();

        Reference.Builder builder = Reference.builder();
        builder.id("12345");
        builder.reference(of(patient.getId()));
        Observation observation = Observation.builder()
                .code(CodeableConcept.builder().id("123").text(of("12343434343")).build()).subject(builder.build())
                .status(ObservationStatus.FINAL).build();
        boolean result = pathEvaluatorLibrary.evaluateBooleanExpression(patient, "Patient.where(name.given = 'Doe').exists()");
        resultsTextView = findViewById(R.id.exists);

        expressionEditText = findViewById(R.id.expression);
        resultsTextView.setText(getString(R.string.result, result));
    }

    public void evaluateExpression(View view) {
        boolean result = pathEvaluatorLibrary.evaluateBooleanExpression(patient, expressionEditText.getText().toString());
        resultsTextView.setText(getString(R.string.result, result));
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}