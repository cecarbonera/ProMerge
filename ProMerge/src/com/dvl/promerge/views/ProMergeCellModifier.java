/**
 * (c) Copyright Mirasol Op'nWorks Inc. 2002, 2003. 
 * http://www.opnworks.com
 * Created on Apr 2, 2003 by lgauthier@opnworks.com
 * 
 */

package com.dvl.promerge.views;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.TableItem;

import com.dvl.core.entitys.ProMergeResumosConflitos;

/**
 * This class implements an ICellModifier An ICellModifier is called when the user modifes a cell in the tableViewer
 */

public class ProMergeCellModifier implements ICellModifier {
	private TableViewerProMerge tableViewerExample;

	/**
	 * Constructor
	 * 
	 * @param TableViewerProMerge
	 *            an instance of a TableViewerExample
	 */
	public ProMergeCellModifier(TableViewerProMerge tableViewerExample) {
		super();
		this.tableViewerExample = tableViewerExample;
	}

	/**
	 * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
	 */
	public boolean canModify(Object element, String property) {
		return false;
	}

	/**
	 * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
	 */
	public Object getValue(Object element, String property) {

		// Find the index of the column
		int columnIndex = tableViewerExample.getColumnNames().indexOf(property);

		Object result = null;
		ProMergeResumosConflitos task = (ProMergeResumosConflitos) element;

		switch (columnIndex) {
		case 0: // USUARIO
			result = task.getDesUsuario();
			break;
		case 1: // ARQUIVO
			result = task.getDesArquivo();
			break;
		case 2: // TIPO_CONFLITO
			result = task.getTipConflito();
			break;
		case 3: // DATA ALTERACAO
			result = task.getDtaAlteracao();
			break;
		case 4: // COMITADO
			result = task.getComitado();
			break;
		case 5: // SEVERIDADE
			result = task.getSeveridade();
			break;
		default:
			result = "";
		}

		return result;

	}

	/**
	 * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
	 */
	public void modify(Object element, String property, Object value) {

		TableItem item = (TableItem) element;
		ProMergeResumosConflitos task = (ProMergeResumosConflitos) item.getData();

		tableViewerExample.getTaskList().taskChanged(task);
	}
}
