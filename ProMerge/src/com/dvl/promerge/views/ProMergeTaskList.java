/**
 * (c) Copyright Mirasol Op'nWorks Inc. 2002, 2003. 
 * http://www.opnworks.com
 * Created on Apr 2, 2003 by lgauthier@opnworks.com
 * 
 */

package com.dvl.promerge.views;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import com.dvl.core.entitys.ProMergeResumosConflitos;

/**
 * Class that plays the role of the domain model in the TableViewerExample In real life, this class would access a persistent store of some kind.
 * 
 */

public class ProMergeTaskList {

	private final int COUNT = 10;
	private Vector tasks = new Vector(COUNT);
	private Set changeListeners = new HashSet();

	/**
	 * Constructor
	 */
	public ProMergeTaskList() {
		super();
		this.initData();
	}

	/*
	 * Initialize the table data. Create COUNT tasks and add them them to the collection of tasks
	 */
	private void initData() {

	};

	/**
	 * Return the collection of tasks
	 */
	public Vector getTasks() {
		return tasks;
	}

	/**
	 * Add a new task to the collection of tasks
	 */
	public void addTask() {
		ProMergeResumosConflitos task = new ProMergeResumosConflitos();
		tasks.add(tasks.size(), task);
		Iterator iterator = changeListeners.iterator();
		while (iterator.hasNext())
			((IProMergeListViewer) iterator.next()).addTask(task);
	}

	/**
	 * @param task
	 */
	public void removeTask(ProMergeResumosConflitos task) {
		tasks.remove(task);
		Iterator iterator = changeListeners.iterator();
		while (iterator.hasNext())
			((IProMergeListViewer) iterator.next()).removeTask(task);
	}

	/**
	 * @param task
	 */
	public void taskChanged(ProMergeResumosConflitos task) {
		Iterator iterator = changeListeners.iterator();
		while (iterator.hasNext())
			((IProMergeListViewer) iterator.next()).updateTask(task);
	}

	public void removeAll() {
		tasks = new Vector(COUNT);
	}

	/**
	 * @param viewer
	 */
	public void removeChangeListener(IProMergeListViewer viewer) {
		changeListeners.remove(viewer);
	}

	/**
	 * @param viewer
	 */
	public void addChangeListener(IProMergeListViewer viewer) {
		changeListeners.add(viewer);
	}

}
