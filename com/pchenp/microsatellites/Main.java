package com.pchenp.microsatellites;

import java.util.HashMap;
import java.util.LinkedList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.pchenp.microsatellites.RepeatMapper.MBuilder;
import com.pchenp.microsatellites.RepeatReader.RBuilder;

public class Main {

	private static final String HELP = "help";
	private static final String RIN = "rin";
	private static final String ROUT = "rout";
	private static final String CM124 = "cm124";
	private static final String PIN = "pin";
	private static final String POUT = "pout";
	private static final String RBUF = "rbuf";
	private static final String SRLEN = "srlen";
	private static final String MINREP = "minrep";
	private static final String COV = "cov";
	private static final String MINPAT = "minpat";
	private static final String MAXPAT = "maxpat";
	private static final String THREADS = "threads";
	private static final String MINSCORE = "minscore";
	private static final String STATS = "stats";
	private static final String ALL = "all";
	
	public static void main(String[] args) {
		Options options = new Options();
		options.addOption("h", HELP, false, "print this");
		options.addOption("s", STATS, false, "print accuracy, execution time, and other statistics");
		options.addOption("r", RIN, true, "reference file input");
		options.addOption("R", ROUT, true, "reference result output, default: stdout");
		options.addOption("w", CM124, true, "file to write cm124 formatted answer");
		options.addOption("p", PIN, true, "private file");
		options.addOption("P", POUT, true, "private result output, default: stdout");
		options.addOption("b", RBUF, true, "read buffer size in bytes, default: " + Const.DEF_BUF);
		options.addOption("l", SRLEN, true, "short read length, default: " + Const.DEF_READ);
		options.addOption("n", MINREP, true, "minimum repeats to qualify, default: " + Const.DEF_MIN_REPEATS);
		options.addOption("c", COV, true, "short read sequence coverage, default: " + Const.DEF_COVERAGE);
		options.addOption("m", MINPAT, true, "minimum STR length, default: " + Const.DEF_MIN_PAT);
		options.addOption("M", MAXPAT, true, "maximum STR length, default: " + Const.DEF_MAX_PAT);
		options.addOption("t", THREADS, true, "number of threads, default: " + Const.DEF_THREADS);
		options.addOption("q", MINSCORE, true, "minimum score to qualify, default: " + Const.DEF_MIN_SCORE);
		options.addOption("a", ALL, false, "output unidentified repeats and reference tags");
		CommandLineParser parser = new GnuParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("str", options);
			return;
		}
		if (cmd.hasOption(HELP)) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("str", options);
			return;
		}
		MBuilder<?> mapperBuilder = SerialRepeatMapper.builder();
		RBuilder<?> readerBuilder;
		if (cmd.hasOption(THREADS)) {
			readerBuilder = ConcurrentRepeatReader.builder();
		} else {
			readerBuilder = SerialRepeatReader.builder();	
		}
		String rin = null;
		if (cmd.hasOption(RIN)) {
			rin = cmd.getOptionValue(RIN);
		} else {
			System.err.println("Must input a reference file: -r --rin");
			return;
		}
		if (cmd.hasOption(STATS)) {
			mapperBuilder.stats(true);
			readerBuilder.stats(true);
		}
		try {
			if (cmd.hasOption(RBUF)) {
				int rbuf = Integer.parseInt(cmd.getOptionValue(RBUF));
				if (rbuf < Const.MIN_BUF || rbuf > Const.MAX_BUF) {
					throw new NumberFormatException("Invalid buffer size.");
				}
				mapperBuilder.bufSize(rbuf);
			}
			if (cmd.hasOption(SRLEN)) {
				int srlen = Integer.parseInt(cmd.getOptionValue(SRLEN));
				if (srlen < Const.MIN_READ || srlen > Const.MAX_READ) {
					throw new NumberFormatException("Invalid read length.");
				}
				mapperBuilder.readSize(srlen);
				readerBuilder.readSize(srlen);
			}
			if (cmd.hasOption(MINREP)) {
				int minrep = Integer.parseInt(cmd.getOptionValue(MINREP));
				if (minrep < Const.MIN_MIN_REPEATS || minrep > Const.MAX_MIN_REPEATS) {
					throw new NumberFormatException("Invalid minimum repeats number.");
				}
				mapperBuilder.minRepeats(minrep);
				readerBuilder.minRepeats(minrep);
			}
			if (cmd.hasOption(COV)) {
				int cov = Integer.parseInt(cmd.getOptionValue(COV));
				if (cov < Const.MIN_COVERAGE || cov > Const.MAX_COVERAGE) {
					throw new NumberFormatException("Invalid coverage number.");
				}
				readerBuilder.coverage(cov);
			}
			if (cmd.hasOption(MINPAT)) {
				int minpat = Integer.parseInt(cmd.getOptionValue(MINPAT));
				if (minpat < Const.MIN_MIN_PAT || minpat > Const.MAX_MAX_PAT) {
					throw new NumberFormatException("Invalid minimum pattern length.");
				}
				mapperBuilder.minPat(minpat);
				readerBuilder.minPat(minpat);
			}
			if (cmd.hasOption(MAXPAT)) {
				int maxpat = Integer.parseInt(cmd.getOptionValue(MAXPAT));
				if (maxpat < Const.MIN_MIN_PAT || maxpat > Const.MAX_MAX_PAT) {
					throw new NumberFormatException("Invalid maximum pattern length.");
				}
				mapperBuilder.maxPat(maxpat);
				readerBuilder.maxPat(maxpat);
			}
			if (cmd.hasOption(THREADS)) {
				int threads = Integer.parseInt(cmd.getOptionValue(THREADS));
				if (threads < Const.MIN_THREADS || threads > Const.MAX_THREADS) {
					throw new NumberFormatException("Invalid number of threads.");
				}
				readerBuilder.numThreads(threads);
			}
			if (cmd.hasOption(MINSCORE)) {
				double minscore = Double.parseDouble(cmd.getOptionValue(MINSCORE));
				if (minscore < Const.MIN_MIN_SCORE || minscore > Const.MAX_MIN_SCORE) {
					throw new NumberFormatException("Invalid minimum score value.");
				}
				readerBuilder.minScore(minscore);
			}
		} catch (NumberFormatException e) {
			System.err.println("--rbuf --srlen --minrep --cov --minpat --maxpat must be a valid integer.");
			System.err.println(e.getMessage());
			return;
		}
		SerialRepeatMapper mapper = (SerialRepeatMapper) mapperBuilder.build();
		HashMap<String, LinkedList<ShortTandemRepeat>> refMap = mapper.mapFile(rin);
		readerBuilder.ref(refMap);
		RepeatReader reader = readerBuilder.build();
		boolean outputAll = cmd.hasOption(ALL);
		if (cmd.hasOption(ROUT)) {
			Utilities.writeRef(cmd.getOptionValue(ROUT), refMap, outputAll);	
		} else {
			Utilities.printRef(refMap, outputAll);
		}
		if (cmd.hasOption(CM124)) {
			Utilities.writeCM124Answer(rin, cmd.getOptionValue(CM124), refMap);
		}
		if (cmd.hasOption(PIN)) {
			String pin = cmd.getOptionValue(PIN);
			HashMap<ShortTandemRepeat, ShortTandemRepeat> priv;
			priv = reader.readFile(pin);
			if (cmd.hasOption(POUT)) {
				Utilities.writePriv(cmd.getOptionValue(POUT), priv, outputAll);
			} else {
				Utilities.printPriv(priv, outputAll);
			}
			if (cmd.hasOption(STATS)) {
				Utilities.printIdentified(refMap, priv);
				Utilities.printCounts(priv);
			}
		} else {
			if (cmd.hasOption(POUT)) {
				System.err.println("Missing private input file.");
				return;
			}	
		}
	}

}
