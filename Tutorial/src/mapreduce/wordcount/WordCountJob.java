package mapreduce.wordcount;

import genepi.hadoop.HadoopJob;

import org.apache.commons.logging.Log;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;

public class WordCountJob extends HadoopJob {

	public WordCountJob() {
		super("Word Count");
	}
	
	public WordCountJob(Log log) {
		super("Word Count", log);
	}

	@Override
	public void setupJob(Job job) {
		job.setJarByClass(WordCountJob.class);
		job.setMapperClass(TokenizerMapper.class);
		job.setCombinerClass(IntSumReducer.class);
		job.setReducerClass(IntSumReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
	}

}
