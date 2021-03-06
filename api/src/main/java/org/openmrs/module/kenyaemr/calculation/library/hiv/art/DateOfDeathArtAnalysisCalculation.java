package org.openmrs.module.kenyaemr.calculation.library.hiv.art;

import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.calculation.result.SimpleResult;
import org.openmrs.module.kenyacore.calculation.AbstractPatientCalculation;
import org.openmrs.module.kenyaemr.calculation.EmrCalculationUtils;
import org.openmrs.module.kenyaemr.calculation.library.rdqa.DateOfDeathCalculation;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.common.DurationUnit;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * Created by codehub on 9/8/15.
 */
public class DateOfDeathArtAnalysisCalculation extends AbstractPatientCalculation {

    @Override
    public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> params, PatientCalculationContext context) {

        Integer outcomePeriod = (params != null && params.containsKey("outcomePeriod")) ? (Integer) params.get("outcomePeriod") : null;
        CalculationResultMap initialArtStart = calculate(new InitialArtStartDateCalculation(), cohort, context);

        if(outcomePeriod != null){
            context.setNow(DateUtil.adjustDate(DateUtil.getStartOfMonth(context.getNow()), outcomePeriod, DurationUnit.MONTHS));
        }

        CalculationResultMap deadPatientsMap = calculate(new DateOfDeathCalculation(), cohort, context);

        CalculationResultMap ret = new CalculationResultMap();

        for(Integer ptId:cohort){
            Date result = EmrCalculationUtils.datetimeResultForPatient(deadPatientsMap, ptId);
            Date artStartDate = EmrCalculationUtils.datetimeResultForPatient(initialArtStart, ptId);
            Date deathDate = null;
            if(artStartDate != null && outcomePeriod != null && result != null) {
                Date futureDate = DateUtil.adjustDate(DateUtil.adjustDate(artStartDate, outcomePeriod, DurationUnit.MONTHS), 1, DurationUnit.DAYS);

                if (result.before(futureDate)) {
                    deathDate = result;

                }
            }
            ret.put(ptId, new SimpleResult(deathDate, this));
        }
        return ret;
    }
}
