package com.dvl.promerge.views;

import com.dvl.core.entitys.ProMergeResumosConflitos;

/*
 * (c) Copyright Mirasol Op'nWorks Inc. 2002, 2003. 
 * http://www.opnworks.com
 * Created on Jun 11, 2003 by lgauthier@opnworks.com
 *
 */

public interface IProMergeListViewer {

	/**
	 * Update the view to reflect the fact that a task was added to the task list
	 * 
	 * @param task
	 */
	public void addTask(ProMergeResumosConflitos task);

	/**
	 * Update the view to reflect the fact that a task was removed from the task list
	 * 
	 * @param task
	 */
	public void removeTask(ProMergeResumosConflitos task);

	/**
	 * Update the view to reflect the fact that one of the tasks was modified
	 * 
	 * @param task
	 */
	public void updateTask(ProMergeResumosConflitos task);
}
