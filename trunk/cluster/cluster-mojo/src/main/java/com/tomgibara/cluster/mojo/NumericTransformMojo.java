package com.tomgibara.cluster.mojo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Goal which converts generic numeric code into java primitives.
 * 
 * @goal numeric-transform
 * 
 * @phase generate-sources
 */
public class NumericTransformMojo extends AbstractMojo {

	// statics
	
	private static final String NL = String.format("%n");
	
	enum NumType {
		DBL, INT, FLT, LNG;
	
		public String getPackageName() {
			switch(this) {
				case DBL : return "dbl";
				case FLT : return "flt";
				case INT : return "intgr";
				case LNG : return "lng";
				default :
					throw new IllegalStateException();
			}
		}

		String getTypeName() {
			switch(this) {
			case DBL : return "double";
			case FLT : return "float";
			case INT : return "int";
			case LNG : return "long";
			default :
				throw new IllegalStateException();
			}
		}
		
		String getZero() {
			switch(this) {
			case DBL : return "0.0";
			case FLT : return "0.0f";
			case INT : return "0";
			case LNG : return "0L";
			default :
				throw new IllegalStateException();
			}
		}
		
		public String getCapName() {
			return toString().substring(0, 1) + toString().substring(1).toLowerCase();
		}
		
	};

	private static String[] splitArguments(String str) {
		str = str.trim();
		int c = 0;
		int l = 0;
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < str.length(); i++) {
			switch (str.charAt(i)) {
			case '(' :  c++; break;
			case ')' :	c--; break;
			case ',' :
				if (c == 0) {
					list.add(str.substring(l, i).trim());
					l = i+1;
				}
			}
		}
		list.add(str.substring(l).trim());
		return (String[]) list.toArray(new String[list.size()]);
	}

	// fields
	
	/**
	 * Directory from which source code will be read.
	 * 
	 * @parameter expression="${project.build.sourceDirectory}"
	 * @required
	 */
	
	private File sourceDirectory;
	
	/**
	 * Directory to which generated sources will be written.
	 * 
	 * @parameter expression="${project.build.directory}/generated-sources/cluster"
	 * @required
	 */
	
	private File generatedDirectory;
	
	/**
	 * The packages for which source code should be generated.
	 * 
	 * @parameter
	 * @required
	 */
	
	private String[] packageNames;

	/**
	 * Name prefix that distinguishes types that should be processed
	 * 
	 * @parameter
	 * @required
	 */
	
	private String typePrefix;
	
	/**
	 * The Maven project.
	 * 
	 * @parameter expression="${project}"
	 * @required
	 */
	private MavenProject project;

	public void execute() throws MojoExecutionException {
		try {
			for (String packageName : packageNames) processPackage(packageName);
		} catch (IOException e) {
			throw new MojoExecutionException("Generating numeric code failed", e);
		}
		project.addCompileSourceRoot( generatedDirectory.getAbsolutePath() );
	}

	private void processPackage(String packageName) throws IOException {
		File srcDir = sourceDirectory;
		File genDir = generatedDirectory;
		
		if (packageName.length() != 0) {
			for (String part : packageName.split("\\.")) {
				srcDir = new File(srcDir, part);
				genDir = new File(genDir, part);
			}
		}

		for (NumType type : NumType.values()) {
			processDir(srcDir, packageName, type, genDir);
		}

	}
	
	private void processDir(File srcDir, String packageName, NumType type, File genDir) throws IOException {
		File[] files = srcDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(typePrefix) && name.endsWith(".java");
			}
		});
		for (File file : files) {
			processFile(file, packageName, type, genDir);
		}
	}

	private void processFile(File srcFile, String packageName, NumType type, File genDir) throws IOException {
		genDir = new File(genDir, type.getPackageName());
		File genFile = new File(genDir, srcFile.getName().replace(typePrefix, type.getCapName()));
		BufferedReader reader = new BufferedReader( new FileReader(srcFile) );
		if (!genDir.exists()) genDir.mkdirs();
		FileWriter writer = new FileWriter(genFile);
		while (true) {
			String line = reader.readLine();
			if (line == null) break;
			String out = line;
			if (out.startsWith("package")) out = "package " + packageName + "." + type.getPackageName() + ";";
			out = out.replace(typePrefix, type.getCapName());
			out = out.replace("NUMBER ", type.getTypeName()+" ");
			out = out.replace("NUMBER[", type.getTypeName()+"[");
			out = out.replace("NUMBER.zero()", type.getZero());
			out = out.replace("import com.tomgibara.cluster.NUMBER;", "");
			out = processSource(type, out);
			writer.write(out);
			writer.write(NL);
		}
		writer.close();
		reader.close();
	}

	private String processSource(NumType type, String src) {
		int i = src.indexOf("NUMBER");
		if (i == -1) return src;
		String pre = src.substring(0, i);
		int c = 0;
		int j = i;
		loop: for (;j < src.length(); j++) {
			switch(src.charAt(j)) {
			case '(' : c++; break;
			case ')' : if (--c == 0) break loop; else break;
			}
		}
		String post = src.substring(j+1);
		String body = src.substring(i, j+1);
		int k = body.indexOf('(');
		String op = body.substring(7, k);
		String all = body.substring(k+1, body.length() - 1);
		String[] args = splitArguments(all);
		String rep;
		if (op.equals("equal")) {
			rep = "(" + processSource(type, args[0]) + " == " + processSource(type, args[1]) + ")";
		} else if (op.equals("unequal")) {
			rep = "(" + processSource(type, args[0]) + " != " + processSource(type, args[1]) + ")";
		} else if (op.equals("lessThan")) {
			rep = "(" + processSource(type, args[0]) + " < " + processSource(type, args[1]) + ")";
		} else if (op.equals("product")) {
			rep = "(" + processSource(type, args[0]) + " * " + processSource(type, args[1]) + ")";
		} else if (op.equals("sum")) { //TODO optimize add
			rep = "(" + processSource(type, args[0]) + " + " + processSource(type, args[1]) + ")";
		} else if (op.equals("quotient")) {
			rep = "(" + processSource(type, args[0]) + " / " + processSource(type, args[1]) + ")";
		} else if (op.equals("difference")) {
			rep = "(" + processSource(type, args[0]) + " - " + processSource(type, args[1]) + ")";
		} else if (op.equals("square")) {
			rep = "(" + processSource(type, args[0]) + " * " + processSource(type, args[0]) + ")";
		} else if (op.equals("doubleValue")) {
			rep = "(double)(" + processSource(type, args[0]) +")";
		} else if (op.equals("add")) {
			rep = args[0] + "[" + args[1] + "] += " + processSource(type, args[2]);
		} else {
			rep = "/* " + op + Arrays.toString(args) + "*/";
		}
		return processSource(type, pre + rep + post);
	}

	public static void main(String[] args) throws MojoExecutionException {
		NumericTransformMojo mojo = new NumericTransformMojo();
		mojo.sourceDirectory = new File("/home/tom/development/workspace/gvm/src/main/java");
		mojo.generatedDirectory = new File("test-output");
		mojo.packageNames = new String[] { "com.tomgibara.cluster.gvm" };
		mojo.typePrefix = "Gvm";
		mojo.execute();
	}
	
	
}
