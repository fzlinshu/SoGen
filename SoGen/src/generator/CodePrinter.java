package generator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CodePrinter {

	private FileWriter fw;
	private int indent;

	public CodePrinter(File file) throws IOException {
		fw = new FileWriter(file);
	}

	private void printIndent() throws IOException {
		for (int i = 0; i < indent; i++)
			fw.write("\t");
	}

	public void incIndent() {
		indent++;
	}

	public void decIndent() {
		indent--;
	}

	public int getIndent() {
		return indent;
	}

	public void setIndent(int i) {
		indent = i;
	}

	public void resetIndent() {
		indent = 0;
	}

	public void print(String s) throws IOException {
		printIndent();
		fw.write(s);
	}

	public void append(String s) throws IOException {
		fw.write(s);
	}

	public void close() throws IOException {
		fw.close();
	}

}
