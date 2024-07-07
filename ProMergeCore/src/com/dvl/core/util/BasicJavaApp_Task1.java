package com.dvl.core.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

public class BasicJavaApp_Task1 {
	
	// Helper method for get the file content
	private static List<String> fileToLines(String filename) {
		List<String> lines = new LinkedList<String>();
		String line = "";
		try {
			@SuppressWarnings("resource")
			BufferedReader in = new BufferedReader(new FileReader(filename));
			while ((line = in.readLine()) != null) {
				lines.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}

	public static void main(String[] args) {
		List<String> original = fileToLines("originalFile.txt");
		List<String> revised = fileToLines("revisedFile.txt");

		// Compute diff. Get the Patch object. Patch is the container for
		// computed deltas.
		Patch patch = DiffUtils.diff(original, revised);

		for (Delta delta : patch.getDeltas()) {
			System.out.println(delta.getOriginal());
			System.out.println(delta.getRevised());
			System.out.println(delta.getType());
			System.out.println(delta);
		}
	}
}