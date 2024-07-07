package com.dvl.core.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.wickedsource.diffparser.api.UnifiedDiffParser;
import org.wickedsource.diffparser.api.model.Diff;
import org.wickedsource.diffparser.api.model.Hunk;
import org.wickedsource.diffparser.api.model.Range;

import com.dvl.core.vos.MetodosVO;

public class DiffParser {

	public static List<MetodosVO> identificarMetodosAlterados(List<MetodosVO> lstMetodosArquivo) throws FileNotFoundException {

		UnifiedDiffParser parser = new UnifiedDiffParser();

		InputStream in = new FileInputStream("result.txt");

		List<Diff> diff = parser.parse(in);

		List<MetodosVO> retorno = new ArrayList<MetodosVO>(0);

		for (Diff diff2 : diff) {

			List<Hunk> hunks = diff2.getHunks();

			if (hunks == null || hunks.isEmpty())
				continue;

			for (Hunk hunk : hunks) {

				Range rangeInicial = hunk.getFromFileRange();

				Range rangeFinal = hunk.getToFileRange();

				int lineIniStart = rangeInicial.getLineStart();
				int lineIniEnd = rangeInicial.getLineEnd();
				int lineFinStart = rangeFinal.getLineStart();
				int lineFinEnd = rangeFinal.getLineEnd();
				
				for (MetodosVO metodo : lstMetodosArquivo) {

					if (metodo.getRangeInicial() < lineIniEnd && metodo.getRangeFinal() > lineIniStart) {
						retorno.add(metodo);
						continue;
					}

					if (metodo.getRangeInicial() < lineFinEnd && metodo.getRangeFinal() > lineFinStart) {
						retorno.add(metodo);
						continue;
					}
				}
			}
		}

		return retorno;
	}

	public static void main(String[] args) throws FileNotFoundException {
		identificarMetodosAlterados(null);
	}

}
