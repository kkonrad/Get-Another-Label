package com.ipeirotis.gal.engine.rpt;

import java.io.IOException;

import com.ipeirotis.gal.decorator.FieldAccessors.EvalDatumFieldAccessor;
import com.ipeirotis.gal.decorator.FieldAccessors.FieldAccessor;
import com.ipeirotis.gal.scripts.Datum;
import com.ipeirotis.gal.scripts.DawidSkene;
import com.ipeirotis.gal.scripts.Worker;

public class SummaryReport extends Report {
	@Override
	public boolean execute(ReportingContext ctx) throws IOException {
		info("Summary");

		ReportTarget[] reportTargets = new ReportTarget[] {
				new StreamReportTarget(System.out),
				new FileReportTarget("results/summary.txt") };

		for (ReportTarget reportTarget : reportTargets) {
			reportTarget.println("Categories: %s", ctx.getEngine()
					.getCategories().size());
			reportTarget.println("Objects in Data Set: %s", ctx.getDawidSkene()
					.getObjects().size());
			reportTarget.println("Workers in Data Set: %s", ctx.getDawidSkene()
					.getWorkers().size());
			reportTarget.println("Labels Assigned by Workers: %s", ctx
					.getEngine().getLabels().size());
			
			for (FieldAccessor a : ctx.getDawidSkene().getFieldAccessors(DawidSkene.class)) {
				Object value = a.getValue(ctx.getDawidSkene());
				
				if (null == value)
					continue;
				
				reportTarget.println("[%s] %s: %s", a.getDesc(), a.getSummaryDescription(), a.getFormatter().format((Double) value));
			}

			for (FieldAccessor a : ctx.getDawidSkene().getFieldAccessors(Datum.class)) {
				if (! a.isAveraged())
					continue;
				
				reportTarget.println("[%s] %s: %s", a.getDesc(), a.getSummaryDescription(), getAverage(a, ctx.getDawidSkene().getObjects().values()));
			}
			
			for (FieldAccessor a : ctx.getDawidSkene().getFieldAccessors(Worker.class)) {
				if (! a.isAveraged())
					continue;
				
				reportTarget.println("[%s] %s: %s", a.getDesc(), a.getSummaryDescription(), getAverage(a, ctx.getDawidSkene().getWorkers().values()));
			}

			reportTarget.close();
		}

		return super.execute(ctx);
	}

	public <T> Object getAverage(FieldAccessor fieldAcessor, Iterable<T> objects) {
		Double accumulator = 0d;
		long count = 0;
		boolean evalP = fieldAcessor instanceof EvalDatumFieldAccessor;
		
		for (T object : objects) {
			if (evalP) {
				Datum datum = ((Datum) object);
				
				if (! datum.isEvaluation())
					continue;
			}
			
			
			Double total = (Double) fieldAcessor.getValue(object);
			
			accumulator += total;
			count++;
		}
		
		Double result = accumulator / count;
		
		if (null != fieldAcessor.getFormatter()) {
			return fieldAcessor.getFormatter().format(result);
		}
		
		return result;
	}
}
