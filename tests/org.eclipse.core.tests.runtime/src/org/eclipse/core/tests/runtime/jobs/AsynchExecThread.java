/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.runtime.jobs;

//import junit.framework.Assert;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * A runnable class that executes the given job and calls done when it is finished
 */
public class AsynchExecThread extends Thread {
	private IProgressMonitor current;
	private Job job;
	private int ticks;
	private int tickLength;
	private String jobName;
	private int[] status;
	private int index;
	
	public AsynchExecThread(IProgressMonitor current, Job job, int ticks, int tickLength, String jobName, int [] status, int index) {
		this.current = current;
		this.job = job;
		this.ticks = ticks;
		this.tickLength = tickLength;
		this.jobName = jobName;
		this.status = status;
		this.index = index;
	}
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		//wait until the main testing method allows this thread to run
		StatusChecker.waitForStatus(status, index, StatusChecker.STATUS_WAIT_FOR_RUN, 100);
		
		//set the current thread as the execution thread
		job.setThread(Thread.currentThread());
		
		status[index] = StatusChecker.STATUS_RUNNING;
		
		//wait until this job is allowed to run by the tester
		StatusChecker.waitForStatus(status, index, StatusChecker.STATUS_WAIT_FOR_DONE, 100);
		
		//must have positive work
		current.beginTask(jobName, ticks <= 0 ? 1 : ticks);
		try {
			
			for (int i = 0; i < ticks; i++) {
				current.subTask("Tick: " + i);
				if (current.isCanceled()) {
					status[index] = StatusChecker.STATUS_DONE;
					job.done(Status.CANCEL_STATUS);
				}
				try {
					//Thread.yield();
					Thread.sleep(tickLength);
				} catch (InterruptedException e) {
				
				}
				current.worked(1);
			}
			if (ticks <= 0)
				current.worked(1);
		} finally {
			status[index] = StatusChecker.STATUS_DONE;
			current.done();
			job.done(Status.OK_STATUS);
		}
	}
	

}