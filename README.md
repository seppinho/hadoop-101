
#A simple Hadoop Tutorial from Code to Results

=========
This repository includes some basic Hadoop YARN Examples (MapReduce and Spark) compiled for the latest CDH version. All examples can be run either on the command line or within [Cloudgene)(http://cloudgene.uibk.ac.at), our graphical workflow system for Hadoop. 

## Ressources
SparkWordCount: https://github.com/sryza/simplesparkapp

BioWordCount for MapReduce and Spark: https://github.com/plantimal

## Running Wordcount in Cloudgene
First we show how a simple WordCount program is designed with Hadoop Mapreduce. We then introduce the genepi-hadoop library to simplify the writing of MapReduce programs. In a last step, we integrate the program into Cloudgene 

### 1) Word Count Standard

#### Mapper
	public class TokenizerMapper extends
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

	public class IntSumReducer extends
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

 	public static void main(String[] args) throws Exception {

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
		
	}

----------------------------

### 2) Word Count using genepi-hadoop

#### Extend HadoopJob by overriding the  setupJob method

	public class WordCountJob extends HadoopJob {

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

Extend you Step class by a main method and use the StepRunner class to start your WorkflowStep class:

	  public static void main(String[] args) throws Exception {
	  
	      boolean result = StepRunner.run(args, new WordCountStep());
	  
	      if (result) {
	          System.exit(0);
	      } else {
	          System.exit(1);
	      }
	  }

The compiled programm is jar file which can be executed with the following command:

	hadoop jar Examples.jar --input bigfile.txt --output wc_out.txt


----------------------------

### 3) Cloudgene Integration

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
