/**
 * (c) Copyright Mirasol Op'nWorks Inc. 2002, 2003. 
 * http://www.opnworks.com
 * Created on Apr 2, 2003 by lgauthier@opnworks.com
 * 
 */

package com.dvl.promerge.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import com.dvl.core.entitys.ProMergeResumosConflitos;

/**
 * Sorter for the TableViewerExample that displays items of type <code>ExampleTask</code>. The sorter supports three sort criteria:
 * <p>
 * <code>DESCRIPTION</code>: Task description (String)
 * </p>
 * <p>
 * <code>OWNER</code>: Task Owner (String)
 * </p>
 * <p>
 * <code>PERCENT_COMPLETE</code>: Task percent completed (int).
 * </p>
 */
public class ProMergeTaskSorter extends ViewerSorter {

	/**
	 * Constructor argument values that indicate to sort items by description, owner or percent complete.
	 */
	public final static int USUARIO = 1;
	public final static int ARQUIVO = 2;
	public final static int TIPO_CONFLITO = 3;
	public final static int DATA_ALTERACAO = 4;
	public final static int COMITADO = 5;
	public final static int SEVERIDADE = 6;

	// Criteria that the instance uses
	private int criteria;

	/**
	 * Creates a resource sorter that will use the given sort criteria.
	 * 
	 * @param criteria
	 *            the sort criterion to use: one of <code>NAME</code> or <code>TYPE</code>
	 */
	public ProMergeTaskSorter(int criteria) {
		super();
		this.criteria = criteria;
	}

	/*
	 * (non-Javadoc) Method declared on ViewerSorter.
	 */
	@SuppressWarnings("deprecation")
	public int compare(Viewer viewer, Object o1, Object o2) {

		ProMergeResumosConflitos task1 = (ProMergeResumosConflitos) o1;
		ProMergeResumosConflitos task2 = (ProMergeResumosConflitos) o2;

		switch (criteria) {
		case USUARIO:
			return collator.compare(task1.getDesUsuario(), task2.getDesUsuario());
		case ARQUIVO:
			return collator.compare(task1.getDesArquivo(), task2.getDesArquivo());
		case TIPO_CONFLITO:
			return collator.compare(task1.getTipConflito(), task2.getTipConflito());
		case DATA_ALTERACAO:
			return collator.compare(task1.getDtaAlteracao(), task2.getDtaAlteracao());
		case COMITADO:
			return collator.compare(task1.getComitado(), task2.getComitado());
		case SEVERIDADE:
			return collator.compare(task1.getSeveridade(), task2.getSeveridade());

		default:
			return 0;
		}
	}

	/**
	 * Returns the sort criteria of this this sorter.
	 * 
	 * @return the sort criterion
	 */
	public int getCriteria() {
		return criteria;
	}
}
