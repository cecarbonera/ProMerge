package com.dvl.core.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import difflib.ChangeDelta;
import difflib.Chunk;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

public class BasicJavaApp_Task2 {
	// Helper method for get the file content
	@SuppressWarnings("resource")
	private static List<String> fileToLines(String filename) {
		List<String> lines = new LinkedList<String>();
		String line = "";
		try {
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
		// At first, parse the unified diff file and get the patch
		Patch patch = DiffUtils.parseUnifiedDiff(fileToLines("result.txt"));

		List<Delta> deltas = patch.getDeltas();
		for (Delta delta : deltas) {

			ChangeDelta a = (ChangeDelta) delta;
			Chunk original = a.getOriginal();
			Chunk revised = a.getRevised();

			List<?> lines = original.getLines();

			for (Object object : lines) {

				System.out.println(object);
			}

			lines = revised.getLines();

			for (Object object : lines) {

				System.out.println(object);
			}

			System.out.println(delta);
		}
	}
}