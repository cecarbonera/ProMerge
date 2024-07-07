/**
 * (c) Copyright Mirasol Op'nWorks Inc. 2002, 2003. 
 * http://www.opnworks.com
 * Created on Apr 2, 2003 by lgauthier@opnworks.com
 */

package com.dvl.promerge.views;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import com.dvl.core.entitys.ProMergeResumosConflitos;
import com.dvl.core.util.AplicationUtils;
 
/**
 * Label provider for the TableViewerExample
 * 
 * @see org.eclipse.jface.viewers.LabelProvider
 */
public class ProMergeLabelProvider extends LabelProvider implements ITableLabelProvider {

	// Names of images used to represent checkboxes
	public static final String CHECKED_IMAGE = "checked";
	public static final String UNCHECKED_IMAGE = "unchecked";

	// For the checkbox images
	private static ImageRegistry imageRegistry = new ImageRegistry();

	/**
	 * Note: An image registry owns all of the image objects registered with it, and automatically disposes of them the SWT Display is
	 * disposed.
	 */
	static {
		String iconPath = "icons/";
		imageRegistry.put(CHECKED_IMAGE, ImageDescriptor.createFromFile(TableViewerProMerge.class, iconPath + CHECKED_IMAGE + ".gif"));
		imageRegistry.put(UNCHECKED_IMAGE, ImageDescriptor.createFromFile(TableViewerProMerge.class, iconPath + UNCHECKED_IMAGE + ".gif"));
	}

	/**
	 * Returns the image with the given key, or <code>null</code> if not found.
	 */
	@SuppressWarnings("unused")
	private Image getImage(boolean isSelected) {
		String key = isSelected ? CHECKED_IMAGE : UNCHECKED_IMAGE;
		return imageRegistry.get(key);
	}

	/**
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	public String getColumnText(Object element, int columnIndex) {
		String result = "";
		ProMergeResumosConflitos task = (ProMergeResumosConflitos) element;

		switch (columnIndex) {
		case 0: // Usuário
			result = task.getDesUsuario();
			break;
		case 1: // Nome Arquivo
			result = task.getDesArquivo();
			break;
		case 2: // Tipo Conflito
			result = getDesConflito(task.getTipConflito());
			break;
		case 3: // Data Alteração
			result = getDtaAlteracao(task.getDtaAlteracao());
			break;
		case 4: // Severidade
			result = AplicationUtils.getDescricaoSeveridade(task.getSeveridade());
			break;
		case 5: // Comitado
			result = getComitado(task.getComitado());
			break;
		default:
			result = "";
		}
		return result;
	}

	private String getComitado(Integer indComitado) {
		return indComitado == 0 ? "Yes" : "No"; 

	}

	private String getDtaAlteracao(String dtaAlteracao) {
		return (dtaAlteracao == null) ? "" : dtaAlteracao;
	}

	private String getDesConflito(Integer tipConflito) {
		// 1 - Conflito Direto e 2 - Conflito Indireto
		return tipConflito == 3 ? "Compile Error" : (tipConflito == 1 ? "Direct Conflict" : "Indirect Conflict");
		
	}

	/**
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

}