/**O
 * (c) Copyright Mirasol Op'nWorks Inc. 2002, 2003. 
 * http://www.opnworks.com
 * Created on Apr 2, 2003 by lgauthier@opnworks.com
 * 
 */

package com.dvl.promerge.views;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.dvl.core.dao.DaoGenerico;
import com.dvl.core.entitys.ProMergeResumosConflitos;

/**
 * The TableViewerExample class is meant to be a fairly complete example of the use of the org.eclipse.jface.viewers.TableViewer class to implement an
 * editable table with text, combobox and image editors.
 * 
 * The example application metaphor consists of a table to view and edit tasks in a task list. It is by no means a complete or truly usable
 * application.
 * 
 * This example draws from sample code in the Eclipse org.eclipse.ui.views.tasklist.TaskList class and some sample code in SWT fragments from the
 * eclipse.org web site.
 * 
 * Known issue: We were not able to get the images to be center aligned in the checkbox column.
 * 
 * @author Laurent Gauthier
 * @created Apr 2, 2003
 */

public class TableViewerProMerge {

	private static Logger log = Logger.getLogger(TableViewerProMerge.class);

	/**
	 * @param parent
	 */
	public TableViewerProMerge(Composite parent) {

		this.addChildControls(parent);
	}

	// private Shell shell;
	private Table table;
	private TableViewer tableViewer;
	private Button closeButton;

	// Create a ExampleTaskList and assign it to an instance variable
	private ProMergeTaskList taskList = new ProMergeTaskList();

	// Set the table column property names
	private final String USUARIO = "User";
	private final String ARQUIVO_ALTERADO = "File Name";
	private final String TIPO_CONFLITO = "Conflict Type";
	private final String DATA_ALTERACAO = "Date/Hour Commit";
	private final String SEVERIDADE = "Severity";
	private final String COMITADO = "Committed ?";

	// Set column names
	private String[] columnNames = new String[] { USUARIO, ARQUIVO_ALTERADO, TIPO_CONFLITO, DATA_ALTERACAO, SEVERIDADE, COMITADO };

