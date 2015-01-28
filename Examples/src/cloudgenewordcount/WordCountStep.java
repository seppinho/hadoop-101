package cloudgenewordcount;

import genepi.hadoop.common.ContextLog;
import genepi.hadoop.common.StepRunner;
import genepi.hadoop.common.WorkflowContext;
import genepi.hadoop.common.WorkflowStep;

public class WordCountStep extends WorkflowStep {

	public boolean run(WorkflowContext context) {

		context.beginTask("Running WordCount...");

		String input = context.get("input");
		String output = context.get("output");

		WordCountJob job = new WordCountJob(new ContextLog(context));
		job.setInput(input);
		job.setOutput(output);

		if (job.execute()) {
			context.endTask("Running WordCount successful.", WorkflowContext.OK);
			return true;
		} else {
			context.endTask("Running WordCount failed.", WorkflowContext.ERROR);
			return false;

		}
	}

	public static void main(String[] args) throws Exception {

		boolean result = StepRunner.run(args, new WordCountStep());

		if (result) {
			System.exit(0);
		} else {
			System.exit(1);
		}

	}

}
