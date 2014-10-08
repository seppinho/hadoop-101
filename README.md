MapReduce
=========
This tutorial introduces Cloudgene. It shows how a simple WordCount program is designed with Hadoop Mapreduce using the standard interfaces. We introduce the genepi-hadoop library to efficiently distribute parameters, using the distributed cache and guarantee the correct order of calls.
The tutorial further shows how to connect applications (such as WordCount) into Cloudgene to execute them graphically.



### Word Count Example

#### Mapper
	public static class TokenizerMapper extends
			Mapper<Object, Text, Text, IntWritable> {

		private final static IntWritable one = new IntWritable(1);
		private Text word = new Text();

		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			StringTokenizer itr = new StringTokenizer(value.toString());
			while (itr.hasMoreTokens()) {
				word.set(itr.nextToken());
				context.write(word, one);
			}
		}
	}

#### Reducer

	public static class IntSumReducer extends
			Reducer<Text, IntWritable, Text, IntWritable> {
		private IntWritable result = new IntWritable();

		public void reduce(Text key, Iterable<IntWritable> values,
				Context context) throws IOException, InterruptedException {
			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
			result.set(sum);
			context.write(key, result);
		}
	}

#### Executing Job

	Configuration conf = new Configuration();
	String[] otherArgs = new GenericOptionsParser(conf, args)
			.getRemainingArgs();
	if (otherArgs.length != 2) {
		System.err.println("Usage: wordcount <in> <out>");
		System.exit(2);
	}
	Job job = new Job(conf, "word count");
	job.setJarByClass(WordCount.class);
	job.setMapperClass(TokenizerMapper.class);
	job.setCombinerClass(IntSumReducer.class);
	job.setReducerClass(IntSumReducer.class);
	job.setOutputKeyClass(Text.class);
	job.setOutputValueClass(IntWritable.class);
	FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
	FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
	if (job.waitForCompletion(true)){
		System.exit(0);
	}else{
		System.exit(1);
	}

### Executing Word Count using genepi-hadoop

#### Extend HadoopJob by overriding the  setupJob method

	public static class WordCountJob extends HadoopJob {

		public WordCountJob() {
			super("Word Count");
		}

		@Override
		public void setupJob(Job job) {
			job.setJarByClass(WordCountGenepi.class);
			job.setMapperClass(TokenizerMapper.class);
			job.setCombinerClass(IntSumReducer.class);
			job.setReducerClass(IntSumReducer.class);
			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(IntWritable.class);
		}

	}

#### WorkflowStep

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
	  }


#### Execute it through a command-line program

	  public static void main(String[] args) throws Exception {
	  
	      boolean result = StepRunner.run(args, new WordCountStep());
	  
	      if (result) {
	          System.exit(0);
	      } else {
	          System.exit(1);
	      }
	  }

### Cloudgene Integration

#### Command-Line program

	 steps:
	    - name: Wordcount
	      jar: Examples.jar
	      params: --input $input --output $output


#### Directly by specifying the classname

	 steps:
	    - name: Wordcount
	      jar: Examples.jar
	      classname: wordcount.WordCountStep