	/**
	 * Main method to launch the window
	 */
	public static void main(String[] args) {

		Shell shell = new Shell();
		shell.setText("Task List - TableViewer Example");

		// Set layout for shell
		GridLayout layout = new GridLayout();
		shell.setLayout(layout);

		// Create a composite to hold the children
		Composite composite = new Composite(shell, SWT.NONE);
		final TableViewerProMerge tableViewerExample = new TableViewerProMerge(composite);

		tableViewerExample.getControl().addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				tableViewerExample.dispose();
			}

		});

		// Ask the shell to display its content
		shell.open();
		tableViewerExample.run(shell);
	}

	/**
	 * Run and wait for a close event
	 * 
	 * @param shell
	 *            Instance of Shell
	 */
	private void run(Shell shell) {

		// Add a listener for the close button
		// closeButton.addSelectionListener(new SelectionAdapter() {
		//
		// // Close the view i.e. dispose of the composite's parent
		// public void widgetSelected(SelectionEvent e) {
		// table.getParent().getParent().dispose();
		// }
		// });

		Display display = shell.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}

	/**
	 * Release resources
	 */
	public void dispose() {

		// Tell the label provider to release its resources
		tableViewer.getLabelProvider().dispose();
	}

	/**
	 * Create a new shell, add the widgets, open the shell
	 * 
	 * @return the shell that was created
	 */
	private void addChildControls(Composite composite) {

		// Create a composite to hold the children
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_BOTH);
		composite.setLayoutData(gridData);

		// Set numColumns to 3 for the buttons
		GridLayout layout = new GridLayout(3, false);
		layout.marginWidth = 4;
		composite.setLayout(layout);

		// Create the table
		createTable(composite);

		// Create and setup the TableViewer
		createTableViewer();
		final ExampleContentProvider exampleContentProvider = new ExampleContentProvider();
		tableViewer.setContentProvider(exampleContentProvider);
		tableViewer.setLabelProvider(new ProMergeLabelProvider());
		// The input for the table viewer is the instance of ExampleTaskList
		taskList = new ProMergeTaskList();
		tableViewer.setInput(taskList);

		Thread atualizacaoFontes = new Thread() {
			public void run() {

				while (true) {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					Display.getDefault().asyncExec(new Runnable() {
						public void run() {

							try {

								// aqui deverá carregar os arquivos alterados localmente
								List<ProMergeResumosConflitos> lista = DaoGenerico.getInstance().buscarResumoConflitos(
										MainView.listaArquivosModificados, MainView.usuario);

								// Limpar os conflitos anteriores
								exampleContentProvider.removeAll();

								// Se retornou vazio, retorna
								if (lista == null || lista.isEmpty()) {
									return;
								}

								// Adiciona os conflitos
								for (ProMergeResumosConflitos promergeResumosConflitos : lista) {
									exampleContentProvider.addTask(promergeResumosConflitos);
								}

							} catch (Exception e) {
								log.error("Error consulting records", e);
							}

						}
					});

				}
			}
		};

		atualizacaoFontes.start();
	}

	/**
	 * Create the Table
	 */
	private void createTable(Composite parent) {
		int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION;

		table = new Table(parent, style);

		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 3;
		table.setLayoutData(gridData);

		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		// 1a Coluna
		TableColumn column = new TableColumn(table, SWT.LEFT, 0);
		column.setText("User");
		column.setWidth(100);
		// Add listener to column so tasks are sorted by description when
		// clicked
		column.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				tableViewer.setSorter(new ProMergeTaskSorter(ProMergeTaskSorter.USUARIO));
			}
		});

		// 2a Coluna
		column = new TableColumn(table, SWT.LEFT, 1);
		column.setText("File Name");
		column.setWidth(300);
		// Add listener to column so tasks are sorted by owner when clicked
		column.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				tableViewer.setSorter(new ProMergeTaskSorter(ProMergeTaskSorter.ARQUIVO));
			}
		});

		// 3a Coluna
		column = new TableColumn(table, SWT.CENTER, 2);
		column.setText("Conflict Type");
		column.setWidth(200);
		// Add listener to column so tasks are sorted by percent when clicked
		column.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				tableViewer.setSorter(new ProMergeTaskSorter(ProMergeTaskSorter.TIPO_CONFLITO));
			}
		});

		// 4a Coluna
		column = new TableColumn(table, SWT.CENTER, 3);
		column.setText("Date/Hour of Commit");
		column.setWidth(200);
		// Add listener to column so tasks are sorted by percent when clicked
		column.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				tableViewer.setSorter(new ProMergeTaskSorter(ProMergeTaskSorter.DATA_ALTERACAO));
			}
		});

		// 5a Coluna
		column = new TableColumn(table, SWT.CENTER, 4);
		column.setText("Severity");
		column.setWidth(200);
		// Add listener to column so tasks are sorted by percent when clicked
		column.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				tableViewer.setSorter(new ProMergeTaskSorter(ProMergeTaskSorter.SEVERIDADE));
			}
		});

		// 6a Coluna
		column = new TableColumn(table, SWT.CENTER, 5);
		column.setText("Committed?");
		column.setWidth(200);
		// Add listener to column so tasks are sorted by percent when clicked
		column.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				tableViewer.setSorter(new ProMergeTaskSorter(ProMergeTaskSorter.COMITADO));
			}
		});
	}

	/**
	 * Create the TableViewer
	 */
	private void createTableViewer() {

		tableViewer = new TableViewer(table);
		tableViewer.setUseHashlookup(true);

		tableViewer.setColumnProperties(columnNames);

		// Create the cell editors
		// USUARIO, ARQUIVO_ALTERADO, TIPO_CONFLITO, DATA_ALTERACAO, SEVERIDADE,
		// IND_COMITADO
		CellEditor[] editors = new CellEditor[columnNames.length];

		// Column 1 : Usuário
		TextCellEditor textEditor = new TextCellEditor(table);
		((Text) textEditor.getControl()).setTextLimit(100);
		editors[0] = textEditor;

		// Column 2 : Arquivo
		textEditor = new TextCellEditor(table);
		((Text) textEditor.getControl()).setTextLimit(100);
		editors[1] = textEditor;

		// Column 3 : Tipo de conflito
		textEditor = new TextCellEditor(table);
		((Text) textEditor.getControl()).setTextLimit(100);
		editors[2] = textEditor;

		// Column 3 : Data alteração
		textEditor = new TextCellEditor(table);
		((Text) textEditor.getControl()).setTextLimit(100);
		editors[3] = textEditor;

		// Column 4 : Severidade
		textEditor = new TextCellEditor(table);
		((Text) textEditor.getControl()).setTextLimit(100);
		editors[4] = textEditor;

		// Column 5 : Comitado (Free text)
		textEditor = new TextCellEditor(table);
		((Text) textEditor.getControl()).setTextLimit(100);
		editors[5] = textEditor;

		// Assign the cell editors to the viewer
		tableViewer.setCellEditors(editors);

		// Set the cell modifier for the viewer
		tableViewer.setCellModifier(new ProMergeCellModifier(this));

		// Set the default sorter for the viewer
		tableViewer.setSorter(new ProMergeTaskSorter(ProMergeTaskSorter.ARQUIVO));

	}

	/*
	 * Close the window and dispose of resources
	 */
	public void close() {
		Shell shell = table.getShell();

		if (shell != null && !shell.isDisposed())
			shell.dispose();
	}

	/**
	 * InnerClass that acts as a proxy for the ExampleTaskList providing content for the Table. It implements the ITaskListViewer interface since it
	 * must register changeListeners with the ExampleTaskList
	 */
	class ExampleContentProvider implements IStructuredContentProvider, IProMergeListViewer {

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			if (newInput != null)
				((ProMergeTaskList) newInput).addChangeListener(this);
			if (oldInput != null)
				((ProMergeTaskList) oldInput).removeChangeListener(this);
		}

		public void dispose() {
			taskList.removeChangeListener(this);
		}

		// Return the tasks as an array of Objects
		public Object[] getElements(Object parent) {
			return taskList.getTasks().toArray();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see ITaskListViewer#addTask(ExampleTask)
		 */
		public void addTask(ProMergeResumosConflitos task) {
			tableViewer.add(task);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see ITaskListViewer#removeTask(ExampleTask)
		 */
		public void removeTask(ProMergeResumosConflitos task) {
			tableViewer.remove(task);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see ITaskListViewer#updateTask(ExampleTask)
		 */
		public void updateTask(ProMergeResumosConflitos task) {
			tableViewer.update(task, null);
		}

		public void removeAll() {
			taskList.removeAll();
			tableViewer.refresh();
		}
	}

	/**
	 * Add the "Add", "Delete" and "Close" buttons
	 * 
	 * @param parent
	 *            the parent composite
	 */
	private void createButtons(Composite parent) {

		// Create and configure the "Add" button
		Button add = new Button(parent, SWT.PUSH | SWT.CENTER);
		add.setText("Add");

		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridData.widthHint = 80;
		add.setLayoutData(gridData);
		add.addSelectionListener(new SelectionAdapter() {

			// Add a task to the ExampleTaskList and refresh the view
			public void widgetSelected(SelectionEvent e) {
				taskList.addTask();
			}
		});

		// Create and configure the "Delete" button
		Button delete = new Button(parent, SWT.PUSH | SWT.CENTER);
		delete.setText("Delete");
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridData.widthHint = 80;
		delete.setLayoutData(gridData);

		delete.addSelectionListener(new SelectionAdapter() {

			// Remove the selection and refresh the view
			public void widgetSelected(SelectionEvent e) {
				ProMergeResumosConflitos task = (ProMergeResumosConflitos) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
				if (task != null) {
					taskList.removeTask(task);
				}
			}
		});

		// Create and configure the "Close" button
		closeButton = new Button(parent, SWT.PUSH | SWT.CENTER);
		closeButton.setText("Close");
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gridData.widthHint = 80;
		closeButton.setLayoutData(gridData);
	}

	/**
	 * Return the column names in a collection
	 * 
	 * @return List containing column names
	 */
	public java.util.List getColumnNames() {
		return Arrays.asList(columnNames);
	}

	/**
	 * @return currently selected item
	 */
	public ISelection getSelection() {
		return tableViewer.getSelection();
	}

	/**
	 * Return the ExampleTaskList
	 */
	public ProMergeTaskList getTaskList() {
		return taskList;
	}

	/**
	 * Return the parent composite
	 */
	public Control getControl() {
		return table.getParent();
	}

	/**
	 * Return the 'close' Button
	 */
	public Button getCloseButton() {
		return closeButton;
	}

	public TableViewer getTableViewer() {
		return tableViewer;
	}

	public void setTableViewer(TableViewer tableViewer) {
		this.tableViewer = tableViewer;
	}
}